package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.DeviceGroup;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.messages.nls.TranslationKeyImpl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum PLCConfigurationDeviceMessage implements DeviceMessageSpecSupplier {

    ForceManualRescanPLCBus(3001, "Force manual rescan of PLC bus") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    SetMulticastAddresses(3002, "Set multicast addresses") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.hexStringSpec(service, DeviceMessageConstants.MulticastAddress1AttributeName, DeviceMessageConstants.MulticastAddress1AttributeDefaultTranslation),
                    this.hexStringSpec(service, DeviceMessageConstants.MulticastAddress2AttributeName, DeviceMessageConstants.MulticastAddress2AttributeDefaultTranslation),
                    this.hexStringSpec(service, DeviceMessageConstants.MulticastAddress3AttributeName, DeviceMessageConstants.MulticastAddress3AttributeDefaultTranslation)
            );
        }
    },
    SetActivePlcChannel(3003, "Set active PLC channel") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.ActiveChannelAttributeName, DeviceMessageConstants.ActiveChannelAttributeDefaultTranslation));
        }
    },
    SetPlcChannelFrequencies(3004, "Set PLC channel frequencies") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.CHANNEL1_FSAttributeName, DeviceMessageConstants.CHANNEL1_FSAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.CHANNEL1_FMAttributeName, DeviceMessageConstants.CHANNEL1_FMAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.CHANNEL2_FSAttributeName, DeviceMessageConstants.CHANNEL2_FSAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.CHANNEL2_FMAttributeName, DeviceMessageConstants.CHANNEL2_FMAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.CHANNEL3_FSAttributeName, DeviceMessageConstants.CHANNEL3_FSAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.CHANNEL3_FMAttributeName, DeviceMessageConstants.CHANNEL3_FMAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.CHANNEL4_FSAttributeName, DeviceMessageConstants.CHANNEL4_FSAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.CHANNEL4_FMAttributeName, DeviceMessageConstants.CHANNEL4_FMAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.CHANNEL5_FSAttributeName, DeviceMessageConstants.CHANNEL5_FSAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.CHANNEL5_FMAttributeName, DeviceMessageConstants.CHANNEL5_FMAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.CHANNEL6_FSAttributeName, DeviceMessageConstants.CHANNEL6_FSAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.CHANNEL6_FMAttributeName, DeviceMessageConstants.CHANNEL6_FMAttributeDefaultTranslation)
            );
        }
    },
    SetSFSKInitiatorPhase(3005, "Set SFSK initiator phase") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.INITIATOR_ELECTRICAL_PHASEAttributeName, DeviceMessageConstants.INITIATOR_ELECTRICAL_PHASEAttributeDefaultTranslation));
        }
    },
    SetSFSKMaxFrameLength(3006, "Set SFSK maximum frame length") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.MAX_FRAME_LENGTHAttributeName, DeviceMessageConstants.MAX_FRAME_LENGTHAttributeDefaultTranslation));
        }
    },

    SetBroadCastLogTableEntryTTLAttributeName(3030, "Set broadcast log table entry TTL") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.durationSpec(service, DeviceMessageConstants.broadCastLogTableEntryTTLAttributeName, DeviceMessageConstants.broadCastLogTableEntryTTLAttributeDefaultTranslation));
        }
    },
    SetMaxJoinWaitTime(3031, "Set maximum join wait time") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.maxJoinWaitTime, DeviceMessageConstants.maxJoinWaitTimeDefaultTranslation));
        }
    },
    SetPathDiscoveryTime(3032, "Set path discovery time") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.pathDiscoveryTime, DeviceMessageConstants.pathDiscoveryTimeDefaultTranslation));
        }
    },
    SetMaxNumberOfHopsAttributeName(3026, "Set maximum number of hops") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.maxNumberOfHopsAttributeName, DeviceMessageConstants.maxNumberOfHopsAttributeDefaultTranslation));
        }
    },
    SetMetricType(3033, "Set metric type") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.metricType, DeviceMessageConstants.metricTypeDefaultTranslation));
        }
    },
    SetCoordShortAddress(3059, "Set coord short address") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.coordShortAddress, DeviceMessageConstants.coordShortAddressDefaultTranslation));
        }
    },
    SetToneMaskAttributeName(3062, "Set tone mask") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.toneMaskAttributeName, DeviceMessageConstants.toneMaskAttributeDefaultTranslation));
        }
    },
    SetTMRTTL(3034, "Set maximum valid time of tone map parameters in the neighbour table") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.TMRTTL, DeviceMessageConstants.TMRTTLDefaultTranslation));
        }
    },
    SetMaxFrameRetries(3035, "Set maximum frame retries") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.MaxFrameRetries, DeviceMessageConstants.MaxFrameRetriesDefaultTranslation));
        }
    },
    SetNeighbourTableEntryTTL(3036, "Set time to live of the neighbour table entries") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.NeighbourTableEntryTTL, DeviceMessageConstants.NeighbourTableEntryTTLDefaultTranslation));
        }
    },
    SetHighPriorityWindowSize(3037, "Set high priority window size") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.HighPriorityWindowSize, DeviceMessageConstants.HighPriorityWindowSizeDefaultTranslation));
        }
    },
    SetCSMAFairnessLimit(3038, "Set CSMA fairness limit") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.CSMAFairnessLimit, DeviceMessageConstants.CSMAFairnessLimitDefaultTranslation));
        }
    },
    SetBeaconRandomizationWindowLength(3039, "Set beacon randomization window length") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.BeaconRandomizationWindowLength, DeviceMessageConstants.BeaconRandomizationWindowLengthDefaultTranslation));
        }
    },
    SetMacA(3040, "Set adaptive CW linear decrease") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.MacA, DeviceMessageConstants.MacADefaultTranslation));
        }
    },
    SetMacK(3041, "Set rate adaptation factor for channel access fairness limit") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.MacK, DeviceMessageConstants.MacKDefaultTranslation));
        }
    },
    SetMinimumCWAttempts(3042, "Set minimum CW attempts") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.MinimumCWAttempts, DeviceMessageConstants.MinimumCWAttemptsDefaultTranslation));
        }
    },
    SetMaxBe(3043, "Set maximum value of backoff exponent") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.maxBe, DeviceMessageConstants.maxBeDefaultTranslation));
        }
    },
    SetMaxCSMABackOff(3044, "Set maximum number of backoff attempts") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.maxCSMABackOff, DeviceMessageConstants.maxCSMABackOffDefaultTranslation));
        }
    },
    SetMinBe(3045, "Set minimum value of backoff exponent") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.minBe, DeviceMessageConstants.minBeDefaultTranslation));
        }
    },
    PathRequest(3046, "Execute path request") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.deviceGroupSpec(service, DeviceMessageConstants.deviceGroupAttributeName, DeviceMessageConstants.deviceGroupAttributeDefaultTranslation));
        }
    },
    SetSecurityLevel(3028, "Set security level for adaptation frames") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.plcSecurityLevel, DeviceMessageConstants.plcSecurityLevelDefaultTranslation));
        }
    },
    SetRoutingConfiguration(3029, "Set routing configuration") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.adp_Kr, DeviceMessageConstants.adp_KrDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.adp_Km, DeviceMessageConstants.adp_KmDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.adp_Kc, DeviceMessageConstants.adp_KcDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.adp_Kq, DeviceMessageConstants.adp_KqDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.adp_Kh, DeviceMessageConstants.adp_KhDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.adp_Krt, DeviceMessageConstants.adp_KrtDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.adp_RREQ_retries, DeviceMessageConstants.adp_RREQ_retriesDefaultTranslation),
                    this.booleanSpec(service, DeviceMessageConstants.adp_RLC_enabled, DeviceMessageConstants.adp_RLC_enabledDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.adp_net_traversal_time, DeviceMessageConstants.adp_net_traversal_timeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.adp_routing_table_entry_TTL, DeviceMessageConstants.adp_routing_table_entry_TTLDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.adp_routing_tuple_TTL, DeviceMessageConstants.adp_routing_tuple_TTLDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.adp_RREQ_RERR_wait, DeviceMessageConstants.adp_RREQ_RERR_waitDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.adp_Blacklist_table_entry_TTL, DeviceMessageConstants.adp_Blacklist_table_entry_TTLDefaultTranslation),
                    this.booleanSpec(service, DeviceMessageConstants.adp_unicast_RREQ_gen_enable, DeviceMessageConstants.adp_unicast_RREQ_gen_enableDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.adp_add_rev_link_cost, DeviceMessageConstants.adp_add_rev_link_costDefaultTranslation)
            );
        }
    },
    SetPanId(3019, "Set PAN id") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.G3PanIdAttributename, DeviceMessageConstants.G3PanIdAttributeDefaultTranslation));
        }
    },
    SetWeakLQIValueAttributeName(3027, "Set weak LQI value") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.weakLQIValueAttributeName, DeviceMessageConstants.weakLQIValueAttributeDefaultTranslation));
        }
    },
    WritePlcG3Timeout(3017, "Write PLC G3 timeout") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.plcG3TimeoutAttributeName, DeviceMessageConstants.plcG3TimeoutAttributeDefaultTranslation));
        }
    },
    ResetPlcOfdmMacCounters(3018, "Reset PLC OFDM mac counters") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    SetDisableDefaultRouting(3060, "Disable default routing") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.booleanSpec(service, DeviceMessageConstants.disableDefaultRouting, DeviceMessageConstants.disableDefaultRoutingDefaultTranslation));
        }
    },
    SetDeviceType(3061, "Set device type") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.deviceType, DeviceMessageConstants.deviceTypeDefaultTranslation));
        }
    },
    SetSFSKRepeater(3021, "Set SFSK repeater") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.REPEATERAttributeName, DeviceMessageConstants.REPEATERAttributeDefaultTranslation));
        }
    },
    SetSFSKGain(3022, "Set SFSK gain") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.MAX_RECEIVING_GAINAttributeName, DeviceMessageConstants.MAX_RECEIVING_GAINAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.MAX_TRANSMITTING_GAINAttributeName, DeviceMessageConstants.MAX_TRANSMITTING_GAINAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.SEARCH_INITIATOR_GAINAttributeName, DeviceMessageConstants.SEARCH_INITIATOR_GAINAttributeDefaultTranslation)
            );
        }
    },
    SetTimeoutNotAddressed(3023, "Set timeout not addressed") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.durationSpec(service, DeviceMessageConstants.TIME_OUT_NOT_ADDRESSEDAttributeName, DeviceMessageConstants.TIME_OUT_NOT_ADDRESSEDAttributeDefaultTranslation));
        }
    },
    SetSFSKMacTimeouts(3024, "Set SFSK mac timeouts") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.SEARCH_INITIATOR_TIMEOUTAttributeName, DeviceMessageConstants.SEARCH_INITIATOR_TIMEOUTAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.SYNCHRONIZATION_CONFIRMATION_TIMEOUTAttributeName, DeviceMessageConstants.SYNCHRONIZATION_CONFIRMATION_TIMEOUTAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.TIME_OUT_NOT_ADDRESSEDAttributeName, DeviceMessageConstants.TIME_OUT_NOT_ADDRESSEDAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.TIME_OUT_FRAME_NOT_OKAttributeName, DeviceMessageConstants.TIME_OUT_FRAME_NOT_OKAttributeDefaultTranslation)
            );
        }
    },
    SetPlcChannelFreqSnrCredits(3025, "Set PLC channel frequency SNR credits") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.CHANNEL1_FSAttributeName, DeviceMessageConstants.CHANNEL1_FSAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.CHANNEL1_FMAttributeName, DeviceMessageConstants.CHANNEL1_FMAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.CHANNEL1_SNRAttributeName, DeviceMessageConstants.CHANNEL1_SNRAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.CHANNEL1_CREDITWEIGHTAttributeName, DeviceMessageConstants.CHANNEL1_CREDITWEIGHTAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.CHANNEL2_FSAttributeName, DeviceMessageConstants.CHANNEL2_FSAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.CHANNEL2_FMAttributeName, DeviceMessageConstants.CHANNEL2_FMAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.CHANNEL2_SNRAttributeName, DeviceMessageConstants.CHANNEL2_SNRAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.CHANNEL2_CREDITWEIGHTAttributeName, DeviceMessageConstants.CHANNEL2_CREDITWEIGHTAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.CHANNEL3_FSAttributeName, DeviceMessageConstants.CHANNEL3_FSAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.CHANNEL3_FMAttributeName, DeviceMessageConstants.CHANNEL3_FMAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.CHANNEL3_SNRAttributeName, DeviceMessageConstants.CHANNEL3_SNRAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.CHANNEL3_CREDITWEIGHTAttributeName, DeviceMessageConstants.CHANNEL3_CREDITWEIGHTAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.CHANNEL4_FSAttributeName, DeviceMessageConstants.CHANNEL4_FSAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.CHANNEL4_FMAttributeName, DeviceMessageConstants.CHANNEL4_FMAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.CHANNEL4_SNRAttributeName, DeviceMessageConstants.CHANNEL4_SNRAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.CHANNEL4_CREDITWEIGHTAttributeName, DeviceMessageConstants.CHANNEL4_CREDITWEIGHTAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.CHANNEL5_FSAttributeName, DeviceMessageConstants.CHANNEL5_FSAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.CHANNEL5_FMAttributeName, DeviceMessageConstants.CHANNEL5_FMAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.CHANNEL5_SNRAttributeName, DeviceMessageConstants.CHANNEL5_SNRAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.CHANNEL5_CREDITWEIGHTAttributeName, DeviceMessageConstants.CHANNEL5_CREDITWEIGHTAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.CHANNEL6_FSAttributeName, DeviceMessageConstants.CHANNEL6_FSAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.CHANNEL6_FMAttributeName, DeviceMessageConstants.CHANNEL6_FMAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.CHANNEL6_SNRAttributeName, DeviceMessageConstants.CHANNEL6_SNRAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.CHANNEL6_CREDITWEIGHTAttributeName, DeviceMessageConstants.CHANNEL6_CREDITWEIGHTAttributeDefaultTranslation)
            );
        }
    },
    PLCPrimeCancelFirmwareUpgrade(3065, "Cancel firmware upgrade") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    PLCPrimeReadPIB(3066, "Read PIB value of a node") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.macAddress, DeviceMessageConstants.macAddressDefaultTranslation));
        }
    },
    PLCPrimeRequestFirmwareVersion(3067, "Request the firmware version of a node") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.macAddress, DeviceMessageConstants.macAddressDefaultTranslation));
        }
    },
    PLCPrimeWritePIB(3068, "Write PIB value of a node") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.macAddress, DeviceMessageConstants.macAddressDefaultTranslation));
        }
    },
    PLCEnableDisable(3069, "Enable or disable PLC") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.enablePLC, DeviceMessageConstants.enablePLCDefaultTranslation));
        }
    },
    PLCFreqPairSelection(3070, "PLC frequency pair selection") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.frequencyPair, DeviceMessageConstants.frequencyPairDefaultTranslation));
        }
    },
    PLCRequestConfig(3071, "PLC request configuration event") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    CIASEDiscoveryMaxCredits(3072, "Set the maximum number of discovery credits") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.discoveryMaxCredits, DeviceMessageConstants.discoveryMaxCreditsDefaultTranslation));
        }
    },
    PLCChangeMacAddress(3073, "Change PLC MAC address") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.macAddress, DeviceMessageConstants.macAddressDefaultTranslation));
        }
    },

    IDISDiscoveryConfiguration(3074, "Discovery configuration") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.interval, DeviceMessageConstants.intervalDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.duration, DeviceMessageConstants.durationDefaultTranslation));
        }
    },
    IDISRepeaterCallConfiguration(3075, "IDIS repeater call configuration") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.interval, DeviceMessageConstants.intervalDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.receptionThreshold, DeviceMessageConstants.receptionThresholdDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.numberOfTimeSlotsForNewSystems, DeviceMessageConstants.numberOfTimeSlotsForNewSystemsDefaultTranslation)
            );
        }
    },
    //Configuration of G3 interface on RTU+Server2
    SetAutomaticRouteManagement(3047, "Set automatic route management") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.booleanSpec(service, DeviceMessageConstants.pingEnabled, DeviceMessageConstants.pingEnabledDefaultTranslation),
                    this.booleanSpec(service, DeviceMessageConstants.routeRequestEnabled, DeviceMessageConstants.routeRequestEnabledDefaultTranslation),
                    this.booleanSpec(service, DeviceMessageConstants.pathRequestEnabled, DeviceMessageConstants.pathRequestEnabledDefaultTranslation)
            );
        }
    },
    EnableSNR(3048, "Enable SNR") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.booleanSpec(service, DeviceMessageConstants.EnableSNR, DeviceMessageConstants.EnableSNRDefaultTranslation));
        }
    },
    SetSNRPacketInterval(3049, "Set SNR packet interval") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.SNRPacketInterval, DeviceMessageConstants.SNRPacketIntervalDefaultTranslation));
        }
    },
    SetSNRQuietTime(3050, "Set SNR quiet time") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.SNRQuietTime, DeviceMessageConstants.SNRQuietTimeDefaultTranslation));
        }
    },
    SetSNRPayload(3051, "Set SNR payload") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.hexStringSpec(service, DeviceMessageConstants.SNRPayload, DeviceMessageConstants.SNRPayloadDefaultTranslation));
        }
    },
    EnableKeepAlive(3052, "Enable keep alive") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.booleanSpec(service, DeviceMessageConstants.EnableKeepAlive, DeviceMessageConstants.EnableKeepAliveDefaultTranslation));
        }
    },
    SetKeepAliveScheduleInterval(3053, "Set interval for keep alive schedule") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.KeepAliveScheduleInterval, DeviceMessageConstants.KeepAliveScheduleIntervalDefaultTranslation));
        }
    },
    SetKeepAliveBucketSize(3054, "Set bucket size for keep alive") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.KeepAliveBucketSize, DeviceMessageConstants.KeepAliveBucketSizeDefaultTranslation));
        }
    },
    SetMinInactiveMeterTime(3055, "Set minimum inactive meter time") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.minInactiveMeterTime, DeviceMessageConstants.minInactiveMeterTimeDefaultTranslation));
        }
    },
    SetMaxInactiveMeterTime(3056, "Set maximum inactive meter time") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.maxInactiveMeterTime, DeviceMessageConstants.maxInactiveMeterTimeDefaultTranslation));
        }
    },
    SetKeepAliveRetries(3057, "Set retries for keep alive") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.KeepAliveRetries, DeviceMessageConstants.KeepAliveRetriesDefaultTranslation));
        }
    },
    SetKeepAliveTimeout(3058, "Set timeout for keep alive") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.KeepAliveTimeout, DeviceMessageConstants.KeepAliveTimeoutDefaultTranslation));
        }
    },
    EnableG3PLCInterface(3063, "Enable G3 PLC interface") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.booleanSpec(service, DeviceMessageConstants.enablePLC, DeviceMessageConstants.enablePLCDefaultTranslation));
        }
    },
    IDISRunRepeaterCallNow(3076, "IDIS Run repeater call now") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList() ;
        }
    },
    IDISRunNewMeterDiscoveryCallNow(3077, "IDIS Run new meter discovery now") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList() ;
        }
    },
    IDISRunAlarmDiscoveryCallNow(3078, "IDIS Run alarm discovery now") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList() ;
        }
    },
    IDISWhitelistConfiguration(3079, "IDIS Local whitelist configuration") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.enabled, DeviceMessageConstants.enabledDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.groupName, DeviceMessageConstants.groupNameDefaultTranslation)
            );
        }
    },
    IDISOperatingWindowConfiguration(3080, "IDIS Operating window configuration") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.enabled, DeviceMessageConstants.enabledDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.startTime, DeviceMessageConstants.startTimeDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.endTime, DeviceMessageConstants.endTimeDefaultTranslation)
            );
        }
    },
    IDISPhyConfiguration(3081, "IDIS phy configuration") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.bitSync, DeviceMessageConstants.bitSyncDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.zeroCrossAdjust, DeviceMessageConstants.zeroCrossAdjustDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.txGain, DeviceMessageConstants.txGainDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.rxGain, DeviceMessageConstants.rxGainDefaultTranslation)
            );
        }
    },
    IDISCreditManagementConfiguration(3082, "IDIS Credit management configuration") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.addCredit, DeviceMessageConstants.addCreditDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.minCredit, DeviceMessageConstants.minCreditDefaultTranslation)
            );
        }
    },
    ConfigurePLcG3KeepAlive(3064, "Configure PLC G3 keep alive") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.booleanSpec(service, DeviceMessageConstants.EnableKeepAlive, DeviceMessageConstants.EnableKeepAliveDefaultTranslation),
                    this.boundedBigDecimalSpec(service, DeviceMessageConstants.keepAliveStartTime, DeviceMessageConstants.keepAliveStartTimeDefaultTranslation, BigDecimal.ZERO, BigDecimal.valueOf(0xFFFFl)),
                    this.boundedBigDecimalSpec(service, DeviceMessageConstants.keepAliveSendPeriod, DeviceMessageConstants.keepAliveSendPeriodDefaultTranslation, BigDecimal.ONE, BigDecimal.valueOf(0xFFl))
            );
        }
    },
    PingMeter(3083, "Ping meter") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.hexStringSpec(service, DeviceMessageConstants.macAddress, DeviceMessageConstants.macAddressDefaultTranslation),
                    this.durationSpec(service, DeviceMessageConstants.timeout, DeviceMessageConstants.timeoutDefaultTranslation)
            );
        }
    },
    AddMetersToBlackList(3084, "Add meters to blacklist") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.macAddresses, DeviceMessageConstants.macAddressesDefaultTranslation));
        }
    },
    RemoveMetersFromBlackList(3085, "Remove meters from blacklist") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.macAddresses, DeviceMessageConstants.macAddressesDefaultTranslation));
        }
    },
    KickMeter(3086, "Kick meter from network") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.hexStringSpec(service, DeviceMessageConstants.macAddress, DeviceMessageConstants.macAddressDefaultTranslation));
        }
    },
    PathRequestWithTimeout(3087, "Execute path request") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.deviceGroupSpec(service, DeviceMessageConstants.deviceGroupAttributeName, DeviceMessageConstants.deviceGroupAttributeDefaultTranslation),
                    this.durationSpec(service, DeviceMessageConstants.timeout, DeviceMessageConstants.timeoutDefaultTranslation)
            );
        }
    },
    SetLowLQIValueAttributeName(3088, "Set Low LQI Value") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.lowLQIValueAttributeName, DeviceMessageConstants.lowLQIValueAttributeDefaultTranslation));
        }
    },
    SetHighLQIValueAttributeName(3089, "Set High LQI Value") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.highLQIValueAttributeName, DeviceMessageConstants.highLQIValueAttributeDefaultTranslation));
        }
    };

    private final long id;
    private final String defaultNameTranslation;

    PLCConfigurationDeviceMessage(long id, String defaultNameTranslation) {
        this.id = id;
        this.defaultNameTranslation = defaultNameTranslation;
    }


    protected abstract List<PropertySpec> getPropertySpecs(PropertySpecService service);

    protected PropertySpec booleanSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .booleanSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
    }

    protected PropertySpec deviceGroupSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .referenceSpec(DeviceGroup.class.getName())
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
    }

    protected PropertySpec durationSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .durationSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
    }

    protected PropertySpec boundedBigDecimalSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, BigDecimal lowerLimit, BigDecimal upperLimit) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .boundedBigDecimalSpec(lowerLimit, upperLimit)
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
    }

    protected PropertySpecBuilder<BigDecimal> bigDecimalSpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .bigDecimalSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired();
    }

    protected PropertySpec bigDecimalSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return this.bigDecimalSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation).finish();
    }

    protected PropertySpec stringSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .stringSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
    }

    protected PropertySpec hexStringSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .hexStringSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
    }

    private String getNameResourceKey() {
        return PLCConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public long id() {
        return this.id;
    }

    @Override
    public DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        return new DeviceMessageSpecImpl(
                id, new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation),
                DeviceMessageCategories.PLC_CONFIGURATION,
                this.getPropertySpecs(propertySpecService),
                propertySpecService, nlsService, converter);
    }

}