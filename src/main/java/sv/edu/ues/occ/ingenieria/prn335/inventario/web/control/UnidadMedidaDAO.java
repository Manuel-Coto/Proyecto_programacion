package sv.edu.ues.occ.ingenieria.prn335.inventario.web.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import java.util.List;
import java.util.Objects;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.UnidadMedida;

@Stateless
public class UnidadMedidaDAO extends InventarioDefaultDataAccess<UnidadMedida> {

    @PersistenceContext(unitName = "consolePU")
    private EntityManager em;

    public UnidadMedidaDAO() {
        super(UnidadMedida.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    // Consultas específicas

    // Buscar unidades de medida por tipo con paginación
    public List<UnidadMedida> findByTipoUnidadMedida(Integer idTipo, int first, int max) {
        Objects.requireNonNull(idTipo, "El idTipo no puede ser nulo");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<UnidadMedida> cq = cb.createQuery(UnidadMedida.class);
        Root<UnidadMedida> root = cq.from(UnidadMedida.class);
        cq.select(root)
                .where(cb.equal(root.get("idTipoUnidadMedida").get("id"), idTipo))
                .orderBy(cb.asc(root.get("id")));
        TypedQuery<UnidadMedida> q = em.createQuery(cq);
        q.setFirstResult(first);
        q.setMaxResults(max);
        return q.getResultList();
    }
}
