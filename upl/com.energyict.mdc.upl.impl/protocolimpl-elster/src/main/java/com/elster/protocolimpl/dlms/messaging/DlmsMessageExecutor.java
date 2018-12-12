package com.elster.protocolimpl.dlms.messaging;

import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;

import com.elster.protocolimpl.dlms.Dlms;
import com.energyict.protocol.MessageResult;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Copyrights
 * Date: 7/06/11
 * Time: 13:03
 */
public class DlmsMessageExecutor {

    private final Dlms dlms;

    public DlmsMessageExecutor(Dlms dlms) {
        this.dlms = dlms;
    }

    public Logger getLogger() {
        return getDlms().getLogger();
    }

    public MessageResult doMessage(MessageEntry messageEntry) throws IOException {
        String msg;
        boolean messageFound = false;
        boolean success = false;
        boolean fuTimeout = false;

        if (messageEntry.getContent() != null) {
            AbstractDlmsMessage[] messages = getSupportedMessages();
            try {
                for (AbstractDlmsMessage message : messages) {
                    if (message.canExecuteThisMessage(messageEntry)) {
                        messageFound = true;
                        message.executeMessage(messageEntry);
                        success = true;
                        break;
                    }
                }
            } catch (FirmwareUpdateTimeoutException fute) {
                fuTimeout = true;
                getLogger().warning("FirmwareUpdateTimeoutException: " + fute.getMessage());
            }

            if (fuTimeout) {
                getLogger().warning("Message \"firmwareupdate\" will retry due to timeout.");
                return MessageResult.createQueued(messageEntry);
            }

            if (success) {
                getLogger().severe("Message " + messageEntry.toString() + " has finished successfully.");
                return MessageResult.createSuccess(messageEntry);
            }

            msg = messageFound ? "Message " + messageEntry.toString() + " failed." : "Unknown message: " + messageEntry.toString();
        } else {
            msg = "Message failed.";
        }
        getLogger().severe(msg);

        return MessageResult.createFailed(messageEntry);

    }

    protected AbstractDlmsMessage[] getSupportedMessages() {
        return new AbstractDlmsMessage[]{};
    }

    public List<MessageCategorySpec> getMessageCategories() {
        return Collections.emptyList();
    }

    public Dlms getDlms() {
        return dlms;
    }
}
