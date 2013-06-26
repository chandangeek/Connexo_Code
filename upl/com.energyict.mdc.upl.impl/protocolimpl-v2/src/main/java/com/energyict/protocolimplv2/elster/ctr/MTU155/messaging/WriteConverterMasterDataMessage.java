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
import com.energyict.protocolimplv2.elster.ctr.MTU155.info.ConverterType;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.AbstractCTRObject;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.CTRObjectFactory;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;

/**
 * Copyrights EnergyICT
 * Date: 2/03/11
 * Time: 16:40
 */
public class WriteConverterMasterDataMessage extends AbstractMTU155Message {

    private static final String OBJECT_ID = "C.2.1";

    private static final int TYPE_LENGTH = 4;
    private static final int SERIAL_MAX_LENGTH = 16;

    public WriteConverterMasterDataMessage(Messaging messaging) {
        super(messaging);
    }

    @Override
    public boolean canExecuteThisMessage(OfflineDeviceMessage message) {
        return message.getDeviceMessageSpecPrimaryKey().getValue().equals(ConfigurationChangeDeviceMessage.ConfigureConverterMasterData.getPrimaryKey().getValue());
    }

    @Override
    public CollectedMessage executeMessage(OfflineDeviceMessage message) {
        CollectedMessage collectedMessage = createCollectedMessage(message);
        String converterTypeString = message.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue().trim();
        String converterSerialNumber = message.getDeviceMessageAttributes().get(1).getDeviceMessageAttributeValue().trim();

        try {
            ConverterType converterType = ConverterType.fromString(converterTypeString);
            validateConverterSerialNumber(collectedMessage, converterSerialNumber);
            writeConverterMasterData(converterType, converterSerialNumber);
            setSuccessfulDeviceMessageStatus(collectedMessage);
        } catch (CTRException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            String deviceMessageSpecName = Environment.getDefault().getTranslation(message.getDeviceMessageSpecPrimaryKey().getName());
            collectedMessage.setFailureInformation(ResultType.InCompatible, MdcManager.getIssueCollector().addProblem(message, "Messages.failed", deviceMessageSpecName, message.getDeviceMessageId(), e.getMessage()));
        }
        return collectedMessage;
    }

    private String validateConverterSerialNumber(CollectedMessage collectedMessage, String converterSerialAttr) throws CTRException {
        if (converterSerialAttr.length() > SERIAL_MAX_LENGTH) {
            String msg = "Converter max length is [" + SERIAL_MAX_LENGTH + "] characters, but [" + converterSerialAttr + "] has [" + converterSerialAttr.length() + "] characters.";
            collectedMessage.setDeviceProtocolInformation(msg);
            throw new CTRException(msg);
        }
        return converterSerialAttr;
    }

    private void writeConverterMasterData(ConverterType converterType, String serial) throws CTRException {
        byte[] rawType = converterType.name().getBytes();
        rawType = ProtocolTools.concatByteArrays(rawType, new byte[TYPE_LENGTH - rawType.length]);

        byte[] rawSerial = serial.getBytes();
        rawSerial = ProtocolTools.concatByteArrays(rawSerial, new byte[SERIAL_MAX_LENGTH - rawSerial.length]);

        byte[] rawData = new CTRObjectID(OBJECT_ID).getBytes();
        rawData = ProtocolTools.concatByteArrays(rawData, rawType);
        rawData = ProtocolTools.concatByteArrays(rawData, rawSerial);

        CTRObjectFactory objectFactory = new CTRObjectFactory();
        AbstractCTRObject object = objectFactory.parse(rawData, 0, AttributeType.getValueAndObjectId());
        getFactory().writeRegister(object);
    }
}
