package com.energyict.genericprotocolimpl.elster.ctr.messaging;

import com.energyict.cbo.BusinessException;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException;
import com.energyict.genericprotocolimpl.elster.ctr.info.ConverterType;
import com.energyict.genericprotocolimpl.elster.ctr.object.AbstractCTRObject;
import com.energyict.genericprotocolimpl.elster.ctr.object.CTRObjectFactory;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRObjectID;
import com.energyict.mdw.shadow.RtuShadow;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.utils.MessagingTools;

import java.sql.SQLException;

import static com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType.getValueAndObjectId;
import static com.energyict.protocolimpl.utils.ProtocolTools.concatByteArrays;

/**
 * Copyrights EnergyICT
 * Date: 2/03/11
 * Time: 16:40
 */
public class WriteConverterMasterDataMessage extends AbstractMTU155Message {

    private static final String OBJECT_ID = "C.2.1";
    
    private static final String MESSAGE_TAG = "WriteConverterMasterData";
    private static final String MESSAGE_DESCRIPTION = "Configure the converter master data";
    private static final String ATTR_CONVERTER_TYPE = "ConverterType";
    private static final String ATTR_CONVERTER_SERIAL = "ConverterSerial";

    private static final int TYPE_LENGTH = 4;
    private static final int SERIAL_MAX_LENGTH = 16;

    public WriteConverterMasterDataMessage(MTU155MessageExecutor messageExecutor) {
        super(messageExecutor);
    }

    @Override
    public boolean canExecuteThisMessage(MessageEntry messageEntry) {
        return isMessageTag(MESSAGE_TAG, messageEntry.getContent());
    }

    @Override
    public void executeMessage(MessageEntry messageEntry) throws BusinessException {
        String converterTypeAttr = MessagingTools.getContentOfAttribute(messageEntry, ATTR_CONVERTER_TYPE);
        String converterSerialAttr = MessagingTools.getContentOfAttribute(messageEntry, ATTR_CONVERTER_SERIAL);

        ConverterType converterType = validateAndGetType(converterTypeAttr);
        String serial = validateAndGetSerial(converterSerialAttr);

        getLogger().warning("Writing 'Converter Master data' to MTU155 [" + converterType + ", " + serial + "]");

        try {
            writeConverterMasterData(converterType, serial);
            updateConverterSerialInEIServer(serial);
        } catch (CTRException e) {
            throw new BusinessException("Unable to write converter master data.", e);
        }
    }

    private void updateConverterSerialInEIServer(String serial) throws BusinessException {
        try {
            RtuShadow shadow = getRtu().getShadow();
            shadow.setName(serial);
            shadow.setExternalName("rtu/" + serial);
            getRtu().update(shadow);
        } catch (SQLException e) {
            throw new BusinessException("Wrote converter master data to device, but could not change the converter serial number in EIServer! " + e.getMessage());
        } catch (BusinessException e) {
            throw new BusinessException("Wrote converter master data to device, but could not change the converter serial number in EIServer! " + e.getMessage());
        }
    }

    private void writeConverterMasterData(ConverterType converterType, String serial) throws CTRException {
        byte[] rawType = converterType.name().getBytes();
        rawType = concatByteArrays(rawType, new byte[TYPE_LENGTH - rawType.length]);

        byte[] rawSerial = serial.getBytes();
        rawSerial = concatByteArrays(rawSerial, new byte[SERIAL_MAX_LENGTH - rawSerial.length]);

        byte[] rawData = new CTRObjectID(OBJECT_ID).getBytes();
        rawData = concatByteArrays(rawData, rawType);
        rawData = concatByteArrays(rawData, rawSerial);

        CTRObjectFactory objectFactory = new CTRObjectFactory();
        AbstractCTRObject object = objectFactory.parse(rawData, 0, getValueAndObjectId());
        getFactory().writeRegister(object);
    }

    private String validateAndGetSerial(String converterSerialAttr) throws BusinessException {
        if (converterSerialAttr == null) {
            throw new BusinessException("Converter serial cannot be 'null'");
        }
        if ("".equals(converterSerialAttr)) {
            throw new BusinessException("Converter serial cannot be empty");
        }
        if (converterSerialAttr.length() > SERIAL_MAX_LENGTH) {
            throw new BusinessException("Converter max length is [" + SERIAL_MAX_LENGTH + "] characters, but [" + converterSerialAttr + "] has [" + converterSerialAttr.length() + "] characters.");
        }
        return converterSerialAttr;
    }

    private ConverterType validateAndGetType(String converterTypeAttr) throws BusinessException {
        if (converterTypeAttr == null) {
            throw new BusinessException("Converter type cannot be 'null'");
        }
        if ("".equals(converterTypeAttr)) {
            throw new BusinessException("Converter type cannot be empty");
        }
        if (converterTypeAttr.length() != TYPE_LENGTH) {
            throw new BusinessException("Converter type should have " + TYPE_LENGTH + " characters, but [" + converterTypeAttr + "] has [" + converterTypeAttr.length() + "] characters");
        }
        ConverterType converterType = ConverterType.fromString(converterTypeAttr);
        if (!converterType.isValid()) {
            throw new BusinessException("Converter type [" + converterTypeAttr + "] is not a valid type. Resulted in [" + converterType + "]");
        }
        return converterType;
    }

    public static MessageSpec getMessageSpec(boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(MESSAGE_DESCRIPTION, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(MESSAGE_TAG);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");
        tagSpec.add(new MessageAttributeSpec(ATTR_CONVERTER_TYPE, true));
        tagSpec.add(new MessageAttributeSpec(ATTR_CONVERTER_SERIAL, true));
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

}
