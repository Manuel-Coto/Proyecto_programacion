package sv.edu.ues.occ.ingenieria.prn335.inventario.web.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.*;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
@LocalBean
public class KardexDAO extends InventarioDefaultDataAccess<Kardex> implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(KardexDAO.class.getName());

    @PersistenceContext(unitName = "consolePU")
    private EntityManager em;

    public KardexDAO() {
        super(Kardex.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    @Override
    public void crear(Kardex entidad) {
        LOGGER.log(Level.INFO, "Creando nuevo registro de kardex...");

        try {
            // Validaciones básicas
            if (entidad.getIdProducto() == null) {
                throw new IllegalArgumentException("El producto es obligatorio");
            }

            if (entidad.getTipoMovimiento() == null || entidad.getTipoMovimiento().isBlank()) {
                throw new IllegalArgumentException("El tipo de movimiento es obligatorio");
            }

            if (entidad.getCantidad() == null) {
                throw new IllegalArgumentException("La cantidad es obligatoria");
            }

            // Generar UUID si no existe
            if (entidad.getId() == null) {
                entidad.setId(UUID.randomUUID());
            }

            // Establecer fecha actual si no tiene
            if (entidad.getFecha() == null) {
                entidad.setFecha(OffsetDateTime.now());
            }

            LOGGER.log(Level.INFO, "Guardando kardex: {0}", entidad.getId());

            // Persistir usando el método de la clase base
            super.crear(entidad);

            LOGGER.log(Level.INFO, "Kardex creado exitosamente con ID: {0}", entidad.getId());

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al crear kardex", e);

            // Obtener la causa raíz
            Throwable causa = e;
            while (causa.getCause() != null) {
                causa = causa.getCause();
            }
            LOGGER.log(Level.SEVERE, "Causa raíz: {0} - {1}",
                    new Object[]{causa.getClass().getName(), causa.getMessage()});

            throw new RuntimeException("Error al crear el registro de kardex: " + causa.getMessage(), e);
        }
    }

    @Override
    public void eliminar(Kardex registro) {
        LOGGER.log(Level.INFO, "Eliminando kardex con ID: {0}", registro.getId());
        try {
            super.eliminar(registro);
            LOGGER.log(Level.INFO, "Kardex eliminado exitosamente");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar kardex", e);
            throw new RuntimeException("Error al eliminar el kardex: " + e.getMessage(), e);
        }
    }

    @Override
    public Kardex findById(Object id) {
        if (!(id instanceof UUID)) {
            throw new IllegalArgumentException("El ID debe ser de tipo UUID");
        }
        LOGGER.log(Level.FINE, "Buscando kardex por ID: {0}", id);
        return super.findById(id);
    }


    /**
     * Obtiene el último movimiento de un producto en un almacén específico
     */
    public Kardex findUltimoMovimiento(UUID idProducto, Integer idAlmacen) {
        if (idProducto == null) {
            throw new IllegalArgumentException("El ID del producto no puede ser nulo");
        }

        try {
            TypedQuery<Kardex> query;

            if (idAlmacen != null) {
                query = em.createQuery(
                        "SELECT k FROM Kardex k WHERE k.idProducto.id = :idProducto " +
                                "AND k.idAlmacen.id = :idAlmacen ORDER BY k.fecha DESC",
                        Kardex.class
                );
                query.setParameter("idAlmacen", idAlmacen);
            } else {
                query = em.createQuery(
                        "SELECT k FROM Kardex k WHERE k.idProducto.id = :idProducto ORDER BY k.fecha DESC",
                        Kardex.class
                );
            }

            query.setParameter("idProducto", idProducto);
            query.setMaxResults(1);

            List<Kardex> resultados = query.getResultList();
            return resultados.isEmpty() ? null : resultados.get(0);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al buscar último movimiento", e);
            throw new RuntimeException("Error al buscar último movimiento del producto", e);
        }
    }

    public void sincronizarDesdeCompra(Compra compra, Integer idAlmacen) {
        try {
            if (compra == null || idAlmacen == null) {
                LOGGER.log(Level.WARNING, "Compra o Almacén nulo");
                return;
            }

            Almacen almacen = em.find(Almacen.class, idAlmacen);
            if (almacen == null) {
                throw new IllegalArgumentException("Almacén no encontrado: " + idAlmacen);
            }

            // Obtener detalles de la compra con query
            TypedQuery<CompraDetalle> queryDetalles = em.createQuery(
                    "SELECT cd FROM CompraDetalle cd WHERE cd.idCompra.id = :idCompra",
                    CompraDetalle.class
            );
            queryDetalles.setParameter("idCompra", compra.getId());
            List<CompraDetalle> detalles = queryDetalles.getResultList();

            if (detalles == null || detalles.isEmpty()) {
                LOGGER.log(Level.WARNING, "Compra sin detalles: {0}", compra.getId());
                return;
            }

            for (CompraDetalle detalle : detalles) {
                Kardex kardex = new Kardex();
                kardex.setId(UUID.randomUUID());
                kardex.setIdProducto(detalle.getIdProducto());
                kardex.setIdAlmacen(almacen);
                kardex.setFecha(OffsetDateTime.now());
                kardex.setTipoMovimiento("ENTRADA");
                kardex.setCantidad(detalle.getCantidad());
                kardex.setPrecio(detalle.getPrecio());
                kardex.setIdCompraDetalle(detalle);
                kardex.setObservaciones("Compra #" + compra.getId());

                em.persist(kardex);

                KardexDetalle kardexDetalle = new KardexDetalle();
                kardexDetalle.setId(UUID.randomUUID());
                kardexDetalle.setIdKardex(kardex);
                kardexDetalle.setLote("LOTE-COMPRA-" + compra.getId());
                kardexDetalle.setActivo(true);

                em.persist(kardexDetalle);
            }

            em.flush();
            LOGGER.log(Level.INFO, "Compra sincronizada a Kardex: {0}", compra.getId());

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error sincronizando compra a Kardex", e);
            throw new RuntimeException("Error: " + e.getMessage(), e);
        }
    }

    public void sincronizarDesdeVenta(Venta venta, Integer idAlmacen) {
        try {
            if (venta == null || idAlmacen == null) {
                LOGGER.log(Level.WARNING, "Venta o Almacén nulo");
                return;
            }

            Almacen almacen = em.find(Almacen.class, idAlmacen);
            if (almacen == null) {
                throw new IllegalArgumentException("Almacén no encontrado: " + idAlmacen);
            }

            // Obtener detalles de la venta con query
            TypedQuery<VentaDetalle> queryDetalles = em.createQuery(
                    "SELECT vd FROM VentaDetalle vd WHERE vd.idVenta.id = :idVenta",
                    VentaDetalle.class
            );
            queryDetalles.setParameter("idVenta", venta.getId());
            List<VentaDetalle> detalles = queryDetalles.getResultList();

            if (detalles == null || detalles.isEmpty()) {
                LOGGER.log(Level.WARNING, "Venta sin detalles: {0}", venta.getId());
                return;
            }

            for (VentaDetalle detalle : detalles) {
                Kardex kardex = new Kardex();
                kardex.setId(UUID.randomUUID());
                kardex.setIdProducto(detalle.getIdProducto());
                kardex.setIdAlmacen(almacen);
                kardex.setFecha(OffsetDateTime.now());
                kardex.setTipoMovimiento("SALIDA");
                kardex.setCantidad(detalle.getCantidad());
                kardex.setPrecio(detalle.getPrecio());
                kardex.setIdVentaDetalle(detalle);
                kardex.setObservaciones("Venta #" + venta.getId());

                em.persist(kardex);

                KardexDetalle kardexDetalle = new KardexDetalle();
                kardexDetalle.setId(UUID.randomUUID());
                kardexDetalle.setIdKardex(kardex);
                kardexDetalle.setLote("LOTE-VENTA-" + venta.getId());
                kardexDetalle.setActivo(true);

                em.persist(kardexDetalle);
            }

            em.flush();
            LOGGER.log(Level.INFO, "Venta sincronizada a Kardex: {0}", venta.getId());

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error sincronizando venta a Kardex", e);
            throw new RuntimeException("Error: " + e.getMessage(), e);
        }
    }



}