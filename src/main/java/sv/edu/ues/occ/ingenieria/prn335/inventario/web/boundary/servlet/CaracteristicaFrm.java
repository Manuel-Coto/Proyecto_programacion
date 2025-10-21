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
import java.util.logging.Level;
import java.util.logging.Logger;

import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.CaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.InventarioDefaultDataAccess;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.Caracteristica;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.TipoUnidadMedida;

@Named("caracteristicaFrm")
@ViewScoped
public class CaracteristicaFrm extends DefaultFrm<Caracteristica> implements Serializable {

    private static final Logger LOG = Logger.getLogger(CaracteristicaFrm.class.getName());

    @Inject
    private CaracteristicaDAO caracteristicaDao;

    // Si ya cuentas con este DAO, inyecta; si no, puedes de momento traer la lista con el EM del DAO de Caracteristica.
    @Inject
    private sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.TipoUnidadMedidaDAO tipoUnidadMedidaDao;

    // UI: selección para el combo
    private Integer selectedTipoUnidadMedidaId;
    private List<TipoUnidadMedida> listaTipoUnidadMedida;

    @PostConstruct
    @Override
    public void inicializar() {
        this.nombreBean = "Características";
        super.inicializar();   // configura LazyDataModel básico (findRange/count)
        cargarUnidades();
    }

    private void cargarUnidades() {
        try {
            if (tipoUnidadMedidaDao != null) {
                this.listaTipoUnidadMedida = tipoUnidadMedidaDao.findAll();
            } else {
                // fallback simple si aún no tienes el DAO de TUM
                this.listaTipoUnidadMedida = caracteristicaDao.getEntityManager()
                        .createQuery("SELECT t FROM TipoUnidadMedida t ORDER BY t.id", TipoUnidadMedida.class)
                        .getResultList();
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error cargando TipoUnidadMedida", e);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "No fue posible cargar las unidades de medida"));
        }
    }

    /* =======================
       Implementación abstracta
       ======================= */

    @Override
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    @Override
    protected InventarioDefaultDataAccess<Caracteristica> getDao() {
        return caracteristicaDao;
    }

    @Override
    protected Caracteristica nuevoRegistro() {
        Caracteristica c = new Caracteristica();
        c.setActivo(Boolean.TRUE);
        c.setNombre("");
        c.setDescripcion("");
        selectedTipoUnidadMedidaId = null;
        return c;
    }

    @Override
    protected Caracteristica buscarRegistroPorId(Object id) {
        if (id == null) return null;
        try {
            Integer iid = (id instanceof Integer) ? (Integer) id : Integer.valueOf(String.valueOf(id));
            return caracteristicaDao.getEntityManager().find(Caracteristica.class, iid);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error en buscarRegistroPorId", e);
            return null;
        }
    }

    @Override
    protected String getIdAsText(Caracteristica r) {
        return (r != null && r.getId() != null) ? String.valueOf(r.getId()) : null;
    }

    @Override
    protected Caracteristica getIdByText(String id) {
        if (id == null) return null;
        return buscarRegistroPorId(id);
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
            if (selectedTipoUnidadMedidaId == null) {
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "Atención", "Debe seleccionar una Unidad de Medida"));
                return;
            }

            // Asignar relación
            TipoUnidadMedida tum = caracteristicaDao.getEntityManager()
                    .find(TipoUnidadMedida.class, selectedTipoUnidadMedidaId);
            if (tum == null) {
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Error", "La Unidad de Medida seleccionada no existe"));
                return;
            }
            this.registro.setIdTipoUnidadMedida(tum);

            // Evitar duplicados por nombre (opcional)
            if (this.registro.getId() == null && caracteristicaDao.existsByNombre(nombre)) {
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "Duplicado", "Ya existe una característica con ese nombre"));
                return;
            }

            // Crear o modificar (según tenga ID)
            if (this.registro.getId() == null) {
                caracteristicaDao.crear(this.registro);
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Éxito", "Registro creado correctamente"));
            } else {
                caracteristicaDao.modificar(this.registro);
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Éxito", "Registro modificado correctamente"));
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
            if (selectedTipoUnidadMedidaId != null) {
                TipoUnidadMedida tum = caracteristicaDao.getEntityManager()
                        .find(TipoUnidadMedida.class, selectedTipoUnidadMedidaId);
                this.registro.setIdTipoUnidadMedida(tum);
            }
            caracteristicaDao.modificar(this.registro);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Modificado correctamente", null));

            this.estado = ESTADO_CRUD.NADA;
            inicializarRegistros();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al modificar", e.getMessage()));
        }
    }

    /* =======================
       Sincronizar selección al editar
       ======================= */

    @Override
    public void selectionHandler(org.primefaces.event.SelectEvent<Caracteristica> evt) {
        super.selectionHandler(evt);
        if (this.registro != null && this.registro.getIdTipoUnidadMedida() != null) {
            try {
                this.selectedTipoUnidadMedidaId = this.registro.getIdTipoUnidadMedida().getId();
            } catch (Exception ignored) { /* noop */ }
        } else {
            this.selectedTipoUnidadMedidaId = null;
        }
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

    public List<TipoUnidadMedida> getListaTipoUnidadMedida() {
        if (listaTipoUnidadMedida == null || listaTipoUnidadMedida.isEmpty()) {
            cargarUnidades();
        }
        return listaTipoUnidadMedida;
    }
}
