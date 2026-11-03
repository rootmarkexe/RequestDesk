package dev.portfolio.requestdesk.security;

import dev.portfolio.requestdesk.user.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.Locale;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final TokenService tokens;
    public AuthService(UserRepository users, PasswordEncoder encoder, TokenService tokens) {
        this.users = users;
        this.encoder = encoder;
        this.tokens = tokens;
    }
    @Transactional
    public AuthController.AuthResponse register(AuthController.RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (users.existsByEmail(email)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Email уже зарегистрирован");

        AppUser user = users.save(new AppUser(email, encoder.encode(request.password()), Role.USER));
        return new AuthController.AuthResponse(tokens.issue(user.getEmail()), user.getEmail(), user.getRole().name());
    }
    @Transactional(readOnly = true)
    public AuthController.AuthResponse login(AuthController.LoginRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        AppUser user = users.findByEmail(email).orElseThrow(() -> badCredentials());
        if (!encoder.matches(request.password(), user.getPasswordHash())) throw badCredentials();

        return new AuthController.AuthResponse(tokens.issue(email), email, user.getRole().name());
    }
    @Transactional(readOnly = true)
    public AppUser currentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated())
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Необходимо войти");
        return users.findByEmail(authentication.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не найден"));
    }


    private ResponseStatusException badCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный email или пароль");
    }
}
