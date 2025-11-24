package sv.edu.ues.occ.ingenieria.prn335.inventario.web.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.CompraDetalle;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.UUID;


@Stateless
@LocalBean
public class CompraDetalleDAO extends InventarioDefaultDataAccess<CompraDetalle> implements Serializable {

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

    @Override
    public CompraDetalle findById(Object id) {
        if (id instanceof UUID) {
            return getEntityManager().find(CompraDetalle.class, id);
        }
        return null;
    }

    public List<CompraDetalle> findLikeConsulta(String consulta) {
        if (consulta == null || consulta.isBlank()) {
            return Collections.emptyList();
        }

        try {
            try {
                UUID uuidConsulta = UUID.fromString(consulta.trim());
                return em.createQuery(
                                "SELECT cd FROM CompraDetalle cd WHERE cd.id = :uuid",
                                CompraDetalle.class)
                        .setParameter("uuid", uuidConsulta)
                        .getResultList();
            } catch (IllegalArgumentException e) {
                return em.createQuery(
                                "SELECT cd FROM CompraDetalle cd WHERE LOWER(cd.idProducto.nombreProducto) LIKE LOWER(:consulta)",
                                CompraDetalle.class)
                        .setParameter("consulta", "%" + consulta + "%")
                        .getResultList();
            }
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }


}