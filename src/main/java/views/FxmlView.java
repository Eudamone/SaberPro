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
    },
    DASHBOARD_DEAN {
        @Override
        public String getTitle() { return "Dashboard - Dean"; }
        @Override
        public String getFxmlFile() { return "/views/dashboardDean.fxml"; }
    },

    DASHBOARD_USER {
        @Override
        public String getTitle() { return "Dashboard - User"; }
        @Override
        public String getFxmlFile() { return "/views/dashboardAdmin.fxml"; }
        // o cambia a "/views/dashboardUser.fxml" si más adelante haces uno separado
    };

    public abstract String getTitle();
    public abstract String getFxmlFile();
}
