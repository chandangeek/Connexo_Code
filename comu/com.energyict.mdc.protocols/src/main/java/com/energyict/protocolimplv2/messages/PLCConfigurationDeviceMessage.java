package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.common.UserEnvironment;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecPrimaryKey;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.RequiredPropertySpecFactory;

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
            RequiredPropertySpecFactory.newInstance().hexStringPropertySpec(DeviceMessageConstants.MulticastAddress1AttributeName),
            RequiredPropertySpecFactory.newInstance().hexStringPropertySpec(DeviceMessageConstants.MulticastAddress2AttributeName),
            RequiredPropertySpecFactory.newInstance().hexStringPropertySpec(DeviceMessageConstants.MulticastAddress3AttributeName)),
    SetActivePlcChannel(RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.ActiveChannelAttributeName)),
    SetPlcChannelFrequencies(
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL1_FSAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL1_FMAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL2_FSAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL2_FMAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL3_FSAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL3_FMAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL4_FSAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL4_FMAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL5_FSAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL5_FMAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL6_FSAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL6_FMAttributeName)),
    SetSFSKInitiatorPhase(
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.INITIATOR_ELECTRICAL_PHASEAttributeName)),
    SetSFSKMaxFrameLength(
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.MAX_FRAME_LENGTHAttributeName)),

    SetActiveScanDurationAttributeName(RequiredPropertySpecFactory.newInstance().timeDurationPropertySpec(DeviceMessageConstants.activeScanDurationAttributeName)),
    SetBroadCastLogTableEntryTTLAttributeName(RequiredPropertySpecFactory.newInstance().timeDurationPropertySpec(DeviceMessageConstants.broadCastLogTableEntryTTLAttributeName)),
    SetDiscoveryAttemptsSpeedAttributeName(RequiredPropertySpecFactory.newInstance().timeDurationPropertySpec(DeviceMessageConstants.discoveryAttemptsSpeedAttributeName)),
    SetMaxAgeTimeAttributeName(RequiredPropertySpecFactory.newInstance().timeDurationPropertySpec(DeviceMessageConstants.maxAgeTimeAttributeName)),
    SetMaxNumberOfHopsAttributeName(RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.maxNumberOfHopsAttributeName)),
    SetMaxPANConflictsCountAttributeName(RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.maxPANConflictsCountAttributeName)),
    SetPanConflictWaitTimeAttributeName(RequiredPropertySpecFactory.newInstance().timeDurationPropertySpec(DeviceMessageConstants.panConflictWaitTimeAttributeName)),
    SetToneMaskAttributeName(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.toneMaskAttributeName)),
    SetWeakLQIValueAttributeName(RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.weakLQIValueAttributeName)),
    WritePlcG3Timeout(RequiredPropertySpecFactory.newInstance().timeDurationPropertySpec(DeviceMessageConstants.plcG3TimeoutAttributeName)),
    ResetPlcOfdmMacCounters(),
    SetPanId(RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.G3PanIdAttributename)),
    SetMaxOrphanTimer(RequiredPropertySpecFactory.newInstance().timeDurationPropertySpec(DeviceMessageConstants.MaxOrphanTimerAttributeName)),

    SetSFSKRepeater(
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.REPEATERAttributeName)),
    SetSFSKGain(
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.MAX_RECEIVING_GAINAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.MAX_TRANSMITTING_GAINAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.SEARCH_INITIATOR_GAINAttributeName)),
    SetTimeoutNotAddressed(RequiredPropertySpecFactory.newInstance().timeDurationPropertySpec(DeviceMessageConstants.TIME_OUT_NOT_ADDRESSEDAttributeName)),
    SetSFSKMacTimeouts(
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.SEARCH_INITIATOR_TIMEOUTAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.SYNCHRONIZATION_CONFIRMATION_TIMEOUTAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.TIME_OUT_NOT_ADDRESSEDAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.TIME_OUT_FRAME_NOT_OKAttributeName)),
    SetPlcChannelFreqSnrCredits(
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL1_FSAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL1_FMAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL1_SNRAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL1_CREDITWEIGHTAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL2_FSAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL2_FMAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL2_SNRAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL2_CREDITWEIGHTAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL3_FSAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL3_FMAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL3_SNRAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL3_CREDITWEIGHTAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL4_FSAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL4_FMAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL4_SNRAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL4_CREDITWEIGHTAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL5_FSAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL5_FMAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL5_SNRAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL5_CREDITWEIGHTAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL6_FSAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL6_FMAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL6_SNRAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.CHANNEL6_CREDITWEIGHTAttributeName));

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