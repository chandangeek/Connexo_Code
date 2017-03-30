/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;

enum PLCConfigurationDeviceMessageAttributes implements TranslationKey {

    ActiveChannelAttributeName(DeviceMessageConstants.ActiveChannelAttributeName, "ActiveChannel"),
    CHANNEL1_FSAttributeName(DeviceMessageConstants.CHANNEL1_FSAttributeName, "CHANNEL1_FS"),
    CHANNEL1_FMAttributeName(DeviceMessageConstants.CHANNEL1_FMAttributeName, "CHANNEL1_FM"),
    CHANNEL1_SNRAttributeName(DeviceMessageConstants.CHANNEL1_SNRAttributeName, "CHANNEL1_SNR"),
    CHANNEL1_CREDITWEIGHTAttributeName(DeviceMessageConstants.CHANNEL1_CREDITWEIGHTAttributeName, "CHANNEL1_CREDITWEIGHT"),
    CHANNEL2_FSAttributeName(DeviceMessageConstants.CHANNEL2_FSAttributeName, "CHANNEL2_FS"),
    CHANNEL2_FMAttributeName(DeviceMessageConstants.CHANNEL2_FMAttributeName, "CHANNEL2_FM"),
    CHANNEL2_SNRAttributeName(DeviceMessageConstants.CHANNEL2_SNRAttributeName, "CHANNEL2_SNR"),
    CHANNEL2_CREDITWEIGHTAttributeName(DeviceMessageConstants.CHANNEL2_CREDITWEIGHTAttributeName, "CHANNEL2_CREDITWEIGHT"),
    CHANNEL3_FSAttributeName(DeviceMessageConstants.CHANNEL3_FSAttributeName, "CHANNEL3_FS"),
    CHANNEL3_FMAttributeName(DeviceMessageConstants.CHANNEL3_FMAttributeName, "CHANNEL3_FM"),
    CHANNEL3_SNRAttributeName(DeviceMessageConstants.CHANNEL3_SNRAttributeName, "CHANNEL3_SNR"),
    CHANNEL3_CREDITWEIGHTAttributeName(DeviceMessageConstants.CHANNEL3_CREDITWEIGHTAttributeName, "CHANNEL3_CREDITWEIGHT"),
    CHANNEL4_FSAttributeName(DeviceMessageConstants.CHANNEL4_FSAttributeName, "CHANNEL4_FS"),
    CHANNEL4_FMAttributeName(DeviceMessageConstants.CHANNEL4_FMAttributeName, "CHANNEL4_FM"),
    CHANNEL4_SNRAttributeName(DeviceMessageConstants.CHANNEL4_SNRAttributeName, "CHANNEL4_SNR"),
    CHANNEL4_CREDITWEIGHTAttributeName(DeviceMessageConstants.CHANNEL4_CREDITWEIGHTAttributeName, "CHANNEL4_CREDITWEIGHT"),
    CHANNEL5_FSAttributeName(DeviceMessageConstants.CHANNEL5_FSAttributeName, "CHANNEL5_FS"),
    CHANNEL5_FMAttributeName(DeviceMessageConstants.CHANNEL5_FMAttributeName, "CHANNEL5_FM"),
    CHANNEL5_SNRAttributeName(DeviceMessageConstants.CHANNEL5_SNRAttributeName, "CHANNEL5_SNR"),
    CHANNEL5_CREDITWEIGHTAttributeName(DeviceMessageConstants.CHANNEL5_CREDITWEIGHTAttributeName, "CHANNEL5_CREDITWEIGHT"),
    CHANNEL6_FSAttributeName(DeviceMessageConstants.CHANNEL6_FSAttributeName, "CHANNEL6_FS"),
    CHANNEL6_FMAttributeName(DeviceMessageConstants.CHANNEL6_FMAttributeName, "CHANNEL6_FM"),
    CHANNEL6_SNRAttributeName(DeviceMessageConstants.CHANNEL6_SNRAttributeName, "CHANNEL6_SNR"),
    CHANNEL6_CREDITWEIGHTAttributeName(DeviceMessageConstants.CHANNEL6_CREDITWEIGHTAttributeName, "CHANNEL6_CREDITWEIGHT"),
    INITIATOR_ELECTRICAL_PHASEAttributeName(DeviceMessageConstants.INITIATOR_ELECTRICAL_PHASEAttributeName, "Electrical phase initiator"),
    MulticastAddress1AttributeName(DeviceMessageConstants.MulticastAddress1AttributeName, "Multicast address 1"),
    MulticastAddress2AttributeName(DeviceMessageConstants.MulticastAddress2AttributeName, "Multicast address 2"),
    MulticastAddress3AttributeName(DeviceMessageConstants.MulticastAddress3AttributeName, "Multicast address 3"),
    activeScanDurationAttributeName(DeviceMessageConstants.activeScanDurationAttributeName, "activeScanDuration"),
    broadCastLogTableEntryTTLAttributeName(DeviceMessageConstants.broadCastLogTableEntryTTLAttributeName, "broadCastLogTableEntryTTL"),
    discoveryAttemptsSpeedAttributeName(DeviceMessageConstants.discoveryAttemptsSpeedAttributeName, "discoveryAttemptsSpeed"),
    maxAgeTimeAttributeName(DeviceMessageConstants.maxAgeTimeAttributeName, "maxAgeTime"),
    maxNumberOfHopsAttributeName(DeviceMessageConstants.maxNumberOfHopsAttributeName, "maxNumberOfHops"),
    maxPANConflictsCountAttributeName(DeviceMessageConstants.maxPANConflictsCountAttributeName, "maxPANConflictsCount"),
    panConflictWaitTimeAttributeName(DeviceMessageConstants.panConflictWaitTimeAttributeName, "panConflictWaitTime"),
    toneMaskAttributeName(DeviceMessageConstants.toneMaskAttributeName, "toneMask"),
    weakLQIValueAttributeName(DeviceMessageConstants.weakLQIValueAttributeName, "weakLQIValue"),
    plcG3TimeoutAttributeName(DeviceMessageConstants.plcG3TimeoutAttributeName, "plcG3Timeout"),
    G3PanIdAttributename(DeviceMessageConstants.G3PanIdAttributename, "G3PanId"),
    MaxOrphanTimerAttributeName(DeviceMessageConstants.MaxOrphanTimerAttributeName, "MaxOrphanTimer"),
    adp_Kr(DeviceMessageConstants.adp_Kr, "adp_Kr"),
    adp_Km(DeviceMessageConstants.adp_Km, "adp_Km"),
    adp_Kc(DeviceMessageConstants.adp_Kc, "adp_Kc"),
    adp_Kq(DeviceMessageConstants.adp_Kq, "adp_Kq"),
    adp_Kh(DeviceMessageConstants.adp_Kh, "adp_Kh"),
    adp_Krt(DeviceMessageConstants.adp_Krt, "adp_Krt"),
    adp_RREQ_retries(DeviceMessageConstants.adp_RREQ_retries, "adp_RREQ_retries"),
    adp_RLC_enabled(DeviceMessageConstants.adp_RLC_enabled, "adp_RLC_enabled"),
    adp_net_traversal_time(DeviceMessageConstants.adp_net_traversal_time, "adp_net_traversal_time"),
    adp_routing_table_entry_TTL(DeviceMessageConstants.adp_routing_table_entry_TTL, "adp_routing_table_entry_TTL"),
    adp_RREQ_RERR_wait(DeviceMessageConstants.adp_RREQ_RERR_wait, "adp_RREQ_RERR_wait"),
    adp_Blacklist_table_entry_TTL(DeviceMessageConstants.adp_Blacklist_table_entry_TTL, "adp_Blacklist_table_entry_TTL"),
    adp_unicast_RREQ_gen_enable(DeviceMessageConstants.adp_unicast_RREQ_gen_enable, "adp_unicast_RREQ_gen_enable"),
    adp_add_rev_link_cost(DeviceMessageConstants.adp_add_rev_link_cost, "adp_add_rev_link_cost"),
    disableDefaultRouting(DeviceMessageConstants.disableDefaultRouting, "disableDefaultRouting"),
    deviceType(DeviceMessageConstants.deviceType, "deviceType"),
    pingEnabled(DeviceMessageConstants.pingEnabled, "pingEnabled"),
    routeRequestEnabled(DeviceMessageConstants.routeRequestEnabled, "routeRequestEnabled"),
    pathRequestEnabled(DeviceMessageConstants.pathRequestEnabled, "pathRequestEnabled"),
    EnableSNR(DeviceMessageConstants.EnableSNR, "enableSNR"),
    SNRPacketInterval(DeviceMessageConstants.SNRPacketInterval, "snrPacketInterval"),
    SNRQuietTime(DeviceMessageConstants.SNRQuietTime, "snrQuietTime"),
    SNRPayload(DeviceMessageConstants.SNRPayload, "snrPayload"),
    EnableKeepAlive(DeviceMessageConstants.EnableKeepAlive, "enableKeepAlive"),
    keepAliveStartTime(DeviceMessageConstants.keepAliveStartTime, "keepAliveStartTime"),
    keepAliveSendPeriod(DeviceMessageConstants.keepAliveSendPeriod, "keepAliveSendPeriod"),
    KeepAliveScheduleInterval(DeviceMessageConstants.KeepAliveScheduleInterval, "keepAliveScheduleInterval"),
    KeepAliveBucketSize(DeviceMessageConstants.KeepAliveBucketSize, "keepAliveBucketSize"),
    minInactiveMeterTime(DeviceMessageConstants.minInactiveMeterTime, "minInactiveMeterTime"),
    maxInactiveMeterTime(DeviceMessageConstants.maxInactiveMeterTime, "maxInactiveMeterTime"),
    KeepAliveRetries(DeviceMessageConstants.KeepAliveRetries, "keepAliveRetries"),
    KeepAliveTimeout(DeviceMessageConstants.KeepAliveTimeout, "keepAliveTimeout"),
    plcSecurityLevel(DeviceMessageConstants.plcSecurityLevel, "plcSecurityLevel"),
    maxJoinWaitTime(DeviceMessageConstants.maxJoinWaitTime, "maxJoinWaitTime"),
    pathDiscoveryTime(DeviceMessageConstants.pathDiscoveryTime, "pathDiscoveryTime"),
    metricType(DeviceMessageConstants.metricType, "metricType"),
    coordShortAddress(DeviceMessageConstants.coordShortAddress, "coordShortAddress"),
    TMRTTL(DeviceMessageConstants.TMRTTL, "mtrTTL"),
    MaxFrameRetries(DeviceMessageConstants.MaxFrameRetries, "maxFrameRetries"),
    NeighbourTableEntryTTL(DeviceMessageConstants.NeighbourTableEntryTTL, "neighbourTableEntryTTL"),
    HighPriorityWindowSize(DeviceMessageConstants.HighPriorityWindowSize, "highPriorityWindowSize"),
    CSMAFairnessLimit(DeviceMessageConstants.CSMAFairnessLimit, "csmaFairnessLimit"),
    BeaconRandomizationWindowLength(DeviceMessageConstants.BeaconRandomizationWindowLength, "beaconRandomizationWindowLength"),
    MacA(DeviceMessageConstants.MacA, "macA"),
    MacK(DeviceMessageConstants.MacK, "macK"),
    MinimumCWAttempts(DeviceMessageConstants.MinimumCWAttempts, "minimumCWAttempts"),
    maxBe(DeviceMessageConstants.maxBe, "maxBe"),
    maxCSMABackOff(DeviceMessageConstants.maxCSMABackOff, "maxCSMABackOff"),
    minBe(DeviceMessageConstants.minBe, "minBe"),
    MAX_FRAME_LENGTHAttributeName(DeviceMessageConstants.MAX_FRAME_LENGTHAttributeName, "Maximum frame length"),
    REPEATERAttributeName(DeviceMessageConstants.REPEATERAttributeName, "Repeater"),
    MAX_RECEIVING_GAINAttributeName(DeviceMessageConstants.MAX_RECEIVING_GAINAttributeName, "MAX_RECEIVING_GAIN"),
    MAX_TRANSMITTING_GAINAttributeName(DeviceMessageConstants.MAX_TRANSMITTING_GAINAttributeName, "MAX_TRANSMITTING_GAIN"),
    SEARCH_INITIATOR_GAINAttributeName(DeviceMessageConstants.SEARCH_INITIATOR_GAINAttributeName, "SEARCH_INITIATOR_GAIN"),
    SEARCH_INITIATOR_TIMEOUTAttributeName(DeviceMessageConstants.SEARCH_INITIATOR_TIMEOUTAttributeName, "SEARCH_INITIATOR_TIMEOUT"),
    SYNCHRONIZATION_CONFIRMATION_TIMEOUTAttributeName(DeviceMessageConstants.SYNCHRONIZATION_CONFIRMATION_TIMEOUTAttributeName, "SYNCHRONIZATION_CONFIRMATION_TIMEOUT"),
    TIME_OUT_NOT_ADDRESSEDAttributeName(DeviceMessageConstants.TIME_OUT_NOT_ADDRESSEDAttributeName, "TIME_OUT_NOT_ADDRESSED"),
    TIME_OUT_FRAME_NOT_OKAttributeName(DeviceMessageConstants.TIME_OUT_FRAME_NOT_OKAttributeName, "TIME_OUT_FRAME_NOT_OK"),
    enablePLC("PLCConfigurationDeviceMessage.enablePLC", "enablePLC")
    ;

    private final String key;
    private final String defaultFormat;

    PLCConfigurationDeviceMessageAttributes(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

}