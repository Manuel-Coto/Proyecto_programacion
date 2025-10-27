package sv.edu.ues.occ.ingenieria.prn335.inventario.web.boundary.converter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;

import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.TipoProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.TipoProducto;

@FacesConverter(value = "tipoProductoConverter", managed = true)
@ApplicationScoped
public class TipoProductoConverter implements Converter<TipoProducto> {

    @Inject
    TipoProductoDAO tipoProductoDAO;

    @Override
    public TipoProducto getAsObject(FacesContext ctx, UIComponent cmp, String value) {
        if (value == null || value.isBlank()) return null;
        try {
            Long id = Long.valueOf(value);
            return tipoProductoDAO.getEntityManager().find(TipoProducto.class, id);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext ctx, UIComponent cmp, TipoProducto value) {
        return (value != null && value.getId() != null) ? String.valueOf(value.getId()) : "";
    }
}
