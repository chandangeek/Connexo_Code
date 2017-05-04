package com.energyict.protocolimplv2.elster.ctr.MTU155.messaging;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.Password;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.MessageSeeds;
import com.energyict.mdc.protocol.api.device.LoadProfileFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedMessage;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.tasks.support.DeviceMessageSupport;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.protocolimplv2.elster.ctr.MTU155.MTU155;
import com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.CodeTableBase64Builder;
import com.energyict.protocolimplv2.messages.convertor.utils.LoadProfileMessageUtils;

import java.time.Clock;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Implementation for {@link DeviceMessageSupport} interface for MTU155 CTR protocol.
 *
 * @author sva
 * @since 12/06/13 - 10:12
 */
public class Messaging implements DeviceMessageSupport {

    private Set<DeviceMessageId> supportedMessageIds = EnumSet.of(
            DeviceMessageId.NETWORK_CONNECTIVITY_CHANGE_GPRS_APN_CREDENTIALS,
            DeviceMessageId.NETWORK_CONNECTIVITY_CHANGE_SMS_CENTER_NUMBER,
            DeviceMessageId.NETWORK_CONNECTIVITY_CHANGE_DEVICE_PHONENUMBER,
            DeviceMessageId.NETWORK_CONNECTIVITY_CHANGE_GPRS_IP_ADDRESS_AND_PORT,
            DeviceMessageId.NETWORK_CONNECTIVITY_CHANGE_WAKEUP_FREQUENCY,
            DeviceMessageId.CONFIGURATION_CHANGE_CONFIGURE_CONVERTER_MASTER_DATA,
            DeviceMessageId.CONFIGURATION_CHANGE_CONFIGURE_GAS_METER_MASTER_DATA,
            DeviceMessageId.CONFIGURATION_CHANGE_CONFIGURE_GAS_PARAMETERS,
            DeviceMessageId.CLOCK_ENABLE_OR_DISABLE_DST,
            DeviceMessageId.CONFIGURATION_CHANGE_WRITE_NEW_PDR_NUMBER,
            DeviceMessageId.SECURITY_ACTIVATE_DEACTIVATE_TEMPORARY_ENCRYPTION_KEY,
            DeviceMessageId.SECURITY_CHANGE_EXECUTION_KEY,
            DeviceMessageId.SECURITY_CHANGE_TEMPORARY_KEY,
            DeviceMessageId.SECURITY_BREAK_OR_RESTORE_SEALS,
            DeviceMessageId.SECURITY_TEMPORARY_BREAK_SEALS,
            DeviceMessageId.ACTIVITY_CALENDAR_CLEAR_AND_DISABLE_PASSIVE_TARIFF,
            DeviceMessageId.ACTIVITY_CALENDER_SEND_WITH_DATE,
            DeviceMessageId.LOAD_PROFILE_PARTIAL_REQUEST,
            DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_VERSION_AND_ACTIVATE_DATE

    );

    private final MTU155 protocol;
    private final Clock clock;
    private final TopologyService topologyService;
    private final IssueService issueService;
    private final CollectedDataFactory collectedDataFactory;
    private final LoadProfileFactory loadProfileFactory;

    public Messaging(MTU155 protocol, Clock clock, TopologyService topologyService, IssueService issueService, CollectedDataFactory collectedDataFactory, LoadProfileFactory loadProfileFactory) {
        this.protocol = protocol;
        this.clock = clock;
        this.topologyService = topologyService;
        this.issueService = issueService;
        this.collectedDataFactory = collectedDataFactory;
        this.loadProfileFactory = loadProfileFactory;
    }

    @Override
    public Set<DeviceMessageId> getSupportedMessages() {
        return this.supportedMessageIds;
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
                collectedMessage.setFailureInformation(
                        ResultType.NotSupported,
                        this.issueService.newWarning(
                                pendingMessage,
                                MessageSeeds.DEVICEMESSAGE_NOT_SUPPORTED,
                                pendingMessage.getDeviceMessageId(),
                                pendingMessage.getSpecification().getCategory().getName(),
                                pendingMessage.getSpecification().getName()));
            }

            result.addCollectedMessages(collectedMessage);
        }
        return result;
    }

    private CollectedMessage createCollectedMessage(OfflineDeviceMessage message) {
        return this.collectedDataFactory.createCollectedMessage(message.getIdentifier());
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return this.collectedDataFactory.createEmptyCollectedMessageList();  //Nothing to do here...
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        switch (propertySpec.getName()) {
            case DeviceMessageConstants.activityCalendarActivationDateAttributeName:
            case DeviceMessageConstants.fromDateAttributeName:
            case DeviceMessageConstants.toDateAttributeName:
            case DeviceMessageConstants.firmwareUpdateActivationDateAttributeName:
                return Long.toString(((Date) messageAttribute).getTime());
            case DeviceMessageConstants.passwordAttributeName:
                return ((Password) messageAttribute).getValue();
            case DeviceMessageConstants.activityCalendarAttributeName:
                return CodeTableBase64Builder.getXmlStringFromCodeTable((Calendar) messageAttribute);
            case DeviceMessageConstants.loadProfileAttributeName:
                return LoadProfileMessageUtils.formatLoadProfile((LoadProfile) messageAttribute, this.topologyService);
            case DeviceMessageConstants.firmwareUpdateFileAttributeName:
                return messageAttribute.toString();     //This is the path of the temp file representing the FirmwareVersion
            default:
                return messageAttribute.toString();
        }
    }

    private AbstractMTU155Message[] getAllSupportedMTU155MessageExecutors() {
        return new AbstractMTU155Message[]{
                // Device configuration group
                new WriteConverterMasterDataMessage(this, this.issueService, this.collectedDataFactory),
                new WriteMeterMasterDataMessage(this, this.issueService, this.collectedDataFactory),
                new WriteGasParametersMessage(this, this.issueService, this.collectedDataFactory),
                new ChangeDSTMessage(this, this.issueService, this.collectedDataFactory),
                new WritePDRMessage(this, this.issueService, this.collectedDataFactory),

                // Connectivity setup group
                new DevicePhoneNumberSetupMessage(this, this.issueService, this.collectedDataFactory),
                new ApnSetupMessage(this, this.issueService, this.collectedDataFactory),
                new SMSCenterSetupMessage(this, this.issueService, this.collectedDataFactory),
                new IPSetupMessage(this, this.issueService, this.collectedDataFactory),
                new WakeUpFrequency(this, this.issueService, this.collectedDataFactory),

                // Key management
                new ActivateTemporaryKeyMessage(this, this.issueService, this.collectedDataFactory),
                new ChangeExecutionKeyMessage(this, this.issueService, this.collectedDataFactory),
                new ChangeTemporaryKeyMessage(this, this.issueService, this.collectedDataFactory),

                // Seals management group
                new TemporaryBreakSealMessage(this, this.issueService, this.collectedDataFactory),
                new ChangeSealStatusMessage(this, this.issueService, this.collectedDataFactory),

                // Tariff management
                new TariffUploadPassiveMessage(this, this.issueService, this.collectedDataFactory),
                new TariffDisablePassiveMessage(this, this.issueService, this.collectedDataFactory),

                // LoadProfile group
                new ReadPartialProfileDataMessage(this, clock, this.issueService, this.topologyService, this.collectedDataFactory, this.loadProfileFactory),

                // Firmware Upgrade
                new FirmwareUpgradeMessage(this, this.issueService, this.collectedDataFactory)
        };
    }

    public MTU155 getProtocol() {
        return protocol;
    }

}