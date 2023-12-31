package com.elster.protocolimpl.dlms.messaging;

import com.energyict.mdc.upl.messages.legacy.MessageAttributeSpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageSpec;
import com.energyict.mdc.upl.messages.legacy.MessageTagSpec;
import com.energyict.mdc.upl.messages.legacy.MessageValueSpec;

import com.elster.dlms.cosem.simpleobjectmodel.Ek280Defs;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleCosemObjectManager;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleDataObject;
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
    public void executeMessage(MessageEntry messageEntry) throws IOException {
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
            throw new IOException("Unable to write meter master data: " + e.getMessage(), e);
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

    private int validateAndGetCaliber(String meterCaliberAttr) {
        if (meterCaliberAttr == null) {
            throw new IllegalArgumentException("Meter caliber cannot be 'null'");
        }
        if ("".equals(meterCaliberAttr)) {
            throw new IllegalArgumentException("Meter caliber cannot be empty");
        }
        if (!ProtocolTools.isNumber(meterCaliberAttr)) {
            throw new IllegalArgumentException("Meter caliber should only contain digits (0-9), but [" + meterCaliberAttr + "] contains other characters.");
        }
        try {
            int caliber = Integer.valueOf(meterCaliberAttr);
            if (caliber > MAX_CALIBER) {
                throw new IllegalArgumentException("Meter caliber has a max value of [" + MAX_CALIBER + "] but was [" + caliber + "]");
            }
            return caliber;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid caliber [" + meterCaliberAttr + "]. " + e.getMessage(), e);
        }
    }

    private String validateAndGetSerial(String meterSerialAttr) {
        if (meterSerialAttr == null) {
            throw new IllegalArgumentException("Meter serial cannot be 'null'");
        }
        if ("".equals(meterSerialAttr)) {
            throw new IllegalArgumentException("Meter serial cannot be empty");
        }
        if (meterSerialAttr.length() > SERIAL_MAX_LENGTH) {
            throw new IllegalArgumentException("Serial max length is [" + SERIAL_MAX_LENGTH + "] characters, but [" + meterSerialAttr + "] has [" + meterSerialAttr.length() + "] characters.");
        }
        return meterSerialAttr;
    }

    private String validateAndGetType(String meterTypeAttr) {
        if (meterTypeAttr == null) {
            throw new IllegalArgumentException("Meter type cannot be 'null'");
        }
        if ("".equals(meterTypeAttr)) {
            throw new IllegalArgumentException("Meter type cannot be empty");
        }
        if (meterTypeAttr.length() != TYPE_LENGTH) {
            throw new IllegalArgumentException("Meter type should have " + TYPE_LENGTH + " characters, but [" + meterTypeAttr + "] has [" + meterTypeAttr.length() + "] characters");
        }

        return meterTypeAttr;
    }

    public static MessageSpec getMessageSpec(boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(MESSAGE_DESCRIPTION, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(MESSAGE_TAG);
        tagSpec.add(new MessageAttributeSpec(ATTR_METER_TYPE, true));
        tagSpec.add(new MessageAttributeSpec(ATTR_METER_CALIBER, true));
        tagSpec.add(new MessageAttributeSpec(ATTR_METER_SERIAL, true));
        tagSpec.add(new MessageValueSpec(" "));
        msgSpec.add(tagSpec);
        return msgSpec;
    }

}
