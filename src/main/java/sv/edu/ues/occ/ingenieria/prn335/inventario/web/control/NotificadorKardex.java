package sv.edu.ues.occ.ingenieria.prn335.inventario.web.control;

import jakarta.annotation.Resource;
import jakarta.ejb.Local;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.jms.*;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
@LocalBean
public class NotificadorKardex implements Serializable {

    @Resource(lookup = "jms/JmsFactory")
    ConnectionFactory connectionFactory;

    @Resource(lookup = "jms/JmsQueue")
    Queue queue;

    public void notificarCambioKardex(String mensaje) {
        TextMessage textMessage;
        try {
            Connection cnx = connectionFactory.createConnection();
            Session session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer producer = session.createProducer(queue);
            textMessage = session.createTextMessage(mensaje+System.currentTimeMillis());
            producer.send(textMessage);
            session.close();
            cnx.close();

        } catch (Exception e) {
            Logger.getLogger(NotificadorKardex.class.getName()).log(Level.SEVERE, null, e);
        }
    }



}
