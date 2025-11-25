// language: java
package sv.edu.ues.occ.ingenieria.prn335.inventario.web.boundary.Converte;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.ProductoTipoProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.ProductoTipoProducto;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
@FacesConverter(value = "productoTipoProductoConverter", managed = true)
public class ProductoTipoProductoConverter implements Converter<ProductoTipoProducto> {

    private static final Logger LOGGER = Logger.getLogger(ProductoTipoProductoConverter.class.getName());

    @Inject
    private ProductoTipoProductoDAO productoTipoProductoDAO;

    @Override
    public ProductoTipoProducto getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            // Si se usa formato "id||nombre" extraer la parte del id
            String raw = value.contains("||") ? value.substring(0, value.indexOf("||")).trim() : value.trim();
            UUID id = UUID.fromString(raw);
            // usar EntityManager directamente para evitar dependencias de métodos concretos
            return productoTipoProductoDAO.getEntityManager().find(ProductoTipoProducto.class, id);
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.SEVERE, "UUID inválido en ProductoTipoProductoConverter: {0}", value);
            throw new ConverterException("UUID inválido: " + value, e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al convertir ProductoTipoProducto", e);
            throw new ConverterException("Error al convertir ProductoTipoProducto", e);
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, ProductoTipoProducto value) {
        if (value == null || value.getId() == null) {
            return "";
        }
        return value.getId().toString();
    }
}
