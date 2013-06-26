package com.energyict.protocolimplv2.elster.ctr.MTU155.messaging;

import com.energyict.cpo.Environment;
import com.energyict.mdc.messages.DeviceMessageStatus;
import com.energyict.mdc.meterdata.CollectedMessage;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AttributeType;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.AbstractCTRObject;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.CTRObjectFactory;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.P_Session;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.ReferenceDate;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.WriteDataBlock;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;

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

    public DevicePhoneNumberSetupMessage(Messaging messaging) {
        super(messaging);
    }

    @Override
    public boolean canExecuteThisMessage(OfflineDeviceMessage message) {
        return message.getDeviceMessageSpecPrimaryKey().getValue().equals(NetworkConnectivityMessage.CHANGE_DEVICE_PHONENUMBER.getPrimaryKey().getValue());
    }

    @Override
    public CollectedMessage executeMessage(OfflineDeviceMessage message) {
        CollectedMessage collectedMessage = createCollectedMessage(message);
        String phoneNumber = message.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue().trim();

        try {
            validatePhoneNumberSetupParameters(collectedMessage, phoneNumber);
            writeDevicePhoneNumber(phoneNumber);
            setSuccessfulDeviceMessageStatus(collectedMessage);
        } catch (CTRException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            String deviceMessageSpecName = Environment.getDefault().getTranslation(message.getDeviceMessageSpecPrimaryKey().getName());
            collectedMessage.setFailureInformation(ResultType.InCompatible, MdcManager.getIssueCollector().addProblem(message, "Messages.failed", deviceMessageSpecName, message.getDeviceMessageId(), e.getMessage()));
        }
        return collectedMessage;
    }

    private void validatePhoneNumberSetupParameters(CollectedMessage collectedMessage, String phoneNumber) throws CTRException {
        if (phoneNumber.length() > PHONE_NUMBER_MAX_LENGTH) {
            String msg = "Device phone number is too long. Max length is " + PHONE_NUMBER_MAX_LENGTH + ". [phoneNumber='" + phoneNumber + "']";
            collectedMessage.setDeviceProtocolInformation(msg);
            throw new CTRException(msg);
        } else if (containsInvalidChars(phoneNumber)) {
            String msg = "Device phone number contains invalid characters. The number can only contain [" + ALLOWED_CHARS + "]. [phoneNumber='" + phoneNumber + "']";
            collectedMessage.setDeviceProtocolInformation(msg);
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
