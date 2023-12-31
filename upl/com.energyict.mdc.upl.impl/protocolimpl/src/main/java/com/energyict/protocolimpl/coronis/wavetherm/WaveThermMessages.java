package com.energyict.protocolimpl.coronis.wavetherm;

import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageAttribute;
import com.energyict.mdc.upl.messages.legacy.MessageAttributeSpec;
import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageElement;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageSpec;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageTagSpec;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.messages.legacy.MessageValueSpec;

import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WaveThermMessages implements MessageProtocol {

    private WaveTherm waveTherm;
    private static final int MAX_SAMPLING_INTERVAL_SECONDS = 63 * 30 * 60;  //See documentation, largest interval possible is 31,5 hours.

    WaveThermMessages(WaveTherm waveTherm) {
        this.waveTherm = waveTherm;
    }

    private String stripOffTag(String content) {
        return content.substring(content.indexOf(">") + 1, content.lastIndexOf("<"));
    }

    @Override
    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        try {
            if (messageEntry.getContent().contains("<ForceTimeSync")) {
                return forceTimeSync(messageEntry);
            }
            if (messageEntry.getContent().contains("<RestartDataLogging")) {
                return restartDataLogging(messageEntry);
            } else if (messageEntry.getContent().contains("<StopDataLogging")) {
                return stopDataLogging(messageEntry);
            } else if (messageEntry.getContent().contains("<SetOperatingMode")) {
                return setOperatingMode(messageEntry);
            } else if (messageEntry.getContent().contains("<ResetApplicationStatus")) {
                return resetApplicationStatus(messageEntry);
            } else if (messageEntry.getContent().contains("<SetDayOfWeek")) {
                return setDayOfWeek(messageEntry);
            } else if (messageEntry.getContent().contains("<SetHourOfMeasurement")) {
                return setHourOfMeasurement(messageEntry);
            } else if (messageEntry.getContent().contains("<SetPeriodicStepLogging")) {
                return setPeriodicStepLogging(messageEntry);
            } else if (messageEntry.getContent().contains("<SetProfileInterval")) {
                return setProfileInterval(messageEntry);
            } else if (messageEntry.getContent().contains("<SetWeeklyLogging")) {
                return setWeeklyLogging(messageEntry);
            } else if (messageEntry.getContent().contains("<SetMonthlyLogging")) {
                return setMonthlyLogging(messageEntry);
            } else if (messageEntry.getContent().contains("<SendAlarmOnLowThreshold")) {
                return sendAlarmOnLowThreshold(messageEntry);
            } else if (messageEntry.getContent().contains("<SendAlarmOnHighThreshold")) {
                return sendAlarmOnHighThreshold(messageEntry);
            } else if (messageEntry.getContent().contains("<EnableHighThresholdDetection")) {
                return setHighThresholdDetection(messageEntry, 1);
            } else if (messageEntry.getContent().contains("<EnableLowThresholdDetection")) {
                return setLowThresholdDetection(messageEntry, 1);
            } else if (messageEntry.getContent().contains("<DisableHighThresholdDetection")) {
                return setHighThresholdDetection(messageEntry, 0);
            } else if (messageEntry.getContent().contains("<DisableLowThresholdDetection")) {
                return setLowThresholdDetection(messageEntry, 0);
            } else if (messageEntry.getContent().contains("<SendAlarmOnBatteryEnd")) {
                return sendAlarmOnBatteryEnd(messageEntry);
            } else if (messageEntry.getContent().contains("<SendAllAlarms")) {
                return sendAllAlarms(messageEntry);
            } else if (messageEntry.getContent().contains("<DisableAlarmOnLowThreshold")) {
                return disableAlarmOnLowThreshold(messageEntry);
            } else if (messageEntry.getContent().contains("<DisableAlarmOnHighThreshold")) {
                return disableAlarmOnHighThreshold(messageEntry);
            } else if (messageEntry.getContent().contains("<DisableAlarmOnBatteryEnd")) {
                return disableAlarmOnBatteryEnd(messageEntry);
            } else if (messageEntry.getContent().contains("<DisableAllAlarms")) {
                return disableAllAlarms(messageEntry);
            } else if (messageEntry.getContent().contains("<SetAlarmConfig")) {
                return setAlarmConfig(messageEntry);
            } else if (messageEntry.getContent().contains("<SetNumberOfRepeaters")) {
                return setNumberOfRepeaters(messageEntry);
            } else if (messageEntry.getContent().contains("<SetRepeaterAddress")) {
                return setRepeaterAddress(messageEntry);
            } else if (messageEntry.getContent().contains("<SetRecipientAddress")) {
                return setRecipientAddress(messageEntry);
            } else if (messageEntry.getContent().contains("<SetNumberOfRetries")) {
                return setNumberOfRetries(messageEntry);
            } else if (messageEntry.getContent().contains("<SetTimeBetweenRetries")) {
                return setTimeBetweenRetries(messageEntry);
            } else if (messageEntry.getContent().contains("<SetMeasurementPeriod")) {
                return setMeasurementPeriod(messageEntry);
            } else if (messageEntry.getContent().contains("<SetLowThresholdAlarmDuration")) {
                return setLowThresholdAlarmDuration(messageEntry);
            } else if (messageEntry.getContent().contains("<SetHighThresholdAlarmDuration")) {
                return setHighThresholdAlarmDuration(messageEntry);
            } else if (messageEntry.getContent().contains("<SetLowThresholdSensor1")) {
                return setLowThreshold(messageEntry, 1);
            } else if (messageEntry.getContent().contains("<SetLowThresholdSensor2")) {
                return setLowThreshold(messageEntry, 2);
            } else if (messageEntry.getContent().contains("<SetHighThresholdSensor1")) {
                return setHighThreshold(messageEntry, 1);
            } else if (messageEntry.getContent().contains("<SetHighThresholdSensor2")) {
                return setHighThreshold(messageEntry, 2);
            } else if (messageEntry.getContent().contains("<SetCumulativeDetectionMode")) {
                return setCumulativeDetectionMode(messageEntry);
            } else if (messageEntry.getContent().contains("<SetSuccessiveDetectionMode")) {
                return setSuccessiveDetectionMode(messageEntry);
            } else {
                return MessageResult.createFailed(messageEntry);
            }
        }
        catch (Exception e) {
            waveTherm.getLogger().severe("Error parsing message, " + e.getMessage());
            return MessageResult.createFailed(messageEntry);
        }
    }

    private MessageResult setMeasurementPeriod(MessageEntry messageEntry) throws IOException {
        waveTherm.getLogger().info("************************* setMeasurementPeriod *************************");
        int value = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (value > 0xFF || value < 1) {
            return MessageResult.createFailed(messageEntry);
        }
        waveTherm.getParameterFactory().writeMeasurementPeriod(value);
        return MessageResult.createSuccess(messageEntry);

    }

    private MessageResult setCumulativeDetectionMode(MessageEntry messageEntry) throws IOException {
        waveTherm.getLogger().info("************************* setCumulativeDetectionMode *************************");
        waveTherm.getParameterFactory().writeCumulativeDetectionMode();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setSuccessiveDetectionMode(MessageEntry messageEntry) throws IOException {
        waveTherm.getLogger().info("************************* setSuccessiveDetectionMode *************************");
        waveTherm.getParameterFactory().writeSuccessiveDetectionMode();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setLowThreshold(MessageEntry messageEntry, int sensor) throws IOException {
        waveTherm.getLogger().info("************************* setLowThreshold *************************");
        String value = stripOffTag(messageEntry.getContent());
        boolean sign = "-".equals(value.substring(0, 1));
        Float threshold = Float.parseFloat(value.substring((sign) ? 1 : 0));
        threshold = threshold * ((sign) ? -1 : 1);
        if (threshold > 127 || threshold < -127) {
            return MessageResult.createFailed(messageEntry);
        }
        waveTherm.getParameterFactory().writeLowThreshold(threshold, sensor);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setHighThreshold(MessageEntry messageEntry, int sensor) throws IOException {
        waveTherm.getLogger().info("************************* setHighThreshold *************************");
        String value = stripOffTag(messageEntry.getContent());
        boolean sign = "-".equals(value.substring(0, 1));
        Float threshold = Float.parseFloat(value.substring((sign) ? 1 : 0));
        threshold = threshold * ((sign) ? -1 : 1);
        if (threshold > 127 || threshold < -127) {
            return MessageResult.createFailed(messageEntry);
        }
        waveTherm.getParameterFactory().writeHighThreshold(threshold, sensor);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setLowThresholdAlarmDuration(MessageEntry messageEntry) throws IOException {
        waveTherm.getLogger().info("************************* setLowThresholdAlarmDuration *************************");
        String[] parts = messageEntry.getContent().split("=");
        int duration;
        try {
            duration = Integer.parseInt(parts[1].substring(1, 4));
        } catch (NumberFormatException e) {
            try {
                duration = Integer.parseInt(parts[1].substring(1, 3));
            } catch (NumberFormatException e1) {
                duration = Integer.parseInt(parts[1].substring(1, 2));
            }
        }
        if (duration > 0xFF || duration < 0) {
            return MessageResult.createFailed(messageEntry);
        }
        int sensor = Integer.parseInt(parts[2].substring(1, 2));
        if (sensor > 2 || sensor < 1) {
            return MessageResult.createFailed(messageEntry);
        }
        waveTherm.getParameterFactory().writeLowThresholdAlarmDuration(duration, sensor);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setHighThresholdAlarmDuration(MessageEntry messageEntry) throws IOException {
        waveTherm.getLogger().info("************************* setHighThresholdAlarmDuration *************************");
        String[] parts = messageEntry.getContent().split("=");
        int duration;
        try {
            duration = Integer.parseInt(parts[1].substring(1, 4));
        } catch (NumberFormatException e) {
            try {
                duration = Integer.parseInt(parts[1].substring(1, 3));
            } catch (NumberFormatException e1) {
                duration = Integer.parseInt(parts[1].substring(1, 2));
            }
        }
        if (duration > 0xFF || duration < 0) {
            return MessageResult.createFailed(messageEntry);
        }
        int sensor = Integer.parseInt(parts[2].substring(1, 2));
        if (sensor > 2 || sensor < 1) {
            return MessageResult.createFailed(messageEntry);
        }
        waveTherm.getParameterFactory().writeHighThresholdAlarmDuration(duration, sensor);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setWeeklyLogging(MessageEntry messageEntry) throws IOException {
        waveTherm.getLogger().info("************************* SetWeeklyLogging *************************");
        waveTherm.getParameterFactory().writeWeeklyDataLogging();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setNumberOfRetries(MessageEntry messageEntry) throws IOException {
        waveTherm.getLogger().info("************************* SetNumberOfRetries *************************");
        int value = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (value > 0xFF || value < 0) {
            return MessageResult.createFailed(messageEntry);
        }
        waveTherm.getParameterFactory().setNumberOfRetries(value);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setTimeBetweenRetries(MessageEntry messageEntry) throws IOException {
        waveTherm.getLogger().info("************************* setTimeBetweenRetries *************************");
        int value = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (value > 0xFF || value < 0) {
            return MessageResult.createFailed(messageEntry);
        }
        waveTherm.getParameterFactory().setTimeBetweenRetries(value);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setRepeaterAddress(MessageEntry messageEntry) throws IOException {
        waveTherm.getLogger().info("************************* SetRepeaterAddress *************************");
        String[] parts = messageEntry.getContent().split("=");
        int number = Integer.parseInt(parts[1].substring(1, 2));
        if (number > 3 || number < 0) {
            return MessageResult.createFailed(messageEntry);
        }
        String address = parts[2].substring(1, 13);
        waveTherm.getParameterFactory().writeRepeaterAddress(address, number);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setRecipientAddress(MessageEntry messageEntry) throws IOException {
        waveTherm.getLogger().info("************************* SetRecipientAddress *************************");
        String[] parts = messageEntry.getContent().split("=");
        String address = parts[1].substring(1, 13);
        waveTherm.getParameterFactory().writeRecipientAddress(address);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setNumberOfRepeaters(MessageEntry messageEntry) throws IOException {
        waveTherm.getLogger().info("************************* SetNumberOfRepeaters *************************");
        int value = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (value > 3 || value < 0) {
            return MessageResult.createFailed(messageEntry);
        }
        waveTherm.getParameterFactory().writeNumberOfRepeaters(value);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setAlarmConfig(MessageEntry messageEntry) throws IOException {
        waveTherm.getLogger().info("************************* SetAlarmConfig *************************");
        int value = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (value > 0xFF || value < 0) {
            return MessageResult.createFailed(messageEntry);
        }
        waveTherm.getParameterFactory().writeAlarmConfigurationByte(value);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult sendAlarmOnLowThreshold(MessageEntry messageEntry) throws IOException {
        waveTherm.getLogger().info("************************* SendAlarmOnLowThreshold *************************");
        waveTherm.getParameterFactory().sendAlarmOnLowThreshold();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult sendAlarmOnHighThreshold(MessageEntry messageEntry) throws IOException {
        waveTherm.getLogger().info("************************* sendAlarmOnHighThreshold *************************");
        waveTherm.getParameterFactory().sendAlarmOnHighThreshold();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setHighThresholdDetection(MessageEntry messageEntry, int enabled) throws IOException {
        waveTherm.getLogger().info("************************* EnableHighThresholdDetection *************************");
        waveTherm.getParameterFactory().setHighThresholdDetection(enabled);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setLowThresholdDetection(MessageEntry messageEntry, int enabled) throws IOException {
        waveTherm.getLogger().info("************************* EnableLowThresholdDetection *************************");
        waveTherm.getParameterFactory().setLowThresholdDetection(enabled);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult sendAlarmOnBatteryEnd(MessageEntry messageEntry) throws IOException {
        waveTherm.getLogger().info("************************* sendAlarmOnBatteryEnd *************************");
        waveTherm.getParameterFactory().sendAlarmOnBatteryEnd();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setDayOfWeek(MessageEntry messageEntry) throws IOException {
        waveTherm.getLogger().info("************************* SetDayOfWeek *************************");
        int dayOfWeek = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        boolean monthly = waveTherm.getParameterFactory().readOperatingMode().isMonthlyMeasurement();
        if (monthly && (dayOfWeek > 28 || dayOfWeek < 1)) {
            return MessageResult.createFailed(messageEntry);
        } else if (!monthly && (dayOfWeek < 0 || dayOfWeek > 6)) {
            return MessageResult.createFailed(messageEntry);
        }
        waveTherm.getParameterFactory().writeDayOfWeek(dayOfWeek);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setMonthlyLogging(MessageEntry messageEntry) throws IOException {
        waveTherm.getLogger().info("************************* SetMonthlyLogging *************************");
        waveTherm.getParameterFactory().writeMonthlyDataLogging();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setHourOfMeasurement(MessageEntry messageEntry) throws IOException {
        waveTherm.getLogger().info("************************* SetHourOfMeasurement *************************");
        int time = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (time < 0 || time > 23) {
            return MessageResult.createFailed(messageEntry);
        }
        waveTherm.getParameterFactory().setStartHourOfMeasurement(time);           //Checks if its periodic logging or weekly/monthly logging, then writes the proper parameter!
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setPeriodicStepLogging(MessageEntry messageEntry) throws IOException {
        waveTherm.getLogger().info("************************* SetPeriodicStepLogging *************************");
        waveTherm.getParameterFactory().writePeriodicTimeStepDataLogging();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setOperatingMode(MessageEntry messageEntry) throws IOException {
        waveTherm.getLogger().info("************************* SetOperatingMode *************************");
        int operationMode = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (operationMode < 0x00 || operationMode > 0xFF) {
            return MessageResult.createFailed(messageEntry);
        }
        waveTherm.getParameterFactory().writeOperatingMode(operationMode);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult stopDataLogging(MessageEntry messageEntry) throws IOException {
        waveTherm.getLogger().info("************************* stopDataLogging *************************");
        waveTherm.getParameterFactory().stopDataLogging();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult resetApplicationStatus(MessageEntry messageEntry) throws IOException {
        waveTherm.getLogger().info("************************* ResetApplicationStatus *************************");
        waveTherm.getParameterFactory().writeApplicationStatus(0);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult forceTimeSync(MessageEntry messageEntry) throws IOException {
        waveTherm.getLogger().info("************************* ForceTimeSync *************************");
        waveTherm.setTime();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setProfileInterval(MessageEntry messageEntry) throws IOException {
        waveTherm.getLogger().info("************************* Set sampling interval *************************");
        int profileInterval = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (profileInterval < 60 || profileInterval > MAX_SAMPLING_INTERVAL_SECONDS) {
            waveTherm.getLogger().severe("Invalid profile interval, must have minimum 60 seconds");
            return MessageResult.createFailed(messageEntry);
        } else {
            waveTherm.getParameterFactory().writeSamplingPeriod(profileInterval);
            return MessageResult.createSuccess(messageEntry);
        }
    }

    private MessageResult restartDataLogging(MessageEntry messageEntry) throws IOException {
        waveTherm.getLogger().info("************************* RestartDataLogging *************************");
        int mode = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (mode < 1 || mode > 3) {
            return MessageResult.createFailed(messageEntry);
        }
        waveTherm.getParameterFactory().restartDataLogging(mode);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult sendAllAlarms(MessageEntry messageEntry) throws IOException {
        waveTherm.getLogger().info("************************* sendAllAlarms *************************");
        waveTherm.getParameterFactory().sendAllAlarms();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult disableAlarmOnLowThreshold(MessageEntry messageEntry) throws IOException {
        waveTherm.getLogger().info("************************* disableAlarmOnLowThreshold *************************");
        waveTherm.getParameterFactory().disableAlarmOnLowThreshold();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult disableAlarmOnHighThreshold(MessageEntry messageEntry) throws IOException {
        waveTherm.getLogger().info("************************* disableAlarmOnHighThreshold *************************");
        waveTherm.getParameterFactory().disableAlarmOnHighThreshold();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult disableAlarmOnBatteryEnd(MessageEntry messageEntry) throws IOException {
        waveTherm.getLogger().info("************************* disableAlarmOnBatteryEnd *************************");
        waveTherm.getParameterFactory().disableAlarmOnBatteryEnd();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult disableAllAlarms(MessageEntry messageEntry) throws IOException {
        waveTherm.getLogger().info("************************* disableAllAlarms *************************");
        waveTherm.getParameterFactory().disableAllAlarms();
        return MessageResult.createSuccess(messageEntry);
    }

    @Override
    public List getMessageCategories() {
        List<MessageCategorySpec> theCategories = new ArrayList<>();

        MessageCategorySpec cat1 = new MessageCategorySpec("Wavetherm general messages");
        cat1.addMessageSpec(addBasicMsgWithValue("Set operating mode", "SetOperatingMode", false));
        cat1.addMessageSpec(addBasicMsg("Reset application status", "ResetApplicationStatus", false));
        cat1.addMessageSpec(addBasicMsg("Force to sync the time", "ForceTimeSync", true));
        theCategories.add(cat1);

        MessageCategorySpec cat2 = new MessageCategorySpec("Wavetherm data logging messages");
        cat2.addMessageSpec(addBasicMsgWithValue("Restart data logging in mode [periodic steps (1), weekly (2), monthly (3)]", "RestartDataLogging", false));
        cat2.addMessageSpec(addBasicMsgWithValue("Set sampling period in seconds", "SetProfileInterval", true));
        cat2.addMessageSpec(addBasicMsgWithValue("Set day of week (sunday = 0, monday = 1, ...)", "SetDayOfWeek", true));
        cat2.addMessageSpec(addBasicMsgWithValue("Set start hour of measurement (default 00:00)", "SetHourOfMeasurement", true));
        cat2.addMessageSpec(addBasicMsg("Enable weekly data logging", "SetWeeklyLogging", true));
        cat2.addMessageSpec(addBasicMsg("Enable monthly data logging", "SetMonthlyLogging", true));
        cat2.addMessageSpec(addBasicMsg("Set data logging by periodic time steps", "SetPeriodicStepLogging", true));
        cat2.addMessageSpec(addBasicMsg("Stop the data logging", "StopDataLogging", true));
        theCategories.add(cat2);

        MessageCategorySpec cat3 = new MessageCategorySpec("Wavetherm detection");
        cat3.addMessageSpec(addBasicMsgWithValue("Set measurement period of the threshold detection (in minutes)", "SetMeasurementPeriod", true));
        cat3.addMessageSpec(addBasicMsg("Set detection mode to cumulative", "SetCumulativeDetectionMode", true));
        cat3.addMessageSpec(addBasicMsg("Set detection mode to successive", "SetSuccessiveDetectionMode", true));
        cat3.addMessageSpec(addBasicMsg("Enable high threshold detection", "EnableHighThresholdDetection", true));
        cat3.addMessageSpec(addBasicMsg("Enable low threshold detection", "EnableLowThresholdDetection", true));
        cat3.addMessageSpec(addBasicMsg("Disable high threshold detection", "DisableHighThresholdDetection", true));
        cat3.addMessageSpec(addBasicMsg("Disable low threshold detection", "DisableLowThresholdDetection", true));
        cat3.addMessageSpec(addBasicMsgWithTwoAttr("Set low threshold alarm duration", "SetLowThresholdAlarmDuration", true, "Duration in multiples of the measurement period", "Sensor id (1 or 2)"));
        cat3.addMessageSpec(addBasicMsgWithTwoAttr("Set high threshold alarm duration", "SetHighThresholdAlarmDuration", true, "Duration in multiples of the measurement period", "Sensor id (1 or 2)"));
        cat3.addMessageSpec(addBasicMsgWithValue("Set low temperature threshold on sensor 1 (between -127 and 127 °C), floating point allowed", "SetLowThresholdSensor1", true));
        cat3.addMessageSpec(addBasicMsgWithValue("Set low temperature threshold on sensor 2 (between -127 and 127 °C), floating point allowed", "SetLowThresholdSensor2", true));
        cat3.addMessageSpec(addBasicMsgWithValue("Set high temperature threshold on sensor 1 (between -127 and 127 °C), floating point allowed", "SetHighThresholdSensor1", true));
        cat3.addMessageSpec(addBasicMsgWithValue("Set high temperature threshold on sensor 2 (between -127 and 127 °C), floating point allowed", "SetHighThresholdSensor2", true));
        theCategories.add(cat3);

        MessageCategorySpec cat4 = new MessageCategorySpec("Wavetherm enable alarm frames");
        cat4.addMessageSpec(addBasicMsg("Send alarm on low threshold detection", "SendAlarmOnLowThreshold", true));
        cat4.addMessageSpec(addBasicMsg("Send alarm on high threshold detection", "SendAlarmOnHighThreshold", true));
        cat4.addMessageSpec(addBasicMsg("Send alarm on end of battery detection", "SendAlarmOnBatteryEnd", true));
        cat4.addMessageSpec(addBasicMsg("Send all alarms", "SendAllAlarms", true));
        theCategories.add(cat4);

        MessageCategorySpec cat5 = new MessageCategorySpec("Wavetherm disable alarm frames");
        cat5.addMessageSpec(addBasicMsg("Disable alarm on low threshold detection", "DisableAlarmOnLowThreshold", true));
        cat5.addMessageSpec(addBasicMsg("Disable alarm on high threshold detection", "DisableAlarmOnHighThreshold", true));
        cat5.addMessageSpec(addBasicMsg("Disable alarm on end of battery detection", "DisableAlarmOnBatteryEnd", true));
        cat5.addMessageSpec(addBasicMsg("Disable all alarms", "DisableAllAlarms", true));
        theCategories.add(cat5);

        MessageCategorySpec cat6 = new MessageCategorySpec("Wavetherm alarm frames configuration");
        //cat6.addMessageSpec(addBasicMsgWithValue("Set the alarm configuration byte", "SetAlarmConfig", true));
        cat6.addMessageSpec(addBasicMsgWithValue("Set number of repeaters (max 3)", "SetNumberOfRepeaters", true));
        cat6.addMessageSpec(addBasicMsgWithTwoAttr("Set address of repeater", "SetRepeaterAddress", true, "Number of the repeater (1, 2 or 3)", "Address (hex string)"));
        cat6.addMessageSpec(addBasicMsgWithAttr("Set address of the recipient", "SetRecipientAddress", true, "Address (hex string)"));
        cat6.addMessageSpec(addBasicMsgWithValue("Set number of retries for an alarm transmission", "SetNumberOfRetries", true));
        cat6.addMessageSpec(addBasicMsgWithValue("Set time between retries for alarm transmissions", "SetTimeBetweenRetries", true));

        theCategories.add(cat6);

        return theCategories;
    }

    protected MessageSpec addBasicMsgWithAttr(final String keyId, final String tagName, final boolean advanced, String attr) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageAttributeSpec(attr, true));
        tagSpec.add(new MessageValueSpec(" "));
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    protected MessageSpec addBasicMsgWithTwoAttr(final String keyId, final String tagName, final boolean advanced, String attr1, String attr2) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageAttributeSpec(attr1, true));
        tagSpec.add(new MessageAttributeSpec(attr2, true));
        tagSpec.add(new MessageValueSpec(" "));
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    private MessageSpec addBasicMsg(final String keyId, final String tagName, final boolean advanced) {
        final MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        final MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    protected MessageSpec addBasicMsgWithValue(final String keyId, final String tagName, final boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageValueSpec());
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    @Override
    public String writeMessage(Message msg) {
        return msg.write(this);
    }

    @Override
    public String writeTag(MessageTag msgTag) {
        StringBuilder builder = new StringBuilder();

        // a. Opening tag
        builder.append("<");
        builder.append(msgTag.getName());

        // b. Attributes
        for (MessageAttribute att : msgTag.getAttributes()) {
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

    @Override
    public String writeValue(MessageValue value) {
        return value.getValue();
    }

    @Override
    public void applyMessages(List<MessageEntry> messageEntries) throws IOException {
    }

}