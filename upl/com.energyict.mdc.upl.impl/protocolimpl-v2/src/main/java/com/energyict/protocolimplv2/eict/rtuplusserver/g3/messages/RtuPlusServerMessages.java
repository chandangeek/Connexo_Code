package com.energyict.protocolimplv2.eict.rtuplusserver.g3.messages;

import com.energyict.mdc.protocol.LegacyProtocolProperties;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.Password;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.tasks.support.DeviceMessageSupport;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.AssociationLN;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.DataAccessResultCode;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.Disconnector;
import com.energyict.dlms.cosem.FirewallSetup;
import com.energyict.dlms.cosem.GenericInvoke;
import com.energyict.dlms.cosem.GenericWrite;
import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.dlms.cosem.SecuritySetup;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.Group;
import com.energyict.mdw.core.UserFile;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.base.Base64EncoderDecoder;
import com.energyict.protocolimpl.dlms.idis.xml.XMLParser;
import com.energyict.protocolimpl.properties.Temporals;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierById;
import com.energyict.protocolimplv2.identifiers.DeviceMessageIdentifierById;
import com.energyict.protocolimplv2.messages.AlarmConfigurationMessage;
import com.energyict.protocolimplv2.messages.ClockDeviceMessage;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.FirewallConfigurationMessage;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.GeneralDeviceMessage;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;
import com.energyict.protocolimplv2.messages.OutputConfigurationMessage;
import com.energyict.protocolimplv2.messages.PLCConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.PPPConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.protocolimplv2.messages.UplinkConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.enums.DlmsAuthenticationLevelMessageValues;
import com.energyict.protocolimplv2.messages.enums.DlmsEncryptionLevelMessageValues;

import java.io.IOException;
import java.text.ParseException;
import java.time.temporal.TemporalAmount;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateImageIdentifierAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateUserFileAttributeName;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 17/06/2014 - 15:00
 */
public class RtuPlusServerMessages implements DeviceMessageSupport {

    private static final ObisCode DEVICE_NAME_OBISCODE = ObisCode.fromString("0.0.128.0.9.255");
    private static final ObisCode EVENT_NOTIFICATION_OBISCODE = ObisCode.fromString("0.0.128.0.12.255");
    protected final DlmsSession session;
    private final OfflineDevice offlineDevice;
    private PLCConfigurationDeviceMessageExecutor plcConfigurationDeviceMessageExecutor;
    private final CollectedDataFactory collectedDataFactory;
    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;
    private final Converter converter;
    private final IssueFactory issueFactory;

    public RtuPlusServerMessages(DlmsSession session, OfflineDevice offlineDevice, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        this.session = session;
        this.offlineDevice = offlineDevice;
        this.collectedDataFactory = collectedDataFactory;
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
        this.converter = converter;
        this.issueFactory = issueFactory;
    }

    public List<DeviceMessageSpec> getSupportedMessages() {
        return Arrays.asList(
            PLCConfigurationDeviceMessage.SetMaxNumberOfHopsAttributeName.get(this.propertySpecService, this.nlsService, this.converter),
            PLCConfigurationDeviceMessage.SetWeakLQIValueAttributeName.get(this.propertySpecService, this.nlsService, this.converter),
            PLCConfigurationDeviceMessage.SetSecurityLevel.get(this.propertySpecService, this.nlsService, this.converter),
            PLCConfigurationDeviceMessage.SetRoutingConfiguration.get(this.propertySpecService, this.nlsService, this.converter),
            PLCConfigurationDeviceMessage.SetBroadCastLogTableEntryTTLAttributeName.get(this.propertySpecService, this.nlsService, this.converter),
            PLCConfigurationDeviceMessage.SetMaxJoinWaitTime.get(this.propertySpecService, this.nlsService, this.converter),
            PLCConfigurationDeviceMessage.SetPathDiscoveryTime.get(this.propertySpecService, this.nlsService, this.converter),
            PLCConfigurationDeviceMessage.SetMetricType.get(this.propertySpecService, this.nlsService, this.converter),
            //PLCConfigurationDeviceMessage.SetCoordShortAddress.get(this.propertySpecService, this.nlsService, this.converter),
            //PLCConfigurationDeviceMessage.SetDisableDefaultRouting.get(this.propertySpecService, this.nlsService, this.converter),
            //PLCConfigurationDeviceMessage.SetDeviceType.get(this.propertySpecService, this.nlsService, this.converter),

            //PLCConfigurationDeviceMessage.ResetPlcOfdmMacCounters.get(this.propertySpecService, this.nlsService, this.converter),

            PLCConfigurationDeviceMessage.SetPanId.get(this.propertySpecService, this.nlsService, this.converter),
            //PLCConfigurationDeviceMessage.SetToneMaskAttributeName.get(this.propertySpecService, this.nlsService, this.converter),
            PLCConfigurationDeviceMessage.SetTMRTTL.get(this.propertySpecService, this.nlsService, this.converter),
            PLCConfigurationDeviceMessage.SetMaxFrameRetries.get(this.propertySpecService, this.nlsService, this.converter),
            PLCConfigurationDeviceMessage.SetNeighbourTableEntryTTL.get(this.propertySpecService, this.nlsService, this.converter),
            PLCConfigurationDeviceMessage.SetHighPriorityWindowSize.get(this.propertySpecService, this.nlsService, this.converter),
            PLCConfigurationDeviceMessage.SetCSMAFairnessLimit.get(this.propertySpecService, this.nlsService, this.converter),
            PLCConfigurationDeviceMessage.SetBeaconRandomizationWindowLength.get(this.propertySpecService, this.nlsService, this.converter),
            PLCConfigurationDeviceMessage.SetMacA.get(this.propertySpecService, this.nlsService, this.converter),
            PLCConfigurationDeviceMessage.SetMacK.get(this.propertySpecService, this.nlsService, this.converter),
            PLCConfigurationDeviceMessage.SetMinimumCWAttempts.get(this.propertySpecService, this.nlsService, this.converter),
            PLCConfigurationDeviceMessage.SetMaxBe.get(this.propertySpecService, this.nlsService, this.converter),
            PLCConfigurationDeviceMessage.SetMaxCSMABackOff.get(this.propertySpecService, this.nlsService, this.converter),
            PLCConfigurationDeviceMessage.SetMinBe.get(this.propertySpecService, this.nlsService, this.converter),
            PLCConfigurationDeviceMessage.PathRequest.get(this.propertySpecService, this.nlsService, this.converter),

            DeviceActionMessage.REBOOT_DEVICE.get(this.propertySpecService, this.nlsService, this.converter),

            SecurityMessage.CHANGE_DLMS_AUTHENTICATION_LEVEL.get(this.propertySpecService, this.nlsService, this.converter),
            SecurityMessage.ACTIVATE_DLMS_ENCRYPTION.get(this.propertySpecService, this.nlsService, this.converter),
            SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEYS.get(this.propertySpecService, this.nlsService, this.converter),
            SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEYS.get(this.propertySpecService, this.nlsService, this.converter),
            SecurityMessage.CHANGE_HLS_SECRET_PASSWORD.get(this.propertySpecService, this.nlsService, this.converter),

            GeneralDeviceMessage.WRITE_FULL_CONFIGURATION.get(this.propertySpecService, this.nlsService, this.converter),
            OutputConfigurationMessage.WriteOutputState.get(this.propertySpecService, this.nlsService, this.converter),

            UplinkConfigurationDeviceMessage.EnableUplinkPing.get(this.propertySpecService, this.nlsService, this.converter),
            UplinkConfigurationDeviceMessage.WriteUplinkPingDestinationAddress.get(this.propertySpecService, this.nlsService, this.converter),
            UplinkConfigurationDeviceMessage.WriteUplinkPingInterval.get(this.propertySpecService, this.nlsService, this.converter),
            UplinkConfigurationDeviceMessage.WriteUplinkPingTimeout.get(this.propertySpecService, this.nlsService, this.converter),
            PPPConfigurationDeviceMessage.SetPPPIdleTime.get(this.propertySpecService, this.nlsService, this.converter),
            NetworkConnectivityMessage.PreferGPRSUpstreamCommunication.get(this.propertySpecService, this.nlsService, this.converter),
            NetworkConnectivityMessage.EnableModemWatchdog.get(this.propertySpecService, this.nlsService, this.converter),
            NetworkConnectivityMessage.SetModemWatchdogParameters.get(this.propertySpecService, this.nlsService, this.converter),
            ConfigurationChangeDeviceMessage.EnableSSL.get(this.propertySpecService, this.nlsService, this.converter),
            AlarmConfigurationMessage.CONFIGURE_PUSH_EVENT_NOTIFICATION.get(this.propertySpecService, this.nlsService, this.converter),
            AlarmConfigurationMessage.ENABLE_EVENT_NOTIFICATIONS.get(this.propertySpecService, this.nlsService, this.converter),

            //G3 interface essages
            PLCConfigurationDeviceMessage.SetAutomaticRouteManagement.get(this.propertySpecService, this.nlsService, this.converter),
            PLCConfigurationDeviceMessage.EnableSNR.get(this.propertySpecService, this.nlsService, this.converter),
            PLCConfigurationDeviceMessage.SetSNRPacketInterval.get(this.propertySpecService, this.nlsService, this.converter),
            PLCConfigurationDeviceMessage.SetSNRQuietTime.get(this.propertySpecService, this.nlsService, this.converter),
            PLCConfigurationDeviceMessage.SetSNRPayload.get(this.propertySpecService, this.nlsService, this.converter),
            PLCConfigurationDeviceMessage.EnableKeepAlive.get(this.propertySpecService, this.nlsService, this.converter),
            PLCConfigurationDeviceMessage.SetKeepAliveScheduleInterval.get(this.propertySpecService, this.nlsService, this.converter),
            PLCConfigurationDeviceMessage.SetKeepAliveBucketSize.get(this.propertySpecService, this.nlsService, this.converter),
            PLCConfigurationDeviceMessage.SetMinInactiveMeterTime.get(this.propertySpecService, this.nlsService, this.converter),
            PLCConfigurationDeviceMessage.SetMaxInactiveMeterTime.get(this.propertySpecService, this.nlsService, this.converter),
            PLCConfigurationDeviceMessage.SetKeepAliveRetries.get(this.propertySpecService, this.nlsService, this.converter),
            PLCConfigurationDeviceMessage.SetKeepAliveTimeout.get(this.propertySpecService, this.nlsService, this.converter),
            PLCConfigurationDeviceMessage.EnableG3PLCInterface.get(this.propertySpecService, this.nlsService, this.converter),

            ClockDeviceMessage.SyncTime.get(this.propertySpecService, this.nlsService, this.converter),
            ConfigurationChangeDeviceMessage.SetDeviceName.get(this.propertySpecService, this.nlsService, this.converter),
            ConfigurationChangeDeviceMessage.SetNTPAddress.get(this.propertySpecService, this.nlsService, this.converter),
            ConfigurationChangeDeviceMessage.SyncNTPServer.get(this.propertySpecService, this.nlsService, this.converter),
            DeviceActionMessage.RebootApplication.get(this.propertySpecService, this.nlsService, this.converter),

            FirewallConfigurationMessage.ActivateFirewall.get(this.propertySpecService, this.nlsService, this.converter),
            FirewallConfigurationMessage.DeactivateFirewall.get(this.propertySpecService, this.nlsService, this.converter),
            FirewallConfigurationMessage.ConfigureFWGPRS.get(this.propertySpecService, this.nlsService, this.converter),
            FirewallConfigurationMessage.ConfigureFWLAN.get(this.propertySpecService, this.nlsService, this.converter),
            FirewallConfigurationMessage.ConfigureFWWAN.get(this.propertySpecService, this.nlsService, this.converter),
            FirewallConfigurationMessage.SetFWDefaultState.get(this.propertySpecService, this.nlsService, this.converter),

            SecurityMessage.CHANGE_WEBPORTAL_PASSWORD1.get(this.propertySpecService, this.nlsService, this.converter),
            SecurityMessage.CHANGE_WEBPORTAL_PASSWORD2.get(this.propertySpecService, this.nlsService, this.converter),

            FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_IMAGE_IDENTIFIER.get(this.propertySpecService, this.nlsService, this.converter));
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList result = this.collectedDataFactory.createCollectedMessageList(pendingMessages);
        for (OfflineDeviceMessage pendingMessage : pendingMessages) {
            CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);   //Optimistic
            try {
                final CollectedMessage plcMessageResult = getPLCConfigurationDeviceMessageExecutor().executePendingMessage(pendingMessage, collectedMessage);
                if (plcMessageResult != null) {
                    collectedMessage = plcMessageResult;
                } else { // if it was not a PLC message
                    if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_WEBPORTAL_PASSWORD1)) {
                        changePasswordUser1(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_WEBPORTAL_PASSWORD2)) {
                        changePasswordUser2(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(UplinkConfigurationDeviceMessage.EnableUplinkPing)) {
                        enableUplinkPing(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_IMAGE_IDENTIFIER)) {
                        upgradeFirmware(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(UplinkConfigurationDeviceMessage.WriteUplinkPingDestinationAddress)) {
                        writeUplinkPingDestinationAddress(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(UplinkConfigurationDeviceMessage.WriteUplinkPingInterval)) {
                        writeUplinkPingInterval(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(UplinkConfigurationDeviceMessage.WriteUplinkPingTimeout)) {
                        writeUplinkPingTimeout(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(PPPConfigurationDeviceMessage.SetPPPIdleTime)) {
                        setPPPIdleTime(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.PreferGPRSUpstreamCommunication)) {
                        preferGPRSUpstreamCommunication(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.EnableModemWatchdog)) {
                        enableModemWatchdog(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.SetModemWatchdogParameters)) {
                        setModemWatchdogParameters(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.EnableSSL)) {
                        enableSSL(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(AlarmConfigurationMessage.ENABLE_EVENT_NOTIFICATIONS)) {
                        enableEventNotifications(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(AlarmConfigurationMessage.CONFIGURE_PUSH_EVENT_NOTIFICATION)) {
                        configurePushEventNotification(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(ClockDeviceMessage.SyncTime)) {
                        syncTime(pendingMessage);
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
                    } else if (pendingMessage.getSpecification().equals(GeneralDeviceMessage.WRITE_FULL_CONFIGURATION)) {
                        writeFullConfig(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(OutputConfigurationMessage.WriteOutputState)) {
                        writeOutputState(pendingMessage);
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
                    } else if (pendingMessage.getSpecification().equals(FirewallConfigurationMessage.SetFWDefaultState)) {
                        setFWDefaultState(pendingMessage);
                    } else {   //Unsupported message
                        collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                        collectedMessage.setFailureInformation(ResultType.NotSupported, createUnsupportedWarning(pendingMessage));
                        collectedMessage.setDeviceProtocolInformation("Message is currently not supported by the protocol");
                    }
                }
            } catch (IOException e) {
                if (DLMSIOExceptionHandler.isUnexpectedResponse(e, session.getProperties().getRetries()+ 1)) {
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
                    collectedMessage.setDeviceProtocolInformation(e.getMessage());
                }   //Else: throw communication exception
            } catch (IndexOutOfBoundsException | ParseException | NumberFormatException e) {
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
                collectedMessage.setDeviceProtocolInformation(e.getMessage());
            }
            result.addCollectedMessage(collectedMessage);
        }
        return result;
    }

    private PLCConfigurationDeviceMessageExecutor getPLCConfigurationDeviceMessageExecutor() {
        if (plcConfigurationDeviceMessageExecutor == null) {
            plcConfigurationDeviceMessageExecutor = new PLCConfigurationDeviceMessageExecutor(session, offlineDevice, this.collectedDataFactory, this.issueFactory);
        }
        return plcConfigurationDeviceMessageExecutor;
    }

    private void upgradeFirmware(OfflineDeviceMessage pendingMessage) throws IOException {

        String b64EncodedImage = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateUserFileAttributeName).getValue();
        String imageIdentifier = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateImageIdentifierAttributeName).getValue(); // Will return empty string if the MessageAttribute could not be found
        byte[] image = new Base64EncoderDecoder().decode(b64EncodedImage);

        ImageTransfer it = session.getCosemObjectFactory().getImageTransfer();

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
                session.getLogger().log(Level.INFO, "Received temporary failure. Meter will activate the image when this communication session is closed, moving on.");
            } else {
                throw e;
            }
        }
    }

    private boolean isTemporaryFailure(DataAccessResultException e) {
        return (e.getDataAccessResult() == DataAccessResultCode.TEMPORARY_FAILURE.getResultCode());
    }

    private void setFWDefaultState(OfflineDeviceMessage pendingMessage) throws IOException, ParseException {
        this.session.getCosemObjectFactory().getFirewallSetup().setEnabledByDefault(getSingleBooleanAttribute(pendingMessage));
    }

    private void configureFWWAN(OfflineDeviceMessage pendingMessage) throws IOException {
        boolean isDLMSAllowed = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.EnableDLMS).getValue());
        boolean isHTTPAllowed = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.EnableHTTP).getValue());
        boolean isSSHAllowed = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.EnableSSH).getValue());
        this.session.getCosemObjectFactory().getFirewallSetup().setWANPortSetup(new FirewallSetup.InterfaceFirewallConfiguration(isDLMSAllowed, isHTTPAllowed, isSSHAllowed));
    }

    private void configureFWLAN(OfflineDeviceMessage pendingMessage) throws IOException {
        boolean isDLMSAllowed = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.EnableDLMS).getValue());
        boolean isHTTPAllowed = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.EnableHTTP).getValue());
        boolean isSSHAllowed = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.EnableSSH).getValue());
        this.session.getCosemObjectFactory().getFirewallSetup().setLANPortSetup(new FirewallSetup.InterfaceFirewallConfiguration(isDLMSAllowed, isHTTPAllowed, isSSHAllowed));
    }

    private void configureFWGPRS(OfflineDeviceMessage pendingMessage) throws IOException {
        boolean isDLMSAllowed = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.EnableDLMS).getValue());
        boolean isHTTPAllowed = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.EnableHTTP).getValue());
        boolean isSSHAllowed = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.EnableSSH).getValue());
        this.session.getCosemObjectFactory().getFirewallSetup().setGPRSPortSetup(new FirewallSetup.InterfaceFirewallConfiguration(isDLMSAllowed, isHTTPAllowed, isSSHAllowed));
    }

    private void deactivateFirewall(OfflineDeviceMessage pendingMessage) throws IOException {
        this.session.getCosemObjectFactory().getFirewallSetup().deactivate();
    }

    private void activateFirewall(OfflineDeviceMessage pendingMessage) throws IOException {
        this.session.getCosemObjectFactory().getFirewallSetup().activate();
    }

    private void writeOutputState(OfflineDeviceMessage pendingMessage) throws IOException {
        int outputId = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.outputId).getValue());
        boolean newState = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.newState).getValue());

        final ObisCode obisCode = new ObisCode(0, outputId, 96, 3, 10, 255);
        final Disconnector disconnector = this.session.getCosemObjectFactory().getDisconnector(obisCode);
        if (newState) {
            disconnector.remoteReconnect();
        } else {
            disconnector.remoteDisconnect();
        }
    }

    private void writeFullConfig(OfflineDeviceMessage pendingMessage) throws IOException {
        String hex = pendingMessage.getDeviceMessageAttributes().get(0).getValue();
        String xmlData = new String(ProtocolTools.getBytesFromHexString(hex));
        XMLParser parser = new XMLParser(session.getLogger(), session.getCosemObjectFactory());
        parser.parseXML(xmlData);
        List<Object[]> parsedObjects = parser.getParsedObjects();

        boolean encounteredError = false;
        String lastError = "";
        session.getLogger().info("Transferring the objects to the device.");
        for (Object[] each : parsedObjects) {
            AbstractDataType value = (AbstractDataType) each[1];
            if (each[0].getClass().equals(GenericWrite.class)) {
                GenericWrite genericWrite = (GenericWrite) each[0];
                try {
                    genericWrite.write(value.getBEREncodedByteArray());
                } catch (DataAccessResultException e) {
                    encounteredError = true;
                    lastError = "ERROR: Failed to write DLMS object " + genericWrite.getObjectReference() + " , attribute " + genericWrite.getAttr() + " : " + e;
                    session.getLogger().severe(lastError);
                }
            } else if (each[0].getClass().equals(GenericInvoke.class)) {
                GenericInvoke genericInvoke = (GenericInvoke) each[0];
                try {
                    genericInvoke.invoke(value.getBEREncodedByteArray());
                } catch (DataAccessResultException e) {
                    encounteredError = true;
                    lastError = "ERROR: Failed to execute action on DLMS object " + genericInvoke.getObjectReference() + " , method " + genericInvoke.getMethod() + " : " + e;
                    session.getLogger().severe(lastError);
                }
            }
        }

        session.getLogger().log(Level.INFO, "Configuration download message finished.");
        if (encounteredError) {
            throw new ProtocolException(lastError);
        }
    }

    private void changeHlsSecret(OfflineDeviceMessage pendingMessage) throws IOException {
        String hex = pendingMessage.getDeviceMessageAttributes().get(0).getValue();
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getAssociationLN().changeHLSSecret(ProtocolTools.getBytesFromHexString(hex, ""));
    }

    protected void changeEncryptionKey(OfflineDeviceMessage pendingMessage) throws IOException {
        String wrappedHexKey = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.newWrappedEncryptionKeyAttributeName).getValue();
        String plainHexKey = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.newEncryptionKeyAttributeName).getValue();
        String oldHexKey = ProtocolTools.getHexStringFromBytes(session.getProperties().getSecurityProvider().getGlobalKey(), "");

        Array encryptionKeyArray = new Array();
        Structure keyData = new Structure();
        keyData.addDataType(new TypeEnum(0));    // 0 means keyType: encryptionKey (global key)
        keyData.addDataType(OctetString.fromByteArray(ProtocolTools.getBytesFromHexString(wrappedHexKey)));
        encryptionKeyArray.addDataType(keyData);
        getSecuritySetup().transferGlobalKey(encryptionKeyArray);

        //Update the key in the security provider, it is used instantly
        session.getProperties().getSecurityProvider().changeEncryptionKey(ProtocolTools.getBytesFromHexString(plainHexKey));

        //Reset frame counter, only if a different key has been written
        if (!oldHexKey.equalsIgnoreCase(plainHexKey)) {
            session.getAso().getSecurityContext().setFrameCounter(1);
        }
    }

    protected SecuritySetup getSecuritySetup() throws IOException {
        return this.session.getCosemObjectFactory().getSecuritySetup();
    }

    protected void changeAuthKey(OfflineDeviceMessage pendingMessage) throws IOException {
        String wrappedHexKey = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.newWrappedAuthenticationKeyAttributeName).getValue();
        String plainHexKey = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.newAuthenticationKeyAttributeName).getValue();

        Array authenticationKeyArray = new Array();
        Structure keyData = new Structure();
        keyData.addDataType(new TypeEnum(2));    // 2 means keyType: authenticationKey
        keyData.addDataType(OctetString.fromByteArray(ProtocolTools.getBytesFromHexString(wrappedHexKey)));
        authenticationKeyArray.addDataType(keyData);
        getSecuritySetup().transferGlobalKey(authenticationKeyArray);

        //Update the key in the security provider, it is used instantly
        session.getProperties().getSecurityProvider().changeAuthenticationKey(ProtocolTools.getBytesFromHexString(plainHexKey));
    }

    private void activateDlmsEncryption(OfflineDeviceMessage pendingMessage) throws IOException {
        getSecuritySetup().activateSecurity(new TypeEnum(getSingleIntegerAttribute(pendingMessage)));
    }

    private void changeDlmAuthLevel(OfflineDeviceMessage pendingMessage) throws IOException {
        int newAuthLevel = getSingleIntegerAttribute(pendingMessage);

        AssociationLN associationLN = this.session.getCosemObjectFactory().getAssociationLN();
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
        String address = pendingMessage.getDeviceMessageAttributes().get(0).getValue();
        this.session.getCosemObjectFactory().getNTPServerAddress().writeNTPServerName(address);
    }

    private void syncNTPServer(OfflineDeviceMessage pendingMessage) throws IOException {
        this.session.getCosemObjectFactory().getNTPServerAddress().ntpSync();
    }

    private void setDeviceName(OfflineDeviceMessage pendingMessage) throws IOException {
        String name = pendingMessage.getDeviceMessageAttributes().get(0).getValue();
        this.session.getCosemObjectFactory().getData(DEVICE_NAME_OBISCODE).setValueAttr(OctetString.fromString(name));
    }

    private void rebootApplication(OfflineDeviceMessage pendingMessage) throws IOException {
        this.session.getCosemObjectFactory().getLifeCycleManagement().restartApplication();
    }

    private void rebootDevice(OfflineDeviceMessage pendingMessage) throws IOException {
        this.session.getCosemObjectFactory().getLifeCycleManagement().rebootDevice();
    }

    private void syncTime(OfflineDeviceMessage pendingMessage) throws IOException {
        Calendar cal = Calendar.getInstance(session.getTimeZone());
        Date currentTime = new Date();
        cal.setTime(currentTime);
        session.getCosemObjectFactory().getClock().setAXDRDateTimeAttr(new AXDRDateTime(cal));
    }

    private void enableSSL(OfflineDeviceMessage pendingMessage) throws IOException {
        //TODO port from 8.11
    }

    private void enableEventNotifications(OfflineDeviceMessage pendingMessage) throws IOException {
        boolean enable = Boolean.parseBoolean(pendingMessage.getDeviceMessageAttributes().get(0).getValue());
        GenericInvoke genericInvoke = this.session.getCosemObjectFactory().getGenericInvoke(EVENT_NOTIFICATION_OBISCODE, DLMSClassId.EVENT_NOTIFICATION.getClassId(), 1);
        genericInvoke.invoke(new BooleanObject(enable).getBEREncodedByteArray());
    }

    private void setModemWatchdogParameters(OfflineDeviceMessage pendingMessage) throws IOException {
        int modemWatchdogInterval = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.modemWatchdogInterval).getValue());
        int pppDaemonResetThreshold = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.PPPDaemonResetThreshold).getValue());
        int modemResetThreshold = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.modemResetThreshold).getValue());
        int systemRebootThreshold = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.systemRebootThreshold).getValue());

        this.session.getCosemObjectFactory().getModemWatchdogConfiguration().writeConfigParameters(
                modemWatchdogInterval,
                pppDaemonResetThreshold,
                modemResetThreshold,
                systemRebootThreshold
        );
    }

    private void enableModemWatchdog(OfflineDeviceMessage pendingMessage) throws IOException {
        boolean enable = Boolean.parseBoolean(pendingMessage.getDeviceMessageAttributes().get(0).getValue());
        this.session.getCosemObjectFactory().getModemWatchdogConfiguration().enableWatchdog(enable);
    }

    private void preferGPRSUpstreamCommunication(OfflineDeviceMessage pendingMessage) throws IOException {
        //TODO port from 8.11
    }

    private void setPPPIdleTime(OfflineDeviceMessage pendingMessage) throws IOException {
        //TODO port from 8.11
    }

    private void configurePushEventNotification(OfflineDeviceMessage pendingMessage) throws IOException {
        String transportTypeString = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.transportTypeAttributeName).getValue();
        int transportType = AlarmConfigurationMessage.TransportType.valueOf(transportTypeString).getId();

        String destinationAddress = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.destinationAddressAttributeName).getValue();

        String messageTypeString = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.messageTypeAttributeName).getValue();
        int messageType = AlarmConfigurationMessage.MessageType.valueOf(messageTypeString).getId();

        this.session.getCosemObjectFactory().getEventPushNotificationConfig().writeSendDestinationAndMethod(transportType, destinationAddress, messageType);
    }

    private void writeUplinkPingTimeout(OfflineDeviceMessage pendingMessage) throws IOException {
        Integer timeout = Integer.valueOf(pendingMessage.getDeviceMessageAttributes().get(0).getValue());
        this.session.getCosemObjectFactory().getUplinkPingConfiguration().writeTimeout(timeout);
    }

    private void changePasswordUser1(OfflineDeviceMessage pendingMessage) throws IOException {
        String newPassword = pendingMessage.getDeviceMessageAttributes().get(0).getValue();
        this.session.getCosemObjectFactory().getWebPortalConfig().changeUser1Password(newPassword);
    }

    private void changePasswordUser2(OfflineDeviceMessage pendingMessage) throws IOException {
        String newPassword = pendingMessage.getDeviceMessageAttributes().get(0).getValue();
        this.session.getCosemObjectFactory().getWebPortalConfig().changeUser2Password(newPassword);
    }

    private void writeUplinkPingInterval(OfflineDeviceMessage pendingMessage) throws IOException {
        Integer interval = Integer.valueOf(pendingMessage.getDeviceMessageAttributes().get(0).getValue());
        this.session.getCosemObjectFactory().getUplinkPingConfiguration().writeInterval(interval);
    }

    private void writeUplinkPingDestinationAddress(OfflineDeviceMessage pendingMessage) throws IOException {
        String destinationAddress = pendingMessage.getDeviceMessageAttributes().get(0).getValue();
        this.session.getCosemObjectFactory().getUplinkPingConfiguration().writeDestAddress(destinationAddress);
    }

    private void enableUplinkPing(OfflineDeviceMessage pendingMessage) throws IOException {
        boolean enable = Boolean.parseBoolean(pendingMessage.getDeviceMessageAttributes().get(0).getValue());
        this.session.getCosemObjectFactory().getUplinkPingConfiguration().enableUplinkPing(enable);
    }

    private int getSingleIntegerAttribute(OfflineDeviceMessage pendingMessage) {
        return Integer.parseInt(pendingMessage.getDeviceMessageAttributes().get(0).getValue());
    }

    private boolean getSingleBooleanAttribute(OfflineDeviceMessage pendingMessage) {
        return Boolean.parseBoolean(pendingMessage.getDeviceMessageAttributes().get(0).getValue());
    }

    protected CollectedMessage createCollectedMessage(OfflineDeviceMessage message) {
        return this.collectedDataFactory.createCollectedMessage(new DeviceMessageIdentifierById(message.getDeviceMessageId()));
    }

    protected CollectedMessage createCollectedMessageWithRegisterData(OfflineDeviceMessage message, List<CollectedRegister> registers) {
        return this.collectedDataFactory.createCollectedMessageWithRegisterData(new DeviceIdentifierById(message.getDeviceId()), new DeviceMessageIdentifierById(message.getDeviceMessageId()), registers);
    }

    protected Issue createMessageFailedIssue(OfflineDeviceMessage pendingMessage, Exception e) {
        return createMessageFailedIssue(pendingMessage, e.getMessage());
    }

    protected Issue createMessageFailedIssue(OfflineDeviceMessage pendingMessage, String message) {
        return this.issueFactory.createWarning(pendingMessage, "DeviceMessage.failed",
                pendingMessage.getDeviceMessageId(),
                pendingMessage.getSpecification().getCategory().getName(),
                pendingMessage.getSpecification().getName(),
                message);
    }

    protected Issue createUnsupportedWarning(OfflineDeviceMessage pendingMessage) throws IOException {
        return this.issueFactory.createWarning(pendingMessage, "DeviceMessage.notSupported",
                pendingMessage.getDeviceMessageId(),
                pendingMessage.getSpecification().getCategory().getName(),
                pendingMessage.getSpecification().getName());
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return this.collectedDataFactory.createEmptyCollectedMessageList();  //Nothing to do here
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(DeviceMessageConstants.broadCastLogTableEntryTTLAttributeName)) {
            return String.valueOf(Temporals.toSeconds((TemporalAmount) messageAttribute));
        } else if (propertySpec.getName().equals(DeviceMessageConstants.configUserFileAttributeName)) {
            return ProtocolTools.getHexStringFromBytes(((UserFile) propertySpec).loadFileInByteArray(), "");
        } else if (propertySpec.getName().equals(DeviceMessageConstants.encryptionLevelAttributeName)) {
            return String.valueOf(DlmsEncryptionLevelMessageValues.getValueFor(messageAttribute.toString()));
        } else if (propertySpec.getName().equals(DeviceMessageConstants.authenticationLevelAttributeName)) {
            return String.valueOf(DlmsAuthenticationLevelMessageValues.getValueFor(messageAttribute.toString()));
        } else if (propertySpec.getName().equals(DeviceMessageConstants.firmwareUpdateUserFileAttributeName)) {
            final byte[] data = ((UserFile) messageAttribute).loadFileInByteArray();
            return new Base64EncoderDecoder().encode(data);
        } else if (propertySpec.getName().equals(DeviceMessageConstants.deviceGroupAttributeName)) {
            Group group = (Group) messageAttribute;
            StringBuilder macAddresses = new StringBuilder();
            for (BusinessObject businessObject : group.getMembers()) {
                if (businessObject instanceof Device) {
                    Device device = (Device) businessObject;
                    String callHomeId = device.getProtocolProperties().getTypedProperty(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME, "");
                    if (!callHomeId.isEmpty()) {
                        if (macAddresses.length() != 0) {
                            macAddresses.append(";");
                        }
                        macAddresses.append(callHomeId);
                    }
                } else {
                    //TODO throw proper exception
                }
            }
            return macAddresses.toString();
        } else if (propertySpec.getName().equals(DeviceMessageConstants.newAuthenticationKeyAttributeName)
                || propertySpec.getName().equals(DeviceMessageConstants.newPasswordAttributeName)
                || propertySpec.getName().equals(DeviceMessageConstants.newEncryptionKeyAttributeName)) {
            return ((Password) messageAttribute).getValue();
        } else {
            return messageAttribute.toString();     //Works for BigDecimal, boolean and (hex)string propertyspecs
        }
    }

    @Override
    public String prepareMessageContext(OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return "";
    }
}
