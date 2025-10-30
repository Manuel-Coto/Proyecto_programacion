package sv.edu.ues.occ.ingenieria.prn335.inventario.web.control;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.Cliente;
import java.util.UUID;
import java.util.List;

@ApplicationScoped
public class ClienteDAO extends InventarioDefaultDataAccess<Cliente> {

    @PersistenceContext(unitName = "inventarioPU")
    private EntityManager em;

    public ClienteDAO() {
        super(Cliente.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    /* =======================
       Métodos personalizados
       ======================= */

    /** Buscar cliente por ID (UUID) */
    public Cliente buscarRegistroPorId(UUID id) {
        if (id == null) return null;
        try {
            return em.find(Cliente.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Verificar si ya existe un cliente con el mismo nombre */
    public boolean existsByNombre(String nombre) {
        try {
            Long count = em.createQuery(
                            "SELECT COUNT(c) FROM Cliente c WHERE c.nombre = :nombre", Long.class)
                    .setParameter("nombre", nombre)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Obtener todos los clientes */
    public List<Cliente> findAll() {
        try {
            return em.createQuery("SELECT c FROM Cliente c", Cliente.class).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Eliminar un cliente por ID */
    public void eliminarPorId(UUID id) {
        try {
            Cliente cliente = buscarRegistroPorId(id);
            if (cliente != null) {
                em.remove(cliente);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Modificar un cliente */
    public void modificar(Cliente cliente) {
        try {
            em.merge(cliente);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Crear un nuevo cliente */
    public void crear(Cliente cliente) {
        try {
            em.persist(cliente);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
