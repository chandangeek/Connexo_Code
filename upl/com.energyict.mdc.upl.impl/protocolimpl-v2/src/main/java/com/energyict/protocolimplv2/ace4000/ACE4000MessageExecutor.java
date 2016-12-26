package com.energyict.protocolimplv2.ace4000;

import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.tasks.support.DeviceLoadProfileSupport;

import com.energyict.cbo.ApplicationException;
import com.energyict.mdw.core.LogBookTypeFactory;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocolimplv2.ace4000.requests.ConfigureConsumptionLimitationsSettings;
import com.energyict.protocolimplv2.ace4000.requests.ConfigureLCDDisplay;
import com.energyict.protocolimplv2.ace4000.requests.ConfigureSpecialDataMode;
import com.energyict.protocolimplv2.ace4000.requests.ConfigureTariffSettings;
import com.energyict.protocolimplv2.ace4000.requests.ContactorCommand;
import com.energyict.protocolimplv2.ace4000.requests.FirmwareUpgrade;
import com.energyict.protocolimplv2.ace4000.requests.LoadProfileConfigRequest;
import com.energyict.protocolimplv2.ace4000.requests.ReadLoadProfile;
import com.energyict.protocolimplv2.ace4000.requests.ReadMeterEvents;
import com.energyict.protocolimplv2.ace4000.requests.SendDisplayMessage;
import com.energyict.protocolimplv2.ace4000.requests.SendEmergencyConsumptionLimitationConfiguration;
import com.energyict.protocolimplv2.ace4000.requests.SendMaxDemandConfigurationRequest;
import com.energyict.protocolimplv2.identifiers.DeviceMessageIdentifierById;
import com.energyict.protocolimplv2.identifiers.LogBookIdentifierByObisCodeAndDevice;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadProfileMessage;
import com.energyict.protocolimplv2.messages.LogBookDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ACE4000MessageExecutor {

    private ACE4000Outbound ace4000;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;

    public ACE4000MessageExecutor(ACE4000Outbound ace4000, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        this.ace4000 = ace4000;
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
    }

    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList result = this.collectedDataFactory.createCollectedMessageList(pendingMessages);
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
            } catch (ApplicationException | NumberFormatException | IndexOutOfBoundsException e) {
                log(Level.INFO, "Message has failed. " + e.toString());
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
                collectedMessage.setDeviceProtocolInformation(e.toString());
            }

            result.addCollectedMessage(collectedMessage);
        }

        return result;
    }

    private CollectedMessage readProfileData(OfflineDeviceMessage pendingMessage) {

        String fromDateString = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.fromDateAttributeName).getValue();
        String toDateString = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.toDateAttributeName).getValue();
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
        LoadProfileReader loadProfileReader = new LoadProfileReader(DeviceLoadProfileSupport.GENERIC_LOAD_PROFILE_OBISCODE, fromDate, toDate, 0, ace4000.getConfiguredSerialNumber(), new ArrayList<ChannelInfo>());

        ReadLoadProfile readLoadProfileRequest = new ReadLoadProfile(ace4000, issueFactory);
        CollectedMessage collectedMessage = createCollectedMessageWithLoadProfileData(pendingMessage, readLoadProfileRequest.request(loadProfileReader).get(0));
        collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);

        return collectedMessage;

    }

    private CollectedMessage createCollectedMessageWithLoadProfileData(OfflineDeviceMessage message, CollectedLoadProfile collectedLoadProfile) {
        CollectedMessage collectedMessageWithLoadProfileData = this.collectedDataFactory.createCollectedMessageWithLoadProfileData(new DeviceMessageIdentifierById(message.getDeviceMessageId()), collectedLoadProfile);
        collectedMessageWithLoadProfileData.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
        return collectedMessageWithLoadProfileData;
    }

    private CollectedMessage readEvents(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) {
        try {
            ReadMeterEvents readMeterEventsRequest = new ReadMeterEvents(ace4000, issueFactory);
            LogBookIdentifierByObisCodeAndDevice logBookIdentifier = new LogBookIdentifierByObisCodeAndDevice(ace4000.getDeviceIdentifier(), LogBookTypeFactory.GENERIC_LOGBOOK_TYPE_OBISCODE);
            LogBookReader logBookReader = new LogBookReader(LogBookTypeFactory.GENERIC_LOGBOOK_TYPE_OBISCODE, new Date(), logBookIdentifier, pendingMessage.getDeviceSerialNumber());
            List<CollectedLogBook> collectedLogBooks = readMeterEventsRequest.request(logBookReader);
            return createCollectedMessageWithLogbookData(pendingMessage, collectedLogBooks.get(0));
        } catch (ApplicationException | NumberFormatException | IndexOutOfBoundsException e) {
            String message = "Read events request failed: " + e.toString();
            Issue issue = createMessageFailedIssue(pendingMessage, message);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.InCompatible, issue);
            collectedMessage.setDeviceProtocolInformation(message);
        }
        return collectedMessage;
    }

    private CollectedMessage createCollectedMessageWithLogbookData(OfflineDeviceMessage message, CollectedLogBook collectedLogBook) {
        CollectedMessage collectedMessageWithLogbookData = this.collectedDataFactory.createCollectedMessageWithLogbookData(new DeviceMessageIdentifierById(message.getDeviceMessageId()), collectedLogBook);
        collectedMessageWithLogbookData.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
        return collectedMessageWithLogbookData;
    }

    public CollectedMessage createCollectedMessage(OfflineDeviceMessage pendingMessage) {
        CollectedMessage collectedMessage = this.collectedDataFactory.createCollectedMessage(new DeviceMessageIdentifierById(pendingMessage.getDeviceMessageId()));
        collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
        return collectedMessage;
    }

    public Issue createUnsupportedWarning(OfflineDeviceMessage pendingMessage) {
        return this.issueFactory
                .createWarning(
                        pendingMessage,
                        "DeviceMessage.notSupported",
                        pendingMessage.getDeviceMessageId(),
                        pendingMessage.getSpecification().getCategory().getName(),
                        pendingMessage.getSpecification().getName());
    }

    public Issue createMessageFailedIssue(OfflineDeviceMessage pendingMessage, Throwable e) {
        return createMessageFailedIssue(pendingMessage, e.getMessage());
    }

    public Issue createMessageFailedIssue(OfflineDeviceMessage pendingMessage, String message) {
        return this.issueFactory
                .createWarning(
                        pendingMessage,
                        "DeviceMessage.failed",
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