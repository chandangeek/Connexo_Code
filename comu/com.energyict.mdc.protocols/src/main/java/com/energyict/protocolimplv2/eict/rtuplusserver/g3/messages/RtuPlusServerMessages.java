package com.energyict.protocolimplv2.eict.rtuplusserver.g3.messages;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Password;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.MessageSeeds;
import com.energyict.mdc.protocol.api.ProtocolException;
import com.energyict.mdc.protocol.api.device.data.*;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.device.messages.DlmsAuthenticationLevelMessageValues;
import com.energyict.mdc.protocol.api.device.messages.DlmsEncryptionLevelMessageValues;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.tasks.support.DeviceMessageSupport;
import com.energyict.protocolimpl.dlms.idis.xml.XMLParser;
import com.energyict.protocolimpl.generic.messages.GenericMessaging;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.RtuPlusServer;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.properties.G3GatewayProperties;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 17/06/2014 - 15:00
 */
public class RtuPlusServerMessages implements DeviceMessageSupport {

    private final DlmsSession session;
    private final IssueService issueService;
    private final CollectedDataFactory collectedDataFactory;
    private final RtuPlusServer deviceProtocol;
    private static final ObisCode DEVICE_NAME_OBISCODE = ObisCode.fromString("0.0.128.0.9.255");

    private Set<DeviceMessageId> supportedMessages = EnumSet.of(
            DeviceMessageId.PLC_CONFIGURATION_SET_MAX_NUMBER_OF_HOPS_ATTRIBUTENAME,
            DeviceMessageId.PLC_CONFIGURATION_SET_WEAK_LQI_VALUE_ATTRIBUTENAME,
            DeviceMessageId.PLC_CONFIGURATION_SET_SECURITY_LEVEL,
            DeviceMessageId.PLC_CONFIGURATION_SET_ROUTING_CONFIGURATION,
            DeviceMessageId.PLC_CONFIGURATION_SET_BROADCAST_LOG_TABLE_ENTRY_TTL_ATTRIBUTENAME,
            DeviceMessageId.PLC_CONFIGURATION_SET_MAX_JOIN_WAIT_TIME,
            DeviceMessageId.PLC_CONFIGURATION_SET_PATH_DISCOVERY_TIME,
            DeviceMessageId.PLC_CONFIGURATION_SET_METRIC_TYPE,
            DeviceMessageId.PLC_CONFIGURATION_SET_PAN_ID,
            DeviceMessageId.PLC_CONFIGURATION_SET_TMR_TTL,
            DeviceMessageId.PLC_CONFIGURATION_SET_MAX_FRAME_RETRIES,
            DeviceMessageId.PLC_CONFIGURATION_SET_NEIGHBOUR_TABLE_ENTRY_TTL,
            DeviceMessageId.PLC_CONFIGURATION_SET_HIGH_PRIORITY_WINDOW_SIZE,
            DeviceMessageId.PLC_CONFIGURATION_SET_CSMA_FAIRNESS_LIMIT,
            DeviceMessageId.PLC_CONFIGURATION_SET_BEACON_RANDOMIZATION_WINDOW_LENGTH,
            DeviceMessageId.PLC_CONFIGURATION_SET_MAC_A,
            DeviceMessageId.PLC_CONFIGURATION_SET_MAC_K,
            DeviceMessageId.PLC_CONFIGURATION_SET_MINIMUM_CW_ATTEMPTS,
            DeviceMessageId.PLC_CONFIGURATION_SET_MAX_BE,
            DeviceMessageId.PLC_CONFIGURATION_SET_MAX_CSMA_BACK_OFF,
            DeviceMessageId.PLC_CONFIGURATION_SET_MIN_BE,
            DeviceMessageId.PLC_CONFIGURATION_PATH_REQUEST,

            DeviceMessageId.DEVICE_ACTIONS_REBOOT_DEVICE,

            DeviceMessageId.SECURITY_CHANGE_DLMS_AUTHENTICATION_LEVEL,
            DeviceMessageId.SECURITY_ACTIVATE_DLMS_ENCRYPTION,
            DeviceMessageId.SECURITY_CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEY,
            DeviceMessageId.SECURITY_CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY,
            DeviceMessageId.SECURITY_CHANGE_HLS_SECRET_WITH_PASSWORD,

            DeviceMessageId.GENERAL_WRITE_FULL_CONFIGURATION,

            DeviceMessageId.OUTPUT_CONFIGURATION_WRITE_OUTPUT_STATE,

            DeviceMessageId.UPLINK_CONFIGURATION_ENABLE_PING,
            DeviceMessageId.UPLINK_CONFIGURATION_WRITE_UPLINK_PING_DESTINATION_ADDRESS,
            DeviceMessageId.UPLINK_CONFIGURATION_WRITE_UPLINK_PING_INTERVAL,
            DeviceMessageId.UPLINK_CONFIGURATION_WRITE_UPLINK_PING_TIMEOUT,

            DeviceMessageId.PPP_CONFIGURATION_SET_IDLE_TIME,
            DeviceMessageId.NETWORK_CONNECTIVITY_PREFER_GPRS_UPSTREAM_COMMUNICATION,
            DeviceMessageId.NETWORK_CONNECTIVITY_ENABLE_MODEM_WATCHDOG,
            DeviceMessageId.NETWORK_CONNECTIVITY_SET_MODEM_WATCHDOG_PARAMETERS,
            DeviceMessageId.CONFIGURATION_CHANGE_ENABLE_SSL,
            DeviceMessageId.ALARM_CONFIGURATION_CONFIGURE_PUSH_EVENT_NOTIFICATION,
            DeviceMessageId.PLC_CONFIGURATION_SET_AUTOMATIC_ROUTE_MANAGEMENT,
            DeviceMessageId.PLC_CONFIGURATION_ENABLE_SNR,
            DeviceMessageId.PLC_CONFIGURATION_SET_SNR_PACKET_INTERVAL,
            DeviceMessageId.PLC_CONFIGURATION_SET_SNR_QUIET_TIME,
            DeviceMessageId.PLC_CONFIGURATION_SET_SNR_PAYLOAD,
            DeviceMessageId.PLC_CONFIGURATION_ENABLE_KEEP_ALIVE,
            DeviceMessageId.PLC_CONFIGURATION_SET_KEEP_ALIVE_SCHEDULE_INTERVAL,
            DeviceMessageId.PLC_CONFIGURATION_SET_KEEP_ALIVE_BUCKET_SIZE,
            DeviceMessageId.PLC_CONFIGURATION_SET_MIN_INACTIVE_METER_TIME,
            DeviceMessageId.PLC_CONFIGURATION_SET_MAX_INACTIVE_METER_TIME,
            DeviceMessageId.PLC_CONFIGURATION_SET_KEEP_ALIVE_RETRIES,
            DeviceMessageId.PLC_CONFIGURATION_SET_KEEP_ALIVE_TIMEOUT,
            DeviceMessageId.CLOCK_SET_SYNCHRONIZE_TIME,
            DeviceMessageId.CONFIGURATION_CHANGE_SET_DEVICENAME,
            DeviceMessageId.CONFIGURATION_CHANGE_SET_NTPADDRESS,
            DeviceMessageId.CONFIGURATION_CHANGE_SYNC_NTPSERVER,
            DeviceMessageId.DEVICE_ACTIONS_REBOOT_APPLICATION,
            DeviceMessageId.FIREWALL_ACTIVATE_FIREWALL,
            DeviceMessageId.FIREWALL_DEACTIVATE_FIREWALL,
            DeviceMessageId.FIREWALL_CONFIGURE_FW_GPRS,
            DeviceMessageId.FIREWALL_CONFIGURE_FW_LAN,
            DeviceMessageId.FIREWALL_CONFIGURE_FW_WAN,
            DeviceMessageId.FIREWALL_SET_FW_DEFAULT_STATE,
            DeviceMessageId.SECURITY_CHANGE_WEBPORTAL_PASSWORD,
            DeviceMessageId.SECURITY_CHANGE_WEBPORTAL_PASSWORD2
    );

    public RtuPlusServerMessages(DlmsSession session, IssueService issueService, CollectedDataFactory collectedDataFactory, RtuPlusServer deviceProtocol) {
        this.session = session;
        this.issueService = issueService;
        this.collectedDataFactory = collectedDataFactory;
        this.deviceProtocol = deviceProtocol;
    }

    public Set<DeviceMessageId> getSupportedMessages() {
        return supportedMessages;
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList result = this.collectedDataFactory.createCollectedMessageList(pendingMessages);
        for (OfflineDeviceMessage pendingMessage : pendingMessages) {
            CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);   //Optimistic
            try {
                if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_NUMBER_OF_HOPS_ATTRIBUTENAME)) {
                    setMaxNumberOfHops(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_WEAK_LQI_VALUE_ATTRIBUTENAME)) {
                    setWeakLQIValue(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.SECURITY_CHANGE_WEBPORTAL_PASSWORD)) {
                    changePasswordUser1(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.SECURITY_CHANGE_WEBPORTAL_PASSWORD2)) {
                    changePasswordUser2(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_SECURITY_LEVEL)) {
                    setSecurityLevelpendingMessage(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_ROUTING_CONFIGURATION)) {
                    setRoutingConfiguration(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_BROAD_CAST_LOG_TABLE_ENTRY_TTL)) {
                    setBroadCastLogTableEntryTTL(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_JOIN_WAIT_TIME)) {
                    setMaxJoinWaitTime(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_PATH_DISCOVERY_TIME)) {
                    setPathDiscoveryTime(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_METRIC_TYPE)) {
                    setMetricType(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_COORD_SHORT_ADDRESS)) {
                    setCoordShortAddress(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_DISABLE_DEFAULT_ROUTING)) {
                    setDisableDefaultRouting(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_DEVICE_TYPE)) {
                    setDeviceType(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_RESET_PLC_OFDM_MAC_COUNTERS)) {
                    resetPlcOfdmMacCounters();
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_PAN_ID)) {
                    setPanId(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_TONE_MASK_ATTRIBUTE_NAME)) {
                    setToneMask(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_TMR_TTL)) {
                    setTMRTTL(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_FRAME_RETRIES)) {
                    setMaxFrameRetries(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_NEIGHBOUR_TABLE_ENTRY_TTL)) {
                    setNeighbourTableEntryTTL(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_HIGH_PRIORITY_WINDOW_SIZE)) {
                    setHighPriorityWindowSize(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_CSMA_FAIRNESS_LIMIT)) {
                    setCSMAFairnessLimit(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_BEACON_RANDOMIZATION_WINDOW_LENGTH)) {
                    setBeaconRandomizationWindowLength(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_MAC_A)) {
                    setMacA(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_MAC_K)) {
                    setMacK(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_MINIMUM_CW_ATTEMPTS)) {
                    setMinimumCWAttempts(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_BE)) {
                    setMaxBe(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_CSMA_BACK_OFF)) {
                    setMaxCSMABackOff(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_MIN_BE)) {
                    setMinBe(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_PATH_REQUEST)) {
                    CollectedTopologyMessageInfo collectedTopologyMessageInfo = pathRequest(pendingMessage);
                    collectedMessage = this.collectedDataFactory.createCollectedMessageTopology(pendingMessage.getIdentifier(), collectedTopologyMessageInfo.collectedTopology);
                    collectedMessage.setDeviceProtocolInformation(collectedTopologyMessageInfo.protocolMessageInfo);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.UPLINK_CONFIGURATION_ENABLE_PING)) {
                    enableUplinkPing(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.UPLINK_CONFIGURATION_WRITE_UPLINK_PING_DESTINATION_ADDRESS)) {
                    writeUplinkPingDestinationAddress(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.UPLINK_CONFIGURATION_WRITE_UPLINK_PING_INTERVAL)) {
                    writeUplinkPingInterval(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.UPLINK_CONFIGURATION_WRITE_UPLINK_PING_TIMEOUT)) {
                    writeUplinkPingTimeout(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PPP_CONFIGURATION_SET_IDLE_TIME)) {
                    setPPPIdleTime();
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.NETWORK_CONNECTIVITY_PREFER_GPRS_UPSTREAM_COMMUNICATION)) {
                    preferGPRSUpstreamCommunication();
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.NETWORK_CONNECTIVITY_ENABLE_MODEM_WATCHDOG)) {
                    enableModemWatchdog(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.NETWORK_CONNECTIVITY_SET_MODEM_WATCHDOG_PARAMETERS)) {
                    setModemWatchdogParameters(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.CONFIGURATION_CHANGE_ENABLE_SSL)) {
                    enableSSL();
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.ALARM_CONFIGURATION_CONFIGURE_PUSH_EVENT_NOTIFICATION)) {
                    configurePushEventNotification(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.CLOCK_SET_SYNCHRONIZE_TIME)) {
                    syncTime();
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.DEVICE_ACTIONS_REBOOT_DEVICE)) {
                    rebootDevice();
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.DEVICE_ACTIONS_REBOOT_APPLICATION)) {
                    rebootApplication();
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.CONFIGURATION_CHANGE_SET_DEVICENAME)) {
                    setDeviceName(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.CONFIGURATION_CHANGE_SET_NTPADDRESS)) {
                    setNTPAddress();
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.CONFIGURATION_CHANGE_SYNC_NTPSERVER)) {
                    syncNTPServer();
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_AUTOMATIC_ROUTE_MANAGEMENT)) {
                    setAutomaticRouteManagement(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_ENABLE_SNR)) {
                    enableSNR(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_SNR_PACKET_INTERVAL)) {
                    setSNRPacketInterval(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_SNR_QUIET_TIME)) {
                    setSNRQuietTime(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_SNR_PAYLOAD)) {
                    setSNRPayload(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_ENABLE_KEEP_ALIVE)) {
                    enableKeepAlive(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_KEEP_ALIVE_SCHEDULE_INTERVAL)) {
                    setKeepAliveScheduleInterval(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_KEEP_ALIVE_BUCKET_SIZE)) {
                    setKeepAliveBucketSize(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_MIN_INACTIVE_METER_TIME)) {
                    setMinInactiveMeterTime(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_INACTIVE_METER_TIME)) {
                    setMaxInactiveMeterTime(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_KEEP_ALIVE_RETRIES)) {
                    setKeepAliveRetries(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_KEEP_ALIVE_TIMEOUT)) {
                    setKeepAliveTimeout(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.SECURITY_CHANGE_DLMS_AUTHENTICATION_LEVEL)) {
                    changeDlmAuthLevel(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.SECURITY_ACTIVATE_DLMS_ENCRYPTION)) {
                    activateDlmsEncryption(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.SECURITY_CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEY)) {
                    changeAuthKey(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.SECURITY_CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY)) {
                    changeEncryptionKey(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.SECURITY_CHANGE_HLS_SECRET_WITH_PASSWORD)) {
                    changeHlsSecret(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.GENERAL_WRITE_FULL_CONFIGURATION)) {
                    writeFullConfig(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.OUTPUT_CONFIGURATION_WRITE_OUTPUT_STATE)) {
                    writeOutputState(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.FIREWALL_ACTIVATE_FIREWALL)) {
                    activateFirewall();
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.FIREWALL_DEACTIVATE_FIREWALL)) {
                    deactivateFirewall();
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.FIREWALL_CONFIGURE_FW_GPRS)) {
                    configureFWGPRS(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.FIREWALL_CONFIGURE_FW_LAN)) {
                    configureFWLAN(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.FIREWALL_CONFIGURE_FW_WAN)) {
                    configureFWWAN(pendingMessage);
                } else if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.FIREWALL_SET_FW_DEFAULT_STATE)) {
                    setFWDefaultState(pendingMessage);
                } else {   //Unsupported message
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setFailureInformation(ResultType.NotSupported, createUnsupportedWarning(pendingMessage));
                }
            } catch (IOException e) {
                if (IOExceptionHandler.isUnexpectedResponse(e, session)) {
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
                }   //Else: throw communication exception
            } catch (IndexOutOfBoundsException | NumberFormatException e) {
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
            }
            result.addCollectedMessages(collectedMessage);
        }
        return result;
    }

    private void setFWDefaultState(OfflineDeviceMessage pendingMessage) throws IOException {
        this.session.getCosemObjectFactory().getFirewallSetup().setEnabledByDefault(getSingleBooleanAttribute(pendingMessage));
    }

    private void configureFWWAN(OfflineDeviceMessage pendingMessage) throws IOException {
        boolean isDLMSAllowed = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.EnableDLMS).getDeviceMessageAttributeValue());
        boolean isHTTPAllowed = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.EnableHTTP).getDeviceMessageAttributeValue());
        boolean isSSHAllowed = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.EnableSSH).getDeviceMessageAttributeValue());
        this.session.getCosemObjectFactory().getFirewallSetup().setWANPortSetup(new FirewallSetup.InterfaceFirewallConfiguration(isDLMSAllowed, isHTTPAllowed, isSSHAllowed));
    }

    private void configureFWLAN(OfflineDeviceMessage pendingMessage) throws IOException {
        boolean isDLMSAllowed = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.EnableDLMS).getDeviceMessageAttributeValue());
        boolean isHTTPAllowed = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.EnableHTTP).getDeviceMessageAttributeValue());
        boolean isSSHAllowed = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.EnableSSH).getDeviceMessageAttributeValue());
        this.session.getCosemObjectFactory().getFirewallSetup().setLANPortSetup(new FirewallSetup.InterfaceFirewallConfiguration(isDLMSAllowed, isHTTPAllowed, isSSHAllowed));
    }

    private void configureFWGPRS(OfflineDeviceMessage pendingMessage) throws IOException {
        boolean isDLMSAllowed = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.EnableDLMS).getDeviceMessageAttributeValue());
        boolean isHTTPAllowed = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.EnableHTTP).getDeviceMessageAttributeValue());
        boolean isSSHAllowed = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.EnableSSH).getDeviceMessageAttributeValue());
        this.session.getCosemObjectFactory().getFirewallSetup().setGPRSPortSetup(new FirewallSetup.InterfaceFirewallConfiguration(isDLMSAllowed, isHTTPAllowed, isSSHAllowed));
    }

    private void deactivateFirewall() throws IOException {
        this.session.getCosemObjectFactory().getFirewallSetup().deactivate();
    }

    private void activateFirewall() throws IOException {
        this.session.getCosemObjectFactory().getFirewallSetup().activate();
    }

    private void writeOutputState(OfflineDeviceMessage pendingMessage) throws IOException {
        int outputId = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.outputId).getDeviceMessageAttributeValue());
        boolean newState = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.newState).getDeviceMessageAttributeValue());

        final ObisCode obisCode = new ObisCode(0, outputId, 96, 3, 10, 255);
        final Disconnector disconnector = this.session.getCosemObjectFactory().getDisconnector(obisCode);
        if (newState) {
            disconnector.remoteReconnect();
        } else {
            disconnector.remoteDisconnect();
        }
    }

    private void writeFullConfig(OfflineDeviceMessage pendingMessage) throws IOException {
        String hex = pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue();
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
        String hex = pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue();
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getAssociationLN().changeHLSSecret(ProtocolTools.getBytesFromHexString(hex, ""));
    }

    private void changeEncryptionKey(OfflineDeviceMessage pendingMessage) throws IOException {
        String wrappedHexKey = pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue();
        Array encryptionKeyArray = new Array();
        Structure keyData = new Structure();
        keyData.addDataType(new TypeEnum(0));    // 0 means keyType: encryptionKey (global key)
        keyData.addDataType(OctetString.fromByteArray(ProtocolTools.getBytesFromHexString(wrappedHexKey)));
        encryptionKeyArray.addDataType(keyData);
        getSecuritySetup().transferGlobalKey(encryptionKeyArray);
    }

    private SecuritySetup getSecuritySetup() throws IOException {
        return this.session.getCosemObjectFactory().getSecuritySetup();
    }

    private void changeAuthKey(OfflineDeviceMessage pendingMessage) throws IOException {
        String wrappedHexKey = pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue();
        Array globalKeyArray = new Array();
        Structure keyData = new Structure();
        keyData.addDataType(new TypeEnum(2));    // 2 means keyType: authenticationKey
        keyData.addDataType(OctetString.fromByteArray(ProtocolTools.getBytesFromHexString(wrappedHexKey)));
        globalKeyArray.addDataType(keyData);
        getSecuritySetup().transferGlobalKey(globalKeyArray);
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

    private void setKeepAliveTimeout(OfflineDeviceMessage pendingMessage) throws IOException {
        this.session.getCosemObjectFactory().getG3NetworkManagement().setKeepAliveTimeout(getSingleIntegerAttribute(pendingMessage));
    }

    private void enableG3PLCInterface(OfflineDeviceMessage pendingMessage) throws IOException {
        this.session.getCosemObjectFactory().getG3NetworkManagement().enableG3Interface(getSingleBooleanAttribute(pendingMessage));
    }

    private void setKeepAliveRetries(OfflineDeviceMessage pendingMessage) throws IOException {
        this.session.getCosemObjectFactory().getG3NetworkManagement().setKeepAliveRetries(getSingleIntegerAttribute(pendingMessage));
    }

    private void setMaxInactiveMeterTime(OfflineDeviceMessage pendingMessage) throws IOException {
        this.session.getCosemObjectFactory().getG3NetworkManagement().setMaxInactiveMeterTime(getSingleIntegerAttribute(pendingMessage));
    }

    private void setMinInactiveMeterTime(OfflineDeviceMessage pendingMessage) throws IOException {
        this.session.getCosemObjectFactory().getG3NetworkManagement().setMinInactiveMeterTime(getSingleIntegerAttribute(pendingMessage));
    }

    private void setKeepAliveBucketSize(OfflineDeviceMessage pendingMessage) throws IOException {
        this.session.getCosemObjectFactory().getG3NetworkManagement().setKeepAliveBucketSize(getSingleIntegerAttribute(pendingMessage));
    }

    private void setKeepAliveScheduleInterval(OfflineDeviceMessage pendingMessage) throws IOException {
        this.session.getCosemObjectFactory().getG3NetworkManagement().setKeepAliveScheduleInterval(getSingleIntegerAttribute(pendingMessage));
    }

    private void enableKeepAlive(OfflineDeviceMessage pendingMessage) throws IOException {
        this.session.getCosemObjectFactory().getG3NetworkManagement().enableKeepAlive(getSingleBooleanAttribute(pendingMessage));
    }

    private void setSNRPayload(OfflineDeviceMessage pendingMessage) throws IOException {
        byte[] payLoad = ProtocolTools.getBytesFromHexString(pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue(), "");
        this.session.getCosemObjectFactory().getG3NetworkManagement().setSNRPayload(payLoad);
    }

    private void setSNRQuietTime(OfflineDeviceMessage pendingMessage) throws IOException {
        this.session.getCosemObjectFactory().getG3NetworkManagement().setSNRQuietTime(getSingleIntegerAttribute(pendingMessage));
    }

    private void setSNRPacketInterval(OfflineDeviceMessage pendingMessage) throws IOException {
        this.session.getCosemObjectFactory().getG3NetworkManagement().setSNRPacketInterval(getSingleIntegerAttribute(pendingMessage));
    }

    private void enableSNR(OfflineDeviceMessage pendingMessage) throws IOException {
        this.session.getCosemObjectFactory().getG3NetworkManagement().enableSNR(getSingleBooleanAttribute(pendingMessage));
    }

    private void setAutomaticRouteManagement(OfflineDeviceMessage pendingMessage) throws IOException {
        boolean pingEnabled = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.pingEnabled).getDeviceMessageAttributeValue());
        boolean routeRequestEnabled = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.routeRequestEnabled).getDeviceMessageAttributeValue());
        boolean pathRequestEnabled = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.pathRequestEnabled).getDeviceMessageAttributeValue());

        this.session.getCosemObjectFactory().getG3NetworkManagement().setAutomaticRouteManagement(pingEnabled, routeRequestEnabled, pathRequestEnabled);
    }

    private void setNTPAddress() {
        //TODO port from 8.11
    }

    private void syncNTPServer() throws IOException {
        this.session.getCosemObjectFactory().getNTPServerAddress().ntpSync();
    }

    private void setDeviceName(OfflineDeviceMessage pendingMessage) throws IOException {
        String name = pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue();
        this.session.getCosemObjectFactory().getData(DEVICE_NAME_OBISCODE).setValueAttr(OctetString.fromString(name));
    }

    private void rebootApplication() throws IOException {
        this.session.getCosemObjectFactory().getLifeCycleManagement().restartApplication();
    }

    private void rebootDevice() throws IOException {
        this.session.getCosemObjectFactory().getLifeCycleManagement().rebootDevice();
    }

    private void syncTime() throws IOException {
        Calendar cal = Calendar.getInstance(session.getTimeZone());
        Date currentTime = new Date();
        cal.setTime(currentTime);
        session.getCosemObjectFactory().getClock().setAXDRDateTimeAttr(new AXDRDateTime(cal));
    }

    private void enableSSL() {
        //TODO port from 8.11
    }

    private void setModemWatchdogParameters(OfflineDeviceMessage pendingMessage) throws IOException {
        int modemWatchdogInterval = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.modemWatchdogInterval).getDeviceMessageAttributeValue());
        int pppDaemonResetThreshold = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.PPPDaemonResetThreshold).getDeviceMessageAttributeValue());
        int modemResetThreshold = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.modemResetThreshold).getDeviceMessageAttributeValue());
        int systemRebootThreshold = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.systemRebootThreshold).getDeviceMessageAttributeValue());

        this.session.getCosemObjectFactory().getModemWatchdogConfiguration().writeConfigParameters(
                modemWatchdogInterval,
                pppDaemonResetThreshold,
                modemResetThreshold,
                systemRebootThreshold
        );
    }

    private void enableModemWatchdog(OfflineDeviceMessage pendingMessage) throws IOException {
        boolean enable = Boolean.parseBoolean(pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());
        this.session.getCosemObjectFactory().getModemWatchdogConfiguration().enableWatchdog(enable);
    }

    private void preferGPRSUpstreamCommunication() {
        //TODO port from 8.11
    }

    private void setPPPIdleTime() {
        //TODO port from 8.11
    }

    private void configurePushEventNotification(OfflineDeviceMessage pendingMessage) throws IOException {
        int transportType = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.transportTypeAttributeName).getDeviceMessageAttributeValue());
        String destinationAddress = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.destinationAddressAttributeName).getDeviceMessageAttributeValue();
        int messageType = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.messageTypeAttributeName).getDeviceMessageAttributeValue());

        this.session.getCosemObjectFactory().getEventPushNotificationConfig().writeSendDestinationAndMethod(transportType, destinationAddress, messageType);
    }

    private void writeUplinkPingTimeout(OfflineDeviceMessage pendingMessage) throws IOException {
        Integer timeout = Integer.valueOf(pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());
        this.session.getCosemObjectFactory().getUplinkPingConfiguration().writeTimeout(timeout);
    }

    private void changePasswordUser1(OfflineDeviceMessage pendingMessage) throws IOException {
        String newPassword = pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue();
        this.session.getCosemObjectFactory().getWebPortalPasswordConfig().changeUser1Password(newPassword);
    }

    private void changePasswordUser2(OfflineDeviceMessage pendingMessage) throws IOException {
        String newPassword = pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue();
        this.session.getCosemObjectFactory().getWebPortalPasswordConfig().changeUser2Password(newPassword);
    }

    private void writeUplinkPingInterval(OfflineDeviceMessage pendingMessage) throws IOException {
        Integer interval = Integer.valueOf(pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());
        this.session.getCosemObjectFactory().getUplinkPingConfiguration().writeInterval(interval);
    }

    private void writeUplinkPingDestinationAddress(OfflineDeviceMessage pendingMessage) throws IOException {
        String destinationAddress = pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue();
        this.session.getCosemObjectFactory().getUplinkPingConfiguration().writeDestAddress(destinationAddress);
    }

    private void enableUplinkPing(OfflineDeviceMessage pendingMessage) throws IOException {
        boolean enable = Boolean.parseBoolean(pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());
        this.session.getCosemObjectFactory().getUplinkPingConfiguration().enableUplinkPing(enable);
    }

    private class CollectedTopologyMessageInfo{
        private final CollectedTopology collectedTopology;
        private final String protocolMessageInfo;

        private CollectedTopologyMessageInfo(CollectedTopology collectedTopology, String protocolMessageInfo) {
            this.collectedTopology = collectedTopology;
            this.protocolMessageInfo = protocolMessageInfo;
        }
    }

    private CollectedTopologyMessageInfo pathRequest(OfflineDeviceMessage pendingMessage) throws IOException {
        CollectedTopology collectedTopology = null;
        String macAddressesString = pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue();
        final G3NetworkManagement topologyManagement = this.session.getCosemObjectFactory().getG3NetworkManagement();
        List<String> macAddresses = Arrays.asList(macAddressesString.split(";"));

        StringBuilder pingFailed = new StringBuilder();
        StringBuilder pingSuccess = new StringBuilder();
        StringBuilder pathFailed = new StringBuilder();
        long aarqTimeout = ((G3GatewayProperties) session.getProperties()).getAarqTimeout();
        long normalTimeout = session.getProperties().getTimeout();
        long pingTimeout = (aarqTimeout == 0 ? normalTimeout : aarqTimeout);
        long fullRoundTripTimeout = 30000 + pingTimeout;
         for (String macAddress : macAddresses) {
            session.getDLMSConnection().setTimeout(fullRoundTripTimeout);     //The ping request can take a long time, increase the timeout of the DLMS connection
            Integer ping;
            try {
                session.getLogger().info("Executing ping request to meter " + macAddress);
                ping = topologyManagement.pingNode(macAddress, (int) (pingTimeout / 1000));
            } catch (DataAccessResultException e) {
                ping = null;
                session.getLogger().warning("Meter " + macAddress + " is not registered to this concentrator! Will not execute path request for this meter.");
            } finally {
                session.getDLMSConnection().setTimeout(normalTimeout);
            }
            if (ping == null) {
                logFailedPingRequest(pingFailed, macAddress);
            } else if (ping > 0) {
                logSuccessfulPingRequest(pingSuccess, macAddress, ping);
                session.getLogger().info("Ping request for meter " + macAddress + " was successful (" + ping + " ms).");
                try {
                    session.getLogger().info("Executing path request to meter " + macAddress);
                    session.getDLMSConnection().setTimeout(fullRoundTripTimeout);
                    collectedTopology = this.deviceProtocol.getG3Topology().doPathRequestFor(macAddress);
                    session.getLogger().info("Path request for meter " + macAddress + " was successful.");
                } finally {
                    session.getDLMSConnection().setTimeout(normalTimeout);
                }
            } else if (ping <= 0) {
                session.getLogger().info("Ping failed for meter " + macAddress + ". Will not execute path request for this meter.");
                logFailedPingRequest(pingFailed, macAddress);
            }
        }
        String allInfo = (pingSuccess.length() == 0 ? "" : (pingSuccess + ". ")) + (pingFailed.length() == 0 ? "" : (pingFailed + ". ")) + (pathFailed.length() == 0 ? "" : (pathFailed + "."));

        if (pingFailed.toString().isEmpty() && pathFailed.toString().isEmpty()) {
            session.getLogger().info("Message result: ping and path requests were successful for every meter.");
        }
        return new CollectedTopologyMessageInfo(collectedTopology, allInfo);
    }

    private void logFailedPingRequest(StringBuilder pingFailed, String macAddress) {
        if (pingFailed.toString().isEmpty()) {
            pingFailed.append("Ping failed for: ");
            pingFailed.append(macAddress);
        } else {
            pingFailed.append(", ");
            pingFailed.append(macAddress);
        }
    }

    private void logSuccessfulPingRequest(StringBuilder pingSuccess, String macAddress, int pingTime) {
        if (pingSuccess.toString().isEmpty()) {
            pingSuccess.append("Ping successful for: ");
            pingSuccess.append(macAddress);
            if (pingTime > 1) {
                pingSuccess.append(" (").append(pingTime).append(" ms)");
            }
        } else {
            pingSuccess.append(", ");
            pingSuccess.append(macAddress);
            if (pingTime > 1) {
                pingSuccess.append(" (").append(pingTime).append(" ms)");
            }
        }
    }

    private void logFailedPathRequest(StringBuilder pathFailed, String macAddress) {
        if (pathFailed.toString().isEmpty()) {
            pathFailed.append("Path request failed for: ");
            pathFailed.append(macAddress);
        } else {
            pathFailed.append(", ");
            pathFailed.append(macAddress);
        }
    }

    //private String getProtocolInfo(int success, int total, int numberPingFailed, int numberPathFailed) {
    //    return "Successful for " + success + "/" + total + " device(s), ping failed for " + numberPingFailed + " device(s), path request failed for " + numberPathFailed + " device(s).";
    //}

    private void setMinBe(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeMinBE(getSingleIntegerAttribute(pendingMessage));

    }

    private void setMaxCSMABackOff(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeMaxCSMABackOff(getSingleIntegerAttribute(pendingMessage));
    }

    private void setMaxBe(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeMaxBE(getSingleIntegerAttribute(pendingMessage));
    }

    private void setMinimumCWAttempts(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeMinCWAttempts(getSingleIntegerAttribute(pendingMessage));
    }

    private void setMacK(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeMacK(getSingleIntegerAttribute(pendingMessage));
    }

    private void setMacA(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeMacA(getSingleIntegerAttribute(pendingMessage));
    }

    private void setBeaconRandomizationWindowLength(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeBeaconRandomizationWindowLength(getSingleIntegerAttribute(pendingMessage));
    }

    private void setCSMAFairnessLimit(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeCSMAFairnessLimit(getSingleIntegerAttribute(pendingMessage));
    }

    private void setHighPriorityWindowSize(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeHighPriorityWindowSize(getSingleIntegerAttribute(pendingMessage));
    }

    private void setNeighbourTableEntryTTL(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeNeighbourTableEntryTTL(getSingleIntegerAttribute(pendingMessage));
    }

    private void setMaxFrameRetries(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeMaxFrameRetries(getSingleIntegerAttribute(pendingMessage));
    }

    private void setTMRTTL(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeTMRTTL(getSingleIntegerAttribute(pendingMessage));
    }

    private void setToneMask(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        boolean[] toneMask = toBooleanArray(pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());
        cof.getPLCOFDMType2MACSetup().writeToneMask(toneMask);
    }

    /**
     * Converts a list of 1's and 0's into a boolean array, e.g. 10101 = [true, false, true, false, true]
     */
    private boolean[] toBooleanArray(final String value) {
        final String cleanBooleanString = value.toUpperCase().replaceAll("[^0-1]", "");
        boolean[] booleans = new boolean[cleanBooleanString.length()];
        for (int i = 0; i < booleans.length; i++) {
            booleans[i] = cleanBooleanString.charAt(i) == '1';
        }
        return booleans;
    }

    private void setPanId(OfflineDeviceMessage pendingMessage) throws IOException {
        this.session.getCosemObjectFactory().getPLCOFDMType2MACSetup().writePANID(getSingleIntegerAttribute(pendingMessage));
    }

    private void resetPlcOfdmMacCounters() throws IOException {
        this.session.getCosemObjectFactory().getPLCOFDMType2PHYAndMACCounters().reset();
    }

    private void setDeviceType(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeDeviceType(getSingleIntegerAttribute(pendingMessage));
    }

    private void setDisableDefaultRouting(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeDisableDefaultRouting(getSingleBooleanAttribute(pendingMessage));
    }

    private void setCoordShortAddress(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeCoordShortAddress(getSingleIntegerAttribute(pendingMessage));
    }

    private void setMetricType(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeMetricType(getSingleIntegerAttribute(pendingMessage));
    }

    private void setPathDiscoveryTime(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writePathDiscoveryTime(getSingleIntegerAttribute(pendingMessage));
    }

    private void setMaxJoinWaitTime(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeMaxJoinWaitTime(getSingleIntegerAttribute(pendingMessage));
    }

    private void setBroadCastLogTableEntryTTL(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeBroadcastLogTableTTL(getSingleIntegerAttribute(pendingMessage));
    }

    private void setRoutingConfiguration(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();

        int adp_net_traversal_time = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_net_traversal_time).getDeviceMessageAttributeValue());
        int adp_routing_table_entry_TTL = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_routing_table_entry_TTL).getDeviceMessageAttributeValue());
        int adp_Kr = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_Kr).getDeviceMessageAttributeValue());
        int adp_Km = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_Km).getDeviceMessageAttributeValue());
        int adp_Kc = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_Kc).getDeviceMessageAttributeValue());
        int adp_Kq = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_Kq).getDeviceMessageAttributeValue());
        int adp_Kh = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_Kh).getDeviceMessageAttributeValue());
        int adp_Krt = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_Krt).getDeviceMessageAttributeValue());
        int adp_RREQ_retries = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_RREQ_retries).getDeviceMessageAttributeValue());
        int adp_RREQ_RERR_wait = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_RREQ_RERR_wait).getDeviceMessageAttributeValue());
        int adp_Blacklist_table_entry_TTL = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_Blacklist_table_entry_TTL).getDeviceMessageAttributeValue());
        boolean adp_unicast_RREQ_gen_enable = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_unicast_RREQ_gen_enable).getDeviceMessageAttributeValue());
        boolean adp_RLC_enabled = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_RLC_enabled).getDeviceMessageAttributeValue());
        int adp_add_rev_link_cost = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_add_rev_link_cost).getDeviceMessageAttributeValue());

        cof.getSixLowPanAdaptationLayerSetup().writeRoutingConfiguration(
                adp_net_traversal_time,
                adp_routing_table_entry_TTL,
                adp_Kr,
                adp_Km,
                adp_Kc,
                adp_Kq,
                adp_Kh,
                adp_Krt,
                adp_RREQ_retries,
                adp_RREQ_RERR_wait,
                adp_Blacklist_table_entry_TTL,
                adp_unicast_RREQ_gen_enable,
                adp_RLC_enabled,
                adp_add_rev_link_cost
        );
    }

    private void setSecurityLevelpendingMessage(OfflineDeviceMessage pendingMessage) throws IOException {
        int value = getSingleIntegerAttribute(pendingMessage);
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeSecurityLevel(value);
    }

    private void setWeakLQIValue(OfflineDeviceMessage pendingMessage) throws IOException {
        int value = getSingleIntegerAttribute(pendingMessage);
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeWeakLqiValue(value);
    }

    private void setMaxNumberOfHops(OfflineDeviceMessage pendingMessage) throws IOException {
        int value = getSingleIntegerAttribute(pendingMessage);
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeMaxHops(value);
    }

    private int getSingleIntegerAttribute(OfflineDeviceMessage pendingMessage) {
        return Integer.parseInt(pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());
    }

    private boolean getSingleBooleanAttribute(OfflineDeviceMessage pendingMessage) {
        return Boolean.parseBoolean(pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());
    }

    protected CollectedMessage createCollectedMessage(OfflineDeviceMessage message) {
        return this.collectedDataFactory.createCollectedMessage(message.getIdentifier());
    }

    protected Issue createMessageFailedIssue(OfflineDeviceMessage pendingMessage, Exception e) {
        return createMessageFailedIssue(pendingMessage, e.getMessage());
    }

    protected Issue createMessageFailedIssue(OfflineDeviceMessage pendingMessage, String message) {
        return this.issueService.newWarning(
                pendingMessage,
                MessageSeeds.DEVICEMESSAGE_FAILED.getKey(),
                pendingMessage.getDeviceMessageId(),
                pendingMessage.getSpecification().getCategory().getName(),
                pendingMessage.getSpecification().getName(),
                message);
    }

    protected Issue createUnsupportedWarning(OfflineDeviceMessage pendingMessage) throws IOException {
        return this.issueService.newWarning(
                pendingMessage,
                MessageSeeds.DEVICEMESSAGE_NOT_SUPPORTED.getKey(),
                pendingMessage.getDeviceMessageId(),
                pendingMessage.getSpecification().getCategory().getName(),
                pendingMessage.getSpecification().getName());
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return this.collectedDataFactory.createEmptyCollectedMessageList();  //Nothing to do here
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(DeviceMessageConstants.broadCastLogTableEntryTTLAttributeName)) {
            return String.valueOf(((TimeDuration) messageAttribute).getSeconds());
        } else if (propertySpec.getName().equals(DeviceMessageConstants.configUserFileAttributeName)) {
            FirmwareVersion firmwareVersion = ((FirmwareVersion) messageAttribute);
            return GenericMessaging.zipAndB64EncodeContent(firmwareVersion.getFirmwareFile());  //Bytes of the firmwareFile as string
        } else if (propertySpec.getName().equals(DeviceMessageConstants.encryptionLevelAttributeName)) {
            return String.valueOf(DlmsEncryptionLevelMessageValues.getValueFor(messageAttribute.toString()));
        } else if (propertySpec.getName().equals(DeviceMessageConstants.authenticationLevelAttributeName)) {
            return String.valueOf(DlmsAuthenticationLevelMessageValues.getValueFor(messageAttribute.toString()));
        } else if (propertySpec.getName().equals(DeviceMessageConstants.deviceListAttributeName)) {

            //TODO fix it!

//            Group group = (Group) messageAttribute;
            StringBuilder macAddresses = new StringBuilder();
//            for (BusinessObject businessObject : group.getMembers()) {
//                if (businessObject instanceof Device) {
//                    Device device = (Device) businessObject;
//                    String callHomeId = device.getProtocolProperties().<String>getTypedProperty(com.energyict.mdc.protocol.api.DeviceProtocolProperty.callHomeId.name(), "");
//                    if (!callHomeId.isEmpty()) {
//                        if (macAddresses.length() != 0) {
//                            macAddresses.append(";");
//                        }
//                        macAddresses.append(callHomeId);
//                    }
//                } else {
//                    TODO throw proper exception
//                }
//            }
            return macAddresses.toString();
        } else if (propertySpec.getName().equals(DeviceMessageConstants.newAuthenticationKeyAttributeName)
                || propertySpec.getName().equals(DeviceMessageConstants.newPasswordAttributeName)
                || propertySpec.getName().equals(DeviceMessageConstants.newEncryptionKeyAttributeName)) {
            return ((Password) messageAttribute).getValue();
        } else {
            return propertySpec.toString();     //Works for BigDecimal, boolean and (hex)string propertyspecs
        }
    }
}