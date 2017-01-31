/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.wavesense;

import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.data.MessageResult;
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

import com.energyict.protocolimpl.coronis.wavesense.core.radiocommand.ModuleType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WaveSenseMessages implements MessageProtocol {

    WaveSense waveSense;
    private static final int MAX_SAMPLING_INTERVAL_SECONDS = 63 * 30 * 60;  //See documentation, largest interval possible is 31,5 hours.
    private ModuleType moduleType = new ModuleType(waveSense, 0x22);

    public void setModuleType(ModuleType moduleType) {
        this.moduleType = moduleType;
    }

    WaveSenseMessages(WaveSense waveSense) {
        this.waveSense = waveSense;
    }

    private String stripOffTag(String content) {
        return content.substring(content.indexOf(">") + 1, content.lastIndexOf("<"));
    }

    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        try {
            if (messageEntry.getContent().indexOf("<RestartDataLogging") >= 0) {
                return restartDataLogging(messageEntry);
            } else if (messageEntry.getContent().indexOf("<ForceTimeSync") >= 0) {
                return forceTimeSync(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SetOperatingMode") >= 0) {
                return setOperatingMode(messageEntry);
            } else if (messageEntry.getContent().indexOf("<ResetApplicationStatus") >= 0) {
                return resetApplicationSatus(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SetDayOfWeek") >= 0) {
                return setDayOfWeek(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SetPeriodicStepLogging") >= 0) {
                return setPeriodicStepLogging(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SetMonthlyLogging") >= 0) {
                return setMonthlyLogging(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SetWeeklyLogging") >= 0) {
                return setWeeklyLogging(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SetHourOfMeasurement") >= 0) {
                return setHourOfMeasurement(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SetProfileInterval") >= 0) {
                return setProfileDataInterval(messageEntry);
            } else if (messageEntry.getContent().indexOf("<StopDataLogging") >= 0) {
                return stopDataLogging(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SetMeasurementPeriod") >= 0) {
                return setMeasurementPeriod(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SetHighThreshold") >= 0) {
                return setHighThreshold(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SetLowThreshold") >= 0) {
                return setLowThreshold(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SetHighThresholdExcessTime") >= 0) {
                return setHighThresholdExcessTime(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SetLowThresholdExcessTime") >= 0) {
                return setLowThresholdExcessTime(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SendAlarmOnLowThreshold") >= 0) {
                return sendAlarmOnLowThreshold(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SendAlarmOnHighThreshold") >= 0) {
                return sendAlarmOnHighThreshold(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SendAlarmOnBatteryEnd") >= 0) {
                return sendAlarmOnBatteryEnd(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SendAlarmOnSensorFault") >= 0) {
                return sendAlarmOnSensorFault(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SendAllAlarms") >= 0) {
                return sendAllAlarms(messageEntry);
            } else if (messageEntry.getContent().indexOf("<DisableAlarmOnLowThreshold") >= 0) {
                return disableAlarmOnLowThreshold(messageEntry);
            } else if (messageEntry.getContent().indexOf("<DisableAlarmOnHighThreshold") >= 0) {
                return disableAlarmOnHighThreshold(messageEntry);
            } else if (messageEntry.getContent().indexOf("<DisableAlarmOnBatteryEnd") >= 0) {
                return disableAlarmOnBatteryEnd(messageEntry);
            } else if (messageEntry.getContent().indexOf("<DisableAlarmOnSensorFault") >= 0) {
                return disableAlarmOnSensorFault(messageEntry);
            } else if (messageEntry.getContent().indexOf("<DisableAllAlarms") >= 0) {
                return disableAllAlarms(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SetAlarmConfig") >= 0) {
                return setAlarmConfig(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SetNumberOfRepeaters") >= 0) {
                return setNumberOfRepeaters(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SetRepeaterAddress") >= 0) {
                return setRepeaterAddress(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SetRecipientAddress") >= 0) {
                return setRecipientAddress(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SetNumberOfRetries") >= 0) {
                return setNumberOfRetries(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SetTimeBetweenRetries") >= 0) {
                return setTimeBetweenRetries(messageEntry);
            } else {
                return MessageResult.createFailed(messageEntry);
            }
        }
        catch (Exception e) {
            waveSense.getLogger().severe("Error parsing message, " + e.getMessage());
            return MessageResult.createFailed(messageEntry);
        }
    }

    private MessageResult setNumberOfRetries(MessageEntry messageEntry) throws IOException {
        waveSense.getLogger().info("************************* SetNumberOfRetries *************************");
        int value = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (value > 0xFF || value < 0) {
            return MessageResult.createFailed(messageEntry);
        }
        waveSense.getParameterFactory().setNumberOfRetries(value);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setTimeBetweenRetries(MessageEntry messageEntry) throws IOException {
        waveSense.getLogger().info("************************* SetTimeBetweenRetries *************************");
        int value = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (value > 0xFF || value < 0) {
            return MessageResult.createFailed(messageEntry);
        }
        waveSense.getParameterFactory().setTimeBetweenRetries(value);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setRepeaterAddress(MessageEntry messageEntry) throws IOException {
        waveSense.getLogger().info("************************* SetRepeaterAddress *************************");
        String[] parts = messageEntry.getContent().split("=");
        int number = Integer.parseInt(parts[1].substring(1, 2));
        if (number > 3 || number < 0) {
            return MessageResult.createFailed(messageEntry);
        }
        String address = parts[2].substring(1, 13);
        waveSense.getParameterFactory().writeRepeaterAddress(address, number);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setRecipientAddress(MessageEntry messageEntry) throws IOException {
        waveSense.getLogger().info("************************* SetRecipientAddress *************************");
        String[] parts = messageEntry.getContent().split("=");
        String address = parts[1].substring(1, 13);
        waveSense.getParameterFactory().writeRecipientAddress(address);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setNumberOfRepeaters(MessageEntry messageEntry) throws IOException {
        waveSense.getLogger().info("************************* SetNumberOfRepeaters *************************");
        int value = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (value > 3 || value < 0) {
            return MessageResult.createFailed(messageEntry);
        }
        waveSense.getParameterFactory().writeNumberOfRepeaters(value);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setAlarmConfig(MessageEntry messageEntry) throws IOException {
        waveSense.getLogger().info("************************* SetAlarmConfig *************************");
        int value = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (value > 0xFF || value < 0) {
            return MessageResult.createFailed(messageEntry);
        }
        waveSense.getParameterFactory().writeAlarmConfigurationByte(value);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult sendAlarmOnLowThreshold(MessageEntry messageEntry) throws IOException {
        waveSense.getLogger().info("************************* SendAlarmOnLowThreshold *************************");
        waveSense.getParameterFactory().sendAlarmOnLowThreshold();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult sendAlarmOnHighThreshold(MessageEntry messageEntry) throws IOException {
        waveSense.getLogger().info("************************* sendAlarmOnHighThreshold *************************");
        waveSense.getParameterFactory().sendAlarmOnHighThreshold();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult sendAlarmOnBatteryEnd(MessageEntry messageEntry) throws IOException {
        waveSense.getLogger().info("************************* sendAlarmOnBatteryEnd *************************");
        waveSense.getParameterFactory().sendAlarmOnBatteryEnd();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult sendAlarmOnSensorFault(MessageEntry messageEntry) throws IOException {
        waveSense.getLogger().info("************************* sendAlarmOnSensorFault *************************");
        waveSense.getParameterFactory().sendAlarmOnSensorFault();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult sendAllAlarms(MessageEntry messageEntry) throws IOException {
        waveSense.getLogger().info("************************* sendAllAlarms *************************");
        waveSense.getParameterFactory().sendAllAlarms();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult disableAlarmOnLowThreshold(MessageEntry messageEntry) throws IOException {
        waveSense.getLogger().info("************************* disableAlarmOnLowThreshold *************************");
        waveSense.getParameterFactory().disableAlarmOnLowThreshold();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult disableAlarmOnHighThreshold(MessageEntry messageEntry) throws IOException {
        waveSense.getLogger().info("************************* disableAlarmOnHighThreshold *************************");
        waveSense.getParameterFactory().disableAlarmOnHighThreshold();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult disableAlarmOnBatteryEnd(MessageEntry messageEntry) throws IOException {
        waveSense.getLogger().info("************************* disableAlarmOnBatteryEnd *************************");
        waveSense.getParameterFactory().disableAlarmOnBatteryEnd();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult disableAlarmOnSensorFault(MessageEntry messageEntry) throws IOException {
        waveSense.getLogger().info("************************* disableAlarmOnSensorFault *************************");
        waveSense.getParameterFactory().disableAlarmOnSensorFault();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult disableAllAlarms(MessageEntry messageEntry) throws IOException {
        waveSense.getLogger().info("************************* disableAllAlarms *************************");
        waveSense.getParameterFactory().disableAllAlarms();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult resetApplicationSatus(MessageEntry messageEntry) throws IOException {
        waveSense.getLogger().info("************************* ResetApplicationStatus *************************");
        waveSense.getParameterFactory().writeApplicationStatus(0);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setMeasurementPeriod(MessageEntry messageEntry) throws IOException {
        waveSense.getLogger().info("************************* SetMeasurementPeriod *************************");
        int period = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (period < 1 || period > 0xFF) {
            return MessageResult.createFailed(messageEntry);
        } else {
            waveSense.getParameterFactory().setMeasurementPeriod(period);
            return MessageResult.createSuccess(messageEntry);
        }
    }

    private MessageResult setHighThreshold(MessageEntry messageEntry) throws IOException {
        waveSense.getLogger().info("************************* SetHighThreshold *************************");
        int threshold = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (threshold < 0 || threshold > 0xFFFF) {
            return MessageResult.createFailed(messageEntry);
        } else {
            waveSense.getParameterFactory().setHighThreshold(threshold);
            return MessageResult.createSuccess(messageEntry);
        }
    }

    private MessageResult setLowThreshold(MessageEntry messageEntry) throws IOException {
        waveSense.getLogger().info("************************* SetLowThreshold *************************");
        int threshold = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (threshold < 0 || threshold > 0xFFFF) {
            return MessageResult.createFailed(messageEntry);
        } else {
            waveSense.getParameterFactory().setLowThreshold(threshold);
            return MessageResult.createSuccess(messageEntry);
        }
    }

    private MessageResult setHighThresholdExcessTime(MessageEntry messageEntry) throws IOException {
        waveSense.getLogger().info("************************* SetHighThresholdExcessTime *************************");
        int time = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (time < 0 || time > 0xFFFF) {
            return MessageResult.createFailed(messageEntry);
        } else {
            waveSense.getParameterFactory().setHighThresholdExcessTime(time);
            return MessageResult.createSuccess(messageEntry);
        }
    }

    private MessageResult setLowThresholdExcessTime(MessageEntry messageEntry) throws IOException {
        waveSense.getLogger().info("************************* SetLowThresholdExcessTime *************************");
        int time = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (time < 0 || time > 0xFFFF) {
            return MessageResult.createFailed(messageEntry);
        } else {
            waveSense.getParameterFactory().setLowThresholdExcessTime(time);
            return MessageResult.createSuccess(messageEntry);
        }
    }

    private MessageResult setProfileDataInterval(MessageEntry messageEntry) throws IOException {
        waveSense.getLogger().info("************************* Set sampling interval *************************");
        int profileInterval = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (profileInterval < 60 || profileInterval > MAX_SAMPLING_INTERVAL_SECONDS) {
            waveSense.getLogger().severe("Invalid profile interval, must have minimum 60 seconds");
            return MessageResult.createFailed(messageEntry);
        } else {
            waveSense.getParameterFactory().writeSamplingPeriod(profileInterval);
            return MessageResult.createSuccess(messageEntry);
        }
    }

    private MessageResult setHourOfMeasurement(MessageEntry messageEntry) throws IOException {
        waveSense.getLogger().info("************************* SetHourOfMeasurement *************************");
        int time = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (time < 0 || time > 23) {
            return MessageResult.createFailed(messageEntry);
        }
        waveSense.getParameterFactory().setStartHourOfMeasurement(time);           //Checks if its periodic logging or weekly/monthly logging, then writes the proper parameter!
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setWeeklyLogging(MessageEntry messageEntry) throws IOException {
        waveSense.getLogger().info("************************* SetWeeklyLogging *************************");
        waveSense.getParameterFactory().writeWeeklyDataLogging();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setMonthlyLogging(MessageEntry messageEntry) throws IOException {
        waveSense.getLogger().info("************************* SetMonthlyLogging *************************");
        waveSense.getParameterFactory().writeMonthlyDataLogging();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setPeriodicStepLogging(MessageEntry messageEntry) throws IOException {
        waveSense.getLogger().info("************************* SetPeriodicStepLogging *************************");
        waveSense.getParameterFactory().writePeriodicTimeStepDataLogging();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setDayOfWeek(MessageEntry messageEntry) throws IOException {
        waveSense.getLogger().info("************************* SetDayOfWeek *************************");
        int dayOfWeek = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        boolean monthly = waveSense.getParameterFactory().readOperatingMode().isMonthlyMeasurement();
        if (monthly && (dayOfWeek > 28 || dayOfWeek < 1)) {
            return MessageResult.createFailed(messageEntry);
        } else if (!monthly && (dayOfWeek < 0 || dayOfWeek > 6)) {
            return MessageResult.createFailed(messageEntry);
        }
        waveSense.getParameterFactory().writeDayOfWeek(dayOfWeek);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setOperatingMode(MessageEntry messageEntry) throws IOException {
        waveSense.getLogger().info("************************* SetOperatingMode *************************");
        int operationMode = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (operationMode < 0x00 || operationMode > 0xFF) {
            return MessageResult.createFailed(messageEntry);
        }
        waveSense.getParameterFactory().writeOperatingMode(operationMode);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult forceTimeSync(MessageEntry messageEntry) throws IOException {
        waveSense.getLogger().info("************************* ForceTimeSync *************************");
        waveSense.setTime();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult restartDataLogging(MessageEntry messageEntry) throws IOException {
        waveSense.getLogger().info("************************* RestartDataLogging *************************");
        int mode = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (mode < 1 || mode > 3) {
            return MessageResult.createFailed(messageEntry);
        }
        waveSense.getParameterFactory().restartDataLogging(mode);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult stopDataLogging(MessageEntry messageEntry) throws IOException {
        waveSense.getLogger().info("************************* stopDataLogging *************************");
        waveSense.getParameterFactory().stopDataLogging();
        return MessageResult.createSuccess(messageEntry);
    }

    public List getMessageCategories() {
        List theCategories = new ArrayList();

        MessageCategorySpec cat1 = new MessageCategorySpec("Wavesense general messages");
        cat1.addMessageSpec(addBasicMsgWithValue("Set operating mode", "SetOperatingMode", false));
        cat1.addMessageSpec(addBasicMsg("Reset application status", "ResetApplicationStatus", false));
        cat1.addMessageSpec(addBasicMsg("Force to sync the time", "ForceTimeSync", true));
        theCategories.add(cat1);

        MessageCategorySpec cat2 = new MessageCategorySpec("Wavesense data logging messages");
        cat2.addMessageSpec(addBasicMsgWithValue("Restart data logging in mode [periodic steps (1), weekly (2), monthly (3)]", "RestartDataLogging", false));
        cat2.addMessageSpec(addBasicMsgWithValue("Set sampling period in seconds", "SetProfileInterval", true));
        cat2.addMessageSpec(addBasicMsgWithValue("Set day of week (sunday = 0, monday = 1, ...)", "SetDayOfWeek", true));
        cat2.addMessageSpec(addBasicMsgWithValue("Set start hour of measurement (default 00:00)", "SetHourOfMeasurement", true));
        cat2.addMessageSpec(addBasicMsg("Enable weekly data logging", "SetWeeklyLogging", true));
        cat2.addMessageSpec(addBasicMsg("Enable monthly data logging", "SetMonthlyLogging", true));
        cat2.addMessageSpec(addBasicMsg("Set data logging by periodic time steps", "SetPeriodicStepLogging", true));
        cat2.addMessageSpec(addBasicMsg("Stop the data logging", "StopDataLogging", true));
        theCategories.add(cat2);

        MessageCategorySpec cat3 = new MessageCategorySpec("Wavesense alarm detection");
        String unit = moduleType.getUnitDescription();
        cat3.addMessageSpec(addBasicMsgWithValue("Set measurement period of the threshold detection (in minutes)", "SetMeasurementPeriod", true));
        cat3.addMessageSpec(addBasicMsgWithValue("Set high threshold (in " + unit + ")", "SetHighThreshold", true));
        cat3.addMessageSpec(addBasicMsgWithValue("Set low threshold (in " + unit + ")", "SetLowThreshold", true));
        cat3.addMessageSpec(addBasicMsg("Set high threshold excess time (in multiples of the measurement period)", "SetHighThresholdExcessTime", true));
        cat3.addMessageSpec(addBasicMsg("Set low threshold excess time (in multiples of the measurement period)", "SetLowThresholdExcessTime", true));
        theCategories.add(cat3);

        MessageCategorySpec cat4 = new MessageCategorySpec("Wavesense enable alarm frames");
        cat4.addMessageSpec(addBasicMsg("Send alarm on low threshold detection", "SendAlarmOnLowThreshold", true));
        cat4.addMessageSpec(addBasicMsg("Send alarm on high threshold detection", "SendAlarmOnHighThreshold", true));
        cat4.addMessageSpec(addBasicMsg("Send alarm on end of battery detection", "SendAlarmOnBatteryEnd", true));
        cat4.addMessageSpec(addBasicMsg("Send alarm on sensor fault detection (Wavesense 4-20 mA only)", "SendAlarmOnSensorFault", true));
        cat4.addMessageSpec(addBasicMsg("Send all alarms", "SendAllAlarms", true));
        theCategories.add(cat4);

        MessageCategorySpec cat5 = new MessageCategorySpec("Wavesense disable alarm frames");
        cat5.addMessageSpec(addBasicMsg("Disable alarm on low threshold detection", "DisableAlarmOnLowThreshold", true));
        cat5.addMessageSpec(addBasicMsg("Disable alarm on high threshold detection", "DisableAlarmOnHighThreshold", true));
        cat5.addMessageSpec(addBasicMsg("Disable alarm on end of battery detection", "DisableAlarmOnBatteryEnd", true));
        cat5.addMessageSpec(addBasicMsg("Disable alarm on sensor fault detection (Wavesense 4-20 mA only)", "DisableAlarmOnSensorFault", true));
        cat5.addMessageSpec(addBasicMsg("Disable all alarms", "DisableAllAlarms", true));
        theCategories.add(cat5);

        MessageCategorySpec cat6 = new MessageCategorySpec("Wavesense alarm frames configuration");
        cat6.addMessageSpec(addBasicMsgWithValue("Set the alarm configuration byte", "SetAlarmConfig", true));
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
        MessageAttributeSpec addAttribute = new MessageAttributeSpec(attr, true);
        tagSpec.add(addAttribute);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" "); //Disable this field
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    protected MessageSpec addBasicMsgWithTwoAttr(final String keyId, final String tagName, final boolean advanced, String attr1, String attr2) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageAttributeSpec addAttribute1 = new MessageAttributeSpec(attr1, true);
        tagSpec.add(addAttribute1);
        MessageAttributeSpec addAttribute2 = new MessageAttributeSpec(attr2, true);
        tagSpec.add(addAttribute2);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" "); //Disable this field
        tagSpec.add(msgVal);
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
}