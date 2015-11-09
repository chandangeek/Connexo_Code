package com.energyict.protocolimplv2.eict.rtuplusserver.g3.messages;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.G3NetworkManagement;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedMessage;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.properties.G3GatewayProperties;
import com.energyict.protocolimplv2.identifiers.RegisterDataIdentifierByObisCodeAndDevice;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;

import java.io.IOException;
import java.time.Clock;
import java.util.*;

/**
 * Helper class that groups all logic related to the execution of the (standard) PLC messages. <br/>
 * This class can be re-used by multiple protocols, who all need/use the same PLC messages.
 *
 * @author sva
 * @since 12/08/2015 - 9:50
 */
public class PLCConfigurationDeviceMessageExecutor {

    private static final int MAX_REGISTER_TEXT_SIZE = 3800;  //The register text field is 4000 chars maximum
    private static final ObisCode PLC_G3_TIMEOUT_OBISCODE = ObisCode.fromString("0.0.94.33.10.255");
    private static final ObisCode PLC_G3_KEEP_ALIVE_OBISCODE = ObisCode.fromString("0.0.94.33.11.255");

    private final DlmsSession session;
    private final OfflineDevice offlineDevice;
    private final CollectedDataFactory collectedDataFactory;
    private final Clock clock;
    private final IssueService issueService;

    public PLCConfigurationDeviceMessageExecutor(DlmsSession session, OfflineDevice offlineDevice, CollectedDataFactory collectedDataFactory, Clock clock, IssueService issueService) {
        this.session = session;
        this.offlineDevice = offlineDevice;
        this.collectedDataFactory = collectedDataFactory;
        this.clock = clock;
        this.issueService = issueService;
    }

    public boolean executePendingMessage(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        if (pendingMessage.getSpecification().equals(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_NUMBER_OF_HOPS_ATTRIBUTENAME)) {
            setMaxNumberOfHops(pendingMessage);
        } else if (pendingMessage.getSpecification().getId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_WEAK_LQI_VALUE_ATTRIBUTENAME)) {
            setWeakLQIValue(pendingMessage);
        } else if (pendingMessage.getSpecification().getId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_SECURITY_LEVEL)) {
            setSecurityLevelpendingMessage(pendingMessage);
        } else if (pendingMessage.getSpecification().getId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_ROUTING_CONFIGURATION)) {
            setRoutingConfiguration(pendingMessage);
        } else if (pendingMessage.getSpecification().getId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_BROAD_CAST_LOG_TABLE_ENTRY_TTL)) {
            setBroadCastLogTableEntryTTL(pendingMessage);
        } else if (pendingMessage.getSpecification().getId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_JOIN_WAIT_TIME)) {
            setMaxJoinWaitTime(pendingMessage);
        } else if (pendingMessage.getSpecification().getId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_PATH_DISCOVERY_TIME)) {
            setPathDiscoveryTime(pendingMessage);
        } else if (pendingMessage.getSpecification().getId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_METRIC_TYPE)) {
            setMetricType(pendingMessage);
        } else if (pendingMessage.getSpecification().getId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_COORD_SHORT_ADDRESS)) {
            setCoordShortAddress(pendingMessage);
        } else if (pendingMessage.getSpecification().getId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_DISABLE_DEFAULT_ROUTING)) {
            setDisableDefaultRouting(pendingMessage);
        } else if (pendingMessage.getSpecification().getId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_DEVICE_TYPE)) {
            setDeviceType(pendingMessage);
        } else if (pendingMessage.getSpecification().getId().equals(DeviceMessageId.PLC_CONFIGURATION_RESET_PLC_OFDM_MAC_COUNTERS)) {
            resetPlcOfdmMacCounters(pendingMessage);
        } else if (pendingMessage.getSpecification().getId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_PAN_ID)) {
            setPanId(pendingMessage);
        } else if (pendingMessage.getSpecification().getId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_TONE_MASK_ATTRIBUTE_NAME)) {
            setToneMask(pendingMessage);
        } else if (pendingMessage.getSpecification().getId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_TMR_TTL)) {
            setTMRTTL(pendingMessage);
        } else if (pendingMessage.getSpecification().getId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_FRAME_RETRIES)) {
            setMaxFrameRetries(pendingMessage);
        } else if (pendingMessage.getSpecification().getId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_NEIGHBOUR_TABLE_ENTRY_TTL)) {
            setNeighbourTableEntryTTL(pendingMessage);
        } else if (pendingMessage.getSpecification().getId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_HIGH_PRIORITY_WINDOW_SIZE)) {
            setHighPriorityWindowSize(pendingMessage);
        } else if (pendingMessage.getSpecification().getId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_CSMA_FAIRNESS_LIMIT)) {
            setCSMAFairnessLimit(pendingMessage);
        } else if (pendingMessage.getSpecification().getId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_BEACON_RANDOMIZATION_WINDOW_LENGTH)) {
            setBeaconRandomizationWindowLength(pendingMessage);
        } else if (pendingMessage.getSpecification().getId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_MAC_A)) {
            setMacA(pendingMessage);
        } else if (pendingMessage.getSpecification().getId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_MAC_K)) {
            setMacK(pendingMessage);
        } else if (pendingMessage.getSpecification().getId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_MINIMUM_CW_ATTEMPTS)) {
            setMinimumCWAttempts(pendingMessage);
        } else if (pendingMessage.getSpecification().getId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_BE)) {
            setMaxBe(pendingMessage);
        } else if (pendingMessage.getSpecification().getId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_CSMA_BACK_OFF)) {
            setMaxCSMABackOff(pendingMessage);
        } else if (pendingMessage.getSpecification().getId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_MIN_BE)) {
            setMinBe(pendingMessage);
        } else if (pendingMessage.getSpecification().getId().equals(DeviceMessageId.PLC_CONFIGURATION_PATH_REQUEST)) {
            PathRequestFeedback pathRequestFeedback = pathRequest(pendingMessage);
            collectedMessage = createCollectedMessageWithRegisterData(pendingMessage, pathRequestFeedback.getRegisters());
            collectedMessage.setDeviceProtocolInformation(pathRequestFeedback.getFeedback());
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
        } else if (pendingMessage.getSpecification().getId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_AUTOMATIC_ROUTE_MANAGEMENT)) {
            setAutomaticRouteManagement(pendingMessage);
        } else if (pendingMessage.getSpecification().getId().equals(DeviceMessageId.PLC_CONFIGURATION_ENABLE_SNR)) {
            enableSNR(pendingMessage);
        } else if (pendingMessage.getSpecification().getId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_SNR_PACKET_INTERVAL)) {
            setSNRPacketInterval(pendingMessage);
        } else if (pendingMessage.getSpecification().getId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_SNR_QUIET_TIME)) {
            setSNRQuietTime(pendingMessage);
        } else if (pendingMessage.getSpecification().getId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_SNR_PAYLOAD)) {
            setSNRPayload(pendingMessage);
        } else if (pendingMessage.getSpecification().getId().equals(DeviceMessageId.PLC_CONFIGURATION_ENABLE_KEEP_ALIVE)) {
            enableKeepAlive(pendingMessage);
        } else if (pendingMessage.getSpecification().getId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_KEEP_ALIVE_SCHEDULE_INTERVAL)) {
            setKeepAliveScheduleInterval(pendingMessage);
        } else if (pendingMessage.getSpecification().getId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_KEEP_ALIVE_BUCKET_SIZE)) {
            setKeepAliveBucketSize(pendingMessage);
        } else if (pendingMessage.getSpecification().getId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_MIN_INACTIVE_METER_TIME)) {
            setMinInactiveMeterTime(pendingMessage);
        } else if (pendingMessage.getSpecification().getId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_INACTIVE_METER_TIME)) {
            setMaxInactiveMeterTime(pendingMessage);
        } else if (pendingMessage.getSpecification().getId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_KEEP_ALIVE_RETRIES)) {
            setKeepAliveRetries(pendingMessage);
        } else if (pendingMessage.getSpecification().getId().equals(DeviceMessageId.PLC_CONFIGURATION_SET_KEEP_ALIVE_TIMEOUT)) {
            setKeepAliveTimeout(pendingMessage);
        } else if (pendingMessage.getSpecification().getId().equals(DeviceMessageId.PLC_CONFIGURATION_ENABLE_G3_INTERFACE)) {
            enableG3PLCInterface(pendingMessage);
        } else if (pendingMessage.getSpecification().getId().equals(DeviceMessageId.PLC_CONFIGURATION_WRITE_PLC_G3_TIMEOUT)) {
            writePlcG3Timeout(pendingMessage);
        } else if (pendingMessage.getSpecification().getId().equals(DeviceMessageId.PLC_CONFIGURATION_WRITE_G3_KEEP_ALIVE)) {
            configurePlcG3KeepAlive(pendingMessage);
        } else {   //Unsupported message
            return false;
        }
        return true;
    }

    private void setMaxNumberOfHops(OfflineDeviceMessage pendingMessage) throws IOException {
        int value = getSingleIntegerAttribute(pendingMessage);
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeMaxHops(value);
    }

    private void setWeakLQIValue(OfflineDeviceMessage pendingMessage) throws IOException {
        int value = getSingleIntegerAttribute(pendingMessage);
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeWeakLqiValue(value);
    }

    private void setSecurityLevelpendingMessage(OfflineDeviceMessage pendingMessage) throws IOException {
        int value = getSingleIntegerAttribute(pendingMessage);
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeSecurityLevel(value);
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

    private void setBroadCastLogTableEntryTTL(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeBroadcastLogTableTTL(getSingleIntegerAttribute(pendingMessage));
    }

    private void setMaxJoinWaitTime(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeMaxJoinWaitTime(getSingleIntegerAttribute(pendingMessage));
    }

    private void setPathDiscoveryTime(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writePathDiscoveryTime(getSingleIntegerAttribute(pendingMessage));
    }

    private void setMetricType(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeMetricType(getSingleIntegerAttribute(pendingMessage));
    }

    private void setCoordShortAddress(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeCoordShortAddress(getSingleIntegerAttribute(pendingMessage));
    }

    private void setDisableDefaultRouting(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeDisableDefaultRouting(getSingleBooleanAttribute(pendingMessage));
    }

    private void setDeviceType(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeDeviceType(getSingleIntegerAttribute(pendingMessage));
    }

    private void resetPlcOfdmMacCounters(OfflineDeviceMessage pendingMessage) throws IOException {
        this.session.getCosemObjectFactory().getPLCOFDMType2PHYAndMACCounters().reset();
    }

    private void setPanId(OfflineDeviceMessage pendingMessage) throws IOException {
        this.session.getCosemObjectFactory().getPLCOFDMType2MACSetup().writePANID(getSingleIntegerAttribute(pendingMessage));
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

    private void setTMRTTL(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeTMRTTL(getSingleIntegerAttribute(pendingMessage));
    }

    private void setMaxFrameRetries(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeMaxFrameRetries(getSingleIntegerAttribute(pendingMessage));
    }

    private void setNeighbourTableEntryTTL(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeNeighbourTableEntryTTL(getSingleIntegerAttribute(pendingMessage));
    }

    private void setHighPriorityWindowSize(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeHighPriorityWindowSize(getSingleIntegerAttribute(pendingMessage));
    }

    private void setCSMAFairnessLimit(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeCSMAFairnessLimit(getSingleIntegerAttribute(pendingMessage));
    }

    private void setBeaconRandomizationWindowLength(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeBeaconRandomizationWindowLength(getSingleIntegerAttribute(pendingMessage));
    }

    private void setMacA(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeMacA(getSingleIntegerAttribute(pendingMessage));
    }

    private void setMacK(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeMacK(getSingleIntegerAttribute(pendingMessage));
    }

    private void setMinimumCWAttempts(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeMinCWAttempts(getSingleIntegerAttribute(pendingMessage));
    }

    private void setMaxBe(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeMaxBE(getSingleIntegerAttribute(pendingMessage));
    }

    private void setMaxCSMABackOff(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeMaxCSMABackOff(getSingleIntegerAttribute(pendingMessage));
    }

    private void setMinBe(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeMinBE(getSingleIntegerAttribute(pendingMessage));

    }

    private PathRequestFeedback pathRequest(OfflineDeviceMessage pendingMessage) throws IOException {
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
        int numberPingFailed = 0;
        int numberPathFailed = 0;
        int success = 0;
        Map<String, String> allPaths = new HashMap<String, String>();   //Remember the full path for every meter

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
                numberPingFailed++;
                logFailedPingRequest(pingFailed, macAddress);
            } else if (ping > 0) {
                logSuccessfulPingRequest(pingSuccess, macAddress, ping);
                session.getLogger().info("Ping request for meter " + macAddress + " was successful (" + ping + " ms).");
                try {
                    session.getLogger().info("Executing path request to meter " + macAddress);
                    session.getDLMSConnection().setTimeout(fullRoundTripTimeout);
                    String fullPath = topologyManagement.requestPath(macAddress);   //If successful, it will be added in the topology of the RTU+Server
                    allPaths.put(macAddress, fullPath);
                    success++;
                    session.getLogger().info("Path request for meter " + macAddress + " was successful.");
                } catch (DataAccessResultException e) {
                    numberPathFailed++;
                    session.getLogger().warning("Path request for meter " + macAddress + " failed.");
                    logFailedPathRequest(pathFailed, macAddress);
                } finally {
                    session.getDLMSConnection().setTimeout(normalTimeout);
                }
            } else if (ping <= 0) {
                numberPingFailed++;
                session.getLogger().info("Ping failed for meter " + macAddress + ". Will not execute path request for this meter.");
                logFailedPingRequest(pingFailed, macAddress);
            }
        }

        String allInfo = (pingSuccess.length() == 0 ? "" : (pingSuccess + ". ")) + (pingFailed.length() == 0 ? "" : (pingFailed + ". ")) + (pathFailed.length() == 0 ? "" : (pathFailed + "."));

        List<CollectedRegister> collectedRegisters = convertPathInfoToRegisters(allPaths);
        if (pingFailed.toString().length() == 0 && pathFailed.toString().length() == 0) {
            session.getLogger().info("Message result: ping and path requests were successful for every meter.");
        }

        return new PathRequestFeedback(allInfo, collectedRegisters);
    }

    private List<CollectedRegister> convertPathInfoToRegisters(Map<String, String> allPaths) throws IOException {
        List<CollectedRegister> result = new ArrayList<>();
        List<String> allDescriptions = new ArrayList<String>();
        StringBuilder currentBuilder = createNewBuilder();    //Start with this builder

        for (String macAddress : allPaths.keySet()) {
            //Prevent that the description size goes over 4000 chars.
            if (currentBuilder.length() > MAX_REGISTER_TEXT_SIZE) {
                allDescriptions.add(currentBuilder.toString());      //Add the generated descriptions to the list
                currentBuilder = createNewBuilder();                 //Continue with a new StringBuilder
            }
            currentBuilder.append(allPaths.get(macAddress));
            currentBuilder.append("\n\r");
        }
        allDescriptions.add(currentBuilder.toString());     //Add the last used builder too

        for (int index = 0; index < allDescriptions.size(); index++) {
            ObisCode topologyObisCode = ProtocolTools.setObisCodeField(G3NetworkManagement.getDefaultObisCode(), 1, (byte) (index + 1));
            DeviceIdentifier deviceIdentifier = offlineDevice.getDeviceIdentifier();
            RegisterDataIdentifierByObisCodeAndDevice registerDataIdentifier = new RegisterDataIdentifierByObisCodeAndDevice(topologyObisCode, topologyObisCode, deviceIdentifier);
            final Optional<ReadingType> readingTypeForObisCode = findReadingTypeForObisCode(topologyObisCode);
            final int finalIndex = index;
            readingTypeForObisCode.map(readingType -> {
                CollectedRegister collectedRegister = this.collectedDataFactory.createDefaultCollectedRegister(registerDataIdentifier, readingType);
                collectedRegister.setCollectedData(allDescriptions.get(finalIndex));
                collectedRegister.setReadTime(clock.instant());
                result.add(collectedRegister);
                return null;
            }).orElseThrow(() -> new IOException("No ReadingType found for obiscode " + topologyObisCode));
        }
        return result;
    }

    private Optional<ReadingType> findReadingTypeForObisCode(ObisCode obisCode) {
        final Optional<OfflineRegister> offlineRegister = this.offlineDevice.getAllRegisters().stream().filter(or -> or.getObisCode().equals(obisCode) || or.getAmrRegisterObisCode().equals(obisCode)).findAny();
        return offlineRegister.map(OfflineRegister::getReadingType);
    }

    private StringBuilder createNewBuilder() {
        StringBuilder sb = new StringBuilder();
        sb.append("Date:MAC_Address:Forth_path:Back_path");
        sb.append("\n\r");
        return sb;
    }

    private void logFailedPingRequest(StringBuilder pingFailed, String macAddress) {
        if (pingFailed.toString().length() == 0) {
            pingFailed.append("Ping failed for: ");
            pingFailed.append(macAddress);
        } else {
            pingFailed.append(", ");
            pingFailed.append(macAddress);
        }
    }

    private void logSuccessfulPingRequest(StringBuilder pingSuccess, String macAddress, int pingTime) {
        if (pingSuccess.toString().length() == 0) {
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
        if (pathFailed.toString().length() == 0) {
            pathFailed.append("Path request failed for: ");
            pathFailed.append(macAddress);
        } else {
            pathFailed.append(", ");
            pathFailed.append(macAddress);
        }
    }

    private void setAutomaticRouteManagement(OfflineDeviceMessage pendingMessage) throws IOException {
        boolean pingEnabled = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.pingEnabled).getDeviceMessageAttributeValue());
        boolean routeRequestEnabled = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.routeRequestEnabled).getDeviceMessageAttributeValue());
        boolean pathRequestEnabled = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.pathRequestEnabled).getDeviceMessageAttributeValue());

        this.session.getCosemObjectFactory().getG3NetworkManagement().setAutomaticRouteManagement(pingEnabled, routeRequestEnabled, pathRequestEnabled);
    }

    private void enableSNR(OfflineDeviceMessage pendingMessage) throws IOException {
        this.session.getCosemObjectFactory().getG3NetworkManagement().enableSNR(getSingleBooleanAttribute(pendingMessage));
    }

    private void setSNRPacketInterval(OfflineDeviceMessage pendingMessage) throws IOException {
        this.session.getCosemObjectFactory().getG3NetworkManagement().setSNRPacketInterval(getSingleIntegerAttribute(pendingMessage));
    }

    private void setSNRQuietTime(OfflineDeviceMessage pendingMessage) throws IOException {
        this.session.getCosemObjectFactory().getG3NetworkManagement().setSNRQuietTime(getSingleIntegerAttribute(pendingMessage));
    }

    private void setSNRPayload(OfflineDeviceMessage pendingMessage) throws IOException {
        byte[] payLoad = ProtocolTools.getBytesFromHexString(pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue(), "");
        this.session.getCosemObjectFactory().getG3NetworkManagement().setSNRPayload(payLoad);
    }

    private void enableKeepAlive(OfflineDeviceMessage pendingMessage) throws IOException {
        this.session.getCosemObjectFactory().getG3NetworkManagement().enableKeepAlive(getSingleBooleanAttribute(pendingMessage));
    }

    private void setKeepAliveScheduleInterval(OfflineDeviceMessage pendingMessage) throws IOException {
        this.session.getCosemObjectFactory().getG3NetworkManagement().setKeepAliveScheduleInterval(getSingleIntegerAttribute(pendingMessage));
    }

    private void setKeepAliveBucketSize(OfflineDeviceMessage pendingMessage) throws IOException {
        this.session.getCosemObjectFactory().getG3NetworkManagement().setKeepAliveBucketSize(getSingleIntegerAttribute(pendingMessage));
    }

    private void setMinInactiveMeterTime(OfflineDeviceMessage pendingMessage) throws IOException {
        this.session.getCosemObjectFactory().getG3NetworkManagement().setMinInactiveMeterTime(getSingleIntegerAttribute(pendingMessage));
    }

    private void setMaxInactiveMeterTime(OfflineDeviceMessage pendingMessage) throws IOException {
        this.session.getCosemObjectFactory().getG3NetworkManagement().setMaxInactiveMeterTime(getSingleIntegerAttribute(pendingMessage));
    }

    private void setKeepAliveRetries(OfflineDeviceMessage pendingMessage) throws IOException {
        this.session.getCosemObjectFactory().getG3NetworkManagement().setKeepAliveRetries(getSingleIntegerAttribute(pendingMessage));
    }

    private void setKeepAliveTimeout(OfflineDeviceMessage pendingMessage) throws IOException {
        this.session.getCosemObjectFactory().getG3NetworkManagement().setKeepAliveTimeout(getSingleIntegerAttribute(pendingMessage));
    }

    private void enableG3PLCInterface(OfflineDeviceMessage pendingMessage) throws IOException {
        this.session.getCosemObjectFactory().getG3NetworkManagement().enableG3Interface(getSingleBooleanAttribute(pendingMessage));
    }

    private void writePlcG3Timeout(OfflineDeviceMessage pendingMessage) throws IOException {
        int timeoutInMinutes = getSingleIntegerAttribute(pendingMessage);
        this.session.getCosemObjectFactory().getData(PLC_G3_TIMEOUT_OBISCODE).setValueAttr(new Unsigned16(timeoutInMinutes));
    }

    private void configurePlcG3KeepAlive(OfflineDeviceMessage pendingMessage) throws IOException {
        boolean enable = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.EnableKeepAlive).getDeviceMessageAttributeValue());
        int startTime = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.keepAliveStartTime).getDeviceMessageAttributeValue());
        int sendPeriod = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.keepAliveSendPeriod).getDeviceMessageAttributeValue());

        Structure structure = new Structure();
        structure.addDataType(new BooleanObject(enable));
        structure.addDataType(new Unsigned16(startTime));
        structure.addDataType(new Unsigned8(sendPeriod));
        this.session.getCosemObjectFactory().getData(PLC_G3_KEEP_ALIVE_OBISCODE).setValueAttr(structure);
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

    protected CollectedMessage createCollectedMessageWithRegisterData(OfflineDeviceMessage message, List<CollectedRegister> registers) {
        return this.collectedDataFactory.createCollectedMessageWithRegisterData(message.getDeviceIdentifier(), message.getIdentifier(), registers);
    }

    protected Issue createMessageFailedIssue(OfflineDeviceMessage pendingMessage, Exception e) {
        return createMessageFailedIssue(pendingMessage, e.getMessage());
    }

    protected Issue createMessageFailedIssue(OfflineDeviceMessage pendingMessage, String message) {
        return this.issueService.newIssueCollector().addWarning(pendingMessage, "DeviceMessage.failed",
                pendingMessage.getDeviceMessageId(),
                pendingMessage.getSpecification().getCategory().getName(),
                pendingMessage.getSpecification().getName(),
                message);
    }

    protected Issue createUnsupportedWarning(OfflineDeviceMessage pendingMessage) throws IOException {
        return this.issueService.newIssueCollector().addWarning(pendingMessage, "DeviceMessage.notSupported",
                pendingMessage.getDeviceMessageId(),
                pendingMessage.getSpecification().getCategory().getName(),
                pendingMessage.getSpecification().getName());
    }

    public class PathRequestFeedback {

        private final String feedback;
        private final List<CollectedRegister> registers;

        public PathRequestFeedback(String feedback, List<CollectedRegister> registers) {
            this.feedback = feedback;
            this.registers = registers;
        }

        public String getFeedback() {
            return feedback;
        }

        public List<CollectedRegister> getRegisters() {
            return registers;
        }
    }
}