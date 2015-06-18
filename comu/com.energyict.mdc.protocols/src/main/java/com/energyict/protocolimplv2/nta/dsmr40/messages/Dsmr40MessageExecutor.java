package com.energyict.protocolimplv2.nta.dsmr40.messages;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.*;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.ProtocolException;
import com.energyict.mdc.protocol.api.device.LoadProfileFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedMessage;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;
import com.energyict.protocolimplv2.nta.abstractnta.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.nta.dsmr23.messages.Dsmr23MessageExecutor;

import java.io.IOException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.*;

/**
 * @author sva
 * @since 6/01/2015 - 13:30
 */
public class Dsmr40MessageExecutor extends Dsmr23MessageExecutor {

    private static final ObisCode OBISCODE_CONFIGURATION_OBJECT = ObisCode.fromString("0.1.94.31.3.255");
    private static final ObisCode OBISCODE_PUSH_SCRIPT = ObisCode.fromString("0.0.10.0.108.255");
    private static final ObisCode OBISCODE_GLOBAL_RESET = ObisCode.fromString("0.1.94.31.5.255");

    public Dsmr40MessageExecutor(AbstractDlmsProtocol protocol, Clock clock, TopologyService topologyService, IssueService issueService, MdcReadingTypeUtilService readingTypeUtilService, CollectedDataFactory collectedDataFactory, LoadProfileFactory loadProfileFactory) {
        super(protocol, clock, topologyService, issueService, readingTypeUtilService, collectedDataFactory, loadProfileFactory);
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList result = getCollectedDataFactory().createCollectedMessageList(pendingMessages);

        List<OfflineDeviceMessage> masterMessages = getMessagesOfMaster(pendingMessages);
        List<OfflineDeviceMessage> mbusMessages = getMbusMessages(pendingMessages);
        if (!mbusMessages.isEmpty()) {
            // Execute messages for MBus devices
            Dsmr40MbusMessageExecutor dsmr40MbusMessageExecutor = new Dsmr40MbusMessageExecutor(getProtocol(), getClock(), getIssueService(), getReadingTypeUtilService(), getTopologyService(), getCollectedDataFactory(), getLoadProfileFactory());
            dsmr40MbusMessageExecutor
                    .executePendingMessages(mbusMessages)
                    .getCollectedMessages()
                    .forEach(result::addCollectedMessages);
        }

        List<OfflineDeviceMessage> notExecutedDeviceMessages = new ArrayList<>();
        for (OfflineDeviceMessage pendingMessage : sortSecurityRelatedDeviceMessages(masterMessages)) {
            CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);   //Optimistic
            try {
                if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.DEVICE_ACTIONS_RESTORE_FACTORY_SETTINGS)) {
                    restoreFactorySettings();
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.SECURITY_ENABLE_DLMS_AUTHENTICATION_LEVEL_P0)) {
                    changeAuthenticationLevel(pendingMessage, 0, true);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.SECURITY_DISABLE_DLMS_AUTHENTICATION_LEVEL_P0)) {
                    changeAuthenticationLevel(pendingMessage, 0, false);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.SECURITY_ENABLE_DLMS_AUTHENTICATION_LEVEL_P3)) {
                    changeAuthenticationLevel(pendingMessage, 3, true);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.SECURITY_DISABLE_DLMS_AUTHENTICATION_LEVEL_P3)) {
                    changeAuthenticationLevel(pendingMessage, 3, false);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.SECURITY_CHANGE_ENCRYPTION_KEY_WITH_NEW_KEYS)) {
                    changeEncryptionKeyAndUseNewKey(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.SECURITY_CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEYS)) {
                    changeAuthenticationKeyAndUseNewKey(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_IMMEDIATE)) {
                    upgradeFirmwareWithActivationDateAndImageIdentifier(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_ACTIVATE_DATE)) {
                    upgradeFirmwareWithActivationDateAndImageIdentifier(pendingMessage);
                } else {
                    collectedMessage = null;
                    notExecutedDeviceMessages.add(pendingMessage);  // These messages are not specific for Dsmr 4.0, but can be executed by the super (= Dsmr 2.3) messageExecutor
                }
            } catch (IOException e) {
                if (IOExceptionHandler.isUnexpectedResponse(e, getProtocol().getDlmsSession())) {
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
                    collectedMessage.setDeviceProtocolInformation(e.getMessage());
                }
            }
            if (collectedMessage != null) {
                result.addCollectedMessages(collectedMessage);
            }
        }

        // Then delegate all other messages to the Dsmr 2.3 message executor
        super.executePendingMessages(notExecutedDeviceMessages).getCollectedMessages()
                .stream()
                .forEach(result::addCollectedMessages);
        return result;
    }

    protected void changeAuthenticationKeyAndUseNewKey(OfflineDeviceMessage pendingMessage) throws IOException {
        throw new ProtocolException("This message is not yet supported in DSMR4.0");
    }

    protected void changeEncryptionKeyAndUseNewKey(OfflineDeviceMessage pendingMessage) throws IOException {
        throw new ProtocolException("This message is not yet supported in DSMR4.0");
    }

    protected void restoreFactorySettings() throws IOException {
        ScriptTable globalResetST = getCosemObjectFactory().getScriptTable(OBISCODE_GLOBAL_RESET);
        globalResetST.execute(0);
    }

    private void changeAuthenticationLevel(OfflineDeviceMessage pendingMessage, int type, boolean enable) throws IOException {
        int newAuthLevel = getIntegerAttribute(pendingMessage);
        if (newAuthLevel != -1) {
            Data config = getCosemObjectFactory().getData(OBISCODE_CONFIGURATION_OBJECT);
            Structure value;
            BitString flags;
            try {
                value = (Structure) config.getValueAttr();
                try {
                    AbstractDataType dataType = value.getDataType(1);
                    flags = (BitString) dataType;
                } catch (IndexOutOfBoundsException e) {
                    throw new ProtocolException("Couldn't write configuration. Expected structure value of [" + OBISCODE_CONFIGURATION_OBJECT.toString() + "] to have 2 elements.");
                } catch (ClassCastException e) {
                    throw new ProtocolException("Couldn't write configuration. Expected second element of structure to be of type 'Bitstring', but was of type '" + value.getDataType(1).getClass().getSimpleName() + "'.");
                }

                flags.set(4 - type + newAuthLevel, enable);    //HLS5_P0 = bit9, HLS4_P0 = bit8, HLS3_P0 = bit7, HLS5_P3 = bit6, HLS4_P3 = bit5, HLS3_P3 = bit4
                config.setValueAttr(value);
            } catch (ClassCastException e) {
                throw new ProtocolException("Couldn't write configuration. Expected value of [" + OBISCODE_CONFIGURATION_OBJECT.toString() + "] to be of type 'Structure', but was of type '" + config.getValueAttr().getClass().getSimpleName() + "'.");
            }
        } else {
            throw new ProtocolException("Message contained an invalid authenticationLevel.");
        }
    }

    protected void upgradeFirmwareWithActivationDateAndImageIdentifier(OfflineDeviceMessage pendingMessage) throws IOException {
        String userFile = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateFileAttributeName).getDeviceMessageAttributeValue();
        String activationDate = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateActivationDateAttributeName).getDeviceMessageAttributeValue();
        String imageIdentifier = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateImageIdentifierAttributeName).getDeviceMessageAttributeValue(); // Will return empty string if the MessageAttribute could not be found
        byte[] image = ProtocolTools.getBytesFromHexString(userFile, "");


        ImageTransfer it = getCosemObjectFactory().getImageTransfer();
        if (isResume(pendingMessage)) {
            int lastTransferredBlockNumber = it.readFirstNotTransferedBlockNumber().intValue();
            if (lastTransferredBlockNumber > 0) {
                it.setStartIndex(lastTransferredBlockNumber - 1);
            }
        }

        it.setBooleanValue(getBooleanValue());
        it.setUsePollingVerifyAndActivate(true);    //Poll verification
        it.setPollingDelay(10000);
        it.setPollingRetries(30);
        it.setDelayBeforeSendingBlocks(5000);
        if (imageIdentifier.isEmpty()) {
            it.upgrade(image, false);
        } else {
            it.upgrade(image, false, imageIdentifier, false);
        }

        if (activationDate.isEmpty()) {
            try {
                it.setUsePollingVerifyAndActivate(false);   //Don't use polling for the activation!
                it.imageActivation();
            } catch (DataAccessResultException e) {
                if (isTemporaryFailure(e)) {
                    getProtocol().getLogger().log(Level.INFO, "Received temporary failure. Meter will activate the image when this communication session is closed, moving on.");
                } else {
                    throw e;
                }
            }
        } else {
            SingleActionSchedule sas = getCosemObjectFactory().getSingleActionSchedule(getMeterConfig().getImageActivationSchedule().getObisCode());
            Array dateArray = convertActivationDateEpochToDateTimeArray(activationDate);
            sas.writeExecutionTime(dateArray);
        }
    }

    /**
     * Convert the given epoch activation date to a proper DateTimeArray
     */
    protected Array convertActivationDateEpochToDateTimeArray(String strDate) {
        return super.convertEpochToDateTimeArray(strDate);
    }

    /**
     * Not supported in DSMR4.0, subclasses can override
     */
    protected boolean isResume(OfflineDeviceMessage pendingMessage) {
        return false;
    }

    protected boolean isTemporaryFailure(DataAccessResultException e) {
        return (e.getDataAccessResult() == DataAccessResultCode.TEMPORARY_FAILURE.getResultCode());
    }

    /**
     * Default value, subclasses can override. This value is used to set the image_transfer_enable attribute.
     */
    protected int getBooleanValue() {
        return 0xFF;
    }

    /**
     * Sort the given list of device messages cause security messages 'enable/disable P0/P3 level' should be executed in the right order.<br></br>
     * The security messages will be sorted & placed at the beginning of the list, so when afterwards looping over the list
     * they will be picked up/executed in the correct order.
     */
    private List<OfflineDeviceMessage> sortSecurityRelatedDeviceMessages(List<OfflineDeviceMessage> deviceMessages) {
        List<OfflineDeviceMessage> sortedDeviceMessages = new ArrayList<>(deviceMessages.size());

        for (OfflineDeviceMessage deviceMessage : deviceMessages) {
            if (isEnableP0(deviceMessage)) {
                sortedDeviceMessages.add(deviceMessage);
            }
        }
        for (OfflineDeviceMessage deviceMessage : deviceMessages) {
            if (isEnableP3(deviceMessage)) {
                sortedDeviceMessages.add(deviceMessage);
            }
        }
        for (OfflineDeviceMessage deviceMessage : deviceMessages) {
            if (isDisableP0(deviceMessage)) {
                sortedDeviceMessages.add(deviceMessage);
            }
        }
        for (OfflineDeviceMessage deviceMessage : deviceMessages) {
            if (isDisableP3(deviceMessage)) {
                sortedDeviceMessages.add(deviceMessage);
            }
        }

        // Add all other messages - the order of these is not important
        for (OfflineDeviceMessage deviceMessage : deviceMessages) {
            if (!sortedDeviceMessages.contains(deviceMessage)) {
                sortedDeviceMessages.add(deviceMessage);
            }
        }
        return sortedDeviceMessages;
    }

    private boolean isDisableP3(OfflineDeviceMessage deviceMessage) {
        return deviceMessage.getDeviceMessageId().equals(DeviceMessageId.SECURITY_DISABLE_DLMS_AUTHENTICATION_LEVEL_P3);
    }

    private boolean isDisableP0(OfflineDeviceMessage deviceMessage) {
        return deviceMessage.getDeviceMessageId().equals(DeviceMessageId.SECURITY_DISABLE_DLMS_AUTHENTICATION_LEVEL_P0);
    }

    private boolean isEnableP3(OfflineDeviceMessage deviceMessage) {
        return deviceMessage.getDeviceMessageId().equals(DeviceMessageId.SECURITY_ENABLE_DLMS_AUTHENTICATION_LEVEL_P3);
    }

    private boolean isEnableP0(OfflineDeviceMessage deviceMessage) {
        return deviceMessage.getDeviceMessageId().equals(DeviceMessageId.SECURITY_ENABLE_DLMS_AUTHENTICATION_LEVEL_P0);
    }
}