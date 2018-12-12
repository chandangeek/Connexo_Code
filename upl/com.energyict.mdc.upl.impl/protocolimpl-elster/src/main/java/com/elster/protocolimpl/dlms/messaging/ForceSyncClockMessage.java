package com.elster.protocolimpl.dlms.messaging;

import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageSpec;
import com.energyict.mdc.upl.messages.legacy.MessageTagSpec;
import com.energyict.mdc.upl.messages.legacy.MessageValueSpec;

import com.elster.dlms.cosem.simpleobjectmodel.Ek280Defs;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleClockObject;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleCosemObjectManager;
import com.elster.dlms.types.basic.DlmsDateTime;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 2/03/11
 * Time: 16:40
 */
@SuppressWarnings({"unused"})
public class ForceSyncClockMessage extends AbstractDlmsMessage {

    private static final String MESSAGE_TAG = "ForceSyncClock";
    private static final String MESSAGE_DESCRIPTION = "Force sync clock";

    public ForceSyncClockMessage(DlmsMessageExecutor messageExecutor) {
        super(messageExecutor);
    }

    @Override
    public boolean canExecuteThisMessage(MessageEntry messageEntry) {
        return isMessageTag(MESSAGE_TAG, messageEntry.getContent());
    }

    @Override
    public void executeMessage(MessageEntry messageEntry) throws IOException {
        try {
            Date currentTime = Calendar.getInstance(getExecutor().getDlms().getTimeZone()).getTime();
            getLogger().severe("Received [" + MESSAGE_TAG + "] message. Writing system time to device: " + currentTime);
            writeTime(currentTime);
        } catch (IOException e) {
            throw new IOException("Unable to set the device time! " + e.getMessage(), e);
        }
    }

    private void writeTime(Date currentTime) throws IOException {

        SimpleCosemObjectManager objectManager = getExecutor().getDlms().getObjectManager();

        SimpleClockObject clockObject =
                objectManager.getSimpleCosemObject(Ek280Defs.CLOCK_OBJECT, SimpleClockObject.class);
        DlmsDateTime dateTime = new DlmsDateTime(new Date());

        clockObject.setTime(dateTime);
    }

    public static MessageSpec getMessageSpec(boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(MESSAGE_DESCRIPTION, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(MESSAGE_TAG);
        tagSpec.add(new MessageValueSpec(" "));
        msgSpec.add(tagSpec);
        return msgSpec;
    }

}
