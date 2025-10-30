package sv.edu.ues.occ.ingenieria.prn335.inventario.web.control;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.Cliente;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ClienteDAO extends InventarioDefaultDataAccess<Cliente> {

    @PersistenceContext(unitName = "inventarioPU")
    private EntityManager em;

    public ClienteDAO() {
        super(Cliente.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    /* ========= CRUD (firmas alineadas con la superclase) ========= */

    @Override
    @Transactional(Transactional.TxType.REQUIRED)
    public void crear(Cliente c) {
        if (c == null) return;
        if (c.getId() == null) c.setId(UUID.randomUUID());
        em.persist(c);
        em.flush(); // <-- asegura INSERT inmediato
    }

    @Override
    @Transactional(Transactional.TxType.REQUIRED)
    public void modificar(Cliente c) {
        if (c == null) return;
        em.merge(c);
        em.flush(); // <-- asegura UPDATE inmediato
    }

    @Override
    @Transactional(Transactional.TxType.REQUIRED)
    public void eliminar(Cliente c) {
        if (c == null) return;
        em.remove(em.contains(c) ? c : em.merge(c));
        em.flush(); // <-- asegura DELETE inmediato
    }

    /* =================== Lecturas / utilitarios =================== */

    @Transactional(Transactional.TxType.SUPPORTS)
    public Cliente buscarRegistroPorId(UUID id) {
        if (id == null) return null;
        return em.find(Cliente.class, id);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public boolean existsByNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) return false;
        Long total = em.createQuery(
                        "SELECT COUNT(c) FROM Cliente c WHERE LOWER(c.nombre) = LOWER(:n)", Long.class)
                .setParameter("n", nombre.trim())
                .getSingleResult();
        return total != null && total > 0;
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<Cliente> findAll() {
        return em.createQuery("SELECT c FROM Cliente c ORDER BY c.nombre", Cliente.class)
                .getResultList();
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public int count() { // coincide con la firma de la base
        Long total = em.createQuery("SELECT COUNT(c) FROM Cliente c", Long.class)
                .getSingleResult();
        return (total == null) ? 0 : total.intValue();
    }
}
