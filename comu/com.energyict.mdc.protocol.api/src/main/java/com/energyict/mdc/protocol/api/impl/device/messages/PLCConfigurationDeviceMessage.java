/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

enum PLCConfigurationDeviceMessage implements DeviceMessageSpecEnum {

    ForceManualRescanPLCBus(DeviceMessageId.PLC_CONFIGURATION_FORCE_MANUAL_RESCAN_PLC_BUS, "Force manual rescan of PLC bus"),
    SetMulticastAddresses(DeviceMessageId.PLC_CONFIGURATION_SET_MULTICAST_ADDRESSES, "Set multicast addresses") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            Stream.of(PLCConfigurationDeviceMessageAttributes.MulticastAddress1AttributeName, PLCConfigurationDeviceMessageAttributes.MulticastAddress2AttributeName, PLCConfigurationDeviceMessageAttributes.MulticastAddress3AttributeName)
                .map(name -> propertySpecService
                        .hexStringSpec()
                        .named(name)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish())
                .forEach(propertySpecs::add);
        }
    },
    SetActivePlcChannel(DeviceMessageId.PLC_CONFIGURATION_SET_ACTIVE_CHANNEL, "Set active PLC channel") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            this.addBigDecimalSpec(propertySpecs, propertySpecService, thesaurus, PLCConfigurationDeviceMessageAttributes.ActiveChannelAttributeName);
        }
    },
    SetPlcChannelFrequencies(DeviceMessageId.PLC_CONFIGURATION_SET_CHANNEL_FREQUENCIES, "Set PLC channel frequencies") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            Stream.of(
                    PLCConfigurationDeviceMessageAttributes.CHANNEL1_FSAttributeName,
                    PLCConfigurationDeviceMessageAttributes.CHANNEL1_FMAttributeName,
                    PLCConfigurationDeviceMessageAttributes.CHANNEL2_FSAttributeName,
                    PLCConfigurationDeviceMessageAttributes.CHANNEL2_FMAttributeName,
                    PLCConfigurationDeviceMessageAttributes.CHANNEL3_FSAttributeName,
                    PLCConfigurationDeviceMessageAttributes.CHANNEL3_FMAttributeName,
                    PLCConfigurationDeviceMessageAttributes.CHANNEL4_FSAttributeName,
                    PLCConfigurationDeviceMessageAttributes.CHANNEL4_FMAttributeName,
                    PLCConfigurationDeviceMessageAttributes.CHANNEL5_FSAttributeName,
                    PLCConfigurationDeviceMessageAttributes.CHANNEL5_FMAttributeName,
                    PLCConfigurationDeviceMessageAttributes.CHANNEL6_FSAttributeName,
                    PLCConfigurationDeviceMessageAttributes.CHANNEL6_FMAttributeName)
                .map(name -> propertySpecService.bigDecimalSpec().named(name).fromThesaurus(thesaurus).markRequired().finish())
                .forEach(propertySpecs::add);
        }
    },
    SetSFSKInitiatorPhase(DeviceMessageId.PLC_CONFIGURATION_SET_SFSK_INITIATOR_PHASE, "Set SFSK initiator phase") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            this.addBigDecimalSpec(propertySpecs, propertySpecService, thesaurus, PLCConfigurationDeviceMessageAttributes.INITIATOR_ELECTRICAL_PHASEAttributeName);
        }
    },
    SetSFSKMaxFrameLength(DeviceMessageId.PLC_CONFIGURATION_SET_SFSK_MAX_FRAME_LENGTH, "Set SFSK maximum frame length") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            this.addBigDecimalSpec(propertySpecs, propertySpecService, thesaurus, PLCConfigurationDeviceMessageAttributes.MAX_FRAME_LENGTHAttributeName);
        }
    },

    SetActiveScanDuration(DeviceMessageId.PLC_CONFIGURATION_SET_ACTIVE_SCAN_DURATION, "Set active scan duration") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .timeDurationSpec()
                            .named(PLCConfigurationDeviceMessageAttributes.activeScanDurationAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    SetBroadCastLogTableEntryTTL(DeviceMessageId.PLC_CONFIGURATION_SET_BROAD_CAST_LOG_TABLE_ENTRY_TTL, "Set broadcast log table entry TTL") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .timeDurationSpec()
                            .named(PLCConfigurationDeviceMessageAttributes.broadCastLogTableEntryTTLAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    SetDiscoveryAttemptsSpeed(DeviceMessageId.PLC_CONFIGURATION_SET_DISCOVERY_ATTEMPTS_SPEED, "Set discovery attempts speed") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .timeDurationSpec()
                            .named(PLCConfigurationDeviceMessageAttributes.discoveryAttemptsSpeedAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    SetMaxAgeTime(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_AGE_TIME, "Set maximum age time") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .timeDurationSpec()
                            .named(PLCConfigurationDeviceMessageAttributes.maxAgeTimeAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    SetMaxNumberOfHops(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_NUMBER_OF_HOPS, "Set maximum number of hops") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .timeDurationSpec()
                            .named(PLCConfigurationDeviceMessageAttributes.maxNumberOfHopsAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    SetMaxPANConflictsCount(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_PAN_CONFLICTS_COUNT, "Set maximum PAN conflicts") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            this.addBigDecimalSpec(propertySpecs, propertySpecService, thesaurus, PLCConfigurationDeviceMessageAttributes.maxPANConflictsCountAttributeName);
        }
    },
    SetPanConflictWaitTime(DeviceMessageId.PLC_CONFIGURATION_SET_PAN_CONFLICT_WAIT_TIME, "Set PAN conflict wait time") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .timeDurationSpec()
                            .named(PLCConfigurationDeviceMessageAttributes.panConflictWaitTimeAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    SetToneMask(DeviceMessageId.PLC_CONFIGURATION_SET_TONE_MASK, "Set tone mask") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            this.addStringSpec(propertySpecs, propertySpecService, thesaurus, PLCConfigurationDeviceMessageAttributes.toneMaskAttributeName);
        }
    },
    SetWeakLQIValue(DeviceMessageId.PLC_CONFIGURATION_SET_WEAK_LQI_VALUE, "Set weak LQI value") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            this.addBigDecimalSpec(propertySpecs, propertySpecService, thesaurus, PLCConfigurationDeviceMessageAttributes.weakLQIValueAttributeName);
        }
    },
    WritePlcG3Timeout(DeviceMessageId.PLC_CONFIGURATION_WRITE_PLC_G3_TIMEOUT, "Write PLC G3 timeout") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .timeDurationSpec()
                            .named(PLCConfigurationDeviceMessageAttributes.plcG3TimeoutAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    ResetPlcOfdmMacCounters(DeviceMessageId.PLC_CONFIGURATION_RESET_PLC_OFDM_MAC_COUNTERS, "Reset PLC OFDM mac counters"),
    SetPanId(DeviceMessageId.PLC_CONFIGURATION_SET_PAN_ID, "Set PAN id") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            this.addBigDecimalSpec(propertySpecs, propertySpecService, thesaurus, PLCConfigurationDeviceMessageAttributes.G3PanIdAttributename);
        }
    },
    SetMaxOrphanTimer(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_ORPHAN_TIMER, "Set maximum orphan timeer") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .timeDurationSpec()
                            .named(PLCConfigurationDeviceMessageAttributes.MaxOrphanTimerAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },

    SetSFSKRepeater(DeviceMessageId.PLC_CONFIGURATION_SET_SFSK_REPEATER, "Set SFSK repeater") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            this.addBigDecimalSpec(propertySpecs, propertySpecService, thesaurus, PLCConfigurationDeviceMessageAttributes.REPEATERAttributeName);
        }
    },
    SetSFSKGain(DeviceMessageId.PLC_CONFIGURATION_SET_SFSK_GAIN, "Set SFSK gain") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            this.addBigDecimalSpec(propertySpecs, propertySpecService, thesaurus, PLCConfigurationDeviceMessageAttributes.MAX_RECEIVING_GAINAttributeName);
            this.addBigDecimalSpec(propertySpecs, propertySpecService, thesaurus, PLCConfigurationDeviceMessageAttributes.MAX_TRANSMITTING_GAINAttributeName);
            this.addBigDecimalSpec(propertySpecs, propertySpecService, thesaurus, PLCConfigurationDeviceMessageAttributes.SEARCH_INITIATOR_GAINAttributeName);
        }
    },
    SetTimeoutNotAddressed(DeviceMessageId.PLC_CONFIGURATION_SET_TIMEOUT_NOT_ADDRESSED, "Set timeout not addressed") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .timeDurationSpec()
                            .named(PLCConfigurationDeviceMessageAttributes.TIME_OUT_NOT_ADDRESSEDAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    SetSFSKMacTimeouts(DeviceMessageId.PLC_CONFIGURATION_SET_SFSK_MAC_TIMEOUTS, "Set SFSK mac timeouts") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            this.addBigDecimalSpec(propertySpecs, propertySpecService, thesaurus, PLCConfigurationDeviceMessageAttributes.SEARCH_INITIATOR_TIMEOUTAttributeName);
            this.addBigDecimalSpec(propertySpecs, propertySpecService, thesaurus, PLCConfigurationDeviceMessageAttributes.SYNCHRONIZATION_CONFIRMATION_TIMEOUTAttributeName);
            this.addBigDecimalSpec(propertySpecs, propertySpecService, thesaurus, PLCConfigurationDeviceMessageAttributes.TIME_OUT_NOT_ADDRESSEDAttributeName);
            this.addBigDecimalSpec(propertySpecs, propertySpecService, thesaurus, PLCConfigurationDeviceMessageAttributes.TIME_OUT_FRAME_NOT_OKAttributeName);
        }
    },
    SetPlcChannelFreqSnrCredits(DeviceMessageId.PLC_CONFIGURATION_SET_PLC_CHANNEL_FREQ_SNR_CREDITS, "Set PLC channel frequency SNR credits") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            Stream.of(
                    PLCConfigurationDeviceMessageAttributes.CHANNEL1_FSAttributeName,
                    PLCConfigurationDeviceMessageAttributes.CHANNEL1_FMAttributeName,
                    PLCConfigurationDeviceMessageAttributes.CHANNEL1_SNRAttributeName,
                    PLCConfigurationDeviceMessageAttributes.CHANNEL1_CREDITWEIGHTAttributeName,
                    PLCConfigurationDeviceMessageAttributes.CHANNEL2_FSAttributeName,
                    PLCConfigurationDeviceMessageAttributes.CHANNEL2_FMAttributeName,
                    PLCConfigurationDeviceMessageAttributes.CHANNEL2_SNRAttributeName,
                    PLCConfigurationDeviceMessageAttributes.CHANNEL2_CREDITWEIGHTAttributeName,
                    PLCConfigurationDeviceMessageAttributes.CHANNEL3_FSAttributeName,
                    PLCConfigurationDeviceMessageAttributes.CHANNEL3_FMAttributeName,
                    PLCConfigurationDeviceMessageAttributes.CHANNEL3_SNRAttributeName,
                    PLCConfigurationDeviceMessageAttributes.CHANNEL3_CREDITWEIGHTAttributeName,
                    PLCConfigurationDeviceMessageAttributes.CHANNEL4_FSAttributeName,
                    PLCConfigurationDeviceMessageAttributes.CHANNEL4_FMAttributeName,
                    PLCConfigurationDeviceMessageAttributes.CHANNEL4_SNRAttributeName,
                    PLCConfigurationDeviceMessageAttributes.CHANNEL4_CREDITWEIGHTAttributeName,
                    PLCConfigurationDeviceMessageAttributes.CHANNEL5_FSAttributeName,
                    PLCConfigurationDeviceMessageAttributes.CHANNEL5_FMAttributeName,
                    PLCConfigurationDeviceMessageAttributes.CHANNEL5_SNRAttributeName,
                    PLCConfigurationDeviceMessageAttributes.CHANNEL5_CREDITWEIGHTAttributeName,
                    PLCConfigurationDeviceMessageAttributes.CHANNEL6_FSAttributeName,
                    PLCConfigurationDeviceMessageAttributes.CHANNEL6_FMAttributeName,
                    PLCConfigurationDeviceMessageAttributes.CHANNEL6_SNRAttributeName,
                    PLCConfigurationDeviceMessageAttributes.CHANNEL6_CREDITWEIGHTAttributeName)
                    .map(name -> propertySpecService.bigDecimalSpec().named(name).fromThesaurus(thesaurus).markRequired().finish())
                    .forEach(propertySpecs::add);
        }
    },
    SetMaxNumberOfHopsAttributeName(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_NUMBER_OF_HOPS_ATTRIBUTENAME, "Set maximum number of hops"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(PLCConfigurationDeviceMessageAttributes.maxNumberOfHopsAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .setDefaultValue(BigDecimal.ZERO)
                            .finish());
        }
    },
    SetWeakLQIValueAttributeName(DeviceMessageId.PLC_CONFIGURATION_SET_WEAK_LQI_VALUE_ATTRIBUTENAME, "Set weak LQI value"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(PLCConfigurationDeviceMessageAttributes.weakLQIValueAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .setDefaultValue(BigDecimal.ZERO)
                            .finish());
        }
    },
    SetSecurityLevel(DeviceMessageId.PLC_CONFIGURATION_SET_SECURITY_LEVEL, "Set security level"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(PLCConfigurationDeviceMessageAttributes.plcSecurityLevel)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .setDefaultValue(BigDecimal.ZERO)
                            .finish());
        }
    },
    SetRoutingConfiguration(DeviceMessageId.PLC_CONFIGURATION_SET_ROUTING_CONFIGURATION, "Set routing configuration"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            Stream.of(
                    PLCConfigurationDeviceMessageAttributes.adp_Kr,
                    PLCConfigurationDeviceMessageAttributes.adp_Km,
                    PLCConfigurationDeviceMessageAttributes.adp_Kc,
                    PLCConfigurationDeviceMessageAttributes.adp_Kq,
                    PLCConfigurationDeviceMessageAttributes.adp_Kh,
                    PLCConfigurationDeviceMessageAttributes.adp_Krt,
                    PLCConfigurationDeviceMessageAttributes.adp_RREQ_retries,
                    PLCConfigurationDeviceMessageAttributes.adp_net_traversal_time,
                    PLCConfigurationDeviceMessageAttributes.adp_routing_table_entry_TTL,
                    PLCConfigurationDeviceMessageAttributes.adp_RREQ_RERR_wait,
                    PLCConfigurationDeviceMessageAttributes.adp_Blacklist_table_entry_TTL,
                    PLCConfigurationDeviceMessageAttributes.adp_add_rev_link_cost)
                .map(name -> propertySpecService
                                .bigDecimalSpec()
                                .named(name)
                                .fromThesaurus(thesaurus)
                                .markRequired()
                                .setDefaultValue(BigDecimal.ZERO)
                                .finish())
                .forEach(propertySpecs::add);
            propertySpecs.add(
                    propertySpecService
                            .booleanSpec()
                            .named(PLCConfigurationDeviceMessageAttributes.adp_RLC_enabled)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .booleanSpec()
                            .named(PLCConfigurationDeviceMessageAttributes.adp_add_rev_link_cost)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    SetBroadCastLogTableEntryTTLAttributeName(DeviceMessageId.PLC_CONFIGURATION_SET_BROADCAST_LOG_TABLE_ENTRY_TTL_ATTRIBUTENAME, "Set broadcast log table entry TTL attribute name"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .timeDurationSpec()
                            .named(PLCConfigurationDeviceMessageAttributes.broadCastLogTableEntryTTLAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .setDefaultValue(TimeDuration.NONE)
                            .finish());
        }
    },
    SetMaxJoinWaitTime(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_JOIN_WAIT_TIME, "Set maximum join wait time"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(PLCConfigurationDeviceMessageAttributes.maxJoinWaitTime)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .setDefaultValue(BigDecimal.ZERO)
                            .finish());
        }
    },
    SetPathDiscoveryTime(DeviceMessageId.PLC_CONFIGURATION_SET_PATH_DISCOVERY_TIME, "Set path discovery time"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(PLCConfigurationDeviceMessageAttributes.pathDiscoveryTime)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .setDefaultValue(BigDecimal.ZERO)
                            .finish());
        }
    },
    SetMetricType(DeviceMessageId.PLC_CONFIGURATION_SET_METRIC_TYPE, "Set metric type"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(PLCConfigurationDeviceMessageAttributes.metricType)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .setDefaultValue(BigDecimal.ZERO)
                            .finish());
        }
    },
    SetTMRTTL(DeviceMessageId.PLC_CONFIGURATION_SET_TMR_TTL, "Set TMR TTL"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(PLCConfigurationDeviceMessageAttributes.TMRTTL)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .setDefaultValue(BigDecimal.ZERO)
                            .finish());
        }
    },
    SetMaxFrameRetries(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_FRAME_RETRIES, "Set maximum frame retries"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(PLCConfigurationDeviceMessageAttributes.MaxFrameRetries)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .setDefaultValue(BigDecimal.ZERO)
                            .finish());
        }
    },
    SetNeighbourTableEntryTTL(DeviceMessageId.PLC_CONFIGURATION_SET_NEIGHBOUR_TABLE_ENTRY_TTL, "Set neighbour table entry TTL"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(PLCConfigurationDeviceMessageAttributes.NeighbourTableEntryTTL)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .setDefaultValue(BigDecimal.ZERO)
                            .finish());
        }
    },
    SetHighPriorityWindowSize(DeviceMessageId.PLC_CONFIGURATION_SET_HIGH_PRIORITY_WINDOW_SIZE, "Set highest priority window size"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(PLCConfigurationDeviceMessageAttributes.HighPriorityWindowSize)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .setDefaultValue(BigDecimal.ZERO)
                            .finish());
        }
    },
    SetCSMAFairnessLimit(DeviceMessageId.PLC_CONFIGURATION_SET_CSMA_FAIRNESS_LIMIT, "Set CSMA fairness limit"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(PLCConfigurationDeviceMessageAttributes.CSMAFairnessLimit)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .setDefaultValue(BigDecimal.ZERO)
                            .finish());
        }
    },
    SetBeaconRandomizationWindowLength(DeviceMessageId.PLC_CONFIGURATION_SET_BEACON_RANDOMIZATION_WINDOW_LENGTH, "Set beacon randomization window length"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(PLCConfigurationDeviceMessageAttributes.BeaconRandomizationWindowLength)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .setDefaultValue(BigDecimal.ZERO)
                            .finish());
        }
    },
    SetMacA(DeviceMessageId.PLC_CONFIGURATION_SET_MAC_A, "Set mac A"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(PLCConfigurationDeviceMessageAttributes.MacA)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .setDefaultValue(BigDecimal.ZERO)
                            .finish());
        }
    },
    SetMacK(DeviceMessageId.PLC_CONFIGURATION_SET_MAC_K, "Set mac K"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(PLCConfigurationDeviceMessageAttributes.MacK)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .setDefaultValue(BigDecimal.ZERO)
                            .finish());
        }
    },
    SetMinimumCWAttempts(DeviceMessageId.PLC_CONFIGURATION_SET_MINIMUM_CW_ATTEMPTS, "Set mininum CW attempts"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(PLCConfigurationDeviceMessageAttributes.MinimumCWAttempts)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .setDefaultValue(BigDecimal.ZERO)
                            .finish());
        }
    },
    SetMaxBe(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_BE, "Set maximum BE"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(PLCConfigurationDeviceMessageAttributes.maxBe)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .setDefaultValue(BigDecimal.ZERO)
                            .finish());
        }
    },
    SetMaxCSMABackOff(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_CSMA_BACK_OFF, "Set maximum CSMA backoff"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(PLCConfigurationDeviceMessageAttributes.maxCSMABackOff)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .setDefaultValue(BigDecimal.ZERO)
                            .finish());
        }
    },
    SetMinBe(DeviceMessageId.PLC_CONFIGURATION_SET_MIN_BE, "Set minimum BE"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(PLCConfigurationDeviceMessageAttributes.minBe)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .setDefaultValue(BigDecimal.ZERO)
                            .finish());
        }
    },
    PathRequest(DeviceMessageId.PLC_CONFIGURATION_PATH_REQUEST, "Path request"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            //TODO we need a group ...
//            propertySpecs.add(propertySpecService.referencePropertySpec(DeviceMessageConstants.deviceGroupPathRequestAttributeName, true, null));
        }
    },
    SetAutomaticRouteManagement(DeviceMessageId.PLC_CONFIGURATION_SET_AUTOMATIC_ROUTE_MANAGEMENT, "Set automatic route management"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            Stream.of(PLCConfigurationDeviceMessageAttributes.pingEnabled, PLCConfigurationDeviceMessageAttributes.routeRequestEnabled, PLCConfigurationDeviceMessageAttributes.pathRequestEnabled)
                .map(name -> propertySpecService.booleanSpec().named(name).fromThesaurus(thesaurus).markRequired().finish())
                .forEach(propertySpecs::add);
        }
    },
    EnableSNR(DeviceMessageId.PLC_CONFIGURATION_ENABLE_SNR, "Enable SNR"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(propertySpecService.booleanSpec().named(EnableSNR).fromThesaurus(thesaurus).markRequired().finish());
        }
    },
    SetSNRPacketInterval(DeviceMessageId.PLC_CONFIGURATION_SET_SNR_PACKET_INTERVAL, "Set SNR packet interval"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(PLCConfigurationDeviceMessageAttributes.SNRPacketInterval)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .setDefaultValue(BigDecimal.ZERO)
                            .finish());
        }
    },
    SetSNRQuietTime(DeviceMessageId.PLC_CONFIGURATION_SET_SNR_QUIET_TIME, "Set SNR quiet time"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(PLCConfigurationDeviceMessageAttributes.SNRQuietTime)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .setDefaultValue(BigDecimal.ZERO)
                            .finish());
        }
    },
    SetSNRPayload(DeviceMessageId.PLC_CONFIGURATION_SET_SNR_PAYLOAD, "Set SNR payload"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .hexStringSpec()
                            .named(PLCConfigurationDeviceMessageAttributes.SNRPayload)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    EnableKeepAlive(DeviceMessageId.PLC_CONFIGURATION_ENABLE_KEEP_ALIVE, "Enable keep alive"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .booleanSpec()
                            .named(PLCConfigurationDeviceMessageAttributes.EnableKeepAlive)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    SetKeepAliveScheduleInterval(DeviceMessageId.PLC_CONFIGURATION_SET_KEEP_ALIVE_SCHEDULE_INTERVAL, "Set keep alive schedule interval"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(PLCConfigurationDeviceMessageAttributes.KeepAliveScheduleInterval)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .setDefaultValue(BigDecimal.ZERO)
                            .finish());
        }
    },
    SetKeepAliveBucketSize(DeviceMessageId.PLC_CONFIGURATION_SET_KEEP_ALIVE_BUCKET_SIZE, "Set keep alive bucket size"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(PLCConfigurationDeviceMessageAttributes.KeepAliveBucketSize)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .setDefaultValue(BigDecimal.ZERO)
                            .finish());
        }
    },
    SetMinInactiveMeterTime(DeviceMessageId.PLC_CONFIGURATION_SET_MIN_INACTIVE_METER_TIME, "Set minimum inactive meter time"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(PLCConfigurationDeviceMessageAttributes.minInactiveMeterTime)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .setDefaultValue(BigDecimal.ZERO)
                            .finish());
        }
    },
    SetMaxInactiveMeterTime(DeviceMessageId.PLC_CONFIGURATION_SET_MAX_INACTIVE_METER_TIME, "Set maximum inactive meter time"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(PLCConfigurationDeviceMessageAttributes.maxInactiveMeterTime)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .setDefaultValue(BigDecimal.ZERO)
                            .finish());
        }
    },
    SetKeepAliveRetries(DeviceMessageId.PLC_CONFIGURATION_SET_KEEP_ALIVE_RETRIES, "Set keep alive retries"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(PLCConfigurationDeviceMessageAttributes.KeepAliveRetries)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .setDefaultValue(BigDecimal.ZERO)
                            .finish());
        }
    },
    SetKeepAliveTimeout(DeviceMessageId.PLC_CONFIGURATION_SET_KEEP_ALIVE_TIMEOUT, "Set keep alive timeout"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(PLCConfigurationDeviceMessageAttributes.KeepAliveTimeout)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .setDefaultValue(BigDecimal.ZERO)
                            .finish());
        }
    },
    SetCoordShortAddress(DeviceMessageId.PLC_CONFIGURATION_SET_COORD_SHORT_ADDRESS, "Set coord short address"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(PLCConfigurationDeviceMessageAttributes.coordShortAddress)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .setDefaultValue(BigDecimal.ZERO)
                            .finish());
        }
    },

    SetDisableDefaultRouting(DeviceMessageId.PLC_CONFIGURATION_SET_DISABLE_DEFAULT_ROUTING, "Set disable default routing"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .booleanSpec()
                            .named(PLCConfigurationDeviceMessageAttributes.disableDefaultRouting)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    SetDeviceType(DeviceMessageId.PLC_CONFIGURATION_SET_DEVICE_TYPE, "Set device type"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(PLCConfigurationDeviceMessageAttributes.deviceType)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .setDefaultValue(BigDecimal.valueOf(-1))
                            .finish());
        }
    },
    SetToneMaskAttributeName(DeviceMessageId.PLC_CONFIGURATION_SET_TONE_MASK_ATTRIBUTE_NAME, "Set tone mask attribute name"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            this.addStringSpec(propertySpecs, propertySpecService, thesaurus, PLCConfigurationDeviceMessageAttributes.toneMaskAttributeName);

        }
    },
    EnableG3PLCInterface(DeviceMessageId.PLC_CONFIGURATION_ENABLE_G3_INTERFACE, "Enable the G3 interface"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .booleanSpec()
                            .named(PLCConfigurationDeviceMessageAttributes.enablePLC)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .setDefaultValue(false)
                            .finish());
        }
    },
    ConfigurePLcG3KeepAlive(DeviceMessageId.PLC_CONFIGURATION_WRITE_G3_KEEP_ALIVE, "Configure the G3 keep alive"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .booleanSpec()
                            .named(PLCConfigurationDeviceMessageAttributes.EnableKeepAlive)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .setDefaultValue(false)
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .boundedBigDecimalSpec(BigDecimal.ZERO, BigDecimal.valueOf(0xFFFFl))
                            .named(PLCConfigurationDeviceMessageAttributes.keepAliveStartTime)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .boundedBigDecimalSpec(BigDecimal.ONE, BigDecimal.valueOf(0xFFl))
                            .named(PLCConfigurationDeviceMessageAttributes.keepAliveSendPeriod)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    };

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

    public final List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        this.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
        return propertySpecs;
    }

    protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        // Default behavior is not to add anything
    };

    protected void addBigDecimalSpec(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus, PLCConfigurationDeviceMessageAttributes name) {
        propertySpecs.add(propertySpecService.bigDecimalSpec().named(name).fromThesaurus(thesaurus).markRequired().finish());
    };

    protected void addStringSpec(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus, PLCConfigurationDeviceMessageAttributes name) {
        propertySpecs.add(propertySpecService.stringSpec().named(name).fromThesaurus(thesaurus).markRequired().setDefaultValue("").finish());
    };

}