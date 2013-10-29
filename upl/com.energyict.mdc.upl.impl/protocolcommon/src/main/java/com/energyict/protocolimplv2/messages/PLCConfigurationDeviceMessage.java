package com.energyict.protocolimplv2.messages;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cuo.core.UserEnvironment;
import com.energyict.mdc.messages.*;

import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum PLCConfigurationDeviceMessage implements DeviceMessageSpec {

    ForceManualRescanPLCBus(),
    SetMulticastAddresses(
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.MulticastAddress1AttributeName),
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.MulticastAddress2AttributeName),
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.MulticastAddress3AttributeName)),
    SetActivePlcChannel(PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.ActiveChannelAttributeName)),
    SetPlcChannelFrequencies(
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
    SetSFSKInitiatorPhase(
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.INITIATOR_ELECTRICAL_PHASEAttributeName)),
    SetSFSKMaxFrameLength(
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.MAX_FRAME_LENGTHAttributeName)),

    SetActiveScanDurationAttributeName(PropertySpecFactory.timeDurationPropertySpecWithSmallUnits(DeviceMessageConstants.activeScanDurationAttributeName)),
    SetBroadCastLogTableEntryTTLAttributeName(PropertySpecFactory.timeDurationPropertySpecWithSmallUnits(DeviceMessageConstants.broadCastLogTableEntryTTLAttributeName)),
    SetDiscoveryAttemptsSpeedAttributeName(PropertySpecFactory.timeDurationPropertySpecWithSmallUnits(DeviceMessageConstants.discoveryAttemptsSpeedAttributeName)),
    SetMaxAgeTimeAttributeName(PropertySpecFactory.timeDurationPropertySpecWithSmallUnits(DeviceMessageConstants.maxAgeTimeAttributeName)),
    SetMaxNumberOfHopsAttributeName(PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.maxNumberOfHopsAttributeName)),
    SetMaxPANConflictsCountAttributeName(PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.maxPANConflictsCountAttributeName)),
    SetPanConflictWaitTimeAttributeName(PropertySpecFactory.timeDurationPropertySpecWithSmallUnits(DeviceMessageConstants.panConflictWaitTimeAttributeName)),
    SetToneMaskAttributeName(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.toneMaskAttributeName)),
    SetWeakLQIValueAttributeName(PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.weakLQIValueAttributeName)),
    WritePlcG3Timeout(PropertySpecFactory.timeDurationPropertySpecWithSmallUnits(DeviceMessageConstants.plcG3TimeoutAttributeName)),
    ResetPlcOfdmMacCounters(),
    SetPanId(PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.G3PanIdAttributename)),
    SetMaxOrphanTimer(PropertySpecFactory.timeDurationPropertySpecWithSmallUnits(DeviceMessageConstants.MaxOrphanTimerAttributeName)),

    SetSFSKRepeater(
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.REPEATERAttributeName)),
    SetSFSKGain(
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.MAX_RECEIVING_GAINAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.MAX_TRANSMITTING_GAINAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.SEARCH_INITIATOR_GAINAttributeName)),
    SetTimeoutNotAddressed(PropertySpecFactory.timeDurationPropertySpecWithSmallUnits(DeviceMessageConstants.TIME_OUT_NOT_ADDRESSEDAttributeName)),
    SetSFSKMacTimeouts(
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.SEARCH_INITIATOR_TIMEOUTAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.SYNCHRONIZATION_CONFIRMATION_TIMEOUTAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.TIME_OUT_NOT_ADDRESSEDAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.TIME_OUT_FRAME_NOT_OKAttributeName)),
    SetPlcChannelFreqSnrCredits(
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
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL6_CREDITWEIGHTAttributeName));

    private static final DeviceMessageCategory category = DeviceMessageCategories.PLC_CONFIGURATION;

    private final List<PropertySpec> deviceMessagePropertySpecs;

    private PLCConfigurationDeviceMessage(PropertySpec... deviceMessagePropertySpecs) {
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
}