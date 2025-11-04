package utils;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;

import java.util.regex.Pattern;

public class FormValidator {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("^[0-9]+$");
    // Nombre de Usuario: No debe contener espacios en blanco (\s) en ninguna parte.
    private static final Pattern NO_SPACES_PATTERN = Pattern.compile("^\\S+$");

    // Contraseña Fuerte:
    // ^                 # Inicio de la cadena
    // (?=.*[A-Z])       # Debe contener al menos una letra mayúscula
    // (?=.*[.,@$!%*?&]) # Debe contener al menos un caracter especial (puedes expandir esta lista)
    // .{6,}             # Debe tener un mínimo de 6 caracteres de longitud
    // $                 # Fin de la cadena
    private static final Pattern STRONG_PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[A-Z])(?=.*[.,@$!%*?&])(?=.*[a-z]).{6,}$"
    );
    private static final String ERROR_STYLE_CLASS_TEXT_FIELD = "fieldTextError";
    private static final String ERROR_STYLE_CLASS_COMBO_BOX = "comboBoxError";
    private static final String STYLE_CLASS_TEXT_FIELD  = "fieldText";
    private static final String STYLE_CLASS_COMBO_BOX = "comboBox";

    /**
     * Valida si un TextField está vacío y/o no cumple con un patrón de RegEx.
     * @param textField El campo a validar.
     * @param regexPattern El patrón de expresión regular (puede ser null para omitir el chequeo de formato).
     * @return true si es válido (no vacío Y cumple el formato), false si falla.
     */
    public static boolean validateTextField(TextField textField, Pattern regexPattern) {
        String text = textField.getText();
        boolean isInvalid = false;

        // 1. Chequeo de campo vacío (Obligatorio)
        if (text == null || text.isBlank()) {
            isInvalid = true;
        }

        // 2. Chequeo de formato (Si se proporciona un patrón)
        else if (regexPattern != null && !regexPattern.matcher(text).matches()) {
            isInvalid = true;
        }

        // Aplicar estilos
        if (isInvalid) {
            applyErrorStyle(textField);
            return false;
        } else {
            removeErrorStyle(textField);
            return true;
        }
    }

    /**
     * Valida si un ComboBox tiene un valor seleccionado (no nulo).
     * @param comboBox El ComboBox a validar.
     * @return true si es válido (tiene selección), false si es inválido.
     */
    public static boolean validateComboBox(ComboBox<?> comboBox) {
        boolean isInvalid = (comboBox.getValue() == null);

        if (isInvalid) {
            applyErrorStyle(comboBox);
            return false;
        } else {
            removeErrorStyle(comboBox);
            return true;
        }
    }

    private static void applyErrorStyle(Control control) {
        // 1. Lógica Específica para TextField
        if (control instanceof TextField) {
            TextField tf = (TextField) control;
            tf.getStyleClass().add(ERROR_STYLE_CLASS_TEXT_FIELD);

            // 2. Lógica Específica para ComboBox
        } else if (control instanceof ComboBox) {
            ComboBox<?> cb = (ComboBox<?>) control;
            cb.getStyleClass().add(ERROR_STYLE_CLASS_COMBO_BOX);
        }
    }

    private static void removeErrorStyle(Control control) {
        // 1. Lógica Específica para TextField
        if (control instanceof TextField) {
            TextField tf = (TextField) control;
            tf.getStyleClass().remove(ERROR_STYLE_CLASS_TEXT_FIELD);
            tf.getStyleClass().add(STYLE_CLASS_TEXT_FIELD);

            // 2. Lógica Específica para ComboBox
        } else if (control instanceof ComboBox) {
            ComboBox<?> cb = (ComboBox<?>) control;
            cb.getStyleClass().remove(ERROR_STYLE_CLASS_COMBO_BOX);
            cb.getStyleClass().add(STYLE_CLASS_COMBO_BOX);
        }
    }

    // Métodos estáticos para acceder a los patrones
    public static Pattern getEmailPattern() {
        return EMAIL_PATTERN;
    }

    public static Pattern getNumericPattern() {
        return NUMERIC_PATTERN;
    }

    public static Pattern getStrongPasswordPattern() {
        return STRONG_PASSWORD_PATTERN;
    }

    public static Pattern getNoSpacesPattern() {
        return NO_SPACES_PATTERN;
    }
}
