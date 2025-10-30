package sv.edu.ues.occ.ingenieria.prn335.inventario.web.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.Venta;
import java.util.List;
import java.util.UUID;

@Stateless
public class VentaDAO extends InventarioDefaultDataAccess<Venta> {

    @PersistenceContext(unitName = "inventarioPU")
    private EntityManager em;

    // Constructor que pasa la clase Venta a la clase padre
    public VentaDAO() {
        super(Venta.class); // Llama al constructor de InventarioDefaultDataAccess con la clase Venta
    }

    // Implementación de getEntityManager() que devuelve el EntityManager inyectado
    @Override
    public EntityManager getEntityManager() {
        return em; // Devuelve el EntityManager inyectado por CDI
    }

    // Puedes añadir métodos personalizados específicos de Venta aquí si es necesario

    // Ejemplo de método personalizado: obtener ventas por estado
    public List<Venta> findByEstado(String estado) {
        try {
            return em.createQuery("SELECT v FROM Venta v WHERE v.estado = :estado", Venta.class)
                    .setParameter("estado", estado)
                    .getResultList();
        } catch (Exception ex) {
            throw new IllegalStateException("Error al obtener los registros por estado", ex);
        }
    }

    // Ejemplo de método personalizado: obtener ventas por ID de cliente
    public List<Venta> findByClienteId(UUID clienteId) {
        try {
            return em.createQuery("SELECT v FROM Venta v WHERE v.idCliente.id = :clienteId", Venta.class)
                    .setParameter("clienteId", clienteId)
                    .getResultList();
        } catch (Exception ex) {
            throw new IllegalStateException("Error al obtener los registros por ID de cliente", ex);
        }
    }
}
