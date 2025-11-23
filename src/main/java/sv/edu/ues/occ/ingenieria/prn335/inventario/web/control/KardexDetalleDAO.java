package sv.edu.ues.occ.ingenieria.prn335.inventario.web.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.KardexDetalle;

import java.util.UUID;

@Stateless
public class KardexDetalleDAO extends InventarioDefaultDataAccess<KardexDetalle> {

    @PersistenceContext(unitName = "consolePU")
    private EntityManager em;

    public KardexDetalleDAO() {
        super(KardexDetalle.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    /**
     * Buscar un registro de KardexDetalle por su UUID.
     * @param id identificador único del KardexDetalle
     * @return entidad KardexDetalle encontrada o null si no existe
     */
    public KardexDetalle findById(UUID id) {
        return em.find(KardexDetalle.class, id);
    }
}