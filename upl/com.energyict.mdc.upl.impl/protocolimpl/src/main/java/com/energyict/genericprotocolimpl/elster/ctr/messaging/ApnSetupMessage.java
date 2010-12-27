package com.energyict.genericprotocolimpl.elster.ctr.messaging;

import com.energyict.cbo.BusinessException;
import com.energyict.genericprotocolimpl.common.messages.RtuMessageConstant;
import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException;
import com.energyict.genericprotocolimpl.elster.ctr.object.AbstractCTRObject;
import com.energyict.genericprotocolimpl.elster.ctr.object.CTRObjectFactory;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRObjectID;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.*;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocolimpl.utils.MessagingTools;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 27-dec-2010
 * Time: 9:27:15
 */
public class ApnSetupMessage extends AbstractMTU155Message {

    public static final int VALUE_LENGTH = 114;
    public static final int APN_MAX_LENGTH = 40;
    public static final int PASS_MAX_LENGTH = 30;
    public static final int USER_MAX_LENGTH = 30;


    public ApnSetupMessage(MTU155MessageExecutor messageExecutor) {
        super(messageExecutor);
    }

    @Override
    public boolean canExecuteThisMessage(MessageEntry messageEntry) {
        return isMessageTag(RtuMessageConstant.GPRS_MODEM_SETUP, messageEntry.getContent());
    }

    /**
     * Send the message (containing the apn configuration) to the meter.
     *
     * @param messageEntry: the message containing the apn configuration
     * @throws com.energyict.cbo.BusinessException:
     *          when a parameter is null or too long
     */
    @Override
    public void executeMessage(MessageEntry messageEntry) throws BusinessException {
        String apn = MessagingTools.getContentOfAttribute(messageEntry, RtuMessageConstant.GPRS_APN);
        String user = MessagingTools.getContentOfAttribute(messageEntry, RtuMessageConstant.GPRS_USERNAME);
        String pssw = MessagingTools.getContentOfAttribute(messageEntry, RtuMessageConstant.GPRS_PASSWORD);
        validateApnSetupMessage(apn, user, pssw);

        ReferenceDate validityDate = new ReferenceDate().parse(new Date(), getFactory().getTimeZone());
        WriteDataBlock wdb = new WriteDataBlock((int) (100 * Math.random()));
        P_Session p_session = new P_Session(0x00);
        AttributeType attributeType = AttributeType.getValueOnly();
        attributeType.setHasIdentifier(true);
        byte[] rawData = getObjectBytes(apn, user, pssw);

        try {
            CTRObjectFactory objectFactory = new CTRObjectFactory();
            AbstractCTRObject object = null;
            object = objectFactory.parse(rawData, 0, attributeType);
            getFactory().writeRegister(validityDate, wdb, p_session, attributeType, object);
        } catch (CTRException e) {
            throw new BusinessException(e);
        }
    }


    private void validateApnSetupMessage(String apn, String user, String pssw) throws BusinessException {
        if ("".equals(apn) || apn == null) {
            throw new BusinessException("Parameter APN was 'null'.");
        } else if ("".equals(pssw) || pssw == null) {
            throw new BusinessException("Parameter password was 'null'.");
        } else if ("".equals(user) || user == null) {
            throw new BusinessException("Parameter username was 'null'.");
        } else if (apn.length() > APN_MAX_LENGTH) {
            throw new BusinessException("Parameter APN exceeded the maximum length (40 characters).");
        } else if (user.length() > USER_MAX_LENGTH) {
            throw new BusinessException("Parameter username exceeded the maximum length (30 characters).");
        } else if (pssw.length() > PASS_MAX_LENGTH) {
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

    private byte[] padData(byte[] fieldData) {
        int paddingLength = VALUE_LENGTH - fieldData.length;
        if (paddingLength > 0) {
            fieldData = ProtocolTools.concatByteArrays(fieldData, new byte[paddingLength]);
        } else if (paddingLength < 0) {
            fieldData = ProtocolTools.getSubArray(fieldData, 0, VALUE_LENGTH);
        }
        return fieldData;
    }

}
