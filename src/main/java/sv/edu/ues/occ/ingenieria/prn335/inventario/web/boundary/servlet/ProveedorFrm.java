package sv.edu.ues.occ.ingenieria.prn335.inventario.web.boundary.servlet;

import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.InventarioDefaultDataAccess;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.ProveedorDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.Proveedor;

@Named("proveedorFrm")
@ViewScoped
public class ProveedorFrm extends DefaultFrm<Proveedor> implements Serializable {
    private static final long serialVersionUID = 1L;
    @Inject
    private ProveedorDAO proveedorDAO;

    public ProveedorFrm() {
        this.nombreBean = "Proveedor";
    }

    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    protected InventarioDefaultDataAccess<Proveedor> getDao() {
        return this.proveedorDAO;
    }

    protected Proveedor nuevoRegistro() {
        Proveedor p = new Proveedor();
        p.setActivo(Boolean.TRUE);
        return p;
    }

    protected Proveedor buscarRegistroPorId(Object id) {
        if (id instanceof Integer) {
            return this.proveedorDAO.buscarRegistroPorId((Integer)id);
        } else if (id instanceof String) {
            String s = (String)id;

            try {
                Integer iid = Integer.valueOf(s);
                return this.proveedorDAO.buscarRegistroPorId(iid);
            } catch (NumberFormatException var4) {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public void inicializarListas() {
    }

    protected String getIdAsText(Proveedor r) {
        return r != null && r.getId() != null ? String.valueOf(r.getId()) : null;
    }

    protected Proveedor getIdByText(String id) {
        if (id != null && !id.isBlank()) {
            try {
                Integer iid = Integer.valueOf(id);
                return this.proveedorDAO.buscarRegistroPorId(iid);
            } catch (NumberFormatException var3) {
                return null;
            }
        } else {
            return null;
        }
    }
}
