package sv.edu.ues.occ.ingenieria.prn335.inventario.web.boundary.Converte;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

import java.util.UUID;

@FacesConverter("uuidConverter")
public class UUIDConverter implements Converter<UUID> {

    @Override
    public UUID getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("UUID inválido: " + value);
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, UUID value) {
        return value != null ? value.toString() : "";
    }
}
