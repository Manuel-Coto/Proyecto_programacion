package sv.edu.ues.occ.ingenieria.prn335.inventario.web.boundary.Converte;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

@FacesConverter("offsetDateTimeConverter")
public class OffsetDateTimeConverter implements Converter<OffsetDateTime> {

    private static final Logger LOGGER = Logger.getLogger(OffsetDateTimeConverter.class.getName());
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final ZoneOffset ZONE = ZoneOffset.of("-06:00");

    @Override
    public OffsetDateTime getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            LOGGER.log(Level.INFO, "Valor nulo recibido en OffsetDateTimeConverter");
            return null;
        }

        try {
            LOGGER.log(Level.INFO, "Convirtiendo valor a OffsetDateTime: {0}", value);
            LocalDateTime ldt = LocalDateTime.parse(value, FORMATTER);
            OffsetDateTime odt = ldt.atOffset(ZONE);
            LOGGER.log(Level.INFO, "Convertido exitosamente a: {0}", odt);
            return odt;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al convertir a OffsetDateTime: {0}", e.getMessage());
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, OffsetDateTime value) {
        if (value == null) {
            LOGGER.log(Level.INFO, "Valor nulo en getAsString de OffsetDateTimeConverter");
            return "";
        }

        try {
            String resultado = value.format(FORMATTER);
            LOGGER.log(Level.INFO, "OffsetDateTime convertido a String: {0}", resultado);
            return resultado;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al convertir OffsetDateTime a String: {0}", e.getMessage());
            return "";
        }
    }
}
