package com.energyict.protocolimpl.dlms.as220.emeter;

import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.base.AbstractSubMessageProtocol;
import com.energyict.protocolimpl.dlms.as220.AS220;
import com.energyict.protocolimpl.utils.MessagingTools;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.*;

public class AS220Messaging extends AbstractSubMessageProtocol {

    /**
     * Message tags
     */
    public static final String CONNECT_EMETER = "ConnectEmeter";
    public static final String DISCONNECT_EMETER = "DisconnectEmeter";
    public static final String ARM_EMETER = "ArmEmeter";

    public static final String FORCE_SET_CLOCK = "ForceSetClock";

    public static final String FIRMWARE_UPDATE = "FirmwareUpdate";
    public static final String DUMMY_MESSAGE = "DummyMessage";

    public static final String ACTIVITY_CALENDAR = "TimeOfUse";
    public static final String ACTIVATION_DATE = "ActivationDate";
    public static final String CALENDAR_NAME = "CalendarName";
    public static final String ACTIVATE_ACTIVITY_CALENDAR = "ActivatePassiveCalendar";
    public static final String ACTIVITY_CALENDAR_ACTIVATION_TIME = "ActivationTime";

    public static final String SET_LOADLIMIT_THRESHOLD = "SetLoadLimitThreshold";
    public static final String SET_LOADLIMIT_DURATION = "SetLoadLimitDuration";
    public static final String LOADLIMIT_DURATION = "LoadLimitDuration";
    public static final String LOADLIMIT_THRESHOLD = "LoadLimitThreshold";


    /**
     * Message descriptions
     */
    private static final String CONNECT_EMETER_DISPLAY = "Remote connect";
    private static final String DISCONNECT_EMETER_DISPLAY = "Remote disconnect";
    private static final String DUMMY_MESSAGE_DISPLAY = "Dummy message";
    public static final String FORCE_SET_CLOCK_DISPLAY = "Force set clock";
    public static final String ACTIVATE_CALENDAR_DISPLAY = "Activate Activity Calendar";
    public static final String LOAD_LIMIT_THRESHOLD_DISPLAY = "Set LoadLimit threshold";
    public static final String LOAD_LIMIT_DURATION_DISPLAY = "Set LoadLimit duration";

    //private static final String	ARM_EMETER_DISPLAY				= "Arm E-Meter";

    private final AS220 as220;

    public AS220Messaging(AS220 as220) {
        this.as220 = as220;
        addSupportedMessageTag(CONNECT_EMETER);
        addSupportedMessageTag(DISCONNECT_EMETER);
        addSupportedMessageTag(ARM_EMETER);
        addSupportedMessageTag(FORCE_SET_CLOCK);
        addSupportedMessageTag(FIRMWARE_UPDATE);
        addSupportedMessageTag(DUMMY_MESSAGE);
        addSupportedMessageTag(ACTIVITY_CALENDAR);
        addSupportedMessageTag(ACTIVATE_ACTIVITY_CALENDAR);
        addSupportedMessageTag(SET_LOADLIMIT_DURATION);
        addSupportedMessageTag(SET_LOADLIMIT_THRESHOLD);
    }

    public AS220 getAs220() {
        return as220;
    }

    public List<MessageCategorySpec> getMessageCategories() {
        List<MessageCategorySpec> categories = new ArrayList<MessageCategorySpec>();
        MessageCategorySpec eMeterCat = new MessageCategorySpec("[01] E-Meter ");
        MessageCategorySpec otherMeterCat = new MessageCategorySpec("[03] Other");

        eMeterCat.addMessageSpec(createMessageSpec(DISCONNECT_EMETER_DISPLAY, DISCONNECT_EMETER, false));
        //eMeterCat.addMessageSpec(createMessageSpec(ARM_EMETER_DISPLAY, ARM_EMETER, false));
        eMeterCat.addMessageSpec(createMessageSpec(CONNECT_EMETER_DISPLAY, CONNECT_EMETER, false));
        eMeterCat.addMessageSpec(createMessageSpec(DUMMY_MESSAGE_DISPLAY, DUMMY_MESSAGE, false));

        eMeterCat.addMessageSpec(createValueMessage(ACTIVATE_CALENDAR_DISPLAY, ACTIVATE_ACTIVITY_CALENDAR, false, ACTIVITY_CALENDAR_ACTIVATION_TIME));
        eMeterCat.addMessageSpec(createValueMessage(LOAD_LIMIT_THRESHOLD_DISPLAY, SET_LOADLIMIT_THRESHOLD, false, LOADLIMIT_THRESHOLD));
        eMeterCat.addMessageSpec(createValueMessage(LOAD_LIMIT_DURATION_DISPLAY, SET_LOADLIMIT_DURATION, false, LOADLIMIT_DURATION));

        otherMeterCat.addMessageSpec(createMessageSpec(FORCE_SET_CLOCK_DISPLAY, FORCE_SET_CLOCK, false));

        categories.add(eMeterCat);
        categories.add(otherMeterCat);
        return categories;
    }

    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        MessageResult result;

        try {

            if (isMessageTag(DISCONNECT_EMETER, messageEntry)) {
                getAs220().geteMeter().getContactorController().doDisconnect();
            } else if (isMessageTag(CONNECT_EMETER, messageEntry)) {
                getAs220().geteMeter().getContactorController().doConnect();
            } else if (isMessageTag(ARM_EMETER, messageEntry)) {
                getAs220().geteMeter().getContactorController().doArm();
            } else if (isMessageTag(FORCE_SET_CLOCK, messageEntry)) {
                getAs220().getLogger().info("FORCE_SET_CLOCK message received");
                getAs220().geteMeter().getClockController().setTime();
            } else if (isMessageTag(FIRMWARE_UPDATE, messageEntry)) {
                getAs220().getLogger().info("FIRMWARE_UPDATE message received");
                AS220ImageTransfer imageTransfer = new AS220ImageTransfer(this, messageEntry);
                imageTransfer.initiate();
                imageTransfer.upgrade();
                imageTransfer.activate();
            } else if (isMessageTag(DUMMY_MESSAGE, messageEntry)) {
                getAs220().getLogger().info("DUMMY_MESSAGE message received");
            } else if (isMessageTag(ACTIVITY_CALENDAR, messageEntry)) {
                getAs220().getLogger().info("Update Activity calendar received");
                getAs220().geteMeter().getActivityCalendarController().parseContent(messageEntry.getContent());
                getAs220().geteMeter().getActivityCalendarController().writeCalendar();
            } else if (isMessageTag(ACTIVATE_ACTIVITY_CALENDAR, messageEntry)) {
                getAs220().getLogger().info("Activate Passive Calendar received");
                Calendar cal = getActivationCalendar(messageEntry);
                getAs220().geteMeter().getActivityCalendarController().writeCalendarActivationTime(cal);
            } else if (isMessageTag(SET_LOADLIMIT_DURATION, messageEntry)) {
                getAs220().getLogger().info("Set LoadLimit duration received");
                try {
                getAs220().geteMeter().getLoadLimitController().writeThresholdOverDuration(Integer.valueOf(MessagingTools.getContentOfAttribute(messageEntry, LOADLIMIT_DURATION)));
                } catch (NumberFormatException e) {
                    throw new IOException(MessagingTools.getContentOfAttribute(messageEntry, SET_LOADLIMIT_DURATION) + " is not a valid Threshold duration.");
                }
            } else if (isMessageTag(SET_LOADLIMIT_THRESHOLD, messageEntry)){
                getAs220().getLogger().info("Set LoadLimit threshold received");
                long threshold = 0;
                try {
                    threshold = Long.valueOf(MessagingTools.getContentOfAttribute(messageEntry, LOADLIMIT_THRESHOLD));
                    if(threshold > 0xFFFFFF){
                        threshold = 0xFFFFFF;
                    }
                } catch (NumberFormatException e) {
                    throw new IOException(MessagingTools.getContentOfAttribute(messageEntry, LOADLIMIT_THRESHOLD) + " is not a valid Threshold value.");
                }

                getAs220().geteMeter().getLoadLimitController().writeThresholdValue(threshold);
            } else {
                throw new IOException("Received unknown message: " + messageEntry);
            }

            result = MessageResult.createSuccess(messageEntry);

        } catch (IOException e) {
            getAs220().getLogger().severe("QueryMessage(), FAILED: " + e.getMessage());
            result = MessageResult.createFailed(messageEntry);
        }

        return result;
    }

    private Calendar getActivationCalendar(MessageEntry msgEntry) {
        String timeInSeconds = ProtocolTools.getEpochTimeFromString(MessagingTools.getContentOfAttribute(msgEntry, ACTIVITY_CALENDAR_ACTIVATION_TIME));
        Calendar calendar = Calendar.getInstance(this.as220.getTimeZone());
        calendar.setTimeInMillis(Long.valueOf(timeInSeconds) * 1000);
        return calendar;
    }

}
