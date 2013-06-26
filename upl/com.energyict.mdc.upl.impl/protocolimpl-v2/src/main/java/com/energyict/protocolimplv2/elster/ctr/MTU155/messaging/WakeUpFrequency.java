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
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.AbstractCTRObject;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.CTRObjectFactory;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;
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
        return message.getDeviceMessageSpecPrimaryKey().getValue().equals(NetworkConnectivityMessage.CHANGE_WAKEUP_FREQUENCY.getPrimaryKey().getValue());
    }

    @Override
    public CollectedMessage executeMessage(OfflineDeviceMessage message) {
        CollectedMessage collectedMessage = createCollectedMessage(message);
        int periodInHours = Integer.parseInt(message.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());

        try {
            writeWakeUp(periodInHours);
            setSuccessfulDeviceMessageStatus(collectedMessage);
        } catch (CTRException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            String deviceMessageSpecName = Environment.getDefault().getTranslation(message.getDeviceMessageSpecPrimaryKey().getName());
            collectedMessage.setFailureInformation(ResultType.InCompatible, MdcManager.getIssueCollector().addProblem(message, "Messages.failed", deviceMessageSpecName, message.getDeviceMessageId(), e.getMessage()));
        }
        return collectedMessage;
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
