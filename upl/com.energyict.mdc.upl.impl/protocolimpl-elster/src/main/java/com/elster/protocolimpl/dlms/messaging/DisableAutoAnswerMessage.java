package com.elster.protocolimpl.dlms.messaging;

import com.energyict.mdc.upl.messages.legacy.MessageAttributeSpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageSpec;
import com.energyict.mdc.upl.messages.legacy.MessageTagSpec;
import com.energyict.mdc.upl.messages.legacy.MessageValueSpec;

import com.elster.dlms.cosem.classes.class28.AutoAnswerModeEnum;
import com.elster.dlms.cosem.simpleobjectmodel.Ek280Defs;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleAutoAnswerObject;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleCosemObjectManager;
import com.energyict.protocolimpl.utils.MessagingTools;

import java.io.IOException;

/**
 * User: heuckeg
 * Date: 06.10.11
 * Time: 14:26
 */
public class DisableAutoAnswerMessage extends AbstractDlmsMessage {

    /**
     * RtuMessage tags for the key change message
     */
    public static final String MESSAGE_TAG = "DisableAutoAnswer";
    public static final String MESSAGE_DESCRIPTION = "Disable auto answer";
    public static final String ATTR_AUTOANSWER_ID = "AutoAnswerId";

    public DisableAutoAnswerMessage(DlmsMessageExecutor messageExecutor) {
        super(messageExecutor);
    }

    @Override
    public boolean canExecuteThisMessage(MessageEntry messageEntry) {
        return isMessageTag(MESSAGE_TAG, messageEntry.getContent());
    }

    /**
     * Send the message  to the meter.
     *
     * @param messageEntry: the message containing the new keys
     * @throws com.energyict.cbo.BusinessException:
     *          when a parameter is null or too long
     */
    @Override
    public void executeMessage(MessageEntry messageEntry) throws IOException {

        String autoAnswerId = MessagingTools.getContentOfAttribute(messageEntry, ATTR_AUTOANSWER_ID);
        validateMessageData(autoAnswerId);
        try {
            write(autoAnswerId);
        } catch (IOException e) {
            throw new IOException("Unable to disable auto answer: " + e.getMessage(), e);
        }

    }

    private void write(String autoAnswerId) throws IOException {

        SimpleCosemObjectManager objectManager = getExecutor().getDlms().getObjectManager();

        int acId = Integer.parseInt(autoAnswerId);

        final SimpleAutoAnswerObject autoAnswer =
                objectManager.getSimpleCosemObject(Ek280Defs.AUTO_ANSWER_1.derive(1, acId),
                        SimpleAutoAnswerObject.class);

        autoAnswer.setMode(AutoAnswerModeEnum.values()[3]);
    }


    private void validateMessageData(String autoAnswerId) {
        checkInt(autoAnswerId, "autoAnswer id", 1, 6);
    }

    public static MessageSpec getMessageSpec(boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(MESSAGE_DESCRIPTION, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(MESSAGE_TAG);

        // Disable the value field in the EIServer message GUI
        tagSpec.add(new MessageAttributeSpec(ATTR_AUTOANSWER_ID, true));
        tagSpec.add(new MessageValueSpec(" "));
        msgSpec.add(tagSpec);
        return msgSpec;
    }
}
