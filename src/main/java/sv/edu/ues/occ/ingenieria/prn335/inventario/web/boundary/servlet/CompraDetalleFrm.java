package sv.edu.ues.occ.ingenieria.prn335.inventario.web.boundary.servlet;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.CompraDetalleDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.CompraDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.ProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.InventarioDefaultDataAccess;

import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.CompraDetalle;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.Compra;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.Producto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named("compraDetalleFrm")
@ViewScoped
public class CompraDetalleFrm extends DefaultFrm<CompraDetalle> implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(CompraDetalleFrm.class.getName());

    @Inject
    private CompraDetalleDAO compraDetalleDAO;

    @Inject
    private CompraDAO compraDao;

    @Inject
    private ProductoDAO productoDao;

    private List<Compra> comprasDisponibles;
    private List<Producto> productosDisponibles;
    private final List<String> estadosDisponibles = List.of("RECIBIDO", "PENDIENTE", "DEVUELTO");

    private boolean modoLista;  // Modo lista
    private boolean modoDetalle;  // Modo detalle

    // ---------------------- Inicialización ----------------------

    @PostConstruct
    @Override
    public void inicializar() {
        super.inicializar();
        inicializarListas();
        this.nombreBean = "Gestión de Detalle de Compra";
        this.modoLista = true;  // Modo inicial: lista
        this.modoDetalle = false;  // Modo inicial: no detalle
        LOGGER.log(Level.INFO, "CompraDetalleFrm inicializado correctamente");
    }

    @Override
    public void inicializarListas() {
        try {
            comprasDisponibles = compraDao != null ? compraDao.findAll() : new ArrayList<>();
            productosDisponibles = productoDao != null ? productoDao.findAll() : new ArrayList<>();
            LOGGER.log(Level.INFO, "Inicializadas {0} compras y {1} productos",
                    new Object[]{comprasDisponibles.size(), productosDisponibles.size()});
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al inicializar listas", e);
            comprasDisponibles = new ArrayList<>();
            productosDisponibles = new ArrayList<>();
        }
    }

    @Override
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    @Override
    protected InventarioDefaultDataAccess<CompraDetalle> getDao() {
        return compraDetalleDAO;
    }

    @Override
    protected CompraDetalle nuevoRegistro() {
        CompraDetalle cd = new CompraDetalle();
        cd.setId(UUID.randomUUID());
        cd.setCantidad(BigDecimal.ZERO);
        cd.setPrecio(BigDecimal.ZERO);
        cd.setEstado("PENDIENTE");
        cd.setIdCompra(new Compra());  // Asegúrate de que el ID de compra es Long
        cd.setIdProducto(new Producto());
        return cd;
    }

    @Override
    protected CompraDetalle buscarRegistroPorId(Object id) {
        if (id instanceof UUID) {
            return compraDetalleDAO.findById((UUID) id);
        }
        return null;
    }

    @Override
    protected String getIdAsText(CompraDetalle r) {
        return (r != null && r.getId() != null) ? r.getId().toString() : null;
    }

    @Override
    protected CompraDetalle getIdByText(String id) {
        try {
            return compraDetalleDAO.findById(UUID.fromString(id));
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected boolean esNombreVacio(CompraDetalle registro) {
        boolean error = false;

        if (registro.getIdCompra() == null || registro.getIdCompra().getId() == null) {
            mensaje("Debe seleccionar una Compra");
            error = true;
        }

        if (registro.getIdProducto() == null || registro.getIdProducto().getId() == null) {
            mensaje("Debe seleccionar un Producto");
            error = true;
        }

        if (registro.getCantidad() == null || registro.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
            mensaje("La cantidad debe ser mayor a cero");
            error = true;
        }

        if (registro.getPrecio() == null || registro.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
            mensaje("Debe ingresar un precio válido");
            error = true;
        }

        if (registro.getEstado() == null || registro.getEstado().isBlank()) {
            mensaje("Debe seleccionar un estado");
            error = true;
        }

        return error;
    }

    private void mensaje(String m) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", m));
    }

    // ---------------------- Guardar / Modificar ----------------------

    private Compra obtenerCompraCompleta(Long id) {
        return compraDao.findById(id);
    }

    private Producto obtenerProductoCompleto(UUID id) {
        return productoDao.findById(id);
    }

    @Override
    public void btnGuardarHandler(jakarta.faces.event.ActionEvent actionEvent) {
        if (registro == null || esNombreVacio(registro)) return;

        Compra compra = obtenerCompraCompleta(registro.getIdCompra().getId());
        Producto producto = obtenerProductoCompleto(registro.getIdProducto().getId());

        registro.setIdCompra(compra);
        registro.setIdProducto(producto);

        getDao().crear(registro);

        mensaje("Guardado correctamente");
        registro = null;
        estado = ESTADO_CRUD.NADA;
        inicializarRegistros();
    }

    @Override
    public void btnModificarHandler(jakarta.faces.event.ActionEvent actionEvent) {
        if (registro == null || esNombreVacio(registro)) return;

        Compra compra = obtenerCompraCompleta(registro.getIdCompra().getId());
        Producto producto = obtenerProductoCompleto(registro.getIdProducto().getId());

        registro.setIdCompra(compra);
        registro.setIdProducto(producto);

        getDao().modificar(registro);

        mensaje("Modificado correctamente");
        registro = null;
        estado = ESTADO_CRUD.NADA;
        inicializarRegistros();
    }

    // ---------------------- Getters ----------------------

    public List<Compra> getComprasDisponibles() {
        if (comprasDisponibles == null) inicializarListas();
        return comprasDisponibles;
    }

    public List<Producto> getProductosDisponibles() {
        if (productosDisponibles == null) inicializarListas();
        return productosDisponibles;
    }

    public List<String> getEstadosDisponibles() {
        return estadosDisponibles;
    }

    public boolean isModoLista() {
        return modoLista;
    }

    public void setModoLista(boolean modoLista) {
        this.modoLista = modoLista;
    }

    public boolean isModoDetalle() {
        return modoDetalle;
    }

    public void setModoDetalle(boolean modoDetalle) {
        this.modoDetalle = modoDetalle;
    }
}
