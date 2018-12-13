package com.energyict.protocolimpl.coronis.wavelog;

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

public class WaveLogMessages implements MessageProtocol {

    WaveLog waveLog;

    WaveLogMessages(WaveLog waveLog) {
        this.waveLog = waveLog;
    }

    private String stripOffTag(String content) {
        return content.substring(content.indexOf(">") + 1, content.lastIndexOf("<"));
    }

    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        try {
            if (messageEntry.getContent().contains("<ForceTimeSync")) {
                return forceTimeSync(messageEntry);
            } else if (messageEntry.getContent().contains("<SetOperatingMode")) {
                return setOperatingMode(messageEntry);
            } else if (messageEntry.getContent().contains("<WriteOutputLevel")) {
                return writeOutputLevel(messageEntry);
            } else if (messageEntry.getContent().contains("<WriteInput1ConfigurationByte")) {
                return writeInputConfigurationByte(1, messageEntry);
            } else if (messageEntry.getContent().contains("<WriteInput2ConfigurationByte")) {
                return writeInputConfigurationByte(2, messageEntry);
            } else if (messageEntry.getContent().contains("<WriteInput3ConfigurationByte")) {
                return writeInputConfigurationByte(3, messageEntry);
            } else if (messageEntry.getContent().contains("<WriteInput4ConfigurationByte")) {
                return writeInputConfigurationByte(4, messageEntry);
            } else if (messageEntry.getContent().contains("<WriteInput1StabilityDuration")) {
                return writeInputStabilityDuration(1, messageEntry);
            } else if (messageEntry.getContent().contains("<WriteInput2StabilityDuration")) {
                return writeInputStabilityDuration(2, messageEntry);
            } else if (messageEntry.getContent().contains("<WriteInput3StabilityDuration")) {
                return writeInputStabilityDuration(3, messageEntry);
            } else if (messageEntry.getContent().contains("<WriteInput4StabilityDuration")) {
                return writeInputStabilityDuration(4, messageEntry);
            } else if (messageEntry.getContent().contains("<WriteOutput1ImpulseDuration")) {
                return writeOutputImpulseDuration(1, messageEntry);
            } else if (messageEntry.getContent().contains("<WriteOutput2ImpulseDuration")) {
                return writeOutputImpulseDuration(2, messageEntry);
            } else if (messageEntry.getContent().contains("<WriteOutput3ImpulseDuration")) {
                return writeOutputImpulseDuration(3, messageEntry);
            } else if (messageEntry.getContent().contains("<WriteOutput4ImpulseDuration")) {
                return writeOutputImpulseDuration(4, messageEntry);
            } else if (messageEntry.getContent().contains("<ResetEventTable")) {
                return resetEventTable(messageEntry);
            } else if (messageEntry.getContent().contains("<InitializeAlarmRoute")) {
                return initializeAlarmRoute(messageEntry);
            } else if (messageEntry.getContent().contains("<InitializeRouteAndConfigBytes")) {
                return initializeAlarmRouteAndConfigByte(messageEntry);
            } else if (messageEntry.getContent().contains("<ResetApplicationStatus")) {
                return resetApplicationStatus(messageEntry);
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
            } else if (messageEntry.getContent().contains("<SetTimeBetweenPeriodicRetries")) {
                return setTimeBetweenPeriodicRetries(messageEntry);
            } else if (messageEntry.getContent().contains("<SetTransmissionPeriodOfPeriodicFrames")) {
                return setTransmissionPeriodOfPeriodicFrames(messageEntry);
            } else if (messageEntry.getContent().contains("<SetNumberOfPeriodicRetries")) {
                return setNumberOfPeriodicRetries(messageEntry);
            } else {
                return MessageResult.createFailed(messageEntry);
            }
        }
        catch (Exception e) {
            waveLog.getLogger().severe("Error parsing message, " + e.getMessage());
            return MessageResult.createFailed(messageEntry);
        }
    }

    private MessageResult writeOutputLevel(MessageEntry messageEntry) throws IOException {
        waveLog.getLogger().info("************************* writeOutputLevel *************************");
        String[] parts = messageEntry.getContent().split("=");
        int output = Integer.parseInt(parts[1].substring(1, 2));
        if (output > 4 || output < 0) {
            return MessageResult.createFailed(messageEntry);
        }
        int type = Integer.parseInt(parts[4].substring(1, 2));
        if (type > 1 || type < 0) {
            return MessageResult.createFailed(messageEntry);
        }
        int level = Integer.parseInt(parts[7].substring(1, 2));
        if (level > 1 || level < 0) {
            return MessageResult.createFailed(messageEntry);
        }
        waveLog.getParameterFactory().writeOutputLevel(output, type, level);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setTimeBetweenPeriodicRetries(MessageEntry messageEntry) throws IOException {
        waveLog.getLogger().info("************************* SetTimeBetweenPeriodicRetries *************************");
        int value = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (value > 0xFF || value < 0) {
            return MessageResult.createFailed(messageEntry);
        }
        waveLog.getParameterFactory().setTimeBetweenPeriodicRetries(value);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setNumberOfPeriodicRetries(MessageEntry messageEntry) throws IOException {
        waveLog.getLogger().info("************************* setNumberOfPeriodicRetries *************************");
        int value = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (value > 0x05 || value < 0) {
            return MessageResult.createFailed(messageEntry);
        }
        waveLog.getParameterFactory().setNumberOfPeriodicRetries(value);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setTransmissionPeriodOfPeriodicFrames(MessageEntry messageEntry) throws IOException {
        waveLog.getLogger().info("************************* setTransmissionPeriodOfPeriodicFrames *************************");
        int period = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (period > 0xFF || period < 0) {
            return MessageResult.createFailed(messageEntry);
        }
        waveLog.getParameterFactory().setTransmissionPeriodOfPeriodicFrames(period);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult writeInputConfigurationByte(int input, MessageEntry messageEntry) throws IOException {
        waveLog.getLogger().info("************************* writeInputConfigurationByte *************************");
        int config = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (config > 0xFF || config < 0) {
            return MessageResult.createFailed(messageEntry);
        }
        waveLog.getParameterFactory().writeInputConfigurationByte(config, input);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult writeOutputImpulseDuration(int input, MessageEntry messageEntry) throws IOException {
        waveLog.getLogger().info("************************* writeOutputImpulseDuration *************************");
        int duration = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (duration < 1 || duration > 0x14) {
            return MessageResult.createFailed(messageEntry);
        }
        waveLog.getParameterFactory().writeImpulseDuration(duration, input);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult writeInputStabilityDuration(int input, MessageEntry messageEntry) throws IOException {
        waveLog.getLogger().info("************************* writeInputStabilityDuration *************************");
        int duration = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (duration < 1) {
            return MessageResult.createFailed(messageEntry);
        }
        waveLog.getParameterFactory().writeStabilityDuration(duration, input);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult resetEventTable(MessageEntry messageEntry) throws IOException {
        waveLog.getLogger().info("************************* resetEventTable *************************");
        waveLog.getRadioCommandFactory().resetEventTable();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult initializeAlarmRouteAndConfigByte(MessageEntry messageEntry) throws IOException {
        waveLog.getLogger().info("************************* initializeAlarmRouteAndConfigurationBytes *************************");
        String[] parts = messageEntry.getContent().split("=");
        byte[] configBytes = new byte[4];

        for (int i = 0; i < 4; i++) {
            try {
                configBytes[i] = (byte) Integer.parseInt(parts[1 + i].substring(1, 4));
            } catch (NumberFormatException e) {
                try {
                    configBytes[i] = (byte) Integer.parseInt(parts[1 + i].substring(1, 3));
                } catch (NumberFormatException e1) {
                    configBytes[i] = (byte) Integer.parseInt(parts[1 + i].substring(1, 2));
                }
            }
            if (configBytes[i] > 0xFF || configBytes[i] < 0) {
                return MessageResult.createFailed(messageEntry);
            }
        }
        waveLog.getRadioCommandFactory().initializeAlarmRoute();
        waveLog.getParameterFactory().writeInputConfigurationByte(configBytes[0], 1);
        waveLog.getParameterFactory().writeInputConfigurationByte(configBytes[1], 2);
        waveLog.getParameterFactory().writeInputConfigurationByte(configBytes[2], 3);
        waveLog.getParameterFactory().writeInputConfigurationByte(configBytes[3], 4);

        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult initializeAlarmRoute(MessageEntry messageEntry) throws IOException {
        waveLog.getLogger().info("************************* initializeAlarmRoute *************************");
        waveLog.getRadioCommandFactory().initializeAlarmRoute();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setNumberOfRetries(MessageEntry messageEntry) throws IOException {
        waveLog.getLogger().info("************************* SetNumberOfRetries *************************");
        int value = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (value > 0x05 || value < 0) {
            return MessageResult.createFailed(messageEntry);
        }
        waveLog.getParameterFactory().setNumberOfRetries(value);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setTimeBetweenRetries(MessageEntry messageEntry) throws IOException {
        waveLog.getLogger().info("************************* setTimeBetweenRetries *************************");
        int value = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (value > 0xFF || value < 0) {
            return MessageResult.createFailed(messageEntry);
        }
        waveLog.getParameterFactory().setTimeBetweenRetries(value);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setRepeaterAddress(MessageEntry messageEntry) throws IOException {
        waveLog.getLogger().info("************************* SetRepeaterAddress *************************");
        String[] parts = messageEntry.getContent().split("=");
        int number = Integer.parseInt(parts[1].substring(1, 2));
        if (number > 3 || number < 0) {
            return MessageResult.createFailed(messageEntry);
        }
        String address = parts[2].substring(1, 13);
        waveLog.getParameterFactory().writeRepeaterAddress(address, number);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setRecipientAddress(MessageEntry messageEntry) throws IOException {
        waveLog.getLogger().info("************************* SetRecipientAddress *************************");
        String[] parts = messageEntry.getContent().split("=");
        String address = parts[1].substring(1, 13);
        waveLog.getParameterFactory().writeRecipientAddress(address);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setNumberOfRepeaters(MessageEntry messageEntry) throws IOException {
        waveLog.getLogger().info("************************* SetNumberOfRepeaters *************************");
        int value = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (value > 3 || value < 0) {
            return MessageResult.createFailed(messageEntry);
        }
        waveLog.getParameterFactory().writeNumberOfRepeaters(value);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setOperatingMode(MessageEntry messageEntry) throws IOException {
        waveLog.getLogger().info("************************* SetOperatingMode *************************");
        int operationMode = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (operationMode < 0x00 || operationMode > 0xFF) {
            return MessageResult.createFailed(messageEntry);
        }
        waveLog.getParameterFactory().writeOperatingMode(operationMode);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult resetApplicationStatus(MessageEntry messageEntry) throws IOException {
        waveLog.getLogger().info("************************* ResetApplicationStatus *************************");
        waveLog.getParameterFactory().writeApplicationStatus(0);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult forceTimeSync(MessageEntry messageEntry) throws IOException {
        waveLog.getLogger().info("************************* ForceTimeSync *************************");
        waveLog.setTime();
        return MessageResult.createSuccess(messageEntry);
    }

    public List getMessageCategories() {
        List<MessageCategorySpec> theCategories = new ArrayList<>();

        MessageCategorySpec cat1 = new MessageCategorySpec("Wavelog general messages");
        cat1.addMessageSpec(addBasicMsgWithValue("Set operating mode", "SetOperatingMode", false));
        cat1.addMessageSpec(addBasicMsg("Reset application status", "ResetApplicationStatus", false));
        cat1.addMessageSpec(addBasicMsg("Force to sync the time", "ForceTimeSync", true));
        cat1.addMessageSpec(addBasicMsgWithThreeAttr("Write output level", "WriteOutputLevel", true, "Output (1, 2, 3 or 4)", "Activation type (0 = pulse, 1 = permanent state)", "Level (0 = 0 Volt, 1 = 2.7 Volt)"));
        theCategories.add(cat1);

        MessageCategorySpec cat2 = new MessageCategorySpec("Wavelog event configuration");
        cat2.addMessageSpec(addBasicMsg("Reset the event table", "ResetEventTable", true));
        cat2.addMessageSpec(addBasicMsgWithValue("Write input 1 configuration byte", "WriteInput1ConfigurationByte", true));
        cat2.addMessageSpec(addBasicMsgWithValue("Write input 2 configuration byte", "WriteInput2ConfigurationByte", true));
        cat2.addMessageSpec(addBasicMsgWithValue("Write input 3 configuration byte", "WriteInput3ConfigurationByte", true));
        cat2.addMessageSpec(addBasicMsgWithValue("Write input 4 configuration byte", "WriteInput4ConfigurationByte", true));
        cat2.addMessageSpec(addBasicMsgWithValue("Write input 1 stability duration (in multiples of 100ms)", "WriteInput1StabilityDuration", true));
        cat2.addMessageSpec(addBasicMsgWithValue("Write input 2 stability duration (in multiples of 100ms)", "WriteInput2StabilityDuration", true));
        cat2.addMessageSpec(addBasicMsgWithValue("Write input 3 stability duration (in multiples of 100ms)", "WriteInput3StabilityDuration", true));
        cat2.addMessageSpec(addBasicMsgWithValue("Write input 4 stability duration (in multiples of 100ms)", "WriteInput4StabilityDuration", true));
        cat2.addMessageSpec(addBasicMsgWithValue("Write output 1 impulse duration (in multiples of 100ms)", "WriteOutput1ImpulseDuration", true));
        cat2.addMessageSpec(addBasicMsgWithValue("Write output 2 impulse duration (in multiples of 100ms)", "WriteOutput2ImpulseDuration", true));
        cat2.addMessageSpec(addBasicMsgWithValue("Write output 3 impulse duration (in multiples of 100ms)", "WriteOutput3ImpulseDuration", true));
        cat2.addMessageSpec(addBasicMsgWithValue("Write output 4 impulse duration (in multiples of 100ms)", "WriteOutput4ImpulseDuration", true));
        theCategories.add(cat2);

        MessageCategorySpec cat3 = new MessageCategorySpec("Wavelog alarm frames configuration");
        cat3.addMessageSpec(addBasicMsg("Request to initialize the alarm route", "InitializeAlarmRoute", true));
        cat3.addMessageSpec(addBasicMsgWithFourAttr("Request to initialize the alarm route and the configuration bytes", "InitializeRouteAndConfigBytes", true, "Configuration byte for input 1", "Configuration byte for input 2", "Configuration byte for input 3", "Configuration byte for input 4"));

        //These messages are hidden
        //cat3.addMessageSpec(addBasicMsgWithValue("Set number of repeaters (max 3)", "SetNumberOfRepeaters", true));
        //cat3.addMessageSpec(addBasicMsgWithTwoAttr("Set address of repeater", "SetRepeaterAddress", true, "Number of the repeater (1, 2 or 3)", "Address (hex string)"));
        //cat3.addMessageSpec(addBasicMsgWithAttr("Set address of the recipient", "SetRecipientAddress", true, "Address (hex string)"));

        cat3.addMessageSpec(addBasicMsgWithValue("Set number of retries for an alarm transmission", "SetNumberOfRetries", true));
        cat3.addMessageSpec(addBasicMsgWithValue("Set time between retries for alarm transmissions (in seconds)", "SetTimeBetweenRetries", true));
        cat3.addMessageSpec(addBasicMsgWithValue("Set time between retries for periodic frames (in seconds)", "SetTimeBetweenPeriodicRetries", true));
        cat3.addMessageSpec(addBasicMsgWithValue("Transmission period of the periodic frames (in minutes)", "SetTransmissionPeriodOfPeriodicFrames", true));
        cat3.addMessageSpec(addBasicMsgWithValue("Set number of retries for periodic frames", "SetNumberOfPeriodicRetries", true));
        theCategories.add(cat3);

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

    protected MessageSpec addBasicMsgWithFourAttr(final String keyId, final String tagName, final boolean advanced, String attr1, String attr2, String attr3, String attr4) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageAttributeSpec(attr1, true));
        tagSpec.add(new MessageAttributeSpec(attr2, true));
        tagSpec.add(new MessageAttributeSpec(attr3, true));
        tagSpec.add(new MessageAttributeSpec(attr4, true));
        tagSpec.add(new MessageValueSpec(" "));
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    protected MessageSpec addBasicMsgWithThreeAttr(final String keyId, final String tagName, final boolean advanced, String attr1, String attr2, String attr3) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageAttributeSpec(attr1, true));
        tagSpec.add(new MessageAttributeSpec(attr2, true));
        tagSpec.add(new MessageAttributeSpec(attr3, true));
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

    public String writeMessage(Message msg) {
        return msg.write(this);
    }

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

    public String writeValue(MessageValue value) {
        return value.getValue();
    }

    public void applyMessages(List messageEntries) throws IOException {
    }
}