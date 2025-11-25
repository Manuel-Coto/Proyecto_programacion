package sv.edu.ues.occ.ingenieria.prn335.inventario.web.boundary.servlet;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.event.SelectEvent;

import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.VentaDetalleDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.VentaDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.InventarioDefaultDataAccess;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.ProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.VentaDetalle;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.Venta;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.Producto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named("ventaDetalleFrm")
@ViewScoped
public class VentaDetalleFrm extends DefaultFrm<VentaDetalle> implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(VentaDetalleFrm.class.getName());

    @Inject
    private VentaDetalleDAO ventaDetalleDAO;

    @Inject
    private VentaDAO ventaDao;

    @Inject
    private ProductoDAO productoDao;

    private List<Venta> ventasDisponibles;
    private List<Producto> productosDisponibles;
    private final List<String> estadosDisponibles = List.of("ENTREGADO", "PENDIENTE", "DEVUELTO", "CANCELADO");

    private boolean modoLista;
    private boolean modoDetalle;

    @PostConstruct
    @Override
    public void inicializar() {
        LOGGER.log(Level.INFO, "Inicializando VentaDetalleFrm...");
        this.nombreBean = "Gestión de Detalles de Venta";
        this.modoLista = true;
        this.modoDetalle = false;
        this.estado = ESTADO_CRUD.NADA;
        inicializarListas();
        inicializarRegistros();
        LOGGER.log(Level.INFO, "VentaDetalleFrm inicializado correctamente");
    }

    public void inicializarListas() {
        try {
            this.ventasDisponibles = ventaDao != null ? ventaDao.findAll() : new ArrayList<>();
            this.productosDisponibles = productoDao != null ? productoDao.findAll() : new ArrayList<>();
            LOGGER.log(Level.INFO, "Listas inicializadas: {0} ventas, {1} productos",
                    new Object[]{ventasDisponibles.size(), productosDisponibles.size()});
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al inicializar listas", e);
            this.ventasDisponibles = new ArrayList<>();
            this.productosDisponibles = new ArrayList<>();
        }
    }

    public void inicializarConVenta(Venta ventaSeleccionada) {
        if (ventaSeleccionada != null && ventaSeleccionada.getId() != null) {
            LOGGER.log(Level.INFO, "Inicializando con venta: {0}", ventaSeleccionada.getId());

            // Si estamos en modo lista, filtrar por la venta seleccionada
            if (this.modelo != null) {
                List<VentaDetalle> detallesFiltrados = new ArrayList<>();
                try {
                    List<VentaDetalle> todos = ventaDetalleDAO.findAll();
                    for (VentaDetalle detalle : todos) {
                        if (detalle.getIdVenta() != null &&
                                detalle.getIdVenta().getId() != null &&
                                detalle.getIdVenta().getId().equals(ventaSeleccionada.getId())) {
                            detallesFiltrados.add(detalle);
                        }
                    }
                    this.modelo.setWrappedData(detallesFiltrados);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error filtrando detalles de venta", e);
                }
            }

            // Si hay un registro nuevo, asignarle la venta automáticamente
            if (this.registro != null && this.registro.getIdVenta() == null) {
                this.registro.setIdVenta(ventaSeleccionada);
            }
        }
    }

    public List<VentaDetalle> obtenerDetallesPorVenta(Venta ventaSeleccionada) {
        if (ventaSeleccionada == null || ventaSeleccionada.getId() == null) {
            return new ArrayList<>();
        }

        try {
            List<VentaDetalle> todos = ventaDetalleDAO.findAll();
            List<VentaDetalle> filtrados = new ArrayList<>();

            for (VentaDetalle detalle : todos) {
                if (detalle.getIdVenta() != null &&
                        detalle.getIdVenta().getId() != null &&
                        detalle.getIdVenta().getId().equals(ventaSeleccionada.getId())) {
                    filtrados.add(detalle);
                }
            }

            return filtrados;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error obteniendo detalles de venta", e);
            return new ArrayList<>();
        }
    }

    @Override
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    @Override
    protected InventarioDefaultDataAccess<VentaDetalle> getDao() {
        return ventaDetalleDAO;
    }

    @Override
    protected VentaDetalle nuevoRegistro() {
        VentaDetalle vd = new VentaDetalle();
        vd.setId(UUID.randomUUID());
        vd.setCantidad(BigDecimal.ZERO);
        vd.setPrecio(BigDecimal.ZERO);
        vd.setEstado("PENDIENTE");
        LOGGER.log(Level.INFO, "Nuevo registro creado con ID: {0}", vd.getId());
        return vd;
    }

    @Override
    protected VentaDetalle buscarRegistroPorId(Object id) {
        if (id == null) return null;
        try {
            UUID uuid = (id instanceof UUID) ? (UUID) id : UUID.fromString(String.valueOf(id));
            return ventaDetalleDAO.findById(uuid);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error buscando VentaDetalle", e);
            return null;
        }
    }

    @Override
    protected String getIdAsText(VentaDetalle r) {
        return (r != null && r.getId() != null) ? r.getId().toString() : null;
    }

    @Override
    protected VentaDetalle getIdByText(String id) {
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
    protected boolean esNombreVacio(VentaDetalle registro) {
        return registro == null || registro.getIdVenta() == null || registro.getIdProducto() == null;
    }

    @Override
    public void selectionHandler(SelectEvent<VentaDetalle> ev) {
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
                agregarMensaje("Debe seleccionar venta y producto", FacesMessage.SEVERITY_WARN);
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

    // Getters y Setters

    public List<Venta> getVentasDisponibles() {
        if (ventasDisponibles == null || ventasDisponibles.isEmpty()) {
            inicializarListas();
        }
        return ventasDisponibles;
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
