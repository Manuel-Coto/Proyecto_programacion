package sv.edu.ues.occ.ingenieria.prn335.inventario.web.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.Compra;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.Proveedor;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
@LocalBean
public class CompraDAO extends InventarioDefaultDataAccess<Compra> implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(CompraDAO.class.getName());

    @PersistenceContext(unitName = "consolePU")
    private EntityManager em;

    public CompraDAO() {
        super(Compra.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    @Override
    public void crear(Compra entidad) {
        LOGGER.log(Level.INFO, "Creando nueva compra...");

        try {
            // Validar proveedor
            if (entidad.getIdProveedor() == null) {
                throw new IllegalArgumentException("El ID del proveedor es obligatorio");
            }

            Proveedor proveedor = em.find(Proveedor.class, entidad.getIdProveedor());
            if (proveedor == null) {
                throw new IllegalArgumentException("El proveedor con ID " + entidad.getIdProveedor() + " no existe");
            }

            entidad.setProveedor(proveedor);

            // Generar ID manualmente si no tiene
            if (entidad.getId() == null) {
                Long nuevoId = generarNuevoId();
                entidad.setId(nuevoId);
                LOGGER.log(Level.INFO, "ID generado: {0}", nuevoId);
            }

            em.persist(entidad);
            em.flush();

            LOGGER.log(Level.INFO, "Compra creada exitosamente con ID: {0}", entidad.getId());

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al crear compra", e);
            throw new RuntimeException("Error al crear la compra: " + e.getMessage(), e);
        }
    }


    @Override
    public void eliminar(Compra registro) {
        LOGGER.log(Level.INFO, "Eliminando compra con ID: {0}", registro.getId());
        try {
            if (!em.contains(registro)) {
                registro = em.merge(registro);
            }
            em.remove(registro);
            em.flush();
            LOGGER.log(Level.INFO, "Compra eliminada exitosamente");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar compra", e);
            throw new RuntimeException("Error al eliminar la compra: " + e.getMessage(), e);
        }
    }

    /**
     * Genera un nuevo ID para compra basándose en el máximo existente
     */
    private Long generarNuevoId() {
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COALESCE(MAX(c.id), 0) + 1 FROM Compra c",
                    Long.class
            );
            Long nuevoId = query.getSingleResult();
            LOGGER.log(Level.INFO, "Nuevo ID calculado: {0}", nuevoId);
            return nuevoId;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al calcular ID, usando timestamp", e);
            // Fallback: usar timestamp
            return System.currentTimeMillis();
        }
    }

    @Override
    public Compra findById(Object id) {
        try {
            Long idLong;
            if (id instanceof Integer) {
                idLong = ((Integer) id).longValue();
            } else if (id instanceof Long) {
                idLong = (Long) id;
            } else if (id instanceof String) {
                idLong = Long.parseLong((String) id);
            } else {
                LOGGER.log(Level.WARNING, "Tipo de ID no soportado: {0}", id.getClass());
                return null;
            }

            return em.find(Compra.class, idLong);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error buscando compra", e);
            return null;
        }
    }

    public void validarProveedor(Integer idProveedor) {
        if (idProveedor != null) {
            Proveedor proveedor = em.find(Proveedor.class, idProveedor);
            if (proveedor == null) {
                throw new IllegalArgumentException("El proveedor con ID " + idProveedor + " no existe");
            }
        }
    }

    public List<Compra> findAllCompras() {
        try {
            TypedQuery<Compra> query = em.createQuery(
                    "SELECT c FROM Compra c ORDER BY c.id DESC",
                    Compra.class
            );
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener todas las compras", e);
            return List.of();
        }
    }
}