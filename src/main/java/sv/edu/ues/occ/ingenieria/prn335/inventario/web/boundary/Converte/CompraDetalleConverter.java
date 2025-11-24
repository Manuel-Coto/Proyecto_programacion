package sv.edu.ues.occ.ingenieria.prn335.inventario.web.boundary.Converte;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.CompraDetalleDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.CompraDetalle;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@FacesConverter(value = "compraDetalleConverter", managed = true)
public class CompraDetalleConverter implements Converter<CompraDetalle> {

    private static final Logger LOGGER = Logger.getLogger(CompraDetalleConverter.class.getName());

    @Inject
    private CompraDetalleDAO compraDetalleDAO;

    @Override
    public CompraDetalle getAsObject(FacesContext context, UIComponent component, String value) {
        // Si value es null o vacío, retornar null (es válido para campos opcionales)
        if (value == null || value.trim().isEmpty()) {
            LOGGER.log(Level.INFO, "Valor nulo o vacío recibido en VentaDetalleConverter");
            return null;
        }

        try {
            // Convertir el string a UUID
            UUID id = UUID.fromString(value.trim());
            LOGGER.log(Level.INFO, "Buscando VentaDetalle con ID: {0}", id);

            // Buscar el detalle de venta en la base de datos
            CompraDetalle compraDetalle = compraDetalleDAO.findById(id);

            if (compraDetalle != null) {
                LOGGER.log(Level.INFO, "VentaDetalle encontrado exitosamente: {0}", id);
                return compraDetalle;
            } else {
                // El detalle de venta no existe en BD
                LOGGER.log(Level.WARNING, "VentaDetalle NO encontrado con ID: {0}", id);
                FacesMessage msg = new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Detalle de venta no válido",
                        "El detalle de venta seleccionado no existe en la base de datos (ID: " + id + ")"
                );
                throw new ConverterException(msg);
            }

        } catch (IllegalArgumentException e) {
            // UUID inválido
            LOGGER.log(Level.SEVERE, "UUID inválido en VentaDetalleConverter: {0}", value);
            FacesMessage msg = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Error de conversión",
                    "El ID del detalle de venta no tiene un formato válido: " + value
            );
            throw new ConverterException(msg, e);
        } catch (ConverterException ce) {
            // Re-lanzar ConverterException tal como está
            throw ce;
        } catch (Exception e) {
            // Cualquier otra excepción
            LOGGER.log(Level.SEVERE, "Error inesperado en VentaDetalleConverter: {0}", e.getMessage());
            e.printStackTrace();
            FacesMessage msg = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Error al procesar detalle de venta",
                    "Error: " + e.getMessage()
            );
            throw new ConverterException(msg, e);
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, CompraDetalle compraDetalle) {
        // Si el objeto es null, retornar string vacío
        if (compraDetalle == null) {
            return "";
        }

        // Si el ID es null, retornar string vacío
        if (compraDetalle.getId() == null) {
            LOGGER.log(Level.WARNING, "VentaDetalle sin ID en getAsString");
            return "";
        }

        // Retornar el ID como string
        String idString = compraDetalle.getId().toString();
        LOGGER.log(Level.INFO, "Convertiendo VentaDetalle a String: {0}", idString);
        return idString;
    }
}
