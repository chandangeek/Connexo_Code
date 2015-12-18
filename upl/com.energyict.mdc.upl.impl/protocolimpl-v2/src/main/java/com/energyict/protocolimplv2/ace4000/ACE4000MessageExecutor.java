package com.energyict.protocolimplv2.ace4000;

import com.energyict.cbo.ApplicationException;
import com.energyict.cbo.BusinessException;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.mdc.meterdata.CollectedLoadProfile;
import com.energyict.mdc.meterdata.CollectedLogBook;
import com.energyict.mdc.protocol.tasks.support.DeviceLoadProfileSupport;
import com.energyict.mdw.core.Code;
import com.energyict.mdw.offline.OfflineLoadProfile;
import com.energyict.mdw.offline.OfflineLogBook;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.generic.MessageParser;
import com.energyict.protocolimpl.generic.messages.MessageHandler;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.ace4000.requests.*;
import com.energyict.protocolimplv2.identifiers.LogBookIdentifierById;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ACE4000MessageExecutor extends MessageParser {

    private ACE4000Outbound ace4000;

    public ACE4000MessageExecutor(ACE4000Outbound ace4000) {
        this.ace4000 = ace4000;
    }

    public Integer sendConfigurationMessage(MessageEntry messageEntry) {
        String messageContent = messageEntry.getContent();
        if (messageContent.contains(RtuMessageConstant.SHORT_DISPLAY_MESSAGE)) {
            sendDisplayMessage(messageContent, 1);
        } else if (messageContent.contains(RtuMessageConstant.LONG_DISPLAY_MESSAGE)) {
            sendDisplayMessage(messageContent, 2);
        } else if (messageContent.contains(RtuMessageConstant.NO_DISPLAY_MESSAGE)) {
            sendDisplayMessage(messageContent, 0);
        } else if (messageContent.contains(RtuMessageConstant.DISPLAY_CONFIG)) {
            sendDisplayConfigRequest(messageContent);
        } else if (messageContent.contains(RtuMessageConstant.CONFIG_LOADPROFILE)) {
            sendLoadProfileConfigurationRequest(messageContent);
        } else if (messageContent.contains(RtuMessageConstant.CONFIG_SPECIAL_DATA_MODE)) {
            sendSDMConfigurationRequest(messageContent);
        } else if (messageContent.contains(RtuMessageConstant.CONFIG_MAX_DEMAND)) {
            sendMaxDemandConfigurationRequest(messageContent);
        } else if (messageContent.contains(RtuMessageConstant.CONFIG_CONSUMPTION_LIMITATION)) {
            sendConsumptionLimitationConfiguration(messageContent);
        } else if (messageContent.contains(RtuMessageConstant.CONFIG_EMERGENCY_CONSUMPTION_LIMITATION)) {
            sendEmergencyConsumptionLimitationConfiguration(messageContent);
        } else if (messageContent.contains(RtuMessageConstant.CONFIG_TARIFF)) {
            sendTariffConfiguration(messageContent);
        } else {
            return null;         //Unknown message, handle properly later on
        }
        return ace4000.getObjectFactory().getTrackingID();
    }

    private String stripOffTag(String content) {
        return content.substring(content.indexOf(">") + 1, content.lastIndexOf("<"));
    }

    private void sendTariffConfiguration(String messageContent) {
        String[] parts = messageContent.split("=");
        int number = Integer.parseInt(parts[1].substring(1).split("\"")[0]);
        int numberOfRates = Integer.parseInt(parts[2].substring(1).split("\"")[0]);

        //TODO: Code table from message entry ?????
        Code codeTable = null;

        if (numberOfRates > 4 || numberOfRates < 0) {
            failMessage("Tariff configuration failed, invalid number of rates");
            return;
        }

        try {
            ace4000.getObjectFactory().sendTariffConfiguration(number, numberOfRates, codeTable);
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

    private List<CollectedLoadProfile> readProfileData(String messageContent) throws IOException, BusinessException, SQLException {
        String[] parts = messageContent.split("=");
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date fromDate = null;
        Date toDate = null;

        if (parts.length > 1) {
            try {
                String fromDateString = parts[1].substring(1, 20);
                fromDate = formatter.parse(fromDateString);
            } catch (Exception e) {
                failMessage("Request for load profile data failed, invalid arguments");
                return null;
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
            failMessage("Request for load profile data failed, invalid arguments");
            return null;
        }

        //Make a new loadProfileReader with the proper from and to date
        LoadProfileReader loadProfileReader = null;
        for (OfflineLoadProfile offlineLoadProfile : ace4000.getOfflineDevice().getMasterOfflineLoadProfiles()) {
            if (offlineLoadProfile.getObisCode().equals(DeviceLoadProfileSupport.GENERIC_LOAD_PROFILE_OBISCODE)) {
                loadProfileReader = new LoadProfileReader(offlineLoadProfile.getObisCode(), fromDate, toDate, offlineLoadProfile.getLoadProfileId(), ace4000.getSerialNumber(), new ArrayList<ChannelInfo>());
            }
        }

        ReadLoadProfile readLoadProfileRequest = new ReadLoadProfile(ace4000);
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
        List<String> switchingTimesDP0 = new ArrayList<String>();
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

        List<Integer> thresholdsDP0 = new ArrayList<Integer>();
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

        List<Integer> unitsDP0 = new ArrayList<Integer>();
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

        List<String> actionsDP0 = new ArrayList<String>();
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
        List<String> switchingTimesDP1 = new ArrayList<String>();
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

        List<Integer> thresholdsDP1 = new ArrayList<Integer>();
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

        List<Integer> unitsDP1 = new ArrayList<Integer>();
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

        List<String> actionsDP1 = new ArrayList<String>();
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

        List<Integer> weekProfile = new ArrayList<Integer>();
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

    public MessageResult executeMessageEntry(final MessageEntry messageEntry) throws IOException {
        boolean success = false;
        String content = messageEntry.getContent();
        MessageHandler messageHandler = new MessageHandler();
        try {
            importMessage(content, messageHandler);
            boolean isReadEvents = messageHandler.getType().equals(RtuMessageConstant.READ_EVENTS);
            boolean isReadProfileData = messageHandler.getType().equals(RtuMessageConstant.READ_PROFILE_DATA);
            boolean isFirmwareUpgrade = messageHandler.getType().equals(RtuMessageConstant.FIRMWARE_UPGRADE);
            boolean isConnect = messageHandler.getType().equals(RtuMessageConstant.CONNECT);
            boolean isDisconnect = messageHandler.getType().equals(RtuMessageConstant.DISCONNECT);
            boolean isShortDisplayMessage = messageHandler.getType().equals(RtuMessageConstant.SHORT_DISPLAY_MESSAGE);
            boolean isLongDisplayMessage = messageHandler.getType().equals(RtuMessageConstant.LONG_DISPLAY_MESSAGE);
            boolean isNoDisplayMessage = messageHandler.getType().equals(RtuMessageConstant.NO_DISPLAY_MESSAGE);
            boolean isDisplayConfig = messageHandler.getType().equals(RtuMessageConstant.DISPLAY_CONFIG);
            boolean isConfigLoadProfile = messageHandler.getType().equals(RtuMessageConstant.CONFIG_LOADPROFILE);
            boolean isConfigSpecialDataMode = messageHandler.getType().equals(RtuMessageConstant.CONFIG_SPECIAL_DATA_MODE);
            boolean isConfigMaxDemand = messageHandler.getType().equals(RtuMessageConstant.CONFIG_MAX_DEMAND);
            boolean isConfigConsumptionLimitation = messageHandler.getType().equals(RtuMessageConstant.CONFIG_CONSUMPTION_LIMITATION);
            boolean isConfigEmergencyConsumptionLimitation = messageHandler.getType().equals(RtuMessageConstant.CONFIG_EMERGENCY_CONSUMPTION_LIMITATION);
            boolean isConfigTariff = messageHandler.getType().equals(RtuMessageConstant.CONFIG_TARIFF);

            if (isReadEvents) {
                //TODO what if the offline device has no logbooks configured??
                ReadMeterEvents readMeterEventsRequest = new ReadMeterEvents(ace4000);
                OfflineLogBook offlineLogBook = ace4000.getOfflineDevice().getAllOfflineLogBooks().get(0);
                List<CollectedLogBook> collectedLogBooks = readMeterEventsRequest.request(new LogBookIdentifierById(offlineLogBook.getLogBookId(), offlineLogBook.getOfflineLogBookSpec().getDeviceObisCode()));
                MeterData meterData = new MeterData();
                for (MeterProtocolEvent collectedMeterEvent : collectedLogBooks.get(0).getCollectedMeterEvents()) {
                    meterData.addMeterEvent(collectedMeterEvent);
                }
                return MeterDataMessageResult.createSuccess(messageEntry, "", meterData);
            } else if (isReadProfileData) {
                List<CollectedLoadProfile> collectedLoadProfiles = readProfileData(content);
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
            } else if (isFirmwareUpgrade) {
                FirmwareUpgrade firmwareUpgrade = new FirmwareUpgrade(ace4000);
                return firmwareUpgrade.request(messageEntry);
            } else if (isConnect) {
                ContactorCommand contactorCommand = new ContactorCommand(ace4000);
                contactorCommand.setCommand(0);
                return contactorCommand.request(messageEntry);
            } else if (isDisconnect) {
                ContactorCommand contactorCommand = new ContactorCommand(ace4000);
                contactorCommand.setCommand(1);
                return contactorCommand.request(messageEntry);
            } else if (isShortDisplayMessage) {
                sendDisplayMessage(content, 1);
            } else if (isLongDisplayMessage) {
                sendDisplayMessage(content, 2);
            } else if (isNoDisplayMessage) {
                sendDisplayMessage(content, 0);
            } else if (isDisplayConfig) {
                sendDisplayConfigRequest(content);
            } else if (isConfigLoadProfile) {
                sendLoadProfileConfigurationRequest(content);
            } else if (isConfigSpecialDataMode) {
                sendSDMConfigurationRequest(content);
            } else if (isConfigMaxDemand) {
                sendMaxDemandConfigurationRequest(content);
            } else if (isConfigConsumptionLimitation) {
                sendConsumptionLimitationConfiguration(content);
            } else if (isConfigEmergencyConsumptionLimitation) {
                sendEmergencyConsumptionLimitationConfiguration(content);
            } else if (isConfigTariff) {
                sendTariffConfiguration(content);
            }else {
                WriteConfiguration writeConfiguration = new WriteConfiguration(ace4000);
                return writeConfiguration.request(messageEntry);
            }

        } catch (BusinessException e) {
            log(Level.INFO, "Message has failed. " + e.getMessage());
        } catch (ConnectionException e) {
            log(Level.INFO, "Message has failed. " + e.getMessage());
        } catch (IOException e) {
            log(Level.INFO, "Message has failed. " + e.getMessage());
        } catch (SQLException e) {
            log(Level.INFO, "Message has failed. " + e.getMessage());
        }
        if (success) {
            log(Level.INFO, "Message has finished.");
            return MessageResult.createSuccess(messageEntry);
        } else {
            return MessageResult.createFailed(messageEntry);
        }
    }

    private Logger getLogger() {
        return this.ace4000.getLogger();
    }

    private void log(Level level, String msg) {
        getLogger().log(level, msg);
    }

    @Override
    protected TimeZone getTimeZone() {
        return this.ace4000.getTimeZone();
    }
}