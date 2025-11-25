package sv.edu.ues.occ.ingenieria.prn335.inventario.web.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;

import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.TipoProductoCaracteristica;

import java.util.List;

@Stateless
public class TipoProductoCaracteristicaDAO extends InventarioDefaultDataAccess<TipoProductoCaracteristica> {

    @PersistenceContext(unitName = "consolePU")
    private EntityManager em;

    public TipoProductoCaracteristicaDAO() {
        super(TipoProductoCaracteristica.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public TipoProductoCaracteristica findById(Long id) {
        return em.find(TipoProductoCaracteristica.class, id);
    }
    public Long obtenerMaximoId() {
        try {
            return em.createQuery(
                    "SELECT COALESCE(MAX(t.id), 0) FROM TipoProductoCaracteristica t", Long.class
            ).getSingleResult();
        } catch (Exception e) {
            return 0L;
        }
    }

    public List<TipoProductoCaracteristica> findCaracteristicasByTipoProductoId(Long idTipoProductoSeleccionado) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<TipoProductoCaracteristica> cq = cb.createQuery(TipoProductoCaracteristica.class);
        Root<TipoProductoCaracteristica> root = cq.from(TipoProductoCaracteristica.class);
        Join<Object, Object> joinTipoProducto = root.join("idTipoProducto");

        cq.select(root)
                .where(cb.equal(joinTipoProducto.get("id"), idTipoProductoSeleccionado))
                .orderBy(cb.asc(root.get("caracteristica").get("nombre")));

        return em.createQuery(cq).getResultList();
    }
}
