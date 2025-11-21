package sv.edu.ues.occ.ingenieria.prn335.inventario.web.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;

import java.util.List;
import java.util.Objects;

import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.Caracteristica;

@Stateless
public class CaracteristicaDAO extends InventarioDefaultDataAccess<Caracteristica> {

    @PersistenceContext(unitName = "inventarioPU")
    private EntityManager em;

    public CaracteristicaDAO() {
        super(Caracteristica.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    /* =========================
       Consultas especializadas
       ========================= */

    /** Búsqueda por nombre (LIKE, case-insensitive). */
    public List<Caracteristica> findByNombreLike(String texto, int first, int max) {
        Objects.requireNonNull(texto, "texto no puede ser null");
        if (first < 0 || max < 1) throw new IllegalArgumentException("first>=0 y max>=1");

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Caracteristica> cq = cb.createQuery(Caracteristica.class);
        Root<Caracteristica> root = cq.from(Caracteristica.class);

        cq.select(root).where(cb.like(cb.lower(root.get("nombre")), "%" + texto.toLowerCase() + "%"))
                .orderBy(cb.asc(root.get("id")));

        TypedQuery<Caracteristica> q = em.createQuery(cq);
        q.setFirstResult(first);
        q.setMaxResults(max);
        return q.getResultList();
    }

    public int countByNombreLike(String texto) {
        Objects.requireNonNull(texto, "texto no puede ser null");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Caracteristica> root = cq.from(Caracteristica.class);

        cq.select(cb.count(root))
                .where(cb.like(cb.lower(root.get("nombre")), "%" + texto.toLowerCase() + "%"));

        return em.createQuery(cq).getSingleResult().intValue();
    }

    /** Lista solo activas (activo = true). */
    public List<Caracteristica> findActivas() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Caracteristica> cq = cb.createQuery(Caracteristica.class);
        Root<Caracteristica> root = cq.from(Caracteristica.class);

        cq.select(root)
                .where(cb.isTrue(root.get("activo")))
                .orderBy(cb.asc(root.get("id")));

        return em.createQuery(cq).getResultList();
    }

    /** Por TipoUnidadMedida (id). */
    public List<Caracteristica> findByTipoUnidadMedidaId(Integer idTipoUnidadMedida) {
        Objects.requireNonNull(idTipoUnidadMedida, "idTipoUnidadMedida no puede ser null");

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Caracteristica> cq = cb.createQuery(Caracteristica.class);
        Root<Caracteristica> root = cq.from(Caracteristica.class);

        cq.select(root)
                .where(cb.equal(root.get("idTipoUnidadMedida").get("id"), idTipoUnidadMedida))
                .orderBy(cb.asc(root.get("id")));

        return em.createQuery(cq).getResultList();
    }

    /** ¿Existe un nombre exacto (case-insensitive)? Útil para evitar duplicados. */
    public boolean existsByNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) return false;

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Caracteristica> root = cq.from(Caracteristica.class);

        cq.select(cb.count(root))
                .where(cb.equal(cb.lower(root.get("nombre")), nombre.toLowerCase()));

        Long c = em.createQuery(cq).getSingleResult();
        return c != null && c > 0;
    }

    /** Activar/Desactivar. */
    public void setActivo(Integer id, boolean activo) {
        Objects.requireNonNull(id, "id no puede ser null");
        Caracteristica c = em.find(Caracteristica.class, id);
        if (c != null) {
            c.setActivo(activo);
            em.merge(c);
        }
    }
}
