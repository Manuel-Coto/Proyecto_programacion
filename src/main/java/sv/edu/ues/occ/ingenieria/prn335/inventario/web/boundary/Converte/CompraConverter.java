package sv.edu.ues.occ.ingenieria.prn335.inventario.web.boundary.Converte;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;
import jakarta.enterprise.inject.spi.CDI;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.CompraDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.Compra;
import java.util.logging.Level;
import java.util.logging.Logger;

@FacesConverter(value = "compraConverter")
public class CompraConverter implements Converter<Compra> {

    private static final Logger LOGGER = Logger.getLogger(CompraConverter.class.getName());

    @Override
    public Compra getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            CompraDAO compraDAO = CDI.current().select(CompraDAO.class).get();
            Long id = Long.parseLong(value.trim());
            Compra compra = compraDAO.findById(id);

            if (compra == null) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                        "Compra no encontrada: " + id);
                throw new ConverterException(msg);
            }
            return compra;
        } catch (NumberFormatException e) {
            LOGGER.log(Level.SEVERE, "ID inválido: {0}", value);
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                    "Formato de ID inválido");
            throw new ConverterException(msg, e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en CompraConverter: {0}", e.getMessage());
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                    e.getMessage());
            throw new ConverterException(msg, e);
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Compra value) {
        if (value == null || value.getId() == null) {
            return "";
        }
        return value.getId().toString();
    }
}
