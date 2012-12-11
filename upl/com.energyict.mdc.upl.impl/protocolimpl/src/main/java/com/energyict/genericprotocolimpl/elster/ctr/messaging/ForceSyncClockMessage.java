package com.energyict.genericprotocolimpl.elster.ctr.messaging;

import com.energyict.cbo.BusinessException;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.*;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Copyrights EnergyICT
 * Date: 2/03/11
 * Time: 16:40
 */
public class ForceSyncClockMessage extends AbstractMTU155Message {

    private static final String MESSAGE_TAG = "ForceSyncClock";
    private static final String MESSAGE_DESCRIPTION = "Force sync clock";

    public ForceSyncClockMessage(MTU155MessageExecutor messageExecutor) {
        super(messageExecutor);
    }

    @Override
    public boolean canExecuteThisMessage(MessageEntry messageEntry) {
        return isMessageTag(MESSAGE_TAG, messageEntry.getContent());
    }

    @Override
    public void executeMessage(MessageEntry messageEntry) throws BusinessException {
        try {
            Date currentTime = Calendar.getInstance(TimeZone.getDefault()).getTime();
            getLogger().severe("Received [" + MESSAGE_TAG + "] message. Writing system time to device: " + currentTime);
            getFactory().getMeterInfo().setTime(currentTime);
        } catch (CTRException e) {
            throw new BusinessException("Unable to set the device time! " + e.getMessage());
        }
    }

    public static MessageSpec getMessageSpec(boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(MESSAGE_DESCRIPTION, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(MESSAGE_TAG);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

}
