package com.energyict.mdc.protocol.api.impl.device.messages;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;

import java.util.ArrayList;
import java.util.List;

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.*;

/**
 * Provides a summary of all <i>Clock</i> related messages
 * <p/>
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum PeakShaverConfigurationDeviceMessage implements DeviceMessageSpecEnum {

    SetActiveChannel(DeviceMessageId.PEAK_SHAVING_SET_ACTIVE_CHANNEL, "Set active channel") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(this.stringProperty(SetActiveChannelAttributeName, propertySpecService));
        }
    },
    SetReactiveChannel(DeviceMessageId.PEAK_SHAVING_SET_REACTIVE_CHANNEL, "Set reactive channel") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(this.stringProperty(SetReactiveChannelAttributeName, propertySpecService));
        }
    },
    SetTimeBase(DeviceMessageId.PEAK_SHAVING_SET_TIME_BASE, "Set time base") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(this.stringProperty(SetTimeBaseAttributeName, propertySpecService));
        }
    },
    SetPOut(DeviceMessageId.PEAK_SHAVING_SET_P_OUT, "Set POut") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(this.stringProperty(SetPOutAttributeName, propertySpecService));
        }
    },
    SetPIn(DeviceMessageId.PEAK_SHAVING_SET_P_IN, "Set PIn") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(this.stringProperty(SetPInAttributeName, propertySpecService));
        }
    },
    SetDeadTime(DeviceMessageId.PEAK_SHAVING_SET_DEAD_TIME, "Set dead time") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(this.stringProperty(SetDeadTimeAttributeName, propertySpecService));
        }
    },
    SetAutomatic(DeviceMessageId.PEAK_SHAVING_SET_AUTOMATIC, "Set automatic") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(this.stringProperty(SetAutomaticAttributeName, propertySpecService));
        }
    },
    SetCyclic(DeviceMessageId.PEAK_SHAVING_SET_CYCLIC, "Set cyclic") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(this.stringProperty(SetCyclicAttributeName, propertySpecService));
        }
    },
    SetInvert(DeviceMessageId.PEAK_SHAVING_SET_INVERT, "Set invert") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(this.stringProperty(SetInvertAttributeName, propertySpecService));
        }
    },
    SetAdaptSetpoint(DeviceMessageId.PEAK_SHAVING_SET_ADAPT_SETPOINT, "Set adapt setpoint") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(this.stringProperty(SetAdaptSetpointAttributeName, propertySpecService));
        }
    },
    SetInstantAnalogOut(DeviceMessageId.PEAK_SHAVING_SET_INSTANT_ANALOG_OUT, "Set instant analog out") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(this.stringProperty(SetInstantAnalogOutAttributeName, propertySpecService));
        }
    },
    SetPredictedAnalogOut(DeviceMessageId.PEAK_SHAVING_SET_PREDICTED_ANALOG_OUT, "Set predicted analog out") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(this.stringProperty(SetPredictedAnalogOutAttributeName, propertySpecService));
        }
    },
    SetpointAnalogOut(DeviceMessageId.PEAK_SHAVING_SETPOINT_ANALOG_OUT, "Set setpoint analog out") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(this.stringProperty(SetpointAnalogOutAttributeName, propertySpecService));
        }
    },
    SetDifferenceAnalogOut(DeviceMessageId.PEAK_SHAVING_SET_DIFFERENCE_ANALOG_OUT, "Set difference analog out") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(this.stringProperty(SetDifferenceAnalogOutAttributeName, propertySpecService));
        }
    },
    SetTariff(DeviceMessageId.PEAK_SHAVING_SET_TARIFF, "Set tariff") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(this.stringProperty(SetTariffAttributeName, propertySpecService));
        }
    },
    SetResetLoads(DeviceMessageId.PEAK_SHAVING_SET_RESET_LOADS, "Set reset loads") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(this.stringProperty(SetResetLoadsAttributeName, propertySpecService));
        }
    },
    SetSetpoint(DeviceMessageId.PEAK_SHAVING_SET_SETPOINT, "Set setpoint") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(this.stringProperty(tariff, propertySpecService));
            propertySpecs.add(this.stringProperty(CurrentValueAttributeName, propertySpecService));
            propertySpecs.add(this.stringProperty(NewValueAttributeName, propertySpecService));
        }
    },
    SetSwitchTime(DeviceMessageId.PEAK_SHAVING_SET_SWITCH_TIME, "Set switch time") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(this.bigDecimalProperty(day, propertySpecService));
            propertySpecs.add(this.bigDecimalProperty(month, propertySpecService));
            propertySpecs.add(this.bigDecimalProperty(year, propertySpecService));
            propertySpecs.add(this.bigDecimalProperty(hour, propertySpecService));
            propertySpecs.add(this.bigDecimalProperty(minute, propertySpecService));
            propertySpecs.add(this.bigDecimalProperty(second, propertySpecService));
        }
    },
    SetLoad(DeviceMessageId.PEAK_SHAVING_SET_LOAD, "Set load") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(this.bigDecimalProperty(loadIdAttributeName, propertySpecService));
            propertySpecs.add(this.stringProperty(MaxOffAttributeName, propertySpecService));
            propertySpecs.add(this.stringProperty(DelayAttributeName, propertySpecService));
            propertySpecs.add(this.stringProperty(ManualAttributeName, propertySpecService));
            propertySpecs.add(this.stringProperty(StatusAttributeName, propertySpecService));
            propertySpecs.add(this.stringProperty(PeakShaverIPAddressAttributeName, propertySpecService));
            propertySpecs.add(this.stringProperty(PeakShaveChnNbrAttributeName, propertySpecService));
        }
    };

    private DeviceMessageId id;
    private String defaultTranslation;

    PeakShaverConfigurationDeviceMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }

    @Override
    public String getKey() {
        return PeakShaverConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
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
        propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.id, true, new BigDecimalFactory()));
    };

    protected PropertySpec stringProperty(String name, PropertySpecService propertySpecService) {
        return propertySpecService.basicPropertySpec(name, true, new StringFactory());
    }

    protected PropertySpec bigDecimalProperty(String name, PropertySpecService propertySpecService) {
        return propertySpecService.basicPropertySpec(name, true, new BigDecimalFactory());
    }

    public final PropertySpec getPropertySpec(String name, PropertySpecService propertySpecService) {
        for (PropertySpec securityProperty : getPropertySpecs(propertySpecService)) {
            if (securityProperty.getName().equals(name)) {
                return securityProperty;
            }
        }
        return null;
    }

}