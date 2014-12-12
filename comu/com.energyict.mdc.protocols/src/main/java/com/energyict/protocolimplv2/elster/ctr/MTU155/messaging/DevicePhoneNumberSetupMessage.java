package com.energyict.protocolimplv2.elster.ctr.MTU155.messaging;

import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.data.CollectedMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AttributeType;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.AbstractCTRObject;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.CTRObjectFactory;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.P_Session;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.ReferenceDate;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.WriteDataBlock;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;


import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 27-dec-2010
 * Time: 9:57:19
 */
public class DevicePhoneNumberSetupMessage extends AbstractMTU155Message {

    private static final CTRObjectID DEVICE_PHONE_NR_OBJECT_ID = new CTRObjectID("E.2.1");
    private static final int VALUE_LENGTH = 24;

    private static final int PHONE_NUMBER_MAX_LENGTH = 14;
    private static final String ALLOWED_CHARS = "1234567890+";

    public DevicePhoneNumberSetupMessage(Messaging messaging, IssueService issueService) {
        super(messaging, issueService);
    }

    @Override
    public boolean canExecuteThisMessage(OfflineDeviceMessage message) {
        return message.getDeviceMessageId().equals(DeviceMessageId.NETWORK_CONNECTIVITY_CHANGE_DEVICE_PHONENUMBER);
    }

    @Override
    protected CollectedMessage doExecuteMessage(OfflineDeviceMessage message) throws CTRException {
        String phoneNumber = getDeviceMessageAttribute(message, DeviceMessageConstants.devicePhoneNumberAttributeName).getDeviceMessageAttributeValue();

        validatePhoneNumberSetupParameters(phoneNumber);
        writeDevicePhoneNumber(phoneNumber);
        return null;
    }

    private void validatePhoneNumberSetupParameters(String phoneNumber) throws CTRException {
        if (phoneNumber.length() > PHONE_NUMBER_MAX_LENGTH) {
            String msg = "Device phone number is too long. Max length is " + PHONE_NUMBER_MAX_LENGTH + ". [phoneNumber='" + phoneNumber + "']";
            throw new CTRException(msg);
        } else if (containsInvalidChars(phoneNumber)) {
            String msg = "Device phone number contains invalid characters. The number can only contain [" + ALLOWED_CHARS + "]. [phoneNumber='" + phoneNumber + "']";
            throw new CTRException(msg);
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

    private void writeDevicePhoneNumber(String phoneNumber) throws CTRException {
        ReferenceDate validityDate = new ReferenceDate().parse(new Date(), getFactory().getTimeZone());
        WriteDataBlock wdb = new WriteDataBlock((int) (100 * Math.random()));
        P_Session p_session = new P_Session(0x00);
        AttributeType attributeType = AttributeType.getValueOnly();
        attributeType.setHasIdentifier(true);
        byte[] rawData = getObjectBytes(phoneNumber);

        CTRObjectFactory objectFactory = new CTRObjectFactory();
        AbstractCTRObject object = objectFactory.parse(rawData, 0, attributeType);
        getFactory().writeRegister(validityDate, wdb, p_session, attributeType, object);
    }

     private byte[] getObjectBytes(String phoneNumber) {
        byte[] rawData = DEVICE_PHONE_NR_OBJECT_ID.getBytes();
         rawData = ProtocolTools.concatByteArrays(rawData, new byte[]{0x02});
        rawData = ProtocolTools.concatByteArrays(rawData, phoneNumber.getBytes());
        rawData = padData(rawData, VALUE_LENGTH);
        return rawData;
    }
}
