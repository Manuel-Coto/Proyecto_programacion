package sv.edu.ues.occ.ingenieria.prn335.inventario.web.boundary.servlet;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.LazyDataModel;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.InventarioDefaultDataAccess;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.ProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.Producto;

@Named("productoFrm")
@ViewScoped
public class ProductoFrm extends DefaultFrm<Producto> implements Serializable {

    @Inject
    private ProductoDAO productoDao;

    @PostConstruct
    @Override
    public void inicializar() {
        this.nombreBean = "Producto";
        super.inicializar();
    }

    /* ==== Requeridos por DefaultFrm ==== */
    @Override
    protected FacesContext getFacesContext() { return FacesContext.getCurrentInstance(); }

    @Override
    protected InventarioDefaultDataAccess<Producto> getDao() { return productoDao; }

    @Override
    protected Producto nuevoRegistro() {
        Producto p = new Producto();
        p.setNombreProducto("");
        p.setActivo(Boolean.TRUE);
        p.setReferenciaExterna("");
        p.setComentarios("");
        return p;
    }

    @Override
    protected Producto buscarRegistroPorId(Object id) {
        if (id == null) return null;
        try {
            UUID uuid = (id instanceof UUID) ? (UUID) id : UUID.fromString(String.valueOf(id));
            return productoDao.findById(uuid);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected String getIdAsText(Producto r) {
        return (r != null && r.getId() != null) ? r.getId().toString() : null;
    }

    @Override
    protected Producto getIdByText(String id) {
        return (id == null || id.isBlank()) ? null : buscarRegistroPorId(id);
    }

    /* ==== Botones ==== */

    /** Genera UUID al crear porque la entidad no tiene @GeneratedValue. */
    @Override
    public void btnGuardarHandler(ActionEvent actionEvent) {
        if (this.registro != null && this.registro.getId() == null) {
            this.registro.setId(UUID.randomUUID());
        }
        super.btnGuardarHandler(actionEvent);
    }

    @Override
    public void selectionHandler(SelectEvent<Producto> ev) {
        super.selectionHandler(ev); // ya setea registro y estado=MODIFICAR
    }

    /* ==== Flags para el XHTML (rendered) ==== */
    public boolean isModoLista()   { return getEstado() == ESTADO_CRUD.NADA; }
    public boolean isModoDetalle() { return getEstado() != ESTADO_CRUD.NADA; }

    /** Útil si quieres el modelo con tipo explícito en el xhtml (no es obligatorio). */
    @SuppressWarnings("unchecked")
    public LazyDataModel<Producto> getModeloTipado() {
        return (LazyDataModel<Producto>) super.getModelo();
    }

    /* Mensajito helper si lo necesitas en el futuro */
    private void msg(FacesMessage.Severity s, String sum, String det) {
        getFacesContext().addMessage(null, new FacesMessage(s, sum, det));
    }
}
