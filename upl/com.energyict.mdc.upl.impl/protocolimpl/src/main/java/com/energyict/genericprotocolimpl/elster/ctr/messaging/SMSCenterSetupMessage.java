package com.energyict.genericprotocolimpl.elster.ctr.messaging;

import com.energyict.cbo.BusinessException;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
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
 * Time: 9:57:19
 */
public class SMSCenterSetupMessage extends AbstractMTU155Message {

    private static final CTRObjectID SMSC_OBJECT_ID = new CTRObjectID("E.3.1");

    private static final int SMSC_NUMBER_MAX_LENGTH = 14;
    private static final int SMSC_NUMBER_REGISTER_LENGTH = 21;
    private static final String ALLOWED_CHARS = "1234567890+";

    public SMSCenterSetupMessage(MTU155MessageExecutor messageExecutor) {
        super(messageExecutor);
    }

    @Override
    public boolean canExecuteThisMessage(MessageEntry messageEntry) {
        return isMessageTag(RtuMessageConstant.SMS_CHANGE_SMSC, messageEntry.getContent());
    }

    @Override
    public void executeMessage(MessageEntry messageEntry) throws BusinessException {
        String smscNumber = MessagingTools.getContentOfAttribute(messageEntry, RtuMessageConstant.SMS_SMSC_NUMBER);
        smscNumber = smscNumber == null ? "" : smscNumber.trim();
        validateParameters(smscNumber);
        smscNumber = padRegisterValue(smscNumber);
        writeSMSCNumber(smscNumber);
    }

    private void writeSMSCNumber(String smscNumber) throws BusinessException {
        ReferenceDate validityDate = new ReferenceDate().parse(new Date(), getFactory().getTimeZone());
        WriteDataBlock wdb = new WriteDataBlock((int) (100 * Math.random()));
        P_Session p_session = new P_Session(0x00);
        AttributeType attributeType = AttributeType.getValueOnly();
        attributeType.setHasIdentifier(true);
        byte[] rawData = ProtocolTools.concatByteArrays(SMSC_OBJECT_ID.getBytes(), new byte[]{0x02}, smscNumber.getBytes());

        try {
            CTRObjectFactory objectFactory = new CTRObjectFactory();
            AbstractCTRObject object = null;
            object = objectFactory.parse(rawData, 0, attributeType);
            getFactory().writeRegister(validityDate, wdb, p_session, attributeType, object);
        } catch (CTRException e) {
            throw new BusinessException(e);
        }
    }

    private void validateParameters(String smscNumber) throws BusinessException {
        if (smscNumber == null || "".equals(smscNumber)) {
            throw new BusinessException("SMS center number is missing. SMSCenterSetupMessage needs a SMSC number. [smscNumber='" + smscNumber + "']");
        } else if (smscNumber.length() > SMSC_NUMBER_MAX_LENGTH) {
            throw new BusinessException("SMS center number is too long. Max length is " + SMSC_NUMBER_MAX_LENGTH + ". [smscNumber='" + smscNumber + "']");
        } else if (containsInvalidChars(smscNumber)) {
            throw new BusinessException("SMS center number contains invalid characters. The number can only contain [" + ALLOWED_CHARS + "]. [smscNumber='" + smscNumber + "']");
        }
    }

    /**
     * Checks if a given string contains a invalid characters
     *
     * @param smscNumber
     * @return
     */
    private boolean containsInvalidChars(String smscNumber) {
        for (int i = 0; i < smscNumber.length(); i++) {
            if (!ALLOWED_CHARS.contains(smscNumber.substring(i, i + 1))) {
                return true;
            }
        }
        return false;
    }

    private String padRegisterValue(String smscNumber) {
        if (smscNumber.length() < SMSC_NUMBER_REGISTER_LENGTH) {
            String number = smscNumber;
            while (number.length() < SMSC_NUMBER_REGISTER_LENGTH) {
                number = number.concat(" ");
            }
            return number;
        } else if (smscNumber.length() > SMSC_NUMBER_REGISTER_LENGTH) {
            return smscNumber.substring(0, SMSC_NUMBER_REGISTER_LENGTH);
        } else {
            return smscNumber;
        }
    }

}
