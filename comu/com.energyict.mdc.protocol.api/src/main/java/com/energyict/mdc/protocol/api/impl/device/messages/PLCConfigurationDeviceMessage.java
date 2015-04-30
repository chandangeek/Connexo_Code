package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.dynamic.HexStringFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.TimeDurationValueFactory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.*;

/**
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum PLCConfigurationDeviceMessage implements DeviceMessageSpecEnum {

    ForceManualRescanPLCBus(DeviceMessageId.PLC_CONFIGURATION_FORCE_MANUAL_RESCAN_PLC_BUS, "Force manual rescan of PLC bus"),
    SetMulticastAddresses(DeviceMessageId.PLC_CONFIGURATION_SET_MULTICAST_ADDRESSES, "Set multicast addresses") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            HexStringFactory factory = new HexStringFactory();
            propertySpecs.add(propertySpecService.basicPropertySpec(MulticastAddress1AttributeName, true, factory));
            propertySpecs.add(propertySpecService.basicPropertySpec(MulticastAddress2AttributeName, true, factory));
            propertySpecs.add(propertySpecService.basicPropertySpec(MulticastAddress3AttributeName, true, factory));
        }
    },
    SetActivePlcChannel(DeviceMessageId.PLC_CONFIGURATION_SET_ACTIVE_CHANNEL, "Set active PLC channel") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(ActiveChannelAttributeName, true, new BigDecimalFactory()));
        }
    },
    SetPlcChannelFrequencies(DeviceMessageId.PLC_CONFIGURATION_SET_CHANNEL_FREQUENCIES, "Set PLC channel frequencies") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            BigDecimalFactory factory = new BigDecimalFactory();
            propertySpecs.add(propertySpecService.basicPropertySpec(CHANNEL1_FSAttributeName, true, factory));
            propertySpecs.add(propertySpecService.basicPropertySpec(CHANNEL1_FMAttributeName, true, factory));
            propertySpecs.add(propertySpecService.basicPropertySpec(CHANNEL2_FSAttributeName, true, factory));
            propertySpecs.add(propertySpecService.basicPropertySpec(CHANNEL2_FMAttributeName, true, factory));
            propertySpecs.add(propertySpecService.basicPropertySpec(CHANNEL3_FSAttributeName, true, factory));
            propertySpecs.add(propertySpecService.basicPropertySpec(CHANNEL3_FMAttributeName, true, factory));
            propertySpecs.add(propertySpecService.basicPropertySpec(CHANNEL4_FSAttributeName, true, factory));
            propertySpecs.add(propertySpecService.basicPropertySpec(CHANNEL4_FMAttributeName, true, factory));
            propertySpecs.add(propertySpecService.basicPropertySpec(CHANNEL5_FSAttributeName, true, factory));
            propertySpecs.add(propertySpecService.basicPropertySpec(CHANNEL5_FMAttributeName, true, factory));
            propertySpecs.add(propertySpecService.basicPropertySpec(CHANNEL6_FSAttributeName, true, factory));
            propertySpecs.add(propertySpecService.basicPropertySpec(CHANNEL6_FMAttributeName, true, factory));
        }
    },
    SetSFSKInitiatorPhase(DeviceMessageId.PLC_CONFIGURATION_SET_SFSK_INITIATOR_PHASE, "Set SFSK initiator phase") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(INITIATOR_ELECTRICAL_PHASEAttributeName, true, new BigDecimalFactory()));
        }
    },
    SetSFSKMaxFrameLength(DeviceMessageId.PLC_CONFIGURATION_SET_SFSK_MAX_FRAME_LENGTH, "Set SFSK maximum frame length") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(MAX_FRAME_LENGTHAttributeName, true, new BigDecimalFactory()));
        }
    },

    SetActiveScanDuration(DeviceMessageId.PLC_CONFIGURATION_SET_ACTIVE_SCAN_DURATION, "Set active scan duration") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(activeScanDurationAttributeName, true, new TimeDurationValueFactory()));
        }
    },
    SetBroadCastLogTableEntryTTL(DeviceMessageId.PLC_CONFIGURATION_SET_BROAD_CAST_LOG_TABLE_ENTRY_TTL, "Set broadcast log table entry TTL") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(broadCastLogTableEntryTTLAttributeName, true, new TimeDurationValueFactory()));
        }
    },
    SetDiscoveryAttemptsSpeed(DeviceMessageId.PLC_CONFIGURATION_SET_DISCOVERY_ATTEMPTS_SPEED, "Set discovery attempts speed") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(discoveryAttemptsSpeedAttributeName, true, new TimeDurationValueFactory()));
        }
    },
    SetMaxAgeTime(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_AGE_TIME, "Set maximum age time") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(maxAgeTimeAttributeName, true, new TimeDurationValueFactory()));
        }
    },
    SetMaxNumberOfHops(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_NUMBER_OF_HOPS, "Set maximum number of hops") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(maxNumberOfHopsAttributeName, true, new BigDecimalFactory()));
        }
    },
    SetMaxPANConflictsCount(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_PAN_CONFLICTS_COUNT, "Set maximum PAN conflicts") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(maxPANConflictsCountAttributeName, true, new BigDecimalFactory()));
        }
    },
    SetPanConflictWaitTime(DeviceMessageId.PLC_CONFIGURATION_SET_PAN_CONFLICT_WAIT_TIME, "Set PAN conflict wait time") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(panConflictWaitTimeAttributeName, true, new TimeDurationValueFactory()));
        }
    },
    SetToneMask(DeviceMessageId.PLC_CONFIGURATION_SET_TONE_MASK, "Set tone mask") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(toneMaskAttributeName, true, new StringFactory()));
        }
    },
    SetWeakLQIValue(DeviceMessageId.PLC_CONFIGURATION_SET_WEAK_LQI_VALUE, "Set weak LQI value") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(weakLQIValueAttributeName, true, new BigDecimalFactory()));
        }
    },
    WritePlcG3Timeout(DeviceMessageId.PLC_CONFIGURATION_WRITE_PLC_G3_TIMEOUT, "Write PLC G3 timeout") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(plcG3TimeoutAttributeName, true, new TimeDurationValueFactory()));
        }
    },
    ResetPlcOfdmMacCounters(DeviceMessageId.PLC_CONFIGURATION_RESET_PLC_OFDM_MAC_COUNTERS, "Reset PLC OFDM mac counters"),
    SetPanId(DeviceMessageId.PLC_CONFIGURATION_SET_PAN_ID, "Set PAN id") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(G3PanIdAttributename, true, new BigDecimalFactory()));
        }
    },
    SetMaxOrphanTimer(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_ORPHAN_TIMER, "Set maximum orphan timeer") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(MaxOrphanTimerAttributeName, true, new TimeDurationValueFactory()));
        }
    },

    SetSFSKRepeater(DeviceMessageId.PLC_CONFIGURATION_SET_SFSK_REPEATER, "Set SFSK repeater") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(REPEATERAttributeName, true, new BigDecimalFactory()));
        }
    },
    SetSFSKGain(DeviceMessageId.PLC_CONFIGURATION_SET_SFSK_GAIN, "Set SFSK gain") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            BigDecimalFactory factory = new BigDecimalFactory();
            propertySpecs.add(propertySpecService.basicPropertySpec(MAX_RECEIVING_GAINAttributeName, true, factory));
            propertySpecs.add(propertySpecService.basicPropertySpec(MAX_TRANSMITTING_GAINAttributeName, true, factory));
            propertySpecs.add(propertySpecService.basicPropertySpec(SEARCH_INITIATOR_GAINAttributeName, true, factory));
        }
    },
    SetTimeoutNotAddressed(DeviceMessageId.PLC_CONFIGURATION_SET_TIMEOUT_NOT_ADDRESSED, "Set timeout not addressed") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(TIME_OUT_NOT_ADDRESSEDAttributeName, true, new TimeDurationValueFactory()));
        }
    },
    SetSFSKMacTimeouts(DeviceMessageId.PLC_CONFIGURATION_SET_SFSK_MAC_TIMEOUTS, "Set SFSK mac timeouts") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            BigDecimalFactory factory = new BigDecimalFactory();
            propertySpecs.add(propertySpecService.basicPropertySpec(SEARCH_INITIATOR_TIMEOUTAttributeName, true, factory));
            propertySpecs.add(propertySpecService.basicPropertySpec(SYNCHRONIZATION_CONFIRMATION_TIMEOUTAttributeName, true, factory));
            propertySpecs.add(propertySpecService.basicPropertySpec(TIME_OUT_NOT_ADDRESSEDAttributeName, true, factory));
            propertySpecs.add(propertySpecService.basicPropertySpec(TIME_OUT_FRAME_NOT_OKAttributeName, true, factory));
        }
    },
    SetPlcChannelFreqSnrCredits(DeviceMessageId.PLC_CONFIGURATION_SET_PLC_CHANNEL_FREQ_SNR_CREDITS, "Set PLC channel frequency SNR credits") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            BigDecimalFactory factory = new BigDecimalFactory();
            propertySpecs.add(propertySpecService.basicPropertySpec(CHANNEL1_FSAttributeName, true, factory));
            propertySpecs.add(propertySpecService.basicPropertySpec(CHANNEL1_FMAttributeName, true, factory));
            propertySpecs.add(propertySpecService.basicPropertySpec(CHANNEL1_SNRAttributeName, true, factory));
            propertySpecs.add(propertySpecService.basicPropertySpec(CHANNEL1_CREDITWEIGHTAttributeName, true, factory));
            propertySpecs.add(propertySpecService.basicPropertySpec(CHANNEL2_FSAttributeName, true, factory));
            propertySpecs.add(propertySpecService.basicPropertySpec(CHANNEL2_FMAttributeName, true, factory));
            propertySpecs.add(propertySpecService.basicPropertySpec(CHANNEL2_SNRAttributeName, true, factory));
            propertySpecs.add(propertySpecService.basicPropertySpec(CHANNEL2_CREDITWEIGHTAttributeName, true, factory));
            propertySpecs.add(propertySpecService.basicPropertySpec(CHANNEL3_FSAttributeName, true, factory));
            propertySpecs.add(propertySpecService.basicPropertySpec(CHANNEL3_FMAttributeName, true, factory));
            propertySpecs.add(propertySpecService.basicPropertySpec(CHANNEL3_SNRAttributeName, true, factory));
            propertySpecs.add(propertySpecService.basicPropertySpec(CHANNEL3_CREDITWEIGHTAttributeName, true, factory));
            propertySpecs.add(propertySpecService.basicPropertySpec(CHANNEL4_FSAttributeName, true, factory));
            propertySpecs.add(propertySpecService.basicPropertySpec(CHANNEL4_FMAttributeName, true, factory));
            propertySpecs.add(propertySpecService.basicPropertySpec(CHANNEL4_SNRAttributeName, true, factory));
            propertySpecs.add(propertySpecService.basicPropertySpec(CHANNEL4_CREDITWEIGHTAttributeName, true, factory));
            propertySpecs.add(propertySpecService.basicPropertySpec(CHANNEL5_FSAttributeName, true, factory));
            propertySpecs.add(propertySpecService.basicPropertySpec(CHANNEL5_FMAttributeName, true, factory));
            propertySpecs.add(propertySpecService.basicPropertySpec(CHANNEL5_SNRAttributeName, true, factory));
            propertySpecs.add(propertySpecService.basicPropertySpec(CHANNEL5_CREDITWEIGHTAttributeName, true, factory));
            propertySpecs.add(propertySpecService.basicPropertySpec(CHANNEL6_FSAttributeName, true, factory));
            propertySpecs.add(propertySpecService.basicPropertySpec(CHANNEL6_FMAttributeName, true, factory));
            propertySpecs.add(propertySpecService.basicPropertySpec(CHANNEL6_SNRAttributeName, true, factory));
            propertySpecs.add(propertySpecService.basicPropertySpec(CHANNEL6_CREDITWEIGHTAttributeName, true, factory));
        }
    },
    SetMaxNumberOfHopsAttributeName(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_NUMBER_OF_HOPS_ATTRIBUTENAME, "Set maximum number of hops"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(DeviceMessageConstants.maxNumberOfHopsAttributeName, true, BigDecimal.ZERO));
        }
    },
    SetWeakLQIValueAttributeName(DeviceMessageId.PLC_CONFIGURATION_SET_WEAK_LQI_VALUE_ATTRIBUTENAME, "Set weak LQI value"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(DeviceMessageConstants.weakLQIValueAttributeName, true, BigDecimal.ZERO));
        }
    },
    SetSecurityLevel(DeviceMessageId.PLC_CONFIGURATION_SET_SECURITY_LEVEL, "Set security level"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(DeviceMessageConstants.plcSecurityLevel, true, BigDecimal.ZERO));
        }
    },
    SetRoutingConfiguration(DeviceMessageId.PLC_CONFIGURATION_SET_ROUTING_CONFIGURATION, "Set routing configuration"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(DeviceMessageConstants.adp_Kr, true, BigDecimal.ZERO));
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(DeviceMessageConstants.adp_Km, true, BigDecimal.ZERO));
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(DeviceMessageConstants.adp_Kc, true, BigDecimal.ZERO));
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(DeviceMessageConstants.adp_Kq, true, BigDecimal.ZERO));
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(DeviceMessageConstants.adp_Kh, true, BigDecimal.ZERO));
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(DeviceMessageConstants.adp_Krt, true, BigDecimal.ZERO));
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(DeviceMessageConstants.adp_RREQ_retries, true, BigDecimal.ZERO));
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(DeviceMessageConstants.adp_net_traversal_time, true, BigDecimal.ZERO));
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(DeviceMessageConstants.adp_routing_table_entry_TTL, true, BigDecimal.ZERO));
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(DeviceMessageConstants.adp_RREQ_RERR_wait, true, BigDecimal.ZERO));
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(DeviceMessageConstants.adp_Blacklist_table_entry_TTL, true, BigDecimal.ZERO));
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(DeviceMessageConstants.adp_add_rev_link_cost, true, BigDecimal.ZERO));
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.adp_RLC_enabled, true, new BooleanFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.adp_add_rev_link_cost, true, new BooleanFactory()));
        }
    },
    SetBroadCastLogTableEntryTTLAttributeName(DeviceMessageId.PLC_CONFIGURATION_SET_BROADCAST_LOG_TABLE_ENTRY_TTL_ATTRIBUTENAME, "Set broadcast log table entry TTL attribute name"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.timeDurationPropertySpec(DeviceMessageConstants.broadCastLogTableEntryTTLAttributeName, true, TimeDuration.NONE));
        }
    },
    SetMaxJoinWaitTime(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_JOIN_WAIT_TIME, "Set maximum join wait time"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(DeviceMessageConstants.maxJoinWaitTime, true, BigDecimal.ZERO));
        }
    },
    SetPathDiscoveryTime(DeviceMessageId.PLC_CONFIGURATION_SET_PATH_DISCOVERY_TIME, "Set path discovery time"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(DeviceMessageConstants.pathDiscoveryTime, true, BigDecimal.ZERO));
        }
    },
    SetMetricType(DeviceMessageId.PLC_CONFIGURATION_SET_METRIC_TYPE, "Set metric type"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(DeviceMessageConstants.metricType, true, BigDecimal.ZERO));
        }
    },
    SetTMRTTL(DeviceMessageId.PLC_CONFIGURATION_SET_TMR_TTL, "Set TMR TTL"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(DeviceMessageConstants.TMRTTL, true, BigDecimal.ZERO));
        }
    },
    SetMaxFrameRetries(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_FRAME_RETRIES, "Set maximum frame retries"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(DeviceMessageConstants.MaxFrameRetries, true, BigDecimal.ZERO));
        }
    },
    SetNeighbourTableEntryTTL(DeviceMessageId.PLC_CONFIGURATION_SET_NEIGHBOUR_TABLE_ENTRY_TTL, "Set neighbour table entry TTL"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(DeviceMessageConstants.NeighbourTableEntryTTL, true, BigDecimal.ZERO));
        }
    },
    SetHighPriorityWindowSize(DeviceMessageId.PLC_CONFIGURATION_SET_HIGH_PRIORITY_WINDOW_SIZE, "Set highest priority window size"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(DeviceMessageConstants.HighPriorityWindowSize, true, BigDecimal.ZERO));
        }
    },
    SetCSMAFairnessLimit(DeviceMessageId.PLC_CONFIGURATION_SET_CSMA_FAIRNESS_LIMIT, "Set CSMA fairness limit"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(DeviceMessageConstants.CSMAFairnessLimit, true, BigDecimal.ZERO));
        }
    },
    SetBeaconRandomizationWindowLength(DeviceMessageId.PLC_CONFIGURATION_SET_BEACON_RANDOMIZATION_WINDOW_LENGTH, "Set beacon randomization window length"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(DeviceMessageConstants.BeaconRandomizationWindowLength, true, BigDecimal.ZERO));
        }
    },
    SetMacA(DeviceMessageId.PLC_CONFIGURATION_SET_MAC_A, "Set mac A"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(DeviceMessageConstants.MacA, true, BigDecimal.ZERO));
        }
    },
    SetMacK(DeviceMessageId.PLC_CONFIGURATION_SET_MAC_K, "Set mac K"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(DeviceMessageConstants.MacK, true, BigDecimal.ZERO));
        }
    },
    SetMinimumCWAttempts(DeviceMessageId.PLC_CONFIGURATION_SET_MINIMUM_CW_ATTEMPTS, "Set mininum CW attempts"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(DeviceMessageConstants.MinimumCWAttempts, true, BigDecimal.ZERO));
        }
    },
    SetMaxBe(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_BE, "Set maximum BE"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(DeviceMessageConstants.maxBe, true, BigDecimal.ZERO));
        }
    },
    SetMaxCSMABackOff(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_CSMA_BACK_OFF, "Set maximum CSMA backoff"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(DeviceMessageConstants.maxCSMABackOff, true, BigDecimal.ZERO));
        }
    },
    SetMinBe(DeviceMessageId.PLC_CONFIGURATION_SET_MIN_BE, "Set minimum BE"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(DeviceMessageConstants.minBe, true, BigDecimal.ZERO));
        }
    },
    PathRequest(DeviceMessageId.PLC_CONFIGURATION_PATH_REQUEST, "Path request"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            //TODO we need a group ...
//            propertySpecs.add(propertySpecService.referencePropertySpec(DeviceMessageConstants.deviceGroupPathRequestAttributeName, true, null));
        }
    },
    SetAutomaticRouteManagement(DeviceMessageId.PLC_CONFIGURATION_SET_AUTOMATIC_ROUTE_MANAGEMENT, "Set automatic route management"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.pingEnabled, true, new BooleanFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.routeRequestEnabled, true, new BooleanFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.pathRequestEnabled, true, new BooleanFactory()));
        }
    },
    EnableSNR(DeviceMessageId.PLC_CONFIGURATION_ENABLE_SNR, "Enable SNR"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.EnableSNR, true, new BooleanFactory()));
        }
    },
    SetSNRPacketInterval(DeviceMessageId.PLC_CONFIGURATION_SET_SNR_PACKET_INTERVAL, "Set SNR packet interval"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(DeviceMessageConstants.SNRPacketInterval, true, BigDecimal.ZERO));
        }
    },
    SetSNRQuietTime(DeviceMessageId.PLC_CONFIGURATION_SET_SNR_QUIET_TIME, "Set SNR quiet time"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(DeviceMessageConstants.SNRQuietTime, true, BigDecimal.ZERO));
        }
    },
    SetSNRPayload(DeviceMessageId.PLC_CONFIGURATION_SET_SNR_PAYLOAD, "Set SNR payload"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.SNRPayload, true, new HexStringFactory()));
        }
    },
    EnableKeepAlive(DeviceMessageId.PLC_CONFIGURATION_ENABLE_KEEP_ALIVE, "Enable keep alive"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.EnableKeepAlive, true, new BooleanFactory()));
        }
    },
    SetKeepAliveScheduleInterval(DeviceMessageId.PLC_CONFIGURATION_SET_KEEP_ALIVE_SCHEDULE_INTERVAL, "Set keep alive schedule interval"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(DeviceMessageConstants.KeepAliveScheduleInterval, true, BigDecimal.ZERO));
        }
    },
    SetKeepAliveBucketSize(DeviceMessageId.PLC_CONFIGURATION_SET_KEEP_ALIVE_BUCKET_SIZE, "Set keep alive bucket size"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(DeviceMessageConstants.KeepAliveBucketSize, true, BigDecimal.ZERO));
        }
    },
    SetMinInactiveMeterTime(DeviceMessageId.PLC_CONFIGURATION_SET_MIN_INACTIVE_METER_TIME, "Set minimum inactive meter time"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(DeviceMessageConstants.minInactiveMeterTime, true, BigDecimal.ZERO));
        }
    },
    SetMaxInactiveMeterTime(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_INACTIVE_METER_TIME, "Set maximum inactive meter time"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(DeviceMessageConstants.maxInactiveMeterTime, true, BigDecimal.ZERO));
        }
    },
    SetKeepAliveRetries(DeviceMessageId.PLC_CONFIGURATION_SET_KEEP_ALIVE_RETRIES, "Set keep alive retries"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(DeviceMessageConstants.KeepAliveRetries, true, BigDecimal.ZERO));
        }
    },
    SetKeepAliveTimeout(DeviceMessageId.PLC_CONFIGURATION_SET_KEEP_ALIVE_TIMEOUT, "Set keep alive timeout"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(DeviceMessageConstants.KeepAliveTimeout, true, BigDecimal.ZERO));
        }
    },
    SetCoordShortAddress(DeviceMessageId.PLC_CONFIGURATION_SET_COORD_SHORT_ADDRESS, "Set coord short address"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(DeviceMessageConstants.coordShortAddress, true, BigDecimal.ZERO));
        }
    },

    SetDisableDefaultRouting(DeviceMessageId.PLC_CONFIGURATION_SET_DISABLE_DEFAULT_ROUTING, "Set disable default routing"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.booleanPropertySpec(DeviceMessageConstants.disableDefaultRouting, true, false));
        }
    },
    SetDeviceType(DeviceMessageId.PLC_CONFIGURATION_SET_DEVICE_TYPE, "Set device type"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(DeviceMessageConstants.deviceType, true, new BigDecimal(-1)));
        }
    },
    SetToneMaskAttributeName(DeviceMessageId.PLC_CONFIGURATION_SET_TONE_MASK_ATTRIBUTE_NAME, "Set tone mask attribute name"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.stringPropertySpec(DeviceMessageConstants.toneMaskAttributeName, true, ""));

        }
    }

    ;
    private DeviceMessageId id;
    private String defaultTranslation;

    PLCConfigurationDeviceMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }

    @Override
    public String getKey() {
        return PLCConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultTranslation;
    }

    @Override
    public DeviceMessageId getId() {
        return this.id;
    }

    public final List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService) {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        this.addPropertySpecs(propertySpecs, propertySpecService);
        return propertySpecs;
    }

    protected void addPropertySpecs (List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
        // Default behavior is not to add anything
    };

    public final PropertySpec getPropertySpec(String name, PropertySpecService propertySpecService) {
        for (PropertySpec securityProperty : getPropertySpecs(propertySpecService)) {
            if (securityProperty.getName().equals(name)) {
                return securityProperty;
            }
        }
        return null;
    }

}