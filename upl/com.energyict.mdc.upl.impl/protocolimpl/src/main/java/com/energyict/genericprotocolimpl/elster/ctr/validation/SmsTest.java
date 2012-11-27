package com.energyict.genericprotocolimpl.elster.ctr.validation;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Sms;
import com.energyict.cpo.*;
import com.energyict.genericprotocolimpl.elster.ctr.SmsHandler;
import com.energyict.mdw.amr.RtuRegister;
import com.energyict.mdw.amr.RtuRegisterReading;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.messaging.MessageService;
import com.energyict.mdw.shadow.DeviceShadow;
import com.energyict.obis.ObisCode;
import com.energyict.oracle.JmsUtils;
import com.energyict.protocolimpl.utils.ProtocolTools;
import oracle.jms.AQjmsSession;

import javax.jms.*;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;

/**
 * Copyrights
 * Date: 24/05/11
 * Time: 15:30
 */
public class SmsTest {

    public static void main(String[] args) throws BusinessException, SQLException {
        MeteringWarehouse.createBatchContext();
        MeteringWarehouse mw = MeteringWarehouse.getCurrent();

        backupMessages();

    }

    private static void backupMessages() {

        Transaction t = new Transaction() {

            public Object doExecute() throws BusinessException, SQLException {
                try {
                    MessageService smsQueue_backup = MeteringWarehouse.getCurrent().getMessageServiceFactory().find("smsQueue_Backup");
                    JmsSessionContext sessionContext = Environment.getDefault().getJmsSessionContext();
                    AQjmsSession session = sessionContext.getSession();
                    Queue smsQueue = session.getQueue(JmsUtils.getDbUser(), "smsQueue");

                    QueueBrowser browser = session.createBrowser(smsQueue);
                    Enumeration browserEnumeration = browser.getEnumeration();
                    int count = 0;
                    while (browserEnumeration.hasMoreElements()) {
                        ObjectMessage message = (ObjectMessage) browserEnumeration.nextElement();
                        Sms sms = (Sms) message.getObject();
                        int length = sms.getMessage().length;
                        System.out.println(sms.getFrom() + ", " + length + ", " + ++count + ", " + ProtocolTools.getHexStringFromBytes(sms.getMessage()));
                        if (length == 140) {
                            try {
                                smsQueue_backup.send(message);
                            } catch (BusinessException e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            } catch (SQLException e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }
                        }
                    }
                    browser.close();
                } catch (BusinessException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        try {
            MeteringWarehouse.getCurrent().executeNested(t);
        } catch (BusinessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }

    private static void updatePhoneNumbers(MeteringWarehouse mw) {
        List<Device> all = mw.getDeviceFactory().findAll();
        for (Device rtu : all) {
            String phoneNumber = rtu.getPhoneNumber();
            if (phoneNumber == null || phoneNumber.equalsIgnoreCase("")) {
                RtuRegister register = rtu.getRegister(ObisCode.fromString("0.0.96.12.6.255"));
                RtuRegisterReading lastReading = register.getLastReading();
                if (lastReading != null) {
                    String text = lastReading.getText();
                    if (text != null) {
                        try {
                            DeviceShadow shadow = rtu.getShadow();
                            shadow.setPhoneNumber(text.trim());
                            rtu.update(shadow);
                            System.out.println(text);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } catch (BusinessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private static void doSmsStuff() throws SQLException {
        MessageService smsQueue = MeteringWarehouse.getCurrent().getMessageServiceFactory().find("smsQueue");

        for (int i = 0; i < 100; i++) {
            try {
                Message message = smsQueue.receive();
                Sms sms = getSmsFromMessage(message);
                int length = sms.getMessage().length;
                System.out.println(sms.getFrom() + ", " + length + ", " + sms.getBits() + ", " + ProtocolTools.getHexStringFromBytes(sms.getMessage()));
                if (length == 140) {
                    try {
                        SmsHandler handler = new SmsHandler();
                        handler.processMessage(message, Logger.getLogger(SmsTest.class.getName()));
                    } catch (JMSException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            } catch (BusinessException e) {
                e.printStackTrace();
            }
        }
    }

    private static Sms getSmsFromMessage(Message message) throws BusinessException {
        try {
            if (message instanceof ObjectMessage) {
                ObjectMessage msg = (ObjectMessage) message;
                Serializable object = msg.getObject();
                if (object instanceof Sms) {
                    return (Sms) object;
                }
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
        throw new BusinessException("No sms found in message");
    }

}
