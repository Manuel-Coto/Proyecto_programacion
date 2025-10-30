package sv.edu.ues.occ.ingenieria.prn335.inventario.web.boundary.servlet;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.VentaDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.VentaDetalleDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.ProductoDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.ClienteDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.Venta;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.VentaDetalle;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.Producto;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.Cliente;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Named("ventaFrm")
@ViewScoped
public class VentaFrm extends DefaultFrm<Venta> implements Serializable {

    @Inject
    private VentaDAO ventaDAO;

    @Inject
    private VentaDetalleDAO ventaDetalleDAO;

    @Inject
    private ProductoDAO productoDAO;

    @Inject
    private ClienteDAO clienteDAO;

    private List<Producto> productos; // Lista de productos disponibles
    private List<VentaDetalle> ventaDetalles; // Detalles de la venta
    private VentaDetalle selectedDetalle; // Detalle de venta seleccionado

    @PostConstruct
    public void init() {
        super.inicializar();
        productos = productoDAO.findAll();
        ventaDetalles = new ArrayList<>();
    }

    @Override
    protected Venta nuevoRegistro() {
        Venta nuevaVenta = new Venta();
        nuevaVenta.setId(UUID.randomUUID());
        nuevaVenta.setFecha(OffsetDateTime.now());
        nuevaVenta.setEstado("PENDIENTE");
        nuevaVenta.setObservaciones("");
        return nuevaVenta;
    }

    @Override
    protected Venta buscarRegistroPorId(Object id) {
        return ventaDAO.find((UUID) id);  // Usa el método find de VentaDAO
    }

    @Override
    protected String getIdAsText(Venta venta) {
        return venta.getId().toString();
    }

    @Override
    protected Venta getIdByText(String id) {
        return ventaDAO.find(UUID.fromString(id));  // Usa el método find de VentaDAO
    }

    // Agregar un detalle a la venta
    public void agregarDetalle() {
        if (selectedDetalle != null) {
            ventaDetalles.add(selectedDetalle);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Detalle agregado", ""));
            selectedDetalle = null; // Limpiar selección
        }
    }

    // Eliminar un detalle de la venta
    public void eliminarDetalle(VentaDetalle detalle) {
        ventaDetalles.remove(detalle);
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Detalle eliminado", ""));
    }

    // Guardar la venta con sus detalles
    public void guardarVenta() {
        if (this.registro != null) {
            try {
                // Guardar la venta
                ventaDAO.crear(this.registro);

                // Guardar los detalles de la venta
                for (VentaDetalle detalle : ventaDetalles) {
                    detalle.setIdVenta((Venta) this.registro); // Asociar el detalle con la venta
                    ventaDetalleDAO.crear(detalle);
                }

                this.registro = null;
                this.estado = ESTADO_CRUD.NADA;
                this.ventaDetalles = new ArrayList<>();
                inicializarRegistros(); // Recargar la tabla de ventas
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Venta y detalles guardados correctamente", ""));
            } catch (Exception e) {
                e.printStackTrace();
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al guardar la venta", e.getMessage()));
            }
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "No hay registro para guardar"));
        }
    }

    // Getter y Setter de propiedades
    public List<Producto> getProductos() {
        return productos;
    }

    public void setProductos(List<Producto> productos) {
        this.productos = productos;
    }

    public List<VentaDetalle> getVentaDetalles() {
        return ventaDetalles;
    }

    public void setVentaDetalles(List<VentaDetalle> ventaDetalles) {
        this.ventaDetalles = ventaDetalles;
    }

    public VentaDetalle getSelectedDetalle() {
        return selectedDetalle;
    }

    public void setSelectedDetalle(VentaDetalle selectedDetalle) {
        this.selectedDetalle = selectedDetalle;
    }

    @Override
    public InventarioDefaultDataAccess<Venta> getDao() {
        return ventaDAO;
    }

    @Override
    protected void btnCancelarHandler(ActionEvent actionEvent) {
        super.btnCancelarHandler(actionEvent);
        ventaDetalles.clear(); // Limpiar los detalles si se cancela la venta
    }
}
