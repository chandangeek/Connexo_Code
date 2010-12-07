package com.energyict.genericprotocolimpl.elster.ctr.messaging;

import com.energyict.cbo.BusinessException;
import com.energyict.genericprotocolimpl.common.GenericMessageExecutor;
import com.energyict.genericprotocolimpl.common.messages.RtuMessageConstant;
import com.energyict.genericprotocolimpl.elster.ctr.GprsRequestFactory;
import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;
import com.energyict.genericprotocolimpl.elster.ctr.object.AbstractCTRObject;
import com.energyict.genericprotocolimpl.elster.ctr.object.CTRObjectFactory;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRObjectID;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.*;
import com.energyict.mdw.core.RtuMessage;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocolimpl.utils.MessagingTools;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.sql.SQLException;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 3-dec-2010
 * Time: 14:41:00
 */
public class MTU155MessageExecutor extends GenericMessageExecutor {

    private Logger logger;
    private GprsRequestFactory factory;
    public static final int VALUE_LENGTH = 114;
    public static final int APN_MAX_LENGTH = 40;
    public static final int PASS_MAX_LENGTH = 30;
    public static final int USER_MAX_LENGTH = 30;

    public MTU155MessageExecutor(Logger logger, GprsRequestFactory factory) {
        this.factory = factory;
        this.logger = logger;
    }

    @Override
    public void doMessage(RtuMessage rtuMessage) throws BusinessException, SQLException {
        boolean success = false;
        try {
            String content = rtuMessage.getContents();
            String trackingId = rtuMessage.getTrackingId();
            MessageEntry messageEntry = new MessageEntry(content, trackingId);

            if (isMessageTag(RtuMessageConstant.GPRS_MODEM_SETUP, content)) {
                doApnSetup(messageEntry);
                success = true;
            } else {
                throw new BusinessException("Received unknown message: " + rtuMessage.toString());
            }
        } finally {
            if (success) {
                rtuMessage.confirm();
                getLogger().info("Message " + rtuMessage.displayString() + " has finished successfully.");
            } else {
                rtuMessage.setFailed();
                getLogger().info("Message " + rtuMessage.displayString() + " has finished unsuccessfully.");
            }
        }
    }

    /**
     * Send the message (containing the apn configuration) to the meter.
     * @param messageEntry: the message containing the apn configuration
     * @throws BusinessException: when a parameter is null or too long
     */
    private void doApnSetup(MessageEntry messageEntry) throws BusinessException {
        String apn = MessagingTools.getContentOfAttribute(messageEntry, RtuMessageConstant.GPRS_APN);
        String user = MessagingTools.getContentOfAttribute(messageEntry, RtuMessageConstant.GPRS_USERNAME);
        String pssw = MessagingTools.getContentOfAttribute(messageEntry, RtuMessageConstant.GPRS_PASSWORD);

        checkParameters(apn, user, pssw);
        ReferenceDate validityDate = new ReferenceDate().parse(new Date(), factory.getTimeZone());
        WriteDataBlock wdb = new WriteDataBlock((int) (100 * Math.random()));
        P_Session p_session = new P_Session(0x00);
        AttributeType attributeType = AttributeType.getValueOnly();
        attributeType.setHasIdentifier(true);
        byte[] rawData = getObjectBytes(apn, user, pssw);

        try {
            CTRObjectFactory objectFactory = new CTRObjectFactory();
            AbstractCTRObject object = objectFactory.parse(rawData, 0, attributeType);
            factory.writeRegister(
                    validityDate,
                    wdb,
                    p_session,
                    attributeType,
                    object
            );
        } catch (CTRParsingException e) {
            throw new BusinessException(e.getMessage());
        } catch (CTRException e) {
            throw new BusinessException(e.getMessage());
        }
    }

    private void checkParameters(String apn, String user, String pssw) throws BusinessException {
        if ("".equals(apn) || apn == null) {
            throw new BusinessException("Parameter APN was 'null'.");
        }
        if ("".equals(pssw) || pssw == null) {
            throw new BusinessException("Parameter password was 'null'.");
        }
        if ("".equals(user) || user == null) {
            throw new BusinessException("Parameter username was 'null'.");
        }
        if (apn.length() > APN_MAX_LENGTH) {
            throw new BusinessException("Parameter APN exceeded the maximum length (40 characters).");
        }
        if (user.length() > USER_MAX_LENGTH) {
            throw new BusinessException("Parameter username exceeded the maximum length (30 characters).");
        }
        if (pssw.length() > PASS_MAX_LENGTH) {
            throw new BusinessException("Parameter password exceeded the maximum length (30 characters).");
        }
    }

    private byte[] getObjectBytes(String apn, String user, String pssw) {
        byte[] rawData = new CTRObjectID("E.E.1").getBytes();
        rawData = ProtocolTools.concatByteArrays(rawData, new byte[1]);
        rawData = ProtocolTools.concatByteArrays(rawData, apn.getBytes());
        rawData = ProtocolTools.concatByteArrays(rawData, new byte[1]);
        rawData = ProtocolTools.concatByteArrays(rawData, user.getBytes());
        rawData = ProtocolTools.concatByteArrays(rawData, new byte[1]);
        rawData = ProtocolTools.concatByteArrays(rawData, pssw.getBytes());
        rawData = padData(rawData);
        return rawData;
    }

    protected boolean isMessageTag(String tag, String content) {
        return (content.indexOf("<" + tag) >= 0);
    }

    private byte[] padData(byte[] fieldData) {
        int paddingLength = VALUE_LENGTH - fieldData.length;
        if (paddingLength > 0) {
            fieldData = ProtocolTools.concatByteArrays(fieldData, new byte[paddingLength]);
        } else if (paddingLength < 0) {
            fieldData = ProtocolTools.getSubArray(fieldData, 0, VALUE_LENGTH);
        }
        return fieldData;
    }

    @Override
    protected TimeZone getTimeZone() {
        return getFactory().getTimeZone();
    }

    public GprsRequestFactory getFactory() {
        return factory;
    }

    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(getClass().getName());
        }
        return logger;
    }
}
