package sv.edu.ues.occ.ingenieria.prn335.inventario.web.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.Cliente;
import java.util.UUID;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
public class ClienteDAO extends InventarioDefaultDataAccess<Cliente> {
    private static final Logger LOG = Logger.getLogger(ClienteDAO.class.getName());

    @PersistenceContext(unitName = "consolePU")
    private EntityManager em;

    public ClienteDAO() {
        super(Cliente.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    /**
     * Busca un cliente por su ID (UUID)
     */
    public Cliente findById(UUID id) {
        if (id == null) {
            LOG.log(Level.WARNING, "Intento de buscar cliente con ID nulo");
            return null;
        }

        try {
            Cliente cliente = em.find(Cliente.class, id);
            if (cliente != null) {
                LOG.log(Level.FINE, "Cliente encontrado: {0}", id);
            } else {
                LOG.log(Level.WARNING, "Cliente no encontrado con ID: {0}", id);
            }
            return cliente;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al buscar cliente por ID: " + id, e);
            throw new RuntimeException("Error al buscar cliente", e);
        }
    }

    /**
     * Crea un nuevo cliente
     */
    @Override
    public void crear(Cliente registro) {
        if (registro == null) {
            throw new IllegalArgumentException("El cliente no puede ser nulo");
        }

        if (registro.getId() == null) {
            throw new IllegalArgumentException("El ID del cliente no puede ser nulo");
        }

        try {
            LOG.log(Level.INFO, "Creando cliente con ID: {0}", registro.getId());
            super.crear(registro);
            LOG.log(Level.INFO, "Cliente creado exitosamente: {0}", registro.getId());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al crear cliente", e);
            throw new RuntimeException("Error al crear cliente: " + e.getMessage(), e);
        }
    }

    /**
     * Modifica un cliente existente
     */
    @Override
    public Cliente modificar(Cliente registro) {
        if (registro == null) {
            throw new IllegalArgumentException("El cliente no puede ser nulo");
        }

        if (registro.getId() == null) {
            throw new IllegalArgumentException("El ID del cliente no puede ser nulo");
        }

        try {
            LOG.log(Level.INFO, "Modificando cliente: {0}", registro.getId());
            Cliente resultado = super.modificar(registro);
            LOG.log(Level.INFO, "Cliente modificado exitosamente: {0}", registro.getId());
            return resultado;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al modificar cliente", e);
            throw new RuntimeException("Error al modificar cliente: " + e.getMessage(), e);
        }
    }

    /**
     * Elimina un cliente
     */
    @Override
    public void eliminar(Cliente entity) {
        if (entity == null) {
            throw new IllegalArgumentException("El cliente no puede ser nulo");
        }

        if (entity.getId() == null) {
            throw new IllegalArgumentException("El ID del cliente no puede ser nulo");
        }

        try {
            LOG.log(Level.INFO, "Eliminando cliente: {0}", entity.getId());
            super.eliminar(entity);
            LOG.log(Level.INFO, "Cliente eliminado exitosamente: {0}", entity.getId());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al eliminar cliente", e);
            throw new RuntimeException("Error al eliminar cliente: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene todos los clientes activos
     */
    public List<Cliente> findActivos() {
        try {
            return em.createQuery(
                    "SELECT c FROM Cliente c WHERE c.activo = true ORDER BY c.nombre",
                    Cliente.class
            ).getResultList();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al obtener clientes activos", e);
            return List.of();
        }
    }
}