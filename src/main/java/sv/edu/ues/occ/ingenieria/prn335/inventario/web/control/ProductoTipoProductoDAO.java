package sv.edu.ues.occ.ingenieria.prn335.inventario.web.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;


import java.util.UUID;

import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.ProductoTipoProducto;

@Stateless
public class ProductoTipoProductoDAO extends InventarioDefaultDataAccess<ProductoTipoProducto> {

    @PersistenceContext(unitName = "consolePU")
    EntityManager em;

    public ProductoTipoProductoDAO() {
        super(ProductoTipoProducto.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public ProductoTipoProducto findById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser nulo");
        }

        try {
            EntityManager em = getEntityManager();
            if (em == null) {
                throw new IllegalStateException("EntityManager no disponible");
            }
            return em.find(ProductoTipoProducto.class, id);
        } catch (Exception ex) {
            throw new IllegalStateException("Error al buscar el registro por ID", ex);
        }
    }
}
