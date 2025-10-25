package sv.edu.ues.occ.ingenieria.prn335.inventario.web.boundary.servlet;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
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

    private static final Logger LOG = Logger.getLogger(ClienteFrm.class.getName());

    @Inject
    private ClienteDAO clienteDAO;

    // UI: para manejar la validación
    private Integer selectedTipoUnidadMedidaId; // Ejemplo si necesitas alguna relación con otra entidad.

    @PostConstruct
    @Override
    public void inicializar() {
        this.nombreBean = "Clientes";
        super.inicializar();   // configura LazyDataModel básico (findRange/count)
    }

    /* =======================
       Implementación abstracta
       ======================= */

    @Override
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    @Override
    protected InventarioDefaultDataAccess<Cliente> getDao() {
        return clienteDAO;
    }

    @Override
    protected Cliente nuevoRegistro() {
        Cliente c = new Cliente();
        c.setId(UUID.randomUUID());  // Generar UUID si no es autogenerado en DB
        c.setActivo(Boolean.TRUE);  // Activo por defecto
        c.setNombre("");           // Nombre vacío por defecto
        c.setDui("");              // DUI vacío
        c.setNit("");              // NIT vacío
        return c;
    }

    @Override
    protected Cliente buscarRegistroPorId(Object id) {
        if (id instanceof UUID) {
            return clienteDAO.buscarRegistroPorId((UUID) id);
        }
        // Permite pasar String (desde tablas PrimeFaces) y convertir a UUID
        if (id instanceof String s) {
            try {
                return clienteDAO.buscarRegistroPorId(UUID.fromString(s));
            } catch (IllegalArgumentException ignored) {
                return null;
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
        if (id == null || id.isBlank()) return null;
        try {
            UUID uuid = UUID.fromString(id);
            return clienteDAO.buscarRegistroPorId(uuid);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /* =======================
       Overrides de botones
       ======================= */

    @Override
    public void btnGuardarHandler(ActionEvent actionEvent) {
        if (this.registro == null) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                    "Atención", "No hay registro para guardar"));
            return;
        }
        try {
            // Validaciones mínimas
            String nombre = this.registro.getNombre();
            if (nombre == null || nombre.isBlank()) {
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "Atención", "El nombre no puede estar vacío"));
                return;
            }

            String dui = this.registro.getDui();
            if (dui == null || dui.isBlank()) {
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "Atención", "El DUI no puede estar vacío"));
                return;
            }

            String nit = this.registro.getNit();
            if (nit == null || nit.isBlank()) {
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "Atención", "El NIT no puede estar vacío"));
                return;
            }

            // Validación de duplicados por nombre
            if (this.registro.getId() == null && clienteDAO.existsByNombre(nombre)) {
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "Duplicado", "Ya existe un cliente con ese nombre"));
                return;
            }

            // Crear o modificar según si tiene ID
            if (this.registro.getId() == null) {
                clienteDAO.crear(this.registro);
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Éxito", "Cliente creado correctamente"));
            } else {
                clienteDAO.modificar(this.registro);
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Éxito", "Cliente modificado correctamente"));
            }

            // Reset UI
            this.registro = null;
            this.estado = ESTADO_CRUD.NADA;
            this.modelo = null;
            inicializarRegistros();

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al guardar", e);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error al guardar", e.getMessage()));
        }
    }

    @Override
    public void btnModificarHandler(ActionEvent actionEvent) {
        try {
            if (this.registro == null) {
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "Atención", "No hay registro para modificar"));
                return;
            }

            clienteDAO.modificar(this.registro);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Modificado correctamente", null));

            this.estado = ESTADO_CRUD.NADA;
            inicializarRegistros();
        } catch (Exception e) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error al modificar", e.getMessage()));
        }
    }

    /* =======================
       Sincronizar selección al editar
       ======================= */

    @Override
    public void selectionHandler(org.primefaces.event.SelectEvent<Cliente> evt) {
        super.selectionHandler(evt);
    }

    /* =======================
       Getters / Setters UI
       ======================= */

    public Integer getSelectedTipoUnidadMedidaId() {
        return selectedTipoUnidadMedidaId;
    }

    public void setSelectedTipoUnidadMedidaId(Integer selectedTipoUnidadMedidaId) {
        this.selectedTipoUnidadMedidaId = selectedTipoUnidadMedidaId;
    }
}
