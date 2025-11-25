package sv.edu.ues.occ.ingenieria.prn335.inventario.web.boundary.servlet;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.event.SelectEvent;

import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.CompraDetalleDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.CompraDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.InventarioDefaultDataAccess;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.ProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.CompraDetalle;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.Compra;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.Producto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named("compraDetalleFrm")
@ViewScoped
public class CompraDetalleFrm extends DefaultFrm<CompraDetalle> implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(CompraDetalleFrm.class.getName());

    @Inject
    private CompraDetalleDAO compraDetalleDAO;

    @Inject
    private CompraDAO compraDao;

    @Inject
    private ProductoDAO productoDao;

    private List<Compra> comprasDisponibles;
    private List<Producto> productosDisponibles;
    private final List<String> estadosDisponibles = List.of("RECIBIDO", "PENDIENTE", "DEVUELTO");

    private boolean modoLista;
    private boolean modoDetalle;

    @PostConstruct
    @Override
    public void inicializar() {
        LOGGER.log(Level.INFO, "Inicializando CompraDetalleFrm...");
        this.nombreBean = "Gestión de Detalles de Compra";
        this.modoLista = true;
        this.modoDetalle = false;
        this.estado = ESTADO_CRUD.NADA;
        inicializarListas();
        inicializarRegistros();
        LOGGER.log(Level.INFO, "CompraDetalleFrm inicializado correctamente");
    }

    public void inicializarListas() {
        try {
            this.comprasDisponibles = compraDao != null ? compraDao.findAll() : new ArrayList<>();
            this.productosDisponibles = productoDao != null ? productoDao.findAll() : new ArrayList<>();
            LOGGER.log(Level.INFO, "Listas inicializadas: {0} compras, {1} productos",
                    new Object[]{comprasDisponibles.size(), productosDisponibles.size()});
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al inicializar listas", e);
            this.comprasDisponibles = new ArrayList<>();
            this.productosDisponibles = new ArrayList<>();
        }
    }

    public void inicializarConCompra(Compra compraSeleccionada) {
        if (compraSeleccionada != null && compraSeleccionada.getId() != null) {
            // Si estamos en modo lista, filtrar por la compra seleccionada
            if (this.modelo != null) {
                // Aquí filtramos los detalles que pertenecen a esta compra
                List<CompraDetalle> detallesFiltrados = new ArrayList<>();
                try {
                    List<CompraDetalle> todos = compraDetalleDAO.findAll();
                    for (CompraDetalle detalle : todos) {
                        if (detalle.getIdCompra() != null &&
                                detalle.getIdCompra().getId() != null &&
                                detalle.getIdCompra().getId().equals(compraSeleccionada.getId())) {
                            detallesFiltrados.add(detalle);
                        }
                    }
                    this.modelo.setWrappedData(detallesFiltrados);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error filtrando detalles de compra", e);
                }
            }

            // Si hay un registro nuevo, asignarle la compra automáticamente
            if (this.registro != null && this.registro.getIdCompra() == null) {
                this.registro.setIdCompra(compraSeleccionada);
            }
        }
    }


    public List<CompraDetalle> obtenerDetallesPorCompra(Compra compraSeleccionada) {
        if (compraSeleccionada == null || compraSeleccionada.getId() == null) {
            return new ArrayList<>();
        }

        try {
            List<CompraDetalle> todos = compraDetalleDAO.findAll();
            List<CompraDetalle> filtrados = new ArrayList<>();

            for (CompraDetalle detalle : todos) {
                if (detalle.getIdCompra() != null &&
                        detalle.getIdCompra().getId() != null &&
                        detalle.getIdCompra().getId().equals(compraSeleccionada.getId())) {
                    filtrados.add(detalle);
                }
            }

            return filtrados;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error obteniendo detalles de compra", e);
            return new ArrayList<>();
        }
    }


    @Override
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    @Override
    protected InventarioDefaultDataAccess<CompraDetalle> getDao() {
        return compraDetalleDAO;
    }

    @Override
    protected CompraDetalle nuevoRegistro() {
        CompraDetalle cd = new CompraDetalle();
        cd.setId(UUID.randomUUID());
        cd.setCantidad(BigDecimal.ZERO);
        cd.setPrecio(BigDecimal.ZERO);
        cd.setEstado("PENDIENTE");
        LOGGER.log(Level.INFO, "Nuevo registro creado con ID: {0}", cd.getId());
        return cd;
    }

    @Override
    protected CompraDetalle buscarRegistroPorId(Object id) {
        if (id == null) return null;
        try {
            UUID uuid = (id instanceof UUID) ? (UUID) id : UUID.fromString(String.valueOf(id));
            return compraDetalleDAO.findById(uuid);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error buscando CompraDetalle", e);
            return null;
        }
    }

    @Override
    protected String getIdAsText(CompraDetalle r) {
        return (r != null && r.getId() != null) ? r.getId().toString() : null;
    }

    @Override
    protected CompraDetalle getIdByText(String id) {
        if (id != null && this.modelo != null && !this.modelo.getWrappedData().isEmpty()) {
            try {
                UUID buscado = UUID.fromString(id);
                return this.modelo.getWrappedData().stream()
                        .filter(r -> r.getId() != null && r.getId().equals(buscado))
                        .findFirst()
                        .orElse(null);
            } catch (IllegalArgumentException e) {
                LOGGER.log(Level.WARNING, "ID no es UUID válido: {0}", id);
                return null;
            }
        }
        return null;
    }

    @Override
    protected boolean esNombreVacio(CompraDetalle registro) {
        return registro == null || registro.getIdCompra() == null || registro.getIdProducto() == null;
    }

    @Override
    public void selectionHandler(SelectEvent<CompraDetalle> ev) {
        super.selectionHandler(ev);
        if (this.registro != null) {
            this.modoLista = false;
            this.modoDetalle = true;
            LOGGER.log(Level.INFO, "Registro seleccionado: {0}", this.registro.getId());
        }
    }

    @Override
    public void btnNuevoHandler(ActionEvent actionEvent) {
        super.btnNuevoHandler(actionEvent);
        this.modoLista = false;
        this.modoDetalle = true;
        LOGGER.log(Level.INFO, "Modo nuevo registro activado");
    }

    @Override
    public void btnCancelarHandler(ActionEvent actionEvent) {
        super.btnCancelarHandler(actionEvent);
        this.modoLista = true;
        this.modoDetalle = false;
        this.registro = null;
        LOGGER.log(Level.INFO, "Operación cancelada");
    }

    @Override
    public void btnGuardarHandler(ActionEvent actionEvent) {
        if (this.registro == null) {
            agregarMensaje("No hay registro para guardar", FacesMessage.SEVERITY_WARN);
            return;
        }

        try {
            if (esNombreVacio(this.registro)) {
                agregarMensaje("Debe seleccionar compra y producto", FacesMessage.SEVERITY_WARN);
                return;
            }

            if (this.registro.getCantidad() == null || this.registro.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
                agregarMensaje("La cantidad debe ser mayor a cero", FacesMessage.SEVERITY_WARN);
                return;
            }

            if (this.registro.getPrecio() == null || this.registro.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
                agregarMensaje("El precio debe ser válido", FacesMessage.SEVERITY_WARN);
                return;
            }

            if (this.estado == ESTADO_CRUD.CREAR) {
                getDao().crear(this.registro);
                LOGGER.log(Level.INFO, "Registro creado: {0}", this.registro.getId());
            } else if (this.estado == ESTADO_CRUD.MODIFICAR) {
                getDao().modificar(this.registro);
                LOGGER.log(Level.INFO, "Registro modificado: {0}", this.registro.getId());
            }

            this.registro = null;
            this.estado = ESTADO_CRUD.NADA;
            this.modoLista = true;
            this.modoDetalle = false;
            inicializarRegistros();

            agregarMensaje("Registro guardado exitosamente", FacesMessage.SEVERITY_INFO);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al guardar", e);
            agregarMensaje("Error al guardar: " + e.getMessage(), FacesMessage.SEVERITY_ERROR);
        }
    }

    @Override
    public void btnModificarHandler(ActionEvent actionEvent) {
        btnGuardarHandler(actionEvent);
    }

    @Override
    public void btnEliminarHandler(ActionEvent actionEvent) {
        if (this.registro == null) {
            agregarMensaje("No hay registro para eliminar", FacesMessage.SEVERITY_WARN);
            return;
        }

        try {
            getDao().eliminar(this.registro);
            LOGGER.log(Level.INFO, "Registro eliminado: {0}", this.registro.getId());

            this.registro = null;
            this.estado = ESTADO_CRUD.NADA;
            this.modoLista = true;
            this.modoDetalle = false;
            inicializarRegistros();

            agregarMensaje("Registro eliminado exitosamente", FacesMessage.SEVERITY_INFO);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar", e);
            agregarMensaje("Error al eliminar: " + e.getMessage(), FacesMessage.SEVERITY_ERROR);
        }
    }

    private void agregarMensaje(String mensaje, FacesMessage.Severity severity) {
        getFacesContext().addMessage(null, new FacesMessage(severity, "Información", mensaje));
    }

    public List<Compra> getComprasDisponibles() {
        if (comprasDisponibles == null || comprasDisponibles.isEmpty()) {
            inicializarListas();
        }
        return comprasDisponibles;
    }

    public List<Producto> getProductosDisponibles() {
        if (productosDisponibles == null || productosDisponibles.isEmpty()) {
            inicializarListas();
        }
        return productosDisponibles;
    }

    public List<String> getEstadosDisponibles() {
        return estadosDisponibles;
    }

    public boolean isModoLista() {
        return modoLista;
    }

    public void setModoLista(boolean modoLista) {
        this.modoLista = modoLista;
    }

    public boolean isModoDetalle() {
        return modoDetalle;
    }

    public void setModoDetalle(boolean modoDetalle) {
        this.modoDetalle = modoDetalle;
    }
}
