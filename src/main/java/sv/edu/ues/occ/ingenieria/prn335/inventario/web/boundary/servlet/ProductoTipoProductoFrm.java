package sv.edu.ues.occ.ingenieria.prn335.inventario.web.boundary.servlet;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.event.SelectEvent;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.*;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.Producto;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.ProductoTipoProducto;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.TipoProducto;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.TipoProductoCaracteristica;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Named("productoTipoProductoFrm")
@ViewScoped
public class ProductoTipoProductoFrm extends DefaultFrm<ProductoTipoProducto> implements Serializable {

    @Inject private ProductoTipoProductoDAO dao;
    @Inject private ProductoDAO productoDao;
    @Inject private TipoProductoDAO tipoProductoDao;
    @Inject private TipoProductoCaracteristicaDAO tipoProductoCaracteristicaDao;
    private TipoProductoCaracteristica selectedCaracteristica;
    private TipoProductoCaracteristicaDAO tipoProductoCaracteristicaDAO;


    public boolean isModoLista()   { return getEstado() == ESTADO_CRUD.NADA; }
    public boolean isModoDetalle() { return getEstado() != ESTADO_CRUD.NADA; }

    // === Diálogo de búsqueda de Producto ===
    private String filtroProducto;
    private List<Producto> resultadosBusquedaProducto = Collections.emptyList();

    // === Autocomplete de Tipo de Producto ===
    private List<TipoProducto> sugerenciasTipo = Collections.emptyList();

    // >>> Compatibilidad si el XHTML viejo aún referencia resultadosBusquedaTipo (ya NO se usa)
    private List<TipoProducto> resultadosBusquedaTipo = Collections.emptyList();

    // === Sección de características (visual) ===
    private boolean mostrarSeccionCaracteristicas = false;
    private List<TipoProductoCaracteristica> carDisponibles = Collections.emptyList();
    private List<TipoProductoCaracteristica> carAsignadas = Collections.emptyList();

    @PostConstruct
    @Override
    public void inicializar() {
        this.nombreBean = "Producto - Tipo de Producto";
        super.inicializar();
        ocultarSeccionCar();
    }

    @Override protected FacesContext getFacesContext() { return FacesContext.getCurrentInstance(); }
    @Override protected InventarioDefaultDataAccess<ProductoTipoProducto> getDao() { return dao; }

    @Override
    protected ProductoTipoProducto nuevoRegistro() {
        ProductoTipoProducto r = new ProductoTipoProducto();
        r.setActivo(Boolean.TRUE);
        r.setFechaCreacion(OffsetDateTime.now());
        r.setObservaciones("");
        ocultarSeccionCar();
        return r;
    }

    @Override
    protected ProductoTipoProducto buscarRegistroPorId(Object id) {
        if (id == null) return null;
        try {
            UUID uuid = (id instanceof UUID) ? (UUID) id : UUID.fromString(String.valueOf(id));
            return dao.findById(uuid);
        } catch (Exception e) { return null; }
    }

    @Override
    public void inicializarListas() {
    }

    @Override protected String getIdAsText(ProductoTipoProducto r) {
        return (r != null && r.getId() != null) ? r.getId().toString() : null;
    }
    @Override protected ProductoTipoProducto getIdByText(String id) {
        return (id == null || id.isBlank()) ? null : buscarRegistroPorId(id);
    }

    // ===== CRUD overrides =====

    @Override
    public void btnGuardarHandler(ActionEvent e) {
        try {
            if (this.registro == null) { msg(FacesMessage.SEVERITY_WARN, "Atención", "No hay registro para guardar."); return; }
            if (this.registro.getIdProducto() == null || this.registro.getIdProducto().getId() == null) {
                msg(FacesMessage.SEVERITY_WARN, "Validación", "Seleccione un Producto."); return;
            }
            if (this.registro.getIdTipoProducto() == null || this.registro.getIdTipoProducto().getId() == null) {
                msg(FacesMessage.SEVERITY_WARN, "Validación", "Seleccione un Tipo de Producto."); return;
            }
            if (dao.existsByProductoUuidAndTipoId(this.registro.getIdProducto().getId(), this.registro.getIdTipoProducto().getId())) {
                msg(FacesMessage.SEVERITY_WARN, "Duplicado", "La relación Producto-Tipo ya existe."); return;
            }
            if (this.registro.getId() == null) this.registro.setId(UUID.randomUUID());
            if (this.registro.getFechaCreacion() == null) this.registro.setFechaCreacion(OffsetDateTime.now());
            if (this.registro.getActivo() == null) this.registro.setActivo(Boolean.TRUE);

            dao.crear(this.registro);

            this.registro = null;
            this.estado = ESTADO_CRUD.NADA;
            this.modelo = null;
            inicializarRegistros();
            ocultarSeccionCar();
            msg(FacesMessage.SEVERITY_INFO, "Éxito", "Asociación creada correctamente.");
        } catch (Exception ex) {
            msg(FacesMessage.SEVERITY_ERROR, "Error al guardar", ex.getMessage());
        }
    }

    @Override
    public void btnModificarHandler(ActionEvent e) {
        try {
            if (this.registro == null) { msg(FacesMessage.SEVERITY_WARN, "Atención", "No hay registro para modificar."); return; }
            if (this.registro.getIdProducto() == null || this.registro.getIdTipoProducto() == null) {
                msg(FacesMessage.SEVERITY_WARN, "Validación", "Debe mantener un Producto y un Tipo seleccionados."); return;
            }
            dao.modificar(this.registro);
            msg(FacesMessage.SEVERITY_INFO, "Modificado", "Asociación modificada.");
            this.estado = ESTADO_CRUD.NADA;
            inicializarRegistros();
            ocultarSeccionCar();
        } catch (Exception ex) {
            msg(FacesMessage.SEVERITY_ERROR, "Error al modificar", ex.getMessage());
        }
    }

    @Override
    public void selectionHandler(SelectEvent<ProductoTipoProducto> ev) {
        super.selectionHandler(ev);
        if (this.registro != null && this.registro.getIdTipoProducto() != null && this.registro.getIdTipoProducto().getId() != null) {
            cargarCaracteristicasDeTipo(this.registro.getIdTipoProducto().getId());
        } else {
            ocultarSeccionCar();
        }
    }

    // ===== Productos (diálogo) =====
    public void buscarProductos() {
        String q = (filtroProducto == null) ? "" : filtroProducto.trim();
        try {
            resultadosBusquedaProducto = q.isEmpty()
                    ? productoDao.findAll()
                    : productoDao.findByNombreLike(q, 0, 30);
        } catch (Exception ex) {
            resultadosBusquedaProducto = Collections.emptyList();
        }
    }

    public void seleccionarProducto(Producto p) {
        if (p == null) return;
        if (this.registro == null) this.registro = nuevoRegistro();
        this.registro.setIdProducto(p);
    }

    // ===== Autocomplete TipoProducto =====
    public List<TipoProducto> autoCompleteTipos(String q) {
        String texto = (q == null) ? "" : q.trim();
        try {
            if (texto.isEmpty()) sugerenciasTipo = tipoProductoDao.findRange(0, 20);
            else                 sugerenciasTipo = tipoProductoDao.findByNombreLike(texto, 0, 20);
        } catch (Exception ex) {
            sugerenciasTipo = Collections.emptyList();
        }
        return sugerenciasTipo;
    }

    public void onTipoSelect(SelectEvent<TipoProducto> ev) {
        TipoProducto t = ev.getObject();
        if (t != null) {
            if (this.registro == null) this.registro = nuevoRegistro();
            this.registro.setIdTipoProducto(t);
            if (t.getId() != null) cargarCaracteristicasDeTipo(t.getId());
        }
    }

    // ===== Características (visual) =====
    private void cargarCaracteristicasDeTipo(Long idTipo) {
        try {
            List<TipoProductoCaracteristica> todas = tipoProductoCaracteristicaDao.findByTipoProductoIdFetch(idTipo);
            if (todas == null) {
                carDisponibles = Collections.emptyList();
                carAsignadas = Collections.emptyList();
                mostrarSeccionCaracteristicas = true;
                return;
            }
            carAsignadas = new ArrayList<>();
            carDisponibles = new ArrayList<>();

            for (TipoProductoCaracteristica tpc : todas) {
                if (Boolean.TRUE.equals(tpc.getObligatorio())) {
                    carAsignadas.add(tpc);
                } else {
                    carDisponibles.add(tpc);
                }
            }
            List<TipoProductoCaracteristica> asignadasIds = new ArrayList<>();
            for (TipoProductoCaracteristica asignada : carAsignadas) {
                asignadasIds.add(asignada); // Guardamos las asignadas
            }
            carDisponibles.removeIf(tpc -> asignadasIds.contains(tpc));
            mostrarSeccionCaracteristicas = true;
        } catch (Exception e) {
            carDisponibles = Collections.emptyList();
            carAsignadas = Collections.emptyList();
            mostrarSeccionCaracteristicas = true;
        }
    }


    public void asignarCaracteristicas() {
        for (TipoProductoCaracteristica tpc : carDisponibles) {
            if (tpc.getSelected()) {
                carAsignadas.add(tpc);
                carDisponibles.remove(tpc);
            }
        }
    }

    public void eliminarCaracteristicas() {
        List<TipoProductoCaracteristica> aEliminar = new ArrayList<>();

        for (TipoProductoCaracteristica tpc : carAsignadas) {
            if (tpc.getSelected()) {
                System.out.println("Característica: " + tpc.getIdCaracteristica().getNombre() + " | Obligatoria: " + tpc.getObligatorio());

                if (Boolean.TRUE.equals(tpc.getObligatorio())) {
                    msg(FacesMessage.SEVERITY_WARN, "No se puede eliminar", "No se puede eliminar una característica obligatoria.");
                    return;
                }

                aEliminar.add(tpc);
            }
        }

        for (TipoProductoCaracteristica tpc : aEliminar) {
            carAsignadas.remove(tpc);
            carDisponibles.add(tpc);
        }

        msg(FacesMessage.SEVERITY_INFO, "Éxito", "Característica eliminada correctamente.");
    }

    public void editarCaracteristica(TipoProductoCaracteristica tipoProductoCaracteristica) {
        // Verificamos que la característica esté seleccionada
        if (tipoProductoCaracteristica != null && tipoProductoCaracteristica.getId() != null) {
            try {
                // Si la característica es modificada, guardamos los cambios.
                tipoProductoCaracteristicaDAO.actualizar(tipoProductoCaracteristica); // Usamos el DAO para actualizar la entidad
                // Mensaje de éxito
                msg(FacesMessage.SEVERITY_INFO, "Éxito", "La característica del producto ha sido actualizada.");
            } catch (Exception e) {
                // En caso de error, mostramos mensaje
                msg(FacesMessage.SEVERITY_ERROR, "Error", "Hubo un problema al actualizar la característica del producto.");
                e.printStackTrace();
            }
        }
    }




    private void ocultarSeccionCar() {
        mostrarSeccionCaracteristicas = false;
        carDisponibles = Collections.emptyList();
        carAsignadas = Collections.emptyList();
    }

    public String getFechaCreacionTexto() {
        if (registro == null || registro.getFechaCreacion() == null) return "";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return fmt.format(registro.getFechaCreacion());
    }

    private void msg(FacesMessage.Severity s, String sum, String det) {
        getFacesContext().addMessage(null, new FacesMessage(s, sum, det));
    }

    // ==== Getters / Setters requeridos por la vista ====
    public String getFiltroProducto() { return filtroProducto; }
    public void setFiltroProducto(String filtroProducto) { this.filtroProducto = filtroProducto; }
    public List<Producto> getResultadosBusquedaProducto() { return resultadosBusquedaProducto; }

    // Compat: si algún xhtml viejo pregunta por esto, no truena
    public List<TipoProducto> getResultadosBusquedaTipo() { return resultadosBusquedaTipo; }
    public void setResultadosBusquedaTipo(List<TipoProducto> l) { this.resultadosBusquedaTipo = l; }

    public boolean isMostrarSeccionCaracteristicas() { return mostrarSeccionCaracteristicas; }
    public List<TipoProductoCaracteristica> getCarDisponibles() { return carDisponibles; }
    public List<TipoProductoCaracteristica> getCarAsignadas() { return carAsignadas; }

}
