package com.energyict.protocolimplv2.ace4000;

import com.elster.jupiter.calendar.Calendar;
import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.MessageResult;
import com.energyict.mdc.protocol.api.device.data.MeterData;
import com.energyict.mdc.protocol.api.device.data.MeterDataMessageResult;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.events.MeterProtocolEvent;
import com.energyict.mdc.protocol.api.messaging.Message;
import com.energyict.mdc.protocol.api.messaging.MessageAttribute;
import com.energyict.mdc.protocol.api.messaging.MessageAttributeSpec;
import com.energyict.mdc.protocol.api.messaging.MessageCategorySpec;
import com.energyict.mdc.protocol.api.messaging.MessageElement;
import com.energyict.mdc.protocol.api.messaging.MessageSpec;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageTagSpec;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.mdc.protocol.api.messaging.MessageValueSpec;
import com.energyict.mdc.protocol.api.tasks.support.DeviceLoadProfileSupport;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.offline.OfflineLoadProfile;
import com.energyict.protocolimplv2.ace4000.requests.ContactorCommand;
import com.energyict.protocolimplv2.ace4000.requests.FirmwareUpgrade;
import com.energyict.protocolimplv2.ace4000.requests.ReadLoadProfile;
import com.energyict.protocolimplv2.ace4000.requests.ReadMeterEvents;
import com.energyict.protocolimplv2.ace4000.requests.WriteConfiguration;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ACE4000MessageExecutor implements MessageProtocol {

    private static final String READ_EVENTS = "ReadEvents";
    private static final String READ_PROFILE_DATA = "ReadProfileData";
    private static final String FIRMWARE_UPGRADE = "FirmwareUpgrade";
    private static final String CONNECT = "CloseContactor";
    private static final String DISCONNECT = "Disconnect";
    private static final String SHORT_DISPLAY_MESSAGE = "ShortDisplayMessage";
    private static final String LONG_DISPLAY_MESSAGE = "LongDisplayMessage";
    private static final String NO_DISPLAY_MESSAGE = "NoDisplayMessage";
    private static final String DISPLAY_CONFIG = "DisplayConfiguration";
    private static final String CONFIG_LOADPROFILE = "ConfigureLoadProfile";
    private static final String CONFIG_SPECIAL_DATA_MODE = "ConfigureSpecialDataMode";
    private static final String CONFIG_MAX_DEMAND = "ConfigureMaxDemand";
    private static final String CONFIG_CONSUMPTION_LIMITATION = "ConfigureConsumptionLimitation";
    private static final String CONFIG_EMERGENCY_CONSUMPTION_LIMITATION = "ConfigureEmergencyConsumptionLimitation";
    private static final String CONFIG_TARIFF = "TariffConfig";

    private final ACE4000Outbound ace4000;
    private final Clock clock;
    private final IssueService issueService;

    public ACE4000MessageExecutor(ACE4000Outbound ace4000, Clock clock, IssueService issueService) {
        this.ace4000 = ace4000;
        this.clock = clock;
        this.issueService = issueService;
    }

    /**
     * Execute a message. The request is wrapped with a retry mechanism and proper timeout handling.
     * If a NAK is received, it is logged in the issue/problem.
     */
    public MessageResult executeMessage(MessageEntry messageEntry) {
        String messageContent = messageEntry.getContent();
        try {
            if (messageContent.contains(READ_EVENTS)) {
                //TODO what if the offline device has no logbooks configured??
                ReadMeterEvents readMeterEventsRequest = new ReadMeterEvents(ace4000, issueService);
                List<CollectedLogBook> collectedLogBooks = readMeterEventsRequest.request(ace4000.getOfflineDevice().getAllOfflineLogBooks().get(0).getLogBookIdentifier());
                MeterData meterData = new MeterData();
                for (MeterProtocolEvent collectedMeterEvent : collectedLogBooks.get(0).getCollectedMeterEvents()) {
                    meterData.addMeterEvent(collectedMeterEvent);
                }
                return MeterDataMessageResult.createSuccess(messageEntry, "", meterData);
            } else if (messageContent.contains(READ_PROFILE_DATA)) {
                List<CollectedLoadProfile> collectedLoadProfiles = readProfileData(messageContent);
                CollectedLoadProfile collectedLoadProfile = collectedLoadProfiles.get(0);
                MeterData meterData = new MeterData();
                ProfileData profileData = new ProfileData();
                for (ChannelInfo channelInfo : collectedLoadProfile.getChannelInfo()) {
                    profileData.addChannel(channelInfo);
                }
                for (IntervalData intervalData : collectedLoadProfile.getCollectedIntervalData()) {
                    profileData.addInterval(intervalData);
                }
                meterData.addProfileData(profileData);
                return MeterDataMessageResult.createSuccess(messageEntry, "", meterData);
            } else if (messageContent.contains(FIRMWARE_UPGRADE)) {
                FirmwareUpgrade firmwareUpgrade = new FirmwareUpgrade(ace4000, issueService);
                return firmwareUpgrade.request(messageEntry);
            } else if (messageContent.contains(CONNECT)) {
                ContactorCommand contactorCommand = new ContactorCommand(ace4000, issueService);
                contactorCommand.setCommand(0);
                return contactorCommand.request(messageEntry);
            } else if (messageContent.contains(DISCONNECT)) {
                ContactorCommand contactorCommand = new ContactorCommand(ace4000, issueService);
                contactorCommand.setCommand(1);
                return contactorCommand.request(messageEntry);
            } else {
                WriteConfiguration writeConfiguration = new WriteConfiguration(ace4000, issueService);
                return writeConfiguration.request(messageEntry);
            }
        } catch (Exception e) {
            return MessageResult.createFailed(messageEntry, "Error parsing/executing message: " + e.getMessage());
        }
    }

    public Integer sendConfigurationMessage(MessageEntry messageEntry) {
        String messageContent = messageEntry.getContent();
        if (messageContent.contains(SHORT_DISPLAY_MESSAGE)) {
            sendDisplayMessage(messageContent, 1);
        } else if (messageContent.contains(LONG_DISPLAY_MESSAGE)) {
            sendDisplayMessage(messageContent, 2);
        } else if (messageContent.contains(NO_DISPLAY_MESSAGE)) {
            sendDisplayMessage(messageContent, 0);
        } else if (messageContent.contains(DISPLAY_CONFIG)) {
            sendDisplayConfigRequest(messageContent);
        } else if (messageContent.contains(CONFIG_LOADPROFILE)) {
            sendLoadProfileConfigurationRequest(messageContent);
        } else if (messageContent.contains(CONFIG_SPECIAL_DATA_MODE)) {
            sendSDMConfigurationRequest(messageContent);
        } else if (messageContent.contains(CONFIG_MAX_DEMAND)) {
            sendMaxDemandConfigurationRequest(messageContent);
        } else if (messageContent.contains(CONFIG_CONSUMPTION_LIMITATION)) {
            sendConsumptionLimitationConfiguration(messageContent);
        } else if (messageContent.contains(CONFIG_EMERGENCY_CONSUMPTION_LIMITATION)) {
            sendEmergencyConsumptionLimitationConfiguration(messageContent);
        } else if (messageContent.contains(CONFIG_TARIFF)) {
            sendTariffConfiguration(messageContent);
        } else {
            return null;         //Unknown message, handle properly later on
        }
        return ace4000.getObjectFactory().getTrackingID();
    }

    public List getMessageCategories() {
        List<MessageCategorySpec> categories = new ArrayList<>();

        MessageCategorySpec cat1 = new MessageCategorySpec("ACE4000 general messages");
        cat1.addMessageSpec(addBasicMsgWithValues("Firmware upgrade", FIRMWARE_UPGRADE, false, "URL path (start with http://)", "Size of the JAR file (bytes)", "Size of the JAD file (bytes)"));
        cat1.addMessageSpec(addBasicMsgWithValues("Connect (close contactor)", CONNECT, false, "Optional date (dd/mm/yyyy hh:mm:ss)"));
        cat1.addMessageSpec(addBasicMsgWithValues("Disconnect (open contactor)", DISCONNECT, false, "Optional date (dd/mm/yyyy hh:mm:ss)"));
        categories.add(cat1);

        MessageCategorySpec cat2 = new MessageCategorySpec("ACE4000 configuration messages");
        cat2.addMessageSpec(addBasicMsgWithValue("Send short display message (max 8 chars)", SHORT_DISPLAY_MESSAGE, false));
        cat2.addMessageSpec(addBasicMsgWithValue("Send long display message (max 1024 chars)", LONG_DISPLAY_MESSAGE, false));
        cat2.addMessageSpec(addBasicMsg("Disable the display message", NO_DISPLAY_MESSAGE, false));
        cat2.addMessageSpec(addBasicMsgWithValues("Configure LCD display", DISPLAY_CONFIG, false, "Number of digits before comma (allowed: 5, 6 or 7)", "Number of digits after comma (allowed: 0, 1, 2 or 3)", "Display sequence (comma separated hex values, e.g.: 1,2,E,12,1A)", "Display cycle time (seconds)"));
        cat2.addMessageSpec(addBasicMsgWithValues("Configure load profile data recording", CONFIG_LOADPROFILE, false, "Enable (1) or disable (0)", "Interval (1, 2, 3, 5, 6, 10, 12, 15, 20, 30, 60, 120 or 240 minutes)", "Maximum number of records (min 1, max 65535)"));
        cat2.addMessageSpec(addBasicMsgWithValues("Configure special data mode", CONFIG_SPECIAL_DATA_MODE, false, "Special data mode duration (days)", "Special data mode activation date (dd/mm/yyyy)", "Special billing register recording: enable (1) or disable (0)", "Special billing register recording: interval (0: hourly, 1: daily, 2: monthly)", "Special billing register recording: max number of records (min 1, max 65535)", "Special load profile: enable (1) or disable (0)", "Special load profile: interval (1, 2, 3, 5, 6, 10, 12, 15, 20, 30, 60, 120 or 240 minutes)", "Special load profile: max number of records (min 1, max 65535)"));
        cat2.addMessageSpec(addBasicMsgWithValues("Configure maximum demand settings", CONFIG_MAX_DEMAND, false, "Active registers (0) or reactive registers (1)", "Number of sub intervals (0, 1, 2, 3, 4, 5, 10 or 15)", "Sub interval duration (30, 60, 300, 600, 900, 1200, 1800 or 3600 seconds)"));
        cat2.addMessageSpec(addBasicMsgWithValues("Configure consumption limitation settings", CONFIG_CONSUMPTION_LIMITATION, false, "Number of sub intervals (0, 1, 2, 3, 4, 5, 10 or 15)", "Sub interval duration (30, 60, 300, 600, 900, 1200, 1800 or 3600 seconds)", "Override rate (0: disabled)", "Allowed excess tolerance (0 - 100 %)", "Threshold selection (0: day profile, 1: maximum threshold)", "8 switching moments for daily profile 0 (comma separated, e.g.: 01:00,05:00,...)", "8 thresholds for daily profile 0 (comma separated)", "8 units for the thresholds (comma separated) (0: Watt, 1: Ampere)", "8 actions (in hex) for daily profile 0 (comma separated)", "8 switching moments for daily profile 1 (comma separated, e.g.: 01:00,05:00,...)", "8 thresholds for daily profile 1 (comma separated)", "8 units for the thresholds (comma separated) (0: Watt, 1: Ampere)", "8 actions (in hex) for daily profile 1 (comma separated)", "Day profiles used for each day of the week (comma separated)", "Activation date (dd/mm/yyyy hh:mm:ss) (optional)"));
        cat2.addMessageSpec(addBasicMsgWithValues("Configure emergency consumption limitation settings", CONFIG_EMERGENCY_CONSUMPTION_LIMITATION, false, "Duration (minutes)", "Threshold value", "Threshold unit (0: Watt, 1: Ampere)", "Override rate (0: disabled)"));
        cat2.addMessageSpec(addBasicMsgWithValues("Configure tariff settings", CONFIG_TARIFF, false, "Unique tariff ID number", "Number of tariff rates (max 4)", "Code table ID"));
        categories.add(cat2);

        return categories;
    }

    protected MessageSpec addBasicMsgWithValue(final String keyId, final String tagName, final boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageValueSpec());
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    protected MessageSpec addBasicMsg(final String keyId, final String tagName, final boolean advanced) {
        final MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        final MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    protected MessageSpec addBasicMsgWithValues(final String keyId, final String tagName, final boolean advanced, String... attributes) {
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        for (String attribute : attributes) {
            tagSpec.add(new MessageAttributeSpec(attribute, true));
        }
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" "); //Disable this field
        tagSpec.add(msgVal);
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    private String stripOffTag(String content) {
        return content.substring(content.indexOf(">") + 1, content.lastIndexOf("<"));
    }

    public String writeMessage(Message msg) {
        return msg.write(this);
    }

    public String writeTag(MessageTag msgTag) {
        StringBuilder builder = new StringBuilder();

        // a. Opening tag
        builder.append("<");
        builder.append(msgTag.getName());

        // b. Attributes
        for (Object o1 : msgTag.getAttributes()) {
            MessageAttribute att = (MessageAttribute) o1;
            if (att.getValue() == null || att.getValue().isEmpty()) {
                continue;
            }
            builder.append(" ").append(att.getSpec().getName());
            builder.append("=").append('"').append(att.getValue()).append('"');
        }
        builder.append(">");

        // c. sub elements
        for (Object o : msgTag.getSubElements()) {
            MessageElement elt = (MessageElement) o;
            if (elt.isTag()) {
                builder.append(writeTag((MessageTag) elt));
            } else if (elt.isValue()) {
                String value = writeValue((MessageValue) elt);
                if (value == null || value.isEmpty()) {
                    return "";
                }
                builder.append(value);
            }
        }

        // d. Closing tag
        builder.append("</");
        builder.append(msgTag.getName());
        builder.append(">");

        return builder.toString();
    }

    public String writeValue(MessageValue value) {
        return value.getValue();
    }

    public void applyMessages(List messageEntries) throws IOException {
    }

    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return null;    //Not used
    }

    private void sendTariffConfiguration(String messageContent) {
        String[] parts = messageContent.split("=");
        int number = Integer.parseInt(parts[1].substring(1).split("\"")[0]);
        int numberOfRates = Integer.parseInt(parts[2].substring(1).split("\"")[0]);

        //TODO: Calendar from message entry ?????
        Calendar calendar = null;

        if (numberOfRates > 4 || numberOfRates < 0) {
            failMessage("Tariff configuration failed, invalid number of rates");
            return;
        }

        try {
            ace4000.getObjectFactory().sendTariffConfiguration(number, numberOfRates, calendar);
        } catch (ApplicationException e) {  //Thrown while parsing the code table
            failMessage("Tariff configuration failed, invalid code table settings");
        }
    }

    private void sendLoadProfileConfigurationRequest(String messageContent) {
        String[] parts = messageContent.split("=");
        int enable = Integer.parseInt(parts[1].substring(1).split("\"")[0]);
        int intervalInMinutes = Integer.parseInt(parts[2].substring(1).split("\"")[0]);
        int maxNumberOfRecords = Integer.parseInt(parts[3].substring(1).split("\"")[0]);
        if (enable != 0 && enable != 1) {
            failMessage("Load profile configuration message failed, invalid arguments");
            return;
        }
        if (intervalInMinutes != 1 && intervalInMinutes != 2 && intervalInMinutes != 3 && intervalInMinutes != 5 && intervalInMinutes != 6 && intervalInMinutes != 0x0A && intervalInMinutes != 0x0C && intervalInMinutes != 0x0F && intervalInMinutes != 0x14 && intervalInMinutes != 0x1E && intervalInMinutes != 0x3C && intervalInMinutes != 0x78 && intervalInMinutes != 0xF0) {
            failMessage("Load profile configuration message failed, invalid arguments");
            return;
        }
        ace4000.getObjectFactory().sendLoadProfileConfiguration(enable, intervalInMinutes, maxNumberOfRecords);
    }

    private List<CollectedLoadProfile> readProfileData(String messageContent) {
        String[] parts = messageContent.split("=");
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Instant fromDate = null;
        Instant toDate = null;

        if (parts.length > 1) {
            try {
                String fromDateString = parts[1].substring(1, 20);
                fromDate = formatter.parse(fromDateString).toInstant();
            } catch (Exception e) {
                failMessage("Request for load profile data failed, invalid arguments");
                return null;
            }
        }
        if (parts.length > 2) {
            try {
                String toDateString = parts[2].substring(1, 20);
                toDate = formatter.parse(toDateString).toInstant();
            } catch (Exception e) {
                toDate = this.clock.instant();
            }
        }
        if (toDate == null) {
            toDate = this.clock.instant();
        }
        if (toDate.isAfter(this.clock.instant())) {
            toDate = this.clock.instant();
        }
        if (fromDate != null && !fromDate.isBefore(toDate)) {
            failMessage("Request for load profile data failed, invalid arguments");
            return null;
        }

        //Make a new loadProfileReader with the proper from and to date
        LoadProfileReader loadProfileReader = null;
        for (OfflineLoadProfile offlineLoadProfile : ace4000.getOfflineDevice().getMasterOfflineLoadProfiles()) {
            if (offlineLoadProfile.getObisCode().equals(DeviceLoadProfileSupport.GENERIC_LOAD_PROFILE_OBISCODE)) {
                loadProfileReader = new LoadProfileReader(
                        this.clock,
                        offlineLoadProfile.getObisCode(),
                        fromDate,
                        toDate,
                        offlineLoadProfile.getLoadProfileId(),
                        loadProfileReader.getDeviceIdentifier(),
                        new ArrayList<>(),
                        loadProfileReader.getMeterSerialNumber(),
                        loadProfileReader.getLoadProfileIdentifier());
            }
        }

        ReadLoadProfile readLoadProfileRequest = new ReadLoadProfile(ace4000, issueService);
        return readLoadProfileRequest.request(loadProfileReader);
    }

    private void sendDisplayMessage(String messageContent, int mode) {
        String message = "";
        if (mode != 0) {
            message = stripOffTag(messageContent);
        }
        if (mode == 1 && message.length() > 8) {
            failMessage("Display message failed, invalid arguments");
        } else if (mode == 2 && message.length() > 1024) {
            failMessage("Display message failed, invalid arguments");
        } else {
            ace4000.getObjectFactory().sendDisplayMessage(mode, message);
        }
    }

    private void sendDisplayConfigRequest(String messageContent) {
        String[] parts = messageContent.split("=");
        int number1 = Integer.parseInt(parts[1].substring(1).split("\"")[0]);
        int number2 = Integer.parseInt(parts[2].substring(1).split("\"")[0]);
        String sequence = parts[3].substring(1).split("\"")[0];
        int intervalInSeconds = Integer.parseInt(parts[4].substring(1).split("\"")[0]);

        int resolutionCode = convertToResolutionCode(number1, number2);
        if (resolutionCode == -1) {
            failMessage("Display configuration request failed, invalid arguments");
            return;
        }

        String[] sequenceCodes = sequence.split(",");
        String sequenceResult = "";
        try {
            for (String code : sequenceCodes) {
                if (code.length() != 1 && code.length() != 2) {
                    failMessage("Display configuration request failed, invalid arguments");
                    return;
                }
                sequenceResult += ((code.length() == 1) ? "0" : "") + code;
            }
        } catch (Exception e) {
            failMessage("Display configuration request failed, invalid arguments");
            return;
        }

        ace4000.getObjectFactory().sendDisplayConfigurationRequest(resolutionCode, sequenceResult, sequence, intervalInSeconds);
    }

    private int convertToResolutionCode(int number1, int number2) {
        if (number1 == 5) {
            switch (number2) {
                case 0:
                    return 5;
                case 1:
                    return 0;
                case 2:
                    return 4;
                case 3:
                    return 3;
            }
        }
        if (number1 == 6) {
            switch (number2) {
                case 0:
                    return 1;
                case 1:
                    return 2;
            }
        }
        if (number1 == 7) {
            switch (number2) {
                case 0:
                    return 6;
                case 1:
                    return 7;
            }
        }
        return -1;
    }

    private void sendMaxDemandConfigurationRequest(String messageContent) {
        String[] parts = messageContent.split("=");
        int register = Integer.parseInt(parts[1].substring(1).split("\"")[0]);
        int numberOfSubIntervals = Integer.parseInt(parts[2].substring(1).split("\"")[0]);
        int subIntervalDuration = Integer.parseInt(parts[3].substring(1).split("\"")[0]);

        if (convertToNumberOfSubIntervalsCode(numberOfSubIntervals) == null) {
            failMessage("Max demand configuration message failed, invalid arguments");
            return;
        }
        if (convertToSubIntervalDurationCode(subIntervalDuration) == null) {
            failMessage("Max demand configuration message failed, invalid arguments");
            return;
        }

        ace4000.getObjectFactory().sendMaxDemandConfiguration(register, convertToNumberOfSubIntervalsCode(numberOfSubIntervals), convertToSubIntervalDurationCode(subIntervalDuration));
    }

    private String pad(String text) {
        if (text.length() == 1) {
            text = "0" + text;
        }
        return text;
    }

    private void sendConsumptionLimitationConfiguration(String messageContent) {
        String[] parts = messageContent.split("=");
        int numberOfSubIntervals = Integer.parseInt(parts[1].substring(1).split("\"")[0]);
        int subIntervalDuration = Integer.parseInt(parts[2].substring(1).split("\"")[0]);
        String failMsg = "Consumption limitation configuration message failed, invalid arguments";
        if (convertToNumberOfSubIntervalsCode(numberOfSubIntervals) == null) {
            failMessage(failMsg);
            return;
        }
        if (convertToSubIntervalDurationCode(subIntervalDuration) == null) {
            failMessage(failMsg);
            return;
        }
        int ovl = Integer.parseInt(parts[3].substring(1).split("\"")[0]);
        if (ovl < 0 || ovl > 4) {
            failMessage(failMsg);
            return;
        }
        int thresholdTolerance = Integer.parseInt(parts[4].substring(1).split("\"")[0]);
        if (thresholdTolerance < 0 || thresholdTolerance > 100) {
            failMessage(failMsg);
            return;
        }
        int thresholdSelection = Integer.parseInt(parts[5].substring(1).split("\"")[0]);
        if (thresholdSelection < 0 || thresholdSelection > 1) {
            failMessage(failMsg);
            return;
        }
        List<String> switchingTimesDP0 = new ArrayList<>();
        try {
            String[] switchingTimesStrings = parts[6].substring(1).split("\"")[0].split(",");
            for (String switchingTime : switchingTimesStrings) {
                int hour = Integer.parseInt(switchingTime.split(":")[0]);
                int minute = Integer.parseInt(switchingTime.split(":")[1]);
                switchingTimesDP0.add(pad(Integer.toString(hour, 16)) + pad(Integer.toString(minute, 16)));
            }
        } catch (Exception e) {
            failMessage(failMsg);
            return;
        }
        if (switchingTimesDP0.size() != 8) {
            failMessage(failMsg);
            return;
        }

        List<Integer> thresholdsDP0 = new ArrayList<>();
        try {
            String[] thresholdStrings = parts[7].substring(1).split("\"")[0].split(",");
            for (String thresholdString : thresholdStrings) {
                int threshold = Integer.parseInt(thresholdString);
                if (threshold < 0) {
                    failMessage(failMsg);
                    return;
                }
                thresholdsDP0.add(threshold);
            }
        } catch (Exception e) {
            failMessage(failMsg);
            return;
        }
        if (thresholdsDP0.size() != 8) {
            failMessage(failMsg);
            return;
        }

        List<Integer> unitsDP0 = new ArrayList<>();
        try {
            String[] unitStrings = parts[8].substring(1).split("\"")[0].split(",");
            for (String threshold : unitStrings) {
                unitsDP0.add(Integer.parseInt(threshold));
            }
        } catch (Exception e) {
            failMessage(failMsg);
            return;
        }
        if (unitsDP0.size() != 8) {
            failMessage(failMsg);
            return;
        }

        List<String> actionsDP0 = new ArrayList<>();
        try {
            String[] actionStrings = parts[9].substring(1).split("\"")[0].split(",");
            for (String action : actionStrings) {
                actionsDP0.add(pad(action));
            }
        } catch (Exception e) {
            failMessage(failMsg);
            return;
        }
        if (actionsDP0.size() != 8) {
            failMessage(failMsg);
            return;
        }
        List<String> switchingTimesDP1 = new ArrayList<>();
        try {
            String[] switchingTimesStrings = parts[10].substring(1).split("\"")[0].split(",");
            for (String switchingTime : switchingTimesStrings) {
                int hour = Integer.parseInt(switchingTime.split(":")[0]);
                int minute = Integer.parseInt(switchingTime.split(":")[1]);
                switchingTimesDP1.add(pad(Integer.toString(hour, 16)) + pad(Integer.toString(minute, 16)));
            }
        } catch (Exception e) {
            failMessage(failMsg);
            return;
        }
        if (switchingTimesDP1.size() != 8) {
            failMessage(failMsg);
            return;
        }

        List<Integer> thresholdsDP1 = new ArrayList<>();
        try {
            String[] thresholdStrings = parts[11].substring(1).split("\"")[0].split(",");
            for (String thresholdString : thresholdStrings) {
                int threshold = Integer.parseInt(thresholdString);
                if (threshold < 0) {
                    failMessage(failMsg);
                    return;
                }
                thresholdsDP1.add(threshold);
            }
        } catch (Exception e) {
            failMessage(failMsg);
            return;
        }
        if (thresholdsDP1.size() != 8) {
            failMessage(failMsg);
            return;
        }

        List<Integer> unitsDP1 = new ArrayList<>();
        try {
            String[] unitStrings = parts[12].substring(1).split("\"")[0].split(",");
            for (String threshold : unitStrings) {
                unitsDP1.add(Integer.parseInt(threshold));
            }
        } catch (Exception e) {
            failMessage(failMsg);
            return;
        }
        if (unitsDP1.size() != 8) {
            failMessage(failMsg);
            return;
        }

        List<String> actionsDP1 = new ArrayList<>();
        try {
            String[] actionStrings = parts[13].substring(1).split("\"")[0].split(",");
            for (String action : actionStrings) {
                actionsDP1.add(pad(action));
            }
        } catch (Exception e) {
            failMessage(failMsg);
            return;
        }
        if (actionsDP1.size() != 8) {
            failMessage(failMsg);
            return;
        }

        List<Integer> weekProfile = new ArrayList<>();
        try {
            String[] dayStrings = parts[14].substring(1).split("\"")[0].split(",");
            for (String day : dayStrings) {
                weekProfile.add(Integer.parseInt(day));
            }
        } catch (Exception e) {
            failMessage(failMsg);
            return;
        }
        if (weekProfile.size() != 7) {
            failMessage(failMsg);
            return;
        }

        Date date = null;
        if (parts.length > 15) {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            try {
                String dateString = parts[15].substring(1).split("\"")[0];
                date = formatter.parse(dateString);
            } catch (ParseException e) {
                failMessage(failMsg);
                return;
            }
        }

        ace4000.getObjectFactory().sendConsumptionLimitationConfigurationRequest(date, convertToNumberOfSubIntervalsCode(numberOfSubIntervals), convertToSubIntervalDurationCode(subIntervalDuration), ovl, thresholdTolerance, thresholdSelection, switchingTimesDP0, thresholdsDP0, unitsDP0, actionsDP0, switchingTimesDP1, thresholdsDP1, unitsDP1, actionsDP1, weekProfile);
    }

    private void sendEmergencyConsumptionLimitationConfiguration(String messageContent) {
        String[] parts = messageContent.split("=");
        int duration = Integer.parseInt(parts[1].substring(1).split("\"")[0]);
        int threshold = Integer.parseInt(parts[2].substring(1).split("\"")[0]);
        int unit = Integer.parseInt(parts[3].substring(1).split("\"")[0]);
        int overrideRate = Integer.parseInt(parts[4].substring(1).split("\"")[0]);
        String failMsg = "Emergency consumption limitation configuration message failed, invalid arguments";
        if (duration > 0xFFFF || duration < 0) {
            failMessage(failMsg);
            return;
        }
        if (threshold < 0) {
            failMessage(failMsg);
            return;
        }
        if (unit != 0 && unit != 1) {
            failMessage(failMsg);
            return;
        }
        if (overrideRate < 0 || overrideRate > 4) {
            failMessage(failMsg);
            return;
        }
        ace4000.getObjectFactory().sendEmergencyConsumptionLimitationConfigurationRequest(duration, threshold, unit, overrideRate);
    }

    private void sendSDMConfigurationRequest(String messageContent) {
        String failMsg = "Special data mode configuration message failed, invalid arguments";
        String[] parts = messageContent.split("=");

        int duration = Integer.parseInt(parts[1].substring(1).split("\"")[0]);
        Date date;
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            date = formatter.parse(parts[2].substring(1).split("\"")[0]);
        } catch (Exception e) {
            failMessage(failMsg);
            return;
        }

        int billingEnable = Integer.parseInt(parts[3].substring(1).split("\"")[0]);
        if (billingEnable != 0 && billingEnable != 1) {
            failMessage(failMsg);
            return;
        }
        int billingInterval = Integer.parseInt(parts[4].substring(1).split("\"")[0]);
        if (billingInterval != 0 && billingInterval != 1 && billingInterval != 2) {
            failMessage(failMsg);
            return;
        }
        int billingNumber = Integer.parseInt(parts[5].substring(1).split("\"")[0]);
        if (billingNumber > 0xFFFF || billingNumber < 0) {
            failMessage(failMsg);
            return;
        }

        int loadProfileEnable = Integer.parseInt(parts[6].substring(1).split("\"")[0]);
        if (loadProfileEnable != 0 && loadProfileEnable != 1) {
            failMessage(failMsg);
            return;
        }
        int loadProfileInterval = Integer.parseInt(parts[7].substring(1).split("\"")[0]);
        if (loadProfileInterval != 1 && loadProfileInterval != 2 && loadProfileInterval != 3 && loadProfileInterval != 5 && loadProfileInterval != 6 && loadProfileInterval != 0x0A && loadProfileInterval != 0x0C && loadProfileInterval != 0x0F && loadProfileInterval != 0x14 && loadProfileInterval != 0x1E && loadProfileInterval != 0x3C && loadProfileInterval != 0x78 && loadProfileInterval != 0xF0) {
            failMessage(failMsg);
            return;
        }
        int loadProfileNumber = Integer.parseInt(parts[8].substring(1).split("\"")[0]);
        if (loadProfileNumber > 0xFFFF || loadProfileNumber < 0) {
            failMessage(failMsg);
            return;
        }
        ace4000.getObjectFactory().sendSDMConfiguration(billingEnable, billingInterval, billingNumber, loadProfileEnable, loadProfileInterval, loadProfileNumber, duration, date);
    }

    private void failMessage(String description) {
        //TODO !
    }


    private Integer convertToSubIntervalDurationCode(int subIntervalDuration) {
        Map<Integer, Integer> map = new HashMap<>();
        map.put(30, 0);
        map.put(60, 1);
        map.put(300, 2);
        map.put(600, 3);
        map.put(900, 4);
        map.put(1200, 5);
        map.put(1800, 6);
        map.put(3600, 7);
        return map.get(subIntervalDuration);
    }

    private Integer convertToNumberOfSubIntervalsCode(int numberOfSubIntervals) {
        Map<Integer, Integer> map = new HashMap<>();
        map.put(0, 0);
        map.put(1, 1);
        map.put(2, 2);
        map.put(3, 3);
        map.put(4, 4);
        map.put(5, 5);
        map.put(10, 6);
        map.put(15, 7);
        return map.get(numberOfSubIntervals);
    }
}