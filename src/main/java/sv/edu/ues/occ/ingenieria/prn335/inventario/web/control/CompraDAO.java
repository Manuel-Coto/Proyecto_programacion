package sv.edu.ues.occ.ingenieria.prn335.inventario.web.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
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
            // Validar que el proveedor esté asignado
            if (entidad.getIdProveedor() == null) {
                throw new IllegalArgumentException("El ID del proveedor es obligatorio");
            }

            // Buscar el proveedor en la base de datos
            Proveedor proveedor = em.find(Proveedor.class, entidad.getIdProveedor());
            if (proveedor == null) {
                throw new IllegalArgumentException("El proveedor con ID " + entidad.getIdProveedor() + " no existe");
            }

            // Asignar la entidad completa del proveedor
            entidad.setProveedor(proveedor);

            // Generar el ID usando la secuencia de compra si no tiene ID
            if (entidad.getId() == null) {
                try {
                    // Obtener el siguiente valor de la secuencia de compra
                    Query query = em.createNativeQuery("SELECT nextval('compra_id_compra_seq'::regclass)");
                    Number nextId = (Number) query.getSingleResult();
                    entidad.setId(nextId.intValue());  // Usar intValue() en lugar de longValue()

                    LOGGER.log(Level.INFO, "ID generado para compra: {0}", entidad.getId());
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "No se pudo obtener ID de secuencia, dejando que JPA lo genere", e);
                    // Si falla, dejar que JPA lo genere automáticamente
                }
            }

            LOGGER.log(Level.INFO, "Guardando compra con ID: {0}", entidad.getId());

            // Persistir usando el método de la clase base
            super.crear(entidad);

            LOGGER.log(Level.INFO, "Compra creada exitosamente con ID: {0}", entidad.getId());

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al crear compra", e);

            // Obtener la causa raíz
            Throwable causa = e;
            while (causa.getCause() != null) {
                causa = causa.getCause();
            }
            LOGGER.log(Level.SEVERE, "Causa raíz: {0} - {1}",
                    new Object[]{causa.getClass().getName(), causa.getMessage()});

            throw new RuntimeException("Error al crear la compra: " + causa.getMessage(), e);
        }
    }

    @Override
    public void eliminar(Compra registro) {
        LOGGER.log(Level.INFO, "Eliminando compra con ID: {0}", registro.getId());
        try {
            super.eliminar(registro);
            LOGGER.log(Level.INFO, "Compra eliminada exitosamente");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar compra", e);
            throw new RuntimeException("Error al eliminar la compra: " + e.getMessage(), e);
        }
    }

    @Override
    public Compra findById(Object id) {
        LOGGER.log(Level.FINE, "Buscando compra por ID: {0}", id);
        return super.findById(id);
    }

    /**
     * Método auxiliar para validar el proveedor antes de modificar
     */
    public void validarProveedor(Integer idProveedor) {
        if (idProveedor != null) {
            Proveedor proveedor = em.find(Proveedor.class, idProveedor);
            if (proveedor == null) {
                throw new IllegalArgumentException("El proveedor con ID " + idProveedor + " no existe");
            }
        }
    }

    /**
     * Busca una compra por su ID (Long)
     * Este método convierte automáticamente tipos si es necesario
     */
    public Compra findCompraById(Long idLong) {
        if (idLong == null) {
            return null;
        }

        try {
            LOGGER.log(Level.FINE, "Buscando compra con ID: {0}", idLong);
            return em.find(Compra.class, idLong);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al buscar compra por ID", e);
            return null;
        }
    }

    /**
     * Obtiene todas las compras
     */
    public List<Compra> findAllCompras() {
        LOGGER.log(Level.FINE, "Buscando todas las compras");
        try {
            TypedQuery<Compra> query = em.createQuery("SELECT c FROM Compra c ORDER BY c.id DESC", Compra.class);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener todas las compras", e);
            return List.of();
        }
    }
}