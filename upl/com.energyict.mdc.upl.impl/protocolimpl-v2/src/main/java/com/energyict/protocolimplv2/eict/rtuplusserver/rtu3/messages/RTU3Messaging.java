package com.energyict.protocolimplv2.eict.rtuplusserver.rtu3.messages;

import com.energyict.cbo.ApplicationException;
import com.energyict.cbo.Password;
import com.energyict.cbo.TimeDuration;
import com.energyict.cpo.PropertySpec;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.*;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.messages.DeviceMessageStatus;
import com.energyict.mdc.meterdata.CollectedMessage;
import com.energyict.mdc.meterdata.CollectedMessageList;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdc.protocol.tasks.support.DeviceMessageSupport;
import com.energyict.mdw.core.UserFile;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocolimpl.base.Base64EncoderDecoder;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.messages.PLCConfigurationDeviceMessageExecutor;
import com.energyict.protocolimplv2.eict.rtuplusserver.rtu3.RTU3;
import com.energyict.protocolimplv2.eict.rtuplusserver.rtu3.messages.firmwareobjects.BroadcastUpgrade;
import com.energyict.protocolimplv2.eict.rtuplusserver.rtu3.messages.firmwareobjects.DeviceInfoSerializer;
import com.energyict.protocolimplv2.eict.rtuplusserver.rtu3.messages.syncobjects.MasterDataSerializer;
import com.energyict.protocolimplv2.eict.rtuplusserver.rtu3.messages.syncobjects.MasterDataSync;
import com.energyict.protocolimplv2.messages.*;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.enums.DlmsAuthenticationLevelMessageValues;
import com.energyict.protocolimplv2.messages.enums.DlmsEncryptionLevelMessageValues;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 22/06/2015 - 9:53
 */
public class RTU3Messaging extends AbstractMessageExecutor implements DeviceMessageSupport {

    private final static List<DeviceMessageSpec> supportedMessages;
    private static final String TEMP_DIR = "java.io.tmpdir";

    private static final ObisCode DEVICE_NAME_OBISCODE = ObisCode.fromString("0.0.128.0.9.255");
    private static final ObisCode EVENT_NOTIFICATION_OBISCODE = ObisCode.fromString("0.0.128.0.12.255");

    static {
        supportedMessages = new ArrayList<>();
        supportedMessages.add(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS);

        supportedMessages.add(DeviceActionMessage.SyncMasterdataForDC);
        supportedMessages.add(DeviceActionMessage.SyncDeviceDataForDC);
        supportedMessages.add(DeviceActionMessage.PauseDCScheduler);
        supportedMessages.add(DeviceActionMessage.ResumeDCScheduler);
        supportedMessages.add(DeviceActionMessage.SyncOneConfigurationForDC);

        supportedMessages.add(PLCConfigurationDeviceMessage.PingMeter);

        supportedMessages.add(FirmwareDeviceMessage.BroadcastFirmwareUpgrade);
        supportedMessages.add(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_IMAGE_IDENTIFIER);

        supportedMessages.add(SecurityMessage.CHANGE_DLMS_AUTHENTICATION_LEVEL);
        supportedMessages.add(SecurityMessage.ACTIVATE_DLMS_ENCRYPTION);
        supportedMessages.add(SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEYS);
        supportedMessages.add(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEYS);
        supportedMessages.add(SecurityMessage.CHANGE_HLS_SECRET_PASSWORD);

        supportedMessages.add(UplinkConfigurationDeviceMessage.EnableUplinkPing);
        supportedMessages.add(UplinkConfigurationDeviceMessage.WriteUplinkPingDestinationAddress);
        supportedMessages.add(UplinkConfigurationDeviceMessage.WriteUplinkPingInterval);
        supportedMessages.add(UplinkConfigurationDeviceMessage.WriteUplinkPingTimeout);
        //supportedMessages.add(PPPConfigurationDeviceMessage.SetPPPIdleTime);
        //supportedMessages.add(NetworkConnectivityMessage.PreferGPRSUpstreamCommunication);
        supportedMessages.add(NetworkConnectivityMessage.EnableModemWatchdog);
        supportedMessages.add(NetworkConnectivityMessage.SetModemWatchdogParameters2);
        //supportedMessages.add(ConfigurationChangeDeviceMessage.EnableSSL);
        supportedMessages.add(AlarmConfigurationMessage.CONFIGURE_PUSH_EVENT_NOTIFICATION);
        supportedMessages.add(AlarmConfigurationMessage.ENABLE_EVENT_NOTIFICATIONS);
        supportedMessages.add(ConfigurationChangeDeviceMessage.SetDeviceName);
        supportedMessages.add(ConfigurationChangeDeviceMessage.SetNTPAddress);
        supportedMessages.add(ConfigurationChangeDeviceMessage.SyncNTPServer);
        supportedMessages.add(DeviceActionMessage.RebootApplication);
        supportedMessages.add(FirewallConfigurationMessage.ActivateFirewall);
        supportedMessages.add(FirewallConfigurationMessage.DeactivateFirewall);
        supportedMessages.add(FirewallConfigurationMessage.ConfigureFWGPRS);
        supportedMessages.add(FirewallConfigurationMessage.ConfigureFWLAN);
        supportedMessages.add(FirewallConfigurationMessage.ConfigureFWWAN);
        supportedMessages.add(SecurityMessage.CHANGE_WEBPORTAL_PASSWORD1);
        supportedMessages.add(SecurityMessage.CHANGE_WEBPORTAL_PASSWORD2);

        supportedMessages.add(PLCConfigurationDeviceMessage.SetMaxNumberOfHopsAttributeName);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetWeakLQIValueAttributeName);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetSecurityLevel);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetRoutingConfiguration);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetBroadCastLogTableEntryTTLAttributeName);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetMaxJoinWaitTime);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetPathDiscoveryTime);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetMetricType);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetPanId);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetTMRTTL);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetMaxFrameRetries);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetNeighbourTableEntryTTL);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetHighPriorityWindowSize);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetCSMAFairnessLimit);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetBeaconRandomizationWindowLength);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetMacA);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetMacK);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetMinimumCWAttempts);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetMaxBe);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetMaxCSMABackOff);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetMinBe);

        supportedMessages.add(PLCConfigurationDeviceMessage.SetAutomaticRouteManagement);
        supportedMessages.add(PLCConfigurationDeviceMessage.EnableKeepAlive);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetKeepAliveScheduleInterval);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetKeepAliveBucketSize);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetMinInactiveMeterTime);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetMaxInactiveMeterTime);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetKeepAliveRetries);
        supportedMessages.add(PLCConfigurationDeviceMessage.SetKeepAliveTimeout);
        supportedMessages.add(PLCConfigurationDeviceMessage.EnableG3PLCInterface);
    }

    private MasterDataSync masterDataSync;
    private PLCConfigurationDeviceMessageExecutor plcConfigurationDeviceMessageExecutor = null;

    public RTU3Messaging(RTU3 protocol) {
        super(protocol);
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return supportedMessages;
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(DeviceMessageConstants.dcDeviceIDAttributeName)) {
            return MasterDataSerializer.serializeMasterData(messageAttribute);
        } else if (propertySpec.getName().equals(DeviceMessageConstants.deviceConfigurationIDAttributeName)) {
            return MasterDataSerializer.serializeMasterDataForOneConfig(messageAttribute);
        } else if (propertySpec.getName().equals(DeviceMessageConstants.dcDeviceID2AttributeName)) {
            return MasterDataSerializer.serializeMeterDetails(messageAttribute);
        } else if (propertySpec.getName().equals(DeviceMessageConstants.broadcastEncryptionKeyAttributeName)
                || propertySpec.getName().equals(DeviceMessageConstants.passwordAttributeName)
                || propertySpec.getName().equals(DeviceMessageConstants.broadcastAuthenticationKeyAttributeName)
                || propertySpec.getName().equals(DeviceMessageConstants.newAuthenticationKeyAttributeName)
                || propertySpec.getName().equals(DeviceMessageConstants.newPasswordAttributeName)
                || propertySpec.getName().equals(DeviceMessageConstants.newEncryptionKeyAttributeName)) {
            return ((Password) messageAttribute).getValue();
        } else if (propertySpec.getName().equals(DeviceMessageConstants.broadcastDevicesGroupAttributeName)) {
            return DeviceInfoSerializer.serializeDeviceInfo(messageAttribute);
        } else if (propertySpec.getName().equals(DeviceMessageConstants.broadcastInitialTimeBetweenBlocksAttributeName)
                || propertySpec.getName().equals(DeviceMessageConstants.timeout)) {
            return String.valueOf(((TimeDuration) messageAttribute).getMilliSeconds()); //Return value in ms
        } else if (propertySpec.getName().equals(DeviceMessageConstants.modemWatchdogInterval)
                || propertySpec.getName().equals(DeviceMessageConstants.modemWatchdogInitialDelay)
                || propertySpec.getName().equals(DeviceMessageConstants.PPPDaemonResetThreshold)
                || propertySpec.getName().equals(DeviceMessageConstants.modemResetThreshold)
                || propertySpec.getName().equals(DeviceMessageConstants.systemRebootThreshold)
                || propertySpec.getName().equals(DeviceMessageConstants.broadCastLogTableEntryTTLAttributeName)) {
            return String.valueOf(((TimeDuration) messageAttribute).getSeconds()); //Return value in seconds
        } else if (propertySpec.getName().equals(DeviceMessageConstants.firmwareUpdateUserFileAttributeName)) {

            //final UserFile userFile = (UserFile) messageAttribute;
            //final byte[] bytes = userFile.loadFileInByteArray();
            //final String fileName = System.getProperty(TEMP_DIR) + userFile.getFileName() + "_" + userFile.getId();

            //final File testFile = new File(fileName);
            //if (testFile.exists()) {
            //    if (testFile.length() != bytes.length) {
            //        if (testFile.delete()) {
            //            ProtocolTools.writeBytesToFile(fileName, bytes, false);
            //        } else {
            //            throw MdcManager.getComServerExceptionFactory().createUnExpectedProtocolError(new IOException("Could not write file '" + userFile.getFileName() + "' to temp directory, it already exists but is still in use"));
            //        }
            //    }   //Else: file exists and has the correct content. Ok, continue to execute message.
            //} else {
            //    ProtocolTools.writeBytesToFile(fileName, bytes, false);
            //}
            //System.gc();    //Remove the in memory byte array


            final byte[] data = ((UserFile) messageAttribute).loadFileInByteArray();
            return new Base64EncoderDecoder().encode(data, true);
        } else if (propertySpec.getName().equals(DeviceMessageConstants.encryptionLevelAttributeName)) {
            return String.valueOf(DlmsEncryptionLevelMessageValues.getValueFor(messageAttribute.toString()));
        } else if (propertySpec.getName().equals(DeviceMessageConstants.authenticationLevelAttributeName)) {
            return String.valueOf(DlmsAuthenticationLevelMessageValues.getValueFor(messageAttribute.toString()));
        } else {
            return messageAttribute.toString();     //Works for BigDecimal, boolean and (hex)string property specs
        }
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList result = MdcManager.getCollectedDataFactory().createCollectedMessageList(pendingMessages);

        for (OfflineDeviceMessage pendingMessage : pendingMessages) {
            CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);   //Optimistic
            try {
                boolean plcMessageExecuted = getPLCConfigurationDeviceMessageExecutor().executePendingMessage(pendingMessage, collectedMessage);
                if (!plcMessageExecuted) { // if it was not a PLC message
                    if (pendingMessage.getSpecification().equals(DeviceActionMessage.SyncMasterdataForDC)) {
                        collectedMessage = getMasterDataSync().syncMasterData(pendingMessage, collectedMessage);
                    } else if (pendingMessage.getSpecification().equals(DeviceActionMessage.SyncOneConfigurationForDC)) {
                        collectedMessage = getMasterDataSync().syncMasterData(pendingMessage, collectedMessage);
                    } else if (pendingMessage.getSpecification().equals(DeviceActionMessage.SyncDeviceDataForDC)) {
                        collectedMessage = getMasterDataSync().syncDeviceData(pendingMessage, collectedMessage);
                    } else if (pendingMessage.getSpecification().equals(DeviceActionMessage.PauseDCScheduler)) {
                        setSchedulerState(SchedulerState.PAUSED);
                    } else if (pendingMessage.getSpecification().equals(DeviceActionMessage.ResumeDCScheduler)) {
                        setSchedulerState(SchedulerState.RUNNING);
                    } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS)) {
                        changeGPRSParameters(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.PingMeter)) {
                        collectedMessage = pingMeter(pendingMessage, collectedMessage);
                    } else if (pendingMessage.getSpecification().equals(FirmwareDeviceMessage.BroadcastFirmwareUpgrade)) {
                        collectedMessage = new BroadcastUpgrade(this).broadcastFirmware(pendingMessage, collectedMessage);
                    } else if (pendingMessage.getSpecification().equals(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_IMAGE_IDENTIFIER)) {
                        upgradeFirmware(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_WEBPORTAL_PASSWORD1)) {
                        changePasswordUser1(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_WEBPORTAL_PASSWORD2)) {
                        changePasswordUser2(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(UplinkConfigurationDeviceMessage.EnableUplinkPing)) {
                        enableUplinkPing(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(UplinkConfigurationDeviceMessage.WriteUplinkPingDestinationAddress)) {
                        writeUplinkPingDestinationAddress(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(UplinkConfigurationDeviceMessage.WriteUplinkPingInterval)) {
                        writeUplinkPingInterval(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(UplinkConfigurationDeviceMessage.WriteUplinkPingTimeout)) {
                        writeUplinkPingTimeout(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.EnableModemWatchdog)) {
                        enableModemWatchdog(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.SetModemWatchdogParameters2)) {
                        setModemWatchdogParameters(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(AlarmConfigurationMessage.ENABLE_EVENT_NOTIFICATIONS)) {
                        enableEventNotifications(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(AlarmConfigurationMessage.CONFIGURE_PUSH_EVENT_NOTIFICATION)) {
                        configurePushEventNotification(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(DeviceActionMessage.REBOOT_DEVICE)) {
                        rebootDevice(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(DeviceActionMessage.RebootApplication)) {
                        rebootApplication(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.SetDeviceName)) {
                        setDeviceName(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.SetNTPAddress)) {
                        setNTPAddress(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.SyncNTPServer)) {
                        syncNTPServer(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_DLMS_AUTHENTICATION_LEVEL)) {
                        changeDlmAuthLevel(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(SecurityMessage.ACTIVATE_DLMS_ENCRYPTION)) {
                        activateDlmsEncryption(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEYS)) {
                        changeAuthKey(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEYS)) {
                        changeEncryptionKey(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_HLS_SECRET_PASSWORD)) {
                        changeHlsSecret(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(FirewallConfigurationMessage.ActivateFirewall)) {
                        activateFirewall(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(FirewallConfigurationMessage.DeactivateFirewall)) {
                        deactivateFirewall(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(FirewallConfigurationMessage.ConfigureFWGPRS)) {
                        configureFWGPRS(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(FirewallConfigurationMessage.ConfigureFWLAN)) {
                        configureFWLAN(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(FirewallConfigurationMessage.ConfigureFWWAN)) {
                        configureFWWAN(pendingMessage);
                    } else {   //Unsupported message
                        collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                        collectedMessage.setDeviceProtocolInformation("Message currently not supported by the protocol");
                        collectedMessage.setFailureInformation(ResultType.NotSupported, createUnsupportedWarning(pendingMessage));
                    }
                }
            } catch (IOException e) {
                if (IOExceptionHandler.isUnexpectedResponse(e, getProtocol().getDlmsSession())) {
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setDeviceProtocolInformation(e.getMessage());
                    collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
                }   //Else: throw communication exception
            } catch (IndexOutOfBoundsException | NumberFormatException | NullPointerException | ApplicationException e) {
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                collectedMessage.setDeviceProtocolInformation(e.toString());
                collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
            } finally {
                result.addCollectedMessage(collectedMessage);
            }
        }

        return result;
    }

    private PLCConfigurationDeviceMessageExecutor getPLCConfigurationDeviceMessageExecutor() {
        if (plcConfigurationDeviceMessageExecutor == null) {
            plcConfigurationDeviceMessageExecutor = new PLCConfigurationDeviceMessageExecutor(getProtocol().getDlmsSession(), getProtocol().getOfflineDevice());
        }
        return plcConfigurationDeviceMessageExecutor;
    }

    private void configureFWWAN(OfflineDeviceMessage pendingMessage) throws IOException {
        boolean isDLMSAllowed = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.EnableDLMS).getDeviceMessageAttributeValue());
        boolean isHTTPAllowed = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.EnableHTTP).getDeviceMessageAttributeValue());
        boolean isSSHAllowed = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.EnableSSH).getDeviceMessageAttributeValue());
        getCosemObjectFactory().getFirewallSetup().setWANPortSetup(new FirewallSetup.InterfaceFirewallConfiguration(isDLMSAllowed, isHTTPAllowed, isSSHAllowed));
    }

    private void configureFWLAN(OfflineDeviceMessage pendingMessage) throws IOException {
        boolean isDLMSAllowed = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.EnableDLMS).getDeviceMessageAttributeValue());
        boolean isHTTPAllowed = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.EnableHTTP).getDeviceMessageAttributeValue());
        boolean isSSHAllowed = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.EnableSSH).getDeviceMessageAttributeValue());
        getCosemObjectFactory().getFirewallSetup().setLANPortSetup(new FirewallSetup.InterfaceFirewallConfiguration(isDLMSAllowed, isHTTPAllowed, isSSHAllowed));
    }

    private void configureFWGPRS(OfflineDeviceMessage pendingMessage) throws IOException {
        boolean isDLMSAllowed = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.EnableDLMS).getDeviceMessageAttributeValue());
        boolean isHTTPAllowed = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.EnableHTTP).getDeviceMessageAttributeValue());
        boolean isSSHAllowed = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.EnableSSH).getDeviceMessageAttributeValue());
        getCosemObjectFactory().getFirewallSetup().setGPRSPortSetup(new FirewallSetup.InterfaceFirewallConfiguration(isDLMSAllowed, isHTTPAllowed, isSSHAllowed));
    }

    private void deactivateFirewall(OfflineDeviceMessage pendingMessage) throws IOException {
        getCosemObjectFactory().getFirewallSetup().deactivate();
    }

    private void activateFirewall(OfflineDeviceMessage pendingMessage) throws IOException {
        getCosemObjectFactory().getFirewallSetup().activate();
    }

    private void changeGPRSParameters(OfflineDeviceMessage pendingMessage) throws IOException {
        String userName = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, usernameAttributeName).getDeviceMessageAttributeValue();
        String password = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, passwordAttributeName).getDeviceMessageAttributeValue();
        String apn = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, apnAttributeName).getDeviceMessageAttributeValue();
        writeGprsSettings(userName, password);
        if (apn != null) {
            getCosemObjectFactory().getGPRSModemSetup().writeAPN(apn);
        }
    }

    private void writeGprsSettings(String userName, String password) throws IOException {
        PPPSetup.PPPAuthenticationType pppat = getCosemObjectFactory().getPPPSetup().new PPPAuthenticationType();
        pppat.setAuthenticationType(PPPSetup.LCPOptionsType.AUTH_PAP);
        if (userName != null) {
            pppat.setUserName(userName);
        }
        if (password != null) {
            pppat.setPassWord(password);
        }
        if ((userName != null) || (password != null)) {
            getCosemObjectFactory().getPPPSetup().writePPPAuthenticationType(pppat);
        }
    }

    private CollectedMessage pingMeter(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        final String hexMacAddress = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.macAddress).getDeviceMessageAttributeValue();
        final long timeoutInMillis = Long.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.timeout).getDeviceMessageAttributeValue());

        try {
            ProtocolTools.getBytesFromHexString(hexMacAddress, "");
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            throw MdcManager.getComServerExceptionFactory().createInvalidPropertyFormatException("MAC address", hexMacAddress, "Should be 16 hex characters");
        }

        final long normalTimeout = getProtocol().getDlmsSessionProperties().getTimeout();
        final long fullRoundTripTimeout = timeoutInMillis + normalTimeout;
        getProtocol().getDlmsSession().getDLMSConnection().setTimeout(fullRoundTripTimeout);
        final int pingTime = getCosemObjectFactory().getG3NetworkManagement().pingNode(hexMacAddress, ((int) timeoutInMillis) / 1000);
        getProtocol().getDlmsSession().getDLMSConnection().setTimeout(normalTimeout);

        collectedMessage.setDeviceProtocolInformation(pingTime + " ms");
        return collectedMessage;
    }

    private MasterDataSync getMasterDataSync() {
        if (masterDataSync == null) {
            masterDataSync = new MasterDataSync(this);
        }
        return masterDataSync;
    }

    private void setSchedulerState(SchedulerState state) throws IOException {
        getCosemObjectFactory().getScheduleManager().writeSchedulerState(state.toDLMSEnum());
    }

    private void upgradeFirmware(OfflineDeviceMessage pendingMessage) throws IOException {

        String b64EncodedImage = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateUserFileAttributeName).getDeviceMessageAttributeValue();
        String imageIdentifier = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateImageIdentifierAttributeName).getDeviceMessageAttributeValue(); // Will return empty string if the MessageAttribute could not be found
        byte[] image = new Base64EncoderDecoder().decode(b64EncodedImage);

        ImageTransfer it = getCosemObjectFactory().getImageTransfer();

        it.setUsePollingVerifyAndActivate(true);    //Poll verification
        it.setPollingDelay(10000);
        it.setPollingRetries(30);
        it.setDelayBeforeSendingBlocks(5000);
        it.upgrade(image, false, imageIdentifier, true);

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
    }

    private boolean isTemporaryFailure(DataAccessResultException e) {
        return (e.getDataAccessResult() == DataAccessResultCode.TEMPORARY_FAILURE.getResultCode());
    }

    private void changeHlsSecret(OfflineDeviceMessage pendingMessage) throws IOException {
        String hex = pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue();
        final CosemObjectFactory cof = getCosemObjectFactory();
        cof.getAssociationLN().changeHLSSecret(ProtocolTools.getBytesFromHexString(hex, ""));
    }

    protected void changeEncryptionKey(OfflineDeviceMessage pendingMessage) throws IOException {
        String wrappedHexKey = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.newWrappedEncryptionKeyAttributeName).getDeviceMessageAttributeValue();
        String plainHexKey = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.newEncryptionKeyAttributeName).getDeviceMessageAttributeValue();
        String oldHexKey = ProtocolTools.getHexStringFromBytes(getProtocol().getDlmsSession().getProperties().getSecurityProvider().getGlobalKey(), "");

        Array encryptionKeyArray = new Array();
        Structure keyData = new Structure();
        keyData.addDataType(new TypeEnum(0));    // 0 means keyType: encryptionKey (global key)
        keyData.addDataType(OctetString.fromByteArray(ProtocolTools.getBytesFromHexString(wrappedHexKey)));
        encryptionKeyArray.addDataType(keyData);
        getSecuritySetup().transferGlobalKey(encryptionKeyArray);

        //Update the key in the security provider, it is used instantly
        getProtocol().getDlmsSession().getProperties().getSecurityProvider().changeEncryptionKey(ProtocolTools.getBytesFromHexString(plainHexKey));

        //Reset frame counter, only if a different key has been written
        if (!oldHexKey.equalsIgnoreCase(plainHexKey)) {
            getProtocol().getDlmsSession().getAso().getSecurityContext().setFrameCounter(1);
        }
    }

    protected SecuritySetup getSecuritySetup() throws IOException {
        return getCosemObjectFactory().getSecuritySetup();
    }

    protected void changeAuthKey(OfflineDeviceMessage pendingMessage) throws IOException {
        String wrappedHexKey = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.newWrappedAuthenticationKeyAttributeName).getDeviceMessageAttributeValue();
        String plainHexKey = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.newAuthenticationKeyAttributeName).getDeviceMessageAttributeValue();

        Array authenticationKeyArray = new Array();
        Structure keyData = new Structure();
        keyData.addDataType(new TypeEnum(2));    // 2 means keyType: authenticationKey
        keyData.addDataType(OctetString.fromByteArray(ProtocolTools.getBytesFromHexString(wrappedHexKey)));
        authenticationKeyArray.addDataType(keyData);
        getSecuritySetup().transferGlobalKey(authenticationKeyArray);

        //Update the key in the security provider, it is used instantly
        getProtocol().getDlmsSession().getProperties().getSecurityProvider().changeAuthenticationKey(ProtocolTools.getBytesFromHexString(plainHexKey));
    }

    private void activateDlmsEncryption(OfflineDeviceMessage pendingMessage) throws IOException {
        getSecuritySetup().activateSecurity(new TypeEnum(getSingleIntegerAttribute(pendingMessage)));
    }

    private void changeDlmAuthLevel(OfflineDeviceMessage pendingMessage) throws IOException {
        int newAuthLevel = getSingleIntegerAttribute(pendingMessage);

        AssociationLN associationLN = getCosemObjectFactory().getAssociationLN();
        AbstractDataType authMechanismName = associationLN.readAuthenticationMechanismName();
        if (authMechanismName.isOctetString()) {
            byte[] octets = ((OctetString) authMechanismName).getOctetStr();
            if (octets[octets.length - 1] != newAuthLevel) {
                octets[octets.length - 1] = (byte) newAuthLevel;
                associationLN.writeAuthenticationMechanismName(new OctetString(octets, 0));
            } else {
                throw new ProtocolException("New authenticationLevel is the same as the one that is already configured in the device, new level will not be written.");
            }
        } else if (authMechanismName.isStructure()) {
            Structure structure = (Structure) authMechanismName;
            Unsigned8 currentLevel = (Unsigned8) structure.getDataType(structure.nrOfDataTypes() - 1);
            if (currentLevel.intValue() != newAuthLevel) {
                structure.setDataType(structure.nrOfDataTypes() - 1, new Unsigned8(newAuthLevel));
                associationLN.writeAuthenticationMechanismName(structure);
            } else {
                throw new ProtocolException("New authenticationLevel is the same as the one that is already configured in the device, new level will not be written.");
            }
        } else {
            throw new ProtocolException("Returned AuthenticationMechanismName is not of the type OctetString or Structure, cannot write new value");
        }
    }

    private void setNTPAddress(OfflineDeviceMessage pendingMessage) throws IOException {
        String address = pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue();
        getCosemObjectFactory().getNTPServerAddress().writeNTPServerName(address);
    }

    private void syncNTPServer(OfflineDeviceMessage pendingMessage) throws IOException {
        getCosemObjectFactory().getNTPServerAddress().ntpSync();
    }

    private void setDeviceName(OfflineDeviceMessage pendingMessage) throws IOException {
        String name = pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue();
        getCosemObjectFactory().getData(DEVICE_NAME_OBISCODE).setValueAttr(OctetString.fromString(name));
    }

    private void rebootApplication(OfflineDeviceMessage pendingMessage) throws IOException {
        getCosemObjectFactory().getLifeCycleManagement().restartApplication();
    }

    private void rebootDevice(OfflineDeviceMessage pendingMessage) throws IOException {
        getCosemObjectFactory().getLifeCycleManagement().rebootDevice();
    }

    private void enableEventNotifications(OfflineDeviceMessage pendingMessage) throws IOException {
        boolean enable = Boolean.parseBoolean(pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());
        GenericInvoke genericInvoke = getCosemObjectFactory().getGenericInvoke(EVENT_NOTIFICATION_OBISCODE, DLMSClassId.EVENT_NOTIFICATION.getClassId(), 1);
        genericInvoke.invoke(new BooleanObject(enable).getBEREncodedByteArray());
    }

    private void setModemWatchdogParameters(OfflineDeviceMessage pendingMessage) throws IOException {

        //Note that all values here are expressed in seconds
        int modemWatchdogInterval = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.modemWatchdogInterval).getDeviceMessageAttributeValue());
        int modemWatchdogInitialDelay = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.modemWatchdogInitialDelay).getDeviceMessageAttributeValue());
        int pppDaemonResetThreshold = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.PPPDaemonResetThreshold).getDeviceMessageAttributeValue());
        int modemResetThreshold = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.modemResetThreshold).getDeviceMessageAttributeValue());
        int systemRebootThreshold = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.systemRebootThreshold).getDeviceMessageAttributeValue());

        getCosemObjectFactory().getModemWatchdogConfiguration().writeExtendedConfigParameters(
                modemWatchdogInterval,
                modemWatchdogInitialDelay,
                pppDaemonResetThreshold,
                modemResetThreshold,
                systemRebootThreshold
        );
    }

    private void enableModemWatchdog(OfflineDeviceMessage pendingMessage) throws IOException {
        boolean enable = Boolean.parseBoolean(pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());
        getCosemObjectFactory().getModemWatchdogConfiguration().enableWatchdog(enable);
    }

    private void configurePushEventNotification(OfflineDeviceMessage pendingMessage) throws IOException {
        String transportTypeString = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.transportTypeAttributeName).getDeviceMessageAttributeValue();
        int transportType = AlarmConfigurationMessage.TransportType.valueOf(transportTypeString).getId();

        String destinationAddress = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.destinationAddressAttributeName).getDeviceMessageAttributeValue();

        String messageTypeString = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.messageTypeAttributeName).getDeviceMessageAttributeValue();
        int messageType = AlarmConfigurationMessage.MessageType.valueOf(messageTypeString).getId();

        getCosemObjectFactory().getEventPushNotificationConfig().writeSendDestinationAndMethod(transportType, destinationAddress, messageType);
    }

    private void writeUplinkPingTimeout(OfflineDeviceMessage pendingMessage) throws IOException {
        Integer timeout = Integer.valueOf(pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());
        getCosemObjectFactory().getUplinkPingConfiguration().writeTimeout(timeout);
    }

    private void changePasswordUser1(OfflineDeviceMessage pendingMessage) throws IOException {
        String newPassword = pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue();
        getCosemObjectFactory().getWebPortalPasswordConfig().changeUser1Password(newPassword);
    }

    private void changePasswordUser2(OfflineDeviceMessage pendingMessage) throws IOException {
        String newPassword = pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue();
        getCosemObjectFactory().getWebPortalPasswordConfig().changeUser2Password(newPassword);
    }

    private void writeUplinkPingInterval(OfflineDeviceMessage pendingMessage) throws IOException {
        Integer interval = Integer.valueOf(pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());
        getCosemObjectFactory().getUplinkPingConfiguration().writeInterval(interval);
    }

    private void writeUplinkPingDestinationAddress(OfflineDeviceMessage pendingMessage) throws IOException {
        String destinationAddress = pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue();
        getCosemObjectFactory().getUplinkPingConfiguration().writeDestAddress(destinationAddress);
    }

    private void enableUplinkPing(OfflineDeviceMessage pendingMessage) throws IOException {
        boolean enable = Boolean.parseBoolean(pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());
        getCosemObjectFactory().getUplinkPingConfiguration().enableUplinkPing(enable);
    }

    private int getSingleIntegerAttribute(OfflineDeviceMessage pendingMessage) {
        return Integer.parseInt(pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());
    }
}