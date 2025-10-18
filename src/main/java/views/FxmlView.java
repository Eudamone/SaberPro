package views;


public enum FxmlView {
    LOGIN{
        @Override
        public String getTitle(){
            return "Login - Saber Pro";
        }
        @Override
        public String getFxmlFile(){
            return "/views/login.fxml";
        }
    },
    RECOVER_PASSWORD{
        @Override
        public String getTitle(){return "Recuperar contraseña - Saber Pro";}
        @Override
        public String getFxmlFile(){return "/views/recoverPassword.fxml";}
    },
    RECOVER_PASSWORD_CONFIRMATION{
        @Override
        public String getTitle(){return "Confirmación de recuperación - Saber Pro";}
        @Override
        public String getFxmlFile(){return "/views/recoverPasswordFinal.fxml";}
    };

    public abstract String getTitle();
    public abstract String getFxmlFile();
}
