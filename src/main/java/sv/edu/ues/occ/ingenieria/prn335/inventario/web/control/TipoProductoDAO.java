package sv.edu.ues.occ.ingenieria.prn335.inventario.web.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;

import java.util.List;
import java.util.Objects;

import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.TipoProducto;

@Stateless
public class TipoProductoDAO extends InventarioDefaultDataAccess<TipoProducto> {

    @PersistenceContext(unitName = "inventarioPU")
    private EntityManager em;

    public TipoProductoDAO() { super(TipoProducto.class); }

    @Override
    public EntityManager getEntityManager() { return em; }

    public List<TipoProducto> findRoots() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<TipoProducto> cq = cb.createQuery(TipoProducto.class);
        Root<TipoProducto> root = cq.from(TipoProducto.class);
        cq.select(root).where(cb.isNull(root.get("idTipoProductoPadre"))).orderBy(cb.asc(root.get("nombre")));
        return em.createQuery(cq).getResultList();
    }

    public List<TipoProducto> findChildren(Long parentId) {
        Objects.requireNonNull(parentId, "parentId no puede ser null");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<TipoProducto> cq = cb.createQuery(TipoProducto.class);
        Root<TipoProducto> r = cq.from(TipoProducto.class);
        cq.select(r).where(cb.equal(r.get("idTipoProductoPadre").get("id"), parentId)).orderBy(cb.asc(r.get("nombre")));
        return em.createQuery(cq).getResultList();
    }

    public List<TipoProducto> findAllFetchPadre() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<TipoProducto> cq = cb.createQuery(TipoProducto.class);
        Root<TipoProducto> r = cq.from(TipoProducto.class);
        r.fetch("idTipoProductoPadre", JoinType.LEFT);
        cq.select(r).orderBy(cb.asc(r.get("nombre")));
        return em.createQuery(cq).getResultList();
    }

    public List<TipoProducto> findByNombreLike(String texto, int first, int max) {
        Objects.requireNonNull(texto, "texto no puede ser null");
        if (first < 0 || max < 1) throw new IllegalArgumentException("first>=0 y max>=1");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<TipoProducto> cq = cb.createQuery(TipoProducto.class);
        Root<TipoProducto> r = cq.from(TipoProducto.class);
        cq.select(r).where(cb.like(cb.lower(r.get("nombre")), "%" + texto.toLowerCase() + "%"))
                .orderBy(cb.asc(r.get("nombre")));
        TypedQuery<TipoProducto> q = em.createQuery(cq);
        q.setFirstResult(first);
        q.setMaxResults(max);
        return q.getResultList();
    }

    public TipoProducto findById(Long id) {
        if (id == null) return null;
        return em.find(TipoProducto.class, id);
    }

    public java.util.Optional<TipoProducto> findOptionalById(Long id) {
        return java.util.Optional.ofNullable(findById(id));
    }

    public List<TipoProducto> findAll() {
        return em.createQuery("SELECT t FROM TipoProducto t", TipoProducto.class).getResultList();
    }
}
