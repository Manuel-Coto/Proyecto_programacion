package sv.edu.ues.occ.ingenieria.prn335.inventario.web.boundary.servlet;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.boundary.ws.KardexEndPoint;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.ClienteDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.InventarioDefaultDataAccess;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.NotificadorKardex;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.VentaDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.Cliente;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.Venta;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named("ventaFrm")
@ViewScoped
public class VentaFrm extends DefaultFrm<Venta> implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(VentaFrm.class.getName());

    @Inject
    private VentaDAO ventaDao;

    @Inject
    private ClienteDAO clienteDao;

    @Inject
    private VentaDetalleFrm ventaDetalleFrm;

    // Se inyecta para notificar al cerrar la venta (JMS -> ReceptorKardex -> WS)
    @Inject
    private NotificadorKardex notificadorKardex;

    @Inject
    private KardexEndPoint kardexEndPoint;

    private List<Cliente> clientesDisponibles;
    private final List<String> estadosDisponibles = List.of("CREADA", "PROCESO", "FINALIZADA", "ANULADA");

    private boolean modoLista;
    private boolean modoDetalle;
    private int activeTab;

    @PostConstruct
    @Override
    public void inicializar() {
        LOGGER.log(Level.INFO, "Inicializando VentaFrm...");
        super.inicializar();
        cargarClientes();
        this.nombreBean = "Gestión de Ventas";
        this.modoLista = true;
        this.modoDetalle = true;
        this.activeTab = 0;

        // Inicializar VentaDetalleFrm
        if (ventaDetalleFrm != null) {
            ventaDetalleFrm.inicializar();
            LOGGER.log(Level.INFO, "✅ VentaDetalleFrm inicializado");
        }

        LOGGER.log(Level.INFO, "VentaFrm inicializado - Modelo: {0}",
                (modelo != null ? "OK" : "NULL"));
    }

    private void cargarClientes() {
        try {
            this.clientesDisponibles = clienteDao.findAll();
            LOGGER.log(Level.INFO, "Clientes cargados: {0}", clientesDisponibles.size());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al cargar clientes", e);
            this.clientesDisponibles = List.of();
        }
    }

    @Override
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    @Override
    protected InventarioDefaultDataAccess<Venta> getDao() {
        return ventaDao;
    }

    @Override
    protected Venta nuevoRegistro() {
        LOGGER.log(Level.INFO, "🆕 Creando nuevo registro Venta");
        Venta v = new Venta();
        v.setId(UUID.randomUUID());
        v.setIdCliente(new Cliente());
        v.setEstado(null);
        v.setFecha(OffsetDateTime.now());
        v.setObservaciones("");
        return v;
    }

    @Override
    protected Venta buscarRegistroPorId(Object id) {
        try {
            if (id != null) {
                UUID uuid = UUID.fromString(id.toString());
                return ventaDao.findById(uuid);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en buscarRegistroPorId", e);
        }
        return null;
    }

    @Override
    protected String getIdAsText(Venta r) {
        return r != null && r.getId() != null ? r.getId().toString() : null;
    }

    @Override
    protected Venta getIdByText(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        try {
            UUID uuid = UUID.fromString(id);
            return ventaDao.findById(uuid);
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Error al parsear UUID", e);
            return null;
        }
    }

    @Override
    protected boolean esNombreVacio(Venta registro) {
        if (registro == null || registro.getIdCliente() == null || registro.getIdCliente().getId() == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "Debe seleccionar un cliente."));
            return true;
        }
        if (registro.getEstado() == null || registro.getEstado().isEmpty()) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "Debe seleccionar un estado."));
            return true;
        }
        if (registro.getFecha() == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "Debe seleccionar una fecha."));
            return true;
        }
        return false;
    }

    @Override
    public void btnGuardarHandler(ActionEvent actionEvent) {
        LOGGER.log(Level.INFO, "Intentando guardar venta...");
        if (this.registro != null) {
            try {
                if (esNombreVacio(this.registro)) {
                    LOGGER.log(Level.INFO, "Validación fallida");
                    return;
                }

                UUID idClienteSeleccionado = this.registro.getIdCliente().getId();
                Cliente clienteEntidad = clienteDao.findById(idClienteSeleccionado);

                if (clienteEntidad == null) {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Cliente no encontrado"));
                    return;
                }

                this.registro.setIdCliente(clienteEntidad);
                LOGGER.log(Level.INFO, "Cliente sincronizado: {0}", clienteEntidad.getNombre());

                getDao().crear(this.registro);
                LOGGER.log(Level.INFO, "Venta guardada con ID: {0}", this.registro.getId());

                this.registro = null;
                this.estado = ESTADO_CRUD.NADA;
                this.modelo = null;
                inicializarRegistros();

                if (ventaDetalleFrm != null) {
                    ventaDetalleFrm.inicializar();
                }

                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Registro guardado correctamente"));
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error al guardar venta", e);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al guardar", e.getMessage()));
            }
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "No hay registro para guardar"));
        }
    }

    @Override
    public void btnModificarHandler(ActionEvent actionEvent) {
        LOGGER.log(Level.INFO, "Intentando modificar venta...");
        if (this.registro != null) {
            try {
                if (esNombreVacio(this.registro)) {
                    return;
                }

                UUID idClienteSeleccionado = this.registro.getIdCliente().getId();
                Cliente clienteEntidad = clienteDao.findById(idClienteSeleccionado);

                if (clienteEntidad == null) {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Cliente no encontrado"));
                    return;
                }

                this.registro.setIdCliente(clienteEntidad);
                getDao().modificar(this.registro);

                // Notificar siempre cuando se modifica
                if (kardexEndPoint != null) {
                    kardexEndPoint.enviarMensajeBroadcast("actualizar");
                    LOGGER.log(Level.INFO, "Notificación enviada al WebSocket desde btnModificarHandler");
                }

                this.registro = null;
                this.estado = ESTADO_CRUD.NADA;
                inicializarRegistros();

                if (ventaDetalleFrm != null) {
                    ventaDetalleFrm.inicializar();
                }

                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Registro modificado correctamente"));
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error al modificar venta", e);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al modificar", e.getMessage()));
            }
        }
    }


    // Nuevo método: marcar como FINALIZADA, persistir y notificar
    public void notificarCambioKardex(ActionEvent actionEvent) {
        if (this.registro == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "No hay registro para notificar"));
            return;
        }
        this.registro.setEstado("FINALIZADA");
        try {
            getDao().modificar(this.registro);

            // Notificar al WebSocket
            if (kardexEndPoint != null) {
                kardexEndPoint.enviarMensajeBroadcast("actualizar");
                LOGGER.log(Level.INFO, "Notificación enviada al WebSocket desde VentaFrm");
            }

            this.registro = null;
            this.estado = ESTADO_CRUD.NADA;
            this.modelo = null;
            inicializarRegistros();

            if (ventaDetalleFrm != null) {
                ventaDetalleFrm.inicializar();
            }

            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Venta cerrada y enviada a Kardex"));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al cerrar venta", e);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al cerrar venta", e.getMessage()));
        }
    }


    @Override
    public void btnEliminarHandler(ActionEvent actionEvent) {
        LOGGER.log(Level.INFO, "Intentando eliminar venta...");
        if (this.registro != null) {
            try {
                getDao().eliminar(this.registro);

                this.registro = null;
                this.estado = ESTADO_CRUD.NADA;
                inicializarRegistros();

                if (ventaDetalleFrm != null) {
                    ventaDetalleFrm.inicializar();
                }

                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Registro eliminado correctamente"));
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error al eliminar venta", e);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al eliminar", e.getMessage()));
            }
        }
    }

    @Override
    public void btnNuevoHandler(ActionEvent actionEvent) {
        super.btnNuevoHandler(actionEvent);
        this.modoLista = false;
        this.modoDetalle = true;
    }

    @Override
    public void btnCancelarHandler(ActionEvent actionEvent) {
        super.btnCancelarHandler(actionEvent);
        this.modoLista = true;
        this.modoDetalle = false;
        this.activeTab = 0;
    }

    @Override
    public void selectionHandler(org.primefaces.event.SelectEvent<Venta> event) {
        super.selectionHandler(event);
        this.modoLista = false;
        this.modoDetalle = true;

        if (ventaDetalleFrm != null && this.registro != null) {
            ventaDetalleFrm.inicializarConVenta(this.registro);
            this.activeTab = 1;
            LOGGER.log(Level.INFO, "VentaDetalleFrm inicializado con venta: {0}",
                    this.registro.getId());
        }
    }

    // Getters y Setters

    public List<Cliente> getClientesDisponibles() {
        if (clientesDisponibles == null || clientesDisponibles.isEmpty()) {
            cargarClientes();
        }
        return clientesDisponibles;
    }

    public List<String> getEstadosDisponibles() {
        return estadosDisponibles;
    }

}
