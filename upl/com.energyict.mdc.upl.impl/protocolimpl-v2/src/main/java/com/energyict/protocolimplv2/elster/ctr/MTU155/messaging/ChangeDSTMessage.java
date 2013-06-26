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
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;

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
        return message.getDeviceMessageSpecPrimaryKey().getValue().equals(ConfigurationChangeDeviceMessage.EnableOrDisableDST.getPrimaryKey().getValue());
    }

    @Override
    public CollectedMessage executeMessage(OfflineDeviceMessage message) {
        CollectedMessage collectedMessage = createCollectedMessage(message);
        boolean enableDST = ProtocolTools.getBooleanFromString(message.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());

        try {
            writeDST(enableDST);
            setSuccessfulDeviceMessageStatus(collectedMessage);
        } catch (CTRException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            String deviceMessageSpecName = Environment.getDefault().getTranslation(message.getDeviceMessageSpecPrimaryKey().getName());
            collectedMessage.setFailureInformation(ResultType.InCompatible, MdcManager.getIssueCollector().addProblem(message, "Messages.failed", deviceMessageSpecName, message.getDeviceMessageId(), e.getMessage()));
        }
        return collectedMessage;
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
