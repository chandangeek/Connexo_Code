/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.core.messages;

import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.data.MessageResult;
import com.energyict.mdc.protocol.api.messaging.Message;
import com.energyict.mdc.protocol.api.messaging.MessageAttribute;
import com.energyict.mdc.protocol.api.messaging.MessageAttributeSpec;
import com.energyict.mdc.protocol.api.messaging.MessageElement;
import com.energyict.mdc.protocol.api.messaging.MessageSpec;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageTagSpec;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.mdc.protocol.api.messaging.MessageValueSpec;

import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.hydreka.parameter.ParameterFactoryHydreka;

import java.io.IOException;
import java.util.List;

public abstract class WaveFlowMessageParser implements MessageProtocol {

    WaveFlow waveFlow;

    private String stripOffTag(String content) {
        return content.substring(content.indexOf(">") + 1, content.lastIndexOf("<"));
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

    protected MessageSpec addBasicMsg(final String keyId, final String tagName, final boolean advanced) {
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

    protected MessageSpec addBasicMsgWithThreeValues(final String keyId, final String tagName, final boolean advanced, String attr1, String attr2) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageAttributeSpec addAttribute = new MessageAttributeSpec(attr1, true);
        tagSpec.add(addAttribute);
        MessageAttributeSpec closeAttribute = new MessageAttributeSpec(attr2, true);
        tagSpec.add(closeAttribute);
        MessageValueSpec msgVal = new MessageValueSpec();
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    protected MessageSpec addBasicMsgWithFourValues(final String keyId, final String tagName, final boolean advanced, String attr1, String attr2, String attr3) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageAttributeSpec addAttribute = new MessageAttributeSpec(attr1, true);
        tagSpec.add(addAttribute);
        MessageAttributeSpec closeAttribute = new MessageAttributeSpec(attr2, true);
        tagSpec.add(closeAttribute);
        MessageAttributeSpec thirdAttribute = new MessageAttributeSpec(attr3, true);
        tagSpec.add(thirdAttribute);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" "); //Disable this field
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    protected MessageSpec addBasicMsgWithSevenAttributes(final String keyId, final String tagName, final boolean advanced, String attr1, String attr2, String attr3, String attr4, String attr5, String attr6, String attr7) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageAttributeSpec attrSpec1 = new MessageAttributeSpec(attr1, true);
        tagSpec.add(attrSpec1);
        MessageAttributeSpec attrSpec2 = new MessageAttributeSpec(attr2, true);
        tagSpec.add(attrSpec2);
        MessageAttributeSpec attrSpec3 = new MessageAttributeSpec(attr3, true);
        tagSpec.add(attrSpec3);
        MessageAttributeSpec attrSpec4 = new MessageAttributeSpec(attr4, true);
        tagSpec.add(attrSpec4);
        MessageAttributeSpec attrSpec5 = new MessageAttributeSpec(attr5, true);
        tagSpec.add(attrSpec5);
        MessageAttributeSpec attrSpec6 = new MessageAttributeSpec(attr6, true);
        tagSpec.add(attrSpec6);
        MessageAttributeSpec attrSpec7 = new MessageAttributeSpec(attr7, true);
        tagSpec.add(attrSpec7);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" "); //Disable this field
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    protected MessageSpec addBasicMsgWithFourteenAttr(final String keyId, final String tagName, final boolean advanced, String attr1, String attr2, String attr3, String attr4, String attr5, String attr6, String attr7, String attr8, String attr9, String attr10, String attr11, String attr12, String attr13, String attr14) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageAttributeSpec attrSpec1 = new MessageAttributeSpec(attr1, true);
        tagSpec.add(attrSpec1);
        MessageAttributeSpec attrSpec2 = new MessageAttributeSpec(attr2, true);
        tagSpec.add(attrSpec2);
        MessageAttributeSpec attrSpec3 = new MessageAttributeSpec(attr3, true);
        tagSpec.add(attrSpec3);
        MessageAttributeSpec attrSpec4 = new MessageAttributeSpec(attr4, true);
        tagSpec.add(attrSpec4);
        MessageAttributeSpec attrSpec5 = new MessageAttributeSpec(attr5, true);
        tagSpec.add(attrSpec5);
        MessageAttributeSpec attrSpec6 = new MessageAttributeSpec(attr6, true);
        tagSpec.add(attrSpec6);
        MessageAttributeSpec attrSpec7 = new MessageAttributeSpec(attr7, true);
        tagSpec.add(attrSpec7);
        MessageAttributeSpec attrSpec8 = new MessageAttributeSpec(attr8, true);
        tagSpec.add(attrSpec8);
        MessageAttributeSpec attrSpec9 = new MessageAttributeSpec(attr9, true);
        tagSpec.add(attrSpec9);
        MessageAttributeSpec attrSpec10 = new MessageAttributeSpec(attr10, true);
        tagSpec.add(attrSpec10);
        MessageAttributeSpec attrSpec11 = new MessageAttributeSpec(attr11, true);
        tagSpec.add(attrSpec11);
        MessageAttributeSpec attrSpec12 = new MessageAttributeSpec(attr12, true);
        tagSpec.add(attrSpec12);
        MessageAttributeSpec attrSpec13 = new MessageAttributeSpec(attr13, true);
        tagSpec.add(attrSpec13);
        MessageAttributeSpec attrSpec14 = new MessageAttributeSpec(attr14, true);
        tagSpec.add(attrSpec14);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" "); //Disable this field
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    protected MessageSpec addBasicMsgWithFifteenAttr(final String keyId, final String tagName, final boolean advanced, String attr1, String attr2, String attr3, String attr4, String attr5, String attr6, String attr7, String attr8, String attr9, String attr10, String attr11, String attr12, String attr13, String attr14, String attr15) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageAttributeSpec attrSpec1 = new MessageAttributeSpec(attr1, true);
        tagSpec.add(attrSpec1);
        MessageAttributeSpec attrSpec2 = new MessageAttributeSpec(attr2, true);
        tagSpec.add(attrSpec2);
        MessageAttributeSpec attrSpec3 = new MessageAttributeSpec(attr3, true);
        tagSpec.add(attrSpec3);
        MessageAttributeSpec attrSpec4 = new MessageAttributeSpec(attr4, true);
        tagSpec.add(attrSpec4);
        MessageAttributeSpec attrSpec5 = new MessageAttributeSpec(attr5, true);
        tagSpec.add(attrSpec5);
        MessageAttributeSpec attrSpec6 = new MessageAttributeSpec(attr6, true);
        tagSpec.add(attrSpec6);
        MessageAttributeSpec attrSpec7 = new MessageAttributeSpec(attr7, true);
        tagSpec.add(attrSpec7);
        MessageAttributeSpec attrSpec8 = new MessageAttributeSpec(attr8, true);
        tagSpec.add(attrSpec8);
        MessageAttributeSpec attrSpec9 = new MessageAttributeSpec(attr9, true);
        tagSpec.add(attrSpec9);
        MessageAttributeSpec attrSpec10 = new MessageAttributeSpec(attr10, true);
        tagSpec.add(attrSpec10);
        MessageAttributeSpec attrSpec11 = new MessageAttributeSpec(attr11, true);
        tagSpec.add(attrSpec11);
        MessageAttributeSpec attrSpec12 = new MessageAttributeSpec(attr12, true);
        tagSpec.add(attrSpec12);
        MessageAttributeSpec attrSpec13 = new MessageAttributeSpec(attr13, true);
        tagSpec.add(attrSpec13);
        MessageAttributeSpec attrSpec14 = new MessageAttributeSpec(attr14, true);
        tagSpec.add(attrSpec14);
        MessageAttributeSpec attrSpec15 = new MessageAttributeSpec(attr15, true);
        tagSpec.add(attrSpec15);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" "); //Disable this field
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    protected MessageSpec addBasicMsgWithThreeAttr(final String keyId, final String tagName, final boolean advanced, String attr1, String attr2, String attr3) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageAttributeSpec attrSpec1 = new MessageAttributeSpec(attr1, true);
        tagSpec.add(attrSpec1);
        MessageAttributeSpec attrSpec2 = new MessageAttributeSpec(attr2, true);
        tagSpec.add(attrSpec2);
        MessageAttributeSpec attrSpec3 = new MessageAttributeSpec(attr3, true);
        tagSpec.add(attrSpec3);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" "); //Disable this field
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    protected MessageSpec addBasicMsgWithFourAttributes(final String keyId, final String tagName, final boolean advanced, String attr1, String attr2, String attr3, String attr4) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageAttributeSpec attrSpec1 = new MessageAttributeSpec(attr1, true);
        tagSpec.add(attrSpec1);
        MessageAttributeSpec attrSpec2 = new MessageAttributeSpec(attr2, true);
        tagSpec.add(attrSpec2);
        MessageAttributeSpec attrSpec3 = new MessageAttributeSpec(attr3, true);
        tagSpec.add(attrSpec3);
        MessageAttributeSpec attrSpec4 = new MessageAttributeSpec(attr4, true);
        tagSpec.add(attrSpec4);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" "); //Disable this field
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    protected MessageSpec addBasicMsgWithTwoValues(final String keyId, final String tagName, final boolean advanced, String attr1) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageAttributeSpec addAttribute = new MessageAttributeSpec(attr1, true);
        tagSpec.add(addAttribute);
        MessageValueSpec msgVal = new MessageValueSpec();
        tagSpec.add(msgVal);
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

    private static final int MAX_SAMPLING_INTERVAL_SECONDS = 63 * 30 * 60;  //See documentation, largest interval possible is 31,5 hours.
    private static final int MAX_TRANSMISSION_PERIOD = 60 * 24 * 63;        //number of minutes in 63 days

    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        try {
            if (messageEntry.getContent().contains("<RestartDataLogging")) {
                return restartDataLogging(messageEntry);
            } else if (messageEntry.getContent().contains("<SimpleRestartDataLogging")) {
                return simpleRestartDataLogging(messageEntry);
            } else if (messageEntry.getContent().contains("<StopDataLogging")) {
                return stopDataLogging(messageEntry);
            } else if (messageEntry.getContent().contains("<SetNumberOfInputs")) {
                return setNumberOfInputs(messageEntry);
            } else if (messageEntry.getContent().contains("<ResetIndexes")) {
                return resetIndexes(messageEntry);
            } else if (messageEntry.getContent().contains("<WriteIndexA")) {
                return writeIndex(messageEntry, 1);
            } else if (messageEntry.getContent().contains("<InitializeRoute")) {
                return initializeRoute(messageEntry);
            } else if (messageEntry.getContent().contains("<WriteIndexB")) {
                return writeIndex(messageEntry, 2);
            } else if (messageEntry.getContent().contains("<WriteIndexC")) {
                return writeIndex(messageEntry, 3);
            } else if (messageEntry.getContent().contains("<WriteIndexD")) {
                return writeIndex(messageEntry, 4);
            } else if (messageEntry.getContent().contains("<ForceTimeSync")) {
                return forceTimeSync(messageEntry);
            } else if (messageEntry.getContent().contains("<SetBackflowDetectionMethod")) {
                return setBackflowDetectionMethod(messageEntry);
            } else if (messageEntry.getContent().contains("<SetOperatingModeWithMask")) {
                return setOperationModeWithMask(messageEntry);
            } else if (messageEntry.getContent().contains("<SetOperatingMode")) {
                return setOperationMode(messageEntry);
            } else if (messageEntry.getContent().contains("<SetLeakageStatusReadingHour")) {
                return setLeakageStatusReadingHour(messageEntry);
            } else if (messageEntry.getContent().contains("<SetHistogramReadingHour")) {
                return setHistogramReadingHour(messageEntry);
            } else if (messageEntry.getContent().contains("<SetRTCResyncPeriod")) {
                return setRTCResyncPeriod(messageEntry);
            } else if (messageEntry.getContent().contains("<ResetApplicationStatusFull")) {
                return resetApplicationStatus(messageEntry);
            } else if (messageEntry.getContent().contains("<ResetApplicationStatusBit0")) {
                return resetApplicationStatusBit(messageEntry, 0);
            } else if (messageEntry.getContent().contains("<ResetApplicationStatusBit1")) {
                return resetApplicationStatusBit(messageEntry, 1);
            } else if (messageEntry.getContent().contains("<ResetApplicationStatusBit3")) {
                return resetApplicationStatusBit(messageEntry, 3);
            } else if (messageEntry.getContent().contains("<ResetApplicationStatusBit4")) {
                return resetApplicationStatusBit(messageEntry, 4);
            } else if (messageEntry.getContent().contains("<ResetApplicationStatusBit5")) {
                return resetApplicationStatusBit(messageEntry, 5);
            } else if (messageEntry.getContent().contains("<ResetApplicationStatusBit7")) {
                return resetApplicationStatusBit(messageEntry, 7);
            } else if (messageEntry.getContent().contains("<ResetValveApplicationStatusFull")) {
                return resetValveApplicationStatus(messageEntry);
            } else if (messageEntry.getContent().contains("<ResetValveApplicationStatusBit0")) {
                return resetValveApplicationStatusBit(messageEntry, 0);
            } else if (messageEntry.getContent().contains("<ResetValveApplicationStatusBit1")) {
                return resetValveApplicationStatusBit(messageEntry, 1);
            } else if (messageEntry.getContent().contains("<ResetValveApplicationStatusBit2")) {
                return resetValveApplicationStatusBit(messageEntry, 2);
            } else if (messageEntry.getContent().contains("<ResetValveApplicationStatusBit3")) {
                return resetValveApplicationStatusBit(messageEntry, 3);
            } else if (messageEntry.getContent().contains("<SetDayOfWeek")) {
                return setDayOfWeekOrMonth(messageEntry);
            } else if (messageEntry.getContent().contains("<SetHourOfDailyIndexStorage")) {
                return setHourForDailyIndexStorage(messageEntry);
            } else if (messageEntry.getContent().contains("<SetPeriodicStepLogging")) {
                return setPeriodicStepLogging(messageEntry);
            } else if (messageEntry.getContent().contains("<SetMonthlyLogging")) {
                return setMonthlyLogging(messageEntry);
            } else if (messageEntry.getContent().contains("<SetWeeklyLogging")) {
                return setWeeklyLogging(messageEntry);
            } else if (messageEntry.getContent().contains("<DefinePulseWeight")) {
                return definePulseWeight(messageEntry);
            } else if (messageEntry.getContent().contains("<SetHourOfMeasurement")) {
                return setHourOfMeasurement(messageEntry);
            } else if (messageEntry.getContent().contains("<SetMinuteOfMeasurement")) {
                return setMinuteOfmeasurement(messageEntry);
            } else if (messageEntry.getContent().contains("<OpenWaterValve")) {
                return openWaterValve(messageEntry);
            } else if (messageEntry.getContent().contains("<CloseWaterValve")) {
                return closeWaterValve(messageEntry);
            } else if (messageEntry.getContent().contains("<CleanWaterValve")) {
                return cleanWaterValve(messageEntry);
            } else if (messageEntry.getContent().contains("<AddCreditBeforeClosing")) {
                return addCreditBeforeClosing(messageEntry);
            } else if (messageEntry.getContent().contains("<SetLeakageThreshold")) {
                return setLeakageThreshold(messageEntry);
            } else if (messageEntry.getContent().contains("<SetLeakageDetectionPeriod")) {
                return setLeakageDetectionPeriod(messageEntry);
            } else if (messageEntry.getContent().contains("<SetMeasurementStep")) {
                return setMeasurementStep(messageEntry);
            } else if (messageEntry.getContent().contains("<SetProfileInterval")) {
                return setProfileInterval(messageEntry);
            } else if (messageEntry.getContent().contains("<SetSimpleBackflowThreshold")) {
                return setBackflowThreshold(messageEntry, true);
            } else if (messageEntry.getContent().contains("<SetAdvancedBackflowThreshold")) {
                return setBackflowThreshold(messageEntry, false);
            } else if (messageEntry.getContent().contains("<SetSimpleBackflowDetectionPeriod")) {
                return setBackflowDetectionPeriod(messageEntry, true);
            } else if (messageEntry.getContent().contains("<SetAdvancedBackflowDetectionPeriod")) {
                return setBackflowDetectionPeriod(messageEntry, false);
            } else if (messageEntry.getContent().contains("<EnablePushFrames")) {
                return enablePushFrames(messageEntry);
            } else if (messageEntry.getContent().contains("<StartPushFrames")) {
                return startPushFrames(messageEntry);
            } else if (messageEntry.getContent().contains("<DisablePushFrames")) {
                return disablePushFrames(messageEntry);
            } else if (messageEntry.getContent().contains("<SetStartOfMechanism")) {
                return setStartOfMechanism(messageEntry);
            } else if (messageEntry.getContent().contains("<SetTransmissionPeriod")) {
                return setTransmissionPeriod(messageEntry);
            } else if (messageEntry.getContent().contains("<SetMaxCancelTimeout")) {
                return setMaxCancelTimeout(messageEntry);
            } else if (messageEntry.getContent().contains("<AddCommandToBuffer")) {
                return addCommandToBuffer(messageEntry);
            } else if (messageEntry.getContent().contains("<ClearCommandBuffer")) {
                return clearCommandBuffer(messageEntry);
            } else if (messageEntry.getContent().contains("<SetAlarmConfig")) {
                return setAlarmConfig(messageEntry);
            } else if (messageEntry.getContent().contains("<SetNumberOfRepeaters")) {
                return setNumberOfRepeaters(messageEntry);
            } else if (messageEntry.getContent().contains("<SetRepeaterAddress")) {
                return setRepeaterAddress(messageEntry);
            } else if (messageEntry.getContent().contains("<SetRecipientAddress")) {
                return setRecipientAddress(messageEntry);
            } else if (messageEntry.getContent().contains("<SendAlarmOnWirecutDetection")) {
                return sendAlarmOnWirecutDetection(messageEntry);
            } else if (messageEntry.getContent().contains("<SendAlarmOnBatteryEnd")) {
                return sendAlarmOnBatteryEnd(messageEntry);
            } else if (messageEntry.getContent().contains("<SendAlarmOnLowLeakDetection")) {
                return sendAlarmOnLowLeakDetection(messageEntry);
            } else if (messageEntry.getContent().contains("<SendAlarmOnHighLeakDetection")) {
                return sendAlarmOnHighLeakDetection(messageEntry);
            } else if (messageEntry.getContent().contains("<SendAlarmOnBackflowDetection")) {
                return sendAlarmOnBackflowDetection(messageEntry);
            } else if (messageEntry.getContent().contains("<SendAlarmOnValveWirecut")) {
                return sendAlarmOnValveWirecut(messageEntry);
            } else if (messageEntry.getContent().contains("<SendAlarmOnValveCloseFault")) {
                return sendAlarmOnValveCloseFault(messageEntry);
            } else if (messageEntry.getContent().contains("<SendAlarmOnThresholdDetectionOfCreditAmount")) {
                return sendAlarmOnThresholdDetectionOfCreditAmount(messageEntry);
            } else if (messageEntry.getContent().contains("<DisableAlarmOnWirecutDetection")) {
                return disableAlarmOnWirecutDetection(messageEntry);
            } else if (messageEntry.getContent().contains("<DisableAlarmOnBatteryEnd")) {
                return disableAlarmOnBatteryEnd(messageEntry);
            } else if (messageEntry.getContent().contains("<DisableAlarmOnLowLeakDetection")) {
                return disableAlarmOnLowLeakDetection(messageEntry);
            } else if (messageEntry.getContent().contains("<DisableAlarmOnHighLeakDetection")) {
                return disableAlarmOnHighLeakDetection(messageEntry);
            } else if (messageEntry.getContent().contains("<DisableAlarmOnBackflowDetection")) {
                return disableAlarmOnBackflowDetection(messageEntry);
            } else if (messageEntry.getContent().contains("<DisableAlarmOnValveWirecut")) {
                return disableAlarmOnValveWirecut(messageEntry);
            } else if (messageEntry.getContent().contains("<DisableAlarmOnValveCloseFault")) {
                return disableAlarmOnValveCloseFault(messageEntry);
            } else if (messageEntry.getContent().contains("<DisableAlarmOnThresholdDetectionOfCreditAmount")) {
                return disableAlarmOnThresholdDetectionOfCreditAmount(messageEntry);
            } else if (messageEntry.getContent().contains("<DisableAllAlarms")) {
                return disableAllAlarms(messageEntry);
            } else if (messageEntry.getContent().contains("<SendAllAlarms")) {
                return sendAllAlarms(messageEntry);
            } else if (messageEntry.getContent().contains("<SetTimeSlotGranularity")) {
                return setTimeSlotGranularity(messageEntry);
            } else if (messageEntry.getContent().contains("<SetTimeSlotDuration")) {
                return setTimeSlotDuration(messageEntry);
            } else if (messageEntry.getContent().contains("<EnableTimeSlotMechanism")) {
                return enableTimeSlotMechanism(messageEntry);
            } else if (messageEntry.getContent().contains("<DisableTimeSlotMechanism")) {
                return disableTimeSlotMechanism(messageEntry);
            } else if (messageEntry.getContent().contains("<WriteFeatureData")) {
                return writeFeatureData(messageEntry);
            } else if (messageEntry.getContent().contains("<WritePeakFlowSettings")) {
                return writePeakFlowSettings(messageEntry);
            } else if (messageEntry.getContent().contains("<WriteOverSpeedParameters")) {
                return writeOverSpeedParameters(messageEntry);
            } else if (messageEntry.getContent().contains("<Write7BandParameters")) {
                return write7BandParameters(messageEntry);
            } else if (messageEntry.getContent().contains("<Write4DailySegmentsParameters")) {
                return write4DailySegmentsParameters(messageEntry);
            } else if (messageEntry.getContent().contains("<WriteDateOfInstallation")) {
                return writeDateOfInstallation(messageEntry);
            } else if (messageEntry.getContent().contains("<WriteCustomerNumber")) {
                return writeCustomerNumber(messageEntry);
            } else if (messageEntry.getContent().contains("<EnableRisingBlockTariffs")) {
                return enableRisingBlockTariffs(messageEntry);
            } else if (messageEntry.getContent().contains("<EnableTimeOfUseTariffs")) {
                return enableTimeOfUseTariffs(messageEntry);
            } else if (messageEntry.getContent().contains("<SetWakeUpSystemStatusWord")) {
                return setWakeUpSystemStatusWord(messageEntry);
            } else if (messageEntry.getContent().contains("<SetDefaultWakeUpPeriod")) {
                return setDefaultWakeUpPeriod(messageEntry);
            } else if (messageEntry.getContent().contains("<SetStartTimeForTimeWindow1")) {
                return setStartTimeForTimeWindow1(messageEntry);
            } else if (messageEntry.getContent().contains("<SetWakeUpPeriodForTimeWindow1")) {
                return setWakeUpPeriodForTimeWindow1(messageEntry);
            } else if (messageEntry.getContent().contains("<SetStartTimeForTimeWindow2")) {
                return setStartTimeForTimeWindow2(messageEntry);
            } else if (messageEntry.getContent().contains("<SetWakeUpPeriodForTimeWindow2")) {
                return setWakeUpPeriodForTimeWindow2(messageEntry);
            } else if (messageEntry.getContent().contains("<SetEnableTimeWindowsByDayOfWeek")) {
                return setEnableTimeWindowsByDayOfWeek(messageEntry);
            } else if (messageEntry.getContent().contains("<SetEnableWakeUpPeriodsByDayOfWeek")) {
                return setEnableWakeUpPeriodsByDayOfWeek(messageEntry);
            } else {
                waveFlow.getLogger().severe("Unknown message, cannot execute");
                return MessageResult.createFailed(messageEntry);
            }
        } catch (NumberFormatException e) {
            waveFlow.getLogger().severe("Error parsing message, " + e.getMessage());
            return MessageResult.createFailed(messageEntry);
        } catch (WaveFlowException e) {
            waveFlow.getLogger().severe("Message failed, " + e.getMessage());
            return MessageResult.createFailed(messageEntry);
        }
    }

    private MessageResult setWakeUpSystemStatusWord(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* setWakeUpSystemStatusWord *************************");
        int value = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (value > 3 || value < 0) {        //Range: 0, 1, 2 or 3
            waveFlow.getLogger().severe("Error writing the wakeup system status word, should be in range 0 - 3");
            return MessageResult.createFailed(messageEntry);
        }
        waveFlow.getParameterFactory().setWakeUpSystemStatusWord(value);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setStartTimeForTimeWindow2(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* setStartTimeForTimeWindow2 *************************");
        int value = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (value > 23 || value < 0) {
            waveFlow.getLogger().severe("Error writing the start time for window 2, range is 0 - 23");
            return MessageResult.createFailed(messageEntry);
        }
        waveFlow.getParameterFactory().setStartTimeForTimeWindow2(value);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setEnableWakeUpPeriodsByDayOfWeek(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* setEnableWakeUpPeriodsByDayOfWeek *************************");
        int value = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (value > 0xFF || value < 0) {
            return MessageResult.createFailed(messageEntry);
        }
        waveFlow.getParameterFactory().setEnableWakeUpPeriodsByDayOfWeek(value);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setEnableTimeWindowsByDayOfWeek(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* setEnableTimeWindowsByDayOfWeek *************************");
        int value = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (value > 0xFF || value < 0) {
            return MessageResult.createFailed(messageEntry);
        }
        waveFlow.getParameterFactory().setEnableTimeWindowsByDayOfWeek(value);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setWakeUpPeriodForTimeWindow2(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* setWakeUpPeriodForTimeWindow2 *************************");
        int value = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (value > 10 || value < 0) {
            waveFlow.getLogger().severe("Error writing the wake up period for window 2, maximum is 10 seconds");
            return MessageResult.createFailed(messageEntry);
        }
        waveFlow.getParameterFactory().setWakeUpPeriodForTimeWindow2(value);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setWakeUpPeriodForTimeWindow1(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* setWakeUpPeriodForTimeWindow1 *************************");
        int value = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (value > 10 || value < 0) {
            waveFlow.getLogger().severe("Error writing the wake up period for window 1, maximum is 10 seconds");
            return MessageResult.createFailed(messageEntry);
        }
        waveFlow.getParameterFactory().setWakeUpPeriodForTimeWindow1(value);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setDefaultWakeUpPeriod(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* setDefaultWakeUpPeriod *************************");
        int value = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (value > 10 || value < 0) {    //Range: max 10 seconds
            waveFlow.getLogger().severe("Error writing the default wake up period, maximum is 10 seconds");
            return MessageResult.createFailed(messageEntry);
        }
        waveFlow.getParameterFactory().setDefaultWakeUpPeriod(value);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setStartTimeForTimeWindow1(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* setStartTimeForTimeWindow1 *************************");
        int value = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (value > 23 || value < 0) {
            waveFlow.getLogger().severe("Error writing the start time for window 1, range is 0 - 23");
            return MessageResult.createFailed(messageEntry);
        }
        waveFlow.getParameterFactory().setStartTimeForTimeWindow1(value);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult simpleRestartDataLogging(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* SimpleRestartDataLogging *************************");
        int mode = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (mode < 1 || mode > 3) {
            return MessageResult.createFailed(messageEntry);
        }
        waveFlow.getParameterFactory().simpleRestartDataLogging(mode);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult stopDataLogging(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* stopDataLogging *************************");
        waveFlow.getParameterFactory().stopDataLogging();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult writeIndex(MessageEntry messageEntry, int input) throws IOException {
        waveFlow.getLogger().info("************************* writeIndex *************************");
        int index = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        waveFlow.getRadioCommandFactory().writeIndexes(index, input);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult initializeRoute(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* initializeRoute *************************");
        String[] parts = messageEntry.getContent().split("=");
        int alarmMode = Integer.parseInt(parts[1].substring(1).split("\"")[0]);
        waveFlow.getRadioCommandFactory().initializeRoute(alarmMode);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult resetIndexes(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* resetIndexes *************************");
        waveFlow.getRadioCommandFactory().resetIndexes();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setTimeSlotGranularity(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* SetTimeSlotGranularity *************************");
        String[] parts = messageEntry.getContent().split("=");
        int minutes = Integer.parseInt(parts[1].substring(1, 3));
        if (minutes != 15 && minutes != 30 && minutes != 60) {
            return MessageResult.createFailed(messageEntry);
        }
        waveFlow.getParameterFactory().setTimeSlotGranularity(minutes);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setTimeSlotDuration(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* SetTimeSlotDuration *************************");
        String[] parts = messageEntry.getContent().split("=");
        int duration = Integer.parseInt(parts[1].substring(1, 3));
        if (duration != 30 && duration != 45 && duration != 60 && duration != 90 && duration != 120) {
            return MessageResult.createFailed(messageEntry);
        }
        waveFlow.getParameterFactory().setTimeSlotDuration(duration);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult enableRisingBlockTariffs(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* EnableRisingBlockTariffs *************************");
        String[] parts = messageEntry.getContent().split("=");
        int numberOfLogBlocks = Integer.parseInt(parts[1].substring(1, 2));
        int scale;
        try {
            scale = Integer.parseInt(parts[2].substring(1, 3));
        } catch (NumberFormatException e) {
            scale = Integer.parseInt(parts[2].substring(1, 2));
        }
        int periodMode = Integer.parseInt(parts[3].substring(1, 2));
        int period;
        try {
            period = Integer.parseInt(parts[4].substring(1, 3));
        } catch (NumberFormatException e) {
            period = Integer.parseInt(parts[4].substring(1, 2));
        }
        int startTime;
        try {
            startTime = Integer.parseInt(parts[5].substring(1, 3));
        } catch (NumberFormatException e) {
            startTime = Integer.parseInt(parts[5].substring(1, 2));
        }
        int[] rb = new int[2];
        for (int i = 0; i < 2; i++) {
            try {
                rb[i] = Integer.parseInt(parts[6 + i].substring(1, 5));
            } catch (NumberFormatException e) {
                try {
                    rb[i] = Integer.parseInt(parts[6 + i].substring(1, 4));
                } catch (NumberFormatException e2) {
                    try {
                        rb[i] = Integer.parseInt(parts[6 + i].substring(1, 3));
                    } catch (NumberFormatException e3) {
                        rb[i] = Integer.parseInt(parts[6 + i].substring(1, 2));
                    }
                }
            }
        }

        if (numberOfLogBlocks != 2 && numberOfLogBlocks != 3) {
            return MessageResult.createFailed(messageEntry);
        }
        if (periodMode != 0 && periodMode != 1) {
            return MessageResult.createFailed(messageEntry);
        }
        if (scale != -3 && scale != 0) {
            return MessageResult.createFailed(messageEntry);
        }
        if (period < 1 || period > (periodMode == 0 ? 28 : 12)) {
            return MessageResult.createFailed(messageEntry);
        }
        if (startTime < (periodMode == 0 ? 0 : 1) || startTime > (periodMode == 0 ? 23 : 28)) {
            return MessageResult.createFailed(messageEntry);
        }
        for (int threshold : rb) {
            if (threshold > 9999 || threshold < 0) {
                return MessageResult.createFailed(messageEntry);
            }
        }

        waveFlow.getRadioCommandFactory().setRisingBlockTariffs(numberOfLogBlocks, scale, period, periodMode, startTime, rb[0], rb[1]);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult write7BandParameters(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* Write7BandParameters *************************");
        String[] parts = messageEntry.getContent().split("=");
        int[] thresholds = new int[8];
        for (int i = 0; i < 8; i++) {
            try {
                thresholds[i] = Integer.parseInt(parts[1 + i].substring(1, 5));
            } catch (NumberFormatException e) {
                try {
                    thresholds[i] = Integer.parseInt(parts[1 + i].substring(1, 4));
                } catch (NumberFormatException e2) {
                    try {
                        thresholds[i] = Integer.parseInt(parts[1 + i].substring(1, 3));
                    } catch (NumberFormatException e3) {
                        thresholds[i] = Integer.parseInt(parts[1 + i].substring(1, 2));
                    }
                }
            }
        }
        int year;
        try {
            year = Integer.parseInt(parts[9].substring(1, 5));
        } catch (NumberFormatException e) {
            return MessageResult.createFailed(messageEntry);
        }
        int month;
        try {
            month = Integer.parseInt(parts[10].substring(1, 3));
        } catch (NumberFormatException e) {
            month = Integer.parseInt(parts[10].substring(1, 2));
        }
        int day;
        try {
            day = Integer.parseInt(parts[11].substring(1, 3));
        } catch (NumberFormatException e) {
            day = Integer.parseInt(parts[11].substring(1, 2));
        }
        int periodMode = Integer.parseInt(parts[12].substring(1, 2));
        int scale;
        try {
            scale = Integer.parseInt(parts[13].substring(1, 3));
        } catch (NumberFormatException e) {
            scale = Integer.parseInt(parts[13].substring(1, 2));
        }
        int period;
        try {
            period = Integer.parseInt(parts[14].substring(1, 3));
        } catch (NumberFormatException e) {
            period = Integer.parseInt(parts[14].substring(1, 2));
        }

        if (periodMode != 0 && periodMode != 1) {
            return MessageResult.createFailed(messageEntry);
        }
        if (scale != -3 && scale != 0) {
            return MessageResult.createFailed(messageEntry);
        }
        if (period > (periodMode == 0 ? 28 : 52) || period < 1) {
            return MessageResult.createFailed(messageEntry);
        }
        for (int threshold : thresholds) {
            if (threshold > 9999 || threshold < 0) {
                return MessageResult.createFailed(messageEntry);
            }
        }
        if (year < 2000) {
            return MessageResult.createFailed(messageEntry);
        }
        if (month < 1 || month > 12) {
            return MessageResult.createFailed(messageEntry);
        }
        if (day < 1 || day > 28) {
            return MessageResult.createFailed(messageEntry);
        }
        waveFlow.getRadioCommandFactory().write7BandParameters(thresholds, year, month, day, periodMode, scale, period);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult write4DailySegmentsParameters(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* Write4DailySegmentsParameters *************************");
        String[] parts = messageEntry.getContent().split("=");

        int startHour;
        try {
            startHour = Integer.parseInt(parts[1].substring(1, 3));
        } catch (NumberFormatException e) {
            startHour = Integer.parseInt(parts[1].substring(1, 2));
        }
        int startMinute;
        try {
            startMinute = Integer.parseInt(parts[2].substring(1, 3));
        } catch (NumberFormatException e) {
            startMinute = Integer.parseInt(parts[2].substring(1, 2));
        }

        int[] stopHours = new int[4];
        int[] stopMinutes = new int[4];
        int index = 0;
        for (int i = 0; i < 8; i++) {
            try {
                stopHours[index] = Integer.parseInt(parts[3 + i].substring(1, 3));
            } catch (NumberFormatException e) {
                stopHours[index] = Integer.parseInt(parts[3 + i].substring(1, 2));
            }
            i++;
            try {
                stopMinutes[index] = Integer.parseInt(parts[3 + i].substring(1, 3));
            } catch (NumberFormatException e) {
                stopMinutes[index] = Integer.parseInt(parts[3 + i].substring(1, 2));
            }
            index++;
        }
        int periodMode = Integer.parseInt(parts[11].substring(1, 2));
        int period;
        try {
            period = Integer.parseInt(parts[12].substring(1, 3));
        } catch (NumberFormatException e) {
            period = Integer.parseInt(parts[12].substring(1, 2));
        }
        int year;
        try {
            year = Integer.parseInt(parts[13].substring(1, 5));
        } catch (NumberFormatException e) {
            return MessageResult.createFailed(messageEntry);
        }
        int month;
        try {
            month = Integer.parseInt(parts[14].substring(1, 3));
        } catch (NumberFormatException e) {
            month = Integer.parseInt(parts[14].substring(1, 2));
        }
        int day;
        try {
            day = Integer.parseInt(parts[15].substring(1, 3));
        } catch (NumberFormatException e) {
            day = Integer.parseInt(parts[15].substring(1, 2));
        }

        if (startHour < 0 || startHour > 23) {
            return MessageResult.createFailed(messageEntry);
        }
        if (startMinute < 0 || startMinute > 59) {
            return MessageResult.createFailed(messageEntry);
        }
        for (int stopMinute : stopMinutes) {
            if (stopMinute < 0 || stopMinute > 59) {
                return MessageResult.createFailed(messageEntry);
            }
        }
        for (int stopHour : stopHours) {
            if (stopHour < 0 || stopHour > 23) {
                return MessageResult.createFailed(messageEntry);
            }
        }
        if (periodMode != 0 && periodMode != 1) {
            return MessageResult.createFailed(messageEntry);
        }
        if (period > (periodMode == 0 ? 28 : 52) || period < 1) {
            return MessageResult.createFailed(messageEntry);
        }
        if (year < 2000) {
            return MessageResult.createFailed(messageEntry);
        }
        if (month < 1 || month > 12) {
            return MessageResult.createFailed(messageEntry);
        }
        if (day < 1 || day > 28) {
            return MessageResult.createFailed(messageEntry);
        }

        waveFlow.getRadioCommandFactory().write4DailySegmentsParameters(startHour, startMinute, stopHours, stopMinutes, year, month, day, periodMode, period);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult enableTimeOfUseTariffs(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* EnableTimeOfUseTariffs *************************");
        String[] parts = messageEntry.getContent().split("=");
        int periodMode = Integer.parseInt(parts[1].substring(1, 2));
        int duration;
        try {
            duration = Integer.parseInt(parts[2].substring(1, 4));
        } catch (NumberFormatException e) {
            try {
                duration = Integer.parseInt(parts[2].substring(1, 3));
            } catch (NumberFormatException e2) {
                duration = Integer.parseInt(parts[2].substring(1, 2));
            }
        }
        int startHourOrMonth;
        try {
            startHourOrMonth = Integer.parseInt(parts[3].substring(1, 3));
        } catch (NumberFormatException e) {
            startHourOrMonth = Integer.parseInt(parts[3].substring(1, 2));
        }
        int startMinuteOrDay;
        try {
            startMinuteOrDay = Integer.parseInt(parts[4].substring(1, 3));
        } catch (NumberFormatException e) {
            startMinuteOrDay = Integer.parseInt(parts[4].substring(1, 2));
        }

        if (periodMode != 0 && periodMode != 1) {
            return MessageResult.createFailed(messageEntry);
        }
        if (duration < 1 || duration > (periodMode == 0 ? 24 : 255)) {
            return MessageResult.createFailed(messageEntry);
        }
        if (startHourOrMonth < (periodMode == 0 ? 0 : 1) || startHourOrMonth > (periodMode == 0 ? 23 : 12)) {
            return MessageResult.createFailed(messageEntry);
        }
        if (startMinuteOrDay < (periodMode == 0 ? 0 : 1) || startMinuteOrDay > (periodMode == 0 ? 59 : 28)) {
            return MessageResult.createFailed(messageEntry);
        }

        waveFlow.getRadioCommandFactory().setTimeOfUseTariffs(periodMode, duration, startHourOrMonth, startMinuteOrDay);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult enableTimeSlotMechanism(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* EnableTimeSlotMechanism *************************");
        waveFlow.getParameterFactory().enableTimeSlotMechanism();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult writeFeatureData(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* WriteFeatureData *************************");
        int value = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        waveFlow.getRadioCommandFactory().writeFeatureData(value);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult disableTimeSlotMechanism(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* DisableTimeSlotMechanism *************************");
        waveFlow.getParameterFactory().disableTimeSlotMechanism();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult disablePushFrames(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* DisablePushFrames *************************");           //TODO: test!
        waveFlow.getParameterFactory().disablePushFrames();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setBackflowDetectionMethod(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* SetBackflowDetectionMethod *************************");               //TODO: test!
        String[] parts = messageEntry.getContent().split("=");
        int mode = Integer.parseInt(parts[3].substring(1, 2));
        if (mode > 1 || mode < 0) {
            return MessageResult.createFailed(messageEntry);
        }
        waveFlow.getParameterFactory().writeBackflowDetectionMethod(mode);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult writePeakFlowSettings(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* WritePeakFlowSettings *************************");
        String[] parts = messageEntry.getContent().split("=");
        int period;
        try {
            period = Integer.parseInt(parts[1].substring(1, 3));
        } catch (NumberFormatException e) {
            period = Integer.parseInt(parts[1].substring(1, 2));
        }
        int dayOfWeek = Integer.parseInt(parts[2].substring(1, 2));
        int weekOfYear;
        try {
            weekOfYear = Integer.parseInt(parts[3].substring(1, 3));
        } catch (NumberFormatException e) {
            weekOfYear = Integer.parseInt(parts[3].substring(1, 2));
        }
        if (period > 28 || period < 1) {
            return MessageResult.createFailed(messageEntry);
        }
        if (dayOfWeek > 6) {
            return MessageResult.createFailed(messageEntry);
        }
        if (weekOfYear < 1 || weekOfYear > 52) {
            return MessageResult.createFailed(messageEntry);
        }
        waveFlow.getRadioCommandFactory().writePeakFlowSettings(period, dayOfWeek, weekOfYear);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult writeOverSpeedParameters(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* WriteOverSpeedParameters *************************");
        String[] parts = messageEntry.getContent().split("=");

        int threshold;
        try {
            threshold = Integer.parseInt(parts[1].substring(1, 5));
        } catch (NumberFormatException e) {
            try {
                threshold = Integer.parseInt(parts[1].substring(1, 4));
            } catch (NumberFormatException e2) {
                try {
                    threshold = Integer.parseInt(parts[1].substring(1, 3));
                } catch (NumberFormatException e3) {
                    threshold = Integer.parseInt(parts[1].substring(1, 2));
                }
            }
        }

        int time;
        try {
            time = Integer.parseInt(parts[2].substring(1, 5));
        } catch (NumberFormatException e) {
            try {
                time = Integer.parseInt(parts[2].substring(1, 4));
            } catch (NumberFormatException e2) {
                try {
                    time = Integer.parseInt(parts[2].substring(1, 3));
                } catch (NumberFormatException e3) {
                    time = Integer.parseInt(parts[2].substring(1, 2));
                }
            }
        }
        if (threshold > 65535 || threshold < 1) {
            return MessageResult.createFailed(messageEntry);
        }
        if (time > 65535 || time < 1) {
            return MessageResult.createFailed(messageEntry);
        }
        waveFlow.getRadioCommandFactory().writeOverSpeedParameters(threshold, time);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult writeDateOfInstallation(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* WriteDateOfInstallation *************************");
        String[] parts = messageEntry.getContent().split("=");
        int day;
        try {
            day = Integer.parseInt(parts[1].substring(1, 3));
        } catch (NumberFormatException e) {
            day = Integer.parseInt(parts[1].substring(1, 2));
        }
        int month;
        try {
            month = Integer.parseInt(parts[2].substring(1, 3));
        } catch (NumberFormatException e) {
            month = Integer.parseInt(parts[2].substring(1, 2));
        }
        int year;
        try {
            year = Integer.parseInt(parts[3].substring(1, 5));
        } catch (NumberFormatException e) {
            return MessageResult.createFailed(messageEntry);
        }

        if (year < 2000) {
            return MessageResult.createFailed(messageEntry);
        }
        if (month < 1 || month > 12) {
            return MessageResult.createFailed(messageEntry);
        }
        if (day < 1 || day > 31) {
            return MessageResult.createFailed(messageEntry);
        }

        waveFlow.getRadioCommandFactory().writeDateOfInstallation(day, month, year);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult writeCustomerNumber(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* WriteCustomerNumber *************************");
        String number = stripOffTag(messageEntry.getContent());
        if (number.length() != 10) {
            return MessageResult.createFailed(messageEntry);
        }
        waveFlow.getRadioCommandFactory().writeCustomerNumber(number);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setNumberOfInputs(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* SetNumberOfInputs *************************");
        int number = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (number < 1 || number > 4) {
            return MessageResult.createFailed(messageEntry);
        }
        waveFlow.getParameterFactory().setNumberOfInputsUsed(number);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult sendAllAlarms(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* SendAllAlarms *************************");
        waveFlow.getParameterFactory().writeAlarmConfigurationByte(0xFF);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult disableAllAlarms(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* DisableAllAlarms *************************");
        waveFlow.getParameterFactory().writeAlarmConfigurationByte(0x00);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult sendAlarmOnThresholdDetectionOfCreditAmount(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* SendAlarmOnThresholdDetectionOfCreditAmount *************************");
        waveFlow.getParameterFactory().sendAlarmOnThresholdDetectionOfCreditAmount();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult sendAlarmOnValveCloseFault(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* SendAlarmOnValveCloseFault *************************");
        waveFlow.getParameterFactory().sendAlarmOnValveCloseFault();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult sendAlarmOnHighLeakDetection(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* SendAlarmOnHighLeakDetection *************************");
        waveFlow.getParameterFactory().sendAlarmOnHighLeakDetection();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult sendAlarmOnBackflowDetection(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* SendAlarmOnBackflowDetection *************************");
        waveFlow.getParameterFactory().sendAlarmOnBackflowDetection();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult sendAlarmOnValveWirecut(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* SendAlarmOnValveWirecut *************************");
        waveFlow.getParameterFactory().sendAlarmOnValveWirecut();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult sendAlarmOnLowLeakDetection(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* SendAlarmOnLowLeakDetection *************************");
        waveFlow.getParameterFactory().sendAlarmOnLowLeakDetection();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult sendAlarmOnBatteryEnd(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* SendAlarmOnBatteryEnd *************************");
        waveFlow.getParameterFactory().sendAlarmOnBatteryEnd();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult sendAlarmOnWirecutDetection(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* SendAlarmOnWirecutDetection *************************");
        waveFlow.getParameterFactory().sendAlarmOnWirecutDetection();
        return MessageResult.createSuccess(messageEntry);
    }


    private MessageResult disableAlarmOnThresholdDetectionOfCreditAmount(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* DisableAlarmOnThresholdDetectionOfCreditAmount *************************");
        waveFlow.getParameterFactory().disableAlarmOnThresholdDetectionOfCreditAmount();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult disableAlarmOnValveCloseFault(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* DisableAlarmOnValveCloseFault *************************");
        waveFlow.getParameterFactory().disableAlarmOnValveCloseFault();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult disableAlarmOnHighLeakDetection(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* DisableAlarmOnHighLeakDetection *************************");
        waveFlow.getParameterFactory().disableAlarmOnHighLeakDetection();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult disableAlarmOnBackflowDetection(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* DisableAlarmOnBackflowDetection *************************");
        waveFlow.getParameterFactory().disableAlarmOnBackflowDetection();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult disableAlarmOnValveWirecut(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* DisableAlarmOnValveWirecut *************************");
        waveFlow.getParameterFactory().disableAlarmOnValveWirecut();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult disableAlarmOnLowLeakDetection(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* DisableAlarmOnLowLeakDetection *************************");
        waveFlow.getParameterFactory().disableAlarmOnLowLeakDetection();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult disableAlarmOnBatteryEnd(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* DisableAlarmOnBatteryEnd *************************");
        waveFlow.getParameterFactory().disableAlarmOnBatteryEnd();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult disableAlarmOnWirecutDetection(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* DisableAlarmOnWirecutDetection *************************");
        waveFlow.getParameterFactory().disableAlarmOnWirecutDetection();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setRepeaterAddress(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* SetRepeaterAddress *************************");
        String[] parts = messageEntry.getContent().split("=");
        int number = Integer.parseInt(parts[1].substring(1, 2));
        if (number > 3 || number < 0) {
            return MessageResult.createFailed(messageEntry);
        }
        String address = parts[2].substring(1, 13);
        waveFlow.getParameterFactory().writeRepeaterAddress(address, number);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setRecipientAddress(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* SetRecipientAddress *************************");
        String[] parts = messageEntry.getContent().split("=");
        String address = parts[1].substring(1, 13);
        waveFlow.getParameterFactory().writeRecipientAddress(address);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setNumberOfRepeaters(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* SetNumberOfRepeaters *************************");
        int value = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (value > 3 || value < 0) {
            return MessageResult.createFailed(messageEntry);
        }
        waveFlow.getParameterFactory().writeNumberOfRepeaters(value);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setAlarmConfig(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* SetAlarmConfig *************************");
        int value = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (value > 0xFF || value < 0) {
            return MessageResult.createFailed(messageEntry);
        }
        waveFlow.getParameterFactory().writeAlarmConfigurationByte(value);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult clearCommandBuffer(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* ClearCommandBuffer *************************");
        waveFlow.getParameterFactory().clearCommandBuffer();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult addCommandToBuffer(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* AddCommandToBuffer *************************");
        String[] parts = messageEntry.getContent().split("=");
        int value;
        try {
            value = Integer.parseInt(parts[2].substring(1, 3));
        } catch (NumberFormatException e) {
            value = Integer.parseInt(parts[2].substring(1, 2));
        }
        if (value > 0xFF || value < 0x01) {
            return MessageResult.createFailed(messageEntry);
        }
        waveFlow.getParameterFactory().addCommandToBuffer(value);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setMaxCancelTimeout(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* SetMaxCancelTimeout *************************");
        int value = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (value > 10 || value < 1) {
            return MessageResult.createFailed(messageEntry);
        }
        waveFlow.getParameterFactory().writeMaxCancelTimeout(value);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setTransmissionPeriod(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* SetTransmissionPeriod *************************");
        int minutes = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (minutes > MAX_TRANSMISSION_PERIOD || minutes < 1) {
            return MessageResult.createFailed(messageEntry);
        }
        waveFlow.getParameterFactory().writeTransmissionPeriod(minutes);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setStartOfMechanism(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* SetStartOfMechanism *************************");
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
            return MessageResult.createFailed(messageEntry);
        }
        waveFlow.getParameterFactory().writeStartOfPushFrameMechanism(hour, minute, second);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult enablePushFrames(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* EnablePushFrames *************************");
        waveFlow.getParameterFactory().enablePushFrames();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult startPushFrames(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* StartPushFrames *************************");
        String[] parts = messageEntry.getContent().split("=");
        int value;
        try {
            value = Integer.parseInt(parts[2].substring(1, 3));
        } catch (NumberFormatException e) {
            value = Integer.parseInt(parts[2].substring(1, 2));
        }
        if (value > 0xFF || value < 0x01) {
            return MessageResult.createFailed(messageEntry);
        }
        int transmissionPeriod = Integer.parseInt(parts[3].substring(1).split("\"")[0]);
        int transmissionPeriodUnit = Integer.parseInt(parts[4].substring(1).split("\"")[0]);
        if (transmissionPeriod < 1 || transmissionPeriod > 63) {
            return MessageResult.createFailed(messageEntry);
        }
        if (transmissionPeriodUnit < 0 || transmissionPeriodUnit > 2) {
            return MessageResult.createFailed(messageEntry);
        }
        transmissionPeriod = (transmissionPeriod << 2) | transmissionPeriodUnit;
        waveFlow.getParameterFactory().writeBubbleUpConfiguration(value, transmissionPeriod);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setBackflowThreshold(MessageEntry messageEntry, boolean simple) throws IOException {
        waveFlow.getLogger().info("************************* SetBackflowThreshold *************************");
        int threshold = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        String[] parts = messageEntry.getContent().split("=");
        int inputChannel = Integer.parseInt(parts[3].substring(1, 2));
        if ((threshold > 0xFF) || (threshold < 0)) {
            return MessageResult.createFailed(messageEntry);
        }
        if (inputChannel > 0 && inputChannel < 3) {
            if (simple) {
                waveFlow.getParameterFactory().writeSimpleBackflowThreshold(threshold, inputChannel);
            } else {
                waveFlow.getParameterFactory().writeAdvancedBackflowThreshold(threshold, inputChannel);
            }
            return MessageResult.createSuccess(messageEntry);
        } else {
            return MessageResult.createFailed(messageEntry);
        }
    }

    private MessageResult setBackflowDetectionPeriod(MessageEntry messageEntry, boolean simple) throws IOException {
        waveFlow.getLogger().info("************************* SetBackflowDetectionPeriod *************************");
        int period = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        String[] parts = messageEntry.getContent().split("=");
        int inputChannel = Integer.parseInt(parts[3].substring(1, 2));
        if ((period > 0xFF) || (period < 1)) {
            return MessageResult.createFailed(messageEntry);
        }
        if (inputChannel > 0 && inputChannel < 3) {
            if (simple) {
                waveFlow.getParameterFactory().writeSimpleBackflowDetectionPeriod(period, inputChannel);
            } else {
                waveFlow.getParameterFactory().writeAdvancedBackflowDetectionPeriod(period, inputChannel);
            }
            return MessageResult.createSuccess(messageEntry);
        } else {
            return MessageResult.createFailed(messageEntry);
        }
    }

    private MessageResult setProfileInterval(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* Set sampling interval *************************");
        int profileInterval = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (profileInterval < 60 || profileInterval > MAX_SAMPLING_INTERVAL_SECONDS) {
            waveFlow.getLogger().severe("Invalid profile interval, must have minimum 60 seconds");
            return MessageResult.createFailed(messageEntry);
        } else {
            waveFlow.getParameterFactory().writeSamplingPeriod(profileInterval);
            return MessageResult.createSuccess(messageEntry);
        }
    }

    private MessageResult setLeakageDetectionPeriod(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* SetLeakageDetectionPeriod *************************");
        int period = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        String[] parts = messageEntry.getContent().split("=");
        int residualOrExtreme = Integer.parseInt(parts[1].substring(1, 2));
        int inputChannel = Integer.parseInt(parts[4].substring(1, 2));
        if ((period > 0xFF) || (period < 1)) {
            return MessageResult.createFailed(messageEntry);
        }
        if ((residualOrExtreme == 0 || residualOrExtreme == 1) && (inputChannel > 0 && inputChannel < 5)) {
            waveFlow.getParameterFactory().setLeakageDetectionPeriod(residualOrExtreme, inputChannel, period);
            return MessageResult.createSuccess(messageEntry);
        } else {
            return MessageResult.createFailed(messageEntry);
        }
    }

    private MessageResult setLeakageThreshold(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* SetLeakageThreshold *************************");
        int threshold = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        String[] parts = messageEntry.getContent().split("=");
        int residualOrExtreme = Integer.parseInt(parts[1].substring(1, 2));
        int inputChannel = Integer.parseInt(parts[4].substring(1, 2));

        //Check the range of the given threshold.
        if ((threshold > 0xFFFF) || (threshold < 0)) {
            return MessageResult.createFailed(messageEntry);
        }
        if ((residualOrExtreme == 0) && (threshold > 0xFF)) {
            return MessageResult.createFailed(messageEntry);
        }
        if ((residualOrExtreme == 0 || residualOrExtreme == 1) && (inputChannel > 0 && inputChannel < 5)) {
            waveFlow.getParameterFactory().setLeakageThreshold(residualOrExtreme, inputChannel, threshold);
            return MessageResult.createSuccess(messageEntry);
        } else {
            return MessageResult.createFailed(messageEntry);
        }
    }

    private MessageResult addCreditBeforeClosing(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* Add credit before closing *************************");
        int quantity = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        String[] parts = messageEntry.getContent().split("=");
        int addOrReplace = Integer.parseInt(parts[1].substring(1, 2));
        int closeOrLimit = Integer.parseInt(parts[2].substring(1, 2));
        if ((addOrReplace == 0 || addOrReplace == 1) && (closeOrLimit == 0 || closeOrLimit == 1) && (quantity > 0)) {    //Check if a valid integer value was given... should be 0 or 1
            boolean success = waveFlow.getRadioCommandFactory().addCreditBeforeClosing(quantity, addOrReplace, closeOrLimit);
            if (success) {
                return MessageResult.createSuccess(messageEntry);
            } else {
                return MessageResult.createFailed(messageEntry);
            }
        } else {
            return MessageResult.createFailed(messageEntry);
        }
    }

    private MessageResult cleanWaterValve(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* Clean water valve *************************");
        boolean success = waveFlow.getRadioCommandFactory().cleanWaterValve();
        if (success) {
            return MessageResult.createSuccess(messageEntry);
        } else {
            return MessageResult.createFailed(messageEntry);
        }
    }

    private MessageResult closeWaterValve(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* Close water valve *************************");
        boolean success = waveFlow.getRadioCommandFactory().closeWaterValve();
        if (success) {
            return MessageResult.createSuccess(messageEntry);
        } else {
            return MessageResult.createFailed(messageEntry);
        }
    }

    private MessageResult openWaterValve(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* Open water valve *************************");
        boolean success = waveFlow.getRadioCommandFactory().openWaterValve();
        if (success) {
            return MessageResult.createSuccess(messageEntry);
        } else {
            return MessageResult.createFailed(messageEntry);
        }
    }

    private MessageResult setMinuteOfmeasurement(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* SetMinuteOfMeasurement *************************");
        int minute = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (minute < 0 || minute > 59) {
            return MessageResult.createFailed(messageEntry);
        }
        waveFlow.getParameterFactory().writeStartMinuteOfMeasurement(minute);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setHourOfMeasurement(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* SetHourOfMeasurement *************************");

        String[] parts = messageEntry.getContent().split("=");
        int time = Integer.parseInt(parts[1].substring(1).split("\"")[0]);
        int mode = Integer.parseInt(parts[2].substring(1).split("\"")[0]);

        if (time < 0 || time > 23) {
            return MessageResult.createFailed(messageEntry);
        }
        if (mode < 1 || mode > 3) {
            return MessageResult.createFailed(messageEntry);
        }
        waveFlow.getParameterFactory().writeStartHourOfMeasurement(time, mode);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult definePulseWeight(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* DefinePulseWeight *************************");
        int weight = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        String[] parts = messageEntry.getContent().split("=");
        int inputChannelIndex = Integer.parseInt(parts[1].substring(1, 2));
        int unitNumber = Integer.parseInt(parts[5].substring(1, 2));
        if (unitNumber < 0 || unitNumber > 6) {
            return MessageResult.createFailed(messageEntry);
        }
        boolean success = waveFlow.getParameterFactory().writePulseWeight(inputChannelIndex, unitNumber - 3, weight);
        if (success) {
            return MessageResult.createSuccess(messageEntry);
        } else {
            return MessageResult.createFailed(messageEntry);
        }
    }

    private MessageResult setWeeklyLogging(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* SetWeeklyLogging *************************");
        waveFlow.getParameterFactory().writeWeeklyDataLogging();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setMonthlyLogging(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* SetMonthlyLogging *************************");
        waveFlow.getParameterFactory().writeMonthlyDataLogging();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setPeriodicStepLogging(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* SetPeriodicStepLogging *************************");
        waveFlow.getParameterFactory().writePeriodicTimeStepDataLogging();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setHourForDailyIndexStorage(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* SetHourOfDailyIndexStorage *************************");
        int hour = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (hour < 0 || hour > 23) {
            return MessageResult.createFailed(messageEntry);
        }
        waveFlow.getParameterFactory().setHourOfDailyIndexStorage(hour);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setDayOfWeekOrMonth(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* SetDayOfWeek *************************");
        int dayOfWeek = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        boolean monthly = waveFlow.getParameterFactory().readOperatingMode().isMonthlyMeasurement();
        if (monthly && (dayOfWeek > 28 || dayOfWeek < 1)) {
            return MessageResult.createFailed(messageEntry);
        } else if (!monthly && (dayOfWeek < 0 || dayOfWeek > 6)) {
            return MessageResult.createFailed(messageEntry);
        }
        waveFlow.getParameterFactory().writeDayOfWeek(dayOfWeek);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult resetApplicationStatus(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* ResetApplicationStatus *************************");
        waveFlow.getParameterFactory().writeApplicationStatus(0);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult resetApplicationStatusBit(MessageEntry messageEntry, int bit) throws IOException {
        waveFlow.getLogger().info("************************* ResetApplicationStatusBit *************************");
        waveFlow.getParameterFactory().writeApplicationStatusBit(bit);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult resetValveApplicationStatusBit(MessageEntry messageEntry, int bit) throws IOException {
        waveFlow.getLogger().info("************************* resetValveApplicationStatusBit *************************");
        waveFlow.getParameterFactory().writeValveApplicationStatusBit(bit);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult resetValveApplicationStatus(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* ResetValveApplicationStatus *************************");
        if (waveFlow.getParameterFactory().readProfileType().supportsWaterValveControl()) {
            waveFlow.getParameterFactory().writeValveApplicationStatus(0);
            return MessageResult.createSuccess(messageEntry);
        }
        return MessageResult.createFailed(messageEntry);
    }

    private MessageResult setOperationMode(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* SetOperatingMode *************************");
        int operationMode = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (operationMode < 0x00 || operationMode > 0xFF) {
            return MessageResult.createFailed(messageEntry);
        }
        waveFlow.getParameterFactory().writeOperatingMode(operationMode);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setLeakageStatusReadingHour(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* SetLeakageStatusReadingHour *************************");
        int hour = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (hour < 0 || hour > 23) {
            waveFlow.getLogger().warning("Invalid reading hour, should be between 0 and 23");
            return MessageResult.createFailed(messageEntry);
        }
        ((ParameterFactoryHydreka) waveFlow.getParameterFactory()).writeReadingHourLeakageStatus(hour);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setHistogramReadingHour(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* SetHistogramReadingHour *************************");
        int hour = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (hour < 0 || hour > 23) {
            waveFlow.getLogger().warning("Invalid reading hour, should be between 0 and 23");
            return MessageResult.createFailed(messageEntry);
        }
        ((ParameterFactoryHydreka) waveFlow.getParameterFactory()).writeReadingHourHistogram(hour);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setRTCResyncPeriod(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* SetRTCResyncPeriod *************************");
        int period = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (period < 0 || period > 0xFF) {
            waveFlow.getLogger().warning("Invalid Period of the RTC resynchronization, should be between 0 and 255");
            return MessageResult.createFailed(messageEntry);
        }
        ((ParameterFactoryHydreka) waveFlow.getParameterFactory()).writeRTCResynchPeriod(period);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setOperationModeWithMask(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* SetOperatingModeWithMask *************************");

        String[] parts = messageEntry.getContent().split("=");
        int operationMode = Integer.parseInt(parts[1].substring(1).split("\"")[0]);
        int mask = Integer.parseInt(parts[2].substring(1).split("\"")[0]);

        if (operationMode < 0x00 || operationMode > 0xFF) {
            return MessageResult.createFailed(messageEntry);
        }
        if (mask < 0x00 || mask > 0xFF) {
            return MessageResult.createFailed(messageEntry);
        }

        waveFlow.getParameterFactory().writeWorkingMode(operationMode, mask);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult forceTimeSync(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* ForceTimeSync *************************");
        waveFlow.forceSetTime();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult restartDataLogging(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* RestartDataLogging *************************");
        int mode = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (mode < 1 || mode > 3) {
            return MessageResult.createFailed(messageEntry);
        }
        waveFlow.getParameterFactory().restartDataLogging(mode);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setMeasurementStep(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* SetMeasurementStep *************************");
        int step = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if ((step <= 0xFF) && (step > 0)) {
            waveFlow.getParameterFactory().writeMeasurementStep(step);
            return MessageResult.createSuccess(messageEntry);
        } else {
            return MessageResult.createFailed(messageEntry);
        }
    }
}