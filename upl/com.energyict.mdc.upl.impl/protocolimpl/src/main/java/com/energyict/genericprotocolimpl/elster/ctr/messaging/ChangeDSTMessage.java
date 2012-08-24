package com.energyict.genericprotocolimpl.elster.ctr.messaging;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.ProcessingException;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException;
import com.energyict.genericprotocolimpl.elster.ctr.object.AbstractCTRObject;
import com.energyict.genericprotocolimpl.elster.ctr.object.CTRObjectFactory;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRObjectID;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.utils.MessagingTools;
import com.energyict.protocolimpl.utils.ProtocolTools;

import static com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType.getValueAndObjectId;
import static com.energyict.protocolimpl.utils.ProtocolTools.getBooleanFromString;
import static com.energyict.protocolimpl.utils.ProtocolTools.getBytesFromHexString;

/**
 * Copyrights EnergyICT
 * Date: 2/03/11
 * Time: 16:40
 */
public class ChangeDSTMessage extends AbstractMTU155Message {

    private static final String OBJECT_ID = "8.2.0";

    private static final String MESSAGE_TAG = "ChangeDST";
    private static final String MESSAGE_DESCRIPTION = "Enable/Disable Daylight Saving Time (DST)";
    private static final String ATTR_ENABLE_DST = "EnableDST";

    public ChangeDSTMessage(MTU155MessageExecutor messageExecutor) {
        super(messageExecutor);
    }

    @Override
    public boolean canExecuteThisMessage(MessageEntry messageEntry) {
        return isMessageTag(MESSAGE_TAG, messageEntry.getContent());
    }

    @Override
    public void executeMessage(MessageEntry messageEntry) throws BusinessException {
        String enableDstString = MessagingTools.getContentOfAttribute(messageEntry, ATTR_ENABLE_DST);
        boolean dst = validateAndGetDST(enableDstString);
        try {
            writeDST(dst);
        } catch (CTRException e) {
            throw new ProcessingException("Unable to " + (dst ? "enable" : "disable") + " DST.", e);
        }
    }

    public void writeDST(boolean dst) throws CTRException, BusinessException {
        byte[] rawData = new CTRObjectID(OBJECT_ID).getBytes();
        rawData = ProtocolTools.concatByteArrays(rawData, getBytesFromHexString(dst ? "01" : "00", ""));
        rawData = ProtocolTools.concatByteArrays(rawData, getBytesFromHexString("00000000", "")); // Start dates (0x00 means last sunday of march/october
        CTRObjectFactory objectFactory = new CTRObjectFactory();
        AbstractCTRObject object = objectFactory.parse(rawData, 0, getValueAndObjectId());
        getFactory().writeRegister(object);
    }

    private boolean validateAndGetDST(String enableDST) throws BusinessException {
        if (enableDST == null) {
            throw new BusinessException("Unable to change DST behaviour. Value was 'null'");
        }
        return getBooleanFromString(enableDST);
    }

    public static MessageSpec getMessageSpec(boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(MESSAGE_DESCRIPTION, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(MESSAGE_TAG);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");
        tagSpec.add(new MessageAttributeSpec(ATTR_ENABLE_DST, true));
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

}
