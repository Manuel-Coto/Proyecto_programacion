package sv.edu.ues.occ.ingenieria.prn335.inventario.web.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.TipoAlmacen;

import java.io.Serializable;
import java.util.List;

@Stateless
@LocalBean
public class TipoAlmacenDAO extends InventarioDefaultDataAccess<TipoAlmacen> implements Serializable {
    public TipoAlmacenDAO() { super(TipoAlmacen.class); }

    @PersistenceContext(unitName = "consolePU")
    EntityManager em;

    @Override
    public EntityManager getEntityManager() {
        return em;
    }


    // Método para obtener todos los tipos de almacen activos
    public List<TipoAlmacen> findActiveTipoAlmacen() {
        String jpql = "SELECT ta FROM TipoAlmacen ta WHERE ta.activo = true";  // Asegura que solo se devuelvan los tipos activos
        TypedQuery<TipoAlmacen> query = em.createQuery(jpql, TipoAlmacen.class);
        return query.getResultList();
    }

    public List<TipoAlmacen> findRange(int first, int max) throws IllegalArgumentException {
        if (first < 0 || max < 1) {
            throw new IllegalArgumentException();
        }

        try {
            EntityManager em = getEntityManager();

            if (em != null) {
                CriteriaBuilder cb = em.getCriteriaBuilder();
                CriteriaQuery<TipoAlmacen> cq = cb.createQuery(TipoAlmacen.class);
                Root<TipoAlmacen> rootEntry = cq.from(TipoAlmacen.class);
                CriteriaQuery<TipoAlmacen> all = cq.select(rootEntry);
                TypedQuery<TipoAlmacen> allQuery = em.createQuery(all);
                allQuery.setFirstResult(first);
                allQuery.setMaxResults(max);
                return allQuery.getResultList();
            }

        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }

        throw new IllegalStateException("No se puede acceder al repositorio");
    }
}
