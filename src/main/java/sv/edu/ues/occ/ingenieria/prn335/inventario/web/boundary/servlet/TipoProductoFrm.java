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
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.CaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.InventarioDefaultDataAccess;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.TipoProductoCaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.TipoProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.Caracteristica;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.TipoProducto;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.TipoProductoCaracteristica;

@Named("tipoProductoFrm")
@ViewScoped
public class TipoProductoFrm extends DefaultFrm<TipoProducto> implements Serializable {

    /* =======================
       Inyección de dependencias
       ======================= */
    @Inject private TipoProductoDAO tipoProductoDao;
    @Inject private CaracteristicaDAO caracteristicaDao;
    @Inject private TipoProductoCaracteristicaDAO tipoProductoCaracteristicaDao;

    /* =======================
       Árbol (TreeTable)
       ======================= */
    private TreeNode<TipoProducto> root;
    private TreeNode<TipoProducto> selectedNode;

    /* =======================
       Combo "Depende de"
       ======================= */
    public static class OpcionPadre {
        private final Long id;
        private final String label;
        public OpcionPadre(Long id, String label) { this.id = id; this.label = label; }
        public Long getId() { return id; }
        public String getLabel() { return label; }
    }
    private List<OpcionPadre> opcionesPadre;
    private Long padreSeleccionadoId;

    /* =======================
       Pestaña: Características
       ======================= */
    private List<TipoProductoCaracteristica> listaTPC;     // tabla
    private TipoProductoCaracteristica tpcRegistro;        // form (crear/editar)
    private boolean tpcMostrandoFormulario = false;

    // Diálogo buscar/seleccionar característica
    private boolean dlgBuscarVisible = false;
    private String filtroCaracteristica;
    private List<Caracteristica> resultadosBusqueda;

    /* =======================
       Ciclo de vida
       ======================= */
    @PostConstruct
    @Override
    public void inicializar() {
        this.nombreBean = "Tipo de Producto";
        super.inicializar();
        construirArbol();
        cargarOpcionesPadre();
        cargarTPC();
        prepararNuevoTPC();
    }

    /* =======================
       Implementaciones DefaultFrm
       ======================= */
    @Override protected FacesContext getFacesContext() { return FacesContext.getCurrentInstance(); }
    @Override protected InventarioDefaultDataAccess<TipoProducto> getDao() { return tipoProductoDao; }

    @Override
    protected TipoProducto nuevoRegistro() {
        TipoProducto t = new TipoProducto();
        t.setNombre("");
        t.setActivo(Boolean.TRUE);
        t.setComentarios("");
        t.setIdTipoProductoPadre(null);
        padreSeleccionadoId = null;
        cargarOpcionesPadre();
        cargarTPC();
        prepararNuevoTPC();
        return t;
    }

    @Override
    protected TipoProducto buscarRegistroPorId(Object id) {
        if (id == null) return null;
        try {
            Long lid = (id instanceof Long) ? (Long) id : Long.valueOf(String.valueOf(id));
            return tipoProductoDao.getEntityManager().find(TipoProducto.class, lid);
        } catch (Exception e) { return null; }
    }

    @Override protected String getIdAsText(TipoProducto r) { return (r!=null && r.getId()!=null) ? String.valueOf(r.getId()) : null; }
    @Override protected TipoProducto getIdByText(String id) { return (id==null) ? null : buscarRegistroPorId(id); }

    /* =======================
       Selección de fila (tabla/árbol)
       ======================= */
    @Override
    public void selectionHandler(SelectEvent<TipoProducto> ev) {
        super.selectionHandler(ev);
        padreSeleccionadoId = (registro!=null && registro.getIdTipoProductoPadre()!=null)
                ? registro.getIdTipoProductoPadre().getId() : null;
        cargarOpcionesPadre();
        cargarTPC();
        prepararNuevoTPC();
    }

    public void onTreeRowSelect() {
        if (selectedNode != null) {
            this.registro = selectedNode.getData();
            this.estado = ESTADO_CRUD.MODIFICAR;
            padreSeleccionadoId = (registro!=null && registro.getIdTipoProductoPadre()!=null)
                    ? registro.getIdTipoProductoPadre().getId() : null;
            cargarOpcionesPadre();
            cargarTPC();
            prepararNuevoTPC();
        }
    }

    @Override
    public void btnNuevoHandler(ActionEvent e) {
        super.btnNuevoHandler(e);
        selectedNode = null;
        padreSeleccionadoId = null;
        cargarOpcionesPadre();
        cargarTPC();
        prepararNuevoTPC();
    }

    @Override
    public void btnCancelarHandler(ActionEvent e) {
        super.btnCancelarHandler(e);
        padreSeleccionadoId = (registro!=null && registro.getIdTipoProductoPadre()!=null)
                ? registro.getIdTipoProductoPadre().getId() : null;
        cargarOpcionesPadre();
        cargarTPC();
        prepararNuevoTPC();
    }

    @Override
    public void btnGuardarHandler(ActionEvent e) {
        try {
            if (registro != null && registro.getId()!=null && padreSeleccionadoId!=null
                    && registro.getId().equals(padreSeleccionadoId)) {
                msg(FacesMessage.SEVERITY_WARN, "Padre inválido", "No puede asignarse como su propio padre.");
                return;
            }
            if (padreSeleccionadoId != null && registro.getId()!=null
                    && getDescendientesIds(registro.getId()).contains(padreSeleccionadoId)) {
                msg(FacesMessage.SEVERITY_WARN, "Relación inválida", "No puede asignar como padre a un descendiente.");
                return;
            }

            registro.setIdTipoProductoPadre(padreSeleccionadoId == null ? null : buscarRegistroPorId(padreSeleccionadoId));
            super.btnGuardarHandler(e);

            construirArbol();
            cargarOpcionesPadre();
            cargarTPC();
            prepararNuevoTPC();
        } catch (Exception ex) {
            msg(FacesMessage.SEVERITY_ERROR, "Error al guardar", ex.getMessage());
        }
    }

    @Override
    public void btnModificarHandler(ActionEvent e) {
        try {
            if (registro == null) return;
            if (registro.getId()!=null && padreSeleccionadoId!=null
                    && registro.getId().equals(padreSeleccionadoId)) {
                msg(FacesMessage.SEVERITY_WARN, "Padre inválido", "No puede asignarse como su propio padre.");
                return;
            }
            if (padreSeleccionadoId != null && registro.getId()!=null
                    && getDescendientesIds(registro.getId()).contains(padreSeleccionadoId)) {
                msg(FacesMessage.SEVERITY_WARN, "Relación inválida", "No puede asignar como padre a un descendiente.");
                return;
            }

            registro.setIdTipoProductoPadre(padreSeleccionadoId == null ? null : buscarRegistroPorId(padreSeleccionadoId));
            super.btnModificarHandler(e);

            construirArbol();
            cargarOpcionesPadre();
            cargarTPC();
            prepararNuevoTPC();
        } catch (Exception ex) {
            msg(FacesMessage.SEVERITY_ERROR, "Error al modificar", ex.getMessage());
        }
    }

    @Override
    public void btnEliminarHandler(ActionEvent e) {
        super.btnEliminarHandler(e);
        construirArbol();
        padreSeleccionadoId = null;
        selectedNode = null;
        cargarTPC();
        prepararNuevoTPC();
    }

    /* =======================
       Árbol
       ======================= */
    public void construirArbol() {
        try {
            root = new DefaultTreeNode<>(null, null);
            List<TipoProducto> raices = tipoProductoDao.findRoots();
            if (raices != null) for (TipoProducto r : raices) {
                TreeNode<TipoProducto> n = new DefaultTreeNode<>("nodo", r, root);
                cargarHijosRec(n, r);
            }
            root.setExpanded(true);
        } catch (Exception ignored) {
            root = new DefaultTreeNode<>(null, null);
            root.setExpanded(true);
        }
    }

    private void cargarHijosRec(TreeNode<TipoProducto> padreNode, TipoProducto padreEntity) {
        List<TipoProducto> hijos = tipoProductoDao.findChildren(padreEntity.getId());
        if (hijos == null || hijos.isEmpty()) return;
        for (TipoProducto h : hijos) {
            TreeNode<TipoProducto> n = new DefaultTreeNode<>("nodo", h, padreNode);
            cargarHijosRec(n, h);
        }
    }

    /* =======================
       Combo "Depende de"
       ======================= */
    private void cargarOpcionesPadre() {
        List<OpcionPadre> out = new ArrayList<>();
        Set<Long> excluir = new HashSet<>();
        Long idActual = (this.registro != null) ? this.registro.getId() : null;
        if (idActual != null) {
            excluir.add(idActual);
            excluir.addAll(getDescendientesIds(idActual));
        }
        List<TipoProducto> raices = tipoProductoDao.findRoots();
        if (raices != null) for (TipoProducto r : raices) buildJerarquia(r, 0, excluir, out);
        this.opcionesPadre = out;
    }

    private void buildJerarquia(TipoProducto nodo, int depth, Set<Long> excluir, List<OpcionPadre> out) {
        if (nodo == null || (nodo.getId()!=null && excluir.contains(nodo.getId()))) return;
        String prefijo = (depth == 0) ? "" : " ".repeat(Math.max(0, depth-1)) + "↳ ";
        out.add(new OpcionPadre(nodo.getId(), prefijo + nodo.getNombre()));
        List<TipoProducto> hijos = tipoProductoDao.findChildren(nodo.getId());
        if (hijos != null) for (TipoProducto h : hijos) buildJerarquia(h, depth + 1, excluir, out);
    }

    private Set<Long> getDescendientesIds(Long id) {
        Set<Long> res = new HashSet<>();
        if (id == null) return res;
        Deque<Long> st = new ArrayDeque<>();
        st.push(id);
        while (!st.isEmpty()) {
            Long cur = st.pop();
            List<TipoProducto> hijos = tipoProductoDao.findChildren(cur);
            if (hijos != null) for (TipoProducto h : hijos) if (h.getId()!=null && res.add(h.getId())) st.push(h.getId());
        }
        return res;
    }

    /* =======================
       Pestaña: Características
       ======================= */
    public void cargarTPC() {
        if (this.registro != null && this.registro.getId() != null) {
            this.listaTPC = tipoProductoCaracteristicaDao.findByTipoProductoIdFetch(this.registro.getId());
        } else {
            this.listaTPC = Collections.emptyList();
        }
    }

    private void prepararNuevoTPC() {
        this.tpcRegistro = new TipoProductoCaracteristica();
        this.tpcRegistro.setIdTipoProducto(this.registro); // puede ser null si aún no guardas el tipo
        this.tpcRegistro.setObligatorio(Boolean.FALSE);
        this.tpcRegistro.setFechaCreacion(OffsetDateTime.now());
        this.tpcMostrandoFormulario = false;

        this.filtroCaracteristica = "";
        this.resultadosBusqueda = Collections.emptyList();
    }

    public void tpcBtnNuevo() {
        if (this.registro == null || (this.registro.getId() == null && this.estado != ESTADO_CRUD.CREAR)) {
            msg(FacesMessage.SEVERITY_WARN, "Atención", "Primero cree o seleccione un Tipo de Producto.");
            return;
        }
        prepararNuevoTPC();
        this.tpcMostrandoFormulario = true;
    }

    public void tpcBtnCancelar() { prepararNuevoTPC(); }

    public void tpcSeleccionarAbrirDialogo() {
        this.dlgBuscarVisible = true;
        this.filtroCaracteristica = "";
        this.resultadosBusqueda = Collections.emptyList();
    }

    public void tpcBuscarCaracteristicas() {
        String q = (filtroCaracteristica == null) ? "" : filtroCaracteristica.trim();
        if (q.isEmpty()) {
            this.resultadosBusqueda = caracteristicaDao.findAll();
        } else {
            this.resultadosBusqueda = caracteristicaDao.findByNombreLike(q, 0, 30);
        }
    }

    public void tpcSeleccionarCaracteristica(Caracteristica c) {
        if (c == null) return;
        if (this.tpcRegistro == null) this.tpcRegistro = new TipoProductoCaracteristica();
        this.tpcRegistro.setIdCaracteristica(c);
        this.dlgBuscarVisible = false;
    }

    /** Guarda/actualiza la asociación TPC. Usa crearYFlush / modificarYFlush del DAO. */
    public void tpcGuardar() {
        // Validaciones básicas de contexto
        if (this.registro == null || (this.registro.getId() == null && this.estado != ESTADO_CRUD.CREAR)) {
            msg(FacesMessage.SEVERITY_WARN, "Atención", "Primero cree o seleccione un Tipo de Producto.");
            return;
        }
        if (this.registro.getId() == null && this.estado == ESTADO_CRUD.CREAR) {
            msg(FacesMessage.SEVERITY_WARN, "Pendiente", "Guarde el Tipo de Producto antes de asociar características.");
            return;
        }
        if (this.tpcRegistro == null || this.tpcRegistro.getIdCaracteristica() == null) {
            msg(FacesMessage.SEVERITY_WARN, "Validación", "Seleccione una característica.");
            return;
        }

        Long idTipo = this.registro.getId();
        Integer idCar = this.tpcRegistro.getIdCaracteristica().getId();

        try {
            boolean existe = tipoProductoCaracteristicaDao
                    .existsByTipoProductoAndCaracteristica(idTipo, Long.valueOf(idCar));

            if (!existe) {
                this.tpcRegistro.setIdTipoProducto(this.registro);
                if (this.tpcRegistro.getFechaCreacion() == null) {
                    this.tpcRegistro.setFechaCreacion(OffsetDateTime.now());
                }
                // >>> ahora el flush ocurre dentro del EJB
                tipoProductoCaracteristicaDao.crearYFlush(this.tpcRegistro);
                msg(FacesMessage.SEVERITY_INFO, "Guardado", "Característica asociada correctamente.");
            } else {
                TipoProductoCaracteristica existente =
                        tipoProductoCaracteristicaDao
                                .findByTipoProductoAndCaracteristica(idTipo, Long.valueOf(idCar)).orElse(null);
                if (existente != null) {
                    existente.setObligatorio(Boolean.TRUE.equals(this.tpcRegistro.getObligatorio()));
                    // >>> ahora el flush ocurre dentro del EJB
                    tipoProductoCaracteristicaDao.modificarYFlush(existente);
                    msg(FacesMessage.SEVERITY_INFO, "Actualizado", "Asociación actualizada.");
                }
            }

            cargarTPC();
            prepararNuevoTPC();

        } catch (Exception ex) {
            msg(FacesMessage.SEVERITY_ERROR, "Error al guardar característica", ex.getMessage());
        }
    }

    public void tpcEliminar(TipoProductoCaracteristica tpc) {
        if (tpc == null || tpc.getId() == null) {
            msg(FacesMessage.SEVERITY_WARN, "Atención", "Seleccione una asociación válida para eliminar.");
            return;
        }
        try {
            // No es necesario flush aquí; el EJB hace commit al terminar.
            tipoProductoCaracteristicaDao.eliminar(tpc);
            msg(FacesMessage.SEVERITY_INFO, "Eliminado", "Asociación eliminada correctamente.");
            cargarTPC();
            prepararNuevoTPC();
        } catch (Exception ex) {
            msg(FacesMessage.SEVERITY_ERROR, "Error al eliminar asociación", ex.getMessage());
        }
    }

    public String getTpcFechaCreacionTexto() {
        if (tpcRegistro == null || tpcRegistro.getFechaCreacion() == null) return "";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return fmt.format(tpcRegistro.getFechaCreacion());
    }

    /* =======================
       Helpers UI
       ======================= */
    private void msg(FacesMessage.Severity s, String sum, String det) {
        getFacesContext().addMessage(null, new FacesMessage(s, sum, det));
    }

    /* =======================
       Getters / Setters para la vista
       ======================= */
    public TreeNode<TipoProducto> getRoot() { return root; }
    public TreeNode<TipoProducto> getSelectedNode() { return selectedNode; }
    public void setSelectedNode(TreeNode<TipoProducto> n) { this.selectedNode = n; }

    public List<OpcionPadre> getOpcionesPadreJerarquicas() { return opcionesPadre; }
    public Long getPadreSeleccionadoId() { return padreSeleccionadoId; }
    public void setPadreSeleccionadoId(Long id) { this.padreSeleccionadoId = id; }

    public List<TipoProductoCaracteristica> getListaTPC() { return listaTPC; }
    public TipoProductoCaracteristica getTpcRegistro() { return tpcRegistro; }
    public void setTpcRegistro(TipoProductoCaracteristica r) { this.tpcRegistro = r; this.tpcMostrandoFormulario = (r!=null); }

    public boolean isTpcMostrandoFormulario() { return tpcMostrandoFormulario; }

    public boolean isDlgBuscarVisible() { return dlgBuscarVisible; }
    public void setDlgBuscarVisible(boolean v) { this.dlgBuscarVisible = v; }

    public String getFiltroCaracteristica() { return filtroCaracteristica; }
    public void setFiltroCaracteristica(String f) { this.filtroCaracteristica = f; }

    public List<Caracteristica> getResultadosBusqueda() { return resultadosBusqueda; }
}
