package application;

import application.SaberProUser;
import application.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository repository;
    private final PasswordEncoder encoder;

    public AuthService(UserRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    //Validates login credentials and returns the user if valid, otherwise null.

    public SaberProUser login(String username, String rawPassword) {
        if (username == null || rawPassword == null) return null;

        return repository.findByUsername(username.trim())       //consulta
                .filter(u -> safeMatches(rawPassword, u.getPassword()))
                .orElse(null);
    }

    private boolean safeMatches(String raw, String stored) {
        if (stored == null || stored.isBlank()) return false;
        try {
            return encoder.matches(raw, stored);
        } catch (Exception e) {
            return false;
        }
    }
}


