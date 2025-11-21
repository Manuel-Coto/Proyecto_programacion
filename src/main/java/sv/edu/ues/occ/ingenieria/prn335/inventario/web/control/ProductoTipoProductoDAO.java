package sv.edu.ues.occ.ingenieria.prn335.inventario.web.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.ProductoTipoProducto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Stateless
public class ProductoTipoProductoDAO extends InventarioDefaultDataAccess<ProductoTipoProducto> {

    @PersistenceContext(unitName = "inventarioPU")
    private EntityManager em;

    public ProductoTipoProductoDAO() {
        super(ProductoTipoProducto.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    /* ========= Utilitarios ========= */

    public ProductoTipoProducto findById(UUID id) {
        return (id == null) ? null : em.find(ProductoTipoProducto.class, id);
    }

    public Optional<ProductoTipoProducto> findOptionalById(UUID id) {
        return Optional.ofNullable(findById(id));
    }

    public void crearYFlush(ProductoTipoProducto e) {
        em.persist(e);
        em.flush();
    }

    public ProductoTipoProducto modificarYFlush(ProductoTipoProducto e) {
        ProductoTipoProducto m = em.merge(e);
        em.flush();
        return m;
    }

    /* ========= Consultas específicas ========= */

    /** Todos los registros asociados a un Producto (UUID). */
    public List<ProductoTipoProducto> findByProductoUuid(UUID idProducto) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ProductoTipoProducto> cq = cb.createQuery(ProductoTipoProducto.class);
        Root<ProductoTipoProducto> r = cq.from(ProductoTipoProducto.class);
        cq.select(r)
                .where(cb.equal(r.get("idProducto").get("id"), idProducto))
                .orderBy(cb.asc(r.get("fechaCreacion")));
        return em.createQuery(cq).getResultList();
    }

    /** Todos los registros asociados a un Tipo de Producto (Long). */
    public List<ProductoTipoProducto> findByTipoProductoId(Long idTipoProducto) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ProductoTipoProducto> cq = cb.createQuery(ProductoTipoProducto.class);
        Root<ProductoTipoProducto> r = cq.from(ProductoTipoProducto.class);
        cq.select(r)
                .where(cb.equal(r.get("idTipoProducto").get("id"), idTipoProducto))
                .orderBy(cb.asc(r.get("fechaCreacion")));
        return em.createQuery(cq).getResultList();
    }

    /** ¿Existe ya la relación (Producto UUID, TipoProducto Long)? */
    public boolean existsByProductoUuidAndTipoId(UUID idProducto, Long idTipoProducto) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<ProductoTipoProducto> r = cq.from(ProductoTipoProducto.class);
        cq.select(cb.count(r))
                .where(
                        cb.equal(r.get("idProducto").get("id"), idProducto),
                        cb.equal(r.get("idTipoProducto").get("id"), idTipoProducto)
                );
        TypedQuery<Long> q = em.createQuery(cq);
        Long cnt = q.getSingleResult();
        return cnt != null && cnt > 0L;
    }
}
