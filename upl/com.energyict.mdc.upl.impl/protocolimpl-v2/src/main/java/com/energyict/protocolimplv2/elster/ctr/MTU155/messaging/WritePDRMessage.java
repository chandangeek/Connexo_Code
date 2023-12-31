package com.energyict.protocolimplv2.elster.ctr.MTU155.messaging;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AttributeType;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.AbstractCTRObject;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.CTRObjectFactory;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.P_Session;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.ReferenceDate;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.WriteDataBlock;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;

import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 2/03/11
 * Time: 16:40
 */
public class WritePDRMessage extends AbstractMTU155Message {

    public WritePDRMessage(Messaging messaging, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(messaging, collectedDataFactory, issueFactory);
    }

    @Override
    public boolean canExecuteThisMessage(OfflineDeviceMessage message) {
        return message.getSpecification().getId() == ConfigurationChangeDeviceMessage.WriteNewPDRNumber.id();
    }

    @Override
    protected CollectedMessage doExecuteMessage(OfflineDeviceMessage message) throws CTRException {
        String pdrString = getDeviceMessageAttribute(message, DeviceMessageConstants.newPDRAttributeName).getValue();

        validatePdr(pdrString);
        writePdr(pdrString);
        return null;
    }

    private void validatePdr(String pdr) throws CTRException {
        if (pdr.length() != 14) {
            String msg = "Unable to write pdr. PDR should be 14 digits but was " + pdr.length() + " [" + pdr + "]";
            throw new CTRException(msg);
        }
        if (!ProtocolTools.isNumber(pdr)) {
            String msg = "Unable to write pdr. PDR should only contain numbers but was [" + pdr + "]";
            throw new CTRException(msg);
        }
        if (pdr.equalsIgnoreCase("00000000000000")) {
            String msg = "Unable to write pdr. PDR with a value of '00000000000000' is not allowed!";
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
