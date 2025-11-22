package sv.edu.ues.occ.ingenieria.prn335.inventario.web.control;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.Cliente;
import java.util.UUID;
import java.util.List;

@ApplicationScoped
public class ClienteDAO extends InventarioDefaultDataAccess<Cliente> {

    @PersistenceContext(unitName = "consolePU")
    EntityManager em;

    public ClienteDAO(Class<Cliente> entityClass) {
        super(entityClass);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public ClienteDAO() { super(Cliente.class); }
    // En ClienteDAO.java
    public Cliente findById(UUID id) {
        return getEntityManager().find(Cliente.class, id);
    }
}
