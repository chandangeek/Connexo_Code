package com.energyict.protocolimplv2.elster.ctr.MTU155.messaging;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessage;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AttributeType;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.AbstractCTRObject;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.CTRObjectFactory;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;

/**
 * Copyrights EnergyICT
 * Date: 2/03/11
 * Time: 16:40
 */
public class WakeUpFrequency extends AbstractMTU155Message {

    public static final String OBJECT_ID = "E.7.0";

    public WakeUpFrequency(Messaging messaging) {
        super(messaging);
    }

    @Override
    public boolean canExecuteThisMessage(OfflineDeviceMessage message) {
        return message.getDeviceMessageSpecPrimaryKey().equals(NetworkConnectivityMessage.CHANGE_WAKEUP_FREQUENCY.getPrimaryKey().getValue());
    }

    @Override
    protected CollectedMessage doExecuteMessage(OfflineDeviceMessage message) throws CTRException {
        int periodInHours = Integer.parseInt(getDeviceMessageAttribute(message, DeviceMessageConstants.wakeupPeriodAttributeName).getValue());

        writeWakeUp(periodInHours);
        return null;
    }

    private void writeWakeUp(int periodInHours) throws CTRException {
        int pOff = ((periodInHours * 60) - 2) * 60;
        byte[] period = new byte[2];
        period[0] = (byte) ((pOff >> 8) & 0x0FF);
        period[1] = (byte) (pOff & 0x0FF);

        byte[] rawData = new CTRObjectID(OBJECT_ID).getBytes();
        rawData = ProtocolTools.concatByteArrays(rawData, ProtocolTools.getBytesFromHexString("0078", ""));
        rawData = ProtocolTools.concatByteArrays(rawData, period);
        rawData = ProtocolTools.concatByteArrays(rawData, ProtocolTools.getBytesFromHexString("00000000", ""));
        rawData = ProtocolTools.concatByteArrays(rawData, ProtocolTools.getBytesFromHexString("000000000000000000", ""));

        CTRObjectFactory objectFactory = new CTRObjectFactory();
        AbstractCTRObject object = objectFactory.parse(rawData, 0, AttributeType.getValueAndObjectId());
        getFactory().writeRegister(object);
    }
}
