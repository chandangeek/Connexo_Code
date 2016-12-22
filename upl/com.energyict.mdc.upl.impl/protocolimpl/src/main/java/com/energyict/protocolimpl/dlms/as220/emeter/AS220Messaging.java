package com.energyict.protocolimpl.dlms.as220.emeter;

import com.energyict.mdc.upl.messages.legacy.MessageAttribute;
import com.energyict.mdc.upl.messages.legacy.MessageAttributeSpec;
import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageSpec;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageTagSpec;
import com.energyict.mdc.upl.messages.legacy.MessageValueSpec;

import com.energyict.dlms.cosem.MBusClient;
import com.energyict.dlms.cosem.attributes.MbusClientAttributes;
import com.energyict.protocol.MessageResult;
import com.energyict.protocolimpl.base.AbstractSubMessageProtocol;
import com.energyict.protocolimpl.dlms.as220.AS220;
import com.energyict.protocolimpl.messages.RtuMessageKeyIdConstants;
import com.energyict.protocolimpl.messages.codetableparsing.CodeTableXml;
import com.energyict.protocolimpl.utils.MessagingTools;
import com.energyict.protocolimpl.utils.ProtocolTools;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
    public static final String WRITE_RAW_IEC1107_CLASS = "WriteRawIEC1107Class";
    public static final String DECOMMISSION_ALL_MBUS_DEVICES_TAG = "DecommissionAll";

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
    public static final String WRITE_RAW_IEC1107_CLASS_DISPLAY = "Write raw IEC1107 class";
    public static final String DECOMMISSION_ALL_MBUS_DEVICES = "Decommission all MBus clients";

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
        addSupportedMessageTag(WRITE_RAW_IEC1107_CLASS);
        addSupportedMessageTag(DECOMMISSION_ALL_MBUS_DEVICES_TAG);
    }

    public AS220 getAs220() {
        return as220;
    }

    public List<MessageCategorySpec> getMessageCategories() {
        List<MessageCategorySpec> categories = new ArrayList<>();
        MessageCategorySpec eMeterCat = new MessageCategorySpec("[01] E-Meter ");
        MessageCategorySpec otherMeterCat = new MessageCategorySpec("[03] Other");

        eMeterCat.addMessageSpec(createMessageSpec(DISCONNECT_EMETER_DISPLAY, DISCONNECT_EMETER, false));
        eMeterCat.addMessageSpec(createMessageSpec(CONNECT_EMETER_DISPLAY, CONNECT_EMETER, false));
        eMeterCat.addMessageSpec(createMessageSpec(DUMMY_MESSAGE_DISPLAY, DUMMY_MESSAGE, false));
        eMeterCat.addMessageSpec(AS220IEC1107AccessController.createWriteIEC1107ClassMessageSpec(WRITE_RAW_IEC1107_CLASS_DISPLAY, WRITE_RAW_IEC1107_CLASS, true));

        eMeterCat.addMessageSpec(addTimeOfUse(RtuMessageKeyIdConstants.ACTIVITYCALENDAR, ACTIVITY_CALENDAR, false));
        eMeterCat.addMessageSpec(createValueMessage(ACTIVATE_CALENDAR_DISPLAY, ACTIVATE_ACTIVITY_CALENDAR, false, ACTIVITY_CALENDAR_ACTIVATION_TIME));
        eMeterCat.addMessageSpec(createValueMessage(LOAD_LIMIT_THRESHOLD_DISPLAY, SET_LOADLIMIT_THRESHOLD, false, LOADLIMIT_THRESHOLD));
        eMeterCat.addMessageSpec(createValueMessage(LOAD_LIMIT_DURATION_DISPLAY, SET_LOADLIMIT_DURATION, false, LOADLIMIT_DURATION));

        otherMeterCat.addMessageSpec(createMessageSpec(FORCE_SET_CLOCK_DISPLAY, FORCE_SET_CLOCK, false));
        otherMeterCat.addMessageSpec(createMessageSpec(DECOMMISSION_ALL_MBUS_DEVICES, DECOMMISSION_ALL_MBUS_DEVICES_TAG, false));

        categories.add(eMeterCat);
        categories.add(otherMeterCat);
        return categories;
    }

    /**
     * Creates a MessageSpec to add ActivityCalendar functionality
     *
     * @param keyId    - id for the MessageSpec
     * @param tagName  - name for the MessageSpec
     * @param advanced - indicates whether it's an advanced message or not
     * @return the newly created MessageSpec
     */
    private MessageSpec addTimeOfUse(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageValueSpec(" "));
        tagSpec.add(new MessageAttributeSpec(CALENDAR_NAME, false));
        tagSpec.add(new MessageAttributeSpec(ACTIVATION_DATE, false));
        tagSpec.add(new MessageAttributeSpec(ACTIVITY_CALENDAR, false));
        msgSpec.add(tagSpec);
        return msgSpec;
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
            } else if (isMessageTag(SET_LOADLIMIT_THRESHOLD, messageEntry)) {
                getAs220().getLogger().info("Set LoadLimit threshold received");
                long threshold;
                try {
                    threshold = Long.valueOf(MessagingTools.getContentOfAttribute(messageEntry, LOADLIMIT_THRESHOLD));
                    if (threshold > 0xFFFFFF) {
                        threshold = 0xFFFFFF;
                    }
                } catch (NumberFormatException e) {
                    throw new IOException(MessagingTools.getContentOfAttribute(messageEntry, LOADLIMIT_THRESHOLD) + " is not a valid Threshold value.");
                }

                getAs220().geteMeter().getLoadLimitController().writeThresholdValue(threshold);
            } else if (isMessageTag(WRITE_RAW_IEC1107_CLASS, messageEntry)) {
                getAs220().getLogger().info("Write raw EIC1107 class received");
                new AS220IEC1107AccessController(getAs220()).executeMessage(messageEntry);
            } else if (isMessageTag(DECOMMISSION_ALL_MBUS_DEVICES_TAG, messageEntry)) {
                getAs220().getLogger().info("Decommissioning all MBus clients.");
                int clientsDecommissioned = decommissionAll();
                getAs220().getLogger().info("Successful decommissioned " + clientsDecommissioned + " MBus client(s).");

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

    private int decommissionAll() {
        int clientsDecommissioned = 0;
        for (int i = 0; i < 4; i++) {
            try {
                getAs220().getLogger().info("Decommissioning MBus client " + (i + 1));
                getMbusClient(i).deinstallSlave();
                clientsDecommissioned++;
                getAs220().getLogger().info("Successful decommissioned MBus client " + (i + 1));
            } catch (Exception e) {
                getAs220().getLogger().info("Failed to decommission MBus client " + (i + 1) + " - probably no MBus client connected. (Error message: " + e.getMessage() + ")");
            }
        }
        return clientsDecommissioned;
    }

    private MBusClient getMbusClient(int physicalAddress) throws IOException {
        if (getAs220().getActiveFirmwareVersion().isHigherOrEqualsThen("2")) {
            return getAs220().getCosemObjectFactory().getMbusClient(getAs220().getMeterConfig().getMbusClient(physicalAddress).getObisCode(), MbusClientAttributes.VERSION10);
        } else {
            return getAs220().getCosemObjectFactory().getMbusClient(getAs220().getMeterConfig().getMbusClient(physicalAddress).getObisCode(), MbusClientAttributes.VERSION9);
        }
    }

    @Override
    public String writeTag(final MessageTag msgTag) {
        if (msgTag.getName().equals(ACTIVITY_CALENDAR)) {
            StringBuilder builder = new StringBuilder();
            addOpeningTag(builder, msgTag.getName());
            long activationDate = 0;
            int codeTableId = -1;
            for (MessageAttribute ma : msgTag.getAttributes()) {
                if (ma.getSpec().getName().equals(ACTIVITY_CALENDAR_ACTIVATION_TIME)) {
                    if (ma.getValue() != null && !ma.getValue().isEmpty()) {
                        activationDate = Long.valueOf(ma.getValue());
                    }
                } else if (ma.getSpec().getName().equals(ACTIVITY_CALENDAR)) {
                    if (ma.getValue() != null && !ma.getValue().isEmpty()) {
                        codeTableId = Integer.valueOf(ma.getValue());
                    }
                }
            }

            try {
                String xmlContent = CodeTableXml.parseActivityCalendarAndSpecialDayTable(codeTableId, activationDate);
                builder.append(xmlContent);
            } catch (ParserConfigurationException e) {
                return null;
            }
            addClosingTag(builder, msgTag.getName());
            return builder.toString();
        } else {
            return super.writeTag(msgTag);
        }
    }

    private void addOpeningTag(StringBuilder builder, String tagName) {
        builder.append("<");
        builder.append(tagName);
        builder.append(">");
    }

    private void addClosingTag(StringBuilder builder, String tagName) {
        builder.append("</");
        builder.append(tagName);
        builder.append(">");
    }
}
