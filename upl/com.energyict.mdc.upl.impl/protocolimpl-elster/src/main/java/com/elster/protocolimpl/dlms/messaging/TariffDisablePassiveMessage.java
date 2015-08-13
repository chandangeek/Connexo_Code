package com.elster.protocolimpl.dlms.messaging;

import com.elster.dlms.cosem.simpleobjectmodel.Ek280Defs;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleActivityCalendarObject;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleCosemObjectManager;
import com.elster.dlms.types.basic.DlmsDateTime;
import com.energyict.cbo.BusinessException;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.MessageSpec;
import com.energyict.protocol.messaging.MessageTagSpec;
import com.energyict.protocol.messaging.MessageValueSpec;

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
    public void executeMessage(MessageEntry messageEntry) throws BusinessException {
        try {
            disableTariff();
        } catch (IOException e) {
            throw new BusinessException("Unable to disable the tariff: " + e.getMessage());
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
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

}
