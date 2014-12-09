package com.energyict.protocolimplv2.elster.ctr.MTU155.messaging;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.Password;
import com.energyict.mdc.protocol.api.UserFile;
import com.energyict.mdc.protocol.api.codetables.Code;
import com.energyict.mdc.protocol.api.device.BaseLoadProfile;
import com.energyict.mdc.protocol.api.device.data.CollectedMessage;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.tasks.support.DeviceMessageSupport;
import com.energyict.protocolimplv2.elster.ctr.MTU155.MTU155;
import com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.CodeTableBase64Builder;
import com.energyict.protocolimplv2.messages.convertor.utils.LoadProfileMessageUtils;

import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Implementation for {@link DeviceMessageSupport} interface for MTU155 CTR protocol
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
            DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_VERSION_AND_ACTIVATE

    );

    private final MTU155 protocol;

    public Messaging(MTU155 protocol) {
        this.protocol = protocol;
    }

    @Override
    public Set<DeviceMessageId> getSupportedMessages() {
        return this.supportedMessageIds;
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList result = com.energyict.mdc.protocol.api.CollectedDataFactoryProvider.instance.get().getCollectedDataFactory().createCollectedMessageList(pendingMessages);

        for (OfflineDeviceMessage pendingMessage : pendingMessages) {
            boolean messageFound = false;
            CollectedMessage collectedMessage = null;
            for (AbstractMTU155Message messageExecutor : getAllSupportedMTU155MessageExecutors()) {
                if (messageExecutor.canExecuteThisMessage(pendingMessage)) {
                    messageFound = true;
                    collectedMessage = messageExecutor.executeMessage(pendingMessage);
                    break;
                }
            }
            if (!messageFound) {
                collectedMessage = createCollectedMessage(pendingMessage);
                collectedMessage.setFailureInformation(ResultType.NotSupported, com.energyict.protocols.mdc.services.impl.Bus.getIssueService().newWarning(pendingMessage, "DeviceMessage.notSupported",
                        pendingMessage.getDeviceMessageId(),
                        pendingMessage.getSpecification().getCategory().getName(),
                        pendingMessage.getSpecification().getName()));
            }

            result.addCollectedMessages(collectedMessage);
        }
        return result;
    }

    private CollectedMessage createCollectedMessage(OfflineDeviceMessage message) {
        return com.energyict.mdc.protocol.api.CollectedDataFactoryProvider.instance.get().getCollectedDataFactory().createCollectedMessage(message.getIdentifier());
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return com.energyict.mdc.protocol.api.CollectedDataFactoryProvider.instance.get().getCollectedDataFactory().createEmptyCollectedMessageList();  //Nothing to do here...
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
            case DeviceMessageConstants.activityCalendarCodeTableAttributeName:
                return CodeTableBase64Builder.getXmlStringFromCodeTable((Code) messageAttribute);
            case DeviceMessageConstants.loadProfileAttributeName:
                return LoadProfileMessageUtils.formatLoadProfile((BaseLoadProfile) messageAttribute);
            case DeviceMessageConstants.firmwareUpdateUserFileAttributeName:
                UserFile userFile = (UserFile) messageAttribute;
                return new String(userFile.loadFileInByteArray());  //Bytes of the userFile, as a string
            default:
                return messageAttribute.toString();
        }
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
                new ApnSetupMessage(this),
                new SMSCenterSetupMessage(this),
                new IPSetupMessage(this),
                new WakeUpFrequency(this),

                // Key management
                new ActivateTemporaryKeyMessage(this),
                new ChangeExecutionKeyMessage(this),
                new ChangeTemporaryKeyMessage(this),

                // Seals management group
                new TemporaryBreakSealMessage(this),
                new ChangeSealStatusMessage(this),

                // Tariff management
                new TariffUploadPassiveMessage(this),
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
