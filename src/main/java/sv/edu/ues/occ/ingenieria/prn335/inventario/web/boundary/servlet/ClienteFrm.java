package sv.edu.ues.occ.ingenieria.prn335.inventario.web.boundary.servlet;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.ClienteDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.InventarioDefaultDataAccess;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.Cliente;

@Named("clienteFrm")
@ViewScoped
public class ClienteFrm extends DefaultFrm<Cliente> implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(ClienteFrm.class.getName());

    @Inject
    private FacesContext facesContext;

    @Inject
    private ClienteDAO clienteDao;

    @PostConstruct
    @Override
    public void inicializar() {
        this.nombreBean = "Gestión de Clientes";
        super.inicializar();
        LOG.log(Level.INFO, "ClienteFrm inicializado correctamente");
    }

    @Override
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    @Override
    protected InventarioDefaultDataAccess<Cliente> getDao() {
        return clienteDao;
    }

    @Override
    protected Cliente nuevoRegistro() {
        Cliente cliente = new Cliente();
        cliente.setId(UUID.randomUUID());
        cliente.setActivo(Boolean.TRUE);
        LOG.log(Level.FINE, "Nuevo cliente creado con ID: {0}", cliente.getId());
        return cliente;
    }

    @Override
    protected Cliente buscarRegistroPorId(Object id) {
        if (id == null) {
            return null;
        }

        try {
            UUID uuid;
            if (id instanceof UUID) {
                uuid = (UUID) id;
            } else if (id instanceof String) {
                uuid = UUID.fromString((String) id);
            } else {
                LOG.log(Level.WARNING, "Tipo de ID no soportado: {0}", id.getClass().getName());
                return null;
            }

            Cliente cliente = clienteDao.findById(uuid);
            LOG.log(Level.FINE, "Cliente encontrado: {0}", uuid);
            return cliente;

        } catch (IllegalArgumentException e) {
            LOG.log(Level.WARNING, "UUID inválido: {0}", id);
            return null;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al buscar cliente por ID", e);
            return null;
        }
    }

    @Override
    protected String getIdAsText(Cliente r) {
        if (r != null && r.getId() != null) {
            return r.getId().toString();
        }
        return null;
    }

    @Override
    protected Cliente getIdByText(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        return buscarRegistroPorId(id);
    }

    @Override
    protected boolean esNombreVacio(Cliente registro) {
        if (registro == null) {
            return true;
        }
        String nombre = registro.getNombre();
        return nombre == null || nombre.trim().isEmpty();
    }

    @Override
    public void btnGuardarHandler(jakarta.faces.event.ActionEvent actionEvent) {
        LOG.log(Level.INFO, "btnGuardarHandler ejecutado");

        if (this.registro == null) {
            addErrorMessage("No hay registro para guardar");
            return;
        }

        try {
            // Validar nombre
            if (esNombreVacio(this.registro)) {
                addWarnMessage("El nombre del cliente es obligatorio");
                return;
            }

            // Asegurar UUID
            if (this.registro.getId() == null) {
                this.registro.setId(UUID.randomUUID());
                LOG.log(Level.FINE, "UUID generado: {0}", this.registro.getId());
            }

            // Asegurar estado activo
            if (this.registro.getActivo() == null) {
                this.registro.setActivo(Boolean.TRUE);
            }

            // Persistir
            getDao().crear(this.registro);

            LOG.log(Level.INFO, "Cliente creado exitosamente: {0}", this.registro.getId());

            // Limpieza
            this.registro = null;
            this.estado = ESTADO_CRUD.NADA;
            this.modelo = null;
            inicializarRegistros();

            addSuccessMessage("Cliente creado correctamente");

        } catch (IllegalArgumentException e) {
            LOG.log(Level.SEVERE, "Error de argumentación al guardar cliente", e);
            addErrorMessage("Error: " + e.getMessage());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al guardar cliente", e);
            addErrorMessage("Error al crear el registro: " + e.getMessage());
        }
    }

    @Override
    public void btnModificarHandler(jakarta.faces.event.ActionEvent actionEvent) {
        LOG.log(Level.INFO, "btnModificarHandler ejecutado");

        if (this.registro == null) {
            addWarnMessage("No hay registro para modificar");
            return;
        }

        try {
            if (esNombreVacio(this.registro)) {
                addWarnMessage("El nombre del cliente es obligatorio");
                return;
            }

            getDao().modificar(this.registro);

            LOG.log(Level.INFO, "Cliente modificado: {0}", this.registro.getId());

            this.registro = null;
            this.estado = ESTADO_CRUD.NADA;
            inicializarRegistros();

            addSuccessMessage("Cliente modificado correctamente");

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al modificar cliente", e);
            addErrorMessage("Error al modificar: " + e.getMessage());
        }
    }

    @Override
    public void btnEliminarHandler(jakarta.faces.event.ActionEvent actionEvent) {
        LOG.log(Level.INFO, "btnEliminarHandler ejecutado");

        if (this.registro == null) {
            addWarnMessage("No hay registro para eliminar");
            return;
        }

        try {
            getDao().eliminar(this.registro);

            LOG.log(Level.INFO, "Cliente eliminado: {0}", this.registro.getId());

            this.registro = null;
            this.estado = ESTADO_CRUD.NADA;
            inicializarRegistros();

            addSuccessMessage("Cliente eliminado correctamente");

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al eliminar cliente", e);
            addErrorMessage("Error al eliminar: " + e.getMessage());
        }
    }

    // Métodos helper para mensajes
    private void addSuccessMessage(String message) {
        facesContext.addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", message));
    }

    private void addWarnMessage(String message) {
        facesContext.addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", message));
    }

    private void addErrorMessage(String message) {
        facesContext.addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", message));
    }
}