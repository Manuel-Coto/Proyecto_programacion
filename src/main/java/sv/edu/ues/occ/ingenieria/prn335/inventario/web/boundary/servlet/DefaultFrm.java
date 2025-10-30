package sv.edu.ues.occ.ingenieria.prn335.inventario.web.boundary.servlet;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.InventarioDefaultDataAccess;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.Almacen;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class DefaultFrm<T> implements Serializable {
    ESTADO_CRUD estado = ESTADO_CRUD.NADA;


    protected String nombreBean;
    protected abstract FacesContext getFacesContext();
    protected abstract InventarioDefaultDataAccess<T> getDao();

    protected LazyDataModel<T> modelo;

    protected T registro;
    protected abstract T nuevoRegistro();
    protected abstract T buscarRegistroPorId(Object id);


    public abstract void inicializarListas();

    protected abstract String getIdAsText(T r);
    protected abstract T getIdByText(String id);

    protected int pageSize = 8;

    @PostConstruct
    public void inicializar() {
        inicializarRegistros();
    }

    public void inicializarRegistros() {
        this.modelo = new LazyDataModel<T>() {
            @Override
            public String getRowKey(T object) {
                if (object != null) {
                    try {
                        return getIdAsText(object);
                    } catch (Exception e) {
                        Logger.getLogger(DefaultFrm.class.getName()).log(Level.SEVERE, null, e);
                    }
                }
                return null;
            }

            @Override
            public T getRowData(String rowKey) {
                if (rowKey != null) {
                    try {
                        return getIdByText(rowKey);
                    } catch (Exception e) {
                        Logger.getLogger(DefaultFrm.class.getName()).log(Level.SEVERE, null, e);
                    }
                }
                return null;
            }

            @Override
            public int count(Map<String, FilterMeta> map) {
                try {
                    return getDao().count();
                } catch (Exception e) {
                    Logger.getLogger(DefaultFrm.class.getName()).log(Level.SEVERE, null, e);
                }
                return 0;
            }

            @Override
            public List<T> load(int first, int max, Map<String, SortMeta> sort, Map<String, FilterMeta> filter) {
                try {
                    return getDao().findRange(first, max);
                } catch (Exception e) {
                    Logger.getLogger(DefaultFrm.class.getName()).log(Level.SEVERE, null, e);
                }
                return Collections.emptyList();
            }
        };
    }

    public void selectionHandler(SelectEvent<T> r) {
        if (r != null) {
            this.registro = r.getObject();
            this.estado = ESTADO_CRUD.MODIFICAR;
        }
    }

    public void btnNuevoHandler(ActionEvent e) {
        this.registro = nuevoRegistro();
        this.estado = ESTADO_CRUD.CREAR;
    }

    public void btnCancelarHandler(ActionEvent e) {
        this.registro = null;
        this.estado = ESTADO_CRUD.NADA;
    }

    public void btnGuardarHandler(ActionEvent e) {
        if (this.registro != null) {
            try {
                if (esNombreVacio(this.registro)) {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "El nombre no puede estar vacío"));
                    return;
                }
                getDao().crear(this.registro);
                this.registro = null;
                this.estado = ESTADO_CRUD.NADA;
                this.modelo = null;
                inicializarRegistros();
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Registro guardado correctamente"));
            } catch (Exception ex) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al guardar", ex.getMessage()));
            }
        }
    }

    public void btnEliminarHandler(ActionEvent e) {
        if (this.registro != null) {
            try {
                getDao().eliminar(this.registro);
                this.registro = null;
                this.estado = ESTADO_CRUD.NADA;
                inicializarRegistros();
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Registro eliminado correctamente"));
            } catch (Exception ex) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al eliminar", ex.getMessage()));
            }
        }
    }

    public void btnModificarHandler(ActionEvent e) {
        try {
            this.getDao().modificar(this.registro);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Modificado correctamente", null));
            this.estado = ESTADO_CRUD.NADA;
            inicializarRegistros();
        } catch (Exception ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al modificar", ex.getMessage()));
        }
    }

    private boolean esNombreVacio(T registro) {
        try {
            var m = registro.getClass().getMethod("getNombre");
            String nombre = (String) m.invoke(registro);
            return nombre == null || nombre.trim().isEmpty();
        } catch (Exception e) {
            return false; // si no hay getNombre(), no aplica esta validación
        }
    }

    public ESTADO_CRUD getEstado() { return estado; }
    public void setEstado(ESTADO_CRUD estado) { this.estado = estado; }
    public String getNombreBean() { return nombreBean; }
    public void setNombreBean(String nombreBean) { this.nombreBean = nombreBean; }
    public T getRegistro() { return registro; }
    public void setRegistro(T registro) { this.registro = registro; }


    /* >>> CORRECTO: el modelo es LazyDataModel<T> */
    public LazyDataModel<T> getModelo() { return modelo; }
    public void setModelo(LazyDataModel<T> modelo) { this.modelo = modelo; }

    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
}
