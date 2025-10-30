package sv.edu.ues.occ.ingenieria.prn335.inventario.web.boundary.servlet;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.logging.Logger;

import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.InventarioDefaultDataAccess;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.TipoUnidadMedidaDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.TipoUnidadMedida;

@Named("tipoUnidadMedidaFrm")
@ViewScoped
public class TipoUnidadMedidaFrm extends DefaultFrm<TipoUnidadMedida> implements Serializable {

    private static final Logger LOG = Logger.getLogger(TipoUnidadMedidaFrm.class.getName());

    @Inject
    private TipoUnidadMedidaDAO tipoUnidadMedidaDao;

    @PostConstruct
    @Override
    public void inicializar() {
        this.nombreBean = "Tipo de Unidad de Medida";
        super.inicializar();
    }

    @Override
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    @Override
    protected InventarioDefaultDataAccess<TipoUnidadMedida> getDao() {
        return tipoUnidadMedidaDao;
    }

    @Override
    protected TipoUnidadMedida nuevoRegistro() {
        TipoUnidadMedida t = new TipoUnidadMedida();
        t.setActivo(Boolean.TRUE);
        t.setNombre("");
        t.setUnidadBase("");
        t.setComentarios("");
        return t;
    }

    @Override
    protected TipoUnidadMedida buscarRegistroPorId(Object id) {
        if (id == null) return null;
        try {
            Integer iid = (id instanceof Integer) ? (Integer) id : Integer.valueOf(String.valueOf(id));
            return tipoUnidadMedidaDao.getEntityManager().find(TipoUnidadMedida.class, iid);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected String getIdAsText(TipoUnidadMedida r) {
        return (r != null && r.getId() != null) ? String.valueOf(r.getId()) : null;
    }

    @Override
    protected TipoUnidadMedida getIdByText(String id) {
        if (id == null) return null;
        return buscarRegistroPorId(id);
    }
}
