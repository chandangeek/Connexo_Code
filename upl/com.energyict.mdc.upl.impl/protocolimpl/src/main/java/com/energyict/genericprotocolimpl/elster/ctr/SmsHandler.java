package com.energyict.genericprotocolimpl.elster.ctr;

import com.energyict.cbo.BusinessException;
import com.energyict.mdw.messaging.MessageHandler;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;


/**
 * Created by IntelliJ IDEA.
 * User: khe
 * Date: 20-sep-2010
 * Time: 9:58:20
 * To change this template use File | Settings | File Templates.
 */
public class SmsHandler implements MessageHandler {

    public void processMessage(Message message, Logger logger) throws JMSException, BusinessException, SQLException {

        ObjectMessage om = (ObjectMessage) message;
/*
        Sms sms1 = (Sms) om.getObject();
        System.out.println(sms1.toString());
*/
    }

    public String getVersion() {
        return "1.0";  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addProperties(Properties properties) {

    }

    public List getRequiredKeys() {
        return new ArrayList();
    }

    public List getOptionalKeys() {
        return new ArrayList();
    }
}