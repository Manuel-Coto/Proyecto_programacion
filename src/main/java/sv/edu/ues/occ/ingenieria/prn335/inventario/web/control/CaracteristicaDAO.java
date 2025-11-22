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

    @PersistenceContext(unitName = "consolePU")
    private EntityManager em;

    public CaracteristicaDAO() {
        super(Caracteristica.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    // Consultas especializadas

    // Buscar por nombre (like), paginado
    public List<Caracteristica> findByNombreLike(String texto, int first, int max) {
        Objects.requireNonNull(texto, "texto no puede ser null");
        if (first < 0 || max < 1) throw new IllegalArgumentException("first>=0 y max>=1");

        CriteriaBuilder cb = em.getCriteriaBuilder(); // Constructor de consultas
        CriteriaQuery<Caracteristica> cq = cb.createQuery(Caracteristica.class);
        Root<Caracteristica> root = cq.from(Caracteristica.class);

        cq.select(root).where(cb.like(cb.lower(root.get("nombre")), "%" + texto.toLowerCase() + "%"))
                .orderBy(cb.asc(root.get("id"))); // Ordenar por id ascendente

        TypedQuery<Caracteristica> q = em.createQuery(cq);
        q.setFirstResult(first);
        q.setMaxResults(max);
        return q.getResultList();
    }

    // Verificar existencia por nombre para evitar duplicados
    public boolean existsByNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) return false;

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Caracteristica> root = cq.from(Caracteristica.class);

        cq.select(cb.count(root))
                .where(cb.equal(cb.lower(root.get("nombre")), nombre.toLowerCase())); // Comparar en minúsculas

        Long c = em.createQuery(cq).getSingleResult();
        return c != null && c > 0;
    }

    // Activar / desactivar
    public void setActivo(Integer id, boolean activo) {
        Objects.requireNonNull(id, "id no puede ser null");
        Caracteristica c = em.find(Caracteristica.class, id);
        if (c != null) {
            c.setActivo(activo);
            em.merge(c);
        }
    }
}
