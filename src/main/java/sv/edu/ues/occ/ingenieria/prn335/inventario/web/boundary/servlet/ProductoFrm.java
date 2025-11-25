package sv.edu.ues.occ.ingenieria.prn335.inventario.web.boundary.servlet;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.primefaces.event.SelectEvent;
import org.primefaces.model.LazyDataModel;

import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.InventarioDefaultDataAccess;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.ProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.TipoProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.Producto;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.TipoProducto;

@Named("productoFrm")
@ViewScoped
public class ProductoFrm extends DefaultFrm<Producto> implements Serializable {

    @Inject
    private ProductoDAO productoDao;

    @Inject
    private TipoProductoDAO tipoProductoDao;

    private List<TipoProducto> listaTipoProducto;
    private Long selectedTipoProductoId;


    @PostConstruct
    @Override
    public void inicializar() {
        this.nombreBean = "Producto";
        super.inicializar();
    }

    // Procesos de carga de listas para combos y demás
    private void cargarTiposDeProducto() {
        this.listaTipoProducto = tipoProductoDao.findAll();
    }

    // Requeridos por DefaultFrm
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
        if (r != null && r.getId() != null) {
            return r.getId().toString();
        }
        return null;
    }

    @Override
    protected Producto getIdByText(String id) {
        if (id != null && this.modelo != null && !this.modelo.getWrappedData().isEmpty()) {
            try {
                java.util.UUID buscado = java.util.UUID.fromString(id);
                return this.modelo.getWrappedData().stream()
                        .filter(r -> r.getId() != null && r.getId().equals(buscado))
                        .findFirst()
                        .orElse(null);
            } catch (IllegalArgumentException e) {
                System.err.println("ID no es un UUID válido: " + id);
                return null;
            }
        }
        return null;
    }

    /* ==== Botones ==== */

    // Antes de guardar, asigna el TipoProducto seleccionado
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

    // Getters y Setters adicionales
    public boolean isModoLista()   { return getEstado() == ESTADO_CRUD.NADA; }

    public boolean isModoDetalle() { return getEstado() != ESTADO_CRUD.NADA; }


}
