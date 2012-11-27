package com.energyict.genericprotocolimpl.elster.ctr.messaging;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.ProcessingException;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException;
import com.energyict.genericprotocolimpl.elster.ctr.info.MeterType;
import com.energyict.genericprotocolimpl.elster.ctr.object.AbstractCTRObject;
import com.energyict.genericprotocolimpl.elster.ctr.object.CTRObjectFactory;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRObjectID;
import com.energyict.mdw.shadow.DeviceShadow;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.utils.MessagingTools;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.sql.SQLException;

import static com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType.getValueAndObjectId;
import static com.energyict.protocolimpl.utils.ProtocolTools.concatByteArrays;

/**
 * Copyrights EnergyICT
 * Date: 2/03/11
 * Time: 16:40
 */
public class WriteMeterMasterDataMessage extends AbstractMTU155Message {

    private static final String OBJECT_ID = "C.2.0";

    private static final String MESSAGE_TAG = "WriteMeterMasterData";
    private static final String MESSAGE_DESCRIPTION = "Configure the gas meter master data";
    private static final String ATTR_METER_TYPE = "MeterType";
    private static final String ATTR_METER_CALIBER = "MeterCaliber";
    private static final String ATTR_METER_SERIAL = "MeterSerial";

    private static final int TYPE_LENGTH = 4;
    private static final int CALIBER_LENGTH = 3;
    private static final int SERIAL_MAX_LENGTH = 13;
    private static final int MAX_CALIBER = 999999;

    public WriteMeterMasterDataMessage(MTU155MessageExecutor messageExecutor) {
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

        MeterType meterType = validateAndGetType(meterTypeAttr);
        int caliber = validateAndGetCaliber(meterCaliberAttr);
        String serial = validateAndGetSerial(meterSerialAttr);

        getLogger().warning("Writing 'Meter Master data' to MTU155 [" + meterType + ", " + caliber + ", " + serial + "]");

        try {
            writeMeterMasterData(meterType, caliber, serial);
            updateMeterSerialInEIServer(serial);
        } catch (CTRException e) {
            throw new ProcessingException("Unable to write meter master data.", e);
        }
    }

    private void updateMeterSerialInEIServer(String serial) throws BusinessException {
        try {
            DeviceShadow shadow = getRtu().getShadow();
            shadow.setSerialNumber(serial);
            getRtu().update(shadow);
        } catch (SQLException e) {
            throw new BusinessException("Wrote meter master data to device, but could not change the meter serial number in EIServer! " + e.getMessage());
        } catch (BusinessException e) {
            throw new BusinessException("Wrote meter master data to device, but could not change the meter serial number in EIServer! " + e.getMessage());
        }
    }


    private void writeMeterMasterData(MeterType meterType, int caliber, String serial) throws CTRException {
        byte[] rawType = meterType.name().getBytes();
        rawType = concatByteArrays(rawType, new byte[TYPE_LENGTH - rawType.length]);

        byte[] rawCaliber = new byte[CALIBER_LENGTH];
        rawCaliber[2] = (byte) (caliber & 0x0FF);
        rawCaliber[1] = (byte) ((caliber >> 8) & 0x0FF);
        rawCaliber[0] = (byte) ((caliber >> 16) & 0x0FF);

        byte[] rawSerial = serial.getBytes();
        rawSerial = concatByteArrays(rawSerial, new byte[SERIAL_MAX_LENGTH - rawSerial.length]);

        byte[] rawData = new CTRObjectID(OBJECT_ID).getBytes();
        rawData = concatByteArrays(rawData, rawType);
        rawData = concatByteArrays(rawData, rawCaliber);
        rawData = concatByteArrays(rawData, rawSerial);

        CTRObjectFactory objectFactory = new CTRObjectFactory();
        AbstractCTRObject object = objectFactory.parse(rawData, 0, getValueAndObjectId());
        getFactory().writeRegister(object);
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

    private MeterType validateAndGetType(String meterTypeAttr) throws BusinessException {
        if (meterTypeAttr == null) {
            throw new BusinessException("Meter type cannot be 'null'");
        }
        if ("".equals(meterTypeAttr)) {
            throw new BusinessException("Meter type cannot be empty");
        }
        if (meterTypeAttr.length() != TYPE_LENGTH) {
            throw new BusinessException("Meter type should have " + TYPE_LENGTH + " characters, but [" + meterTypeAttr + "] has [" + meterTypeAttr.length() + "] characters");
        }
        MeterType meterType = MeterType.fromString(meterTypeAttr);
        if (!meterType.isValid()) {
            throw new BusinessException("Meter type [" + meterTypeAttr + "] is not a valid type. Resulted in [" + meterType + "]");
        }
        return meterType;
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
