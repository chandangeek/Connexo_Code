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
public class SMSCenterSetupMessage extends AbstractMTU155Message {

    protected static final int VALUE_LENGTH = 24;
    protected static final int SMSC_NUMBER_MAX_LENGTH = 14;
    protected static final String ALLOWED_CHARS = "1234567890+";

    protected CTRObjectID smsc_Object_ID = new CTRObjectID("E.3.1");

    public SMSCenterSetupMessage(Messaging messaging) {
        super(messaging);
    }

    @Override
    public boolean canExecuteThisMessage(OfflineDeviceMessage message) {
        return message.getDeviceMessageSpecPrimaryKey().getValue().equals(NetworkConnectivityMessage.CHANGE_SMS_CENTER_NUMBER.getPrimaryKey().getValue());
    }

    @Override
    public CollectedMessage executeMessage(OfflineDeviceMessage message) {
        CollectedMessage collectedMessage = createCollectedMessage(message);
        String smscNumber = message.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue().trim();
        try {
            validateParameters(collectedMessage, smscNumber);
            writeSMSCNumber(smscNumber);
            setSuccessfulDeviceMessageStatus(collectedMessage);
        } catch (CTRException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            String deviceMessageSpecName = Environment.getDefault().getTranslation(message.getDeviceMessageSpecPrimaryKey().getName());
            collectedMessage.setFailureInformation(ResultType.InCompatible, MdcManager.getIssueCollector().addProblem(message, "Messages.failed", deviceMessageSpecName, message.getDeviceMessageId(), e.getMessage()));
        }
        return collectedMessage;
    }

    private void validateParameters(CollectedMessage collectedMessage, String smscNumber) throws CTRException {
        if (smscNumber.length() > SMSC_NUMBER_MAX_LENGTH) {
            String msg = "SMS center number is too long. Max length is " + SMSC_NUMBER_MAX_LENGTH + ". [smscNumber='" + smscNumber + "']";
            collectedMessage.setDeviceProtocolInformation(msg);
            throw new CTRException(msg);
        } else if (containsInvalidChars(smscNumber)) {
            String msg = "SMS center number contains invalid characters. The number can only contain [" + ALLOWED_CHARS + "]. [smscNumber='" + smscNumber + "']";
            collectedMessage.setDeviceProtocolInformation(msg);
            throw new CTRException(msg);
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

    protected void writeSMSCNumber(String smscNumber) throws CTRException {
        ReferenceDate validityDate = new ReferenceDate().parse(new Date(), getFactory().getTimeZone());
        WriteDataBlock wdb = new WriteDataBlock((int) (100 * Math.random()));
        P_Session p_session = new P_Session(0x00);
        AttributeType attributeType = AttributeType.getValueOnly();
        attributeType.setHasIdentifier(true);
        byte[] rawData = getObjectBytes(smscNumber);

        CTRObjectFactory objectFactory = new CTRObjectFactory();
        AbstractCTRObject object = null;
        object = objectFactory.parse(rawData, 0, attributeType);
        getFactory().writeRegister(validityDate, wdb, p_session, attributeType, object);
    }

    protected byte[] getObjectBytes(String smscNumber) {
        byte[] rawData = smsc_Object_ID.getBytes();
        rawData = ProtocolTools.concatByteArrays(rawData, new byte[]{(byte) 0x02});
        rawData = ProtocolTools.concatByteArrays(rawData, smscNumber.getBytes());
        rawData = padData(rawData, VALUE_LENGTH);
        return rawData;
    }
}
