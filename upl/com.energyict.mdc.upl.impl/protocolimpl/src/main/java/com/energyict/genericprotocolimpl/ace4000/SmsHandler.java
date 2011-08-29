package com.energyict.genericprotocolimpl.ace4000;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Sms;
import com.energyict.mdw.messaging.MessageHandler;

import javax.jms.*;
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
    public static final String METER_TYPE = "MeterType";

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
        ACE4000 ace4000 = new ACE4000();
        ace4000.doExecuteSms((Sms) om.getObject());
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
        optional.add(METER_TYPE);
        return optional;
    }

    public List<String> getRequiredKeys() {
        List<String> required = new ArrayList<String>();
        return required;
    }
}