package sv.edu.ues.occ.ingenieria.prn335.inventario.web.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.util.List;

public class InventarioDefaultDataAccess<T> implements InventarioDAOInterface<T> {

    @PersistenceContext(unitName = "inventarioPU")
    protected EntityManager em;

    protected final Class<T> entityClass;

    public InventarioDefaultDataAccess(Class<T> entityClass) {
        if (entityClass == null) {
            throw new IllegalArgumentException("entityClass no puede ser nulo");
        }
        this.entityClass = entityClass;
    }

    protected EntityManager getEntityManager() {
        if (em == null) {
            throw new IllegalStateException("EntityManager no disponible (¿inyección fallida?)");
        }
        return em;
    }

    @Override
    public List<T> findAll() throws IllegalArgumentException {
        try {
            EntityManager em = getEntityManager();

            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<T> cq = cb.createQuery(entityClass);
            Root<T> root = cq.from(entityClass);
            cq.select(root);
            TypedQuery<T> query = em.createQuery(cq);
            return query.getResultList();
        } catch (Exception ex) {
            throw new IllegalStateException("Error al acceder a todos los registros", ex);
        }
    }

    @Override
    public List<T> findRange(int first, int max) throws IllegalArgumentException {
        if (first < 0 || max < 1) {
            throw new IllegalArgumentException("Parámetros inválidos: first debe ser >= 0, max debe ser >= 1");
        }

        try {
            EntityManager em = getEntityManager();

            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<T> cq = cb.createQuery(entityClass);
            Root<T> root = cq.from(entityClass);
            cq.select(root);
            TypedQuery<T> query = em.createQuery(cq);
            query.setFirstResult(first);
            query.setMaxResults(max);
            return query.getResultList();
        } catch (Exception ex) {
            throw new IllegalStateException("Error al acceder al rango de registros", ex);
        }
    }

    @Override
    public int count() throws IllegalArgumentException {
        try {
            EntityManager em = getEntityManager();

            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Long> cq = cb.createQuery(Long.class);
            Root<T> root = cq.from(entityClass);
            cq.select(cb.count(root));
            TypedQuery<Long> query = em.createQuery(cq);
            return query.getSingleResult().intValue();
        } catch (Exception ex) {
            throw new IllegalStateException("Error al contar registros", ex);
        }
    }

    @Override
    public void crear(T registro) throws IllegalArgumentException {
        if (registro == null) {
            throw new IllegalArgumentException("El registro no puede ser nulo");
        }

        try {
            EntityManager em = getEntityManager();
            em.persist(registro);
        } catch (Exception ex) {
            throw new IllegalStateException("Error al crear el registro", ex);
        }
    }

    @Override
    public void modificar(T registro) throws IllegalArgumentException {
        if (registro == null) {
            throw new IllegalArgumentException("El registro no puede ser nulo");
        }

        try {
            EntityManager em = getEntityManager();
            em.merge(registro);
        } catch (Exception ex) {
            throw new IllegalStateException("Error al modificar el registro", ex);
        }
    }

    @Override
    public void eliminar(T entity) throws IllegalArgumentException {
        if (entity == null) {
            throw new IllegalArgumentException("La entidad no puede ser nula");
        }

        try {
            EntityManager em = getEntityManager();
            if (!em.contains(entity)) {
                entity = em.merge(entity);
            }
            em.remove(entity);
        } catch (Exception ex) {
            System.err.println("Error en DAO.eliminar: " + ex.getMessage());
            ex.printStackTrace();
            throw new IllegalStateException("Error al eliminar el registro", ex);
        }
    }

    @Override
    public void eliminarPorId(Object id) throws IllegalArgumentException {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser nulo");
        }

        try {
            EntityManager em = getEntityManager();
            T registro = em.find(entityClass, id);
            if (registro != null) {
                em.remove(registro);
            } else {
                throw new IllegalArgumentException("Registro con ID " + id + " no encontrado");
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Error al eliminar el registro por ID", ex);
        }
    }

    public T findById(Object id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser nulo");
        }
        try {
            return getEntityManager().find(entityClass, id);
        } catch (Exception e) {
            throw new RuntimeException("Error al encontrar la entidad", e);
        }
    }

}
