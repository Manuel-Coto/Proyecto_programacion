package sv.edu.ues.occ.ingenieria.prn335.inventario.web.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.util.List;
import java.util.UUID;

import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.Producto;

@Stateless
public class ProductoDAO extends InventarioDefaultDataAccess<Producto> {

    @PersistenceContext(unitName = "consolePU")
    private EntityManager em;

    public ProductoDAO() { super(Producto.class); }

    @Override
    public EntityManager getEntityManager() { return em; }

    // Método adicional específico para Producto
    public Producto findById(UUID id) { return em.find(Producto.class, id); }

    // Método para buscar productos por nombre con paginación
    public List<Producto> findByNombreLike(String texto, int first, int max) {
        if (first < 0 || max < 1) throw new IllegalArgumentException("first>=0 y max>=1");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Producto> cq = cb.createQuery(Producto.class);
        Root<Producto> r = cq.from(Producto.class);
        cq.select(r).where(cb.like(cb.lower(r.get("nombreProducto")), "%" + texto.toLowerCase() + "%"))
                .orderBy(cb.asc(r.get("nombreProducto")));
        TypedQuery<Producto> q = em.createQuery(cq);
        q.setFirstResult(first);
        q.setMaxResults(max);
        return q.getResultList();
    }
}