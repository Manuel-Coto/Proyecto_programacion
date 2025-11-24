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

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase base para todos los formularios de gestión CRUD
 * Proporciona funcionalidad común de crear, leer, actualizar y eliminar registros
 */
public abstract class DefaultFrm<T> implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(DefaultFrm.class.getName());

    protected ESTADO_CRUD estado = ESTADO_CRUD.NADA;
    protected String nombreBean;
    protected LazyDataModel<T> modelo;
    protected T registro;
    protected int pageSize = 8;

    // ===== Métodos abstractos que deben implementar las subclases =====

    /**
     * Obtiene el FacesContext actual
     */
    protected abstract FacesContext getFacesContext();

    /**
     * Obtiene el DAO para acceder a datos
     */
    protected abstract InventarioDefaultDataAccess<T> getDao();

    /**
     * Crea un nuevo registro vacío
     */
    protected abstract T nuevoRegistro();

    /**
     * Busca un registro por su ID
     */
    protected abstract T buscarRegistroPorId(Object id);

    /**
     * Convierte el ID a texto
     */
    protected abstract String getIdAsText(T r);

    /**
     * Obtiene un registro desde su ID en texto
     */
    protected abstract T getIdByText(String id);

    // ===== Inicialización =====

    @PostConstruct
    public void inicializar() {
        LOGGER.log(Level.INFO, "Inicializando {0}", this.getClass().getSimpleName());
        inicializarRegistros();
    }

    /**
     * Inicializa el modelo lazy de PrimeFaces para la tabla
     */
    public void inicializarRegistros() {
        this.modelo = new LazyDataModel<T>() {
            private static final long serialVersionUID = 1L;

            @Override
            public String getRowKey(T object) {
                if (object != null) {
                    try {
                        return getIdAsText(object);
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Error al obtener rowKey", e);
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
                        LOGGER.log(Level.SEVERE, "Error al obtener rowData", e);
                    }
                }
                return null;
            }

            @Override
            public int count(Map<String, FilterMeta> map) {
                try {
                    Long total = getDao().count();
                    return total != null ? total.intValue() : 0;
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error al contar registros", e);
                }
                return 0;
            }

            @Override
            public List<T> load(int first, int max, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
                try {
                    List<T> resultado = getDao().findRange(first, max);
                    return resultado != null ? resultado : Collections.emptyList();
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error al cargar registros", e);
                }
                return Collections.emptyList();
            }
        };
    }

    // ===== Handlers de eventos =====

    /**
     * Handler para seleccionar un registro de la tabla
     */
    public void selectionHandler(SelectEvent<T> event) {
        LOGGER.log(Level.INFO, "Registro seleccionado en tabla");
        if (event != null && event.getObject() != null) {
            this.registro = event.getObject();
            this.estado = ESTADO_CRUD.MODIFICAR;
            LOGGER.log(Level.INFO, "Estado cambiado a MODIFICAR");
        }
    }

    /**
     * Handler para el botón "Nuevo"
     */
    public void btnNuevoHandler(ActionEvent actionEvent) {
        LOGGER.log(Level.INFO, "Botón nuevo presionado");
        this.registro = nuevoRegistro();
        this.estado = ESTADO_CRUD.CREAR;
        LOGGER.log(Level.INFO, "Estado cambiado a CREAR");
    }

    /**
     * Handler para el botón "Cancelar"
     */
    public void btnCancelarHandler(ActionEvent actionEvent) {
        LOGGER.log(Level.INFO, "Botón cancelar presionado");
        this.registro = null;
        this.estado = ESTADO_CRUD.NADA;
        LOGGER.log(Level.INFO, "Estado cambiado a NADA");
    }

    /**
     * Handler para guardar un nuevo registro
     */
    public void btnGuardarHandler(ActionEvent actionEvent) {
        LOGGER.log(Level.INFO, "Botón guardar presionado");

        if (this.registro == null) {
            LOGGER.log(Level.WARNING, "Intento de guardar con registro nulo");
            addMessage(FacesMessage.SEVERITY_WARN, "Atención", "No hay registro para guardar");
            return;
        }

        try {
            if (esNombreVacio(this.registro)) {
                LOGGER.log(Level.WARNING, "Validación fallida: nombre vacío");
                addMessage(FacesMessage.SEVERITY_WARN, "Atención", "El registro no cumple validaciones");
                return;
            }

            LOGGER.log(Level.INFO, "Creando nuevo registro...");
            getDao().crear(this.registro);

            this.registro = null;
            this.estado = ESTADO_CRUD.NADA;
            this.modelo = null;
            inicializarRegistros();

            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Registro guardado correctamente");
            LOGGER.log(Level.INFO, "Registro guardado exitosamente");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al guardar", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Error al guardar", e.getMessage());
        }
    }

    /**
     * Handler para eliminar un registro
     */
    public void btnEliminarHandler(ActionEvent actionEvent) {
        LOGGER.log(Level.INFO, "Botón eliminar presionado");

        if (this.registro == null) {
            LOGGER.log(Level.WARNING, "Intento de eliminar con registro nulo");
            addMessage(FacesMessage.SEVERITY_WARN, "Atención", "No hay registro para eliminar");
            return;
        }

        try {
            LOGGER.log(Level.INFO, "Eliminando registro...");
            getDao().eliminar(this.registro);

            this.registro = null;
            this.estado = ESTADO_CRUD.NADA;
            this.modelo = null;
            inicializarRegistros();

            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Registro eliminado correctamente");
            LOGGER.log(Level.INFO, "Registro eliminado exitosamente");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Error al eliminar", e.getMessage());
        }
    }

    /**
     * Handler para modificar un registro existente
     */
    public void btnModificarHandler(ActionEvent actionEvent) {
        LOGGER.log(Level.INFO, "Botón modificar presionado");

        if (this.registro == null) {
            LOGGER.log(Level.WARNING, "Intento de modificar con registro nulo");
            addMessage(FacesMessage.SEVERITY_WARN, "Atención", "No hay registro para modificar");
            return;
        }

        try {
            if (esNombreVacio(this.registro)) {
                LOGGER.log(Level.WARNING, "Validación fallida: nombre vacío");
                addMessage(FacesMessage.SEVERITY_WARN, "Atención", "El registro no cumple validaciones");
                return;
            }

            LOGGER.log(Level.INFO, "Modificando registro...");
            getDao().modificar(this.registro);

            this.registro = null;
            this.estado = ESTADO_CRUD.NADA;
            this.modelo = null;
            inicializarRegistros();

            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Registro modificado correctamente");
            LOGGER.log(Level.INFO, "Registro modificado exitosamente");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al modificar", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Error al modificar", e.getMessage());
        }
    }

    // ===== Métodos de validación =====

    /**
     * Valida si el registro está vacío o no cumple los requisitos mínimos
     * Subclases deben sobrescribir este método para validaciones específicas
     */
    protected boolean esNombreVacio(T registro) {
        if (registro == null) {
            return true;
        }

        try {
            // Intenta obtener el campo nombre usando reflexión
            java.lang.reflect.Method metodoGetNombre = registro.getClass().getMethod("getNombre");
            String nombre = (String) metodoGetNombre.invoke(registro);
            return nombre == null || nombre.trim().isEmpty();
        } catch (Exception e) {
            // Si no existe el método getNombre, permite la validación en la subclase
            LOGGER.log(Level.FINE, "Método getNombre no encontrado, usando validación default");
            return false;
        }
    }

    /**
     * Agrega un mensaje a la interfaz
     */
    protected void addMessage(FacesMessage.Severity severityInfo, String éxito, String registroModificadoCorrectamente) {
        FacesContext context = getFacesContext();
        if (context != null) {
            context.addMessage(null, new FacesMessage());
        }
    }

    // ===== Getters y Setters =====

    public ESTADO_CRUD getEstado() {
        return estado;
    }

    public void setEstado(ESTADO_CRUD estado) {
        this.estado = estado;
    }

    public String getNombreBean() {
        return nombreBean;
    }

    public void setNombreBean(String nombreBean) {
        this.nombreBean = nombreBean;
    }

    public T getRegistro() {
        return registro;
    }

    public void setRegistro(T registro) {
        this.registro = registro;
    }

    public LazyDataModel<T> getModelo() {
        return modelo;
    }

    public void setModelo(LazyDataModel<T> modelo) {
        this.modelo = modelo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}