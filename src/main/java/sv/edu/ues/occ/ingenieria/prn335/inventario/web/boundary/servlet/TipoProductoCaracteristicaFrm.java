package sv.edu.ues.occ.ingenieria.prn335.inventario.web.boundary.servlet;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.InventarioDefaultDataAccess;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.TipoProductoCaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.Caracteristica;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.TipoProducto;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.TipoProductoCaracteristica;

/**
 * Managed Bean para administrar la relación TipoProducto-Caracteristica.
 * Hereda el comportamiento base de DefaultFrm<T>.
 */
@Named("tipoProductoCaracteristicaFrm")
@ViewScoped
public class TipoProductoCaracteristicaFrm extends DefaultFrm<TipoProductoCaracteristica> implements Serializable {

    private static final Logger LOG = Logger.getLogger(TipoProductoCaracteristicaFrm.class.getName());

    /* =======================
       Inyecciones de DAO
       ======================= */
    @Inject
    private TipoProductoCaracteristicaDAO tpcDao;

    // Si ya tienes estos DAO, inyéctalos; si no, puedes crearlos siguiendo tu base abstracta.
    @Inject
    private sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.TipoProductoDAO tipoProductoDao;

    @Inject
    private sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.CaracteristicaDAO caracteristicaDao;

    /* =======================
       Campos de UI / selección
       ======================= */
    private Long selectedTipoProductoId;
    private Long selectedCaracteristicaId;

    private List<TipoProducto> listaTipoProducto;
    private List<Caracteristica> listaCaracteristica;

    @PostConstruct
    @Override
    public void inicializar() {
        this.nombreBean = "TipoProducto-Característica";
        super.inicializar(); // inicializa LazyDataModel
        cargarCombos();
    }

    private void cargarCombos() {
        try {
            this.listaTipoProducto   = tipoProductoDao != null ? tipoProductoDao.findAll() : List.of();
            this.listaCaracteristica = caracteristicaDao != null ? caracteristicaDao.findAll() : List.of();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error cargando combos", e);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "No fue posible cargar listas para selección"));
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
    protected InventarioDefaultDataAccess<TipoProductoCaracteristica> getDao() {
        return tpcDao;
    }

    @Override
    protected TipoProductoCaracteristica nuevoRegistro() {
        TipoProductoCaracteristica t = new TipoProductoCaracteristica();
        t.setFechaCreacion(OffsetDateTime.now());
        t.setObligatorio(Boolean.FALSE);
        // al crear, limpia selección
        selectedTipoProductoId = null;
        selectedCaracteristicaId = null;
        return t;
    }

    @Override
    protected TipoProductoCaracteristica buscarRegistroPorId(Object id) {
        if (id == null) return null;
        try {
            Long lid = (id instanceof Long) ? (Long) id : Long.valueOf(String.valueOf(id));
            // usamos el EM del DAO concreto
            return tpcDao.getEntityManager().find(TipoProductoCaracteristica.class, lid);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error en buscarRegistroPorId", e);
            return null;
        }
    }

    @Override
    protected String getIdAsText(TipoProductoCaracteristica r) {
        return (r != null && r.getId() != null) ? String.valueOf(r.getId()) : null;
    }

    @Override
    protected TipoProductoCaracteristica getIdByText(String id) {
        if (id == null) return null;
        return buscarRegistroPorId(id);
    }

    /* =======================
       Botones / Handlers
       ======================= */

    /** Sobrescribimos para poblar selección al EDITAR (cuando el usuario selecciona una fila). */
    @Override
    public void selectionHandler(org.primefaces.event.SelectEvent<TipoProductoCaracteristica> evt) {
        super.selectionHandler(evt);
        if (this.registro != null) {
            try {
                this.selectedTipoProductoId =
                        (this.registro.getIdTipoProducto() != null) ? this.registro.getIdTipoProducto().getId() : null;
                this.selectedCaracteristicaId =
                        Long.valueOf((this.registro.getIdCaracteristica() != null) ? this.registro.getIdCaracteristica().getId() : null);
            } catch (Exception ex) {
                LOG.log(Level.FINE, "No se pudo sincronizar IDs seleccionados", ex);
            }
        }
    }

    /**
     * Importante: tu DefaultFrm valida "getNombre()", pero esta entidad no tiene nombre.
     * Por eso, sobrescribimos el guardar para validar (tipoProducto, caracteristica) y "obligatorio".
     */
    @Override
    public void btnGuardarHandler(ActionEvent actionEvent) {
        if (this.registro == null) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                    "Atención", "No hay registro para guardar"));
            return;
        }
        try {
            // Validaciones mínimas
            if (selectedTipoProductoId == null) {
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "Atención", "Debe seleccionar un Tipo de Producto"));
                return;
            }
            if (selectedCaracteristicaId == null) {
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "Atención", "Debe seleccionar una Característica"));
                return;
            }

            // Armar relaciones
            TipoProducto tp = tipoProductoDao.getEntityManager().find(TipoProducto.class, selectedTipoProductoId);
            Caracteristica ca = caracteristicaDao.getEntityManager().find(Caracteristica.class, selectedCaracteristicaId);
            if (tp == null || ca == null) {
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Error", "No se pudo encontrar el Tipo de Producto o la Característica seleccionados"));
                return;
            }
            this.registro.setIdTipoProducto(tp);
            this.registro.setIdCaracteristica(ca);

            // Evitar duplicados (tipoProducto, caracteristica)
            boolean exists = tpcDao.existsByTipoProductoAndCaracteristica(selectedTipoProductoId, selectedCaracteristicaId);
            if (exists && (this.registro.getId() == null)) {
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "Duplicado", "Ya existe esta combinación de Tipo de Producto y Característica"));
                return;
            }

            // Crear (si no hay ID) o Modificar (si ya tiene ID)
            if (this.registro.getId() == null) {
                if (this.registro.getFechaCreacion() == null) {
                    this.registro.setFechaCreacion(OffsetDateTime.now());
                }
                tpcDao.crear(this.registro);
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Éxito", "Registro creado correctamente"));
            } else {
                tpcDao.modificar(this.registro);
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

    /* =======================
       Getters / Setters para UI
       ======================= */

    public Long getSelectedTipoProductoId() {
        return selectedTipoProductoId;
    }
    public void setSelectedTipoProductoId(Long selectedTipoProductoId) {
        this.selectedTipoProductoId = selectedTipoProductoId;
    }

    public Long getSelectedCaracteristicaId() {
        return selectedCaracteristicaId;
    }
    public void setSelectedCaracteristicaId(Long selectedCaracteristicaId) {
        this.selectedCaracteristicaId = selectedCaracteristicaId;
    }

    public List<TipoProducto> getListaTipoProducto() {
        if (listaTipoProducto == null || listaTipoProducto.isEmpty()) {
            cargarCombos();
        }
        return listaTipoProducto;
    }

    public List<Caracteristica> getListaCaracteristica() {
        if (listaCaracteristica == null || listaCaracteristica.isEmpty()) {
            cargarCombos();
        }
        return listaCaracteristica;
    }
}
