package sv.edu.ues.occ.ingenieria.prn335.inventario.web.boundary.servlet;

import jakarta.el.MethodExpression;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.*;

import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.InventarioDefaultDataAccess;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.TipoProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.TipoProducto;

@Named("tipoProductoFrm")
@ViewScoped
public class TipoProductoFrm extends DefaultFrm<TipoProducto> implements Serializable {

    @Inject
    FacesContext facesContext;

    @Inject
    TipoProductoDAO tipoProductoDao;

    private List<SelectItem> tiposProductoHierarchy;
    private Long tipoProductoPadreSeleccionado;
    private TreeNode root;
    private TreeNode selectedNode;

    public TipoProductoFrm() {
        this.nombreBean = "Tipo de Producto";
    }

    @Override
    protected FacesContext getFacesContext() {
        return facesContext;
    }

    @Override
    protected InventarioDefaultDataAccess<TipoProducto> getDao() {
        return tipoProductoDao;
    }

    @Override
    protected TipoProducto nuevoRegistro() {
        TipoProducto tp = new TipoProducto();
        tp.setActivo(true);
        tipoProductoPadreSeleccionado = null;
        return tp;
    }

    @Override
    protected TipoProducto buscarRegistroPorId(Object id) {
        if (id != null && id instanceof Integer buscado && this.modelo.getWrappedData().isEmpty()) {
            for (TipoProducto tp : (Iterable<TipoProducto>) tipoProductoDao.findAll()) {
                if (tp.getId().equals(buscado)) {
                    return tp;
                }
            }
        }
        return null;
    }

    @Override
    protected String getIdAsText(TipoProducto r) {
        if (r != null && r.getId() != null) {
            return r.getId().toString();
        }
        return null;
    }

    @Override
    protected TipoProducto getIdByText(String id) {
        if (id != null && this.modelo != null && !this.modelo.getWrappedData().isEmpty()) {
            try {
                Long buscado = Long.parseLong(id);
                return this.modelo.getWrappedData().stream()
                        .filter(r -> r.getId() != null && r.getId().equals(buscado))
                        .findFirst()
                        .orElse(null);
            } catch (NumberFormatException e) {
                System.err.println("ID no es un número válido: " + id);
                return null;
            }
        }
        return null;
    }

    /**
     * Carga TODOS los tipos de producto organizados jerárquicamente
     * Muestra la estructura padre-hijo de forma legible
     */
    private void cargarTiposProductoHierarchy() {
        try {
            tiposProductoHierarchy = new ArrayList<>();
            List<TipoProducto> todosTipos = (List<TipoProducto>) tipoProductoDao.findAll();

            if (todosTipos == null || todosTipos.isEmpty()) {
                System.out.println("No hay tipos de producto en la base de datos");
                return;
            }

            // Agregar primero los tipos raíz (sin padre)
            for (TipoProducto tp : todosTipos) {
                if (tp.getIdTipoProductoPadre() == null) {
                    tiposProductoHierarchy.add(new SelectItem(tp.getId(), tp.getNombre()));
                    System.out.println("Tipo raíz agregado: " + tp.getNombre() + " (ID: " + tp.getId() + ")");

                    // Agregar recursivamente los hijos
                    agregarHijosAlSelector(tp, todosTipos, "");
                }
            }

            System.out.println("Total de tipos cargados: " + tiposProductoHierarchy.size());

        } catch (Exception e) {
            System.err.println("Error al cargar tipos de producto: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void agregarHijosAlSelector(TipoProducto tipoPadre, List<TipoProducto> todosTipos, String prefijo) {
        // Buscar todos los hijos del tipo padre actual
        List<TipoProducto> hijos = todosTipos.stream()
                .filter(tp -> tp.getIdTipoProductoPadre() != null &&
                        tp.getIdTipoProductoPadre().getId().equals(tipoPadre.getId()))
                .sorted(Comparator.comparing(TipoProducto::getNombre))
                .toList();

        for (int i = 0; i < hijos.size(); i++) {
            TipoProducto hijo = hijos.get(i);
            boolean esUltimo = (i == hijos.size() - 1);

            // Crear representación visual con símbolos de árbol
            String simbolo = esUltimo ? "└─ " : "├─ ";
            String etiqueta = prefijo + simbolo + hijo.getNombre();

            tiposProductoHierarchy.add(new SelectItem(hijo.getId(), etiqueta));
            System.out.println("Hijo agregado: " + etiqueta + " (ID: " + hijo.getId() + ")");

            // Prefijo para los hijos del hijo actual
            String nuevoPrefijo = prefijo + (esUltimo ? "   " : "│  ");

            // Agregar recursivamente los hijos del hijo
            agregarHijosAlSelector(hijo, todosTipos, nuevoPrefijo);
        }
    }

    /**
     * Construye el árbol jerárquico completo de tipos de producto
     */
    private void construirArbolTipos() {
        try {
            root = new DefaultTreeNode("Tipos de Producto", null);
            List<TipoProducto> todosTipos = (List<TipoProducto>) tipoProductoDao.findAll();

            if (todosTipos != null && !todosTipos.isEmpty()) {
                // Agregar solo los tipos raíz (sin padre)
                for (TipoProducto tp : todosTipos) {
                    if (tp.getIdTipoProductoPadre() == null) {
                        TreeNode nodoRaiz = new DefaultTreeNode(tp, root);
                        agregarHijosRecursivo(tp, nodoRaiz, todosTipos);
                    }
                }
            }
            System.out.println("Árbol de tipos construido correctamente");
        } catch (Exception e) {
            System.err.println("Error al construir árbol: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Agrega recursivamente los hijos de un tipo de producto
     */
    private void agregarHijosRecursivo(TipoProducto tipoPadre, TreeNode nodoPadre, List<TipoProducto> todosTipos) {
        for (TipoProducto tp : todosTipos) {
            if (tp.getIdTipoProductoPadre() != null && tp.getIdTipoProductoPadre().getId().equals(tipoPadre.getId())) {
                TreeNode nodoHijo = new DefaultTreeNode(tp, nodoPadre);
                agregarHijosRecursivo(tp, nodoHijo, todosTipos);
            }
        }
    }

    /**
     * Maneja la selección de un nodo en el árbol
     */
    public void onTreeRowSelect(org.primefaces.event.NodeSelectEvent event) {
        try {
            TreeNode node = event.getTreeNode();
            if (node != null && node.getData() instanceof TipoProducto) {
                TipoProducto tp = (TipoProducto) node.getData();
                this.registro = tp;
                if (tp.getIdTipoProductoPadre() != null) {
                    tipoProductoPadreSeleccionado = tp.getIdTipoProductoPadre().getId();
                } else {
                    tipoProductoPadreSeleccionado = null;
                }
                estado = ESTADO_CRUD.MODIFICAR;
                System.out.println("Tipo seleccionado: " + tp.getNombre());
            }
        } catch (Exception e) {
            System.err.println("Error al seleccionar nodo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void btnGuardarHandler(ActionEvent actionEvent) {
        try {
            asignarTipoProductoPadre();
            super.btnGuardarHandler(actionEvent);
            cargarTiposProductoHierarchy();

            // Reconstruir árbol pero mantener la rama expandida
            root = null;
            construirArbolTipos();

            if (this.registro != null && this.registro.getId() != null) {
                Long idNodoPadre = null;
                if (this.registro.getIdTipoProductoPadre() != null) {
                    idNodoPadre = this.registro.getIdTipoProductoPadre().getId();
                }
                expandirYSeleccionarNodo(idNodoPadre, this.registro.getId());
            }

        } catch (Exception e) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error al guardar", e.getMessage()));
        }
    }


    @Override
    public void btnModificarHandler(ActionEvent actionEvent) {
        try {
            // Guardar el ID del nodo padre antes de modificar
            Long idNodoPadre = null;
            if (this.registro != null && this.registro.getIdTipoProductoPadre() != null) {
                idNodoPadre = this.registro.getIdTipoProductoPadre().getId();
            }

            asignarTipoProductoPadre();
            super.btnModificarHandler(actionEvent);
            cargarTiposProductoHierarchy();

            // Reconstruir árbol pero mantener la rama expandida
            root = null;
            construirArbolTipos();
            expandirYSeleccionarNodo(idNodoPadre, this.registro.getId());

        } catch (Exception e) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error al modificar", e.getMessage()));
        }
    }

    /**
     * Expande recursivamente el árbol hasta encontrar y seleccionar el nodo especificado
     */
    private void expandirYSeleccionarNodo(Long idNodoPadre, Long idNodoASeleccionar) {
        if (root == null) return;

        expandirNodosRecursivo(root, idNodoPadre, idNodoASeleccionar);
    }

    /**
     * Busca recursivamente y expande los nodos hasta el destino
     */
    private void expandirNodosRecursivo(TreeNode nodo, Long idNodoPadre, Long idNodoASeleccionar) {
        if (nodo.getData() instanceof TipoProducto) {
            TipoProducto tp = (TipoProducto) nodo.getData();

            // Si encontramos el nodo a seleccionar, lo seleccionamos
            if (tp.getId().equals(idNodoASeleccionar)) {
                nodo.setExpanded(true);
                selectedNode = nodo;
                System.out.println("Nodo seleccionado y expandido: " + tp.getNombre());
                return;
            }

            // Si este es el padre, expandir
            if (idNodoPadre != null && tp.getId().equals(idNodoPadre)) {
                nodo.setExpanded(true);
                System.out.println("Nodo padre expandido: " + tp.getNombre());
            }
        }

        // Buscar en los hijos
        if (nodo.getChildren() != null && !nodo.getChildren().isEmpty()) {
            for (Object hijo : nodo.getChildren()) {
                expandirNodosRecursivo((TreeNode) hijo, idNodoPadre, idNodoASeleccionar);
            }
        }
    }


    @Override
    public void btnEliminarHandler(ActionEvent actionEvent) {
        try {
            super.btnEliminarHandler(actionEvent);
            cargarTiposProductoHierarchy();
            root = null; // Reconstruir árbol
        } catch (Exception e) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error al eliminar", e.getMessage()));
        }
    }

    @Override
    public void btnCancelarHandler(ActionEvent actionEvent) {
        super.btnCancelarHandler(actionEvent);
        tipoProductoPadreSeleccionado = null;
        selectedNode = null;
    }

    @Override
    public void btnNuevoHandler(ActionEvent actionEvent) {
        tipoProductoPadreSeleccionado = null;
        tiposProductoHierarchy = null; // Limpiar cache
        cargarTiposProductoHierarchy();
        super.btnNuevoHandler(actionEvent);
    }

    /**
     * Sobrescribe selectionHandler para cargar el tipo padre en el selector
     */
    @Override
    public void selectionHandler(org.primefaces.event.SelectEvent<TipoProducto> r) {
        super.selectionHandler(r);
        if (this.registro != null && this.registro.getIdTipoProductoPadre() != null) {
            tipoProductoPadreSeleccionado = this.registro.getIdTipoProductoPadre().getId();
        } else {
            tipoProductoPadreSeleccionado = null;
        }
    }

    /**
     * Asigna el tipo padre seleccionado al registro
     */
    private void asignarTipoProductoPadre() {
        if (this.registro != null) {
            if (tipoProductoPadreSeleccionado != null && tipoProductoPadreSeleccionado > 0) {
                // Buscar el tipo padre en la lista de todos los tipos
                List<TipoProducto> todosTipos = (List<TipoProducto>) tipoProductoDao.findAll();
                TipoProducto tipoPadre = todosTipos.stream()
                        .filter(tp -> tp.getId().equals(tipoProductoPadreSeleccionado))
                        .findFirst()
                        .orElse(null);
                this.registro.setIdTipoProductoPadre(tipoPadre);
                System.out.println("Tipo padre asignado: " + (tipoPadre != null ? tipoPadre.getNombre() : "ninguno"));
            } else {
                this.registro.setIdTipoProductoPadre(null);
                System.out.println("Tipo padre limpiado (será tipo raíz)");
            }
        }
    }

    /**
     * Getter para el árbol jerárquico
     */
    public TreeNode getRoot() {
        if (root == null) {
            construirArbolTipos();
        }
        return root;
    }

    /**
     * Setter para el árbol jerárquico
     */
    public void setRoot(TreeNode root) {
        this.root = root;
    }

    /**
     * Getter para el nodo seleccionado
     */
    public TreeNode getSelectedNode() {
        return selectedNode;
    }

    /**
     * Setter para el nodo seleccionado
     */
    public void setSelectedNode(TreeNode selectedNode) {
        this.selectedNode = selectedNode;
    }

    /**
     * Getter para los tipos de producto - siempre carga fresco
     */
    public List<SelectItem> getTiposProductoHierarchy() {
        if (tiposProductoHierarchy == null) {
            cargarTiposProductoHierarchy();
        }
        return tiposProductoHierarchy;
    }

    public Long getTipoProductoPadreSeleccionado() {
        return tipoProductoPadreSeleccionado;
    }

    public void setTipoProductoPadreSeleccionado(Long tipoProductoPadreSeleccionado) {
        this.tipoProductoPadreSeleccionado = tipoProductoPadreSeleccionado;
    }
}
