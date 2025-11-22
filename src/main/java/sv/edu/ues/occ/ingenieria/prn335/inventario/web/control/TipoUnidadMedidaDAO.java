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

    @PersistenceContext(unitName = "consolePU")
    private EntityManager em;

    public TipoUnidadMedidaDAO() {
        super(TipoUnidadMedida.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    //Consultas especializadas

    // Obtener todos los tipos de unidad de medida activos (version anterior)
    public List<TipoUnidadMedida> findActivos() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<TipoUnidadMedida> cq = cb.createQuery(TipoUnidadMedida.class);
        Root<TipoUnidadMedida> root = cq.from(TipoUnidadMedida.class);
        cq.select(root).where(cb.isTrue(root.get("activo"))).orderBy(cb.asc(root.get("id")));
        return em.createQuery(cq).getResultList();
    }

    // Buscar tipos de unidad de medida por nombre con paginación (version anterior)
    public List<TipoUnidadMedida> findByNombreLike(String texto, int first, int max) {
        Objects.requireNonNull(texto);
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

    // Verificar existencia de un tipo de unidad de medida por nombre (version anterior)
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

    // Activar o desactivar un tipo de unidad de medida por ID
    public void setActivo(Integer id, boolean activo) {
        Objects.requireNonNull(id);
        TipoUnidadMedida t = em.find(TipoUnidadMedida.class, id);
        if (t != null) {
            t.setActivo(activo);
            em.merge(t);
        }
    }
}
