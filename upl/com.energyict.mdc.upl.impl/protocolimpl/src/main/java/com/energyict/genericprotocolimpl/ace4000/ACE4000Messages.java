package com.energyict.genericprotocolimpl.ace4000;

import com.energyict.cbo.BusinessException;
import com.energyict.genericprotocolimpl.common.GenericMessageExecutor;
import com.energyict.mdw.core.RtuMessage;
import com.energyict.protocol.*;
import com.energyict.protocol.messaging.*;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;

public class ACE4000Messages extends GenericMessageExecutor implements MessageProtocol {

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

    private ACE4000 ace4000;
    private boolean newRequest = false;
    private boolean eventsRequested = false;
    private boolean profileDataRequested = false;

    public ACE4000Messages(ACE4000 ace4000) {
        this.ace4000 = ace4000;
    }

    public boolean isEventsRequested() {
        return eventsRequested;
    }

    public boolean isProfileDataRequested() {
        return profileDataRequested;
    }

    @Override
    public void doMessage(RtuMessage messageEntry) throws BusinessException, SQLException, IOException {
        try {
            if (messageEntry.getContents().contains(READ_EVENTS)) {
                if (ace4000.getObjectFactory().shouldRetryEvents()) {
                    readEvents();
                    eventsRequested = true;
                    newRequest = true;
                }
            } else if (messageEntry.getContents().contains(READ_PROFILE_DATA)) {
                if (ace4000.getObjectFactory().shouldRetryLoadProfile()) {
                    readProfileData(messageEntry);
                    profileDataRequested = true;
                    newRequest = true;
                }
            } else if (messageEntry.getContents().contains(FIRMWARE_UPGRADE)) {
                if (ace4000.getObjectFactory().shouldRetryFirmwareUpgrade()) {
                    sendFirmwareUpgradeRequest(messageEntry);
                    newRequest = true;
                }
            } else if (messageEntry.getContents().contains(CONNECT)) {
                if (ace4000.getObjectFactory().shouldRetryConnectCommand()) {
                    sendContactorCommand(messageEntry, 0);
                    newRequest = true;
                }
            } else if (messageEntry.getContents().contains(DISCONNECT)) {
                if (ace4000.getObjectFactory().shouldRetryDisconnectCommand()) {
                    sendContactorCommand(messageEntry, 1);
                    newRequest = true;
                }
            } else if (messageEntry.getContents().contains(SHORT_DISPLAY_MESSAGE)) {
                if (ace4000.getObjectFactory().shouldRetryDisplayMessageRequest()) {
                    sendDisplayMessage(messageEntry, 1);
                    newRequest = true;
                }
            } else if (messageEntry.getContents().contains(LONG_DISPLAY_MESSAGE)) {
                if (ace4000.getObjectFactory().shouldRetryDisplayMessageRequest()) {
                    sendDisplayMessage(messageEntry, 2);
                    newRequest = true;
                }
            } else if (messageEntry.getContents().contains(NO_DISPLAY_MESSAGE)) {
                if (ace4000.getObjectFactory().shouldRetryDisplayMessageRequest()) {
                    sendDisplayMessage(messageEntry, 0);
                    newRequest = true;
                }
            } else if (messageEntry.getContents().contains(DISPLAY_CONFIG)) {
                if (ace4000.getObjectFactory().shouldRetryDisplayConfigRequest()) {
                    sendDisplayConfigRequest(messageEntry);
                    newRequest = true;
                }
            } else if (messageEntry.getContents().contains(CONFIG_LOADPROFILE)) {
                if (ace4000.getObjectFactory().shouldRetryLoadProfileConfiguration()) {
                    sendLoadProfileConfigurationRequest(messageEntry);
                    newRequest = true;
                }
            } else if (messageEntry.getContents().contains(CONFIG_SPECIAL_DATA_MODE)) {
                if (ace4000.getObjectFactory().shouldRetrySDMConfiguration()) {
                    sendSDMConfigurationRequest(messageEntry);
                    newRequest = true;
                }
            } else if (messageEntry.getContents().contains(CONFIG_MAX_DEMAND)) {
                if (ace4000.getObjectFactory().shouldRetryMaxDemandConfiguration()) {
                    sendMaxDemandConfigurationRequest(messageEntry);
                    newRequest = true;
                }
            } else if (messageEntry.getContents().contains(CONFIG_CONSUMPTION_LIMITATION)) {
                if (ace4000.getObjectFactory().shouldRetryConsumptionLimitationConfiguration()) {
                    sendConsumptionLimitationConfiguration(messageEntry);
                    newRequest = true;
                }
            } else if (messageEntry.getContents().contains(CONFIG_EMERGENCY_CONSUMPTION_LIMITATION)) {
                if (ace4000.getObjectFactory().shouldRetryEmergencyConsumptionLimitationConfiguration()) {
                    sendEmergencyConsumptionLimitationConfiguration(messageEntry);
                    newRequest = true;
                }
            } else if (messageEntry.getContents().contains(CONFIG_TARIFF)) {
                if (ace4000.getObjectFactory().shouldRetryTariffConfiguration()) {
                    sendTariffConfiguration(messageEntry);
                    newRequest = true;
                }
            } else {
                ace4000.getLogger().warning("Unknown message: [" + messageEntry.getContents() + "], cannot execute.");
                messageEntry.setFailed();
            }
        } catch (Exception e) {
            ace4000.getLogger().severe("Error parsing message, " + e.getMessage());
            messageEntry.setFailed();
        }
    }

    @Override
    protected TimeZone getTimeZone() {
        return ace4000.getDeviceTimeZone();
    }

    public List getMessageCategories() {
        List<MessageCategorySpec> categories = new ArrayList<MessageCategorySpec>();

        MessageCategorySpec cat1 = new MessageCategorySpec("ACE4000 general messages");
        cat1.addMessageSpec(addBasicMsg("Read all events", READ_EVENTS, false));
        cat1.addMessageSpec(addBasicMsgWith2Values("Read profile data from... to...", READ_PROFILE_DATA, false, "From date (dd/mm/yyyy hh:mm:ss)", "To date (dd/mm/yyyy hh:mm:ss)"));
        cat1.addMessageSpec(addBasicMsgWith3Values("Firmware upgrade", FIRMWARE_UPGRADE, false, "URL path (start with http://)", "Size of the JAR file (bytes)", "Size of the JAD file (bytes)"));
        cat1.addMessageSpec(addBasicMsgWith1Attr("Connect (close contactor)", CONNECT, false, "Optional date (dd/mm/yyyy hh:mm:ss)"));
        cat1.addMessageSpec(addBasicMsgWith1Attr("Disconnect (open contactor)", DISCONNECT, false, "Optional date (dd/mm/yyyy hh:mm:ss)"));
        categories.add(cat1);

        MessageCategorySpec cat2 = new MessageCategorySpec("ACE4000 configuration messages");
        cat2.addMessageSpec(addBasicMsgWithValue("Send short display message (max 8 chars)", SHORT_DISPLAY_MESSAGE, false));
        cat2.addMessageSpec(addBasicMsgWithValue("Send long display message (max 1024 chars)", LONG_DISPLAY_MESSAGE, false));
        cat2.addMessageSpec(addBasicMsg("Disable the display message", NO_DISPLAY_MESSAGE, false));
        cat2.addMessageSpec(addBasicMsgWith4Values("Configure LCD display", DISPLAY_CONFIG, false, "Number of digits before comma (allowed: 5, 6 or 7)", "Number of digits after comma (allowed: 0, 1, 2 or 3)", "Display sequence (comma separated hex values, e.g.: 1,2,E,12,1A)", "Display cycle time (seconds)"));
        cat2.addMessageSpec(addBasicMsgWith3Values("Configure load profile data recording", CONFIG_LOADPROFILE, false, "Enable (1) or disable (0)", "Interval (1, 2, 3, 5, 6, 10, 12, 15, 20, 30, 60, 120 or 240 minutes)", "Maximum number of records (min 1, max 65535)"));
        cat2.addMessageSpec(addBasicMsgWith8Values("Configure special data mode", CONFIG_SPECIAL_DATA_MODE, false, "Special data mode duration (days)", "Special data mode activation date (dd/mm/yyyy)", "Special billing register recording: enable (1) or disable (0)", "Special billing register recording: interval (0: hourly, 1: daily, 2: monthly)", "Special billing register recording: max number of records (min 1, max 65535)", "Special load profile: enable (1) or disable (0)", "Special load profile: interval (1, 2, 3, 5, 6, 10, 12, 15, 20, 30, 60, 120 or 240 minutes)", "Special load profile: max number of records (min 1, max 65535)"));
        cat2.addMessageSpec(addBasicMsgWith3Values("Configure maximum demand settings", CONFIG_MAX_DEMAND, false, "Active registers (0) or reactive registers (1)", "Number of sub intervals (0, 1, 2, 3, 4, 5, 10 or 15)", "Sub interval duration (30, 60, 300, 600, 900, 1200, 1800 or 3600 seconds)"));
        cat2.addMessageSpec(addBasicMsgWith15Values("Configure consumption limitation settings", CONFIG_CONSUMPTION_LIMITATION, false, "Number of sub intervals (0, 1, 2, 3, 4, 5, 10 or 15)", "Sub interval duration (30, 60, 300, 600, 900, 1200, 1800 or 3600 seconds)", "Override rate (0: disabled)", "Allowed excess tolerance (0 - 100 %)", "Threshold selection (0: day profile, 1: maximum threshold)", "8 switching moments for daily profile 0 (comma separated, e.g.: 01:00,05:00,...)", "8 thresholds for daily profile 0 (comma separated)", "8 units for the thresholds (comma separated) (0: Watt, 1: Ampere)", "8 actions (in hex) for daily profile 0 (comma separated)", "8 switching moments for daily profile 1 (comma separated, e.g.: 01:00,05:00,...)", "8 thresholds for daily profile 1 (comma separated)", "8 units for the thresholds (comma separated) (0: Watt, 1: Ampere)", "8 actions (in hex) for daily profile 1 (comma separated)", "Day profiles used for each day of the week (comma separated)", "Activation date (dd/mm/yyyy hh:mm:ss) (optional)"));
        cat2.addMessageSpec(addBasicMsgWith4Values("Configure emergency consumption limitation settings", CONFIG_EMERGENCY_CONSUMPTION_LIMITATION, false, "Duration (minutes)", "Threshold value", "Threshold unit (0: Watt, 1: Ampere)", "Override rate (0: disabled)"));
        cat2.addMessageSpec(addBasicMsgWith3Values("Configure tariff settings", CONFIG_TARIFF, false, "Unique tariff ID number", "Number of tariff rates (max 4)", "Code table ID"));

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

    protected MessageSpec addBasicMsgWith1Attr(final String keyId, final String tagName, final boolean advanced, String attr1) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageAttributeSpec addAttribute1 = new MessageAttributeSpec(attr1, false);
        tagSpec.add(addAttribute1);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" "); //Disable this field
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    protected MessageSpec addBasicMsgWith2Values(final String keyId, final String tagName, final boolean advanced, String attr1, String attr2) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageAttributeSpec addAttribute1 = new MessageAttributeSpec(attr1, false);
        tagSpec.add(addAttribute1);
        MessageAttributeSpec addAttribute2 = new MessageAttributeSpec(attr2, false);
        tagSpec.add(addAttribute2);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" "); //Disable this field
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    protected MessageSpec addBasicMsgWith3Values(final String keyId, final String tagName, final boolean advanced, String attr1, String attr2, String attr3) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageAttributeSpec addAttribute1 = new MessageAttributeSpec(attr1, true);
        tagSpec.add(addAttribute1);
        MessageAttributeSpec addAttribute2 = new MessageAttributeSpec(attr2, true);
        tagSpec.add(addAttribute2);
        MessageAttributeSpec addAttribute3 = new MessageAttributeSpec(attr3, true);
        tagSpec.add(addAttribute3);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" "); //Disable this field
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    protected MessageSpec addBasicMsgWith4Values(final String keyId, final String tagName, final boolean advanced, String attr1, String attr2, String attr3, String attr4) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageAttributeSpec addAttribute1 = new MessageAttributeSpec(attr1, true);
        tagSpec.add(addAttribute1);
        MessageAttributeSpec addAttribute2 = new MessageAttributeSpec(attr2, true);
        tagSpec.add(addAttribute2);
        MessageAttributeSpec addAttribute3 = new MessageAttributeSpec(attr3, true);
        tagSpec.add(addAttribute3);
        MessageAttributeSpec addAttribute4 = new MessageAttributeSpec(attr4, true);
        tagSpec.add(addAttribute4);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" "); //Disable this field
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    protected MessageSpec addBasicMsgWith15Values(final String keyId, final String tagName, final boolean advanced, String attr1, String attr2, String attr3, String attr4, String attr5, String attr6, String attr7, String attr8, String attr9, String attr10, String attr11, String attr12, String attr13, String attr14, String attr15) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageAttributeSpec addAttribute1 = new MessageAttributeSpec(attr1, true);
        tagSpec.add(addAttribute1);
        MessageAttributeSpec addAttribute2 = new MessageAttributeSpec(attr2, true);
        tagSpec.add(addAttribute2);
        MessageAttributeSpec addAttribute3 = new MessageAttributeSpec(attr3, true);
        tagSpec.add(addAttribute3);
        MessageAttributeSpec addAttribute4 = new MessageAttributeSpec(attr4, true);
        tagSpec.add(addAttribute4);
        MessageAttributeSpec addAttribute5 = new MessageAttributeSpec(attr5, true);
        tagSpec.add(addAttribute5);
        MessageAttributeSpec addAttribute6 = new MessageAttributeSpec(attr6, true);
        tagSpec.add(addAttribute6);
        MessageAttributeSpec addAttribute7 = new MessageAttributeSpec(attr7, true);
        tagSpec.add(addAttribute7);
        MessageAttributeSpec addAttribute8 = new MessageAttributeSpec(attr8, true);
        tagSpec.add(addAttribute8);
        MessageAttributeSpec addAttribute9 = new MessageAttributeSpec(attr9, true);
        tagSpec.add(addAttribute9);
        MessageAttributeSpec addAttribute10 = new MessageAttributeSpec(attr10, true);
        tagSpec.add(addAttribute10);
        MessageAttributeSpec addAttribute11 = new MessageAttributeSpec(attr11, true);
        tagSpec.add(addAttribute11);
        MessageAttributeSpec addAttribute12 = new MessageAttributeSpec(attr12, true);
        tagSpec.add(addAttribute12);
        MessageAttributeSpec addAttribute13 = new MessageAttributeSpec(attr13, true);
        tagSpec.add(addAttribute13);
        MessageAttributeSpec addAttribute14 = new MessageAttributeSpec(attr14, true);
        tagSpec.add(addAttribute14);
        MessageAttributeSpec addAttribute15 = new MessageAttributeSpec(attr15, false);
        tagSpec.add(addAttribute15);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" "); //Disable this field
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    protected MessageSpec addBasicMsgWith8Values(final String keyId, final String tagName, final boolean advanced, String attr1, String attr2, String attr3, String attr4, String attr5, String attr6, String attr7, String attr8) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageAttributeSpec addAttribute1 = new MessageAttributeSpec(attr1, true);
        tagSpec.add(addAttribute1);
        MessageAttributeSpec addAttribute2 = new MessageAttributeSpec(attr2, true);
        tagSpec.add(addAttribute2);
        MessageAttributeSpec addAttribute3 = new MessageAttributeSpec(attr3, true);
        tagSpec.add(addAttribute3);
        MessageAttributeSpec addAttribute4 = new MessageAttributeSpec(attr4, true);
        tagSpec.add(addAttribute4);
        MessageAttributeSpec addAttribute5 = new MessageAttributeSpec(attr5, true);
        tagSpec.add(addAttribute5);
        MessageAttributeSpec addAttribute6 = new MessageAttributeSpec(attr6, true);
        tagSpec.add(addAttribute6);
        MessageAttributeSpec addAttribute7 = new MessageAttributeSpec(attr7, true);
        tagSpec.add(addAttribute7);
        MessageAttributeSpec addAttribute8 = new MessageAttributeSpec(attr8, true);
        tagSpec.add(addAttribute8);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" "); //Disable this field
        tagSpec.add(msgVal);
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
        StringBuffer buf = new StringBuffer();

        // a. Opening tag
        buf.append("<");
        buf.append(msgTag.getName());

        // b. Attributes
        for (Object o1 : msgTag.getAttributes()) {
            MessageAttribute att = (MessageAttribute) o1;
            if (att.getValue() == null || att.getValue().length() == 0) {
                continue;
            }
            buf.append(" ").append(att.getSpec().getName());
            buf.append("=").append('"').append(att.getValue()).append('"');
        }
        buf.append(">");

        // c. sub elements
        for (Object o : msgTag.getSubElements()) {
            MessageElement elt = (MessageElement) o;
            if (elt.isTag()) {
                buf.append(writeTag((MessageTag) elt));
            } else if (elt.isValue()) {
                String value = writeValue((MessageValue) elt);
                if (value == null || value.length() == 0) {
                    return "";
                }
                buf.append(value);
            }
        }

        // d. Closing tag
        buf.append("</");
        buf.append(msgTag.getName());
        buf.append(">");

        return buf.toString();
    }

    public String writeValue(MessageValue value) {
        return value.getValue();
    }

    public void applyMessages(List messageEntries) throws IOException {
    }

    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return null;    //Not used
    }


    private void sendTariffConfiguration(RtuMessage messageEntry) throws IOException, BusinessException, SQLException {
        String[] parts = messageEntry.getContents().split("=");
        int number = Integer.parseInt(parts[1].substring(1).split("\"")[0]);
        int numberOfRates = Integer.parseInt(parts[2].substring(1).split("\"")[0]);
        int codeTableId = Integer.parseInt(parts[3].substring(1).split("\"")[0]);
        if (numberOfRates > 4 || numberOfRates < 0) {
            failMessage(messageEntry, "Tariff configuration failed, invalid number of rates");
            return;
        }

        try {
            ace4000.getObjectFactory().sendTariffConfiguration(number, numberOfRates, codeTableId);
        } catch (InvalidPropertyException e) {
            failMessage(messageEntry, "Tariff configuration failed, invalid code table settings");
        }
    }

    private void sendLoadProfileConfigurationRequest(RtuMessage messageEntry) throws IOException, BusinessException, SQLException {
        String[] parts = messageEntry.getContents().split("=");
        int enable = Integer.parseInt(parts[1].substring(1).split("\"")[0]);
        int intervalInMinutes = Integer.parseInt(parts[2].substring(1).split("\"")[0]);
        int maxNumberOfRecords = Integer.parseInt(parts[3].substring(1).split("\"")[0]);
        if (enable != 0 && enable != 1) {
            failMessage(messageEntry, "Load profile configuration message failed, invalid arguments");
            return;
        }
        if (intervalInMinutes != 1 && intervalInMinutes != 2 && intervalInMinutes != 3 && intervalInMinutes != 5 && intervalInMinutes != 6 && intervalInMinutes != 0x0A && intervalInMinutes != 0x0C && intervalInMinutes != 0x0F && intervalInMinutes != 0x14 && intervalInMinutes != 0x1E && intervalInMinutes != 0x3C && intervalInMinutes != 0x78 && intervalInMinutes != 0xF0) {
            failMessage(messageEntry, "Load profile configuration message failed, invalid arguments");
            return;
        }
        ace4000.getObjectFactory().sendLoadProfileConfiguration(enable, intervalInMinutes, maxNumberOfRecords);
    }

    private void readProfileData(RtuMessage messageEntry) throws IOException, BusinessException, SQLException {
        String[] parts = messageEntry.getContents().split("=");
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date fromDate = null;
        Date toDate = null;

        if (parts.length > 1) {
            try {
                String fromDateString = parts[1].substring(1, 20);
                fromDate = formatter.parse(fromDateString);
            } catch (Exception e) {
                failMessage(messageEntry, "Request for load profile data failed, invalid arguments");
                return;
            }
        }
        if (parts.length > 2) {
            try {
                String toDateString = parts[2].substring(1, 20);
                toDate = formatter.parse(toDateString);
            } catch (Exception e) {
                toDate = new Date();
            }
        }
        if (toDate == null) {
            toDate = new Date();
        }
        if (toDate.after(new Date())) {
            toDate = new Date();
        }
        if (fromDate != null && !fromDate.before(toDate)) {
            failMessage(messageEntry, "Request for load profile data failed, invalid arguments");
            return;
        }

        ace4000.getObjectFactory().sendLoadProfileRequest(fromDate, toDate);
    }

    private void sendContactorCommand(RtuMessage messageEntry, int cmd) throws IOException, BusinessException, SQLException {
        String[] parts = messageEntry.getContents().split("=");
        Date date = null;

        if (parts.length > 1) {
            try {
                String timeStamp = parts[1].substring(1, 20);
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                date = formatter.parse(timeStamp);
            } catch (Exception e) {
                failMessage(messageEntry, "Contactor control message failed, invalid arguments");
                return;
            }
        }
        ace4000.getObjectFactory().sendContactorCommand(date, cmd);
    }

    private void sendDisplayMessage(RtuMessage messageEntry, int mode) throws IOException, BusinessException, SQLException {
        String message = "";
        if (mode != 0) {
            message = stripOffTag(messageEntry.getContents());
        }
        if (mode == 1 && message.length() > 8) {
            failMessage(messageEntry, "Display message failed, invalid arguments");
        } else if (mode == 2 && message.length() > 1024) {
            failMessage(messageEntry, "Display message failed, invalid arguments");
        } else {
            ace4000.getObjectFactory().sendDisplayMessage(mode, message);
        }
    }


    private void sendDisplayConfigRequest(RtuMessage messageEntry) throws IOException, BusinessException, SQLException {
        String[] parts = messageEntry.getContents().split("=");
        int number1 = Integer.parseInt(parts[1].substring(1).split("\"")[0]);
        int number2 = Integer.parseInt(parts[2].substring(1).split("\"")[0]);
        String sequence = parts[3].substring(1).split("\"")[0];
        int intervalInSeconds = Integer.parseInt(parts[4].substring(1).split("\"")[0]);

        int resolutionCode = convertToResolutionCode(number1, number2);
        if (resolutionCode == -1) {
            failMessage(messageEntry, "Display configuration request failed, invalid arguments");
            return;
        }

        String[] sequenceCodes = sequence.split(",");
        String sequenceResult = "";
        try {
            for (String code : sequenceCodes) {
                if (code.length() != 1 && code.length() != 2) {
                    failMessage(messageEntry, "Display configuration request failed, invalid arguments");
                    return;
                }
                sequenceResult += ((code.length() == 1) ? "0" : "") + code;
            }
        } catch (Exception e) {
            failMessage(messageEntry, "Display configuration request failed, invalid arguments");
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


    private void sendFirmwareUpgradeRequest(RtuMessage messageEntry) throws IOException {
        String[] parts = messageEntry.getContents().split("=");
        String path = parts[1].substring(1).split("\"")[0];
        int jarSize = Integer.parseInt(parts[2].substring(1).split("\"")[0]);
        int jadSize = Integer.parseInt(parts[3].substring(1).split("\"")[0]);
        ace4000.getObjectFactory().sendFirmwareUpgradeRequest(path, jarSize, jadSize);
    }

    private void sendMaxDemandConfigurationRequest(RtuMessage messageEntry) throws IOException, BusinessException, SQLException {
        String[] parts = messageEntry.getContents().split("=");
        int register = Integer.parseInt(parts[1].substring(1).split("\"")[0]);
        int numberOfSubIntervals = Integer.parseInt(parts[2].substring(1).split("\"")[0]);
        int subIntervalDuration = Integer.parseInt(parts[3].substring(1).split("\"")[0]);

        if (convertToNumberOfSubIntervalsCode(numberOfSubIntervals) == null) {
            failMessage(messageEntry, "Max demand configuration message failed, invalid arguments");
            return;
        }
        if (convertToSubIntervalDurationCode(subIntervalDuration) == null) {
            failMessage(messageEntry, "Max demand configuration message failed, invalid arguments");
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

    private void sendConsumptionLimitationConfiguration(RtuMessage messageEntry) throws BusinessException, SQLException, IOException {
        String[] parts = messageEntry.getContents().split("=");
        int numberOfSubIntervals = Integer.parseInt(parts[1].substring(1).split("\"")[0]);
        int subIntervalDuration = Integer.parseInt(parts[2].substring(1).split("\"")[0]);
        String failMsg = "Consumption limitation configuration message failed, invalid arguments";
        if (convertToNumberOfSubIntervalsCode(numberOfSubIntervals) == null) {
            failMessage(messageEntry, failMsg);
            return;
        }
        if (convertToSubIntervalDurationCode(subIntervalDuration) == null) {
            failMessage(messageEntry, failMsg);
            return;
        }
        int ovl = Integer.parseInt(parts[3].substring(1).split("\"")[0]);
        if (ovl < 0 || ovl > 4) {
            failMessage(messageEntry, failMsg);
            return;
        }
        int thresholdTolerance = Integer.parseInt(parts[4].substring(1).split("\"")[0]);
        if (thresholdTolerance < 0 || thresholdTolerance > 100) {
            failMessage(messageEntry, failMsg);
            return;
        }
        int thresholdSelection = Integer.parseInt(parts[5].substring(1).split("\"")[0]);
        if (thresholdSelection < 0 || thresholdSelection > 1) {
            failMessage(messageEntry, failMsg);
            return;
        }
        List<String> switchingTimesDP0 = new ArrayList<String>();
        try {
            String[] switchingTimesStrings = parts[6].substring(1).split("\"")[0].split(",");
            for (String switchingTime : switchingTimesStrings) {
                int hour = Integer.parseInt(switchingTime.split(":")[0]);
                int minute = Integer.parseInt(switchingTime.split(":")[1]);
                switchingTimesDP0.add(pad(Integer.toString(hour, 16)) + pad(Integer.toString(minute, 16)));
            }
        } catch (Exception e) {
            failMessage(messageEntry, failMsg);
            return;
        }
        if (switchingTimesDP0.size() != 8) {
            failMessage(messageEntry, failMsg);
            return;
        }

        List<Integer> thresholdsDP0 = new ArrayList<Integer>();
        try {
            String[] thresholdStrings = parts[7].substring(1).split("\"")[0].split(",");
            for (String thresholdString : thresholdStrings) {
                int threshold = Integer.parseInt(thresholdString);
                if (threshold < 0) {
                    failMessage(messageEntry, failMsg);
                    return;
                }
                thresholdsDP0.add(threshold);
            }
        } catch (Exception e) {
            failMessage(messageEntry, failMsg);
            return;
        }
        if (thresholdsDP0.size() != 8) {
            failMessage(messageEntry, failMsg);
            return;
        }

        List<Integer> unitsDP0 = new ArrayList<Integer>();
        try {
            String[] unitStrings = parts[8].substring(1).split("\"")[0].split(",");
            for (String threshold : unitStrings) {
                unitsDP0.add(Integer.parseInt(threshold));
            }
        } catch (Exception e) {
            failMessage(messageEntry, failMsg);
            return;
        }
        if (unitsDP0.size() != 8) {
            failMessage(messageEntry, failMsg);
            return;
        }

        List<String> actionsDP0 = new ArrayList<String>();
        try {
            String[] actionStrings = parts[9].substring(1).split("\"")[0].split(",");
            for (String action : actionStrings) {
                actionsDP0.add(pad(action));
            }
        } catch (Exception e) {
            failMessage(messageEntry, failMsg);
            return;
        }
        if (actionsDP0.size() != 8) {
            failMessage(messageEntry, failMsg);
            return;
        }
        List<String> switchingTimesDP1 = new ArrayList<String>();
        try {
            String[] switchingTimesStrings = parts[10].substring(1).split("\"")[0].split(",");
            for (String switchingTime : switchingTimesStrings) {
                int hour = Integer.parseInt(switchingTime.split(":")[0]);
                int minute = Integer.parseInt(switchingTime.split(":")[1]);
                switchingTimesDP1.add(pad(Integer.toString(hour, 16)) + pad(Integer.toString(minute, 16)));
            }
        } catch (Exception e) {
            failMessage(messageEntry, failMsg);
            return;
        }
        if (switchingTimesDP1.size() != 8) {
            failMessage(messageEntry, failMsg);
            return;
        }

        List<Integer> thresholdsDP1 = new ArrayList<Integer>();
        try {
            String[] thresholdStrings = parts[11].substring(1).split("\"")[0].split(",");
            for (String thresholdString : thresholdStrings) {
                int threshold = Integer.parseInt(thresholdString);
                if (threshold < 0) {
                    failMessage(messageEntry, failMsg);
                    return;
                }
                thresholdsDP1.add(threshold);
            }
        } catch (Exception e) {
            failMessage(messageEntry, failMsg);
            return;
        }
        if (thresholdsDP1.size() != 8) {
            failMessage(messageEntry, failMsg);
            return;
        }

        List<Integer> unitsDP1 = new ArrayList<Integer>();
        try {
            String[] unitStrings = parts[12].substring(1).split("\"")[0].split(",");
            for (String threshold : unitStrings) {
                unitsDP1.add(Integer.parseInt(threshold));
            }
        } catch (Exception e) {
            failMessage(messageEntry, failMsg);
            return;
        }
        if (unitsDP1.size() != 8) {
            failMessage(messageEntry, failMsg);
            return;
        }

        List<String> actionsDP1 = new ArrayList<String>();
        try {
            String[] actionStrings = parts[13].substring(1).split("\"")[0].split(",");
            for (String action : actionStrings) {
                actionsDP1.add(pad(action));
            }
        } catch (Exception e) {
            failMessage(messageEntry, failMsg);
            return;
        }
        if (actionsDP1.size() != 8) {
            failMessage(messageEntry, failMsg);
            return;
        }

        List<Integer> weekProfile = new ArrayList<Integer>();
        try {
            String[] dayStrings = parts[14].substring(1).split("\"")[0].split(",");
            for (String day : dayStrings) {
                weekProfile.add(Integer.parseInt(day));
            }
        } catch (Exception e) {
            failMessage(messageEntry, failMsg);
            return;
        }
        if (weekProfile.size() != 7) {
            failMessage(messageEntry, failMsg);
            return;
        }

        Date date = null;
        if (parts.length > 15) {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            try {
                String dateString = parts[15].substring(1).split("\"")[0];
                date = formatter.parse(dateString);
            } catch (ParseException e) {
                failMessage(messageEntry, failMsg);
                return;
            }
        }

        ace4000.getObjectFactory().sendConsumptionLimitationConfigurationRequest(date, convertToNumberOfSubIntervalsCode(numberOfSubIntervals), convertToSubIntervalDurationCode(subIntervalDuration), ovl, thresholdTolerance, thresholdSelection, switchingTimesDP0, thresholdsDP0, unitsDP0, actionsDP0, switchingTimesDP1, thresholdsDP1, unitsDP1, actionsDP1, weekProfile);
    }

    private void sendEmergencyConsumptionLimitationConfiguration(RtuMessage messageEntry) throws BusinessException, SQLException, IOException {
        String[] parts = messageEntry.getContents().split("=");
        int duration = Integer.parseInt(parts[1].substring(1).split("\"")[0]);
        int threshold = Integer.parseInt(parts[2].substring(1).split("\"")[0]);
        int unit = Integer.parseInt(parts[3].substring(1).split("\"")[0]);
        int overrideRate = Integer.parseInt(parts[4].substring(1).split("\"")[0]);
        String failMsg = "Emergency consumption limitation configuration message failed, invalid arguments";
        if (duration > 0xFFFF || duration < 0) {
            failMessage(messageEntry, failMsg);
            return;
        }
        if (threshold < 0) {
            failMessage(messageEntry, failMsg);
            return;
        }
        if (unit != 0 && unit != 1) {
            failMessage(messageEntry, failMsg);
            return;
        }
        if (overrideRate < 0 || overrideRate > 4) {
            failMessage(messageEntry, failMsg);
            return;
        }
        ace4000.getObjectFactory().sendEmergencyConsumptionLimitationConfigurationRequest(duration, threshold, unit, overrideRate);
    }

    private void sendSDMConfigurationRequest(RtuMessage messageEntry) throws BusinessException, SQLException, IOException {
        String failMsg = "Special data mode configuration message failed, invalid arguments";
        String[] parts = messageEntry.getContents().split("=");

        int duration = Integer.parseInt(parts[1].substring(1).split("\"")[0]);
        Date date;
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            date = formatter.parse(parts[2].substring(1).split("\"")[0]);
        } catch (Exception e) {
            failMessage(messageEntry, failMsg);
            return;
        }

        int billingEnable = Integer.parseInt(parts[3].substring(1).split("\"")[0]);
        if (billingEnable != 0 && billingEnable != 1) {
            failMessage(messageEntry, failMsg);
            return;
        }
        int billingInterval = Integer.parseInt(parts[4].substring(1).split("\"")[0]);
        if (billingInterval != 0 && billingInterval != 1 && billingInterval != 2) {
            failMessage(messageEntry, failMsg);
            return;
        }
        int billingNumber = Integer.parseInt(parts[5].substring(1).split("\"")[0]);
        if (billingNumber > 0xFFFF || billingNumber < 0) {
            failMessage(messageEntry, failMsg);
            return;
        }

        int loadProfileEnable = Integer.parseInt(parts[6].substring(1).split("\"")[0]);
        if (loadProfileEnable != 0 && loadProfileEnable != 1) {
            failMessage(messageEntry, failMsg);
            return;
        }
        int loadProfileInterval = Integer.parseInt(parts[7].substring(1).split("\"")[0]);
        if (loadProfileInterval != 1 && loadProfileInterval != 2 && loadProfileInterval != 3 && loadProfileInterval != 5 && loadProfileInterval != 6 && loadProfileInterval != 0x0A && loadProfileInterval != 0x0C && loadProfileInterval != 0x0F && loadProfileInterval != 0x14 && loadProfileInterval != 0x1E && loadProfileInterval != 0x3C && loadProfileInterval != 0x78 && loadProfileInterval != 0xF0) {
            failMessage(messageEntry, failMsg);
            return;
        }
        int loadProfileNumber = Integer.parseInt(parts[8].substring(1).split("\"")[0]);
        if (loadProfileNumber > 0xFFFF || loadProfileNumber < 0) {
            failMessage(messageEntry, failMsg);
            return;
        }
        ace4000.getObjectFactory().sendSDMConfiguration(billingEnable, billingInterval, billingNumber, loadProfileEnable, loadProfileInterval, loadProfileNumber, duration, date);
    }


    private void failMessage(RtuMessage messageEntry, String msg) throws BusinessException, SQLException {
        ace4000.log(Level.WARNING, msg);
        messageEntry.setFailed();
    }

    private Integer convertToSubIntervalDurationCode(int subIntervalDuration) {
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
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
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
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

    private void readEvents() throws IOException, BusinessException, SQLException {
        ace4000.getObjectFactory().sendEventRequest();
    }

    public boolean isNewRequest() {
        return newRequest;
    }

    public void setMessageResult(boolean end, List<RtuMessage> messages) throws BusinessException, SQLException {
        for (RtuMessage messageEntry : messages) {
            if (messageEntry.getContents().contains(READ_EVENTS)) {
                setMessage(messageEntry, ace4000.getObjectFactory().getReceivedEvents(), "Request for event data failed");
            }
            if (messageEntry.getContents().contains(READ_PROFILE_DATA)) {
                setMessage(messageEntry, ace4000.getObjectFactory().getReceivedLoadProfile(), "Request for load profile data failed");
            }
            if (messageEntry.getContents().contains(DISPLAY_CONFIG)) {
                setMessage(messageEntry, ace4000.getObjectFactory().getDisplayConfigurationSucceeded(), "Display configuration request failed");
            }
            if (messageEntry.getContents().contains(CONFIG_LOADPROFILE)) {
                setMessage(messageEntry, ace4000.getObjectFactory().getLoadProfileConfigurationSucceeded(), "Load profile configuration message failed");
            }
            if (messageEntry.getContents().contains(CONFIG_SPECIAL_DATA_MODE)) {
                setMessage(messageEntry, ace4000.getObjectFactory().getSDMConfigurationSucceeded(), "Special data mode configuration message failed");
            }
            if (messageEntry.getContents().contains(CONFIG_MAX_DEMAND)) {
                setMessage(messageEntry, ace4000.getObjectFactory().getMaxDemandConfigurationSucceeded(), "Max demand configuration message failed");
            }
            if (messageEntry.getContents().contains(CONFIG_CONSUMPTION_LIMITATION)) {
                setMessage(messageEntry, ace4000.getObjectFactory().getConsumptionLimitationConfigurationSucceeded(), "Consumption limitation configuration message failed");
            }
            if (messageEntry.getContents().contains(CONFIG_EMERGENCY_CONSUMPTION_LIMITATION)) {
                setMessage(messageEntry, ace4000.getObjectFactory().getEmergencyConsumptionLimitationConfigurationSucceeded(), "Emergency consumption limitation configuration message failed");
            }
            if (messageEntry.getContents().contains(CONFIG_TARIFF)) {
                setMessage(messageEntry, ace4000.getObjectFactory().getTariffConfigurationSucceeded(), "Tariff configuration message failed");
            }
            if (messageEntry.getContents().contains(SHORT_DISPLAY_MESSAGE) || messageEntry.getContents().contains(LONG_DISPLAY_MESSAGE) || messageEntry.getContents().contains(NO_DISPLAY_MESSAGE)) {
                setMessage(messageEntry, ace4000.getObjectFactory().getDisplayMessageSucceeded(), "Display message request failed");
            }
            if (messageEntry.getContents().contains(FIRMWARE_UPGRADE)) {
                if (end) {
                    setMessage(messageEntry, ace4000.getObjectFactory().getFirmWareSucceeded(), "Request firmware upgrade failed");
                }
            }
            if (messageEntry.getContents().contains(CONNECT)) {
                if (end) {
                    setMessage(messageEntry, ace4000.getObjectFactory().getConnectSucceeded(), "Contactor control message (connect) failed");
                }
            }
            if (messageEntry.getContents().contains(DISCONNECT)) {
                if (end) {
                    setMessage(messageEntry, ace4000.getObjectFactory().getDisconnectSucceeded(), "Contactor control message (disconnect) failed");
                }
            }
        }
    }

    private void setMessage(RtuMessage messageEntry, Boolean success, String failMsg) throws BusinessException, SQLException {
        if (!(success == null)) {
            if (success) {
                messageEntry.confirm();
            } else {
                failMessage(messageEntry, failMsg);
            }
        }
    }

    public boolean shouldRetry(List<RtuMessage> messages) {
        for (RtuMessage messageEntry : messages) {
            if (messageEntry.getContents().contains(READ_EVENTS)) {
                if (ace4000.getObjectFactory().shouldRetryEvents()) {
                    return true;
                }
            }
            if (messageEntry.getContents().contains(READ_PROFILE_DATA)) {
                if (ace4000.getObjectFactory().shouldRetryLoadProfile()) {
                    return true;
                }
            }
            if (messageEntry.getContents().contains(FIRMWARE_UPGRADE)) {
                if (ace4000.getObjectFactory().shouldRetryFirmwareUpgrade()) {
                    return true;
                }
            }
            if (messageEntry.getContents().contains(CONNECT)) {
                if (ace4000.getObjectFactory().shouldRetryConnectCommand()) {
                    return true;
                }
            }
            if (messageEntry.getContents().contains(DISCONNECT)) {
                if (ace4000.getObjectFactory().shouldRetryDisconnectCommand()) {
                    return true;
                }
            }
            if (messageEntry.getContents().contains(DISPLAY_CONFIG)) {
                if (ace4000.getObjectFactory().shouldRetryDisplayConfigRequest()) {
                    return true;
                }
            }
            if (messageEntry.getContents().contains(CONFIG_LOADPROFILE)) {
                if (ace4000.getObjectFactory().shouldRetryLoadProfileConfiguration()) {
                    return true;
                }
            }
            if (messageEntry.getContents().contains(CONFIG_SPECIAL_DATA_MODE)) {
                if (ace4000.getObjectFactory().shouldRetrySDMConfiguration()) {
                    return true;
                }
            }
            if (messageEntry.getContents().contains(CONFIG_MAX_DEMAND)) {
                if (ace4000.getObjectFactory().shouldRetryMaxDemandConfiguration()) {
                    return true;
                }
            }
            if (messageEntry.getContents().contains(CONFIG_CONSUMPTION_LIMITATION)) {
                if (ace4000.getObjectFactory().shouldRetryConsumptionLimitationConfiguration()) {
                    return true;
                }
            }
            if (messageEntry.getContents().contains(CONFIG_EMERGENCY_CONSUMPTION_LIMITATION)) {
                if (ace4000.getObjectFactory().shouldRetryEmergencyConsumptionLimitationConfiguration()) {
                    return true;
                }
            }
            if (messageEntry.getContents().contains(CONFIG_TARIFF)) {
                if (ace4000.getObjectFactory().shouldRetryTariffConfiguration()) {
                    return true;
                }
            }
            if (messageEntry.getContents().contains(SHORT_DISPLAY_MESSAGE) || messageEntry.getContents().contains(LONG_DISPLAY_MESSAGE) || messageEntry.getContents().contains(NO_DISPLAY_MESSAGE)) {
                if (ace4000.getObjectFactory().shouldRetryDisplayMessageRequest()) {
                    return true;
                }
            }
        }
        return false;
    }

    public void logTimeoutMessages(List<RtuMessage> messages, int retries) throws BusinessException, SQLException {
        for (RtuMessage messageEntry : messages) {
            if (messageEntry.getContents().contains(READ_EVENTS)) {
                if (ace4000.getObjectFactory().shouldRetryEvents()) {
                    ace4000.log("Sent request for events " + (retries + 1) + " times, meter didn't reply");
                    messageEntry.setFailed();
                }
            }
            if (messageEntry.getContents().contains(READ_PROFILE_DATA)) {
                if (ace4000.getObjectFactory().shouldRetryLoadProfile()) {
                    ace4000.log("Sent request for profile data " + (retries + 1) + " times, meter didn't reply. Possibly there's no profile data in the requested range.");
                    messageEntry.setFailed();
                }
            }
            if (messageEntry.getContents().contains(FIRMWARE_UPGRADE)) {
                if (ace4000.getObjectFactory().shouldRetryFirmwareUpgrade()) {
                    ace4000.log("Sent request for firmware upgrade" + (retries + 1) + " times, meter didn't reply.");
                    messageEntry.setFailed();
                }
            }
            if (messageEntry.getContents().contains(CONNECT)) {
                if (ace4000.getObjectFactory().shouldRetryConnectCommand()) {
                    ace4000.log("Sent contactor connect command " + (retries + 1) + " times, meter didn't reply.");
                    messageEntry.setFailed();
                }
            }
            if (messageEntry.getContents().contains(DISCONNECT)) {
                if (ace4000.getObjectFactory().shouldRetryDisconnectCommand()) {
                    ace4000.log("Sent contactor disconnect command " + (retries + 1) + " times, meter didn't reply.");
                    messageEntry.setFailed();
                }
            }
            if (messageEntry.getContents().contains(DISPLAY_CONFIG)) {
                if (ace4000.getObjectFactory().shouldRetryDisplayConfigRequest()) {
                    ace4000.log("Sent display configuration request " + (retries + 1) + " times, meter didn't reply.");
                    messageEntry.setFailed();
                }
            }
            if (messageEntry.getContents().contains(CONFIG_LOADPROFILE)) {
                if (ace4000.getObjectFactory().shouldRetryLoadProfileConfiguration()) {
                    ace4000.log("Sent load profile data recording configuration request " + (retries + 1) + " times, meter didn't reply.");
                    messageEntry.setFailed();
                }
            }
            if (messageEntry.getContents().contains(CONFIG_SPECIAL_DATA_MODE)) {
                if (ace4000.getObjectFactory().shouldRetrySDMConfiguration()) {
                    ace4000.log("Sent special data mode configuration request " + (retries + 1) + " times, meter didn't reply.");
                    messageEntry.setFailed();
                }
            }
            if (messageEntry.getContents().contains(CONFIG_MAX_DEMAND)) {
                if (ace4000.getObjectFactory().shouldRetryMaxDemandConfiguration()) {
                    ace4000.log("Sent request to configure maximum demand settings " + (retries + 1) + " times, meter didn't reply.");
                    messageEntry.setFailed();
                }
            }
            if (messageEntry.getContents().contains(CONFIG_CONSUMPTION_LIMITATION)) {
                if (ace4000.getObjectFactory().shouldRetryConsumptionLimitationConfiguration()) {
                    ace4000.log("Sent request to configure consumption limitation settings " + (retries + 1) + " times, meter didn't reply.");
                    messageEntry.setFailed();
                }
            }
            if (messageEntry.getContents().contains(CONFIG_EMERGENCY_CONSUMPTION_LIMITATION)) {
                if (ace4000.getObjectFactory().shouldRetryEmergencyConsumptionLimitationConfiguration()) {
                    ace4000.log("Sent request to configure emergency consumption limitation settings " + (retries + 1) + " times, meter didn't reply.");
                    messageEntry.setFailed();
                }
            }
            if (messageEntry.getContents().contains(CONFIG_TARIFF)) {
                if (ace4000.getObjectFactory().shouldRetryEmergencyConsumptionLimitationConfiguration()) {
                    ace4000.log("Sent request to configure tariff settings " + (retries + 1) + " times, meter didn't reply.");
                    messageEntry.setFailed();
                }
            }
            if (messageEntry.getContents().contains(SHORT_DISPLAY_MESSAGE) || messageEntry.getContents().contains(LONG_DISPLAY_MESSAGE) || messageEntry.getContents().contains(NO_DISPLAY_MESSAGE)) {
                if (ace4000.getObjectFactory().shouldRetryDisplayMessageRequest()) {
                    ace4000.log("Sent request for display message " + (retries + 1) + " times, meter didn't reply.");
                    messageEntry.setFailed();
                }
            }
        }
    }
}