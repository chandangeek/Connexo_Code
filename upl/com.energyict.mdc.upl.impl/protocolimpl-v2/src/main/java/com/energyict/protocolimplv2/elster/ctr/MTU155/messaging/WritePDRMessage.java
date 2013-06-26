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
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.P_Session;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.ReferenceDate;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.WriteDataBlock;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;

import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 2/03/11
 * Time: 16:40
 */
public class WritePDRMessage extends AbstractMTU155Message {

    public WritePDRMessage(Messaging messaging) {
        super(messaging);
    }

    @Override
    public boolean canExecuteThisMessage(OfflineDeviceMessage message) {
        return message.getDeviceMessageSpecPrimaryKey().getValue().equals(ConfigurationChangeDeviceMessage.WriteNewPDRNumber.getPrimaryKey().getValue());
    }

    @Override
    public CollectedMessage executeMessage(OfflineDeviceMessage message) {
        CollectedMessage collectedMessage = createCollectedMessage(message);
        String pdrString = message.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue().trim();

        try {
            validatePdr(collectedMessage, pdrString);
            writePdr(pdrString);
            setSuccessfulDeviceMessageStatus(collectedMessage);
        } catch (CTRException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            String deviceMessageSpecName = Environment.getDefault().getTranslation(message.getDeviceMessageSpecPrimaryKey().getName());
            collectedMessage.setFailureInformation(ResultType.InCompatible, MdcManager.getIssueCollector().addProblem(message, "Messages.failed", deviceMessageSpecName, message.getDeviceMessageId(), e.getMessage()));
        }

        return collectedMessage;
    }

    private void validatePdr(CollectedMessage collectedMessage, String pdr) throws CTRException {
        if (pdr.length() != 14) {
            String msg = "Unable to write pdr. PDR should be 14 digits but was [" + pdr.length() + "] [" + pdr + "]";
            collectedMessage.setDeviceProtocolInformation(msg);
            throw new CTRException(msg);
        }
        if (!ProtocolTools.isNumber(pdr)) {
            String msg = "Unable to write pdr. PDR should only contain numbers but was [" + pdr + "]";
            collectedMessage.setDeviceProtocolInformation(msg);
            throw new CTRException(msg);
        }
        if (pdr.equalsIgnoreCase("00000000000000")) {
            String msg = "Unable to write pdr. PDR with a value of '00000000000000' is not allowed!]";
            collectedMessage.setDeviceProtocolInformation(msg);
            throw new CTRException(msg);
        }
    }

    private void writePdr(String pdr) throws CTRException {
        ReferenceDate validityDate = new ReferenceDate().parse(new Date(), getFactory().getTimeZone());
        WriteDataBlock wdb = new WriteDataBlock((int) (100 * Math.random()));
        P_Session p_session = new P_Session(0x00);
        AttributeType attributeType = AttributeType.getValueOnly();
        attributeType.setHasIdentifier(true);
        byte[] rawData = ProtocolTools.concatByteArrays(new CTRObjectID("C.0.0").getBytes(), ProtocolTools.getBytesFromHexString(pdr, ""));
        CTRObjectFactory objectFactory = new CTRObjectFactory();
        AbstractCTRObject object = objectFactory.parse(rawData, 0, attributeType);
        getFactory().writeRegister(validityDate, wdb, p_session, attributeType, object);
    }
}
