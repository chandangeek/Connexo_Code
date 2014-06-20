package com.energyict.protocolimplv2.eict.rtuplusserver.g3.messages;

import com.energyict.cbo.Password;
import com.energyict.cbo.TimeDuration;
import com.energyict.cpo.BusinessObject;
import com.energyict.cpo.PropertySpec;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.messages.DeviceMessageStatus;
import com.energyict.mdc.meterdata.CollectedMessage;
import com.energyict.mdc.meterdata.CollectedMessageList;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdc.meterdata.identifiers.DeviceMessageIdentifierById;
import com.energyict.mdc.protocol.tasks.support.DeviceMessageSupport;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.Group;
import com.energyict.mdw.core.UserFile;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocolimpl.dlms.idis.xml.XMLParser;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.properties.G3GatewayProperties;
import com.energyict.protocolimplv2.messages.*;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.enums.DlmsAuthenticationLevelMessageValues;
import com.energyict.protocolimplv2.messages.enums.DlmsEncryptionLevelMessageValues;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;

import java.io.IOException;
import java.text.ParseException;
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
    private List<DeviceMessageSpec> supportedMessages = null;
    public static final String CALL_HOME_ID_PROPERTY_NAME = "callHomeId";

    public RtuPlusServerMessages(DlmsSession session) {
        this.session = session;
    }

    public List<DeviceMessageSpec> getSupportedMessages() {
        if (supportedMessages == null) {
            supportedMessages = new ArrayList<>();

            supportedMessages.add(PLCConfigurationDeviceMessage.SetMaxNumberOfHopsAttributeName);
            supportedMessages.add(PLCConfigurationDeviceMessage.SetWeakLQIValueAttributeName);
            supportedMessages.add(PLCConfigurationDeviceMessage.SetSecurityLevel);
            supportedMessages.add(PLCConfigurationDeviceMessage.SetRoutingConfiguration);
            supportedMessages.add(PLCConfigurationDeviceMessage.SetBroadCastLogTableEntryTTLAttributeName);
            supportedMessages.add(PLCConfigurationDeviceMessage.SetMaxJoinWaitTime);
            supportedMessages.add(PLCConfigurationDeviceMessage.SetPathDiscoveryTime);
            supportedMessages.add(PLCConfigurationDeviceMessage.SetMetricType);
            supportedMessages.add(PLCConfigurationDeviceMessage.SetCoordShortAddress);
            supportedMessages.add(PLCConfigurationDeviceMessage.SetDisableDefaultRouting);
            supportedMessages.add(PLCConfigurationDeviceMessage.SetDeviceType);

            supportedMessages.add(PLCConfigurationDeviceMessage.ResetPlcOfdmMacCounters);

            supportedMessages.add(PLCConfigurationDeviceMessage.SetPanId);
            supportedMessages.add(PLCConfigurationDeviceMessage.SetToneMaskAttributeName);
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
            supportedMessages.add(PLCConfigurationDeviceMessage.PathRequest);

            supportedMessages.add(DeviceActionMessage.REBOOT_DEVICE);

            supportedMessages.add(SecurityMessage.CHANGE_DLMS_AUTHENTICATION_LEVEL);
            supportedMessages.add(SecurityMessage.ACTIVATE_DLMS_ENCRYPTION);
            supportedMessages.add(SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEY);
            supportedMessages.add(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY);
            supportedMessages.add(SecurityMessage.CHANGE_HLS_SECRET_HEX);
            supportedMessages.add(SecurityMessage.CHANGE_LLS_SECRET_HEX);

            supportedMessages.add(GeneralDeviceMessage.WRITE_FULL_CONFIGURATION);
            supportedMessages.add(OutputConfigurationMessage.WriteOutputState);

            supportedMessages.add(UplinkConfigurationDeviceMessage.EnableUplinkPing);
            supportedMessages.add(UplinkConfigurationDeviceMessage.WriteUplinkPingDestinationAddress);
            supportedMessages.add(UplinkConfigurationDeviceMessage.WriteUplinkPingInterval);
            supportedMessages.add(UplinkConfigurationDeviceMessage.WriteUplinkPingTimeout);
            supportedMessages.add(PPPConfigurationDeviceMessage.SetPPPIdleTime);
            supportedMessages.add(NetworkConnectivityMessage.PreferGPRSUpstreamCommunication);
            supportedMessages.add(NetworkConnectivityMessage.EnableModemWatchdog);
            supportedMessages.add(NetworkConnectivityMessage.SetModemWatchdogInterval);
            supportedMessages.add(PPPConfigurationDeviceMessage.SetPPPDaemonResetThreshold);
            supportedMessages.add(NetworkConnectivityMessage.SetModemResetThreshold);
            supportedMessages.add(ConfigurationChangeDeviceMessage.SetSystemRebootThreshold);
            supportedMessages.add(ConfigurationChangeDeviceMessage.EnableSSL);

            //G3 interface messages
            supportedMessages.add(PLCConfigurationDeviceMessage.SetAutomaticRouteManagement);
            supportedMessages.add(PLCConfigurationDeviceMessage.EnableSNR);
            supportedMessages.add(PLCConfigurationDeviceMessage.SetSNRPacketInterval);
            supportedMessages.add(PLCConfigurationDeviceMessage.SetSNRQuietTime);
            supportedMessages.add(PLCConfigurationDeviceMessage.SetSNRPayload);
            supportedMessages.add(PLCConfigurationDeviceMessage.EnableKeepAlive);
            supportedMessages.add(PLCConfigurationDeviceMessage.SetKeepAliveScheduleInterval);
            supportedMessages.add(PLCConfigurationDeviceMessage.SetKeepAliveBucketSize);
            supportedMessages.add(PLCConfigurationDeviceMessage.SetMinInactiveMeterTime);
            supportedMessages.add(PLCConfigurationDeviceMessage.SetMaxInactiveMeterTime);
            supportedMessages.add(PLCConfigurationDeviceMessage.SetKeepAliveRetries);
            supportedMessages.add(PLCConfigurationDeviceMessage.SetKeepAliveTimeout);

            supportedMessages.add(ClockDeviceMessage.SyncTime);
            supportedMessages.add(ConfigurationChangeDeviceMessage.SetDeviceName);
            supportedMessages.add(ConfigurationChangeDeviceMessage.SetNTPAddress);
            supportedMessages.add(DeviceActionMessage.RebootApplication);

            supportedMessages.add(FirewallConfigurationMessage.ActivateFirewall);
            supportedMessages.add(FirewallConfigurationMessage.DeactivateFirewall);
            supportedMessages.add(FirewallConfigurationMessage.ConfigureFWGPRS);
            supportedMessages.add(FirewallConfigurationMessage.ConfigureFWLAN);
            supportedMessages.add(FirewallConfigurationMessage.ConfigureFWWAN);
            supportedMessages.add(FirewallConfigurationMessage.SetFWDefaultState);
        }
        return supportedMessages;
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList result = MdcManager.getCollectedDataFactory().createCollectedMessageList(pendingMessages);
        for (OfflineDeviceMessage pendingMessage : pendingMessages) {
            CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);   //Optimistic
            try {
                if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetMaxNumberOfHopsAttributeName)) {
                    setMaxNumberOfHops(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetWeakLQIValueAttributeName)) {
                    setWeakLQIValue(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetSecurityLevel)) {
                    setSecurityLevelpendingMessage(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetRoutingConfiguration)) {
                    setRoutingConfiguration(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetBroadCastLogTableEntryTTLAttributeName)) {
                    setBroadCastLogTableEntryTTL(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetMaxJoinWaitTime)) {
                    setMaxJoinWaitTime(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetPathDiscoveryTime)) {
                    setPathDiscoveryTime(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetMetricType)) {
                    setMetricType(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetCoordShortAddress)) {
                    setCoordShortAddress(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetDisableDefaultRouting)) {
                    setDisableDefaultRouting(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetDeviceType)) {
                    setDeviceType(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.ResetPlcOfdmMacCounters)) {
                    resetPlcOfdmMacCounters(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetPanId)) {
                    setPanId(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetToneMaskAttributeName)) {
                    setToneMask(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetTMRTTL)) {
                    setTMRTTL(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetMaxFrameRetries)) {
                    setMaxFrameRetries(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetNeighbourTableEntryTTL)) {
                    setNeighbourTableEntryTTL(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetHighPriorityWindowSize)) {
                    setHighPriorityWindowSize(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetCSMAFairnessLimit)) {
                    setCSMAFairnessLimit(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetBeaconRandomizationWindowLength)) {
                    setBeaconRandomizationWindowLength(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetMacA)) {
                    setMacA(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetMacK)) {
                    setMacK(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetMinimumCWAttempts)) {
                    setMinimumCWAttempts(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetMaxBe)) {
                    setMaxBe(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetMaxCSMABackOff)) {
                    setMaxCSMABackOff(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetMinBe)) {
                    setMinBe(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.PathRequest)) {
                    String feedback = pathRequest(pendingMessage);
                    collectedMessage.setDeviceProtocolInformation(feedback);
                } else if (pendingMessage.getSpecification().equals(UplinkConfigurationDeviceMessage.EnableUplinkPing)) {
                    enableUplinkPing(pendingMessage);
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
                } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.SetModemWatchdogInterval)) {
                    setModemWatchdogInterval(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(PPPConfigurationDeviceMessage.SetPPPDaemonResetThreshold)) {
                    setPPPDaemonResetThreshold(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.SetModemResetThreshold)) {
                    setModemResetThreshold(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.SetSystemRebootThreshold)) {
                    setSystemRebootThreshold(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.EnableSSL)) {
                    enableSSL(pendingMessage);
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
                } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetAutomaticRouteManagement)) {
                    setAutomaticRouteManagement(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.EnableSNR)) {
                    enableSNR(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetSNRPacketInterval)) {
                    setSNRPacketInterval(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetSNRQuietTime)) {
                    setSNRQuietTime(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetSNRPayload)) {
                    setSNRPayload(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.EnableKeepAlive)) {
                    enableKeepAlive(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetKeepAliveScheduleInterval)) {
                    setKeepAliveScheduleInterval(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetKeepAliveBucketSize)) {
                    setKeepAliveBucketSize(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetMinInactiveMeterTime)) {
                    setMinInactiveMeterTime(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetMaxInactiveMeterTime)) {
                    setMaxInactiveMeterTime(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetKeepAliveRetries)) {
                    setKeepAliveRetries(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetKeepAliveTimeout)) {
                    setKeepAliveTimeout(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_DLMS_AUTHENTICATION_LEVEL)) {
                    changeDlmAuthLevel(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(SecurityMessage.ACTIVATE_DLMS_ENCRYPTION)) {
                    activateDlmsEncryption(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEY)) {
                    changeAuthKey(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY)) {
                    changeEncryptionKey(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_HLS_SECRET_HEX)) {
                    changeHlsSecret(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_LLS_SECRET_HEX)) {
                    changeLlsSecdret(pendingMessage);
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
                }
            } catch (IOException e) {
                if (IOExceptionHandler.isUnexpectedResponse(e, session)) {
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
                }   //Else: throw communication exception
            } catch (IndexOutOfBoundsException | ParseException | NumberFormatException e) {
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
            }
            result.addCollectedMessage(collectedMessage);
        }
        return result;
    }

    private void setFWDefaultState(OfflineDeviceMessage pendingMessage) throws IOException, ParseException {
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

    private void deactivateFirewall(OfflineDeviceMessage pendingMessage) throws IOException {
        this.session.getCosemObjectFactory().getFirewallSetup().deactivate();
    }

    private void activateFirewall(OfflineDeviceMessage pendingMessage) throws IOException {
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

    private void changeLlsSecdret(OfflineDeviceMessage pendingMessage) throws IOException {
        String hex = pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue();
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getAssociationLN().writeSecret(OctetString.fromByteArray(ProtocolTools.getBytesFromHexString(hex, "")));
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

    private void setNTPAddress(OfflineDeviceMessage pendingMessage) throws IOException {
        //TODO port from 8.11
    }

    private void setDeviceName(OfflineDeviceMessage pendingMessage) throws IOException {
        //TODO port from 8.11
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

    private void setSystemRebootThreshold(OfflineDeviceMessage pendingMessage) throws IOException {
        //TODO port from 8.11
    }

    private void setModemResetThreshold(OfflineDeviceMessage pendingMessage) throws IOException {
        //TODO port from 8.11
    }

    private void setPPPDaemonResetThreshold(OfflineDeviceMessage pendingMessage) throws IOException {
        //TODO port from 8.11
    }

    private void setModemWatchdogInterval(OfflineDeviceMessage pendingMessage) throws IOException {
        //TODO port from 8.11
    }

    private void enableModemWatchdog(OfflineDeviceMessage pendingMessage) throws IOException {
        //TODO port from 8.11
    }

    private void preferGPRSUpstreamCommunication(OfflineDeviceMessage pendingMessage) throws IOException {
        //TODO port from 8.11
    }

    private void setPPPIdleTime(OfflineDeviceMessage pendingMessage) throws IOException {
        //TODO port from 8.11
    }

    private void writeUplinkPingTimeout(OfflineDeviceMessage pendingMessage) throws IOException {
        //TODO port from 8.11
    }

    private void writeUplinkPingInterval(OfflineDeviceMessage pendingMessage) throws IOException {
        //TODO port from 8.11
    }

    private void writeUplinkPingDestinationAddress(OfflineDeviceMessage pendingMessage) throws IOException {
        //TODO port from 8.11
    }

    private void enableUplinkPing(OfflineDeviceMessage pendingMessage) throws IOException {
        //TODO port from 8.11
    }

    private String pathRequest(OfflineDeviceMessage pendingMessage) throws IOException {
        String macAddressesString = pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue();
        final G3NetworkManagement topologyManagement = this.session.getCosemObjectFactory().getG3NetworkManagement();
        List<String> macAddresses = Arrays.asList(macAddressesString.split(";"));

        long aarqTimeout = ((G3GatewayProperties) session.getProperties()).getAarqTimeout();
        long normalTimeout = session.getProperties().getTimeout();
        long pingTimeout = (aarqTimeout == 0 ? normalTimeout : aarqTimeout);
        long fullRoundTripTimeout = 30000 + pingTimeout;
        int numberPingFailed = 0;
        int numberPathFailed = 0;
        int success = 0;
        for (String macAddress : macAddresses) {
            session.getDLMSConnection().setTimeout(fullRoundTripTimeout);     //The ping request can take a long time, increase the timeout of the DLMS connection
            Integer ping;
            try {
                ping = topologyManagement.pingNode(macAddress, (int) (pingTimeout / 1000));
            } catch (DataAccessResultException e) {
                ping = null;
            } finally {
                session.getDLMSConnection().setTimeout(normalTimeout);
            }
            if (ping == null) {
                numberPingFailed++;
            } else if (ping > 0) {
                try {
                    session.getDLMSConnection().setTimeout(fullRoundTripTimeout);
                    topologyManagement.requestPath(macAddress);     //If successful, it will be added in the topology of the RTU+Server
                    success++;
                } catch (DataAccessResultException e) {
                    numberPathFailed++;
                } finally {
                    session.getDLMSConnection().setTimeout(normalTimeout);
                }
            } else if (ping <= 0) {
                numberPingFailed++;
            }
        }

        return getProtocolInfo(success, macAddresses.size(), numberPingFailed, numberPathFailed);
    }

    private String getProtocolInfo(int success, int total, int numberPingFailed, int numberPathFailed) {
        return "Successful for " + success + "/" + total + " device(s), ping failed for " + numberPingFailed + " device(s), path request failed for " + numberPathFailed + " device(s).";
    }

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

    private void resetPlcOfdmMacCounters(OfflineDeviceMessage pendingMessage) throws IOException {
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
        return MdcManager.getCollectedDataFactory().createCollectedMessage(new DeviceMessageIdentifierById(message.getDeviceMessageId()));
    }

    protected Issue createMessageFailedIssue(OfflineDeviceMessage pendingMessage, Exception e) {
        return createMessageFailedIssue(pendingMessage, e.getMessage());
    }

    protected Issue createMessageFailedIssue(OfflineDeviceMessage pendingMessage, String message) {
        return MdcManager.getIssueCollector().addWarning(pendingMessage, "DeviceMessage.failed",
                pendingMessage.getDeviceMessageId(),
                pendingMessage.getSpecification().getCategory().getName(),
                pendingMessage.getSpecification().getName(),
                message);
    }

    protected Issue createUnsupportedWarning(OfflineDeviceMessage pendingMessage) throws IOException {
        return MdcManager.getIssueCollector().addWarning(pendingMessage, "DeviceMessage.notSupported",
                pendingMessage.getDeviceMessageId(),
                pendingMessage.getSpecification().getCategory().getName(),
                pendingMessage.getSpecification().getName());
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return MdcManager.getCollectedDataFactory().createEmptyCollectedMessageList();  //Nothing to do here
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(DeviceMessageConstants.broadCastLogTableEntryTTLAttributeName)) {
            return String.valueOf(((TimeDuration) messageAttribute).getSeconds());
        } else if (propertySpec.getName().equals(DeviceMessageConstants.configUserFileAttributeName)) {
            return ProtocolTools.getHexStringFromBytes(((UserFile) propertySpec).loadFileInByteArray(), "");
        } else if (propertySpec.getName().equals(DeviceMessageConstants.encryptionLevelAttributeName)) {
            return String.valueOf(DlmsEncryptionLevelMessageValues.getValueFor(messageAttribute.toString()));
        } else if (propertySpec.getName().equals(DeviceMessageConstants.authenticationLevelAttributeName)) {
            return String.valueOf(DlmsAuthenticationLevelMessageValues.getValueFor(messageAttribute.toString()));
        } else if (propertySpec.getName().equals(DeviceMessageConstants.deviceGroupAttributeName)) {
            Group group = (Group) messageAttribute;
            StringBuilder macAddresses = new StringBuilder();
            for (BusinessObject businessObject : group.getMembers()) {
                if (businessObject instanceof Device) {
                    Device device = (Device) businessObject;
                    String callHomeId = device.getProtocolProperties().<String>getTypedProperty(CALL_HOME_ID_PROPERTY_NAME, "");
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
                || propertySpec.getName().equals(DeviceMessageConstants.newEncryptionKeyAttributeName)) {
            return ((Password) messageAttribute).getValue();
        } else {
            return propertySpec.toString();     //Works for BigDecimal, boolean and (hex)string propertyspecs
        }
    }
}