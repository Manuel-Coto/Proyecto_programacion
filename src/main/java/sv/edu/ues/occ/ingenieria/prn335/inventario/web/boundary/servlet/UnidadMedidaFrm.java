package sv.edu.ues.occ.ingenieria.prn335.inventario.web.boundary.servlet;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.InventarioDefaultDataAccess;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.UnidadMedidaDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.TipoUnidadMedida;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.UnidadMedida;

@Named("unidadMedidaFrm")
@ViewScoped
public class UnidadMedidaFrm extends DefaultFrm<UnidadMedida> implements Serializable {

    @Inject
    private UnidadMedidaDAO unidadMedidaDao;

    @Inject
    private TipoUnidadMedidaFrm tipoUnidadMedidaFrm;

    @PostConstruct
    @Override
    public void inicializar() {
        this.nombreBean = "Unidad de Medida";
        super.inicializar();
    }

    @Override
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    @Override
    protected InventarioDefaultDataAccess<UnidadMedida> getDao() {
        return unidadMedidaDao;
    }

    @Override
    protected UnidadMedida nuevoRegistro() {
        UnidadMedida um = new UnidadMedida();
        um.setActivo(Boolean.TRUE);
        um.setEquivalencia(BigDecimal.ZERO);
        um.setExpresionRegular("");
        um.setComentarios("");

        // Asigna automáticamente el tipo seleccionado
        if (tipoUnidadMedidaFrm != null && tipoUnidadMedidaFrm.getRegistro() != null) {
            TipoUnidadMedida tipo = tipoUnidadMedidaFrm.getRegistro();
            if (tipo.getId() != null) {
                um.setIdTipoUnidadMedida(tipo);
            }
        }
        return um;
    }

    // Busca UnidadMedida por ID
    @Override
    protected UnidadMedida buscarRegistroPorId(Object id) {
        if (id == null) return null;
        try {
            Integer iid = (id instanceof Integer) ? (Integer) id : Integer.valueOf(String.valueOf(id));
            return unidadMedidaDao.getEntityManager().find(UnidadMedida.class, iid);
        } catch (Exception e) {
            return null;
        }
    }


    // Función para obtener el texto a partir del ID
    @Override
    protected String getIdAsText(UnidadMedida r) {
        return (r != null && r.getId() != null) ? String.valueOf(r.getId()) : null;
    }

    // Función para obtener el ID a partir del texto
    @Override
    protected UnidadMedida getIdByText(String id) {
        if (id == null) return null;
        return buscarRegistroPorId(id);
    }

    // Evita validación getNombre() que no aplica aquí
    @Override
    public void btnGuardarHandler(ActionEvent e) {
        if (this.registro == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Atención", "No hay registro para guardar"));
            return;
        }
        try {
            if (registro.getIdTipoUnidadMedida() == null && tipoUnidadMedidaFrm.getRegistro() != null) {
                registro.setIdTipoUnidadMedida(tipoUnidadMedidaFrm.getRegistro());
            }

            unidadMedidaDao.crear(registro);
            this.registro = null;
            this.estado = ESTADO_CRUD.NADA;
            inicializarRegistros();

            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Unidad de Medida guardada correctamente"));
        } catch (Exception ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al guardar", ex.getMessage()));
        }
    }

    // Devuelve unidades de un tipo (version anterior a la integración con TipoUnidadMedidaFrm)
    public List<UnidadMedida> getListaUnidadMedida(Integer idTipo) {
        if (idTipo == null) return java.util.Collections.emptyList();
        return unidadMedidaDao.findByTipoUnidadMedida(idTipo, 0, 200);
    }
}
