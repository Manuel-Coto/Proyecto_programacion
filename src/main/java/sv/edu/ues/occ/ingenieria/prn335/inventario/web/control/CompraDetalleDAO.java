package sv.edu.ues.occ.ingenieria.prn335.inventario.web.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.CompraDetalle;

import java.io.Serializable;
import java.util.UUID;

@LocalBean
@Stateless
public class CompraDetalleDAO extends InventarioDefaultDataAccess<CompraDetalle> {

    // Nombre de la unidad de persistencia (asegúrate de que coincida con tu persistence.xml)
    @PersistenceContext(unitName = "consolePU")
    EntityManager em;

    public CompraDetalleDAO() {
        super(CompraDetalle.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public CompraDetalle findById(UUID id) {
        return getEntityManager().find(CompraDetalle.class, id);
    }
}