package utils;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import model.Usuario;

@Converter(autoApply = true)
public class RolTypeConverter implements AttributeConverter<Usuario.rolType, String> {
    @Override
    public String convertToDatabaseColumn(Usuario.rolType rol) {
        if (rol == null) {
            return null;
        }
        return rol.getTipo();
    }
    @Override
    public Usuario.rolType convertToEntityAttribute(String rol) {
        if (rol == null) {
            return null;
        }
        return Usuario.rolType.fromTipo(rol);
    }
}
