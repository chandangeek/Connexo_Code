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
 * Time: 9:57:19
 */
public class DevicePhoneNumberSetupMessage extends AbstractMTU155Message {

    private static final CTRObjectID DEVICE_PHONE_NR_OBJECT_ID = new CTRObjectID("E.2.1");

    private static final int PHONE_NUMBER_MAX_LENGTH = 14;
    private static final int PHONE_NUMBER_REGISTER_LENGTH = 21;
    private static final String ALLOWED_CHARS = "1234567890+";

    public DevicePhoneNumberSetupMessage(MTU155MessageExecutor messageExecutor) {
        super(messageExecutor);
    }

    @Override
    public boolean canExecuteThisMessage(MessageEntry messageEntry) {
        return isMessageTag(RtuMessageConstant.CHANGE_DEVICE_PHONE_NUMBER, messageEntry.getContent());
    }

    @Override
    public void executeMessage(MessageEntry messageEntry) throws BusinessException {
        String phoneNumber = MessagingTools.getContentOfAttribute(messageEntry, RtuMessageConstant.DEVICE_PHONE_NUMBER);
        phoneNumber = phoneNumber == null ? "" : phoneNumber.trim();
        validateParameters(phoneNumber);
        phoneNumber = padRegisterValue(phoneNumber);
        writeDevicePhoneNumber(phoneNumber);
    }

    private void writeDevicePhoneNumber(String phoneNumber) throws BusinessException {
        ReferenceDate validityDate = new ReferenceDate().parse(new Date(), getFactory().getTimeZone());
        WriteDataBlock wdb = new WriteDataBlock((int) (100 * Math.random()));
        P_Session p_session = new P_Session(0x00);
        AttributeType attributeType = AttributeType.getValueOnly();
        attributeType.setHasIdentifier(true);
        byte[] rawData = ProtocolTools.concatByteArrays(DEVICE_PHONE_NR_OBJECT_ID.getBytes(), new byte[]{0x02}, phoneNumber.getBytes());

        try {
            CTRObjectFactory objectFactory = new CTRObjectFactory();
            AbstractCTRObject object = null;
            object = objectFactory.parse(rawData, 0, attributeType);
            getFactory().writeRegister(validityDate, wdb, p_session, attributeType, object);
        } catch (CTRException e) {
            throw new BusinessException(e);
        }
    }

    private void validateParameters(String phoneNumber) throws BusinessException {
        if (phoneNumber == null || "".equals(phoneNumber)) {
            throw new BusinessException("Device phone number is missing. DevicePhoneNumberSetupMessage needs a phone number. [phoneNumber='" + phoneNumber + "']");
        } else if (phoneNumber.length() > PHONE_NUMBER_MAX_LENGTH) {
            throw new BusinessException("Device phone number is too long. Max length is " + PHONE_NUMBER_MAX_LENGTH + ". [phoneNumber='" + phoneNumber + "']");
        } else if (containsInvalidChars(phoneNumber)) {
            throw new BusinessException("Device phone number contains invalid characters. The number can only contain [" + ALLOWED_CHARS + "]. [phoneNumber='" + phoneNumber + "']");
        }
    }

    /**
     * Checks if a given string contains a invalid characters
     *
     * @param phoneNumber
     * @return
     */
    private boolean containsInvalidChars(String phoneNumber) {
        for (int i = 0; i < phoneNumber.length(); i++) {
            if (!ALLOWED_CHARS.contains(phoneNumber.substring(i, i + 1))) {
                return true;
            }
        }
        return false;
    }

    private String padRegisterValue(String phoneNumber) {
        if (phoneNumber.length() < PHONE_NUMBER_REGISTER_LENGTH) {
            String number = phoneNumber;
            while (number.length() < PHONE_NUMBER_REGISTER_LENGTH) {
                number = number.concat(" ");
            }
            return number;
        } else if (phoneNumber.length() > PHONE_NUMBER_REGISTER_LENGTH) {
            return phoneNumber.substring(0, PHONE_NUMBER_REGISTER_LENGTH);
        } else {
            return phoneNumber;
        }
    }

}
