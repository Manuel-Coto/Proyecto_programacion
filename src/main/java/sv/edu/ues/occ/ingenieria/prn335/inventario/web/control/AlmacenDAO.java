package sv.edu.ues.occ.ingenieria.prn335.inventario.web.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.Almacen;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.TipoAlmacen;

import java.io.Serializable;
import java.util.List;

@Stateless
@LocalBean
public class AlmacenDAO extends InventarioDefaultDataAccess<Almacen> implements Serializable {

    // Constructor que indica la clase Almacen para el DAO
    public AlmacenDAO() {
        super(Almacen.class);
    }

    // Inyección del EntityManager para interactuar con la base de datos
    @PersistenceContext(unitName = "consolePU")
    EntityManager em;


    // Método para obtener el EntityManager
    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    // Método para obtener todos los tipos de almacen activos
    public List<TipoAlmacen> findActiveTipoAlmacen() {
        String jpql = "SELECT t FROM TipoAlmacen t WHERE t.activo = true";  // Asegura que solo se devuelvan los tipos activos
        TypedQuery<TipoAlmacen> query = em.createQuery(jpql, TipoAlmacen.class);
        return query.getResultList();
    }

    // Método para buscar un rango de registros
    public List<Almacen> findRange(int first, int max) throws IllegalArgumentException {
        if (first < 0 || max < 1) {
            throw new IllegalArgumentException("El primer valor debe ser mayor o igual a 0 y el máximo debe ser mayor que 0");
        }

        try {
            EntityManager em = getEntityManager();
            if (em != null) {
                // Usamos Criteria API para crear la consulta
                CriteriaBuilder cb = em.getCriteriaBuilder();
                CriteriaQuery<Almacen> cq = cb.createQuery(Almacen.class);
                Root<Almacen> rootEntry = cq.from(Almacen.class);
                CriteriaQuery<Almacen> all = cq.select(rootEntry);

                // Creamos la consulta y le aplicamos el rango de resultados
                TypedQuery<Almacen> allQuery = em.createQuery(all);
                allQuery.setFirstResult(first);
                allQuery.setMaxResults(max);

                // Retornamos el resultado de la consulta
                return allQuery.getResultList();
            }

        } catch (Exception ex) {
            throw new IllegalStateException("Error al acceder al repositorio de Almacen", ex);
        }

        throw new IllegalStateException("No se puede acceder al repositorio de Almacen");
    }

    // Método para buscar un Almacen por su ID
    public Almacen find(Object id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser nulo");
        }
        return em.find(Almacen.class, id); // Usa EntityManager para buscar por ID
    }
}
