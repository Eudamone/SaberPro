package services;

import dto.UsuarioSession;
import model.Usuario;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import repository.UsuarioRepository;

import java.util.Optional;

@Service
public class AuthService {
    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;

    public AuthService(UsuarioRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    //Validates login credentials and returns the user if valid, otherwise null.

    public UsuarioSession login(String username, String rawPassword) {
        if (username == null || rawPassword == null) return null;

        Optional<UsuarioRepository.Credentials> credentials = repository.findCredentialsByUsername(username.trim());

        return credentials.filter(c -> safeMatches(rawPassword,c.getPass()))
                .map(c -> new UsuarioSession(c.getId(),c.getUsername(),c.getRol()))
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
