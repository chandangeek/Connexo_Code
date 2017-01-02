package com.elster.protocolimpl.dlms.messaging;

import com.energyict.mdc.upl.messages.legacy.MessageAttributeSpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageSpec;
import com.energyict.mdc.upl.messages.legacy.MessageTagSpec;
import com.energyict.mdc.upl.messages.legacy.MessageValueSpec;

import com.elster.dlms.cosem.classes.class28.AutoAnswerModeEnum;
import com.elster.dlms.cosem.classes.common.TimeWindow;
import com.elster.dlms.cosem.simpleobjectmodel.Ek280Defs;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleAutoAnswerObject;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleCosemObjectManager;
import com.elster.dlms.types.basic.DlmsDateTime;
import com.energyict.protocolimpl.utils.MessagingTools;

import java.io.IOException;

import static com.elster.protocolimpl.dlms.util.RepetitiveDate.checkRepetitiveDate;
import static com.elster.protocolimpl.dlms.util.RepetitiveDate.dateStringToDlmsDateTime;

/**
 * User: heuckeg
 * Date: 06.10.11
 * Time: 14:18
 */
public class WriteAutoAnswerMessage extends AbstractDlmsMessage {

    /**
     * RtuMessage tags for the key change message
     */
    public static final String MESSAGE_TAG = "SetAutoAnswer";
    public static final String MESSAGE_DESCRIPTION = "Change auto answer data";
    public static final String ATTR_AUTOANSWER_ID = "AutoAnswerId";
    public static final String ATTR_AUTOANSWER_START = "AutoAnswerStart";
    public static final String ATTR_AUTOANSWER_END = "AutoAnswerEnd";

    public WriteAutoAnswerMessage(DlmsMessageExecutor messageExecutor) {
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
        String autoAnswerStart = MessagingTools.getContentOfAttribute(messageEntry, ATTR_AUTOANSWER_START);
        String autoAnswerEnd = MessagingTools.getContentOfAttribute(messageEntry, ATTR_AUTOANSWER_END);
        validateMessageData(autoAnswerId, autoAnswerStart, autoAnswerEnd);
        try {
            write(autoAnswerId, autoAnswerStart, autoAnswerEnd);
        } catch (IOException e) {
            throw new IOException("Unable to set auto answer data: " + e.getMessage(), e);
        }

    }

    private void write(String autoAnswerId, String autoAnswerStart, String autoAnswerEnd) throws IOException {

        SimpleCosemObjectManager objectManager = getExecutor().getDlms().getObjectManager();

        int acId = Integer.parseInt(autoAnswerId);

        final SimpleAutoAnswerObject autoAnswer =
                objectManager.getSimpleCosemObject(Ek280Defs.AUTO_ANSWER_1.derive(1, acId),
                        SimpleAutoAnswerObject.class);

        autoAnswer.setMode(AutoAnswerModeEnum.values()[0]);

        DlmsDateTime start = dateStringToDlmsDateTime(autoAnswerStart);
        DlmsDateTime end = dateStringToDlmsDateTime(autoAnswerEnd);
        autoAnswer.setListeningWindow(new TimeWindow[]{new TimeWindow(start, end)});
    }


    private void validateMessageData(String autoAnswerId, String autoAnswerStart, String autoAnswerEnd) {
        checkInt(autoAnswerId, "autoAnswer id", 1, 6);
        checkRepetitiveDate(autoAnswerStart, "AutoAnswer start");
        checkRepetitiveDate(autoAnswerEnd, "AutoAnswer end");
    }

    public static MessageSpec getMessageSpec(boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(MESSAGE_DESCRIPTION, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(MESSAGE_TAG);

        tagSpec.add(new MessageAttributeSpec(ATTR_AUTOANSWER_ID, true));
        tagSpec.add(new MessageAttributeSpec(ATTR_AUTOANSWER_START, true));
        tagSpec.add(new MessageAttributeSpec(ATTR_AUTOANSWER_END, false));
        tagSpec.add(new MessageValueSpec(" "));
        msgSpec.add(tagSpec);
        return msgSpec;
    }
}
