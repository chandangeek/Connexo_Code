package com.energyict.protocolimplv2.messages;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cuo.core.UserEnvironment;
import com.energyict.mdc.messages.DeviceMessageCategory;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.messages.DeviceMessageSpecPrimaryKey;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum PLCConfigurationDeviceMessage implements DeviceMessageSpec {

    ForceManualRescanPLCBus(0),
    SetMulticastAddresses(1,
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.MulticastAddress1AttributeName),
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.MulticastAddress2AttributeName),
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.MulticastAddress3AttributeName)),
    SetActivePlcChannel(2, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.ActiveChannelAttributeName)),
    SetPlcChannelFrequencies(3,
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL1_FSAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL1_FMAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL2_FSAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL2_FMAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL3_FSAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL3_FMAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL4_FSAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL4_FMAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL5_FSAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL5_FMAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL6_FSAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL6_FMAttributeName)),
    SetSFSKInitiatorPhase(4,
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.INITIATOR_ELECTRICAL_PHASEAttributeName)),
    SetSFSKMaxFrameLength(5,
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.MAX_FRAME_LENGTHAttributeName)),

    SetBroadCastLogTableEntryTTLAttributeName(7, PropertySpecFactory.timeDurationPropertySpecWithSmallUnits(DeviceMessageConstants.broadCastLogTableEntryTTLAttributeName)),
    SetMaxJoinWaitTime(8, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.maxJoinWaitTime)),
    SetPathDiscoveryTime(9, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.pathDiscoveryTime)),
    SetMaxNumberOfHopsAttributeName(10, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.maxNumberOfHopsAttributeName)),
    SetMetricType(11, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.metricType)),
    SetCoordShortAddress(12, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.coordShortAddress)),
    SetToneMaskAttributeName(13, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.toneMaskAttributeName)),
    SetTMRTTL(35, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.TMRTTL)),
    SetMaxFrameRetries(36, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.MaxFrameRetries)),
    SetNeighbourTableEntryTTL(37, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.NeighbourTableEntryTTL)),
    SetHighPriorityWindowSize(38, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.HighPriorityWindowSize)),
    SetCSMAFairnessLimit(39, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.CSMAFairnessLimit)),
    SetBeaconRandomizationWindowLength(40, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.BeaconRandomizationWindowLength)),
    SetMacA(41, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.MacA)),
    SetMacK(42, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.MacK)),
    SetMinimumCWAttempts(43, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.MinimumCWAttempts)),
    SetMaxBe(44, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.maxBe)),
    SetMaxCSMABackOff(45, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.maxCSMABackOff)),
    SetMinBe(46, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.minBe)),
    PathRequest(47, PropertySpecFactory.groupReferencePropertySpec(DeviceMessageConstants.deviceGroupAttributeName)),
    SetSecurityLevel(48, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.plcSecurityLevel)),
    SetRoutingConfiguration(49,
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.adp_Kr),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.adp_Km),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.adp_Kc),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.adp_Kq),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.adp_Kh),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.adp_Krt),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.adp_RREQ_retries),
            PropertySpecFactory.notNullableBooleanPropertySpec(DeviceMessageConstants.adp_RLC_enabled),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.adp_net_traversal_time),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.adp_routing_table_entry_TTL),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.adp_RREQ_RERR_wait),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.adp_Blacklist_table_entry_TTL),
            PropertySpecFactory.notNullableBooleanPropertySpec(DeviceMessageConstants.adp_unicast_RREQ_gen_enable),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.adp_add_rev_link_cost)
    ),
    SetPanId(50, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.G3PanIdAttributename)),
    SetWeakLQIValueAttributeName(14, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.weakLQIValueAttributeName)),
    WritePlcG3Timeout(15, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.plcG3TimeoutAttributeName)),
    ResetPlcOfdmMacCounters(16),
    SetDisableDefaultRouting(17, PropertySpecFactory.notNullableBooleanPropertySpec(DeviceMessageConstants.disableDefaultRouting)),
    SetDeviceType(18, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.deviceType)),

    SetSFSKRepeater(19,
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.REPEATERAttributeName)),
    SetSFSKGain(20,
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.MAX_RECEIVING_GAINAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.MAX_TRANSMITTING_GAINAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.SEARCH_INITIATOR_GAINAttributeName)),
    SetTimeoutNotAddressed(21, PropertySpecFactory.timeDurationPropertySpecWithSmallUnits(DeviceMessageConstants.TIME_OUT_NOT_ADDRESSEDAttributeName)),
    SetSFSKMacTimeouts(22,
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.SEARCH_INITIATOR_TIMEOUTAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.SYNCHRONIZATION_CONFIRMATION_TIMEOUTAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.TIME_OUT_NOT_ADDRESSEDAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.TIME_OUT_FRAME_NOT_OKAttributeName)),
    SetPlcChannelFreqSnrCredits(23,
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL1_FSAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL1_FMAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL1_SNRAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL1_CREDITWEIGHTAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL2_FSAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL2_FMAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL2_SNRAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL2_CREDITWEIGHTAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL3_FSAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL3_FMAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL3_SNRAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL3_CREDITWEIGHTAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL4_FSAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL4_FMAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL4_SNRAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL4_CREDITWEIGHTAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL5_FSAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL5_FMAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL5_SNRAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL5_CREDITWEIGHTAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL6_FSAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL6_FMAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL6_SNRAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL6_CREDITWEIGHTAttributeName)),
    PLCPrimeCancelFirmwareUpgrade(24),
    PLCPrimeReadPIB(25, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.macAddress)),
    PLCPrimeRequestFirmwareVersion(26, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.macAddress)),
    PLCPrimeWritePIB(27, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.macAddress)),

    PLCEnableDisable(28, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.enablePLC)),
    PLCFreqPairSelection(29, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.frequencyPair)),
    PLCRequestConfig(30),
    CIASEDiscoveryMaxCredits(31, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.discoveryMaxCredits)),
    PLCChangeMacAddress(32, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.macAddress)),

    IDISDiscoveryConfiguration(33,
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.interval),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.duration)),
    IDISRepeaterCallConfiguration(34,
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.interval),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.receptionThreshold),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.numberOfTimeSlotsForNewSystems)),

    //Configuration of G3 interface on RTU+Server2
    SetAutomaticRouteManagement(51,
            PropertySpecFactory.notNullableBooleanPropertySpec(DeviceMessageConstants.pingEnabled),
            PropertySpecFactory.notNullableBooleanPropertySpec(DeviceMessageConstants.routeRequestEnabled),
            PropertySpecFactory.notNullableBooleanPropertySpec(DeviceMessageConstants.pathRequestEnabled)
    ),
    EnableSNR(52, PropertySpecFactory.notNullableBooleanPropertySpec(DeviceMessageConstants.EnableSNR)),
    SetSNRPacketInterval(53, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.SNRPacketInterval)),
    SetSNRQuietTime(54, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.SNRQuietTime)),
    SetSNRPayload(55, PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.SNRPayload)),
    EnableKeepAlive(56, PropertySpecFactory.notNullableBooleanPropertySpec(DeviceMessageConstants.EnableKeepAlive)),
    SetKeepAliveScheduleInterval(57, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.KeepAliveScheduleInterval)),
    SetKeepAliveBucketSize(58, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.KeepAliveBucketSize)),
    SetMinInactiveMeterTime(59, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.minInactiveMeterTime)),
    SetMaxInactiveMeterTime(60, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.maxInactiveMeterTime)),
    SetKeepAliveRetries(61, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.KeepAliveRetries)),
    SetKeepAliveTimeout(62, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.KeepAliveTimeout)),
    EnableG3PLCInterface(63, PropertySpecFactory.booleanPropertySpec(DeviceMessageConstants.enablePLC)),
    IDISRunRepeaterCallNow(64),
    IDISRunNewMeterDiscoveryCallNow(65),
    IDISRunAlarmDiscoveryCallNow(66),
    IDISWhitelistConfiguration(67,
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.enabled),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.groupName)
    ),
    IDISOperatingWindowConfiguration(68,
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.enabled),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.startTime),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.endTime)
    ),
    IDISPhyConfiguration(69,
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.bitSync),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.zeroCrossAdjust),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.txGain),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.rxGain)
    ),
    IDISCreditManagementConfiguration(70,
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.addCredit),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.minCredit)
    ),
    ConfigurePLcG3KeepAlive(71,
            PropertySpecFactory.notNullableBooleanPropertySpec(DeviceMessageConstants.EnableKeepAlive),
            PropertySpecFactory.boundedDecimalPropertySpec(DeviceMessageConstants.keepAliveStartTime, BigDecimal.ZERO, BigDecimal.valueOf(0xFFFFl)),
            PropertySpecFactory.boundedDecimalPropertySpec(DeviceMessageConstants.keepAliveSendPeriod, BigDecimal.ONE, BigDecimal.valueOf(0xFFl))
    ),
    PingMeter(72, PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.macAddress),
            PropertySpecFactory.timeDurationPropertySpecWithSmallUnits(DeviceMessageConstants.timeout));


    private static final DeviceMessageCategory category = DeviceMessageCategories.PLC_CONFIGURATION;

    private final List<PropertySpec> deviceMessagePropertySpecs;
    private final int id;

    private PLCConfigurationDeviceMessage(int id, PropertySpec... deviceMessagePropertySpecs) {
        this.id = id;
        this.deviceMessagePropertySpecs = Arrays.asList(deviceMessagePropertySpecs);
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return category;
    }

    @Override
    public String getName() {
        return UserEnvironment.getDefault().getTranslation(this.getNameResourceKey());
    }

    /**
     * Gets the resource key that determines the name
     * of this category to the user's language settings.
     *
     * @return The resource key
     */
    private String getNameResourceKey() {
        return PLCConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return this.deviceMessagePropertySpecs;
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        for (PropertySpec securityProperty : getPropertySpecs()) {
            if (securityProperty.getName().equals(name)) {
                return securityProperty;
            }
        }
        return null;
    }

    @Override
    public DeviceMessageSpecPrimaryKey getPrimaryKey() {
        return new DeviceMessageSpecPrimaryKey(this, name());
    }

    @Override
    public int getMessageId() {
        return id;
    }
}