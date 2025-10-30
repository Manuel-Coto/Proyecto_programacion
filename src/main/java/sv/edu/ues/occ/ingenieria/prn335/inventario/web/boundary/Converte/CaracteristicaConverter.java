package sv.edu.ues.occ.ingenieria.prn335.inventario.web.boundary.Converte;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.CaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.Caracteristica;

@ApplicationScoped
@FacesConverter(value = "caracteristicaConverter", managed = true)
public class CaracteristicaConverter implements Converter<Caracteristica> {

    @Inject
    CaracteristicaDAO caracteristicaDAO;

    @Override
    public String getAsString(FacesContext ctx, UIComponent c, Caracteristica v) {
        return (v == null || v.getId() == null) ? "" : String.valueOf(v.getId());
    }

    @Override
    public Caracteristica getAsObject(FacesContext ctx, UIComponent c, String v) {
        if (v == null || v.isBlank()) return null;
        try {
            Integer id = Integer.valueOf(v);
            return caracteristicaDAO.getEntityManager().find(Caracteristica.class, id);
        } catch (Exception e) {
            return null;
        }
    }
}
