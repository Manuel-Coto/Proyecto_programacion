package sv.edu.ues.occ.ingenieria.prn335.inventario.web.boundary.servlet;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.event.ActionEvent;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.AlmacenDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.CompraDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.KardexDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.Almacen;
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
    private transient CompraDAO compraDAO;

    private List<Compra> comprasPagadas = new ArrayList<>();
    private String nombreBean = "Monitor de Compras Pagadas";

    @PostConstruct
    public void init() {
        LOGGER.log(Level.INFO, "Inicializando ReceptorKardexFrm...");
        cargarComprasPagadas();
        cargarAlmacenes();
    }

    public synchronized void cargarComprasPagadas() {
        try {
            // Limpiar la lista completamente para forzar recarga
            this.comprasPagadas.clear();

            List<Compra> todasLasCompras = compraDAO.findAll();

            if (todasLasCompras != null) {
                for (Compra compra : todasLasCompras) {
                    if (compra.getEstado() != null &&
                            compra.getEstado().equalsIgnoreCase(EstadoCompra.PAGADA.name())) {
                        this.comprasPagadas.add(compra);
                    }
                }
            }

            LOGGER.log(Level.INFO, "Compras pagadas recargas: {0}", this.comprasPagadas.size());

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "✗ Error al cargar compras pagadas", e);
            this.comprasPagadas = new ArrayList<>();
        }
    }

    public void actualizarTabla() {
        LOGGER.log(Level.INFO, "Actualizando tabla desde WebSocket");
        cargarComprasPagadas();
    }

    public void actualizarTabla(ActionEvent actionEvent) {
        LOGGER.log(Level.INFO, "Actualizando tabla desde remoteCommand");
        cargarComprasPagadas();
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


    public void registrarCompraEnKardex(Compra compra) {
        if (idAlmacenSeleccionado == null) {
            LOGGER.log(Level.WARNING, "Almacén no seleccionado");
            return;
        }

        try {
            kardexDAO.sincronizarDesdeCompra(compra, idAlmacenSeleccionado);
            LOGGER.log(Level.INFO, "Compra registrada en Kardex");


            cargarComprasPagadas(); // Opcional: refrescar la tabla local
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error registrando compra en Kardex", e);
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
