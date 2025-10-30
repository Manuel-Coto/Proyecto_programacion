package sv.edu.ues.occ.ingenieria.prn335.inventario.web.boundary.servlet;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.VentaDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.Venta;

import java.io.Serializable;
import java.util.List;

@Named("ventaFrm")
@ViewScoped
public class VentaFrm implements Serializable {

    @Inject
    private VentaDAO ventaDAO; // Inyecta el DAO de Venta

    private Venta venta; // Objeto para almacenar la venta actual
    private List<Venta> ventas; // Lista para almacenar todas las ventas

    // Para manejar la selección de ventas (por ejemplo, para editar o ver detalles)
    private Venta selectedVenta;

    // Método de inicialización
    @PostConstruct
    public void init() {
        ventas = ventaDAO.findAll(); // Cargar todas las ventas al iniciar el formulario
    }

    // Método para crear una nueva venta
    public void crear() {
        try {
            ventaDAO.crear(venta); // Crear la venta usando el DAO
            ventas.add(venta); // Añadir la venta a la lista
            venta = new Venta(); // Limpiar el objeto para una nueva venta
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Venta creada con éxito", ""));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al crear la venta", ""));
        }
    }

    // Método para modificar una venta
    public void modificar() {
        try {
            ventaDAO.modificar(selectedVenta); // Modificar la venta seleccionada usando el DAO
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Venta modificada con éxito", ""));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al modificar la venta", ""));
        }
    }

    // Método para eliminar una venta
    public void eliminar() {
        try {
            ventaDAO.eliminar(selectedVenta); // Eliminar la venta seleccionada usando el DAO
            ventas.remove(selectedVenta); // Eliminar de la lista
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Venta eliminada con éxito", ""));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al eliminar la venta", ""));
        }
    }

    // Getters y Setters
    public Venta getVenta() {
        return venta;
    }

    public void setVenta(Venta venta) {
        this.venta = venta;
    }

    public List<Venta> getVentas() {
        return ventas;
    }

    public void setVentas(List<Venta> ventas) {
        this.ventas = ventas;
    }

    public Venta getSelectedVenta() {
        return selectedVenta;
    }

    public void setSelectedVenta(Venta selectedVenta) {
        this.selectedVenta = selectedVenta;
    }
}
