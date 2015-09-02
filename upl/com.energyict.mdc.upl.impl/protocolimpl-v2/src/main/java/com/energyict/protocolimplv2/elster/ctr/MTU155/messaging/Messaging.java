package com.energyict.protocolimplv2.elster.ctr.MTU155.messaging;

import com.energyict.cbo.Password;
import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.meterdata.CollectedMessage;
import com.energyict.mdc.meterdata.CollectedMessageList;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdc.protocol.tasks.support.DeviceMessageSupport;
import com.energyict.mdw.core.Code;
import com.energyict.mdw.core.LoadProfile;
import com.energyict.mdw.core.UserFile;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocolimplv2.MdcManager;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Implementation for {@link DeviceMessageSupport} interface for MTU155 CTR protocol
 *
 * @author sva
 * @since 12/06/13 - 10:12
 */
public class Messaging implements DeviceMessageSupport {

    private final MTU155 protocol;

    public Messaging(MTU155 protocol) {
        this.protocol = protocol;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        List<DeviceMessageSpec> result = new ArrayList<>();

        // Change connectivity setup
        result.add(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS);
        result.add(NetworkConnectivityMessage.CHANGE_SMS_CENTER_NUMBER);
        result.add(NetworkConnectivityMessage.CHANGE_DEVICE_PHONENUMBER);
        result.add(NetworkConnectivityMessage.CHANGE_GPRS_IP_ADDRESS_AND_PORT);
        result.add(NetworkConnectivityMessage.CHANGE_WAKEUP_FREQUENCY);

        // Device Configuration
        result.add(ConfigurationChangeDeviceMessage.ConfigureConverterMasterData);
        result.add(ConfigurationChangeDeviceMessage.ConfigureGasMeterMasterData);
        result.add(ConfigurationChangeDeviceMessage.ConfigureGasParameters);
        result.add(ClockDeviceMessage.EnableOrDisableDST);
        result.add(ConfigurationChangeDeviceMessage.WriteNewPDRNumber);

        // Key management
        result.add(SecurityMessage.ACTIVATE_DEACTIVATE_TEMPORARY_ENCRYPTION_KEY);
        result.add(SecurityMessage.CHANGE_EXECUTION_KEY);
        result.add(SecurityMessage.CHANGE_TEMPORARY_KEY);

        // Seals management
        result.add(SecurityMessage.BREAK_OR_RESTORE_SEALS);
        result.add(SecurityMessage.TEMPORARY_BREAK_SEALS);

        // Tariff management
        result.add(ActivityCalendarDeviceMessage.CLEAR_AND_DISABLE_PASSIVE_TARIFF);
        result.add(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATE);

        // LoadProfile group
        result.add(LoadProfileMessage.PARTIAL_LOAD_PROFILE_REQUEST);

        // Firmware upgrade
        result.add(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_VERSION_AND_ACTIVATE);

        return result;
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList result = MdcManager.getCollectedDataFactory().createCollectedMessageList(pendingMessages);

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
                collectedMessage.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueFactory().createWarning(pendingMessage, "DeviceMessage.notSupported",
                        pendingMessage.getDeviceMessageId(),
                        pendingMessage.getSpecification().getCategory().getName(),
                        pendingMessage.getSpecification().getName()));
            }

            result.addCollectedMessage(collectedMessage);
        }
        return result;
    }

    private CollectedMessage createCollectedMessage(OfflineDeviceMessage message) {
        return MdcManager.getCollectedDataFactory().createCollectedMessage(new DeviceMessageIdentifierById(message.getDeviceMessageId()));
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return MdcManager.getCollectedDataFactory().createEmptyCollectedMessageList();  //Nothing to do here...
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
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
                return CodeTableBase64Builder.getXmlStringFromCodeTable((Code) messageAttribute);
            case DeviceMessageConstants.loadProfileAttributeName:
                return LoadProfileMessageUtils.formatLoadProfile((LoadProfile) messageAttribute);
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
