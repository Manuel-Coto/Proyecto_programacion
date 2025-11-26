package sv.edu.ues.occ.ingenieria.prn335.inventario.web.boundary.Converte;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.ProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.Producto;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@FacesConverter(value = "productoConverter")
public class ProductoConverter implements Converter<Producto> {

    private static final Logger LOGGER = Logger.getLogger(ProductoConverter.class.getName());

    private ProductoDAO getProductoDAO() {
        return CDI.current().select(ProductoDAO.class).get();
    }

    @Override
    public Producto getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            LOGGER.log(Level.INFO, "Valor nulo o vacío recibido en ProductoConverter");
            return null;
        }

        try {
            UUID id = UUID.fromString(value.trim());
            LOGGER.log(Level.INFO, "Buscando Producto con ID: {0}", id);

            Producto producto = getProductoDAO().findById(id);

            if (producto != null) {
                LOGGER.log(Level.INFO, "Producto encontrado exitosamente: {0}", id);
                return producto;
            } else {
                LOGGER.log(Level.WARNING, "Producto NO encontrado con ID: {0}", id);
                FacesMessage msg = new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Producto no válido",
                        "El producto seleccionado no existe en la base de datos (ID: " + id + ")"
                );
                throw new ConverterException(msg);
            }

        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.SEVERE, "UUID inválido en ProductoConverter: {0}", value);
            FacesMessage msg = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Error de conversión",
                    "El ID del producto no tiene un formato válido: " + value
            );
            throw new ConverterException(msg, e);
        } catch (ConverterException ce) {
            throw ce;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error inesperado en ProductoConverter: {0}", e.getMessage());
            FacesMessage msg = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Error al procesar producto",
                    "Error: " + e.getMessage()
            );
            throw new ConverterException(msg, e);
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Producto producto) {
        if (producto == null) {
            return "";
        }

        if (producto.getId() == null) {
            LOGGER.log(Level.WARNING, "Producto sin ID en getAsString");
            return "";
        }

        String idString = producto.getId().toString();
        LOGGER.log(Level.INFO, "Convertiendo Producto a String: {0}", idString);
        return idString;
    }
}
