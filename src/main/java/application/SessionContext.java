package application;

import dto.UsuarioInfoDTO;
import org.springframework.stereotype.Component;

@Component
public class SessionContext {
    private UsuarioInfoDTO currentUser;
    private UsuarioInfoDTO userTemp;

    public void setCurrentUser(UsuarioInfoDTO currentUser) {
        this.currentUser = currentUser;
    }

    public UsuarioInfoDTO getCurrentUser() {
        return currentUser;
    }

    public void logout() {
        this.currentUser = null;
    }

    public void setUserTemp(UsuarioInfoDTO userTemp) {
        this.userTemp = userTemp;
    }

    public UsuarioInfoDTO getUserTemp() {
        return userTemp;
    }

    public void clearUserTemp() {
        this.userTemp = null;
    }
}