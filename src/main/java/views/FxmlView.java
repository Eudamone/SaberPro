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
    CHANGE_PASSWORD {
        @Override
        public String getTitle(){return "Cambiar contraseña - Saber Pro";}
        @Override
        public String getFxmlFile(){return "/views/changePassword.fxml";}
    },
    DASHBOARD_DEAN{
        @Override
        public String getTitle(){return "Dashboard Decano - Saber Pro";}
        @Override
        public String getFxmlFile(){return "/views/Dean/dashboardDean.fxml";}
    },
    DASHBOARD_USER {
        @Override
        public String getTitle() { return "Dashboard - User"; }
        @Override
        public String getFxmlFile() { return "/views/dashboardAdmin.fxml"; }
        // o cambia a "/views/dashboardUser.fxml" si más adelante haces uno separado
    },
    CREATE_USER_DEAN{
        @Override
        public String getTitle() { return "Crear Usuario - Saber Pro"; }
        @Override
        public String getFxmlFile() { return "/views/Dean/createUser.fxml"; }
    },
    LOAD_MASSIVE_USERS_DEAN{
        @Override
        public String getTitle() { return "Carga masiva de usuarios - Saber Pro"; }
        @Override
        public String getFxmlFile() { return "/views/Dean/loadMassiveUsers.fxml"; }
    },
    EDIT_AND_DELETE_USERS_DEAN{
        @Override
        public String getTitle() { return "Editar usuarios - Saber Pro"; }
        @Override
        public String getFxmlFile() { return "/views/Dean/editAndDeleteUsers.fxml"; }
    },
    CONSULT_RESULTS_DEAN{
        @Override
        public String getTitle() { return "Consulta Resultados - Saber Pro"; }
        @Override
        public String getFxmlFile() { return "/views/Dean/consultResultsDean.fxml"; }
    },
    RESULTS_STUDENT{
        @Override
        public String getTitle() { return "Dashboard Estudiante - Saber Pro"; }
        @Override
        public String getFxmlFile() { return "/views/Student/resultsStudent.fxml"; }
    },
    FREQUENTLY_QUESTIONS_STUDENT{
        @Override
        public String getTitle() { return "Preguntas frecuentes - Saber Pro"; }
        @Override
        public String getFxmlFile() {return "/views/Student/frequentlyQuestionsStudent.fxml";}
    },
    USER_MANUAL_STUDENT{
        @Override
        public String getTitle() { return "Manual de usuario - Saber Pro"; }
        @Override
        public String getFxmlFile() { return "/views/Student/userManualStudent.fxml"; }
    },
    LOAD_RESULTS_ADMIN{
        @Override
        public String getTitle() {return "Carga de resultados - Saber Pro";}
        @Override
        public String getFxmlFile() {return "/views/Admin/loadResultsAdmin.fxml";}
    },
    DASHBOARD_ADMIN{
        @Override
        public String getTitle() { return "Dashboard Administrador - Saber Pro"; }
        @Override
        public String getFxmlFile() { return "/views/Admin/dashboardAdmin.fxml"; }
    },
    CREATE_USER_ADMIN{
        @Override
        public String getTitle() { return "Crear Administrador - Saber Pro"; }
        @Override
        public String getFxmlFile() { return "/views/Admin/createUserAdmin.fxml"; }
    },
    EDIT_AND_DELETE_USERS_ADMIN{
        @Override
        public String getTitle() { return "Editar Administrador - Saber Pro"; }
        @Override
        public String getFxmlFile() {return "/views/Admin/editAndDeleteUsersAdmin.fxml";}
    }
    ,LOAD_MASSIVE_USERS_ADMIN{
        @Override
        public String getTitle() { return "Carga masiva de usuarios - Saber Pro"; }
        @Override
        public String getFxmlFile() {return "/views/Admin/loadMassiveUsersAdmin.fxml";}
    }
    ;

    public abstract String getTitle();
    public abstract String getFxmlFile();
}
