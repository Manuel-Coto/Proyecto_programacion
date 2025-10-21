package sv.edu.ues.occ.ingenieria.prn335.inventario.web.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;

import java.util.List;
import java.util.Objects;

import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.TipoUnidadMedida;

@Stateless
public class TipoUnidadMedidaDAO extends InventarioDefaultDataAccess<TipoUnidadMedida> {

    @PersistenceContext(unitName = "inventarioPU")
    private EntityManager em;

    public TipoUnidadMedidaDAO() {
        super(TipoUnidadMedida.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    /* =========================
       Consultas especializadas
       ========================= */

    /** Solo activos (activo = true). */
    public List<TipoUnidadMedida> findActivos() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<TipoUnidadMedida> cq = cb.createQuery(TipoUnidadMedida.class);
        Root<TipoUnidadMedida> root = cq.from(TipoUnidadMedida.class);
        cq.select(root).where(cb.isTrue(root.get("activo"))).orderBy(cb.asc(root.get("id")));
        return em.createQuery(cq).getResultList();
    }

    /** Búsqueda por nombre (LIKE, case-insensitive) con paginado. */
    public List<TipoUnidadMedida> findByNombreLike(String texto, int first, int max) {
        Objects.requireNonNull(texto, "texto no puede ser null");
        if (first < 0 || max < 1) throw new IllegalArgumentException("first>=0 y max>=1");

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<TipoUnidadMedida> cq = cb.createQuery(TipoUnidadMedida.class);
        Root<TipoUnidadMedida> root = cq.from(TipoUnidadMedida.class);

        cq.select(root)
                .where(cb.like(cb.lower(root.get("nombre")), "%" + texto.toLowerCase() + "%"))
                .orderBy(cb.asc(root.get("id")));

        TypedQuery<TipoUnidadMedida> q = em.createQuery(cq);
        q.setFirstResult(first);
        q.setMaxResults(max);
        return q.getResultList();
    }

    public int countByNombreLike(String texto) {
        Objects.requireNonNull(texto, "texto no puede ser null");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<TipoUnidadMedida> root = cq.from(TipoUnidadMedida.class);

        cq.select(cb.count(root))
                .where(cb.like(cb.lower(root.get("nombre")), "%" + texto.toLowerCase() + "%"));

        return em.createQuery(cq).getSingleResult().intValue();
    }

    /** Búsqueda por unidad base. */
    public List<TipoUnidadMedida> findByUnidadBaseLike(String texto, int first, int max) {
        Objects.requireNonNull(texto, "texto no puede ser null");
        if (first < 0 || max < 1) throw new IllegalArgumentException("first>=0 y max>=1");

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<TipoUnidadMedida> cq = cb.createQuery(TipoUnidadMedida.class);
        Root<TipoUnidadMedida> root = cq.from(TipoUnidadMedida.class);

        cq.select(root)
                .where(cb.like(cb.lower(root.get("unidadBase")), "%" + texto.toLowerCase() + "%"))
                .orderBy(cb.asc(root.get("id")));

        TypedQuery<TipoUnidadMedida> q = em.createQuery(cq);
        q.setFirstResult(first);
        q.setMaxResults(max);
        return q.getResultList();
    }

    /** ¿Existe un nombre exacto (case-insensitive)? */
    public boolean existsByNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) return false;

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<TipoUnidadMedida> root = cq.from(TipoUnidadMedida.class);

        cq.select(cb.count(root))
                .where(cb.equal(cb.lower(root.get("nombre")), nombre.toLowerCase()));

        Long c = em.createQuery(cq).getSingleResult();
        return c != null && c > 0;
    }

    /** Activar/Desactivar por id. */
    public void setActivo(Integer id, boolean activo) {
        Objects.requireNonNull(id, "id no puede ser null");
        TipoUnidadMedida t = em.find(TipoUnidadMedida.class, id);
        if (t != null) {
            t.setActivo(activo);
            em.merge(t);
        }
    }
}
