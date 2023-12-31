package com.elster.protocolimpl.dlms.messaging;

import com.energyict.mdc.upl.messages.legacy.MessageEntry;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Copyrights
 * Date: 7/06/11
 * Time: 13:14
 */
@SuppressWarnings({"unused"})
public abstract class AbstractDlmsMessage {

    private final DlmsMessageExecutor executor;

    public abstract boolean canExecuteThisMessage(MessageEntry messageEntry);

    public abstract void executeMessage(MessageEntry messageEntry) throws IOException;

    public AbstractDlmsMessage(DlmsMessageExecutor executor) {
        this.executor = executor;
    }

    public DlmsMessageExecutor getExecutor() {
        return executor;
    }

    protected boolean isMessageTag(String tag, String content) {
        return content.contains("<" + tag);
    }

    public Logger getLogger() {
        return getExecutor().getLogger();
    }

    private void checkString(String stringToCheck, String name) {
        if ((stringToCheck == null) || (stringToCheck.isEmpty())) {
            throw new IllegalArgumentException("Parameter " + name + " is 'null' or empty.");
        }
    }

    protected void checkInt(String stringToCheck, String name, int min, int max) {
        if ((stringToCheck == null) || (stringToCheck.isEmpty())) {
            throw new IllegalArgumentException("Parameter " + name + " is 'null' or empty.");
        }
        try {
            int i = Integer.parseInt(stringToCheck);
            if ((i < min) || (i > max)) {
                throw new NumberFormatException("Out of range (" + min + "-" + max + ")");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Error in client id: " + e.getMessage());
        }
    }
}
