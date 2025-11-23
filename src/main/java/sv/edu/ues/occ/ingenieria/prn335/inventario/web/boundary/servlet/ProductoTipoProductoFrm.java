package sv.edu.ues.occ.ingenieria.prn335.inventario.web.boundary.servlet;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.primefaces.event.SelectEvent;

import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.ProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.ProductoTipoProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.TipoProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.Producto;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.ProductoTipoProducto;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.TipoProducto;

@Named("productoTipoProductoFrm")
@ViewScoped
public class ProductoTipoProductoFrm extends DefaultFrm<ProductoTipoProducto> implements Serializable {
    private static final Logger LOGGER = Logger.getLogger(ProductoTipoProductoFrm.class.getName());

    @EJB
    private ProductoTipoProductoDAO productoTipoProductoDAO;

    @EJB
    private ProductoDAO productoDAO;

    @EJB
    private TipoProductoDAO tipoProductoDAO;

    private List<Producto> listaProductos;
    private List<TipoProducto> listaTipoProductos;

    // ✅ CAMBIO: Ahora es Producto (objeto completo), no UUID
    private Producto idProductoSeleccionado;
    private Long idTipoProductoSeleccionado;

    @PostConstruct
    @Override
    public void inicializar() {
        super.inicializar();
        this.nombreBean = "Gestión de Producto - Tipo Producto";
        cargarListaProductos();
        cargarListaTipoProductos();
        LOGGER.log(Level.INFO, "ProductoTipoProductoFrm inicializado correctamente");
    }

    private void cargarListaProductos() {
        try {
            this.listaProductos = productoDAO != null ? productoDAO.findAll() : new ArrayList<>();
            LOGGER.log(Level.INFO, "Productos cargados: {0}", listaProductos.size());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al cargar productos", e);
            this.listaProductos = new ArrayList<>();
        }
    }

    private void cargarListaTipoProductos() {
        try {
            this.listaTipoProductos = tipoProductoDAO != null ? tipoProductoDAO.findAll() : new ArrayList<>();
            LOGGER.log(Level.INFO, "Tipos de producto cargados: {0}", listaTipoProductos.size());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al cargar tipos de producto", e);
            this.listaTipoProductos = new ArrayList<>();
        }
    }

    @Override
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    @Override
    protected ProductoTipoProductoDAO getDao() {
        return productoTipoProductoDAO;
    }

    @Override
    protected ProductoTipoProducto nuevoRegistro() {
        ProductoTipoProducto nuevo = new ProductoTipoProducto();
        nuevo.setId(UUID.randomUUID());
        nuevo.setFechaCreacion(OffsetDateTime.now());
        nuevo.setActivo(true);
        return nuevo;
    }

    @Override
    protected ProductoTipoProducto buscarRegistroPorId(Object id) {
        if (id instanceof UUID) {
            return productoTipoProductoDAO.findById((UUID) id);
        }
        return null;
    }

    @Override
    protected String getIdAsText(ProductoTipoProducto r) {
        if (r != null && r.getId() != null) {
            return r.getId().toString();
        }
        return null;
    }

    @Override
    protected ProductoTipoProducto getIdByText(String id) {
        if (id != null && !id.isEmpty()) {
            try {
                return buscarRegistroPorId(UUID.fromString(id));
            } catch (IllegalArgumentException e) {
                LOGGER.log(Level.WARNING, "Error al convertir ID: " + e.getMessage());
            }
        }
        return null;
    }

    @Override
    protected boolean esNombreVacio(ProductoTipoProducto registro) {
        return registro.getIdProducto() == null || registro.getIdTipoProducto() == null;
    }

    @Override
    public void btnGuardarHandler(ActionEvent actionEvent) {
        if (this.registro != null) {
            try {
                if (idProductoSeleccionado == null || idTipoProductoSeleccionado == null) {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención",
                                    "Debe seleccionar Producto y Tipo de Producto"));
                    return;
                }

                // ✅ idProductoSeleccionado YA ES UN OBJETO Producto (gracias al converter)
                registro.setIdProducto(idProductoSeleccionado);

                // Buscar TipoProducto por ID
                TipoProducto tipo = tipoProductoDAO.findById(idTipoProductoSeleccionado);
                if (tipo == null) {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                                    "El tipo de producto seleccionado no existe"));
                    return;
                }
                registro.setIdTipoProducto(tipo);

                if (estado == ESTADO_CRUD.CREAR) {
                    getDao().crear(registro);
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito",
                                    "Registro creado correctamente"));
                } else if (estado == ESTADO_CRUD.MODIFICAR) {
                    getDao().modificar(registro);
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito",
                                    "Registro modificado correctamente"));
                }

                registro = null;
                estado = ESTADO_CRUD.NADA;
                inicializarRegistros();
                idProductoSeleccionado = null;
                idTipoProductoSeleccionado = null;

            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error al guardar", e);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al guardar",
                                e.getMessage()));
            }
        }
    }

    @Override
    public void btnModificarHandler(ActionEvent actionEvent) {
        if (this.registro != null) {
            try {
                if (idProductoSeleccionado == null || idTipoProductoSeleccionado == null) {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención",
                                    "Debe seleccionar Producto y Tipo de Producto"));
                    return;
                }

                // ✅ idProductoSeleccionado YA ES UN OBJETO Producto
                registro.setIdProducto(idProductoSeleccionado);

                TipoProducto tipo = tipoProductoDAO.findById(idTipoProductoSeleccionado);
                if (tipo == null) {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                                    "El tipo de producto seleccionado no existe"));
                    return;
                }
                registro.setIdTipoProducto(tipo);

                getDao().modificar(this.registro);

                this.registro = null;
                this.estado = ESTADO_CRUD.NADA;
                inicializarRegistros();
                this.idProductoSeleccionado = null;
                this.idTipoProductoSeleccionado = null;

                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito",
                                "Registro modificado correctamente"));

            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error al modificar", e);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al modificar",
                                e.getMessage()));
            }
        }
    }

    @Override
    public void selectionHandler(SelectEvent<ProductoTipoProducto> r) {
        if (r != null && r.getObject() != null) {
            this.registro = r.getObject();
            this.estado = ESTADO_CRUD.MODIFICAR;

            // ✅ Sincronizar los auxiliares para que los combos muestren el valor actual
            if (registro.getIdProducto() != null) {
                this.idProductoSeleccionado = registro.getIdProducto();
            }

            if (registro.getIdTipoProducto() != null) {
                this.idTipoProductoSeleccionado = registro.getIdTipoProducto().getId();
            }
        }
    }

    @Override
    public void btnNuevoHandler(ActionEvent actionEvent) {
        super.btnNuevoHandler(actionEvent);
        // Limpiar selectores
        this.idProductoSeleccionado = null;
        this.idTipoProductoSeleccionado = null;
    }

    // ✅ Getters y Setters (ACTUALIZADOS)

    public Producto getIdProductoSeleccionado() {
        return idProductoSeleccionado;
    }

    public void setIdProductoSeleccionado(Producto idProductoSeleccionado) {
        this.idProductoSeleccionado = idProductoSeleccionado;
    }

    public Long getIdTipoProductoSeleccionado() {
        return idTipoProductoSeleccionado;
    }

    public void setIdTipoProductoSeleccionado(Long idTipoProductoSeleccionado) {
        this.idTipoProductoSeleccionado = idTipoProductoSeleccionado;
    }

    public List<Producto> getListaProductos() {
        return listaProductos;
    }

    public void setListaProductos(List<Producto> listaProductos) {
        this.listaProductos = listaProductos;
    }

    public List<TipoProducto> getListaTipoProductos() {
        return listaTipoProductos;
    }

    public void setListaTipoProductos(List<TipoProducto> listaTipoProductos) {
        this.listaTipoProductos = listaTipoProductos;
    }

    public String getNombreBean() {
        return nombreBean;
    }
}