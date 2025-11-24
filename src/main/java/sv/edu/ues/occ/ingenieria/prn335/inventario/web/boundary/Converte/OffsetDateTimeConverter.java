package sv.edu.ues.occ.ingenieria.prn335.inventario.web.boundary.Converte;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@FacesConverter(value = "offsetDateTimeConverter", managed = false)
public class OffsetDateTimeConverter implements Converter<OffsetDateTime> {

    private static final ZoneId ZONE = ZoneId.of("America/El_Salvador");

    @Override
    public OffsetDateTime getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            LocalDateTime ldt = LocalDateTime.parse(value.trim(),
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            return ldt.atZone(ZONE).toOffsetDateTime();
        } catch (Exception e) {
            throw new ConverterException("Formato de fecha inválido: " + value);
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, OffsetDateTime value) {
        if (value == null) {
            return "";
        }
        return value.atZoneSameInstant(ZONE).toLocalDateTime()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }
}
