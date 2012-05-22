package com.energyict.genericprotocolimpl.ace4000;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Sms;
import com.energyict.cpo.*;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.MeteringWarehouseFactory;
import com.energyict.mdw.imp.*;
import com.energyict.mdw.messaging.MessageHandler;
import com.energyict.mdw.shadow.imp.ConsumptionRequestShadow;
import com.energyict.protocolimpl.utils.ProtocolTools;
import org.apache.axis.encoding.Base64;

import javax.jms.*;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 25/08/11
 * Time: 17:27
 */
public class SmsHandler implements MessageHandler {


    public static final String TIMEOUT = "Timeout";
    public static final String RETRIES = "Retries";

    /**
     * Processes a given message containing an sms object
     *
     * @param message: the given message
     * @param logger:  the logger
     * @throws JMSException
     * @throws BusinessException
     * @throws SQLException
     */
    public void processMessage(Message message, Logger logger) throws JMSException, BusinessException, SQLException {
        ObjectMessage om = (ObjectMessage) message;
        Sms sms = (Sms) om.getObject();
        try {
            processMessage(sms.getText(), sms.getFrom());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new BusinessException(e);
        }
    }

    /**
     * Check the database for relevant fragments if indicated that the sms is a fragment of a message.
     * Concatenate and parse if all fragments are found.
     */
    public static void processMessage(String msg, String from) throws JMSException, BusinessException, SQLException {
        msg = msg.substring(1, msg.length() - 4);     //Ignore the first character (#) and the trailing CRC
        int header = ProtocolTools.getUnsignedIntFromBytes(Base64.decode(msg.substring(0, 4)));
        int nr = header >> 18;                              //first 6 bits
        int parts = (header & 0x03FFFF) >> 12;              //second 6 bits
        int trackingId = header & 0x000FFF;
        String externalName = from + String.valueOf(trackingId);
        String xml = msg.substring(4);

        if (parts == 0) {
            ACE4000 ace4000 = new ACE4000();
            ace4000.doExecuteSms(xml);
        } else {
            Map<Integer, String> fragments = new HashMap<Integer, String>();

            new MeteringWarehouseFactory().getBatch();
            ConsumptionRequestFactory factory = MeteringWarehouse.getCurrent().getConsumptionRequestFactory();
            ConsumptionRequestLoggingFilter filter = new ConsumptionRequestLoggingFilter();
            filter.setExternalNameMask(externalName);
            List<ConsumptionRequest> consumptionRequests = factory.findByFilter(filter);
            for (ConsumptionRequest consumptionRequest : consumptionRequests) {
                fragments.put(consumptionRequest.getState(), consumptionRequest.getRequest());
            }
            fragments.put(nr, xml);
            if (fragments.size() == (parts + 1)) {
                for (ConsumptionRequest consumptionRequest : consumptionRequests) {
                    consumptionRequest.delete();
                }

                xml = "";
                Map<Integer, String> sorted = new TreeMap<Integer, String>(fragments);
                for (String xmlPart : sorted.values()) {
                    xml += xmlPart;
                }
                ACE4000 ace4000 = new ACE4000();
                ace4000.doExecuteSms(xml);
            } else {
                ConsumptionRequestShadow shadow = new ConsumptionRequestShadow();
                shadow.setExternalName(externalName);
                shadow.setRequest(xml);
                shadow.setCompletionCode(parts);
                shadow.setState(nr);
                factory.create(shadow);
            }
        }
    }

    public String getVersion() {
        return "$Date$";
    }


    public void addProperties(Properties properties) {

    }

    public List<String> getOptionalKeys() {
        List<String> optional = new ArrayList<String>();
        optional.add(TIMEOUT);
        optional.add(RETRIES);
        return optional;
    }

    public List<String> getRequiredKeys() {
        List<String> required = new ArrayList<String>();
        return required;
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return PropertySpecFactory.toPropertySpecs(getRequiredKeys());
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return PropertySpecFactory.toPropertySpecs(getOptionalKeys());
    }

    @Override
    public void addProperties(TypedProperties properties) {
        addProperties(properties.toStringProperties());
    }
}