package sv.edu.ues.occ.ingenieria.prn335.inventario.web.control;

import jakarta.ejb.MessageDriven;
import jakarta.inject.Inject;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.TextMessage;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.boundary.ws.KardexEndPoint;

import java.util.logging.Level;
import java.util.logging.Logger;

@MessageDriven(activationConfig = {
        @jakarta.ejb.ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "jms/JmsQueue"),
        @jakarta.ejb.ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Queue"),
        @jakarta.ejb.ActivationConfigProperty(propertyName = "connectionFactoryLookup", propertyValue = "JmsFactory")
})

public class ReceptorKardex implements MessageListener {

    @Inject
    KardexEndPoint kardexEndPoint;

    @Override
    public void onMessage(Message message) {
        TextMessage textMessage = (TextMessage) message;
        try {
            System.out.println("Mensaje recibido en ReceptorKardex: " + textMessage.getText());
            kardexEndPoint.enviarMensajeBroadcast(textMessage.getText());
        } catch (Exception e) {
            Logger.getLogger(ReceptorKardex.class.getName()).log(Level.SEVERE, null, e);
        }
    }


}
