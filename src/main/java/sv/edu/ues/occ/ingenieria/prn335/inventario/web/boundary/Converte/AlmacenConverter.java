package sv.edu.ues.occ.ingenieria.prn335.inventario.web.boundary.Converte;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.AlmacenDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.Almacen;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@FacesConverter(value = "almacenConverter", managed = true)
public class AlmacenConverter implements Converter<Almacen> {

    private static final Logger LOGGER = Logger.getLogger(AlmacenConverter.class.getName());

    @Inject
    private AlmacenDAO almacenDAO;

    @Override
    public Almacen getAsObject(FacesContext context, UIComponent component, String value) {
        // Si value es null o vacío, retornar null
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            // Convertir el string a UUID
            UUID id = UUID.fromString(value.trim());

            // Buscar el almacén en la base de datos
            Almacen almacen = almacenDAO.findById(id);

            if (almacen != null) {
                LOGGER.log(Level.INFO, "Almacén encontrado: {0}", id);
                return almacen;
            } else {
                LOGGER.log(Level.WARNING, "Almacén NO encontrado con ID: {0}", id);
                FacesMessage msg = new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Almacén no válido",
                        "El almacén seleccionado no existe en la base de datos"
                );
                throw new ConverterException(msg);
            }

        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.SEVERE, "UUID inválido: {0}", value);
            FacesMessage msg = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Error de conversión",
                    "El ID del almacén no tiene un formato válido"
            );
            throw new ConverterException(msg, e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al convertir Almacén: {0}", e.getMessage());
            FacesMessage msg = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Error al procesar almacén",
                    "Ocurrió un error al buscar el almacén: " + e.getMessage()
            );
            throw new ConverterException(msg, e);
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Almacen almacen) {
        // Si el objeto es null, retornar string vacío
        if (almacen == null) {
            return "";
        }

        // Si el ID es null, retornar string vacío
        if (almacen.getId() == null) {
            return "";
        }

        // Retornar el ID como string
        String id = almacen.getId().toString();
        LOGGER.log(Level.INFO, "Convertiendo Almacén a String: {0}", id);
        return id;
    }
}