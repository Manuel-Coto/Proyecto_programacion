package sv.edu.ues.occ.ingenieria.prn335.inventario.web.boundary.servlet;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
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

    // ======== Estados de UI ========
    private boolean pnlDetalle = false;
    private boolean mostrarFormulario = false;
    private boolean editionMode = false;

    @PostConstruct
    @Override
    public void inicializar() {
        this.nombreBean = "Tipo de Unidad de Medida";
        super.inicializar();
        pnlDetalle = false;
        mostrarFormulario = false;
        editionMode = false;
    }

    // ======================================================
    // IMPLEMENTACIONES OBLIGATORIAS DE DefaultFrm
    // ======================================================

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
            Integer iid = (id instanceof Integer)
                    ? (Integer) id
                    : Integer.valueOf(String.valueOf(id));
            return tipoUnidadMedidaDao.getEntityManager().find(TipoUnidadMedida.class, iid);
        } catch (Exception e) {
            LOG.warning("Error buscando TipoUnidadMedida por ID: " + e.getMessage());
            return null;
        }
    }

    @Override
    protected String getIdAsText(TipoUnidadMedida r) {
        return (r != null && r.getId() != null)
                ? String.valueOf(r.getId())
                : null;
    }

    @Override
    protected TipoUnidadMedida getIdByText(String id) {
        if (id == null) return null;
        return buscarRegistroPorId(id);
    }

    // ======================================================
    // FUNCIONALIDAD UI ADAPTADA (Toolbar + Detalle + Dialog)
    // ======================================================

    public void selectionHandler(org.primefaces.event.SelectEvent<TipoUnidadMedida> ev) {
        super.selectionHandler(ev);
        pnlDetalle = true;
        mostrarFormulario = false;
        editionMode = true;
    }

    public void rowUnselectHandler() {
        this.registro = null;
        this.estado = ESTADO_CRUD.NADA;
        pnlDetalle = false;
        mostrarFormulario = false;
        editionMode = false;
    }

    public String btnNuevoToolbar() {
        this.registro = nuevoRegistro();
        this.estado = ESTADO_CRUD.CREAR;
        mostrarFormulario = true;
        editionMode = false;
        return null;
    }

    public String volver() {
        pnlDetalle = false;
        registro = null;
        estado = ESTADO_CRUD.NADA;
        return null;
    }

    public String btnEditarHandler(TipoUnidadMedida r) {
        if (r != null) {
            this.registro = r;
            this.estado = ESTADO_CRUD.MODIFICAR;
            this.mostrarFormulario = true;
            this.editionMode = true;
        }
        return null;
    }

    public void btnGuardarDesdeDialog(ActionEvent e) {
        if (estado == ESTADO_CRUD.CREAR) {
            super.btnGuardarHandler(e);
        } else if (estado == ESTADO_CRUD.MODIFICAR) {
            super.btnModificarHandler(e);
        }
        mostrarFormulario = false;
    }

    public void btnEliminarDesdeDetalle(ActionEvent e) {
        super.btnEliminarHandler(e);
        pnlDetalle = false;
    }

    // ======================================================
    // Getters / Setters
    // ======================================================
    public boolean isPnlDetalle() {
        return pnlDetalle;
    }

    public void setPnlDetalle(boolean pnlDetalle) {
        this.pnlDetalle = pnlDetalle;
    }

    public boolean isMostrarFormulario() {
        return mostrarFormulario;
    }

    public void setMostrarFormulario(boolean mostrarFormulario) {
        this.mostrarFormulario = mostrarFormulario;
    }

    public boolean isEditionMode() {
        return editionMode;
    }

    public void setEditionMode(boolean editionMode) {
        this.editionMode = editionMode;
    }
}
