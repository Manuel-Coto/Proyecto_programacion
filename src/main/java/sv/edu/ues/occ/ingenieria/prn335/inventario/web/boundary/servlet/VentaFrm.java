package sv.edu.ues.occ.ingenieria.prn335.inventario.web.boundary.servlet;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.ClienteDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.InventarioDefaultDataAccess;
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

    private static final Logger LOGGER = Logger.getLogger(VentaFrm.class.getName());

    @Inject
    private VentaDAO ventaDao;

    @Inject
    private ClienteDAO clienteDao;

    // ⭐ INYECTAR VentaDetalleFrm para que esté disponible en la vista
    @Inject
    private VentaDetalleFrm ventaDetalleFrm;

    private List<Cliente> clientesDisponibles;

    private final List<String> estadosDisponibles = List.of("CREADA", "PROCESO", "FINALIZADA", "ANULADA");

    @PostConstruct
    @Override
    public void inicializar() {
        System.out.println("Iniciando VentaFrm...");
        super.inicializar(); // Inicializa el LazyDataModel
        cargarClientes();
        this.nombreBean = "Gestión de Ventas";

        // ⭐ Inicializar VentaDetalleFrm
        if (ventaDetalleFrm != null) {
            ventaDetalleFrm.inicializar();
            LOGGER.log(Level.INFO, "✅ VentaDetalleFrm inicializado");
        }

        System.out.println("Bean VentaFrm creado - Modelo: " + (modelo != null ? "OK" : "NULL"));
    }

    private void cargarClientes() {
        try {
            this.clientesDisponibles = clienteDao.findAll();
            System.out.println("Clientes cargados: " + clientesDisponibles.size());
            LOGGER.log(Level.INFO, "Clientes cargados: {0}", clientesDisponibles.size());
        } catch (Exception e) {
            System.err.println("Error al cargar clientes: " + e.getMessage());
            e.printStackTrace();
            LOGGER.log(Level.SEVERE, "Error al cargar clientes", e);
            this.clientesDisponibles = List.of();
        }
    }

    // --- Implementación de Métodos Abstractos ---

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
        System.out.println("🆕 Creando nuevo registro Venta");
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
            System.err.println("Error en buscarRegistroPorId: " + e.getMessage());
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
            System.err.println("Error al parsear UUID: " + e.getMessage());
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

    // --- Manejadores de Botones ---



    @Override
    public void btnGuardarHandler(ActionEvent actionEvent) {
        System.out.println("Intentando guardar venta...");
        if (this.registro != null) {
            try {
                // 1. Validar
                if (esNombreVacio(this.registro)) {
                    System.out.println("Validación fallida");
                    return;
                }

                // 2. Sincronizar Cliente completo
                UUID idClienteSeleccionado = this.registro.getIdCliente().getId();
                Cliente clienteEntidad = clienteDao.findById(idClienteSeleccionado);

                if (clienteEntidad == null) {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Cliente no encontrado"));
                    return;
                }

                this.registro.setIdCliente(clienteEntidad);
                System.out.println("Cliente sincronizado: " + clienteEntidad.getNombre());
                LOGGER.log(Level.INFO, "Cliente sincronizado: {0}", clienteEntidad.getNombre());

                // 3. Persistir
                getDao().crear(this.registro);
                System.out.println("Venta guardada con ID: " + this.registro.getId());
                LOGGER.log(Level.INFO, "Venta guardada con ID: {0}", this.registro.getId());

                // 4. Limpiar y Notificación
                this.registro = null;
                this.estado = ESTADO_CRUD.NADA;
                this.modelo = null;
                inicializarRegistros();

                // ⭐ Reinicializar VentaDetalleFrm
                if (ventaDetalleFrm != null) {
                    ventaDetalleFrm.inicializar();
                }

                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Registro guardado correctamente"));
            } catch (Exception e) {
                System.err.println("Error al guardar: " + e.getMessage());
                e.printStackTrace();
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
        System.out.println("Intentando modificar venta...");
        if (this.registro != null) {
            try {
                // Validar
                if (esNombreVacio(this.registro)) {
                    return;
                }

                // Sincronizar Cliente
                UUID idClienteSeleccionado = this.registro.getIdCliente().getId();
                Cliente clienteEntidad = clienteDao.findById(idClienteSeleccionado);

                if (clienteEntidad == null) {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Cliente no encontrado"));
                    return;
                }

                this.registro.setIdCliente(clienteEntidad);

                // Modificar
                getDao().modificar(this.registro);

                this.registro = null;
                this.estado = ESTADO_CRUD.NADA;
                inicializarRegistros();

                // ⭐ Reinicializar VentaDetalleFrm
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

    @Override
    public void btnEliminarHandler(ActionEvent actionEvent) {
        System.out.println("Intentando eliminar venta...");
        if (this.registro != null) {
            try {
                getDao().eliminar(this.registro);

                this.registro = null;
                this.estado = ESTADO_CRUD.NADA;
                inicializarRegistros();

                // ⭐ Reinicializar VentaDetalleFrm
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

    // --- Getters para JSF ---

    public List<Cliente> getClientesDisponibles() {
        if (clientesDisponibles == null || clientesDisponibles.isEmpty()) {
            cargarClientes();
        }
        return clientesDisponibles;
    }

    public List<String> getEstadosDisponibles() {
        return estadosDisponibles;
    }

    // ⭐ Getter para que VentaDetalleFrm sea accesible en la vista
    public VentaDetalleFrm getVentaDetalleFrm() {
        return ventaDetalleFrm;
    }
}