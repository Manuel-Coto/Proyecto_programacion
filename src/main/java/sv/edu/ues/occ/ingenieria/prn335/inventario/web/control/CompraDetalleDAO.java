package sv.edu.ues.occ.ingenieria.prn335.inventario.web.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.CompraDetalle;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
@LocalBean
public class CompraDetalleDAO extends InventarioDefaultDataAccess<CompraDetalle> implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(CompraDetalleDAO.class.getName());

    @PersistenceContext(unitName = "consolePU")
    private EntityManager em;

    public CompraDetalleDAO() {
        super(CompraDetalle.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    @Override
    public void crear(CompraDetalle entidad) {
        LOGGER.log(Level.INFO, "Creando nuevo detalle de compra...");

        try {
            // Validaciones de la entidad CompraDetalle
            if (entidad.getIdCompra() == null || entidad.getIdProducto() == null) {
                throw new IllegalArgumentException("La compra y el producto son obligatorios");
            }

            LOGGER.log(Level.INFO, "Guardando detalle de compra: {0}", entidad);

            // Persistir el detalle de la compra
            super.crear(entidad);

            LOGGER.log(Level.INFO, "Detalle de compra creado exitosamente con ID: {0}", entidad.getId());

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al crear detalle de compra", e);
            throw new RuntimeException("Error al crear el detalle de compra: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(CompraDetalle registro) {
        LOGGER.log(Level.INFO, "Eliminando detalle de compra con ID: {0}", registro.getId());
        try {
            super.eliminar(registro);
            LOGGER.log(Level.INFO, "Detalle de compra eliminado exitosamente");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar detalle de compra", e);
            throw new RuntimeException("Error al eliminar el detalle de compra: " + e.getMessage(), e);
        }
    }

    @Override
    public CompraDetalle findById(Object id) {
        LOGGER.log(Level.FINE, "Buscando detalle de compra por ID: {0}", id);
        return super.findById(id);
    }

    // Método para obtener un listado de todos los detalles de compra
    public List<CompraDetalle> findAllCompraDetalles() {
        LOGGER.log(Level.FINE, "Buscando todos los detalles de compra");
        TypedQuery<CompraDetalle> query = em.createQuery("SELECT cd FROM CompraDetalle cd", CompraDetalle.class);
        return query.getResultList();
    }

    // Método para obtener todos los detalles de compra asociados a una compra específica
    public List<CompraDetalle> findByCompraId(UUID compraId) {
        LOGGER.log(Level.FINE, "Buscando detalles de compra para la compra con ID: {0}", compraId);
        TypedQuery<CompraDetalle> query = em.createQuery("SELECT cd FROM CompraDetalle cd WHERE cd.idCompra.id = :compraId", CompraDetalle.class);
        query.setParameter("compraId", compraId);
        return query.getResultList();
    }

    // Método auxiliar para verificar si la entidad está asociada correctamente
    public boolean verificarExistencia(CompraDetalle entidad) {
        LOGGER.log(Level.FINE, "Verificando existencia de detalle de compra");
        return findById(entidad.getId()) != null;
    }
}