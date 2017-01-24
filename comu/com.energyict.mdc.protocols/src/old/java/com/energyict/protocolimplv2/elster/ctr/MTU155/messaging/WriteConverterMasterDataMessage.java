package com.energyict.protocolimplv2.elster.ctr.MTU155.messaging;

import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.data.CollectedMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AttributeType;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.info.ConverterType;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.AbstractCTRObject;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.CTRObjectFactory;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;

/**
 * Copyrights EnergyICT
 * Date: 2/03/11
 * Time: 16:40
 */
public class WriteConverterMasterDataMessage extends AbstractMTU155Message {

    private static final String OBJECT_ID = "C.2.1";

    private static final int TYPE_LENGTH = 4;
    private static final int SERIAL_MAX_LENGTH = 16;

    public WriteConverterMasterDataMessage(Messaging messaging, IssueService issueService, CollectedDataFactory collectedDataFactory) {
        super(messaging, issueService, collectedDataFactory);
    }

    @Override
    public boolean canExecuteThisMessage(OfflineDeviceMessage message) {
        return message.getDeviceMessageId().equals(DeviceMessageId.CONFIGURATION_CHANGE_CONFIGURE_CONVERTER_MASTER_DATA);
    }

    @Override
    protected CollectedMessage doExecuteMessage(OfflineDeviceMessage message) throws CTRException {
        String converterTypeString = getDeviceMessageAttribute(message, DeviceMessageConstants.converterTypeAttributeName).getDeviceMessageAttributeValue();
        String converterSerialNumber = getDeviceMessageAttribute(message, DeviceMessageConstants.converterSerialNumberAttributeName).getDeviceMessageAttributeValue();

        ConverterType converterType = ConverterType.fromString(converterTypeString);
        validateConverterSerialNumber(converterSerialNumber);
        writeConverterMasterData(converterType, converterSerialNumber);
        return null;
    }

    private String validateConverterSerialNumber(String converterSerialAttr) throws CTRException {
        if (converterSerialAttr.length() > SERIAL_MAX_LENGTH) {
            String msg = "Converter max length is [" + SERIAL_MAX_LENGTH + "] characters, but [" + converterSerialAttr + "] has [" + converterSerialAttr.length() + "] characters.";
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