package sv.edu.ues.occ.ingenieria.prn335.inventario.web.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.*;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
@LocalBean
public class KardexDAO extends InventarioDefaultDataAccess<Kardex> implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(KardexDAO.class.getName());

    @PersistenceContext(unitName = "consolePU")
    private EntityManager em;

    public KardexDAO() {
        super(Kardex.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    @Override
    public void crear(Kardex entidad) {
        LOGGER.log(Level.INFO, "Creando nuevo registro de kardex...");

        try {
            // Validaciones básicas
            if (entidad.getIdProducto() == null) {
                throw new IllegalArgumentException("El producto es obligatorio");
            }

            if (entidad.getTipoMovimiento() == null || entidad.getTipoMovimiento().isBlank()) {
                throw new IllegalArgumentException("El tipo de movimiento es obligatorio");
            }

            if (entidad.getCantidad() == null) {
                throw new IllegalArgumentException("La cantidad es obligatoria");
            }

            // Generar UUID si no existe
            if (entidad.getId() == null) {
                entidad.setId(UUID.randomUUID());
            }

            // Establecer fecha actual si no tiene
            if (entidad.getFecha() == null) {
                entidad.setFecha(OffsetDateTime.now());
            }

            LOGGER.log(Level.INFO, "Guardando kardex: {0}", entidad.getId());

            // Persistir usando el método de la clase base
            super.crear(entidad);

            LOGGER.log(Level.INFO, "Kardex creado exitosamente con ID: {0}", entidad.getId());

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al crear kardex", e);

            // Obtener la causa raíz
            Throwable causa = e;
            while (causa.getCause() != null) {
                causa = causa.getCause();
            }
            LOGGER.log(Level.SEVERE, "Causa raíz: {0} - {1}",
                    new Object[]{causa.getClass().getName(), causa.getMessage()});

            throw new RuntimeException("Error al crear el registro de kardex: " + causa.getMessage(), e);
        }
    }

    @Override
    public void eliminar(Kardex registro) {
        LOGGER.log(Level.INFO, "Eliminando kardex con ID: {0}", registro.getId());
        try {
            super.eliminar(registro);
            LOGGER.log(Level.INFO, "Kardex eliminado exitosamente");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar kardex", e);
            throw new RuntimeException("Error al eliminar el kardex: " + e.getMessage(), e);
        }
    }

    @Override
    public Kardex findById(Object id) {
        if (!(id instanceof UUID)) {
            throw new IllegalArgumentException("El ID debe ser de tipo UUID");
        }
        LOGGER.log(Level.FINE, "Buscando kardex por ID: {0}", id);
        return super.findById(id);
    }


    /**
     * Obtiene el último movimiento de un producto en un almacén específico
     */
    public Kardex findUltimoMovimiento(UUID idProducto, Integer idAlmacen) {
        if (idProducto == null) {
            throw new IllegalArgumentException("El ID del producto no puede ser nulo");
        }

        try {
            TypedQuery<Kardex> query;

            if (idAlmacen != null) {
                query = em.createQuery(
                        "SELECT k FROM Kardex k WHERE k.idProducto.id = :idProducto " +
                                "AND k.idAlmacen.id = :idAlmacen ORDER BY k.fecha DESC",
                        Kardex.class
                );
                query.setParameter("idAlmacen", idAlmacen);
            } else {
                query = em.createQuery(
                        "SELECT k FROM Kardex k WHERE k.idProducto.id = :idProducto ORDER BY k.fecha DESC",
                        Kardex.class
                );
            }

            query.setParameter("idProducto", idProducto);
            query.setMaxResults(1);

            List<Kardex> resultados = query.getResultList();
            return resultados.isEmpty() ? null : resultados.get(0);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al buscar último movimiento", e);
            throw new RuntimeException("Error al buscar último movimiento del producto", e);
        }
    }


}