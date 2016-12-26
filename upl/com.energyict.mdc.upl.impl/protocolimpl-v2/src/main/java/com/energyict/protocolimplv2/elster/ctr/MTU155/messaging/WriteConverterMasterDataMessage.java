package com.energyict.protocolimplv2.elster.ctr.MTU155.messaging;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessage;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AttributeType;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.info.ConverterType;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.AbstractCTRObject;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.CTRObjectFactory;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;

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
        super(messaging, collectedDataFactory, issueFactory);
    }

    @Override
    public boolean canExecuteThisMessage(OfflineDeviceMessage message) {
        return message.getDeviceMessageSpecPrimaryKey().equals(ConfigurationChangeDeviceMessage.ConfigureConverterMasterData.getPrimaryKey().getValue());
    }

    @Override
    protected CollectedMessage doExecuteMessage(OfflineDeviceMessage message) throws CTRException {
        String converterTypeString = getDeviceMessageAttribute(message, DeviceMessageConstants.converterTypeAttributeName).getValue();
        String converterSerialNumber = getDeviceMessageAttribute(message, DeviceMessageConstants.converterSerialNumberAttributeName).getValue();

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
