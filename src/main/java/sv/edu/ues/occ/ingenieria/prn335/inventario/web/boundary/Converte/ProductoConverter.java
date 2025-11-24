package sv.edu.ues.occ.ingenieria.prn335.inventario.web.boundary.Converte;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;
import jakarta.enterprise.inject.spi.CDI;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.ProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.Producto;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@FacesConverter(value = "productoConverter")
public class ProductoConverter implements Converter<Producto> {

    private static final Logger LOGGER = Logger.getLogger(ProductoConverter.class.getName());

    @Override
    public Producto getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            ProductoDAO productoDAO = CDI.current().select(ProductoDAO.class).get();
            UUID id = UUID.fromString(value.trim());
            Producto producto = productoDAO.findById(id);

            if (producto == null) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                        "Producto no encontrado: " + id);
                throw new ConverterException(msg);
            }
            return producto;
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.SEVERE, "UUID inválido: {0}", value);
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                    "Formato de ID inválido");
            throw new ConverterException(msg, e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en ProductoConverter: {0}", e.getMessage());
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                    e.getMessage());
            throw new ConverterException(msg, e);
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Producto value) {
        if (value == null || value.getId() == null) {
            return "";
        }
        return value.getId().toString();
    }
}
