package com.energyict.protocolimplv2.ace4000;

import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.messages.DeviceMessageStatus;
import com.energyict.mdc.meterdata.*;
import com.energyict.mdc.protocol.tasks.support.DeviceLoadProfileSupport;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineLoadProfile;
import com.energyict.mdw.offline.OfflineLogBook;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.ace4000.requests.*;
import com.energyict.protocolimplv2.identifiers.DeviceMessageIdentifierById;
import com.energyict.protocolimplv2.identifiers.LogBookIdentifierById;
import com.energyict.protocolimplv2.messages.*;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
                if (pendingMessage.getSpecification().equals(LogBookDeviceMessage.ReadLogBook)) {
                    collectedMessage = readEvents(pendingMessage, collectedMessage);
                } else if (pendingMessage.getSpecification().equals(LoadProfileMessage.READ_PROFILE_DATA)) {
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
                } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.SendShortDisplayMessage)) {
                    SendDisplayMessage sendDisplayMessage = new SendDisplayMessage(ace4000, 1);
                    collectedMessage = sendDisplayMessage.request(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.SendLongDisplayMessage)) {
                    SendDisplayMessage sendDisplayMessage = new SendDisplayMessage(ace4000, 2);
                    collectedMessage = sendDisplayMessage.request(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.ResetDisplayMessage)) {
                    SendDisplayMessage sendDisplayMessage = new SendDisplayMessage(ace4000, 0);
                    collectedMessage = sendDisplayMessage.request(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.ConfigureLCDDisplay)) {
                    ConfigureLCDDisplay configureLCDDisplay = new ConfigureLCDDisplay(ace4000);
                    collectedMessage = configureLCDDisplay.request(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.ConfigureLoadProfileDataRecording)) {
                    LoadProfileConfigRequest loadProfileConfigRequest = new LoadProfileConfigRequest(ace4000);
                    collectedMessage = loadProfileConfigRequest.request(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.ConfigureSpecialDataMode)) {
                    ConfigureSpecialDataMode configureSpecialDataMode = new ConfigureSpecialDataMode(ace4000);
                    collectedMessage = configureSpecialDataMode.request(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.ConfigureMaxDemandSettings)) {
                    SendMaxDemandConfigurationRequest sendMaxDemandConfigurationRequest = new SendMaxDemandConfigurationRequest(ace4000);
                    collectedMessage = sendMaxDemandConfigurationRequest.request(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.ConfigureConsumptionLimitationsSettings)) {
                    ConfigureConsumptionLimitationsSettings configureConsumptionLimitationsSettings = new ConfigureConsumptionLimitationsSettings(ace4000);
                    collectedMessage = configureConsumptionLimitationsSettings.request(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.ConfigureEmergencyConsumptionLimitation)) {
                    SendEmergencyConsumptionLimitationConfiguration sendEmergencyConsumptionLimitationConfiguration = new SendEmergencyConsumptionLimitationConfiguration(ace4000);
                    collectedMessage = sendEmergencyConsumptionLimitationConfiguration.request(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.ConfigureTariffSettings)) {
                    ConfigureTariffSettings configureTariffSettings = new ConfigureTariffSettings(ace4000);
                    collectedMessage = configureTariffSettings.request(pendingMessage);
                } else {
                    String msg = "This message is not supported in the current protocol implementation";
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setFailureInformation(ResultType.NotSupported, createMessageFailedIssue(pendingMessage, msg));
                    collectedMessage.setDeviceProtocolInformation(msg);
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

    private CollectedMessage readProfileData(OfflineDeviceMessage pendingMessage) {

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

    private CollectedMessage readEvents(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) {
        try {
            ReadMeterEvents readMeterEventsRequest = new ReadMeterEvents(ace4000);
            OfflineLogBook offlineLogBook = ace4000.getOfflineDevice().getAllOfflineLogBooks().get(0);

            ObisCode logBookDeviceObisCode = offlineLogBook.getOfflineLogBookSpec().getDeviceObisCode();
            LogBookIdentifierById logBookIdentifierById = new LogBookIdentifierById(offlineLogBook.getLogBookId(), logBookDeviceObisCode);
            LogBookReader logBookReader = new LogBookReader(logBookDeviceObisCode, new Date(), logBookIdentifierById, pendingMessage.getDeviceSerialNumber());

            List<CollectedLogBook> collectedLogBooks = readMeterEventsRequest.request(logBookReader);
            return createCollectedMessageWithLogbookData(pendingMessage, collectedLogBooks.get(0));
        } catch (Exception e) {
            String message = "Read events request failed: " + e.getMessage();
            Issue issue = createMessageFailedIssue(pendingMessage, message);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.InCompatible, issue);
            collectedMessage.setDeviceProtocolInformation(message);
        }
        return collectedMessage;
    }

    private CollectedMessage createCollectedMessageWithLogbookData(OfflineDeviceMessage message, CollectedLogBook collectedLogBook) {
        return MdcManager.getCollectedDataFactory().createCollectedMessageWithLogbookData(new DeviceMessageIdentifierById(message.getDeviceMessageId()), collectedLogBook);
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