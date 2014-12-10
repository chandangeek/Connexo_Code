package com.energyict.protocolimplv2.elster.ctr.MTU155.messaging;

import com.energyict.mdc.protocol.api.device.data.CollectedMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AttributeType;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.AbstractCTRObject;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.CTRObjectFactory;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;
import com.energyict.mdc.protocol.api.impl.device.messages.ClockDeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;

/**
 * Copyrights EnergyICT
 * Date: 2/03/11
 * Time: 16:40
 */
public class ChangeDSTMessage extends AbstractMTU155Message {

    private static final String OBJECT_ID = "8.2.0";

    public ChangeDSTMessage(Messaging messaging) {
        super(messaging);
    }

    @Override
    public boolean canExecuteThisMessage(OfflineDeviceMessage message) {
        return message.getDeviceMessageId().equals(ClockDeviceMessage.EnableOrDisableDST.getId());
    }

    @Override
    protected CollectedMessage doExecuteMessage(OfflineDeviceMessage message) throws CTRException {
        boolean enableDST = ProtocolTools.getBooleanFromString(getDeviceMessageAttribute(message, DeviceMessageConstants.enableDSTAttributeName).getDeviceMessageAttributeValue());
        writeDST(enableDST);
        return null;
    }

    public void writeDST(boolean dst) throws CTRException {
        byte[] rawData = new CTRObjectID(OBJECT_ID).getBytes();
        rawData = ProtocolTools.concatByteArrays(rawData,ProtocolTools.getBytesFromHexString(dst ? "01" : "00", ""));
        rawData = ProtocolTools.concatByteArrays(rawData, ProtocolTools.getBytesFromHexString("00000000", "")); // Start dates (0x00 means last sunday of march/october
        CTRObjectFactory objectFactory = new CTRObjectFactory();
        AbstractCTRObject object = objectFactory.parse(rawData, 0, AttributeType.getValueAndObjectId());
        getFactory().writeRegister(object);
    }
}
