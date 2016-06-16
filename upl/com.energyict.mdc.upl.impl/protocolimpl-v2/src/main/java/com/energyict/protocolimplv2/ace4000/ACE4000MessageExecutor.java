package com.energyict.protocolimplv2.ace4000;

import com.energyict.cbo.ApplicationException;
import com.energyict.cbo.BusinessException;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.messages.DeviceMessageStatus;
import com.energyict.mdc.meterdata.*;
import com.energyict.mdc.protocol.tasks.support.DeviceLoadProfileSupport;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineLoadProfile;
import com.energyict.mdw.offline.OfflineLogBook;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.ace4000.requests.*;
import com.energyict.protocolimplv2.common.objectserialization.codetable.CodeObjectValidator;
import com.energyict.protocolimplv2.common.objectserialization.codetable.CodeTableBase64Parser;
import com.energyict.protocolimplv2.common.objectserialization.codetable.objects.CodeObject;
import com.energyict.protocolimplv2.identifiers.DeviceMessageIdentifierById;
import com.energyict.protocolimplv2.identifiers.LogBookIdentifierById;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ACE4000MessageExecutor {

    private ACE4000Outbound ace4000;

    public ACE4000MessageExecutor(ACE4000Outbound ace4000) {
        this.ace4000 = ace4000;
    }

    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList result = MdcManager.getCollectedDataFactory().createCollectedMessageList(pendingMessages);
        for (OfflineDeviceMessage pendingMessage : pendingMessages) {
            CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);   //Optimistic
            try {
                if (pendingMessage.getSpecification().equals(RtuMessageConstant.READ_EVENTS)) {
                    collectedMessage = readEvents(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(RtuMessageConstant.READ_PROFILE_DATA)) {
                    collectedMessage = readProfileData(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(FirmwareDeviceMessage.FirmwareUpgradeWithUrlJarJadFileSize)) {
                    FirmwareUpgrade firmwareUpgrade = new FirmwareUpgrade(ace4000);
                    collectedMessage = firmwareUpgrade.request(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(ContactorDeviceMessage.CONTACTOR_CLOSE)) {
                    ContactorCommand contactorCommand = new ContactorCommand(ace4000);
                    contactorCommand.setCommand(0);
                    collectedMessage = contactorCommand.request(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE)) {
                    ContactorCommand contactorCommand = new ContactorCommand(ace4000);
                    contactorCommand.setCommand(0);
                    contactorCommand.useActivationDate(true);
                    collectedMessage = contactorCommand.request(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(ContactorDeviceMessage.CONTACTOR_OPEN)) {
                    ContactorCommand contactorCommand = new ContactorCommand(ace4000);
                    contactorCommand.setCommand(1);
                    collectedMessage = contactorCommand.request(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE)) {
                    ContactorCommand contactorCommand = new ContactorCommand(ace4000);
                    contactorCommand.setCommand(1);
                    contactorCommand.useActivationDate(true);
                    collectedMessage = contactorCommand.request(pendingMessage);
                } else {
                    WriteConfiguration writeConfiguration = new WriteConfiguration(ace4000);
                    collectedMessage = writeConfiguration.request(pendingMessage);
                }

            } catch (Exception e) {
                log(Level.INFO, "Message has failed. " + e.getMessage());
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
                collectedMessage.setDeviceProtocolInformation(e.getMessage());
            }

            result.addCollectedMessage(collectedMessage);
        }

        return result;
    }

    public Integer sendConfigurationMessage(OfflineDeviceMessage pendingMessage) {

        if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.SendShortDisplayMessage)){
            sendDisplayMessage(pendingMessage, 1);
        } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.SendLongDisplayMessage)) {
            sendDisplayMessage(pendingMessage, 2);
        } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.DisplayMessage)) {
            sendDisplayMessage(pendingMessage, 0);
        } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.ConfigureLCDDisplay)) {
            sendDisplayConfigRequest(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.ConfigureLoadProfileDataRecording)) {
            sendLoadProfileConfigurationRequest(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.ConfigureSpecialDataMode)) {
            sendSDMConfigurationRequest(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.ConfigureMaxDemandSettings)) {
            sendMaxDemandConfigurationRequest(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.ConfigureConsumptionLimitationsSettings)) {
            sendConsumptionLimitationConfiguration(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.ConfigureEmergencyConsumptionLimitation)) {
            sendEmergencyConsumptionLimitationConfiguration(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.ConfigureTariffSettings)) {
            sendTariffConfiguration(pendingMessage);
        } else {
            return null;         //Unknown message, handle properly later on
        }
        return ace4000.getObjectFactory().getTrackingID();
    }

    private void sendLoadProfileConfigurationRequest(OfflineDeviceMessage pendingMessage) {
        int enable = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.ENABLE_DISABLE).getDeviceMessageAttributeValue());
        int intervalInMinutes = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.CONFIG_LOAD_PROFILE_INTERVAL).getDeviceMessageAttributeValue());
        int maxNumberOfRecords = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.MAX_NUMBER_RECORDS).getDeviceMessageAttributeValue());
        if (enable != 0 && enable != 1) {
            createMessageFailedIssue(pendingMessage, "Load profile configuration message failed, invalid arguments");
            return;
        }
        if (intervalInMinutes != 1 && intervalInMinutes != 2 && intervalInMinutes != 3 && intervalInMinutes != 5 && intervalInMinutes != 6 && intervalInMinutes != 0x0A && intervalInMinutes != 0x0C && intervalInMinutes != 0x0F && intervalInMinutes != 0x14 && intervalInMinutes != 0x1E && intervalInMinutes != 0x3C && intervalInMinutes != 0x78 && intervalInMinutes != 0xF0) {
            createMessageFailedIssue(pendingMessage, "Load profile configuration message failed, invalid arguments");
            return;
        }
        ace4000.getObjectFactory().sendLoadProfileConfiguration(enable, intervalInMinutes, maxNumberOfRecords);
    }

    private CollectedMessage readProfileData(OfflineDeviceMessage pendingMessage){

        String fromDateString = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.fromDateAttributeName).getDeviceMessageAttributeValue();
        String toDateString = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.toDateAttributeName).getDeviceMessageAttributeValue();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date fromDate = null;
        Date toDate = null;

        try {
            fromDate = formatter.parse(fromDateString);
        } catch (Exception e) {
            createMessageFailedIssue(pendingMessage, "Request for load profile data failed, invalid arguments");
            return null;
        }

        try {
            toDate = formatter.parse(toDateString);
        } catch (Exception e) {
            toDate = new Date();
        }

        if (toDate == null) {
            toDate = new Date();
        }
        if (toDate.after(new Date())) {
            toDate = new Date();
        }
        if (fromDate != null && !fromDate.before(toDate)) {
            createMessageFailedIssue(pendingMessage, "Request for load profile data failed, invalid arguments");
            return null;
        }

        //Make a new loadProfileReader with the proper from and to date
        LoadProfileReader loadProfileReader = null;
        for (OfflineLoadProfile offlineLoadProfile : ace4000.getOfflineDevice().getMasterOfflineLoadProfiles()) {
            if (offlineLoadProfile.getObisCode().equals(DeviceLoadProfileSupport.GENERIC_LOAD_PROFILE_OBISCODE)) {
                loadProfileReader = new LoadProfileReader(offlineLoadProfile.getObisCode(), fromDate, toDate, offlineLoadProfile.getLoadProfileId(), ace4000.getConfiguredSerialNumber(), new ArrayList<ChannelInfo>());
            }
        }

        ReadLoadProfile readLoadProfileRequest = new ReadLoadProfile(ace4000);
        CollectedMessage collectedMessage = createCollectedMessageWithLoadProfileData(pendingMessage, readLoadProfileRequest.request(loadProfileReader).get(0));
        collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);

        return collectedMessage;

    }

    private CollectedMessage createCollectedMessageWithLoadProfileData(OfflineDeviceMessage message, CollectedLoadProfile collectedLoadProfile) {
        return MdcManager.getCollectedDataFactory().createCollectedMessageWithLoadProfileData(new DeviceMessageIdentifierById(message.getDeviceMessageId()), collectedLoadProfile);
    }

    private CollectedMessage readEvents(OfflineDeviceMessage pendingMessage){
        try{
            ReadMeterEvents readMeterEventsRequest = new ReadMeterEvents(ace4000);
            OfflineLogBook offlineLogBook = ace4000.getOfflineDevice().getAllOfflineLogBooks().get(0);
            List<CollectedLogBook> collectedLogBooks = readMeterEventsRequest.request(new LogBookIdentifierById(offlineLogBook.getLogBookId(), offlineLogBook.getOfflineLogBookSpec().getDeviceObisCode()));
            CollectedMessage collectedMessage = createCollectedMessageWithLogbookData(pendingMessage, collectedLogBooks.get(0));
            return collectedMessage;
        }catch (Exception e){
            createMessageFailedIssue(pendingMessage, "Read events request failed");
        }
        return null;
    }

    private CollectedMessage createCollectedMessageWithLogbookData(OfflineDeviceMessage message, CollectedLogBook collectedLogBook) {
        return MdcManager.getCollectedDataFactory().createCollectedMessageWithLogbookData(new DeviceMessageIdentifierById(message.getDeviceMessageId()), collectedLogBook);
    }

    private void sendDisplayMessage(OfflineDeviceMessage pendingMessage, int mode) {
        String message = "";
        if (mode == 1) {
            message = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.SHORT_DISPLAY_MESSAGE).getDeviceMessageAttributeValue();
            if(message.length() > 8){
                createMessageFailedIssue(pendingMessage, "Short display message failed, invalid arguments");
            }
        } else if(mode == 2) {
            message = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.LONG_DISPLAY_MESSAGE).getDeviceMessageAttributeValue();
            if(message.length() > 1024){
                createMessageFailedIssue(pendingMessage, "Long display message failed, invalid arguments");
            }
        }

        ace4000.getObjectFactory().sendDisplayMessage(mode, message);

    }

    private void sendDisplayConfigRequest(OfflineDeviceMessage pendingMessage) {
        int number1 = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.NUMBER_OF_DIGITS_BEFORE_COMMA).getDeviceMessageAttributeValue());
        int number2 = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.NUMBER_OF_DIGITS_AFTER_COMMA).getDeviceMessageAttributeValue());
        String sequence = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.DISPLAY_SEQUENCE).getDeviceMessageAttributeValue();
        int intervalInSeconds = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.DISPLAY_CYCLE_TIME).getDeviceMessageAttributeValue());

        int resolutionCode = convertToResolutionCode(number1, number2);
        if (resolutionCode == -1) {
            createMessageFailedIssue(pendingMessage, "Display configuration request failed, invalid arguments");
            return;
        }

        String[] sequenceCodes = sequence.split(",");
        String sequenceResult = "";
        try {
            for (String code : sequenceCodes) {
                if (code.length() != 1 && code.length() != 2) {
                    createMessageFailedIssue(pendingMessage, "Display configuration request failed, invalid arguments");
                    return;
                }
                sequenceResult += ((code.length() == 1) ? "0" : "") + code;
            }
        } catch (Exception e) {
            createMessageFailedIssue(pendingMessage, "Display configuration request failed, invalid arguments");
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

    private void sendMaxDemandConfigurationRequest(OfflineDeviceMessage pendingMessage) {
        int register = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.ACTIVE_REGISTERS_0_OR_REACTIVE_REGISTERS_1).getDeviceMessageAttributeValue());
        int numberOfSubIntervals = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.NUMBER_OF_SUBINTERVALS).getDeviceMessageAttributeValue());
        int subIntervalDuration = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.SUB_INTERVAL_DURATION).getDeviceMessageAttributeValue());

        if (convertToNumberOfSubIntervalsCode(numberOfSubIntervals) == null) {
            createMessageFailedIssue(pendingMessage, "Max demand configuration message failed, invalid arguments");
            return;
        }
        if (convertToSubIntervalDurationCode(subIntervalDuration) == null) {
            createMessageFailedIssue(pendingMessage, "Max demand configuration message failed, invalid arguments");
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

    private void sendConsumptionLimitationConfiguration(OfflineDeviceMessage pendingMessage) {
        int numberOfSubIntervals = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.NUMBER_OF_SUBINTERVALS).getDeviceMessageAttributeValue());
        int subIntervalDuration = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.SUB_INTERVAL_DURATION).getDeviceMessageAttributeValue());
        String failMsg = "Consumption limitation configuration message failed, invalid arguments";
        if (convertToNumberOfSubIntervalsCode(numberOfSubIntervals) == null) {
            createMessageFailedIssue(pendingMessage, failMsg);
            return;
        }
        if (convertToSubIntervalDurationCode(subIntervalDuration) == null) {
            createMessageFailedIssue(pendingMessage, failMsg);
            return;
        }
        int ovl = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.OVERRIDE_RATE).getDeviceMessageAttributeValue());
        if (ovl < 0 || ovl > 4) {
            createMessageFailedIssue(pendingMessage, failMsg);
            return;
        }
        int thresholdTolerance = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.ALLOWED_EXCESS_TOLERANCE).getDeviceMessageAttributeValue());
        if (thresholdTolerance < 0 || thresholdTolerance > 100) {
            createMessageFailedIssue(pendingMessage, failMsg);
            return;
        }
        int thresholdSelection = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.THRESHOLD_SELECTION).getDeviceMessageAttributeValue());
        if (thresholdSelection < 0 || thresholdSelection > 1) {
            createMessageFailedIssue(pendingMessage, failMsg);
            return;
        }
        List<String> switchingTimesDP0 = new ArrayList<String>();
        try {
            String[] switchingTimesStrings = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.SWITCHING_MOMENTS_DAILY_PROFILE0).getDeviceMessageAttributeValue().split(",");
            for (String switchingTime : switchingTimesStrings) {
                int hour = Integer.parseInt(switchingTime.split(":")[0]);
                int minute = Integer.parseInt(switchingTime.split(":")[1]);
                switchingTimesDP0.add(pad(Integer.toString(hour, 16)) + pad(Integer.toString(minute, 16)));
            }
        } catch (Exception e) {
            createMessageFailedIssue(pendingMessage, failMsg);
            return;
        }
        if (switchingTimesDP0.size() != 8) {
            createMessageFailedIssue(pendingMessage, failMsg);
            return;
        }

        List<Integer> thresholdsDP0 = new ArrayList<Integer>();
        try {
            String[] thresholdStrings = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.THRESHOLDS_MOMENTS_DAILY_PROFILE0).getDeviceMessageAttributeValue().split(",");
            for (String thresholdString : thresholdStrings) {
                int threshold = Integer.parseInt(thresholdString);
                if (threshold < 0) {
                    createMessageFailedIssue(pendingMessage, failMsg);
                    return;
                }
                thresholdsDP0.add(threshold);
            }
        } catch (Exception e) {
            createMessageFailedIssue(pendingMessage, failMsg);
            return;
        }
        if (thresholdsDP0.size() != 8) {
            createMessageFailedIssue(pendingMessage, failMsg);
            return;
        }

        List<Integer> unitsDP0 = new ArrayList<Integer>();
        try {
            String[] unitStrings = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.THRESHOLDS_MOMENTS).getDeviceMessageAttributeValue().split(",");
            for (String threshold : unitStrings) {
                unitsDP0.add(Integer.parseInt(threshold));
            }
        } catch (Exception e) {
            createMessageFailedIssue(pendingMessage, failMsg);
            return;
        }
        if (unitsDP0.size() != 8) {
            createMessageFailedIssue(pendingMessage, failMsg);
            return;
        }

        List<String> actionsDP0 = new ArrayList<String>();
        try {
            String[] actionStrings = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.ACTIONS_IN_HEX_DAILY_PROFILE0).getDeviceMessageAttributeValue().split(",");
            for (String action : actionStrings) {
                actionsDP0.add(pad(action));
            }
        } catch (Exception e) {
            createMessageFailedIssue(pendingMessage, failMsg);
            return;
        }
        if (actionsDP0.size() != 8) {
            createMessageFailedIssue(pendingMessage, failMsg);
            return;
        }
        List<String> switchingTimesDP1 = new ArrayList<String>();
        try {
            String[] switchingTimesStrings = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.SWITCHING_MOMENTS_DAILY_PROFILE1).getDeviceMessageAttributeValue().split(",");
            for (String switchingTime : switchingTimesStrings) {
                int hour = Integer.parseInt(switchingTime.split(":")[0]);
                int minute = Integer.parseInt(switchingTime.split(":")[1]);
                switchingTimesDP1.add(pad(Integer.toString(hour, 16)) + pad(Integer.toString(minute, 16)));
            }
        } catch (Exception e) {
            createMessageFailedIssue(pendingMessage, failMsg);
            return;
        }
        if (switchingTimesDP1.size() != 8) {
            createMessageFailedIssue(pendingMessage, failMsg);
            return;
        }

        List<Integer> thresholdsDP1 = new ArrayList<Integer>();
        try {
            String[] thresholdStrings = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.THRESHOLDS_MOMENTS_DAILY_PROFILE1).getDeviceMessageAttributeValue().split(",");
            for (String thresholdString : thresholdStrings) {
                int threshold = Integer.parseInt(thresholdString);
                if (threshold < 0) {
                    createMessageFailedIssue(pendingMessage, failMsg);
                    return;
                }
                thresholdsDP1.add(threshold);
            }
        } catch (Exception e) {
            createMessageFailedIssue(pendingMessage, failMsg);
            return;
        }
        if (thresholdsDP1.size() != 8) {
            createMessageFailedIssue(pendingMessage, failMsg);
            return;
        }

        List<Integer> unitsDP1 = new ArrayList<Integer>();
        try {
            String[] unitStrings = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.THRESHOLDS_MOMENTS).getDeviceMessageAttributeValue().split(",");
            for (String threshold : unitStrings) {
                unitsDP1.add(Integer.parseInt(threshold));
            }
        } catch (Exception e) {
            createMessageFailedIssue(pendingMessage, failMsg);
            return;
        }
        if (unitsDP1.size() != 8) {
            createMessageFailedIssue(pendingMessage, failMsg);
            return;
        }

        List<String> actionsDP1 = new ArrayList<String>();
        try {
            String[] actionStrings = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.ACTIONS_IN_HEX_DAILY_PROFILE1).getDeviceMessageAttributeValue().split(",");
            for (String action : actionStrings) {
                actionsDP1.add(pad(action));
            }
        } catch (Exception e) {
            createMessageFailedIssue(pendingMessage, failMsg);
            return;
        }
        if (actionsDP1.size() != 8) {
            createMessageFailedIssue(pendingMessage, failMsg);
            return;
        }

        List<Integer> weekProfile = new ArrayList<Integer>();
        try {
            String[] dayStrings = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.DAY_PROFILES).getDeviceMessageAttributeValue().split(",");
            for (String day : dayStrings) {
                weekProfile.add(Integer.parseInt(day));
            }
        } catch (Exception e) {
            createMessageFailedIssue(pendingMessage, failMsg);
            return;
        }
        if (weekProfile.size() != 7) {
            createMessageFailedIssue(pendingMessage, failMsg);
            return;
        }

        Date date = null;

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        try {
            String dateString = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.ACTIVATION_DATE).getDeviceMessageAttributeValue();
            date = formatter.parse(dateString);
        } catch (ParseException e) {
            createMessageFailedIssue(pendingMessage, failMsg);
            return;
        }


        ace4000.getObjectFactory().sendConsumptionLimitationConfigurationRequest(date, convertToNumberOfSubIntervalsCode(numberOfSubIntervals), convertToSubIntervalDurationCode(subIntervalDuration), ovl, thresholdTolerance, thresholdSelection, switchingTimesDP0, thresholdsDP0, unitsDP0, actionsDP0, switchingTimesDP1, thresholdsDP1, unitsDP1, actionsDP1, weekProfile);
    }

    private void sendEmergencyConsumptionLimitationConfiguration(OfflineDeviceMessage pendingMessage) {
        int duration = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.DURATION_MINUTES).getDeviceMessageAttributeValue());
        int threshold = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.TRESHOLD_VALUE).getDeviceMessageAttributeValue());
        int unit = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.TRESHOLD_UNIT).getDeviceMessageAttributeValue());
        int overrideRate = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.OVERRIDE_RATE).getDeviceMessageAttributeValue());
        String failMsg = "Emergency consumption limitation configuration message failed, invalid arguments";
        if (duration > 0xFFFF || duration < 0) {
            createMessageFailedIssue(pendingMessage, failMsg);
            return;
        }
        if (threshold < 0) {
            createMessageFailedIssue(pendingMessage, failMsg);
            return;
        }
        if (unit != 0 && unit != 1) {
            createMessageFailedIssue(pendingMessage, failMsg);
            return;
        }
        if (overrideRate < 0 || overrideRate > 4) {
            createMessageFailedIssue(pendingMessage, failMsg);
            return;
        }
        ace4000.getObjectFactory().sendEmergencyConsumptionLimitationConfigurationRequest(duration, threshold, unit, overrideRate);
    }

    private void sendSDMConfigurationRequest(OfflineDeviceMessage pendingMessage) {
        String failMsg = "Special data mode configuration message failed, invalid arguments";

        int duration = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.SPECIAL_DATE_MODE_DURATION_DAYS).getDeviceMessageAttributeValue());
        Date date;
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            date = formatter.parse(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.SPECIAL_DATE_MODE_DURATION_DATE).getDeviceMessageAttributeValue());
        } catch (Exception e) {
            createMessageFailedIssue(pendingMessage, failMsg);
            return;
        }

        int billingEnable = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.SPECIAL_BILLING_REGISTER_RECORDING).getDeviceMessageAttributeValue());
        if (billingEnable != 0 && billingEnable != 1) {
            createMessageFailedIssue(pendingMessage, failMsg);
            return;
        }
        int billingInterval = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.SPECIAL_BILLING_REGISTER_RECORDING_INTERVAL).getDeviceMessageAttributeValue());
        if (billingInterval != 0 && billingInterval != 1 && billingInterval != 2) {
            createMessageFailedIssue(pendingMessage, failMsg);
            return;
        }
        int billingNumber = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.SPECIAL_BILLING_REGISTER_RECORDING_MAX_NUMBER_RECORDS).getDeviceMessageAttributeValue());
        if (billingNumber > 0xFFFF || billingNumber < 0) {
            createMessageFailedIssue(pendingMessage, failMsg);
            return;
        }

        int loadProfileEnable = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.SPECIAL_LOAD_PROFILE).getDeviceMessageAttributeValue());
        if (loadProfileEnable != 0 && loadProfileEnable != 1) {
            createMessageFailedIssue(pendingMessage, failMsg);
            return;
        }
        int loadProfileInterval = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.SPECIAL_LOAD_PROFILE_INTERVAL).getDeviceMessageAttributeValue());
        if (loadProfileInterval != 1 && loadProfileInterval != 2 && loadProfileInterval != 3 && loadProfileInterval != 5 && loadProfileInterval != 6 && loadProfileInterval != 0x0A && loadProfileInterval != 0x0C && loadProfileInterval != 0x0F && loadProfileInterval != 0x14 && loadProfileInterval != 0x1E && loadProfileInterval != 0x3C && loadProfileInterval != 0x78 && loadProfileInterval != 0xF0) {
            createMessageFailedIssue(pendingMessage, failMsg);
            return;
        }
        int loadProfileNumber = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.SPECIAL_LOAD_PROFILE_MAX_NO).getDeviceMessageAttributeValue());
        if (loadProfileNumber > 0xFFFF || loadProfileNumber < 0) {
            createMessageFailedIssue(pendingMessage, failMsg);
            return;
        }
        ace4000.getObjectFactory().sendSDMConfiguration(billingEnable, billingInterval, billingNumber, loadProfileEnable, loadProfileInterval, loadProfileNumber, duration, date);
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

    private void sendTariffConfiguration(OfflineDeviceMessage pendingMessage) {
        int number = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.UNIQUE_TARIFF_ID_NO).getDeviceMessageAttributeValue());
        int numberOfRates = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.NUMBER_OF_TARIFF_RATES).getDeviceMessageAttributeValue());
        String codeTableBase64 = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.CODE_TABLE_ID).getDeviceMessageAttributeValue();

        if (numberOfRates > 4 || numberOfRates < 0) {
            createMessageFailedIssue(pendingMessage, "Tariff configuration failed, invalid number of rates");
            return;
        }

        try {
            CodeObject codeObject = validateAndGetCodeObject(codeTableBase64);
            ace4000.getObjectFactory().sendTariffConfiguration(number, numberOfRates, codeObject);
        } catch (ApplicationException | IOException e) {  //Thrown while parsing the code table
            createMessageFailedIssue(pendingMessage, "Tariff configuration failed, invalid code table settings");
        }
    }

    private CodeObject validateAndGetCodeObject(String codeTableBase64) throws IOException {
        try {
            CodeObject codeObject = CodeTableBase64Parser.getCodeTableFromBase64(codeTableBase64);
            CodeObjectValidator.validateCodeObject(codeObject);
            return codeObject;
        } catch (BusinessException e) {
            throw new IOException(e.getMessage());
        }
    }

    public CollectedMessage createCollectedMessage(OfflineDeviceMessage pendingMessage) {
        return MdcManager.getCollectedDataFactory().createCollectedMessage(new DeviceMessageIdentifierById(pendingMessage.getDeviceMessageId()));
    }

    public Issue createUnsupportedWarning(OfflineDeviceMessage pendingMessage) {
        return MdcManager.getIssueFactory().createWarning(pendingMessage, "DeviceMessage.notSupported",
                pendingMessage.getDeviceMessageId(),
                pendingMessage.getSpecification().getCategory().getName(),
                pendingMessage.getSpecification().getName());
    }

    public Issue createMessageFailedIssue(OfflineDeviceMessage pendingMessage, Exception e) {
        return createMessageFailedIssue(pendingMessage, e.getMessage());
    }

    public Issue createMessageFailedIssue(OfflineDeviceMessage pendingMessage, String message) {
        return MdcManager.getIssueFactory().createWarning(pendingMessage, "DeviceMessage.failed",
                pendingMessage.getDeviceMessageId(),
                pendingMessage.getSpecification().getCategory().getName(),
                pendingMessage.getSpecification().getName(),
                message);
    }

    private Logger getLogger() {
        return this.ace4000.getLogger();
    }

    private void log(Level level, String msg) {
        getLogger().log(level, msg);
    }

}