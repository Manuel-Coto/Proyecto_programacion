package sv.edu.ues.occ.ingenieria.prn335.inventario.web.boundary.ws;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import java.io.Serializable;

@ApplicationScoped
@ServerEndpoint("/kardex")
public class KardexEndPoint implements Serializable {


    @Inject
    SessionHandler sessionHandler;

    @OnOpen
    public void abrirConexion(Session session) {
        sessionHandler.addSession(session);
    }

    @OnClose
    public void cerrarConexion(Session session) {
        sessionHandler.removeSession(session);
    }

    public void enviarMensajeBroadcast(String mensaje) {
        for (Session session : sessionHandler.getSessions()) {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(mensaje);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }




}
