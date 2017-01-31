/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.messaging;

import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AttributeType;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.info.MeterType;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.AbstractCTRObject;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.CTRObjectFactory;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;


public class WriteMeterMasterDataMessage extends AbstractMTU155Message {

    private static final String OBJECT_ID = "C.2.0";

    private static final int TYPE_LENGTH = 4;
    private static final int CALIBER_LENGTH = 3;
    private static final int SERIAL_MAX_LENGTH = 13;
    private static final int MAX_CALIBER = 999999;

    public WriteMeterMasterDataMessage(Messaging messaging, IssueService issueService, CollectedDataFactory collectedDataFactory) {
        super(messaging, issueService, collectedDataFactory);
    }

    @Override
    public boolean canExecuteThisMessage(OfflineDeviceMessage message) {
        return message.getDeviceMessageId().equals(DeviceMessageId.CONFIGURATION_CHANGE_CONFIGURE_GAS_METER_MASTER_DATA);
    }

    @Override
    protected CollectedMessage doExecuteMessage(OfflineDeviceMessage message) throws CTRException {
        String meterTypeString = message.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue().trim();
        String meterCaliberString = message.getDeviceMessageAttributes().get(1).getDeviceMessageAttributeValue().trim();
        String meterSerialNumber = message.getDeviceMessageAttributes().get(2).getDeviceMessageAttributeValue().trim();

        MeterType meterType = MeterType.fromString(meterTypeString);
        int meterCaliber = validateAndGetMeterCaliber(meterCaliberString);
        validateMeterSerialnumber(meterSerialNumber);
        writeMeterMasterData(meterType, meterCaliber, meterSerialNumber);
        return null;
    }

    private int validateAndGetMeterCaliber(String meterCaliberAttr) throws CTRException {
        try {
            int caliber = Integer.valueOf(meterCaliberAttr);
            if (caliber > MAX_CALIBER) {
                String msg = "Meter caliber has a max value of [" + MAX_CALIBER + "] but was [" + caliber + "]";
                throw new CTRException(msg);
            }
            return caliber;
        } catch (NumberFormatException e) {
            String msg = "Invalid caliber [" + meterCaliberAttr + "], " + e.getMessage();
            throw new CTRException(msg);
        }
    }

    private void validateMeterSerialnumber(String meterSerialAttr) throws CTRException {
        if (meterSerialAttr.length() > SERIAL_MAX_LENGTH) {
            String msg = "Serial max length is [" + SERIAL_MAX_LENGTH + "] characters, but [" + meterSerialAttr + "] has [" + meterSerialAttr.length() + "] characters.";
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