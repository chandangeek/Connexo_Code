package com.energyict.genericprotocolimpl.elster.ctr.messaging;

import com.energyict.cbo.BusinessException;
import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException;
import com.energyict.genericprotocolimpl.elster.ctr.object.AbstractCTRObject;
import com.energyict.genericprotocolimpl.elster.ctr.object.CTRObjectFactory;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRObjectID;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.*;
import com.energyict.mdw.shadow.RtuShadow;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.utils.MessagingTools;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.sql.SQLException;
import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 2/03/11
 * Time: 16:40
 */
public class WritePDRMessage extends AbstractMTU155Message {

    private static final String MESSAGE_TAG = "WritePDR";
    private static final String MESSAGE_DESCRIPTION = "Write new PDR to device";
    private static final String ATTR_PDR = "PdrToWrite";

    public WritePDRMessage(MTU155MessageExecutor messageExecutor) {
        super(messageExecutor);
    }

    @Override
    public boolean canExecuteThisMessage(MessageEntry messageEntry) {
        return isMessageTag(MESSAGE_TAG, messageEntry.getContent());
    }

    @Override
    public void executeMessage(MessageEntry messageEntry) throws BusinessException {
        String pdr = MessagingTools.getContentOfAttribute(messageEntry, ATTR_PDR);
        validatePdr(pdr);
        try {
            writePdr(pdr);
            updatePdrInEiserver(pdr);
        } catch (CTRException e) {
            throw new BusinessException("Unable to write PDR.", e);
        }
    }

    private void updatePdrInEiserver(String pdr) throws BusinessException {
        try {
            RtuShadow shadow = getRtu().getShadow();
            shadow.setDialHomeId(pdr);
            getRtu().update(shadow);
        } catch (SQLException e) {
            throw new BusinessException("Wrote PDR to device, but could not change the PDR in EIServer! " + e.getMessage());
        } catch (BusinessException e) {
            throw new BusinessException("Wrote PDR to device, but could not change the PDR in EIServer! " + e.getMessage());
        }
    }

    private void writePdr(String pdr) throws CTRException, BusinessException {
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

    private void validatePdr(String pdr) throws BusinessException {
        if (pdr == null) {
            throw new BusinessException("Unable to write pdr. PDR value was 'null'");
        }
        if (pdr.length() != 14) {
            throw new BusinessException("Unable to write pdr. PDR should be 14 digits but was [" + pdr.length() + "] [" + pdr + "]");
        }
        if (!ProtocolTools.isNumber(pdr)) {
            throw new BusinessException("Unable to write pdr. PDR should only contain numbers but was [" + pdr + "]");
        }
        if (pdr.equalsIgnoreCase("00000000000000")) {
            throw new BusinessException("Unable to write pdr. PDR with a value of '00000000000000' is not allowed!]");
        }
    }

    public static MessageSpec getMessageSpec(boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(MESSAGE_DESCRIPTION, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(MESSAGE_TAG);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");
        tagSpec.add(new MessageAttributeSpec(ATTR_PDR, true));
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

}
