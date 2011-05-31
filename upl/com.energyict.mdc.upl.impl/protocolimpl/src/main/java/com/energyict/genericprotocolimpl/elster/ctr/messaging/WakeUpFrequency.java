package com.energyict.genericprotocolimpl.elster.ctr.messaging;

import com.energyict.cbo.BusinessException;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException;
import com.energyict.genericprotocolimpl.elster.ctr.object.AbstractCTRObject;
import com.energyict.genericprotocolimpl.elster.ctr.object.CTRObjectFactory;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRObjectID;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.utils.MessagingTools;
import com.energyict.protocolimpl.utils.ProtocolTools;

import static com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType.getValueAndObjectId;
import static com.energyict.protocolimpl.utils.ProtocolTools.getBytesFromHexString;
import static com.energyict.protocolimpl.utils.ProtocolTools.isNumber;

/**
 * Copyrights EnergyICT
 * Date: 2/03/11
 * Time: 16:40
 */
public class WakeUpFrequency extends AbstractMTU155Message {

    public static final String OBJECT_ID = "E.7.0";

    private static final String MESSAGE_TAG = "WakeUpFrequency";
    private static final String MESSAGE_DESCRIPTION = "Change the wake up frequency";
    private static final String ATTR_PERIOD_IN_HOURS = "PeriodInHours";

    public WakeUpFrequency(MTU155MessageExecutor messageExecutor) {
        super(messageExecutor);
    }

    @Override
    public boolean canExecuteThisMessage(MessageEntry messageEntry) {
        return isMessageTag(MESSAGE_TAG, messageEntry.getContent());
    }

    @Override
    public void executeMessage(MessageEntry messageEntry) throws BusinessException {
        String periodInHoursAttr = MessagingTools.getContentOfAttribute(messageEntry, ATTR_PERIOD_IN_HOURS);
        int periodInHours = validateAttributes(periodInHoursAttr);
        try {
            writeWakeUp(periodInHours);
        } catch (CTRException e) {
            throw new BusinessException("Unable to change the wake up period.", e);
        }
    }

    private int validateAttributes(String periodInHoursAttr) throws BusinessException {
        if (periodInHoursAttr == null) {
            throw new BusinessException("Unable to change wake up frequency. The period cannot be 'null'.");
        }

        periodInHoursAttr = periodInHoursAttr.trim();

        if (!isNumber(periodInHoursAttr)) {
            throw new BusinessException("Unable to change wake up frequency. The period should be a number but was [" + periodInHoursAttr + "].");
        }

        if (!isNumber(periodInHoursAttr)) {
            throw new BusinessException("Unable to change wake up frequency. The period should be a number but was [" + periodInHoursAttr + "].");
        }

        int value;
        try {
            value = Integer.valueOf(periodInHoursAttr);
        } catch (NumberFormatException e) {
            throw new BusinessException("Invalid period [" + periodInHoursAttr + "].", e);
        }

        int[] allowed = new int[]{1, 2, 4, 6, 8, 12};
        for (int i : allowed) {
            if (i == value) {
                return i;
            }
        }
        throw new BusinessException("Invalid period [" + periodInHoursAttr + "]. Only 1, 2, 4, 6, 8 and 12 are allowed.");
    }

    private void writeWakeUp(int periodInHours) throws CTRException {
        int pOff = ((periodInHours * 60) - 2) * 60;
        byte[] period = new byte[2];
        period[0] = (byte) ((pOff >> 8) & 0x0FF);
        period[1] = (byte) (pOff & 0x0FF);

        byte[] rawData = new CTRObjectID(OBJECT_ID).getBytes();
        rawData = ProtocolTools.concatByteArrays(rawData, getBytesFromHexString("0078", ""));
        rawData = ProtocolTools.concatByteArrays(rawData, period);
        rawData = ProtocolTools.concatByteArrays(rawData, getBytesFromHexString("00000000", ""));
        rawData = ProtocolTools.concatByteArrays(rawData, getBytesFromHexString("000000000000000000", ""));

        CTRObjectFactory objectFactory = new CTRObjectFactory();
        AbstractCTRObject object = objectFactory.parse(rawData, 0, getValueAndObjectId());
        getFactory().writeRegister(object);

    }

    public static MessageSpec getMessageSpec(boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(MESSAGE_DESCRIPTION, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(MESSAGE_TAG);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");
        tagSpec.add(new MessageAttributeSpec(ATTR_PERIOD_IN_HOURS, true));
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

}
