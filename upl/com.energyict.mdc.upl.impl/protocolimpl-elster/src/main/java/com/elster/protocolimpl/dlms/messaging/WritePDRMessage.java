package com.elster.protocolimpl.dlms.messaging;

import com.elster.dlms.cosem.simpleobjectmodel.Ek280Defs;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleCosemObjectManager;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleDataObject;
import com.energyict.cbo.BusinessException;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.MessageAttributeSpec;
import com.energyict.protocol.messaging.MessageSpec;
import com.energyict.protocol.messaging.MessageTagSpec;
import com.energyict.protocol.messaging.MessageValueSpec;
import com.energyict.protocolimpl.utils.MessagingTools;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 2/03/11
 * Time: 16:40
 */
public class WritePDRMessage extends AbstractDlmsMessage {

    private static final String MESSAGE_TAG = "WritePDR";
    private static final String MESSAGE_DESCRIPTION = "Write new PDR to device";
    private static final String ATTR_PDR = "PdrToWrite";

    public WritePDRMessage(DlmsMessageExecutor messageExecutor) {
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
        } catch (IOException e) {
            throw new BusinessException("Unable to write PDR.");
        }
    }

    private void writePdr(String pdr) throws IOException {

        SimpleCosemObjectManager objectManager = getExecutor().getDlms().getObjectManager();
        SimpleDataObject dataObject = objectManager.getSimpleCosemObject(Ek280Defs.METERING_POINT_ID, SimpleDataObject.class);
        dataObject.getValueAsString();
        dataObject.setStringValue(pdr);
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
