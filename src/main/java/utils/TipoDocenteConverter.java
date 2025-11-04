package utils;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import model.Decano;

@Converter(autoApply = true)
public class TipoDocenteConverter implements AttributeConverter<Decano.tipoDocente, String> {
    @Override
    public String convertToDatabaseColumn(Decano.tipoDocente atributo) {
        if(atributo == null){
            return null;
        }
        // Convierte el ENUM a la etiqueta para guardar en la BD
        return atributo.getEtiqueta();
    }

    @Override
    public Decano.tipoDocente convertToEntityAttribute(String dbData) {
        if(dbData == null){
            return null;
        }
        // Se usa el método personalizado para encontrar el ENUM por la etiqueta
        return Decano.tipoDocente.fromEtiqueta(dbData);
    }
}
