package dev.portfolio.requestdesk.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AgentSeed {
    @Bean CommandLineRunner createAgent(UserRepository users, PasswordEncoder encoder,
        @Value("${DEMO_AGENT_EMAIL:agent@example.com}") String email,
        @Value("${DEMO_AGENT_PASSWORD:}") String password) {
        return args -> {
            if (password == null || password.length() < 8)
                throw new IllegalStateException("DEMO_AGENT_PASSWORD must have at least 8 characters");

            String normalized = email.trim().toLowerCase(java.util.Locale.ROOT);
            if (!users.existsByEmail(normalized))
                users.save(new AppUser(normalized, encoder.encode(password), Role.AGENT));
        };
    }
}
