// java
package sv.edu.ues.occ.ingenieria.prn335.inventario.web.boundary.servlet;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.CompraDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.InventarioDefaultDataAccess;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.NotificadorKardex;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.ProveedorDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.Compra;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.Proveedor;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named("compraFrm")
@ViewScoped
public class CompraFrm extends DefaultFrm<Compra> implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(CompraFrm.class.getName());

    @Inject
    private CompraDAO compraDao;

    @Inject
    private ProveedorDAO proveedorDao;

    @Inject
    private NotificadorKardex notificadorKardex;

    private List<Proveedor> listaProveedores;
    private List<EstadoCompra> estadosDisponibles;

    @Override
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    @Override
    protected InventarioDefaultDataAccess<Compra> getDao() {
        return compraDao;
    }

    @Override
    protected Compra nuevoRegistro() {
        Compra nuevaCompra = new Compra();
        nuevaCompra.setFecha(OffsetDateTime.now());
        return nuevaCompra;
    }

    @Override
    protected Compra buscarRegistroPorId(Object id) {
        try {
            if (id instanceof Integer) {
                return compraDao.findById(((Integer) id).longValue());
            } else if (id instanceof Long) {
                return compraDao.findById(id);
            } else if (id instanceof String) {
                return compraDao.findById(Long.parseLong((String) id));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error buscando Compra por ID", e);
        }
        return null;
    }

    protected void crearEntidad(Compra entidad) throws Exception {
        if (entidad.getFecha() == null) {
            throw new Exception("La fecha es obligatoria");
        }
        if (entidad.getIdProveedor() == null) {
            throw new Exception("Debe seleccionar un proveedor");
        }
        if (entidad.getEstado() == null || entidad.getEstado().isBlank()) {
            throw new Exception("Debe seleccionar un estado");
        }
        Proveedor proveedorEntity = proveedorDao.findById(entidad.getIdProveedor());
        if (proveedorEntity == null) {
            throw new Exception("El proveedor seleccionado no existe.");
        }
        compraDao.crear(entidad);
    }

    @Override
    protected String getIdAsText(Compra r) {
        return (r != null && r.getId() != null) ? r.getId().toString() : null;
    }

    @Override
    protected Compra getIdByText(String id) {
        try {
            return buscarRegistroPorId(Long.parseLong(id));
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "ID no es un número válido: {0}", id);
            return null;
        }
    }

    @Override
    protected boolean esNombreVacio(Compra registro) {
        return registro == null || registro.getIdProveedor() == null;
    }

    public void inicializarListas() {
        try {
            this.listaProveedores = proveedorDao != null ? proveedorDao.findAll() : new ArrayList<>();
            this.estadosDisponibles = Arrays.asList(EstadoCompra.values());
            LOGGER.log(Level.INFO, "Listas inicializadas: {0} proveedores", listaProveedores.size());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al inicializar listas", e);
            this.listaProveedores = new ArrayList<>();
            this.estadosDisponibles = Collections.emptyList();
        }
    }

    @PostConstruct
    public void init() {
        super.inicializar();
        this.nombreBean = "Gestión de Compras";
        inicializarListas();
        LOGGER.log(Level.INFO, "CompraFrm inicializado correctamente");
    }

    @Override
    public void btnGuardarHandler(ActionEvent actionEvent) {
        if (this.registro == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "No hay registro para guardar"));
            return;
        }
        try {
            if (esNombreVacio(this.registro)) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "Debe seleccionar un proveedor"));
                return;
            }
            if (registro.getEstado() == null || registro.getEstado().isBlank()) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "Debe seleccionar un estado"));
                return;
            }
            if (registro.getFecha() == null) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "La fecha es obligatoria"));
                return;
            }
            if (this.estado == ESTADO_CRUD.CREAR) {
                crearEntidad(this.registro);
            } else if (this.estado == ESTADO_CRUD.MODIFICAR) {
                compraDao.validarProveedor(this.registro.getIdProveedor());
                Proveedor proveedor = proveedorDao.findById(this.registro.getIdProveedor());
                this.registro.setProveedor(proveedor);
                getDao().modificar(this.registro);
            }
            this.registro = null;
            this.estado = ESTADO_CRUD.NADA;
            this.modelo = null;
            inicializarRegistros();
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Registro guardado correctamente"));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al guardar compra", e);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al guardar", e.getMessage()));
        }
    }

    @Override
    public void btnModificarHandler(ActionEvent actionEvent) {
        if (this.registro == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "No hay registro para modificar"));
            return;
        }

        try {
            if (esNombreVacio(this.registro)) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "Debe seleccionar un proveedor"));
                return;
            }

            if (registro.getEstado() == null || registro.getEstado().isBlank()) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "Debe seleccionar un estado"));
                return;
            }

            compraDao.validarProveedor(this.registro.getIdProveedor());
            Proveedor proveedor = proveedorDao.findById(this.registro.getIdProveedor());
            this.registro.setProveedor(proveedor);

            boolean notificar = EstadoCompra.PAGADA.name().equalsIgnoreCase(this.registro.getEstado());

            getDao().modificar(this.registro);

            if (notificar) {
                try {
                    String mensaje = "Compra PAGADA: " + this.registro.getId();
                    notificadorKardex.notificarCambioKardex(mensaje);
                } catch (Exception ne) {
                    LOGGER.log(Level.WARNING, "Error notificando cambio al Kardex", ne);
                }
            }

            this.registro = null;
            this.estado = ESTADO_CRUD.NADA;
            this.modelo = null;
            inicializarRegistros();

            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Registro modificado correctamente"));

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al modificar compra", e);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al modificar", e.getMessage()));
        }
    }

    /**
     * Método que marca el registro como PAGADA y delega en btnModificarHandler para persistir y notificar.
     */
    public void notificarCambioKardex(ActionEvent actionEvent) {
        if (this.registro == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "No hay registro para notificar"));
            return;
        }
        this.registro.setEstado(EstadoCompra.PAGADA.name());
        // Delegar en el handler de modificación (éste notificará antes de limpiar)
        btnModificarHandler(actionEvent);
    }

    @Override
    public void btnEliminarHandler(ActionEvent actionEvent) {
        if (this.registro == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "No hay registro para eliminar"));
            return;
        }
        try {
            LOGGER.log(Level.INFO, "Eliminando compra con ID: {0}", this.registro.getId());
            compraDao.eliminar(this.registro);
            this.registro = null;
            this.estado = ESTADO_CRUD.NADA;
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Registro eliminado correctamente"));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar compra", e);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al eliminar", e.getMessage()));
        }
    }



    public List<Proveedor> getListaProveedores() {
        if (listaProveedores == null || listaProveedores.isEmpty()) {
            inicializarListas();
        }
        return listaProveedores;
    }

    public void setListaProveedores(List<Proveedor> listaProveedores) {
        this.listaProveedores = listaProveedores;
    }

    public boolean isModoLista() {
        return this.estado == ESTADO_CRUD.NADA;
    }

    public boolean isModoDetalle() {
        return this.estado != ESTADO_CRUD.NADA;
    }

    public List<EstadoCompra> getEstadosDisponibles() {
        if (estadosDisponibles == null || estadosDisponibles.isEmpty()) {
            inicializarListas();
        }
        return estadosDisponibles;
    }

    public void setEstadosDisponibles(List<EstadoCompra> estadosDisponibles) {
        this.estadosDisponibles = estadosDisponibles;
    }
}
