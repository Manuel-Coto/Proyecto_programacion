package sv.edu.ues.occ.ingenieria.prn335.inventario.web.boundary.servlet;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.primefaces.event.SelectEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.*;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.*;

@Named("kardexFrm")
@ViewScoped
public class KardexFrm extends DefaultFrm<Kardex> implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(KardexFrm.class.getName());

    @Inject
    private KardexDAO kardexDAO;

    @Inject
    private ProductoDAO productoDAO;

    @Inject
    private AlmacenDAO almacenDAO;

    @Inject
    private CompraDetalleDAO compraDetalleDAO;

    @Inject
    private VentaDetalleDAO ventaDetalleDAO;

    // Listas para los selectOneMenu
    private List<Producto> productosDisponibles;
    private List<Almacen> almacenesDisponibles;
    private List<CompraDetalle> comprasDetalleDisponibles;
    private List<VentaDetalle> ventasDetalleDisponibles;

    // IDs seleccionados en los selectOneMenu
    private UUID productoSeleccionadoId;
    private Integer almacenSeleccionadoId;
    private UUID compraDetalleSeleccionadaId;
    private UUID ventaDetalleSeleccionadaId;

    private final List<String> tiposMovimiento = List.of(
            "ENTRADA_COMPRA",
            "SALIDA_VENTA",
            "AJUSTE_ENTRADA",
            "AJUSTE_SALIDA",
            "TRANSFERENCIA_ENTRADA",
            "TRANSFERENCIA_SALIDA",
            "DEVOLUCION_COMPRA",
            "DEVOLUCION_VENTA"
    );

    // ===== INICIALIZACIÓN =====

    @PostConstruct
    @Override
    public void inicializar() {
        super.inicializar();
        this.nombreBean = "Gestión de Kardex";
        cargarDatosFiltros();
        LOGGER.log(Level.INFO, "KardexFrm inicializado correctamente");
    }

    private void cargarDatosFiltros() {
        try {
            this.productosDisponibles = productoDAO != null ? productoDAO.findAll() : new ArrayList<>();
            this.almacenesDisponibles = almacenDAO != null ? almacenDAO.findAll() : new ArrayList<>();
            this.comprasDetalleDisponibles = compraDetalleDAO != null ? compraDetalleDAO.findAll() : new ArrayList<>();
            this.ventasDetalleDisponibles = ventaDetalleDAO != null ? ventaDetalleDAO.findAll() : new ArrayList<>();

            LOGGER.log(Level.INFO, "Cargados {0} productos, {1} almacenes",
                    new Object[]{productosDisponibles.size(), almacenesDisponibles.size()});
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al cargar datos de filtros", e);
            this.productosDisponibles = new ArrayList<>();
            this.almacenesDisponibles = new ArrayList<>();
            this.comprasDetalleDisponibles = new ArrayList<>();
            this.ventasDetalleDisponibles = new ArrayList<>();
        }
    }

    // ===== MÉTODOS ABSTRACTOS DE DefaultFrm =====

    @Override
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    @Override
    protected InventarioDefaultDataAccess<Kardex> getDao() {
        return kardexDAO;
    }

    @Override
    protected Kardex nuevoRegistro() {
        Kardex kardex = new Kardex();
        kardex.setId(UUID.randomUUID());
        kardex.setFecha(OffsetDateTime.now());
        kardex.setCantidad(BigDecimal.ZERO);
        kardex.setPrecio(BigDecimal.ZERO);
        kardex.setCantidadActual(BigDecimal.ZERO);
        kardex.setPrecioActual(BigDecimal.ZERO);

        // Inicializar entidades FK para evitar NullPointer en XHTML
        kardex.setIdProducto(new Producto());
        kardex.setIdAlmacen(new Almacen());

        // Limpiar IDs seleccionados
        this.productoSeleccionadoId = null;
        this.almacenSeleccionadoId = null;
        this.compraDetalleSeleccionadaId = null;
        this.ventaDetalleSeleccionadaId = null;

        LOGGER.log(Level.INFO, "Nuevo registro de kardex creado con ID: {0}", kardex.getId());
        return kardex;
    }

    @Override
    protected Kardex buscarRegistroPorId(Object id) {
        if (id instanceof UUID) {
            return kardexDAO.findById((UUID) id);
        }
        return null;
    }

    @Override
    protected String getIdAsText(Kardex r) {
        return r != null && r.getId() != null ? r.getId().toString() : null;
    }

    @Override
    protected Kardex getIdByText(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        try {
            UUID uuid = UUID.fromString(id);
            return kardexDAO.findById(uuid);
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "ID inválido: {0}", id);
            return null;
        }
    }

    @Override
    protected boolean esNombreVacio(Kardex registro) {
        boolean fallo = false;

        // 1. Validar Producto
        if (registro.getIdProducto() == null || registro.getIdProducto().getId() == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "Debe seleccionar un Producto."));
            fallo = true;
        }

        // 2. Validar Almacén
        if (registro.getIdAlmacen() == null || registro.getIdAlmacen().getId() == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "Debe seleccionar un Almacén."));
            fallo = true;
        }

        // 3. Validar Tipo de Movimiento
        if (registro.getTipoMovimiento() == null || registro.getTipoMovimiento().trim().isEmpty()) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "Debe seleccionar un tipo de movimiento."));
            fallo = true;
        }

        // 4. Validar Cantidad
        if (registro.getCantidad() == null || registro.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "La cantidad debe ser mayor a cero."));
            fallo = true;
        }

        // 5. Validar Precio
        if (registro.getPrecio() == null || registro.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "El precio debe ser mayor o igual a cero."));
            fallo = true;
        }

        // 6. Validar Fecha
        if (registro.getFecha() == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "La fecha es obligatoria."));
            fallo = true;
        }

        return fallo;
    }

    // ===== MANEJADORES DE EVENTOS DEL XHTML =====

    /**
     * Manejador cuando se selecciona un producto en el selectOneMenu
     */
    public void onProductoChange() {
        if (productoSeleccionadoId != null) {
            Producto producto = obtenerProductoCompleto(productoSeleccionadoId);
            if (registro != null && producto != null) {
                registro.setIdProducto(producto);
                LOGGER.log(Level.INFO, "Producto sincronizado: {0}", producto.getId());
            }
        } else {
            if (registro != null) {
                registro.setIdProducto(new Producto());
            }
        }
    }

    /**
     * Manejador cuando se selecciona un almacén en el selectOneMenu
     */
    public void onAlmacenChange() {
        if (almacenSeleccionadoId != null) {
            Almacen almacen = obtenerAlmacenCompleto(almacenSeleccionadoId);
            if (registro != null && almacen != null) {
                registro.setIdAlmacen(almacen);
                LOGGER.log(Level.INFO, "Almacén sincronizado: {0}", almacen.getId());
            }
        } else {
            if (registro != null) {
                registro.setIdAlmacen(new Almacen());
            }
        }
    }

    /**
     * Manejador cuando se selecciona un detalle de compra en el selectOneMenu
     */
    public void onCompraChange() {
        if (compraDetalleSeleccionadaId != null) {
            CompraDetalle detalle = obtenerCompraDetalleCompleto(compraDetalleSeleccionadaId);
            if (registro != null) {
                registro.setIdCompraDetalle(detalle);
                LOGGER.log(Level.INFO, "CompraDetalle sincronizado: {0}", compraDetalleSeleccionadaId);
            }
        } else {
            if (registro != null) {
                registro.setIdCompraDetalle(null);
            }
        }
    }

    /**
     * Manejador cuando se selecciona un detalle de venta en el selectOneMenu
     */
    public void onVentaChange() {
        if (ventaDetalleSeleccionadaId != null) {
            VentaDetalle detalle = obtenerVentaDetalleCompleto(ventaDetalleSeleccionadaId);
            if (registro != null) {
                registro.setIdVentaDetalle(detalle);
                LOGGER.log(Level.INFO, "VentaDetalle sincronizado: {0}", ventaDetalleSeleccionadaId);
            }
        } else {
            if (registro != null) {
                registro.setIdVentaDetalle(null);
            }
        }
    }

    /**
     * Manejador cuando se selecciona una fila en la tabla
     */
    public void selectionHandler(SelectEvent<Kardex> event) {
        this.registro = event.getObject();

        // Sincronizar IDs con los selectOneMenu
        if (this.registro != null) {
            if (this.registro.getIdProducto() != null && this.registro.getIdProducto().getId() != null) {
                this.productoSeleccionadoId = this.registro.getIdProducto().getId();
                this.registro.setIdProducto(obtenerProductoCompleto(this.productoSeleccionadoId));
            }

            if (this.registro.getIdAlmacen() != null && this.registro.getIdAlmacen().getId() != null) {
                this.almacenSeleccionadoId = this.registro.getIdAlmacen().getId();
                this.registro.setIdAlmacen(obtenerAlmacenCompleto(this.almacenSeleccionadoId));
            }

            if (this.registro.getIdCompraDetalle() != null && this.registro.getIdCompraDetalle().getId() != null) {
                this.compraDetalleSeleccionadaId = this.registro.getIdCompraDetalle().getId();
            }

            if (this.registro.getIdVentaDetalle() != null && this.registro.getIdVentaDetalle().getId() != null) {
                this.ventaDetalleSeleccionadaId = this.registro.getIdVentaDetalle().getId();
            }
        }

        this.estado = ESTADO_CRUD.MODIFICAR;
        LOGGER.log(Level.INFO, "Kardex seleccionado: {0}", this.registro.getId());
    }

    // ===== MÉTODOS AUXILIARES =====

    private Producto obtenerProductoCompleto(UUID id) {
        if (id == null) return null;
        try {
            Producto producto = productoDAO.findById(id);
            LOGGER.log(Level.INFO, "Producto cargado: {0}", id);
            return producto;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener Producto con ID: " + id, e);
            return null;
        }
    }

    private Almacen obtenerAlmacenCompleto(Integer id) {
        if (id == null) return null;
        try {
            Almacen almacen = almacenDAO.findById(id);
            LOGGER.log(Level.INFO, "Almacén cargado: {0}", id);
            return almacen;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener Almacén con ID: " + id, e);
            return null;
        }
    }

    private CompraDetalle obtenerCompraDetalleCompleto(UUID id) {
        if (id == null) return null;
        try {
            CompraDetalle detalle = compraDetalleDAO.findById(id);
            LOGGER.log(Level.INFO, "CompraDetalle cargado: {0}", id);
            return detalle;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener CompraDetalle con ID: " + id, e);
            return null;
        }
    }

    private VentaDetalle obtenerVentaDetalleCompleto(UUID id) {
        if (id == null) return null;
        try {
            VentaDetalle detalle = ventaDetalleDAO.findById(id);
            LOGGER.log(Level.INFO, "VentaDetalle cargado: {0}", id);
            return detalle;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener VentaDetalle con ID: " + id, e);
            return null;
        }
    }

    /**
     * Calcula los valores actuales basándose en el último movimiento
     */
    private void calcularValoresActuales(Kardex kardex) {
        try {
            if (kardex.getIdProducto() == null || kardex.getIdAlmacen() == null) {
                LOGGER.log(Level.WARNING, "No se pueden calcular valores: Producto o Almacén nulos");
                return;
            }

            Kardex ultimoMovimiento = kardexDAO.findUltimoMovimiento(
                    kardex.getIdProducto().getId(),
                    kardex.getIdAlmacen().getId()
            );

            BigDecimal cantidadAnterior = ultimoMovimiento != null
                    ? ultimoMovimiento.getCantidadActual()
                    : BigDecimal.ZERO;

            BigDecimal precioAnterior = ultimoMovimiento != null
                    ? ultimoMovimiento.getPrecioActual()
                    : BigDecimal.ZERO;

            String tipoMov = kardex.getTipoMovimiento();
            BigDecimal cantidad = kardex.getCantidad();
            BigDecimal precio = kardex.getPrecio();

            if (tipoMov.startsWith("ENTRADA") || tipoMov.equals("DEVOLUCION_VENTA")) {
                kardex.setCantidadActual(cantidadAnterior.add(cantidad));
            } else if (tipoMov.startsWith("SALIDA") || tipoMov.equals("DEVOLUCION_COMPRA")) {
                kardex.setCantidadActual(cantidadAnterior.subtract(cantidad));
            } else {
                kardex.setCantidadActual(cantidadAnterior);
            }

            // Calcular precio promedio
            if (kardex.getCantidadActual().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal totalCosto = precioAnterior.multiply(cantidadAnterior)
                        .add(precio.multiply(cantidad));
                kardex.setPrecioActual(totalCosto.divide(kardex.getCantidadActual(), 2, RoundingMode.HALF_UP));
            } else {
                kardex.setPrecioActual(BigDecimal.ZERO);
            }

            LOGGER.log(Level.INFO, "Valores actuales calculados: Cantidad={0}, Precio={1}",
                    new Object[]{kardex.getCantidadActual(), kardex.getPrecioActual()});

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al calcular valores actuales", e);
        }
    }

    // ===== GETTERS Y SETTERS =====

    public List<Producto> getProductosDisponibles() {
        if (productosDisponibles == null) {
            productosDisponibles = new ArrayList<>();
        }
        return productosDisponibles;
    }

    public List<Almacen> getAlmacenesDisponibles() {
        if (almacenesDisponibles == null) {
            almacenesDisponibles = new ArrayList<>();
        }
        return almacenesDisponibles;
    }

    public List<CompraDetalle> getComprasDetalleDisponibles() {
        if (comprasDetalleDisponibles == null) {
            comprasDetalleDisponibles = new ArrayList<>();
        }
        return comprasDetalleDisponibles;
    }

    public List<VentaDetalle> getVentasDetalleDisponibles() {
        if (ventasDetalleDisponibles == null) {
            ventasDetalleDisponibles = new ArrayList<>();
        }
        return ventasDetalleDisponibles;
    }

    public List<String> getTiposMovimiento() {
        return tiposMovimiento;
    }

    public UUID getProductoSeleccionadoId() {
        return productoSeleccionadoId;
    }

    public void setProductoSeleccionadoId(UUID productoSeleccionadoId) {
        this.productoSeleccionadoId = productoSeleccionadoId;
    }

    public Integer getAlmacenSeleccionadoId() {
        return almacenSeleccionadoId;
    }

    public void setAlmacenSeleccionadoId(Integer almacenSeleccionadoId) {
        this.almacenSeleccionadoId = almacenSeleccionadoId;
    }

    public UUID getCompraDetalleSeleccionadaId() {
        return compraDetalleSeleccionadaId;
    }

    public void setCompraDetalleSeleccionadaId(UUID compraDetalleSeleccionadaId) {
        this.compraDetalleSeleccionadaId = compraDetalleSeleccionadaId;
    }

    public UUID getVentaDetalleSeleccionadaId() {
        return ventaDetalleSeleccionadaId;
    }

    public void setVentaDetalleSeleccionadaId(UUID ventaDetalleSeleccionadaId) {
        this.ventaDetalleSeleccionadaId = ventaDetalleSeleccionadaId;
    }
}
