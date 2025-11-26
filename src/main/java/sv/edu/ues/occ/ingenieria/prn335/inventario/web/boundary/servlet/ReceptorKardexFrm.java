package sv.edu.ues.occ.ingenieria.prn335.inventario.web.boundary.servlet;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.event.ActionEvent;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.CompraDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.Compra;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named("receptorKardexFrm")
@SessionScoped
public class ReceptorKardexFrm implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ReceptorKardexFrm.class.getName());

    @Inject
    private CompraDAO compraDAO;

    private List<Compra> comprasPagadas = new ArrayList<>();
    private String nombreBean = "Monitor de Compras Pagadas";

    @PostConstruct
    public void init() {
        LOGGER.log(Level.INFO, "Inicializando ReceptorKardexFrm...");
        cargarComprasPagadas();
    }

    public synchronized void cargarComprasPagadas() {
        try {
            List<Compra> todasLasCompras = compraDAO.findAll();
            List<Compra> nuevaLista = new ArrayList<>();

            if (todasLasCompras != null) {
                for (Compra compra : todasLasCompras) {
                    if (compra.getEstado() != null &&
                            compra.getEstado().equalsIgnoreCase(EstadoCompra.PAGADA.name())) {
                        nuevaLista.add(compra);
                    }
                }
            }

            this.comprasPagadas = nuevaLista;
            LOGGER.log(Level.INFO, "Compras pagadas cargadas: {0}", this.comprasPagadas.size());

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "✗ Error al cargar compras pagadas", e);
            this.comprasPagadas = new ArrayList<>();
        }
    }

    // Método sin parámetros (para <p:poll>)
    public void actualizarTabla() {
        LOGGER.log(Level.INFO, "actualizarTabla() EJECUTADO (sin ActionEvent)");
        cargarComprasPagadas();
        LOGGER.log(Level.INFO, "Tabla actualizada con {0} compras pagadas", this.comprasPagadas.size());
    }

    // Método con ActionEvent (para <p:remoteCommand>)
    public void actualizarTabla(ActionEvent actionEvent) {
        LOGGER.log(Level.INFO, "actualizarTabla() EJECUTADO (con ActionEvent)");
        cargarComprasPagadas();
        LOGGER.log(Level.INFO, "Tabla actualizada con {0} compras pagadas", this.comprasPagadas.size());
    }

    public List<Compra> getComprasPagadas() {
        return comprasPagadas;
    }

    public void setComprasPagadas(List<Compra> comprasPagadas) {
        this.comprasPagadas = comprasPagadas;
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
