package sv.edu.ues.occ.ingenieria.prn335.inventario.web.boundary.servlet;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.faces.event.ActionEvent;
import org.primefaces.model.LazyDataModel;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.AlmacenDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.InventarioDefaultDataAccess;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.control.TipoAlmacenDAO;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.Almacen;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.TipoAlmacen;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@ViewScoped
public class AlmacenFrm extends DefaultFrm<Almacen> implements Serializable {

    @Inject
    FacesContext facesContext;

    @Inject
    AlmacenDAO almacenDAO;

    @Inject
    TipoAlmacenDAO tipoAlmacenDAO;

    List<TipoAlmacen> listaTipoAlmacen;

    @Override
    protected InventarioDefaultDataAccess<Almacen> getDao() {
        return almacenDAO;
    }

    protected Almacen createNewEntity() {
        Almacen almacen = new Almacen();
        almacen.setActivo(true);
        if(this.listaTipoAlmacen != null && !this.listaTipoAlmacen.isEmpty()){
            // Asigna el primer tipo activo como predeterminado
            TipoAlmacen primerActivo = this.listaTipoAlmacen.stream()
                    .filter(ta -> Boolean.TRUE.equals(ta.getActivo()))
                    .findFirst()
                    .orElse(null);
            almacen.setIdTipoAlmacen(primerActivo);
        }
        return almacen;
    }

    @PostConstruct
    public void init() {
        this.nombreBean = "Almacen";
        inicializarListas();
    }

    public void inicializarListas() {
        try {
            this.listaTipoAlmacen = tipoAlmacenDAO.findActiveTipoAlmacen();
            Logger.getLogger(AlmacenFrm.class.getName()).log(Level.INFO,
                    "Lista de tipos de almacén cargada: {0} elementos",
                    listaTipoAlmacen != null ? listaTipoAlmacen.size() : 0);
        } catch (Exception e) {
            Logger.getLogger(AlmacenFrm.class.getName()).log(Level.SEVERE, "Error al cargar tipos de almacén", e);
            listaTipoAlmacen = List.of();
        }
    }


    @Override
    protected String getIdAsText(Almacen dato) {
        if (dato != null && dato.getId() != null) {
            return dato.getId().toString();
        }
        return null;
    }

    @Override
    protected Almacen getIdByText(String id) {
        if (id != null && this.modelo != null && this.modelo.getWrappedData() != null
                && !this.modelo.getWrappedData().toString().isEmpty()) {
            try {
                Integer buscado = Integer.valueOf(id);
                return this.modelo.getWrappedData().stream()
                        .filter(almacen -> almacen.getId().equals(buscado))
                        .findFirst()
                        .orElse(null);
            } catch (Exception e) {
                Logger.getLogger(AlmacenFrm.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        return null;
    }

    @Override
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    @Override
    protected Almacen nuevoRegistro() {
        return createNewEntity();
    }

    @Override
    protected Almacen buscarRegistroPorId(Object id) {
        if (id != null && almacenDAO != null) {
            return almacenDAO.find(id);
        }
        return null;
    }


    public void btnModificarHandler(ActionEvent e) {
        try {
            if (this.registro != null) {
                if (this.registro.getIdTipoAlmacen() == null || !this.registro.getIdTipoAlmacen().getActivo()) {
                    facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Advertencia", "Debe seleccionar un tipo de almacén activo"));
                    return;
                }

                almacenDAO.modificar(this.registro);

                this.registro = null;
                this.estado = ESTADO_CRUD.NADA;
                inicializarListas();

                facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Éxito", "Almacén modificado correctamente"));
            }
        } catch (Exception ex) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "Hubo un problema al modificar el almacén"));
        }
    }

    public void btnEliminarHandler(ActionEvent e) {
        try {
            if (this.registro != null) {
                almacenDAO.eliminar(this.registro);

                this.registro = null;
                this.estado = ESTADO_CRUD.NADA;
                inicializarListas();

                facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Éxito", "Almacén eliminado correctamente"));
            }
        } catch (Exception ex) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "Hubo un problema al eliminar el almacén"));
        }
    }

    public void seleccionarRegistro(org.primefaces.event.SelectEvent<Almacen> event) {
        if (event != null && event.getObject() != null) {
            this.registro = event.getObject();
            this.estado = ESTADO_CRUD.MODIFICAR;
        }
    }

    @Override
    public LazyDataModel<Almacen> getModelo() {
        return super.getModelo();
    }


    public List<TipoAlmacen> getListaTipoAlmacen() {
        return listaTipoAlmacen;
    }

    public void setListaTipoAlmacen(List<TipoAlmacen> listaTipoAlmacen) {
        this.listaTipoAlmacen = listaTipoAlmacen;
    }


}
