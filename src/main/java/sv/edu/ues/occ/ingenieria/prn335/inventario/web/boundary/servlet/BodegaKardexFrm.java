package sv.edu.ues.occ.ingenieria.prn335.inventario.web.boundary.servlet;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.event.ActionEvent;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.AlmacenDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.KardexDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.VentaDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.Almacen;
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
    private transient VentaDAO ventaDAO;

    private List<Venta> ventasFinalizadas = new ArrayList<>();
    private String nombreBean = "Monitor de Ventas Finalizadas";

    @PostConstruct
    public void init() {
        LOGGER.log(Level.INFO, "Inicializando BodegaKardexFrm...");
        cargarVentasFinalizadas();
        cargarAlmacenes();
    }

    public synchronized void cargarVentasFinalizadas() {
        try {
            // Limpiar la lista completamente para forzar recarga
            this.ventasFinalizadas.clear();

            List<Venta> todasLasVentas = ventaDAO.findAll();

            if (todasLasVentas != null) {
                for (Venta venta : todasLasVentas) {
                    if (venta.getEstado() != null &&
                            venta.getEstado().equalsIgnoreCase("FINALIZADA")) {
                        this.ventasFinalizadas.add(venta);
                    }
                }
            }

            LOGGER.log(Level.INFO, "Ventas finalizadas recargadas: {0}", this.ventasFinalizadas.size());

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "✗ Error al cargar ventas finalizadas", e);
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

    @Inject
    private transient KardexDAO kardexDAO;

    @Inject
    private transient AlmacenDAO almacenDAO;

    private List<Almacen> listaAlmacenes = new ArrayList<>();
    private Integer idAlmacenSeleccionado;

    private void cargarAlmacenes() {
        try {
            this.listaAlmacenes = almacenDAO.findAll();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error cargando almacenes", e);
            this.listaAlmacenes = new ArrayList<>();
        }
    }

    public void registrarVentaEnKardex(Venta venta) {
        if (idAlmacenSeleccionado == null) {
            LOGGER.log(Level.WARNING, "Almacén no seleccionado");
            return;
        }

        try {
            kardexDAO.sincronizarDesdeVenta(venta, idAlmacenSeleccionado);
            LOGGER.log(Level.INFO, "Venta registrada en Kardex");


            cargarVentasFinalizadas(); // Opcional: refrescar la tabla local
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error registrando venta en Kardex", e);
        }
    }


    public List<Almacen> getListaAlmacenes() {
        return listaAlmacenes != null ? listaAlmacenes : new ArrayList<>();
    }

    public Integer getIdAlmacenSeleccionado() {
        return idAlmacenSeleccionado;
    }

    public void setIdAlmacenSeleccionado(Integer idAlmacenSeleccionado) {
        this.idAlmacenSeleccionado = idAlmacenSeleccionado;
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
