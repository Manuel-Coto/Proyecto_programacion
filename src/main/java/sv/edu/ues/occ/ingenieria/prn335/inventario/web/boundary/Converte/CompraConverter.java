package sv.edu.ues.occ.ingenieria.prn335.inventario.web.boundary.Converte;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.CompraDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.Compra;

import java.util.logging.Level;
import java.util.logging.Logger;

@FacesConverter(value = "compraConverter")
public class CompraConverter implements Converter<Compra> {

    private static final Logger LOGGER = Logger.getLogger(CompraConverter.class.getName());

    private CompraDAO getCompraDAO() {
        return CDI.current().select(CompraDAO.class).get();
    }

    @Override
    public Compra getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            LOGGER.log(Level.INFO, "Valor nulo o vacío recibido en CompraConverter");
            return null;
        }

        try {
            Long id = Long.parseLong(value.trim());
            LOGGER.log(Level.INFO, "Buscando Compra con ID: {0}", id);

            Compra compra = getCompraDAO().findById(id);

            if (compra != null) {
                LOGGER.log(Level.INFO, "Compra encontrada exitosamente: {0}", id);
                return compra;
            } else {
                LOGGER.log(Level.WARNING, "Compra NO encontrada con ID: {0}", id);
                FacesMessage msg = new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Compra no válida",
                        "La compra seleccionada no existe en la base de datos (ID: " + id + ")"
                );
                throw new ConverterException(msg);
            }

        } catch (NumberFormatException e) {
            LOGGER.log(Level.SEVERE, "ID inválido en CompraConverter: {0}", value);
            FacesMessage msg = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Error de conversión",
                    "El ID de la compra no tiene un formato válido: " + value
            );
            throw new ConverterException(msg, e);
        } catch (ConverterException ce) {
            throw ce;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error inesperado en CompraConverter: {0}", e.getMessage());
            FacesMessage msg = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Error al procesar compra",
                    "Error: " + e.getMessage()
            );
            throw new ConverterException(msg, e);
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Compra compra) {
        if (compra == null) {
            return "";
        }

        if (compra.getId() == null) {
            LOGGER.log(Level.WARNING, "Compra sin ID en getAsString");
            return "";
        }

        String idString = compra.getId().toString();
        LOGGER.log(Level.INFO, "Convertiendo Compra a String: {0}", idString);
        return idString;
    }
}
