package com.elster.protocolimpl.dlms.messaging;

import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageSpec;
import com.energyict.mdc.upl.messages.legacy.MessageTagSpec;
import com.energyict.mdc.upl.messages.legacy.MessageValueSpec;

import com.elster.dlms.cosem.simpleobjectmodel.Ek280Defs;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleActivityCalendarObject;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleCosemObjectManager;
import com.elster.dlms.types.basic.DlmsDateTime;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 14/04/11
 * Time: 14:09
 */
public class TariffDisablePassiveMessage extends AbstractDlmsMessage {

    private static final String MESSAGE_TAG = "ClearPassiveTariff";
    private static final String MESSAGE_DESCRIPTION = "Clear and disable the passive tariff";

    public TariffDisablePassiveMessage(DlmsMessageExecutor messageExecutor) {
        super(messageExecutor);
    }

    @Override
    public boolean canExecuteThisMessage(MessageEntry messageEntry) {
        return isMessageTag(MESSAGE_TAG, messageEntry.getContent());
    }

    @Override
    public void executeMessage(MessageEntry messageEntry) throws IOException {
        try {
            disableTariff();
        } catch (IOException e) {
            throw new IOException("Unable to disable the tariff: " + e.getMessage(), e);
        }
    }

    private void disableTariff() throws IOException {

        SimpleCosemObjectManager objectManager = getExecutor().getDlms().getObjectManager();

        SimpleActivityCalendarObject activityCalendar = objectManager.getSimpleCosemObject(
                Ek280Defs.ACTIVITY_CALENDAR, SimpleActivityCalendarObject.class);

        activityCalendar.setActivatePassiveCalendarTime(DlmsDateTime.NOT_SPECIFIED_DATE_TIME);
    }

    public static MessageSpec getMessageSpec(boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(MESSAGE_DESCRIPTION, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(MESSAGE_TAG);
        tagSpec.add(new MessageValueSpec(" "));
        msgSpec.add(tagSpec);
        return msgSpec;
    }

}
