package sv.edu.ues.occ.ingenieria.prn335.inventario.web.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.Proveedor;

@Stateless
public class ProveedorDAO extends InventarioDefaultDataAccess<Proveedor> {
    @PersistenceContext(
            unitName = "consolePU"
    )
    private EntityManager em;

    public ProveedorDAO() {
        super(Proveedor.class);
    }

    public EntityManager getEntityManager() {
        return this.em;
    }

    public Proveedor buscarRegistroPorId(Integer id) {
        return id == null ? null : (Proveedor)this.em.find(Proveedor.class, id);
    }

    public List<Proveedor> findByNombre(String nombre) {
        String filtro = nombre == null ? "" : nombre.trim();
        TypedQuery<Proveedor> q = this.em.createQuery("SELECT p FROM Proveedor p WHERE (:f = '' OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :f, '%')))", Proveedor.class);
        q.setParameter("f", filtro);
        return q.getResultList();
    }
}
