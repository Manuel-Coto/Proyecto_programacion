package sv.edu.ues.occ.ingenieria.prn335.inventario.web.boundary.servlet;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.InventarioDefaultDataAccess;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.TipoAlmacenDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.TipoAlmacen;

import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class TipoAlmacenFrm extends DefaultFrm<TipoAlmacen> implements Serializable {

    @Inject
    FacesContext facesContext;

    @Inject
    TipoAlmacenDAO tipoAlmacenDAO;

    private List<TipoAlmacen> listaTipoAlmacen;

    @PostConstruct
    public void init() {
        this.nombreBean = "Tipo Almacen";
        this.listaTipoAlmacen = tipoAlmacenDAO.findAll();
    }

    @Override
    protected FacesContext getFacesContext() {
        return facesContext;
    }

    @Override
    protected InventarioDefaultDataAccess<TipoAlmacen> getDao() {
        return tipoAlmacenDAO;
    }

    @Override
    protected TipoAlmacen nuevoRegistro() {
        TipoAlmacen tipoAlmacen = new TipoAlmacen();
        return tipoAlmacen;
    }

    @Override
    protected TipoAlmacen buscarRegistroPorId(Object id) {
        if (id != null && id instanceof Integer) {
            for (TipoAlmacen ta : listaTipoAlmacen) {
                if (ta.getId().equals(id)) {
                    return ta;
                }
            }
        }
        return null;
    }


    @Override
    protected String getIdAsText(TipoAlmacen r) {
        if (r != null && r.getId() != null) {
            return r.getId().toString();
        }
        return null;
    }

    @Override
    protected TipoAlmacen getIdByText(String id) {
        if (id != null) {
            try {
                Integer buscado = Integer.parseInt(id);
                for (TipoAlmacen ta : listaTipoAlmacen) {
                    if (ta.getId().equals(buscado)) {
                        return ta;
                    }
                }
            } catch (NumberFormatException e) {
                System.err.println("ID no es un número válido: " + id);
            }
        }
        return null;
    }

}
