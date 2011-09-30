package com.energyict.protocolimpl.coronis.waveflow.core.messages;

import com.energyict.protocol.*;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;

import java.io.IOException;
import java.util.List;

/**
 * Class that parses and executes the messages for the WaveFlow V1, V2 and V210 protocol.
 * Only the implementation of getMessageCategories() is different for each protocol.
 * <p/>
 * Copyrights EnergyICT
 * Date: 18-mei-2011
 * Time: 15:38:34
 */
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

    private static final int MAX_SAMPLING_INTERVAL_SECONDS = 63 * 30 * 60;  //See documentation, largest interval possible is 31,5 hours.
    private static final int MAX_TRANSMISSION_PERIOD = 60 * 24 * 63;        //number of minutes in 63 days

    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        try {
            if (messageEntry.getContent().indexOf("<RestartDataLogging") >= 0) {
                return restartDataLogging(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SimpleRestartDataLogging") >= 0) {
                return simpleRestartDataLogging(messageEntry);
            } else if (messageEntry.getContent().indexOf("<StopDataLogging") >= 0) {
                return stopDataLogging(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SetNumberOfInputs") >= 0) {
                return setNumberOfInputs(messageEntry);
            } else if (messageEntry.getContent().indexOf("<ResetIndexes") >= 0) {
                return resetIndexes(messageEntry);
            } else if (messageEntry.getContent().indexOf("<WriteIndexA") >= 0) {
                return writeIndex(messageEntry, 1);
            } else if (messageEntry.getContent().indexOf("<InitializeRoute") >= 0) {
                return initializeRoute(messageEntry);
            } else if (messageEntry.getContent().indexOf("<WriteIndexB") >= 0) {
                return writeIndex(messageEntry, 2);
            } else if (messageEntry.getContent().indexOf("<WriteIndexC") >= 0) {
                return writeIndex(messageEntry, 3);
            } else if (messageEntry.getContent().indexOf("<WriteIndexD") >= 0) {
                return writeIndex(messageEntry, 4);
            } else if (messageEntry.getContent().indexOf("<ForceTimeSync") >= 0) {
                return forceTimeSync(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SetBackflowDetectionMethod") >= 0) {
                return setBackflowDetectionMethod(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SetOperatingMode") >= 0) {
                return setOperationMode(messageEntry);
            } else if (messageEntry.getContent().indexOf("<ResetApplicationStatus") >= 0) {
                return resetApplicationStatus(messageEntry);
            } else if (messageEntry.getContent().indexOf("<ResetValveApplicationStatus") >= 0) {
                return resetValveApplicationStatus(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SetDayOfWeek") >= 0) {
                return setDayOfWeekOrMonth(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SetHourOfDailyIndexStorage") >= 0) {
                return setHourForDailyIndexStorage(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SetPeriodicStepLogging") >= 0) {
                return setPeriodicStepLogging(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SetMonthlyLogging") >= 0) {
                return setMonthlyLogging(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SetWeeklyLogging") >= 0) {
                return setWeeklyLogging(messageEntry);
            } else if (messageEntry.getContent().indexOf("<DefinePulseWeight") >= 0) {
                return definePulseWeight(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SetHourOfMeasurement") >= 0) {
                return setHourOfMeasurement(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SetMinuteOfMeasurement") >= 0) {
                return setMinuteOfmeasurement(messageEntry);
            } else if (messageEntry.getContent().indexOf("<OpenWaterValve") >= 0) {
                return openWaterValve(messageEntry);
            } else if (messageEntry.getContent().indexOf("<CloseWaterValve") >= 0) {
                return closeWaterValve(messageEntry);
            } else if (messageEntry.getContent().indexOf("<CleanWaterValve") >= 0) {
                return cleanWaterValve(messageEntry);
            } else if (messageEntry.getContent().indexOf("<AddCreditBeforeClosing") >= 0) {
                return addCreditBeforeClosing(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SetLeakageThreshold") >= 0) {
                return setLeakageThreshold(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SetLeakageDetectionPeriod") >= 0) {
                return setLeakageDetectionPeriod(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SetMeasurementStep") >= 0) {
                return setMeasurementStep(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SetProfileInterval") >= 0) {
                return setProfileInterval(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SetSimpleBackflowThreshold") >= 0) {
                return setBackflowThreshold(messageEntry, true);
            } else if (messageEntry.getContent().indexOf("<SetAdvancedBackflowThreshold") >= 0) {
                return setBackflowThreshold(messageEntry, false);
            } else if (messageEntry.getContent().indexOf("<SetSimpleBackflowDetectionPeriod") >= 0) {
                return setBackflowDetectionPeriod(messageEntry, true);
            } else if (messageEntry.getContent().indexOf("<SetAdvancedBackflowDetectionPeriod") >= 0) {
                return setBackflowDetectionPeriod(messageEntry, false);
            } else if (messageEntry.getContent().indexOf("<EnablePushFrames") >= 0) {
                return enablePushFrames(messageEntry);
            } else if (messageEntry.getContent().indexOf("<StartPushFrames") >= 0) {
                return startPushFrames(messageEntry);
            } else if (messageEntry.getContent().indexOf("<DisablePushFrames") >= 0) {
                return disablePushFrames(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SetStartOfMechanism") >= 0) {
                return setStartOfMechanism(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SetTransmissionPeriod") >= 0) {
                return setTransmissionPeriod(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SetMaxCancelTimeout") >= 0) {
                return setMaxCancelTimeout(messageEntry);
            } else if (messageEntry.getContent().indexOf("<AddCommandToBuffer") >= 0) {
                return addCommandToBuffer(messageEntry);
            } else if (messageEntry.getContent().indexOf("<ClearCommandBuffer") >= 0) {
                return clearCommandBuffer(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SetAlarmConfig") >= 0) {
                return setAlarmConfig(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SetNumberOfRepeaters") >= 0) {
                return setNumberOfRepeaters(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SetRepeaterAddress") >= 0) {
                return setRepeaterAddress(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SetRecipientAddress") >= 0) {
                return setRecipientAddress(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SendAlarmOnWirecutDetection") >= 0) {
                return sendAlarmOnWirecutDetection(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SendAlarmOnBatteryEnd") >= 0) {
                return sendAlarmOnBatteryEnd(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SendAlarmOnLowLeakDetection") >= 0) {
                return sendAlarmOnLowLeakDetection(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SendAlarmOnHighLeakDetection") >= 0) {
                return sendAlarmOnHighLeakDetection(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SendAlarmOnBackflowDetection") >= 0) {
                return sendAlarmOnBackflowDetection(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SendAlarmOnValveWirecut") >= 0) {
                return sendAlarmOnValveWirecut(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SendAlarmOnValveCloseFault") >= 0) {
                return sendAlarmOnValveCloseFault(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SendAlarmOnThresholdDetectionOfCreditAmount") >= 0) {
                return sendAlarmOnThresholdDetectionOfCreditAmount(messageEntry);
            } else if (messageEntry.getContent().indexOf("<DisableAlarmOnWirecutDetection") >= 0) {
                return disableAlarmOnWirecutDetection(messageEntry);
            } else if (messageEntry.getContent().indexOf("<DisableAlarmOnBatteryEnd") >= 0) {
                return disableAlarmOnBatteryEnd(messageEntry);
            } else if (messageEntry.getContent().indexOf("<DisableAlarmOnLowLeakDetection") >= 0) {
                return disableAlarmOnLowLeakDetection(messageEntry);
            } else if (messageEntry.getContent().indexOf("<DisableAlarmOnHighLeakDetection") >= 0) {
                return disableAlarmOnHighLeakDetection(messageEntry);
            } else if (messageEntry.getContent().indexOf("<DisableAlarmOnBackflowDetection") >= 0) {
                return disableAlarmOnBackflowDetection(messageEntry);
            } else if (messageEntry.getContent().indexOf("<DisableAlarmOnValveWirecut") >= 0) {
                return disableAlarmOnValveWirecut(messageEntry);
            } else if (messageEntry.getContent().indexOf("<DisableAlarmOnValveCloseFault") >= 0) {
                return disableAlarmOnValveCloseFault(messageEntry);
            } else if (messageEntry.getContent().indexOf("<DisableAlarmOnThresholdDetectionOfCreditAmount") >= 0) {
                return disableAlarmOnThresholdDetectionOfCreditAmount(messageEntry);
            } else if (messageEntry.getContent().indexOf("<DisableAllAlarms") >= 0) {
                return disableAllAlarms(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SendAllAlarms") >= 0) {
                return sendAllAlarms(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SetTimeSlotGranularity") >= 0) {
                return setTimeSlotGranularity(messageEntry);
            } else if (messageEntry.getContent().indexOf("<SetTimeSlotDuration") >= 0) {
                return setTimeSlotDuration(messageEntry);
            } else if (messageEntry.getContent().indexOf("<EnableTimeSlotMechanism") >= 0) {
                return enableTimeSlotMechanism(messageEntry);
            } else if (messageEntry.getContent().indexOf("<DisableTimeSlotMechanism") >= 0) {
                return disableTimeSlotMechanism(messageEntry);
            } else if (messageEntry.getContent().indexOf("<WriteFeatureData") >= 0) {
                return writeFeatureData(messageEntry);
            } else if (messageEntry.getContent().indexOf("<WritePeakFlowSettings") >= 0) {
                return writePeakFlowSettings(messageEntry);
            } else if (messageEntry.getContent().indexOf("<WriteOverSpeedParameters") >= 0) {
                return writeOverSpeedParameters(messageEntry);
            } else if (messageEntry.getContent().indexOf("<Write7BandParameters") >= 0) {
                return write7BandParameters(messageEntry);
            } else if (messageEntry.getContent().indexOf("<Write4DailySegmentsParameters") >= 0) {
                return write4DailySegmentsParameters(messageEntry);
            } else if (messageEntry.getContent().indexOf("<WriteDateOfInstallation") >= 0) {
                return writeDateOfInstallation(messageEntry);
            } else if (messageEntry.getContent().indexOf("<WriteCustomerNumber") >= 0) {
                return writeCustomerNumber(messageEntry);
            } else if (messageEntry.getContent().indexOf("<EnableRisingBlockTariffs") >= 0) {
                return enableRisingBlockTariffs(messageEntry);
            } else if (messageEntry.getContent().indexOf("<EnableTimeOfUseTariffs") >= 0) {
                return enableTimeOfUseTariffs(messageEntry);
            } else {
                waveFlow.getLogger().severe("Unknown message, cannot execute");
                return MessageResult.createFailed(messageEntry);
            }
        } catch (NumberFormatException e) {
            waveFlow.getLogger().severe("Error parsing message, " + e.getMessage());
            return MessageResult.createFailed(messageEntry);
        }
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
        int alarmMode = Integer.parseInt(stripOffTag(messageEntry.getContent()));
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
        waveFlow.getParameterFactory().sendAllAlarms();
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult disableAllAlarms(MessageEntry messageEntry) throws IOException {
        waveFlow.getLogger().info("************************* DisableAllAlarms *************************");
        waveFlow.getParameterFactory().disableAllAlarms();
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
        waveFlow.getParameterFactory().writeBubbleUpConfiguration(value);
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
        int time = Integer.parseInt(stripOffTag(messageEntry.getContent()));
        if (time < 0 || time > 23) {
            return MessageResult.createFailed(messageEntry);
        }
        waveFlow.getParameterFactory().writeStartHourOfMeasurement(time);
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