package sv.edu.ues.occ.ingenieria.prn335.inventario.web.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.VentaDetalle;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Stateless
@LocalBean
public class VentaDetalleDAO extends InventarioDefaultDataAccess<VentaDetalle> implements Serializable {

    @PersistenceContext(unitName = "consolePU")
    EntityManager em;
    public VentaDetalleDAO() {
        super(VentaDetalle.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public VentaDetalle findById(UUID id) {
        return getEntityManager().find(VentaDetalle.class, id);
    }

    public List<VentaDetalle> buscarPorVenta(UUID id) {
        return em.createQuery(
                        "SELECT t FROM VentaDetalle t WHERE t.idVenta.id = :id ORDER BY t.id",
                        VentaDetalle.class)
                .setParameter("id", id)
                .getResultList();
    }

    public List<VentaDetalle> findLikeConsulta(String consulta) {
        if (consulta == null || consulta.isBlank()) {
            return Collections.emptyList();
        }

        try {

            try {
                UUID uuidConsulta = UUID.fromString(consulta.trim());
                return em.createQuery(
                                "SELECT vd FROM VentaDetalle vd WHERE vd.id = :uuid",
                                VentaDetalle.class)
                        .setParameter("uuid", uuidConsulta)
                        .getResultList();
            } catch (IllegalArgumentException e) {

                return em.createQuery(
                                "SELECT vd FROM VentaDetalle vd WHERE LOWER(vd.idProducto.nombreProducto) LIKE LOWER(:consulta)",
                                VentaDetalle.class)
                        .setParameter("consulta", "%" + consulta + "%")
                        .getResultList();
            }
        } catch (Exception e) {

            return Collections.emptyList();
        }
    }
}
