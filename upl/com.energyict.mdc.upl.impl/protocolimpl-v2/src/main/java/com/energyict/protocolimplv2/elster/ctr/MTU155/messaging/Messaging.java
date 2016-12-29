package com.energyict.protocolimplv2.elster.ctr.MTU155.messaging;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.Extractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.LoadProfile;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.DeviceMessageFile;
import com.energyict.mdc.upl.properties.Password;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TariffCalendar;
import com.energyict.mdc.upl.tasks.support.DeviceMessageSupport;

import com.energyict.protocolimplv2.elster.ctr.MTU155.MTU155;
import com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.CodeTableBase64Builder;
import com.energyict.protocolimplv2.identifiers.DeviceMessageIdentifierById;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.ClockDeviceMessage;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadProfileMessage;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.protocolimplv2.messages.convertor.utils.LoadProfileMessageUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Implementation for {@link DeviceMessageSupport} interface for MTU155 CTR protocol.
 *
 * @author sva
 * @since 12/06/13 - 10:12
 */
public class Messaging implements DeviceMessageSupport {

    private final MTU155 protocol;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;
    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;
    private final Converter converter;
    private final Extractor extractor;

    public Messaging(MTU155 protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, Extractor extractor) {
        this.protocol = protocol;
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
        this.converter = converter;
        this.extractor = extractor;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return Arrays.asList(
        // Change connectivity setup
        NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS.get(this.propertySpecService, this.nlsService, this.converter),
        NetworkConnectivityMessage.CHANGE_SMS_CENTER_NUMBER.get(this.propertySpecService, this.nlsService, this.converter),
        NetworkConnectivityMessage.CHANGE_DEVICE_PHONENUMBER.get(this.propertySpecService, this.nlsService, this.converter),
        NetworkConnectivityMessage.CHANGE_GPRS_IP_ADDRESS_AND_PORT.get(this.propertySpecService, this.nlsService, this.converter),
        NetworkConnectivityMessage.CHANGE_WAKEUP_FREQUENCY.get(this.propertySpecService, this.nlsService, this.converter),

        // Device Configuration
        ConfigurationChangeDeviceMessage.ConfigureConverterMasterData.get(this.propertySpecService, this.nlsService, this.converter),
        ConfigurationChangeDeviceMessage.ConfigureGasMeterMasterData.get(this.propertySpecService, this.nlsService, this.converter),
        ConfigurationChangeDeviceMessage.ConfigureGasParameters.get(this.propertySpecService, this.nlsService, this.converter),
        ClockDeviceMessage.EnableOrDisableDST.get(this.propertySpecService, this.nlsService, this.converter),
        ConfigurationChangeDeviceMessage.WriteNewPDRNumber.get(this.propertySpecService, this.nlsService, this.converter),

        // Key management
        SecurityMessage.ACTIVATE_DEACTIVATE_TEMPORARY_ENCRYPTION_KEY.get(this.propertySpecService, this.nlsService, this.converter),
        SecurityMessage.CHANGE_EXECUTION_KEY.get(this.propertySpecService, this.nlsService, this.converter),
        SecurityMessage.CHANGE_TEMPORARY_KEY.get(this.propertySpecService, this.nlsService, this.converter),

        // Seals management
        SecurityMessage.BREAK_OR_RESTORE_SEALS.get(this.propertySpecService, this.nlsService, this.converter),
        SecurityMessage.TEMPORARY_BREAK_SEALS.get(this.propertySpecService, this.nlsService, this.converter),

        // Tariff management
        ActivityCalendarDeviceMessage.CLEAR_AND_DISABLE_PASSIVE_TARIFF.get(this.propertySpecService, this.nlsService, this.converter),
        ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATE.get(this.propertySpecService, this.nlsService, this.converter),

        // LoadProfile group
        LoadProfileMessage.PARTIAL_LOAD_PROFILE_REQUEST.get(this.propertySpecService, this.nlsService, this.converter),

        // Firmware upgrade
        FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_VERSION_AND_ACTIVATE.get(this.propertySpecService, this.nlsService, this.converter));
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList result = this.collectedDataFactory.createCollectedMessageList(pendingMessages);

        for (OfflineDeviceMessage pendingMessage : pendingMessages) {
            boolean messageNotFound = true;
            CollectedMessage collectedMessage = null;
            for (AbstractMTU155Message messageExecutor : getAllSupportedMTU155MessageExecutors()) {
                if (messageExecutor.canExecuteThisMessage(pendingMessage)) {
                    messageNotFound = false;
                    collectedMessage = messageExecutor.executeMessage(pendingMessage);
                    break;
                }
            }
            if (messageNotFound) {
                collectedMessage = createCollectedMessage(pendingMessage);
                collectedMessage.setFailureInformation(ResultType.NotSupported, this.issueFactory.createWarning(pendingMessage, "DeviceMessage.notSupported",
                        pendingMessage.getDeviceMessageId(),
                        pendingMessage.getSpecification().getCategory().getName(),
                        pendingMessage.getSpecification().getName()));
            }

            result.addCollectedMessage(collectedMessage);
        }
        return result;
    }

    private CollectedMessage createCollectedMessage(OfflineDeviceMessage message) {
        return this.collectedDataFactory.createCollectedMessage(new DeviceMessageIdentifierById(message.getDeviceMessageId()));
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return this.collectedDataFactory.createEmptyCollectedMessageList();  //Nothing to do here...
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, com.energyict.mdc.upl.properties.PropertySpec propertySpec, Object messageAttribute) {
        switch (propertySpec.getName()) {
            case DeviceMessageConstants.activityCalendarActivationDateAttributeName:
            case DeviceMessageConstants.fromDateAttributeName:
            case DeviceMessageConstants.toDateAttributeName:
            case DeviceMessageConstants.firmwareUpdateActivationDateAttributeName:
                return Long.toString(((Date) messageAttribute).getTime());
            case DeviceMessageConstants.executionKeyAttributeName:
            case DeviceMessageConstants.temporaryKeyAttributeName:
            case DeviceMessageConstants.passwordAttributeName:
                return ((Password) messageAttribute).getValue();
            case DeviceMessageConstants.activityCalendarCodeTableAttributeName:
                return CodeTableBase64Builder.getXmlStringFromCodeTable((TariffCalendar) messageAttribute, this.extractor);
            case DeviceMessageConstants.loadProfileAttributeName:
                return LoadProfileMessageUtils.formatLoadProfile((LoadProfile) messageAttribute, this.extractor);
            case DeviceMessageConstants.firmwareUpdateUserFileAttributeName:
                DeviceMessageFile messageFile = (DeviceMessageFile) messageAttribute;
                return this.extractor.contents(messageFile);
            default:
                return messageAttribute.toString();
        }
    }

    @Override
    public String prepareMessageContext(OfflineDevice offlineDevice, com.energyict.mdc.upl.messages.DeviceMessage deviceMessage) {
        return "";
    }

    private AbstractMTU155Message[] getAllSupportedMTU155MessageExecutors() {
        return new AbstractMTU155Message[]{
                // Device configuration group
                new WriteConverterMasterDataMessage(this),
                new WriteMeterMasterDataMessage(this),
                new WriteGasParametersMessage(this),
                new ChangeDSTMessage(this),
                new WritePDRMessage(this),

                // Connectivity setup group
                new DevicePhoneNumberSetupMessage(this),
                new ApnSetupMessage(this, collectedDataFactory, issueFactory),
                new SMSCenterSetupMessage(this),
                new IPSetupMessage(this),
                new WakeUpFrequency(this),

                // Key management
                new ActivateTemporaryKeyMessage(this, this.collectedDataFactory, this.issueFactory),
                new ChangeExecutionKeyMessage(this),
                new ChangeTemporaryKeyMessage(this),

                // Seals management group
                new TemporaryBreakSealMessage(this),
                new ChangeSealStatusMessage(this),

                // Tariff management
                new TariffUploadPassiveMessage(this, this.collectedDataFactory, this.issueFactory),
                new TariffDisablePassiveMessage(this),

                // LoadProfile group
                new ReadPartialProfileDataMessage(this),

                // Firmware Upgrade
                new FirmwareUpgradeMessage(this)
        };
    }

    public MTU155 getProtocol() {
        return protocol;
    }
}
