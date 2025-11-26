package sv.edu.ues.occ.ingenieria.prn335.inventario.web.boundary.Converte;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Conversor seguro para Long <-> Long en BD.
 * - Desactiva autoApply para evitar aplicarlo globalmente y provocar ClassCastException.
 * - Maneja nulls de forma segura.
 */
@Converter(autoApply = false)
public class LongIntegerAttributeConverter implements AttributeConverter<Long, Long> {

    @Override
    public Long convertToDatabaseColumn(Long attribute) {
        return attribute; // si la BD espera Integer, anotar explícitamente el campo o adaptar según necesidad
    }

    @Override
    public Long convertToEntityAttribute(Long dbData) {
        return dbData;
    }
}
