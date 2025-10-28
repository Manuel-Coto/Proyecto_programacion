package sv.edu.ues.occ.ingenieria.prn335.inventario.web.boundary.servlet;

import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.PrimeFaces;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.ClienteDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.InventarioDefaultDataAccess;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.Cliente;

import java.io.Serializable;
import java.util.UUID;

@Named
@ViewScoped
public class ClienteFrm extends DefaultFrm<Cliente> implements Serializable {

    @Inject
    FacesContext facesContext;

    @Inject
    ClienteDAO clienteDAO;

    /** 0 = Lista, 1 = Detalle */
    private int activeIndex = 0;

    public ClienteFrm() {
        this.nombreBean = " Lista de Clientes";
    }

    // --- Getter/Setter requeridos por el XHTML ---
    public int getActiveIndex() { return activeIndex; }
    public void setActiveIndex(int activeIndex) { this.activeIndex = activeIndex; }

    @Override
    protected FacesContext getFacesContext() {
        return facesContext;
    }

    @Override
    protected InventarioDefaultDataAccess<Cliente> getDao() {
        return clienteDAO;
    }

    @Override
    protected Cliente nuevoRegistro() {
        Cliente c = new Cliente();
        c.setId(UUID.randomUUID());
        c.setActivo(Boolean.TRUE);
        c.setNombre("");
        c.setDui("");
        c.setNit("");
        return c;
    }

    @Override
    protected Cliente buscarRegistroPorId(Object id) {
        if (id != null && id instanceof UUID buscado && this.modelo != null && this.modelo.getWrappedData().isEmpty()) {
            for (Cliente cli : (Iterable<Cliente>) clienteDAO.findAll()) {
                if (buscado.equals(cli.getId())) return cli;
            }
        }
        return null;
    }

    @Override
    protected String getIdAsText(Cliente r) {
        return (r != null && r.getId() != null) ? r.getId().toString() : null;
    }

    @Override
    protected Cliente getIdByText(String id) {
        if (id != null && this.modelo != null && !this.modelo.getWrappedData().isEmpty()) {
            try {
                UUID buscado = UUID.fromString(id);
                return this.modelo.getWrappedData().stream()
                        .filter(r -> r.getId() != null && r.getId().equals(buscado))
                        .findFirst().orElse(null);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    // ================== Navegación de pestañas ==================

    @Override
    public void btnNuevoHandler(ActionEvent e) {
        super.btnNuevoHandler(e);
        activeIndex = 1; // ir a Detalle
    }

    @Override
    public void selectionHandler(org.primefaces.event.SelectEvent<Cliente> evt) {
        super.selectionHandler(evt);
        activeIndex = 1; // abrir Detalle al seleccionar
    }

    @Override
    public void btnCancelarHandler(ActionEvent e) {
        super.btnCancelarHandler(e);
        activeIndex = 0; // volver a Lista
    }

    @Override
    public void btnGuardarHandler(ActionEvent e) {
        super.btnGuardarHandler(e);
        refrescarYVolverALista();
    }

    @Override
    public void btnModificarHandler(ActionEvent e) {
        super.btnModificarHandler(e);
        refrescarYVolverALista();
    }

    @Override
    public void btnEliminarHandler(ActionEvent e) {
        super.btnEliminarHandler(e);
        refrescarYVolverALista();
    }

    private void refrescarYVolverALista() {
        this.modelo = null;
        inicializarRegistros(); // si tu DefaultFrm lo expone
        activeIndex = 0;
        PrimeFaces.current().executeScript(
                "if(PF('tablaTop')){PF('tablaTop').clearFilters(); PF('tablaTop').getPaginator().setPage(0);}");
    }
}
