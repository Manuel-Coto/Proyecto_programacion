package sv.edu.ues.occ.ingenieria.prn335.inventario.web.boundary.servlet;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.event.ActionEvent;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.VentaDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.Venta;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named("bodegaKardexFrm")
@SessionScoped
public class BodegaKardexFrm implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(BodegaKardexFrm.class.getName());

    @Inject
    private VentaDAO ventaDAO;

    private List<Venta> ventasFinalizadas = new ArrayList<>();
    private String nombreBean = "Monitor de Ventas Finalizadas";

    @PostConstruct
    public void init() {
        LOGGER.log(Level.INFO, "Inicializando BodegaKardexFrm...");
        cargarVentasFinalizadas();
    }

    public synchronized void cargarVentasFinalizadas() {
        try {
            List<Venta> todas = ventaDAO.findAll();
            List<Venta> filtradas = new ArrayList<>();
            if (todas != null) {
                for (Venta v : todas) {
                    if (v.getEstado() != null && v.getEstado().equalsIgnoreCase("FINALIZADA")) {
                        filtradas.add(v);
                    }
                }
            }
            this.ventasFinalizadas = filtradas;
            LOGGER.log(Level.INFO, "Ventas finalizadas cargadas: {0}", this.ventasFinalizadas.size());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al cargar ventas finalizadas", e);
            this.ventasFinalizadas = new ArrayList<>();
        }
    }

    // Método sin parámetros (para p:poll y llamadas que esperan no-arg)
    public void actualizarTabla() {
        LOGGER.log(Level.INFO, "actualizarTabla() EJECUTADO (sin ActionEvent)");
        cargarVentasFinalizadas();
        LOGGER.log(Level.INFO, "Tabla actualizada con {0} ventas finalizadas", this.ventasFinalizadas.size());
    }

    // Método con ActionEvent (para p:remoteCommand que puede invocar actionListener)
    public void actualizarTabla(ActionEvent actionEvent) {
        LOGGER.log(Level.INFO, "actualizarTabla() EJECUTADO (con ActionEvent)");
        cargarVentasFinalizadas();
        LOGGER.log(Level.INFO, "Tabla actualizada con {0} ventas finalizadas", this.ventasFinalizadas.size());
    }

    public List<Venta> getVentasFinalizadas() {
        return ventasFinalizadas;
    }

    public void setVentasFinalizadas(List<Venta> ventasFinalizadas) {
        this.ventasFinalizadas = ventasFinalizadas;
    }

    public String getNombreBean() {
        return nombreBean;
    }

    public void setNombreBean(String nombreBean) {
        this.nombreBean = nombreBean;
    }

    public Object getNumeroRandom() {
        return Math.random();
    }
}
