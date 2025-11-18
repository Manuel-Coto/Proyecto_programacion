package sv.edu.ues.occ.ingenieria.prn335.inventario.web.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.VentaDetalle;
import java.util.List;
import java.util.UUID;


@Stateless
public class VentaDetalleDAO extends InventarioDefaultDataAccess<VentaDetalle> {

    @PersistenceContext(unitName = "inventarioPU")
    private EntityManager em;

    // Constructor que pasa la clase VentaDetalle a la clase padre
    public VentaDetalleDAO() {
        super(VentaDetalle.class); // Llama al constructor de InventarioDefaultDataAccess con la clase VentaDetalle
    }

    // Implementación de getEntityManager() que devuelve el EntityManager inyectado
    @Override
    public EntityManager getEntityManager() {
        return em; // Devuelve el EntityManager inyectado por CDI
    }

    // Puedes añadir métodos personalizados específicos de VentaDetalle aquí si es necesario

    // Ejemplo de método personalizado: obtener detalles de ventas por estado
    public List<VentaDetalle> findByEstado(String estado) {
        try {
            return em.createQuery("SELECT v FROM VentaDetalle v WHERE v.estado = :estado", VentaDetalle.class)
                    .setParameter("estado", estado)
                    .getResultList();
        } catch (Exception ex) {
            throw new IllegalStateException("Error al obtener los registros por estado", ex);
        }
    }

    // Ejemplo de método personalizado: obtener detalles de venta por ID de venta
    public List<VentaDetalle> findByVentaId(UUID ventaId) {
        try {
            return em.createQuery("SELECT v FROM VentaDetalle v WHERE v.idVenta.id = :ventaId", VentaDetalle.class)
                    .setParameter("ventaId", ventaId)
                    .getResultList();
        } catch (Exception ex) {
            throw new IllegalStateException("Error al obtener los registros por ID de venta", ex);
        }
    }
}
