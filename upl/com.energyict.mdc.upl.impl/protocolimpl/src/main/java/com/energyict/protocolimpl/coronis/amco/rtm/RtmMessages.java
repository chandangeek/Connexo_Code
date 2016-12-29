package com.energyict.protocolimpl.coronis.amco.rtm;

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
import com.energyict.protocolimpl.coronis.amco.rtm.core.parameter.ProfileType;
import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class RtmMessages implements MessageProtocol {

    RTM rtm;
    private static final int MAX_TRANSMISSION_PERIOD = 60 * 24 * 63;        //number of minutes in 63 days
    private static final int MAX_SAMPLING_INTERVAL_SECONDS = 63 * 30 * 60;  //See documentation, largest interval possible is 31,5 hours.

    RtmMessages(RTM rtm) {
        this.rtm = rtm;
    }

    private String stripOffTag(String content) {
        return content.substring(content.indexOf(">") + 1, content.lastIndexOf("<"));
    }

    /**
     * This type is cached in the parameter factory.
     *
     * @return the profile type defining the meter's behavior
     * @throws IOException
     */
    private ProfileType getProfileType() throws IOException {
        return rtm.getParameterFactory().readProfileType();
    }

    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        try {
            if (messageEntry.getContent().contains("<ForceTimeSync")) {
                return forceTimeSync(messageEntry);
            } else if (messageEntry.getContent().contains("<EncoderModelDetection")) {
                return encoderModelDetection(messageEntry);
            } else if (messageEntry.getContent().contains("<WriteIndexA")) {
                return writeIndex(messageEntry, 1);
            } else if (messageEntry.getContent().contains("<WriteIndexB")) {
                return writeIndex(messageEntry, 2);
            } else if (messageEntry.getContent().contains("<WriteIndexC")) {
                return writeIndex(messageEntry, 3);
            } else if (messageEntry.getContent().contains("<WriteIndexD")) {
                return writeIndex(messageEntry, 4);
            } else if (messageEntry.getContent().contains("<WriteEncoderUnit")) {
                return writeEncoderUnit(messageEntry);
            } else if (messageEntry.getContent().contains("<SetAlarmWindowConfiguration")) {
                return setAlarmWindowConfiguration(messageEntry);
            } else if (messageEntry.getContent().contains("<AutoConfigAlarmRoute")) {
                return autoConfigAlarmRoute(messageEntry);
            } else if (messageEntry.getContent().contains("<WritePulseWeight")) {
                return writePulseWeight(messageEntry);
            } else if (messageEntry.getContent().contains("<SetMeterModelA")) {
                return setMeterModel(messageEntry, 1);
            } else if (messageEntry.getContent().contains("<SetMeterModelB")) {
                return setMeterModel(messageEntry, 2);
            } else if (messageEntry.getContent().contains("<SetMeterModelC")) {
                return setMeterModel(messageEntry, 3);
            } else if (messageEntry.getContent().contains("<SetMeterModelD")) {
                return setMeterModel(messageEntry, 4);
            } else if (messageEntry.getContent().contains("<SetOperatingMode")) {
                return setOperatingMode(messageEntry);
            } else if (messageEntry.getContent().contains("<ResetApplicationStatus")) {
                return resetApplicationStatus(messageEntry);
            } else if (messageEntry.getContent().contains("<SetAlarmConfiguration")) {
                return setAlarmConfiguration(messageEntry);
            } else if (messageEntry.getContent().contains("<EnableAllAlarms")) {
                return enableAllAlarms(messageEntry);
            } else if (messageEntry.getContent().contains("<DisableAllAlarms")) {
                return disableAllAlarms(messageEntry);
            } else if (messageEntry.getContent().contains("<setAlarmOnBackFlow")) {
                return setAlarmOnBackFlow(messageEntry, 1);
            } else if (messageEntry.getContent().contains("<setAlarmOnCutCable")) {
                return setAlarmOnCutCable(messageEntry, 1);
            } else if (messageEntry.getContent().contains("<setAlarmOnCutRegisterCable")) {
                return setAlarmOnCutRegisterCable(messageEntry, 1);
            } else if (messageEntry.getContent().contains("<setAlarmOnDefaultValve")) {
                return setAlarmOnDefaultValve(messageEntry, 1);
            } else if (messageEntry.getContent().contains("<setAlarmOnEncoderCommunicationFailure")) {
                return setAlarmOnEncoderCommunicationFailure(messageEntry, 1);
            } else if (messageEntry.getContent().contains("<setAlarmOnEncoderMisread")) {
                return setAlarmOnEncoderMisread(messageEntry, 1);
            } else if (messageEntry.getContent().contains("<setAlarmOnHighThreshold")) {
                return setAlarmOnHighThreshold(messageEntry, 1);
            } else if (messageEntry.getContent().contains("<setAlarmOnLowBattery")) {
                return setAlarmOnLowBattery(messageEntry, 1);
            } else if (messageEntry.getContent().contains("<setAlarmOnLowThreshold")) {
                return setAlarmOnLowThreshold(messageEntry, 1);
            } else if (messageEntry.getContent().contains("<disableAlarmOnBackFlow")) {
                return setAlarmOnBackFlow(messageEntry, 0);
            } else if (messageEntry.getContent().contains("<disableAlarmOnCutCable")) {
                return setAlarmOnCutCable(messageEntry, 0);
            } else if (messageEntry.getContent().contains("<disableAlarmOnCutRegisterCable")) {
                return setAlarmOnCutRegisterCable(messageEntry, 0);
            } else if (messageEntry.getContent().contains("<disableAlarmOnDefaultValve")) {
                return setAlarmOnDefaultValve(messageEntry, 0);
            } else if (messageEntry.getContent().contains("<disableAlarmOnEncoderCommunicationFailure")) {
                return setAlarmOnEncoderCommunicationFailure(messageEntry, 0);
            } else if (messageEntry.getContent().contains("<disableAlarmOnEncoderMisread")) {
                return setAlarmOnEncoderMisread(messageEntry, 0);
            } else if (messageEntry.getContent().contains("<disableAlarmOnHighThreshold")) {
                return setAlarmOnHighThreshold(messageEntry, 0);
            } else if (messageEntry.getContent().contains("<disableAlarmOnLowBattery")) {
                return setAlarmOnLowBattery(messageEntry, 0);
            } else if (messageEntry.getContent().contains("<disableAlarmOnLowThreshold")) {
                return setAlarmOnLowThreshold(messageEntry, 0);
            } else if (messageEntry.getContent().contains("<SetNumberOfRepeaters")) {
                return setNumberOfRepeaters(messageEntry);
            } else if (messageEntry.getContent().contains("<SetRepeaterAddress")) {
                return setRepeaterAddress(messageEntry);
            } else if (messageEntry.getContent().contains("<SetRecipientAddress")) {
                return setRecipientAddress(messageEntry);
            } else if (messageEntry.getContent().contains("<WriteSamplingPeriod")) {
                return writeSamplingPeriod(messageEntry);
            } else if (messageEntry.getContent().contains("<WriteDayOfWeekOrMonth")) {
                return writeDayOfWeekOrMonth(messageEntry);
            } else if (messageEntry.getContent().contains("<SetHourOfMeasurement")) {
                return setHourOfMeasurement(messageEntry);
            } else if (messageEntry.getContent().contains("<StopDataLogging")) {
                return stopDataLogging(messageEntry);
            } else if (messageEntry.getContent().contains("<SetPeriodicStepsLogging")) {
                return setPeriodicStepsLogging(messageEntry);
            } else if (messageEntry.getContent().contains("<SetWeeklyDataLogging")) {
                return setWeeklyLogging(messageEntry);
            } else if (messageEntry.getContent().contains("<SetMonthlyDataLogging")) {
                return setMonthlyLogging(messageEntry);
            } else if (messageEntry.getContent().contains("<WriteTOUBucketStartHour")) {
                return writeTOUBucketStartHour(messageEntry);
            } else if (messageEntry.getContent().contains("<EnableTOUBuckets")) {
                return enableTOUBuckets(messageEntry);
            } else if (messageEntry.getContent().contains("<DisableTOUBuckets")) {
                return disableTOUBuckets(messageEntry);
            } else if (messageEntry.getContent().contains("<RestartDataLogging")) {
                return restartDataLogging(messageEntry);
            } else if (messageEntry.getContent().contains("<SetWakeUpChannel")) {
                return setWakeUpChannel(messageEntry);
            } else if (messageEntry.getContent().contains("<SetInterAnswerDelay")) {
                return setInterAnswerDelay(messageEntry);
            } else if (messageEntry.getContent().contains("<SetStartOfMechanism")) {
                return setStartOfMechanism(messageEntry);
            } else if (messageEntry.getContent().contains("<SetEndOfMechanism")) {
                return setEndOfMechanism(messageEntry);
            } else if (messageEntry.getContent().contains("<SetTransmissionPeriod")) {
                return setTransmissionPeriod(messageEntry);
            } else if (messageEntry.getContent().contains("<SetMaxCancelTimeout")) {
                return setMaxCancelTimeout(messageEntry);
            } else if (messageEntry.getContent().contains("<AddCommandToBuffer")) {
                return addCommandToBuffer(messageEntry);
            } else if (messageEntry.getContent().contains("<EnableBubbleUpMechanism")) {
                return enableBubbleUpMechanism(messageEntry);
            } else if (messageEntry.getContent().contains("<DisableBubbleUpMechanism")) {
                return disableBubbleUpMechanism(messageEntry);
            } else if (messageEntry.getContent().contains("<ClearCommandBuffer")) {
                return clearCommandBuffer(messageEntry);
            } else if (messageEntry.getContent().contains("<StartBubbleUpMechanism")) {
                return startBubbleUpMechanism(messageEntry);
            } else if (messageEntry.getContent().contains("<SetLeakageConsumptionRate")) {
                return setLeakageConsumptionRate(messageEntry);
            } else if (messageEntry.getContent().contains("<SetLeakageDetectionPeriod")) {
                return setLeakageDetectionPeriod(messageEntry);
            } else if (messageEntry.getContent().contains("<SetBackflowDetectionPeriod")) {
                return setBackflowDetectionPeriod(messageEntry);
            } else if (messageEntry.getContent().contains("<SetNumberOfBackflowsBeforeIndication")) {
                return setNumberOfBackflowsBeforeIndication(messageEntry);
            } else if (messageEntry.getContent().contains("<WriteBackflowThreshold")) {
                return writeBackflowThreshold(messageEntry);
            } else if (messageEntry.getContent().contains("<ClearBackFlowFlags")) {
                return clearBackFlowFlags(messageEntry);
            } else if (messageEntry.getContent().contains("<OpenWaterValve")) {
                return openWaterValve(messageEntry);
            } else if (messageEntry.getContent().contains("<CloseWaterValve")) {
                return closeWaterValve(messageEntry);
            } else if (messageEntry.getContent().contains("<CleanWaterValve")) {
                return cleanWaterValve(messageEntry);
            } else {
                return MessageResult.createFailed(messageEntry);
            }
        } catch (NumberFormatException e) {
            rtm.getLogger().severe("Error parsing message, " + e.getMessage());
            return MessageResult.createFailed(messageEntry);
        } catch (WaveFlowException e) {
            rtm.getLogger().severe("Message failed, " + e.getMessage());
            return MessageResult.createFailed(messageEntry);
        }
    }

    private MessageResult setBackflowDetectionPeriod(MessageEntry messageEntry) throws IOException {
        rtm.getLogger().info("************************* SetBackflowDetectionPeriod *************************");
        if (!getProfileType().isEncoder()) {
            rtm.getLogger().warning("Can not write the backflow detection period for device of type pulse register");
            return MessageResult.createFailed(messageEntry);
        }
        String[] parts = messageEntry.getContent().split("=");
        int inputChannel = Integer.parseInt(parts[3].substring(1, 2));
        int period;
        try {
            period = Integer.parseInt(parts[4].substring(1, 4));
        } catch (NumberFormatException e) {
            try {
                period = Integer.parseInt(parts[4].substring(1, 3));
            } catch (NumberFormatException e1) {
                period = Integer.parseInt(parts[4].substring(1, 2));
            }
        }
        if ((period > 0xFF) || (period < 1)) {
            rtm.getLogger().warning("Invalid period parameter given");
            return MessageResult.createFailed(messageEntry);
        }
        if (inputChannel > 0 && inputChannel < 3) {
            rtm.getParameterFactory().writeBackflowDetectionPeriod(period, inputChannel);
            return MessageResult.createSuccess(messageEntry);
        } else {
            rtm.getLogger().warning("Invalid input channel given");
            return MessageResult.createFailed(messageEntry);
        }
    }

    private MessageResult setNumberOfBackflowsBeforeIndication(MessageEntry messageEntry) throws IOException {
        rtm.getLogger().info("************************* SetNumberOfBackflowsBeforeIndication *************************");
        if (!getProfileType().isEncoder()) {
            rtm.getLogger().warning("Can not write the number of back flows before indication, for device of type pulse register");
            return MessageResult.createFailed(messageEntry);
        }
        String[] parts = messageEntry.getContent().split("=");
        int inputChannel = Integer.parseInt(parts[3].substring(1, 2));
        int number;
        try {
            number = Integer.parseInt(parts[4].substring(1, 4));
        } catch (NumberFormatException e) {
            try {
                number = Integer.parseInt(parts[4].substring(1, 3));
            } catch (NumberFormatException e1) {
                number = Integer.parseInt(parts[4].substring(1, 2));
            }
        }
        if ((number > 0xFF) || (number < 1)) {
            rtm.getLogger().warning("Invalid number given");
            return MessageResult.createFailed(messageEntry);
        }
        if (inputChannel > 0 && inputChannel < 3) {
            rtm.getParameterFactory().writeNumberOfBackflowsBeforeIndication(number, inputChannel);
            return MessageResult.createSuccess(messageEntry);
        } else {
            rtm.getLogger().warning("Invalid input channel given");
            return MessageResult.createFailed(messageEntry);
        }
    }

    private MessageResult writeBackflowThreshold(MessageEntry messageEntry) throws IOException {
        rtm.getLogger().info("************************* writeBackflowThreshold *************************");
        if (!getProfileType().isEncoder()) {
            rtm.getLogger().warning("Can not write the backflow threshold for device of type pulse register");
            return MessageResult.createFailed(messageEntry);
        }
        String[] parts = messageEntry.getContent().split("=");
        int inputChannel = Integer.parseInt(parts[3].substring(1, 2));
        int threshold;
        try {
            threshold = Integer.parseInt(parts[4].substring(1, 4));
        } catch (NumberFormatException e) {
            try {
                threshold = Integer.parseInt(parts[4].substring(1, 3));
            } catch (NumberFormatException e1) {
                threshold = Integer.parseInt(parts[4].substring(1, 2));
            }
        }
        if ((threshold > 0xFF) || (threshold < 1)) {
            rtm.getLogger().warning("Invalid threshold given");
            return MessageResult.createFailed(messageEntry);
        }
        if (inputChannel > 0 && inputChannel < 3) {
            rtm.getParameterFactory().writeBackflowThreshold(threshold, inputChannel);
            return MessageResult.createSuccess(messageEntry);
        } else {
            rtm.getLogger().warning("Invalid input channel given");
            return MessageResult.createFailed(messageEntry);
        }
    }

    private MessageResult setLeakageDetectionPeriod(MessageEntry messageEntry) throws IOException {
        rtm.getLogger().info("************************* SetLeakageDetectionPeriod *************************");
        if (getProfileType().isEvoHop()) {
            rtm.getLogger().warning("Can not write the leakage detection period for module of type EvoHop");
            return MessageResult.createFailed(messageEntry);
        }
        String[] parts = messageEntry.getContent().split("=");
        int residualOrExtreme = Integer.parseInt(parts[1].substring(1, 2));
        int inputChannel = Integer.parseInt(parts[4].substring(1, 2));
        int period;
        try {
            period = Integer.parseInt(parts[5].substring(1, 4));
        } catch (NumberFormatException e) {
            try {
                period = Integer.parseInt(parts[5].substring(1, 3));
            } catch (NumberFormatException e1) {
                period = Integer.parseInt(parts[5].substring(1, 2));
            }
        }
        if ((period > 0xFF) || (period < 0)) {
            rtm.getLogger().warning("Invalid period given");
            return MessageResult.createFailed(messageEntry);
        }
        if ((residualOrExtreme == 0 || residualOrExtreme == 1) && (inputChannel > 0 && inputChannel < 5)) {
            rtm.getParameterFactory().setLeakageDetectionPeriod(residualOrExtreme, inputChannel, period);
            return MessageResult.createSuccess(messageEntry);
        } else {
            rtm.getLogger().warning("Invalid parameters given");
            return MessageResult.createFailed(messageEntry);
        }
    }

    private MessageResult setRepeaterAddress(MessageEntry messageEntry) throws IOException {
        rtm.getLogger().info("************************* SetRepeaterAddress *************************");
        String[] parts = messageEntry.getContent().split("=");
        int number = Integer.parseInt(parts[1].substring(1, 2));
        if (number > 3 || number < 1) {
            rtm.getLogger().warning("Invalid repeater number given, should be 1, 2 or 3.");
            return MessageResult.createFailed(messageEntry);
        }
        String address = parts[2].substring(1, 13);
        rtm.getParameterFactory().writeRepeaterAddress(address, number);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setRecipientAddress(MessageEntry messageEntry) throws IOException {
        rtm.getLogger().info("************************* SetRecipientAddress *************************");
        String[] parts = messageEntry.getContent().split("=");
        String address = parts[1].substring(1, 13);
        rtm.getParameterFactory().writeRecipientAddress(address);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setNumberOfRepeaters(MessageEntry messageEntry) throws IOException {
        rtm.getLogger().info("************************* SetNumberOfRepeaters *************************");
        int value = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (value > 3 || value < 0) {
            rtm.getLogger().warning("Invalid number of repeaters given, should be 0, 1, 2 or 3.");
            return MessageResult.createFailed(messageEntry);
        }
        rtm.getParameterFactory().writeNumberOfRepeaters(value);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setLeakageConsumptionRate(MessageEntry messageEntry) throws IOException {
        rtm.getLogger().info("************************* SetLeakageThreshold *************************");
        if (getProfileType().isEvoHop()) {
            rtm.getLogger().warning("Can not write the leakage threshold for module of type EvoHop");
            return MessageResult.createFailed(messageEntry);
        }
        String[] parts = messageEntry.getContent().split("=");
        int residualOrExtreme = Integer.parseInt(parts[1].substring(1, 2));
        int inputChannel = Integer.parseInt(parts[4].substring(1, 2));
        int threshold;
        try {
            threshold = Integer.parseInt(parts[5].substring(1, 4));
        } catch (NumberFormatException e) {
            try {
                threshold = Integer.parseInt(parts[5].substring(1, 3));
            } catch (NumberFormatException e1) {
                threshold = Integer.parseInt(parts[5].substring(1, 2));
            }
        }

        //Check the range of the given threshold.
        if ((threshold > 0xFFFF) || (threshold < 0)) {
            rtm.getLogger().warning("Invalid threshold given.");
            return MessageResult.createFailed(messageEntry);
        }
        if ((residualOrExtreme == 0) && (threshold > 0xFF)) {
            rtm.getLogger().warning("Invalid threshold given.");
            return MessageResult.createFailed(messageEntry);
        }
        if ((residualOrExtreme == 0 || residualOrExtreme == 1) && (inputChannel > 0 && inputChannel < 5)) {
            rtm.getParameterFactory().setLeakageThreshold(residualOrExtreme, inputChannel, threshold);
            return MessageResult.createSuccess(messageEntry);
        } else {
            rtm.getLogger().warning("Invalid parameter given.");
            return MessageResult.createFailed(messageEntry);
        }
    }

    private MessageResult clearCommandBuffer(MessageEntry messageEntry) throws IOException {
        rtm.getLogger().info("************************* ClearCommandBuffer *************************");
        rtm.getParameterFactory().clearCommandBuffer();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult clearBackFlowFlags(MessageEntry messageEntry) throws IOException {
        rtm.getLogger().info("************************* ClearBackFlowFlags *************************");
        rtm.getParameterFactory().clearBackFlowFlags();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult addCommandToBuffer(MessageEntry messageEntry) throws IOException {
        rtm.getLogger().info("************************* AddCommandToBuffer *************************");
        String[] parts = messageEntry.getContent().split("=");
        int value;
        try {
            value = Integer.parseInt(parts[2].substring(1, 3));
        } catch (NumberFormatException e) {
            value = Integer.parseInt(parts[2].substring(1, 2));
        }
        if (value > 0xFF || value < 0x01) {
            rtm.getLogger().warning("Invalid applicative command given.");
            return MessageResult.createFailed(messageEntry);
        }

        int portMask = 0;
        int numberOfReadings = 0;
        if (value == 7) {
            try {
                portMask = Integer.parseInt(parts[3].substring(1, 3));
            } catch (NumberFormatException e) {
                portMask = Integer.parseInt(parts[3].substring(1, 2));
            }
            try {
                numberOfReadings = Integer.parseInt(parts[4].substring(1, 3));
            } catch (NumberFormatException e) {
                numberOfReadings = Integer.parseInt(parts[4].substring(1, 2));
            }
        }

        rtm.getParameterFactory().replaceCommandInBuffer(value, portMask, numberOfReadings, 0);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult enableBubbleUpMechanism(MessageEntry messageEntry) throws IOException {
        rtm.getLogger().info("************************* EnableBubbleUpMechanism *************************");
        rtm.getParameterFactory().setBubbleUpManagement(1);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult disableBubbleUpMechanism(MessageEntry messageEntry) throws IOException {
        rtm.getLogger().info("************************* DisableBubbleUpMechanism *************************");
        rtm.getParameterFactory().setBubbleUpManagement(0);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult startBubbleUpMechanism(MessageEntry messageEntry) throws IOException {
        rtm.getLogger().info("************************* StartBubbleUpMechanism *************************");
        String[] parts = messageEntry.getContent().split("=");
        int value;
        try {
            value = Integer.parseInt(parts[2].substring(1, 3));
        } catch (NumberFormatException e) {
            value = Integer.parseInt(parts[2].substring(1, 2));
        }
        if (value > 0xFF || value < 0x01) {
            rtm.getLogger().warning("Invalid applicative command given.");
            return MessageResult.createFailed(messageEntry);
        }

        int portMask = 0;
        int numberOfReadings = 0;
        if (value == 7) {
            try {
                portMask = Integer.parseInt(parts[3].substring(1, 3));
            } catch (NumberFormatException e) {
                portMask = Integer.parseInt(parts[3].substring(1, 2));
            }
            try {
                numberOfReadings = Integer.parseInt(parts[4].substring(1, 3));
            } catch (NumberFormatException e) {
                numberOfReadings = Integer.parseInt(parts[4].substring(1, 2));
            }
        }

        int transmissionPeriod;
        int transmissionPeriodUnit;
        try {
            transmissionPeriod = Integer.parseInt(parts[5 - (value == 7 ? 0 : 2)].substring(1).split("\"")[0]);
            transmissionPeriodUnit = Integer.parseInt(parts[6 - (value == 7 ? 0 : 2)].substring(1).split("\"")[0]);
            if (transmissionPeriod < 1 || transmissionPeriod > 63) {
                return MessageResult.createFailed(messageEntry);
            }
            if (transmissionPeriodUnit < 0 || transmissionPeriodUnit > 2) {
                return MessageResult.createFailed(messageEntry);
            }
        } catch (IndexOutOfBoundsException e) {     //Use the default values
            transmissionPeriod = 1;
            transmissionPeriodUnit = 2;
        }

        transmissionPeriod = (transmissionPeriod << 2) | transmissionPeriodUnit;
        if (rtm.getBubbleUpStartMoment() == -1) {
            rtm.getLogger().log(Level.INFO, "Custom property containing bubble up info is not set, message failed");
            return MessageResult.createFailed(messageEntry);
        }
        rtm.getParameterFactory().writeBubbleUpConfiguration(value, portMask, numberOfReadings, 0, transmissionPeriod);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setMaxCancelTimeout(MessageEntry messageEntry) throws IOException {
        rtm.getLogger().info("************************* SetMaxCancelTimeout *************************");
        int value = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (value > 10 || value < 1) {
            rtm.getLogger().warning("Invalid max cancellation time given.");
            return MessageResult.createFailed(messageEntry);
        }
        rtm.getParameterFactory().writeMaxCancelTimeout(value);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setTransmissionPeriod(MessageEntry messageEntry) throws IOException {
        rtm.getLogger().info("************************* SetTransmissionPeriod *************************");
        int minutes = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (minutes > MAX_TRANSMISSION_PERIOD) {
            rtm.getLogger().warning("Invalid transmission period given.");
            return MessageResult.createFailed(messageEntry);
        }
        rtm.getParameterFactory().writeTransmissionPeriod(minutes);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setEndOfMechanism(MessageEntry messageEntry) throws IOException {
        rtm.getLogger().info("************************* SetEndOfMechanism *************************");
        int hour = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (hour > 23) {
            rtm.getLogger().warning("Invalid end hour of mechanism given.");
            return MessageResult.createFailed(messageEntry);
        }
        rtm.getParameterFactory().writeEndOfPushFrameMechanism(hour);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setStartOfMechanism(MessageEntry messageEntry) throws IOException {
        rtm.getLogger().info("************************* SetStartOfMechanism *************************");
        String[] parts = messageEntry.getContent().split("=");
        int hour;
        int minute;
        int second;
        try {
            hour = Integer.parseInt(parts[1].substring(1, 3));
        } catch (NumberFormatException e) {
            hour = Integer.parseInt(parts[1].substring(1, 2));      //try again for a shorter length
        }
        try {
            minute = Integer.parseInt(parts[2].substring(1, 3));
        } catch (NumberFormatException e) {
            minute = Integer.parseInt(parts[2].substring(1, 2));
        }
        try {
            second = Integer.parseInt(parts[3].substring(1, 3));
        } catch (NumberFormatException e) {
            second = Integer.parseInt(parts[3].substring(1, 2));
        }
        if ((hour > 23 || hour < 0) || (minute < 0 || minute > 59) || (second < 0 || second > 59)) {
            rtm.getLogger().warning("Invalid start of mechanism given.");
            return MessageResult.createFailed(messageEntry);
        }
        rtm.getParameterFactory().writeStartOfPushFrameMechanism(hour, minute, second);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult forceTimeSync(MessageEntry messageEntry) throws IOException {
        rtm.getLogger().info("************************* ForceTimeSync *************************");
        rtm.setTime();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult encoderModelDetection(MessageEntry messageEntry) throws IOException {
        rtm.getLogger().info("************************* EncoderModelDetection *************************");
        rtm.getRadioCommandFactory().detectEncoderModel();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setAlarmOnBackFlow(MessageEntry messageEntry, int enable) throws IOException {
        rtm.getLogger().info("************************* setAlarmOnBackFlow: " + enable + " *************************");
        rtm.getParameterFactory().setAlarmOnBackFlow(enable);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setAlarmOnCutCable(MessageEntry messageEntry, int enable) throws IOException {
        rtm.getLogger().info("************************* setAlarmOnCutCable: " + enable + " *************************");
        rtm.getParameterFactory().setAlarmOnCutCable(enable);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setAlarmOnCutRegisterCable(MessageEntry messageEntry, int enable) throws IOException {
        rtm.getLogger().info("************************* setAlarmOnCutRegisterCable: " + enable + " *************************");
        rtm.getParameterFactory().setAlarmOnCutRegisterCable(enable);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setAlarmOnDefaultValve(MessageEntry messageEntry, int enable) throws IOException {
        rtm.getLogger().info("************************* setAlarmOnDefaultValve: " + enable + " *************************");
        rtm.getParameterFactory().setAlarmOnDefaultValve(enable);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setAlarmOnEncoderCommunicationFailure(MessageEntry messageEntry, int enable) throws IOException {
        rtm.getLogger().info("************************* setAlarmOnEncoderCommunicationFailure: " + enable + " *************************");
        rtm.getParameterFactory().setAlarmOnEncoderCommunicationFailure(enable);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult enableTOUBuckets(MessageEntry messageEntry) throws IOException {
        rtm.getLogger().info("************************* EnableTOUBuckets *************************");
        rtm.getParameterFactory().setTOUBuckets(1);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult disableTOUBuckets(MessageEntry messageEntry) throws IOException {
        rtm.getLogger().info("************************* DisableTOUBuckets *************************");
        rtm.getParameterFactory().setTOUBuckets(0);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult writeTOUBucketStartHour(MessageEntry messageEntry) throws IOException {
        rtm.getLogger().info("************************* writeTOUBucketStartHour *************************");
        if (getProfileType().isEvoHop()) {
            rtm.getLogger().warning("Writing the TOU buckets start hours is not supported by the EvoHop device.");
            return MessageResult.createFailed(messageEntry);
        }

        String[] parts = messageEntry.getContent().split("=");
        int length = Integer.parseInt(parts[1].substring(1, 2));
        int[] startHours = new int[length];

        //Get the start hours, can be 1 or 2 digits long.
        for (int i = 0; i < length; i++) {
            try {
                startHours[i] = Integer.parseInt(parts[2 + i].substring(1, 3));
            } catch (NumberFormatException e) {
                try {
                    startHours[i] = Integer.parseInt(parts[2 + i].substring(1, 2));
                } catch (NumberFormatException e1) {
                    startHours[i] = 0;
                }
            }
            if (startHours[i] < 0 || startHours[i] > 23) {
                rtm.getLogger().warning("Invalid start hour given.");
                return MessageResult.createFailed(messageEntry);
            }
        }

        rtm.getParameterFactory().writeTOUBucketStartHour(length, startHours);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setAlarmOnEncoderMisread(MessageEntry messageEntry, int enable) throws IOException {
        rtm.getLogger().info("************************* setAlarmOnEncoderMisread: " + enable + " *************************");
        rtm.getParameterFactory().setAlarmOnEncoderMisread(enable);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setAlarmOnHighThreshold(MessageEntry messageEntry, int enable) throws IOException {
        rtm.getLogger().info("************************* setAlarmOnHighThreshold: " + enable + " *************************");
        rtm.getParameterFactory().setAlarmOnHighThreshold(enable);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setAlarmOnLowBattery(MessageEntry messageEntry, int enable) throws IOException {
        rtm.getLogger().info("************************* setAlarmOnLowBattery: " + enable + " *************************");
        rtm.getParameterFactory().setAlarmOnLowBattery(enable);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setAlarmOnLowThreshold(MessageEntry messageEntry, int enable) throws IOException {
        rtm.getLogger().info("************************* setAlarmOnLowThreshold: " + enable + " *************************");
        rtm.getParameterFactory().setAlarmOnLowThreshold(enable);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult resetApplicationStatus(MessageEntry messageEntry) throws IOException {
        rtm.getLogger().info("************************* ResetApplicationStatus *************************");
        rtm.getParameterFactory().writeApplicationStatus(0);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setOperatingMode(MessageEntry messageEntry) throws IOException {
        rtm.getLogger().info("************************* setOperatingMode *************************");
        int value = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        rtm.getParameterFactory().writeOperatingMode(value);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult writeSamplingPeriod(MessageEntry messageEntry) throws IOException {
        rtm.getLogger().info("************************* writeSamplingPeriod *************************");
        if (getProfileType().isEvoHop()) {
            rtm.getLogger().warning("Writing the sampling period is not supported by the EvoHop module.");
            return MessageResult.createFailed(messageEntry);
        }
        int value = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (value < 60 || value > MAX_SAMPLING_INTERVAL_SECONDS) {
            rtm.getLogger().severe("Invalid profile interval, must have minimum 60 seconds");
            return MessageResult.createFailed(messageEntry);
        }

        rtm.getParameterFactory().writeSamplingIntervalInSeconds(value);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult writeDayOfWeekOrMonth(MessageEntry messageEntry) throws IOException {
        rtm.getLogger().info("************************* WriteDayOfWeekOrMonth *************************");
        if (getProfileType().isEvoHop()) {
            rtm.getLogger().warning("Writing data logging day of month is not supported by the EvoHop module.");
            return MessageResult.createFailed(messageEntry);
        }
        int dayOfWeek = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        boolean monthly = rtm.getParameterFactory().readOperatingMode().isMonthlyLogging();
        if (monthly && (dayOfWeek > 28 || dayOfWeek < 1)) {
            rtm.getLogger().warning("Invalid day of month given.");
            return MessageResult.createFailed(messageEntry);
        } else if (!monthly && (dayOfWeek < 0 || dayOfWeek > 6)) {
            rtm.getLogger().warning("Invalid day of week given.");
            return MessageResult.createFailed(messageEntry);
        }
        rtm.getParameterFactory().writeDayOfWeek(dayOfWeek);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setHourOfMeasurement(MessageEntry messageEntry) throws IOException {
        rtm.getLogger().info("************************* SetHourOfMeasurement *************************");
        if (getProfileType().isEvoHop()) {
            rtm.getLogger().warning("Writing data logging hour is not supported by the EvoHop module.");
            return MessageResult.createFailed(messageEntry);
        }
        int value = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (value < 0 || value > 23) {
            rtm.getLogger().warning("Invalid hour given.");
            return MessageResult.createFailed(messageEntry);
        }
        rtm.getParameterFactory().writeStartHourOfMeasurement(value);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult stopDataLogging(MessageEntry messageEntry) throws IOException {
        rtm.getLogger().info("************************* StopDataLogging *************************");
        if (getProfileType().isEvoHop()) {
            rtm.getLogger().warning("Data logging is not supported by the EvoHop module.");
            return MessageResult.createFailed(messageEntry);
        }
        rtm.getParameterFactory().stopDataLogging();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setPeriodicStepsLogging(MessageEntry messageEntry) throws IOException {
        rtm.getLogger().info("************************* SetPeriodicStepsLogging *************************");
        if (getProfileType().isEvoHop()) {
            rtm.getLogger().warning("Data logging is not supported by the EvoHop module.");
            return MessageResult.createFailed(messageEntry);
        }
        rtm.getParameterFactory().setDataLoggingToPeriodic();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setWeeklyLogging(MessageEntry messageEntry) throws IOException {
        rtm.getLogger().info("************************* SetWeeklyLogging *************************");
        if (getProfileType().isEvoHop()) {
            rtm.getLogger().warning("Data logging is not supported by the EvoHop module.");
            return MessageResult.createFailed(messageEntry);
        }
        rtm.getParameterFactory().setDataLoggingToWeekly();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setMonthlyLogging(MessageEntry messageEntry) throws IOException {
        rtm.getLogger().info("************************* SetMonthlyLogging *************************");
        if (getProfileType().isEvoHop()) {
            rtm.getLogger().warning("Data logging is not supported by the EvoHop module.");
            return MessageResult.createFailed(messageEntry);
        }
        rtm.getParameterFactory().setDataLoggingToMonthly();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setAlarmConfiguration(MessageEntry messageEntry) throws IOException {
        rtm.getLogger().info("************************* setAlarmConfiguration *************************");
        int value = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (value > 0xFF || value < 0x00) {
            rtm.getLogger().warning("Cannot write alarm configuration, invalid value given.");
            return MessageResult.createFailed(messageEntry);
        }
        rtm.getParameterFactory().writeAlarmConfiguration(value);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setWakeUpChannel(MessageEntry messageEntry) throws IOException {
        rtm.getLogger().info("************************* SetWakeUpChannel *************************");
        if (getProfileType().isEvoHop()) {
            rtm.getLogger().warning("Writing data logging hour is not supported by the EvoHop module.");
            return MessageResult.createFailed(messageEntry);
        }
        int value = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (value > 0xFF || value < 0x00) {
            rtm.getLogger().warning("Cannot write the wake up channel, invalid value given.");
            return MessageResult.createFailed(messageEntry);
        }
        rtm.getParameterFactory().writeWakeUpChannel(value);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult restartDataLogging(MessageEntry messageEntry) throws IOException {
        rtm.getLogger().info("************************* RestartDataLogging *************************");
        if (getProfileType().isEvoHop()) {
            rtm.getLogger().warning("Data logging is not supported by the EvoHop module.");
            return MessageResult.createFailed(messageEntry);
        }
        int mode = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (mode > 3 || mode < 1) {
            rtm.getLogger().warning("Cannot restart the data logging, invalid mode given.");
            return MessageResult.createFailed(messageEntry);
        }
        rtm.getParameterFactory().restartDataLogging(mode);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult writeIndex(MessageEntry messageEntry, int port) throws IOException {
        rtm.getLogger().info("************************* writeIndex *************************");
        if (!getProfileType().isPulse()) {
            rtm.getLogger().warning("Can not write the indexes for device of type encoder");
            return MessageResult.createFailed(messageEntry);
        }
        int value = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        rtm.getRadioCommandFactory().writeIndex(value, port);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setMeterModel(MessageEntry messageEntry, int port) throws IOException {
        rtm.getLogger().info("************************* SetMeterModel *************************");
        if (!getProfileType().isPulse()) {
            rtm.getLogger().warning("Can not set the meter model for device of type encoder");
            return MessageResult.createFailed(messageEntry);
        }
        int value = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (value > 0xFF || value < 0x00) {
            rtm.getLogger().warning("Invalid meter model value given.");
            return MessageResult.createFailed(messageEntry);
        }
        if (port < 1 || port > 4) {
            rtm.getLogger().warning("Invalid port given.");
            return MessageResult.createFailed(messageEntry);
        }
        rtm.getParameterFactory().setMeterModel(value, port);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult disableAllAlarms(MessageEntry messageEntry) throws IOException {
        rtm.getLogger().info("************************* DisableAllAlarms *************************");
        rtm.getParameterFactory().disableAllAlarms();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult enableAllAlarms(MessageEntry messageEntry) throws IOException {
        rtm.getLogger().info("************************* EnableAllAlarms *************************");
        rtm.getParameterFactory().enableAllAlarms();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setInterAnswerDelay(MessageEntry messageEntry) throws IOException {
        rtm.getLogger().info("************************* SetInterAnswerDelay *************************");
        if (getProfileType().isEvoHop()) {
            rtm.getLogger().warning("Inter answer delay is not supported by the EvoHop module.");
            return MessageResult.createFailed(messageEntry);
        }
        String[] parts = messageEntry.getContent().split("=");
        int hour;
        int minute;
        int second;
        try {
            hour = Integer.parseInt(parts[1].substring(1, 3));
        } catch (NumberFormatException e) {
            hour = Integer.parseInt(parts[1].substring(1, 2));      //try again for a shorter length
        }
        try {
            minute = Integer.parseInt(parts[2].substring(1, 3));
        } catch (NumberFormatException e) {
            minute = Integer.parseInt(parts[2].substring(1, 2));
        }
        try {
            second = Integer.parseInt(parts[3].substring(1, 3));
        } catch (NumberFormatException e) {
            second = Integer.parseInt(parts[3].substring(1, 2));
        }
        if ((hour > 23 || hour < 0) || (minute < 0 || minute > 59) || (second < 0 || second > 59)) {
            rtm.getLogger().warning("Invalid delay given.");
            return MessageResult.createFailed(messageEntry);
        }
        if ((hour + minute + second) == 0) {
            rtm.getLogger().warning("Invalid delay given, should be greater than 0.");
            return MessageResult.createFailed(messageEntry);
        }
        rtm.getParameterFactory().setInterAnswerDelay(hour, minute, second);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult writePulseWeight(MessageEntry messageEntry) throws IOException {
        rtm.getLogger().info("************************* writePulseWeight *************************");
        if (!getProfileType().isPulse()) {
            rtm.getLogger().warning("Writing the pulse weight is only supported by pulse register devices.");
            return MessageResult.createFailed(messageEntry);
        }
        String[] parts = messageEntry.getContent().split("=");
        int port;
        int scale;
        int multiplier;
        int unit;
        port = Integer.parseInt(parts[1].substring(1, 2));
        if ("-".equals(parts[2].substring(1, 2))) {
            scale = -1 * Integer.parseInt(parts[2].substring(2, 3));
        } else {
            scale = Integer.parseInt(parts[2].substring(1, 2));
        }
        multiplier = Integer.parseInt(parts[3].substring(1, 2));
        unit = Integer.parseInt(parts[9].substring(1, 2));              //TODO test
        rtm.getParameterFactory().writePulseWeight(port, scale, multiplier, unit);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult writeEncoderUnit(MessageEntry messageEntry) throws IOException {
        rtm.getLogger().info("************************* WriteEncoderUnit *************************");
        if (!getProfileType().isEncoder()) {
            rtm.getLogger().warning("Writing the encoder unit is only supported by encoder devices.");
            return MessageResult.createFailed(messageEntry);
        }
        String[] parts = messageEntry.getContent().split("=");
        int port = Integer.parseInt(parts[1].substring(1, 2));
        int numberOfDecimals = Integer.parseInt(parts[2].substring(1, 2));
        int unitNumber;
        try {
            String hex = parts[3].substring(1, 3);
            unitNumber = ProtocolTools.getBytesFromHexString(hex, "")[0] & 0xFF;
        } catch (NumberFormatException e) {
            String hex = parts[3].substring(1, 2);
            unitNumber = ProtocolTools.getBytesFromHexString("0" + hex, "")[0] & 0xFF;
        }
        if (numberOfDecimals < 1 || numberOfDecimals > 6) {
            rtm.getLogger().warning("Invalid number of decimals given.");
            return MessageResult.createFailed(messageEntry);
        }
        rtm.getParameterFactory().writeEncoderUnit(port, numberOfDecimals, unitNumber);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setAlarmWindowConfiguration(MessageEntry messageEntry) throws IOException {
        rtm.getLogger().info("************************* SetAlarmWindowConfiguration *************************");
        String[] parts = messageEntry.getContent().split("=");
        boolean activation = "1".equals(parts[3].substring(1, 2));
        int duration;
        try {
            duration = Integer.parseInt(parts[4].substring(1, 3));
        } catch (NumberFormatException e) {
            duration = Integer.parseInt(parts[4].substring(1, 2));
        }
        int granularity = Integer.parseInt(parts[5].substring(1, 3));

        if (granularity != 15 && granularity != 30 && granularity != 60) {
            rtm.getLogger().warning("Invalid granularity given.");
            return MessageResult.createFailed(messageEntry);
        }
        if (duration != 30 && duration != 45 && duration != 60 && duration != 90 && duration != 120) {
            rtm.getLogger().warning("Invalid duration given.");
            return MessageResult.createFailed(messageEntry);
        }
        rtm.getParameterFactory().setAlarmWindowConfiguration(duration, activation, granularity);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult autoConfigAlarmRoute(MessageEntry messageEntry) throws IOException {
        rtm.getLogger().info("************************* AutoConfigAlarmRoute *************************");
        int response = rtm.getParameterFactory().autoConfigAlarmRoute();
        if (response != 0x00) {
            rtm.getLogger().warning("Setting the route failed. Expected 0x00, received 0x" + response);
            return MessageResult.createFailed(messageEntry);
        }
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult cleanWaterValve(MessageEntry messageEntry) throws IOException {
        rtm.getLogger().info("************************* Clean water valve *************************");
        boolean success = rtm.getRadioCommandFactory().cleanWaterValve();
        if (success) {
            return MessageResult.createSuccess(messageEntry);
        } else {
            rtm.getLogger().warning("Could not clean the water valve.");
            return MessageResult.createFailed(messageEntry);
        }
    }

    private MessageResult closeWaterValve(MessageEntry messageEntry) throws IOException {
        rtm.getLogger().info("************************* Close water valve *************************");
        boolean success = rtm.getRadioCommandFactory().closeWaterValve();
        if (success) {
            return MessageResult.createSuccess(messageEntry);
        } else {
            rtm.getLogger().warning("Could not close the water valve.");
            return MessageResult.createFailed(messageEntry);
        }
    }

    private MessageResult openWaterValve(MessageEntry messageEntry) throws IOException {
        rtm.getLogger().info("************************* Open water valve *************************");
        boolean success = rtm.getRadioCommandFactory().openWaterValve();
        if (success) {
            return MessageResult.createSuccess(messageEntry);
        } else {
            rtm.getLogger().warning("Could not open the water valve.");
            return MessageResult.createFailed(messageEntry);
        }
    }

    public List<MessageCategorySpec> getMessageCategories() {
        List<MessageCategorySpec> theCategories = new ArrayList<>();

        MessageCategorySpec cat1 = new MessageCategorySpec("RTM general messages");
        cat1.addMessageSpec(addBasicMsgWithValue("Set operating mode", "SetOperatingMode", false));
        cat1.addMessageSpec(addBasicMsg("Reset application status", "ResetApplicationStatus", false));
        cat1.addMessageSpec(addBasicMsg("Force to sync the time", "ForceTimeSync", true));
        cat1.addMessageSpec(addBasicMsg("Execute encoder model detection", "EncoderModelDetection", false));
        cat1.addMessageSpec(addBasicMsgWithValue("Write index on port A (pulse register only)", "WriteIndexA", true));
        cat1.addMessageSpec(addBasicMsgWithValue("Write index on port B (pulse register only)", "WriteIndexB", true));
        cat1.addMessageSpec(addBasicMsgWithValue("Write index on port C (pulse register only)", "WriteIndexC", true));
        cat1.addMessageSpec(addBasicMsgWithValue("Write index on port D (pulse register only)", "WriteIndexD", true));
        cat1.addMessageSpec(addBasicMsgWithValue("Set meter model on port A (pulse register only)", "SetMeterModelA", true));
        cat1.addMessageSpec(addBasicMsgWithValue("Set meter model on port B (pulse register only)", "SetMeterModelB", true));
        cat1.addMessageSpec(addBasicMsgWithValue("Set meter model on port C (pulse register only)", "SetMeterModelC", true));
        cat1.addMessageSpec(addBasicMsgWithValue("Set meter model on port D (pulse register only)", "SetMeterModelD", true));
        cat1.addMessageSpec(addBasicMsgWithFourAttr("Write pulse weight (only for pulse registers)", "WritePulseWeight", true, "Port number (1 - 4)", "Scale (-4 to 4)", "Multiplier (1 or 5)", "Unit (1 = m3, 2 = liter, 3 = cubic ft., 4 = imperial gallons, 5 = US gallons)"));
        cat1.addMessageSpec(addBasicMsgWithThreeAttr("Write encoder unit (only for encoder registers)", "WriteEncoderUnit", true, "Port number (1 or 2)", "Number of digits before decimal point (max 6)", "Unit number in hex (see protocol release notes)"));
        cat1.addMessageSpec(addBasicMsgWithValue("Set number of repeaters (max 3)", "SetNumberOfRepeaters", true));
        cat1.addMessageSpec(addBasicMsgWithTwoAttr("Set address of repeater", "SetRepeaterAddress", true, "Number of the repeater (1, 2 or 3)", "Address (hex string)"));
        cat1.addMessageSpec(addBasicMsgWithAttr("Set address of the recipient", "SetRecipientAddress", true, "Address (hex string)"));
        theCategories.add(cat1);

        MessageCategorySpec cat2 = new MessageCategorySpec("RTM reading management");
        cat2.addMessageSpec(addBasicMsgWithValue("Restart the data logging in mode [1 = periodic steps, 2 = weekly logging, 3 = monthly logging]", "RestartDataLogging", true));
        cat2.addMessageSpec(addBasicMsgWithValue("Write the profile data interval", "WriteSamplingPeriod", false));
        cat2.addMessageSpec(addBasicMsgWithValue("Set day of week (or month) for weekly/monthly data logging", "WriteDayOfWeekOrMonth", false));
        cat2.addMessageSpec(addBasicMsgWithValue("Set hour of measurement for weekly/monthly data logging", "SetHourOfMeasurement", false));
        cat2.addMessageSpec(addBasicMsg("Stop the data logging", "StopDataLogging", false));
        cat2.addMessageSpec(addBasicMsg("Start data logging in periodic time steps", "SetPeriodicStepsLogging", false));
        cat2.addMessageSpec(addBasicMsg("Start weekly data logging", "SetWeeklyDataLogging", false));
        cat2.addMessageSpec(addBasicMsg("Start monthly data logging", "SetMonthlyDataLogging", false));
        cat2.addMessageSpec(addBasicMsgWithSevenAttr("Write start hour of the TOU buckets", "WriteTOUBucketStartHour", false, "Number of TOU buckets (minimum 2)", "Start hour of the 1st TOU bucket", "Start hour of the 2nd TOU bucket", "Start hour of the 3rd TOU bucket (if applicable)", "Start hour of the 4th TOU bucket (if applicable)", "Start hour of the 5th TOU bucket (if applicable)", "Start hour of the 6th TOU bucket (if applicable)"));
        cat2.addMessageSpec(addBasicMsg("Enable the TOU Buckets", "EnableTOUBuckets", false));
        cat2.addMessageSpec(addBasicMsg("Disable the TOU Buckets", "DisableTOUBuckets", false));
        theCategories.add(cat2);

        MessageCategorySpec cat3 = new MessageCategorySpec("RTM alarm frames configuration");
        cat3.addMessageSpec(addBasicMsgWithThreeAttr("Write time windows dedicated to alarm sending", "SetAlarmWindowConfiguration", true, "Time slot mechanism activation (0 = disable, 1 = enable)", "Time slot duration (30, 45, 60, 90 or 120 seconds)", "Time slot granularity (every 15, 30 or 60 minutes)"));
        cat3.addMessageSpec(addBasicMsg("Automatically configure the destination route", "AutoConfigAlarmRoute", true));
        cat3.addMessageSpec(addBasicMsgWithValue("Set the alarm configuration byte", "SetAlarmConfiguration", true));
        cat3.addMessageSpec(addBasicMsg("Enable all alarms", "EnableAllAlarms", true));
        cat3.addMessageSpec(addBasicMsg("Disable all alarms", "DisableAllAlarms", true));
        cat3.addMessageSpec(addBasicMsg("Set alarm on backflow detection", "setAlarmOnBackFlow", true));
        cat3.addMessageSpec(addBasicMsg("Set alarm on cut cable detection", "setAlarmOnCutCable", true));
        cat3.addMessageSpec(addBasicMsg("Set alarm on cut register cable detection", "setAlarmOnCutRegisterCable", true));
        cat3.addMessageSpec(addBasicMsg("Set alarm on default valve detection", "setAlarmOnDefaultValve", true));
        cat3.addMessageSpec(addBasicMsg("Set alarm on encoder communication failure detection", "setAlarmOnEncoderCommunicationFailure", true));
        cat3.addMessageSpec(addBasicMsg("Set alarm on encoder misread detection", "setAlarmOnEncoderMisread", true));
        cat3.addMessageSpec(addBasicMsg("Set alarm on high threshold detection", "setAlarmOnHighThreshold", true));
        cat3.addMessageSpec(addBasicMsg("Set alarm on low battery detection", "setAlarmOnLowBattery", true));
        cat3.addMessageSpec(addBasicMsg("Set alarm on low threshold detection", "setAlarmOnLowThreshold", true));
        cat3.addMessageSpec(addBasicMsg("Disable alarm on backflow detection", "disableAlarmOnBackFlow", true));
        cat3.addMessageSpec(addBasicMsg("Disable alarm on cut cable detection", "disableAlarmOnCutCable", true));
        cat3.addMessageSpec(addBasicMsg("Disable alarm on cut register cable detection", "disableAlarmOnCutRegisterCable", true));
        cat3.addMessageSpec(addBasicMsg("Disable alarm on default valve detection", "disableAlarmOnDefaultValve", true));
        cat3.addMessageSpec(addBasicMsg("Disable alarm on encoder communication failure detection", "disableAlarmOnEncoderCommunicationFailure", true));
        cat3.addMessageSpec(addBasicMsg("Disable alarm on encoder misread detection", "disableAlarmOnEncoderMisread", true));
        cat3.addMessageSpec(addBasicMsg("Disable alarm on high threshold detection", "disableAlarmOnHighThreshold", true));
        cat3.addMessageSpec(addBasicMsg("Disable alarm on low battery detection", "disableAlarmOnLowBattery", true));
        cat3.addMessageSpec(addBasicMsg("Disable alarm on low threshold detection", "disableAlarmOnLowThreshold", true));
        theCategories.add(cat3);

        MessageCategorySpec cat4 = new MessageCategorySpec("Fixed network/Walk by/Drive by switching");
        cat4.addMessageSpec(addBasicMsgWithValue("Set walk by/drive by wake up channel (0 - 83)", "SetWakeUpChannel", true));
        cat4.addMessageSpec(addBasicMsgWithThreeAttr("Set drive by inter-answer delay", "SetInterAnswerDelay", true, "Hour (0 - 23)", "Minutes (0 - 59)", "Seconds (0 - 59)"));
        theCategories.add(cat4);

        MessageCategorySpec cat5 = new MessageCategorySpec("Pseudo bubble up configuration");
        cat5.addMessageSpec(addBasicMsgWithThreeAttr("Set starting hour, minutes and seconds of the mechanism", "SetStartOfMechanism", true, "starting hour (0 - 23)", "minutes (0 - 59)", "seconds (0 - 59)"));
        cat5.addMessageSpec(addBasicMsgWithValue("Set end hour of bubble up period", "SetEndOfMechanism", true));
        cat5.addMessageSpec(addBasicMsgWithValue("Set transmission period (in minutes!)", "SetTransmissionPeriod", true));
        cat5.addMessageSpec(addBasicMsgWithValue("Set max cancellation timeout (1 - 10 seconds)", "SetMaxCancelTimeout", true));
        cat5.addMessageSpec(addBasicMsgWithOptionalAttr("Add applicative command to the command buffer", "AddCommandToBuffer", true, "Applicative command (e.g. Current reading = 1)", "Port mask (decimal value) (only for command 7)", "Expected number of readings per port (only for command 7)"));
        cat5.addMessageSpec(addBasicMsg("Enable bubble up mechanism flag in the operation mode", "EnableBubbleUpMechanism", true));
        cat5.addMessageSpec(addBasicMsg("Disable bubble up mechanism flag in the operation mode", "DisableBubbleUpMechanism", true));
        cat5.addMessageSpec(addBasicMsgWithOptionalAttr2("Start bubble up mechanism", "StartBubbleUpMechanism", true, "Applicative command (e.g. Current reading = 1)", "Port mask (decimal value) (only for command 7)", "Expected number of readings per port (only for command 7)", "Transmission period (range: 1 - 63)", "Transmission period unit (0: minute, 1: hour, 2: day)"));
        cat5.addMessageSpec(addBasicMsg("Clear the command buffer", "ClearCommandBuffer", true));
        theCategories.add(cat5);

        MessageCategorySpec cat6 = new MessageCategorySpec("Event detection configuration");
        cat6.addMessageSpec(addBasicMsgWithThreeAttr("Set leakage consumption-rate", "SetLeakageConsumptionRate", true, "Residual (0) or extreme (1)", "Port (1 = A, 2 = B,...)", "Consumption-rate (same unit as the reading unit)"));
        cat6.addMessageSpec(addBasicMsgWithThreeAttr("Set leakage detection period", "SetLeakageDetectionPeriod", true, "Residual (0) or extreme (1)", "Port (1 = A, 2 = B,...)", "Detection period (multiples of profile data interval)"));
        cat6.addMessageSpec(addBasicMsgWithTwoAttr("Set backflow detection period", "SetBackflowDetectionPeriod", true, "Port (1 = A, 2 = B)", "Detection period (multiples of profile data interval)"));
        cat6.addMessageSpec(addBasicMsgWithTwoAttr("Set number of backflow detections before indication", "SetNumberOfBackflowsBeforeIndication", true, "Port (1 = A, 2 = B)", "Number of backflow detections before indication"));
        cat6.addMessageSpec(addBasicMsgWithTwoAttr("Set backflow threshold", "WriteBackflowThreshold", true, "Port (1 = A, 2 = B)", "Threshold (same unit as encoder)"));
        cat6.addMessageSpec(addBasicMsg("Clear the backflow flags", "ClearBackFlowFlags", true));
        theCategories.add(cat6);

        MessageCategorySpec cat7 = new MessageCategorySpec("Valve control messages");
        cat7.addMessageSpec(addBasicMsg("Open the water valve", "OpenWaterValve", true));
        cat7.addMessageSpec(addBasicMsg("Close the water valve", "CloseWaterValve", true));
        cat7.addMessageSpec(addBasicMsg("Clean the water valve", "CleanWaterValve", true));
        theCategories.add(cat7);

        return theCategories;
    }

    protected MessageSpec addBasicMsgWithAttr(final String keyId, final String tagName, final boolean advanced, String attr) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageAttributeSpec addAttribute = new MessageAttributeSpec(attr, true);
        tagSpec.add(addAttribute);
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

    protected MessageSpec addBasicMsgWithOptionalAttr(final String keyId, final String tagName, final boolean advanced, String attr1, String attr2, String attr3) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageAttributeSpec(attr1, true));
        tagSpec.add(new MessageAttributeSpec(attr2, false));
        tagSpec.add(new MessageAttributeSpec(attr3, false));
        tagSpec.add(new MessageValueSpec(" "));
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    protected MessageSpec addBasicMsgWithOptionalAttr2(final String keyId, final String tagName, final boolean advanced, String attr1, String attr2, String attr3, String attr4, String attr5) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageAttributeSpec(attr1, true));
        tagSpec.add(new MessageAttributeSpec(attr2, false));
        tagSpec.add(new MessageAttributeSpec(attr3, false));
        tagSpec.add(new MessageAttributeSpec(attr4, true));
        tagSpec.add(new MessageAttributeSpec(attr5, true));
        tagSpec.add(new MessageValueSpec(" "));
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    protected MessageSpec addBasicMsgWithSevenAttr(final String keyId, final String tagName, final boolean advanced, String attr1, String attr2, String attr3, String attr4, String attr5, String attr6, String attr7) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageAttributeSpec(attr1, true));
        tagSpec.add(new MessageAttributeSpec(attr2, true));
        tagSpec.add(new MessageAttributeSpec(attr3, true));
        tagSpec.add(new MessageAttributeSpec(attr4, false));
        tagSpec.add(new MessageAttributeSpec(attr5, false));
        tagSpec.add(new MessageAttributeSpec(attr6, false));
        tagSpec.add(new MessageAttributeSpec(attr7, false));
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