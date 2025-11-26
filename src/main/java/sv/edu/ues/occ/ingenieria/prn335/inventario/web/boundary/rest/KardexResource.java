package sv.edu.ues.occ.ingenieria.prn335.inventario.web.boundary.rest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.boundary.servlet.ReceptorKardexFrm;

import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
@Path("actualizar-kardex")
@Produces(MediaType.APPLICATION_JSON)
public class KardexResource {

    private static final Logger LOGGER = Logger.getLogger(KardexResource.class.getName());

    @Inject
    @Named("receptorKardexFrm")
    private ReceptorKardexFrm receptorKardexFrm;

    @GET
    public Response actualizarKardex() {
        try {
            LOGGER.log(Level.INFO, "📨 REST: Solicitando actualización de kardex");
            receptorKardexFrm.actualizarTabla(null);
            LOGGER.log(Level.INFO, "✅ REST: Kardex actualizado correctamente");
            return Response.ok("{\"status\":\"success\"}").build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en REST", e);
            return Response.status(500).entity("{\"status\":\"error\"}").build();
        }
    }
}
