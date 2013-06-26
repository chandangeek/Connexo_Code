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
import com.energyict.protocolimplv2.elster.ctr.MTU155.info.MeterType;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.AbstractCTRObject;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.CTRObjectFactory;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;

/**
 * Copyrights EnergyICT
 * Date: 2/03/11
 * Time: 16:40
 */
public class WriteMeterMasterDataMessage extends AbstractMTU155Message {

    private static final String OBJECT_ID = "C.2.0";

    private static final int TYPE_LENGTH = 4;
    private static final int CALIBER_LENGTH = 3;
    private static final int SERIAL_MAX_LENGTH = 13;
    private static final int MAX_CALIBER = 999999;

    public WriteMeterMasterDataMessage(Messaging messaging) {
        super(messaging);
    }

    @Override
    public boolean canExecuteThisMessage(OfflineDeviceMessage message) {
        return message.getDeviceMessageSpecPrimaryKey().getValue().equals(ConfigurationChangeDeviceMessage.ConfigureGasMeterMasterData.getPrimaryKey().getValue());
    }

    @Override
    public CollectedMessage executeMessage(OfflineDeviceMessage message) {
        CollectedMessage collectedMessage = createCollectedMessage(message);
        String meterTypeString = message.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue().trim();
        String meterCaliberString = message.getDeviceMessageAttributes().get(1).getDeviceMessageAttributeValue().trim();
        String meterSerialNumber = message.getDeviceMessageAttributes().get(2).getDeviceMessageAttributeValue().trim();

        try {
            MeterType meterType = MeterType.fromString(meterTypeString);
            int meterCaliber = validateAndGetMeterCaliber(collectedMessage, meterCaliberString);
            validateMeterSerialnumber(collectedMessage, meterSerialNumber);

            writeMeterMasterData(meterType, meterCaliber, meterSerialNumber);
            setSuccessfulDeviceMessageStatus(collectedMessage);
        } catch (CTRException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            String deviceMessageSpecName = Environment.getDefault().getTranslation(message.getDeviceMessageSpecPrimaryKey().getName());
            collectedMessage.setFailureInformation(ResultType.InCompatible, MdcManager.getIssueCollector().addProblem(message, "Messages.failed", deviceMessageSpecName, message.getDeviceMessageId(), e.getMessage()));
        }
        return collectedMessage;
    }

    private int validateAndGetMeterCaliber(CollectedMessage collectedMessage, String meterCaliberAttr) throws CTRException {
        if (!ProtocolTools.isNumber(meterCaliberAttr)) {
            String msg = "Meter caliber should only contain digits (0-9), but [" + meterCaliberAttr + "] contains other characters.";
            collectedMessage.setDeviceProtocolInformation(msg);
            throw new CTRException(msg);
        }
        try {
            int caliber = Integer.valueOf(meterCaliberAttr);
            if (caliber > MAX_CALIBER) {
                String msg = "Meter caliber has a max value of [" + MAX_CALIBER + "] but was [" + caliber + "]";
                collectedMessage.setDeviceProtocolInformation(msg);
                throw new CTRException(msg);
            }
            return caliber;
        } catch (NumberFormatException e) {
            String msg = "Invalid caliber [" + meterCaliberAttr + "], " + e.getMessage();
            collectedMessage.setDeviceProtocolInformation(msg);
            throw new CTRException(msg);
        }
    }

    private void validateMeterSerialnumber(CollectedMessage collectedMessage, String meterSerialAttr) throws CTRException {
        if (meterSerialAttr.length() > SERIAL_MAX_LENGTH) {
            String msg = "Serial max length is [" + SERIAL_MAX_LENGTH + "] characters, but [" + meterSerialAttr + "] has [" + meterSerialAttr.length() + "] characters.";
            collectedMessage.setDeviceProtocolInformation(msg);
            throw new CTRException(msg);
        }
    }

    private void writeMeterMasterData(MeterType meterType, int caliber, String serial) throws CTRException {
        byte[] rawType = meterType.name().getBytes();
        rawType = ProtocolTools.concatByteArrays(rawType, new byte[TYPE_LENGTH - rawType.length]);

        byte[] rawCaliber = new byte[CALIBER_LENGTH];
        rawCaliber[2] = (byte) (caliber & 0x0FF);
        rawCaliber[1] = (byte) ((caliber >> 8) & 0x0FF);
        rawCaliber[0] = (byte) ((caliber >> 16) & 0x0FF);

        byte[] rawSerial = serial.getBytes();
        rawSerial = ProtocolTools.concatByteArrays(rawSerial, new byte[SERIAL_MAX_LENGTH - rawSerial.length]);

        byte[] rawData = new CTRObjectID(OBJECT_ID).getBytes();
        rawData = ProtocolTools.concatByteArrays(rawData, rawType);
        rawData = ProtocolTools.concatByteArrays(rawData, rawCaliber);
        rawData = ProtocolTools.concatByteArrays(rawData, rawSerial);

        CTRObjectFactory objectFactory = new CTRObjectFactory();
        AbstractCTRObject object = objectFactory.parse(rawData, 0, AttributeType.getValueAndObjectId());
        getFactory().writeRegister(object);
    }
}
