package com.elster.protocolimpl.dlms.messaging;

import com.elster.dlms.cosem.simpleobjectmodel.Ek280Defs;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleCosemObjectManager;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleDataObject;
import com.energyict.cbo.BusinessException;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.MessageAttributeSpec;
import com.energyict.protocol.messaging.MessageSpec;
import com.energyict.protocol.messaging.MessageTagSpec;
import com.energyict.protocol.messaging.MessageValueSpec;
import com.energyict.protocolimpl.utils.MessagingTools;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 2/03/11
 * Time: 16:40
 */
public class WriteMeterMasterDataMessage extends AbstractDlmsMessage {

    private static final String MESSAGE_TAG = "WriteMeterMasterData";
    private static final String MESSAGE_DESCRIPTION = "Configure the gas meter master data";
    private static final String ATTR_METER_TYPE = "MeterType";
    private static final String ATTR_METER_CALIBER = "MeterCaliber";
    private static final String ATTR_METER_SERIAL = "MeterSerial";

    private static final int TYPE_LENGTH = 4;
    private static final int SERIAL_MAX_LENGTH = 13;
    private static final int MAX_CALIBER = 999999;

    public WriteMeterMasterDataMessage(DlmsMessageExecutor messageExecutor) {
        super(messageExecutor);
    }

    @Override
    public boolean canExecuteThisMessage(MessageEntry messageEntry) {
        return isMessageTag(MESSAGE_TAG, messageEntry.getContent());
    }

    @Override
    public void executeMessage(MessageEntry messageEntry) throws BusinessException {
        String meterTypeAttr = MessagingTools.getContentOfAttribute(messageEntry, ATTR_METER_TYPE);
        String meterCaliberAttr = MessagingTools.getContentOfAttribute(messageEntry, ATTR_METER_CALIBER);
        String meterSerialAttr = MessagingTools.getContentOfAttribute(messageEntry, ATTR_METER_SERIAL);

        String meterType = validateAndGetType(meterTypeAttr);
        int caliber = validateAndGetCaliber(meterCaliberAttr);
        String serial = validateAndGetSerial(meterSerialAttr);

        getLogger().warning("Writing 'Meter Master data' to EK280 [" + meterType + ", " + caliber + ", " + serial + "]");

        try {
            writeMeterMasterData(meterType, caliber, serial);
        } catch (IOException e) {
            throw new BusinessException("Unable to write meter master data: " + e.getMessage());
        }
    }

    private void writeMeterMasterData(String meterType, int caliber, String serial) throws IOException {

        SimpleDataObject dataObject;
        SimpleCosemObjectManager objectManager = getExecutor().getDlms().getObjectManager();

        dataObject = objectManager.getSimpleCosemObject(Ek280Defs.INST_METER_TYPE,  SimpleDataObject.class);
        dataObject.getValueAsString();
        dataObject.setStringValue(meterType);

        dataObject = objectManager.getSimpleCosemObject(Ek280Defs.INST_METER_CALIBER,  SimpleDataObject.class);
        dataObject.getValueAsString();
        dataObject.setStringValue("" + caliber);

        dataObject = objectManager.getSimpleCosemObject(Ek280Defs.INST_METER_SERIAL,  SimpleDataObject.class);
        dataObject.getValueAsString();
        dataObject.setStringValue(serial);
    }

    private int validateAndGetCaliber(String meterCaliberAttr) throws BusinessException {
        if (meterCaliberAttr == null) {
            throw new BusinessException("Meter caliber cannot be 'null'");
        }
        if ("".equals(meterCaliberAttr)) {
            throw new BusinessException("Meter caliber cannot be empty");
        }
        if (!ProtocolTools.isNumber(meterCaliberAttr)) {
            throw new BusinessException("Meter caliber should only contain digits (0-9), but [" + meterCaliberAttr + "] contains other characters.");
        }
        try {
            int caliber = Integer.valueOf(meterCaliberAttr);
            if (caliber > MAX_CALIBER) {
                throw new BusinessException("Meter caliber has a max value of [" + MAX_CALIBER + "] but was [" + caliber + "]");
            }
            return caliber;
        } catch (NumberFormatException e) {
            throw new BusinessException("Invalid caliber [" + meterCaliberAttr + "]. " + e.getMessage());
        }
    }

    private String validateAndGetSerial(String meterSerialAttr) throws BusinessException {
        if (meterSerialAttr == null) {
            throw new BusinessException("Meter serial cannot be 'null'");
        }
        if ("".equals(meterSerialAttr)) {
            throw new BusinessException("Meter serial cannot be empty");
        }
        if (meterSerialAttr.length() > SERIAL_MAX_LENGTH) {
            throw new BusinessException("Serial max length is [" + SERIAL_MAX_LENGTH + "] characters, but [" + meterSerialAttr + "] has [" + meterSerialAttr.length() + "] characters.");
        }
        return meterSerialAttr;
    }

    private String validateAndGetType(String meterTypeAttr) throws BusinessException {
        if (meterTypeAttr == null) {
            throw new BusinessException("Meter type cannot be 'null'");
        }
        if ("".equals(meterTypeAttr)) {
            throw new BusinessException("Meter type cannot be empty");
        }
        if (meterTypeAttr.length() != TYPE_LENGTH) {
            throw new BusinessException("Meter type should have " + TYPE_LENGTH + " characters, but [" + meterTypeAttr + "] has [" + meterTypeAttr.length() + "] characters");
        }

        return meterTypeAttr;
    }

    public static MessageSpec getMessageSpec(boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(MESSAGE_DESCRIPTION, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(MESSAGE_TAG);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");
        tagSpec.add(new MessageAttributeSpec(ATTR_METER_TYPE, true));
        tagSpec.add(new MessageAttributeSpec(ATTR_METER_CALIBER, true));
        tagSpec.add(new MessageAttributeSpec(ATTR_METER_SERIAL, true));
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

}
