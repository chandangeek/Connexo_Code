package com.energyict.mdc.protocol.api.impl.device.messages;

import com.energyict.mdc.dynamic.HexStringFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.TimeDurationValueFactory;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;

import java.util.ArrayList;
import java.util.List;

import static com.energyict.mdc.protocol.api.impl.device.messages.DeviceMessageConstants.*;

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
    };

    private DeviceMessageId id;
    private String defaultTranslation;

    PLCConfigurationDeviceMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }

    @Override
    public String getNameResourceKey() {
        return PLCConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public String defaultTranslation() {
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