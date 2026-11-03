package dev.portfolio.requestdesk.security;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {
    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/auth/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return auth.register(request);
    }
    @PostMapping("/auth/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) { return auth.login(request); }

    @GetMapping("/me")
    public Map<String, String> me() {
        var user = auth.currentUser();
        return Map.of("email", user.getEmail(), "role", user.getRole().name());
    }


    public record RegisterRequest(@NotBlank @Email String email, @NotBlank @Size(min=8, max=72) String password) {}
    public record LoginRequest(@NotBlank String email, @NotBlank String password) {}
    public record AuthResponse(String token, String email, String role) {}
}
