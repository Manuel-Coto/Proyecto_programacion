package sv.edu.ues.occ.ingenieria.prn335.inventario.web.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.TipoProductoCaracteristica;

@Stateless
public class TipoProductoCaracteristicaDAO extends InventarioDefaultDataAccess<TipoProductoCaracteristica> {

    @PersistenceContext(unitName = "inventarioPU")
    private EntityManager em;

    public TipoProductoCaracteristicaDAO() {
        super(TipoProductoCaracteristica.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    /* ==========================
       Consultas por TipoProducto
       ========================== */

    /** Lista todas las características asociadas a un TipoProducto por su id. */
    public List<TipoProductoCaracteristica> findByTipoProductoId(Long idTipoProducto) {
        Objects.requireNonNull(idTipoProducto, "idTipoProducto no puede ser null");
        EntityManager em = getOrFail();

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<TipoProductoCaracteristica> cq = cb.createQuery(TipoProductoCaracteristica.class);
        Root<TipoProductoCaracteristica> root = cq.from(TipoProductoCaracteristica.class);

        cq.select(root)
                .where(cb.equal(root.get("idTipoProducto").get("id"), idTipoProducto))
                .orderBy(cb.asc(root.get("id")));

        return em.createQuery(cq).getResultList();
    }

    /** Versión con paginado. */
    public List<TipoProductoCaracteristica> findByTipoProductoId(Long idTipoProducto, int first, int max) {
        Objects.requireNonNull(idTipoProducto, "idTipoProducto no puede ser null");
        if (first < 0 || max < 1) throw new IllegalArgumentException("first>=0 y max>=1");

        EntityManager em = getOrFail();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<TipoProductoCaracteristica> cq = cb.createQuery(TipoProductoCaracteristica.class);
        Root<TipoProductoCaracteristica> root = cq.from(TipoProductoCaracteristica.class);

        cq.select(root)
                .where(cb.equal(root.get("idTipoProducto").get("id"), idTipoProducto))
                .orderBy(cb.asc(root.get("id")));

        TypedQuery<TipoProductoCaracteristica> q = em.createQuery(cq);
        q.setFirstResult(first);
        q.setMaxResults(max);
        return q.getResultList();
    }

    /** Cuenta las características asociadas a un TipoProducto. */
    public int countByTipoProductoId(Long idTipoProducto) {
        Objects.requireNonNull(idTipoProducto, "idTipoProducto no puede ser null");
        EntityManager em = getOrFail();

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<TipoProductoCaracteristica> root = cq.from(TipoProductoCaracteristica.class);

        cq.select(cb.count(root))
                .where(cb.equal(root.get("idTipoProducto").get("id"), idTipoProducto));

        return em.createQuery(cq).getSingleResult().intValue(); // Mantengo int para calzar con tu base
    }

    /** Trae con fetch los many-to-one para evitar LazyInitialization si necesitas datos completos. */
    public List<TipoProductoCaracteristica> findByTipoProductoIdFetch(Long idTipoProducto) {
        Objects.requireNonNull(idTipoProducto, "idTipoProducto no puede ser null");
        EntityManager em = getOrFail();

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<TipoProductoCaracteristica> cq = cb.createQuery(TipoProductoCaracteristica.class);
        Root<TipoProductoCaracteristica> root = cq.from(TipoProductoCaracteristica.class);
        // fetch de relaciones ManyToOne (seguro)
        root.fetch("idCaracteristica", JoinType.LEFT);
        root.fetch("idTipoProducto", JoinType.LEFT);

        cq.select(root)
                .where(cb.equal(root.get("idTipoProducto").get("id"), idTipoProducto))
                .orderBy(cb.asc(root.get("id")));

        return em.createQuery(cq).getResultList();
    }

    /** Solo las obligatorias de un TipoProducto. */
    public List<TipoProductoCaracteristica> findObligatoriasByTipoProductoId(Long idTipoProducto) {
        Objects.requireNonNull(idTipoProducto, "idTipoProducto no puede ser null");
        EntityManager em = getOrFail();

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<TipoProductoCaracteristica> cq = cb.createQuery(TipoProductoCaracteristica.class);
        Root<TipoProductoCaracteristica> root = cq.from(TipoProductoCaracteristica.class);

        cq.select(root)
                .where(
                        cb.and(
                                cb.equal(root.get("idTipoProducto").get("id"), idTipoProducto),
                                cb.isTrue(root.get("obligatorio"))
                        )
                )
                .orderBy(cb.asc(root.get("id")));

        return em.createQuery(cq).getResultList();
    }

    /* =============================
       Consultas por Característica
       ============================= */

    public List<TipoProductoCaracteristica> findByCaracteristicaId(Long idCaracteristica) {
        Objects.requireNonNull(idCaracteristica, "idCaracteristica no puede ser null");
        EntityManager em = getOrFail();

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<TipoProductoCaracteristica> cq = cb.createQuery(TipoProductoCaracteristica.class);
        Root<TipoProductoCaracteristica> root = cq.from(TipoProductoCaracteristica.class);

        cq.select(root)
                .where(cb.equal(root.get("idCaracteristica").get("id"), idCaracteristica))
                .orderBy(cb.asc(root.get("id")));

        return em.createQuery(cq).getResultList();
    }

    /* ==========================================
       Búsqueda/Existencia por la combinación única
       ========================================== */

    /** Busca por (idTipoProducto, idCaracteristica). */
    public Optional<TipoProductoCaracteristica> findByTipoProductoAndCaracteristica(Long idTipoProducto, Long idCaracteristica) {
        Objects.requireNonNull(idTipoProducto, "idTipoProducto no puede ser null");
        Objects.requireNonNull(idCaracteristica, "idCaracteristica no puede ser null");

        EntityManager em = getOrFail();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<TipoProductoCaracteristica> cq = cb.createQuery(TipoProductoCaracteristica.class);
        Root<TipoProductoCaracteristica> root = cq.from(TipoProductoCaracteristica.class);

        cq.select(root)
                .where(
                        cb.and(
                                cb.equal(root.get("idTipoProducto").get("id"), idTipoProducto),
                                cb.equal(root.get("idCaracteristica").get("id"), idCaracteristica)
                        )
                );

        List<TipoProductoCaracteristica> lista = em.createQuery(cq).setMaxResults(1).getResultList();
        return lista.isEmpty() ? Optional.empty() : Optional.of(lista.get(0));
    }

    /** Verifica existencia por (idTipoProducto, idCaracteristica). */
    public boolean existsByTipoProductoAndCaracteristica(Long idTipoProducto, Long idCaracteristica) {
        Objects.requireNonNull(idTipoProducto, "idTipoProducto no puede ser null");
        Objects.requireNonNull(idCaracteristica, "idCaracteristica no puede ser null");

        EntityManager em = getOrFail();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<TipoProductoCaracteristica> root = cq.from(TipoProductoCaracteristica.class);

        cq.select(cb.count(root))
                .where(
                        cb.and(
                                cb.equal(root.get("idTipoProducto").get("id"), idTipoProducto),
                                cb.equal(root.get("idCaracteristica").get("id"), idCaracteristica)
                        )
                );

        Long c = em.createQuery(cq).getSingleResult();
        return c != null && c > 0;
    }

    /* ======================
       Helpers internos
       ====================== */

    private EntityManager getOrFail() {
        EntityManager em = getEntityManager();
        if (em == null) {
            throw new IllegalStateException("EntityManager no disponible");
        }
        return em;
    }
}
