package com.energyict.genericprotocolimpl.elster.ctr.encryption;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Sms;
import com.energyict.genericprotocolimpl.elster.ctr.*;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRConnectionException;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CtrCipheringException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.SMSFrame;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.messaging.MessageService;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Connection class used to drop SMS messages on the Outbound SMS Queue.
 *
 * Copyrights EnergyICT
 * User: sva
 * Date: 8/03/12
 * Time: 15:02
 */
public class SecureSmsConnection implements CtrConnection<SMSFrame> {

    // Name of the Message Service, which is the message queue handling the sending of SMS messages to the broker
    private final String smsQueue;
    private final Logger logger;
    private CTREncryption ctrEncryption;

    private MessageService messageService;
     ArrayList<Sms> smsesReadyToSend = new ArrayList<Sms>();    // Backlog of smses still to be send out
    private String phoneNumber;

    /**
     * @param properties
     */
    public SecureSmsConnection(MTU155Properties properties, Logger logger, String phoneNumber) {
        this.smsQueue = properties.getSmsQueue();
        this.ctrEncryption = new CTREncryption(properties);
        this.logger = logger;
        this.phoneNumber = phoneNumber;
    }

    /**
     * Construct a proper SMS message out of the SMSFrame and drop it in the SMS Queue.
     *
     * @param frame
     * @return
     * @throws CTRConnectionException
     */
    public SMSFrame sendFrameGetResponse(SMSFrame frame) throws CTRConnectionException {
        try {
            SMSFrame encryptedFrame = (SMSFrame) ctrEncryption.encryptFrame(frame);
            encryptedFrame.setCrc();

            Sms sms = new Sms("", phoneNumber, new Date(), "", Integer.toString(OutboundSmsHandler.MESSAGE_ID), 0, encryptedFrame.getBytes());
            smsesReadyToSend.add(sms);
            return null;
        } catch (CtrCipheringException e) {
            throw new CTRConnectionException("An error occurred in the secure connection!", e);
        }
    }

    /**
     *  Post the pending smses to the queue.
     *
     *  This function is called after the execute of each device message.
     *  The pending smses - all for the same device message -  will be grouped and post onto the queue as one big message.
     *  This will allow to confirm the device message only when all individual messages are send out.
     */
    public void postPendingSmsToQueue() {
        postSmsesToQueue();
        smsesReadyToSend.clear();
    }

    public CTREncryption getCTREncryption() {
        return ctrEncryption;
    }

    /**
     * Get the active message service in EiServer and send the SMSES to the JMS Queue.
     *
     */
    private void postSmsesToQueue() {
        try {
            ObjectMessage objectMessage = getMessageService().createObjectMessage();
            objectMessage.setObject(smsesReadyToSend);
            getMessageService().send(objectMessage);
        } catch (BusinessException e) {
            logger.log(Level.SEVERE, e.getMessage());
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
        } catch (JMSException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
    }

    private MessageService getMessageService() throws BusinessException {
        if (messageService == null) {
            MeteringWarehouse mw = MeteringWarehouse.getCurrent();
            if (smsQueue != null) {
                MessageService service = mw.getMessageServiceFactory().find(smsQueue);
                if (service != null) {
                    messageService = service;
                } else {
                    String msg = "The Message Service " + smsQueue + " could not be found. The service is required for sending SMS messages.";
                    logger.log(Level.SEVERE, msg);
                    throw new BusinessException(msg);
                }
            } else {
                throw new BusinessException("Custom property smsQueue is a required property, but it isn't set.");
            }
        }
        return messageService;
    }
}