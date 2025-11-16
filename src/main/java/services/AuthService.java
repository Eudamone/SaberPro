package services;

import application.SessionContext;
import dto.UsuarioInfoDTO;
import dto.UsuarioSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import repository.UsuarioRepository;

import java.util.Optional;

@Service
public class AuthService {
    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;
    private final SessionContext sessionContext;

    public AuthService(UsuarioRepository repository, PasswordEncoder encoder, SessionContext sessionContext) {
        this.repository = repository;
        this.encoder = encoder;
        this.sessionContext = sessionContext;
    }

    //Validates login credentials and returns the user if valid, otherwise null.

    public UsuarioSession login(String username, String rawPassword) {
        if (username == null || rawPassword == null) return null;

        Optional<UsuarioRepository.Credentials> credentials = repository.findCredentialsByUsername(username.trim());

        if (credentials.isPresent()) {
            UsuarioInfoDTO userCurrent = repository.findUserInfo(credentials.get().getId()).orElse(null);
            if (userCurrent != null) {
                sessionContext.setCurrentUser(userCurrent);
            }else{
                throw new RuntimeException("Usuario no encontrado para establecer usuario logueado");
            }
        }

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
