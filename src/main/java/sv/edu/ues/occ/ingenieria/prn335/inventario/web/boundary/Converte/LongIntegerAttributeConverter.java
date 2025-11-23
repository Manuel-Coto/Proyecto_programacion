package sv.edu.ues.occ.ingenieria.prn335.inventario.web.boundary.Converte;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LongIntegerAttributeConverter implements AttributeConverter<Long, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Long attribute) {
        if (attribute == null) return null;
        return attribute.intValue();  // Convertir Long a Integer para la base de datos
    }

    @Override
    public Long convertToEntityAttribute(Integer dbData) {
        if (dbData == null) return null;
        return dbData.longValue();  // Convertir Integer a Long para la entidad
    }
}