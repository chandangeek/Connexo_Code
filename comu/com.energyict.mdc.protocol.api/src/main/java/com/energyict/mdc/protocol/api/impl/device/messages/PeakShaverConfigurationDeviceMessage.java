/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.util.ArrayList;
import java.util.List;

enum PeakShaverConfigurationDeviceMessage implements DeviceMessageSpecEnum {

    SetActiveChannel(DeviceMessageId.PEAK_SHAVING_SET_ACTIVE_CHANNEL, "Set active channel") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(propertySpecService
                    .stringSpec()
                    .named(PeakShaverDeviceMessageAttributes.SetActiveChannelAttributeName)
                    .fromThesaurus(thesaurus)
                    .markRequired()
                    .finish());
        }
    },
    SetReactiveChannel(DeviceMessageId.PEAK_SHAVING_SET_REACTIVE_CHANNEL, "Set reactive channel") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(propertySpecService
                    .stringSpec()
                    .named(PeakShaverDeviceMessageAttributes.SetReactiveChannelAttributeName)
                    .fromThesaurus(thesaurus)
                    .markRequired()
                    .finish());
        }
    },
    SetTimeBase(DeviceMessageId.PEAK_SHAVING_SET_TIME_BASE, "Set time base") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(propertySpecService
                    .stringSpec()
                    .named(PeakShaverDeviceMessageAttributes.SetTimeBaseAttributeName)
                    .fromThesaurus(thesaurus)
                    .markRequired()
                    .finish());
        }
    },
    SetPOut(DeviceMessageId.PEAK_SHAVING_SET_P_OUT, "Set POut") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(propertySpecService
                    .stringSpec()
                    .named(PeakShaverDeviceMessageAttributes.SetPOutAttributeName)
                    .fromThesaurus(thesaurus)
                    .markRequired()
                    .finish());
        }
    },
    SetPIn(DeviceMessageId.PEAK_SHAVING_SET_P_IN, "Set PIn") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(propertySpecService
                    .stringSpec()
                    .named(PeakShaverDeviceMessageAttributes.SetPInAttributeName)
                    .fromThesaurus(thesaurus)
                    .markRequired()
                    .finish());
        }
    },
    SetDeadTime(DeviceMessageId.PEAK_SHAVING_SET_DEAD_TIME, "Set dead time") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(propertySpecService
                    .stringSpec()
                    .named(PeakShaverDeviceMessageAttributes.SetDeadTimeAttributeName)
                    .fromThesaurus(thesaurus)
                    .markRequired()
                    .finish());
        }
    },
    SetAutomatic(DeviceMessageId.PEAK_SHAVING_SET_AUTOMATIC, "Set automatic") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(propertySpecService
                    .stringSpec()
                    .named(PeakShaverDeviceMessageAttributes.SetAutomaticAttributeName)
                    .fromThesaurus(thesaurus)
                    .markRequired()
                    .finish());
        }
    },
    SetCyclic(DeviceMessageId.PEAK_SHAVING_SET_CYCLIC, "Set cyclic") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(propertySpecService
                    .stringSpec()
                    .named(PeakShaverDeviceMessageAttributes.SetCyclicAttributeName)
                    .fromThesaurus(thesaurus)
                    .markRequired()
                    .finish());
        }
    },
    SetInvert(DeviceMessageId.PEAK_SHAVING_SET_INVERT, "Set invert") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(propertySpecService
                    .stringSpec()
                    .named(PeakShaverDeviceMessageAttributes.SetInvertAttributeName)
                    .fromThesaurus(thesaurus)
                    .markRequired()
                    .finish());
        }
    },
    SetAdaptSetpoint(DeviceMessageId.PEAK_SHAVING_SET_ADAPT_SETPOINT, "Set adapt setpoint") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(propertySpecService
                    .stringSpec()
                    .named(PeakShaverDeviceMessageAttributes.SetAdaptSetpointAttributeName)
                    .fromThesaurus(thesaurus)
                    .markRequired()
                    .finish());
        }
    },
    SetInstantAnalogOut(DeviceMessageId.PEAK_SHAVING_SET_INSTANT_ANALOG_OUT, "Set instant analog out") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(propertySpecService
                    .stringSpec()
                    .named(PeakShaverDeviceMessageAttributes.SetInstantAnalogOutAttributeName)
                    .fromThesaurus(thesaurus)
                    .markRequired()
                    .finish());
        }
    },
    SetPredictedAnalogOut(DeviceMessageId.PEAK_SHAVING_SET_PREDICTED_ANALOG_OUT, "Set predicted analog out") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(propertySpecService
                    .stringSpec()
                    .named(PeakShaverDeviceMessageAttributes.SetPredictedAnalogOutAttributeName)
                    .fromThesaurus(thesaurus)
                    .markRequired()
                    .finish());
        }
    },
    SetpointAnalogOut(DeviceMessageId.PEAK_SHAVING_SETPOINT_ANALOG_OUT, "Set setpoint analog out") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(propertySpecService
                    .stringSpec()
                    .named(PeakShaverDeviceMessageAttributes.SetpointAnalogOutAttributeName)
                    .fromThesaurus(thesaurus)
                    .markRequired()
                    .finish());
        }
    },
    SetDifferenceAnalogOut(DeviceMessageId.PEAK_SHAVING_SET_DIFFERENCE_ANALOG_OUT, "Set difference analog out") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(propertySpecService
                    .stringSpec()
                    .named(PeakShaverDeviceMessageAttributes.SetDifferenceAnalogOutAttributeName)
                    .fromThesaurus(thesaurus)
                    .markRequired()
                    .finish());
        }
    },
    SetTariff(DeviceMessageId.PEAK_SHAVING_SET_TARIFF, "Set tariff") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(propertySpecService
                    .stringSpec()
                    .named(PeakShaverDeviceMessageAttributes.SetTariffAttributeName)
                    .fromThesaurus(thesaurus)
                    .markRequired()
                    .finish());
        }
    },
    SetResetLoads(DeviceMessageId.PEAK_SHAVING_SET_RESET_LOADS, "Set reset loads") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(propertySpecService
                    .stringSpec()
                    .named(PeakShaverDeviceMessageAttributes.SetResetLoadsAttributeName)
                    .fromThesaurus(thesaurus)
                    .markRequired()
                    .finish());
        }
    },
    SetSetpoint(DeviceMessageId.PEAK_SHAVING_SET_SETPOINT, "Set setpoint") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(propertySpecService
                    .stringSpec()
                    .named(DeviceMessageConstants.tariff, PeakShaverDeviceMessageAttributes.tariff)
                    .fromThesaurus(thesaurus)
                    .markRequired()
                    .finish());
            propertySpecs.add(propertySpecService
                    .stringSpec()
                    .named(PeakShaverDeviceMessageAttributes.CurrentValueAttributeName)
                    .fromThesaurus(thesaurus)
                    .markRequired()
                    .finish());
            propertySpecs.add(propertySpecService
                    .stringSpec()
                    .named(PeakShaverDeviceMessageAttributes.NewValueAttributeName)
                    .fromThesaurus(thesaurus)
                    .markRequired()
                    .finish());
        }
    },
    SetSwitchTime(DeviceMessageId.PEAK_SHAVING_SET_SWITCH_TIME, "Set switch time") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(propertySpecService
                    .bigDecimalSpec()
                    .named(DeviceMessageConstants.day, PeakShaverDeviceMessageAttributes.day)
                    .fromThesaurus(thesaurus)
                    .markRequired()
                    .finish());
            propertySpecs.add(propertySpecService
                    .bigDecimalSpec()
                    .named(DeviceMessageConstants.month, PeakShaverDeviceMessageAttributes.month)
                    .fromThesaurus(thesaurus)
                    .markRequired()
                    .finish());
            propertySpecs.add(propertySpecService
                    .bigDecimalSpec()
                    .named(DeviceMessageConstants.year, PeakShaverDeviceMessageAttributes.year)
                    .fromThesaurus(thesaurus)
                    .markRequired()
                    .finish());
            propertySpecs.add(propertySpecService
                    .bigDecimalSpec()
                    .named(DeviceMessageConstants.hour, PeakShaverDeviceMessageAttributes.hour)
                    .fromThesaurus(thesaurus)
                    .markRequired()
                    .finish());
            propertySpecs.add(propertySpecService
                    .bigDecimalSpec()
                    .named(DeviceMessageConstants.minute, PeakShaverDeviceMessageAttributes.minute)
                    .fromThesaurus(thesaurus)
                    .markRequired()
                    .finish());
            propertySpecs.add(propertySpecService
                    .bigDecimalSpec()
                    .named(DeviceMessageConstants.second, PeakShaverDeviceMessageAttributes.second)
                    .fromThesaurus(thesaurus)
                    .markRequired()
                    .finish());
        }
    },
    SetLoad(DeviceMessageId.PEAK_SHAVING_SET_LOAD, "Set load") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(propertySpecService
                    .bigDecimalSpec()
                    .named(PeakShaverDeviceMessageAttributes.loadIdAttributeName)
                    .fromThesaurus(thesaurus)
                    .markRequired()
                    .finish());
            propertySpecs.add(propertySpecService
                    .stringSpec()
                    .named(PeakShaverDeviceMessageAttributes.MaxOffAttributeName)
                    .fromThesaurus(thesaurus)
                    .markRequired()
                    .finish());
            propertySpecs.add(propertySpecService
                    .stringSpec()
                    .named(PeakShaverDeviceMessageAttributes.DelayAttributeName)
                    .fromThesaurus(thesaurus)
                    .markRequired()
                    .finish());
            propertySpecs.add(propertySpecService
                    .stringSpec()
                    .named(PeakShaverDeviceMessageAttributes.ManualAttributeName)
                    .fromThesaurus(thesaurus)
                    .markRequired()
                    .finish());
            propertySpecs.add(propertySpecService
                    .stringSpec()
                    .named(PeakShaverDeviceMessageAttributes.StatusAttributeName)
                    .fromThesaurus(thesaurus)
                    .markRequired()
                    .finish());
            propertySpecs.add(propertySpecService
                    .stringSpec()
                    .named(PeakShaverDeviceMessageAttributes.PeakShaverIPAddressAttributeName)
                    .fromThesaurus(thesaurus)
                    .markRequired()
                    .finish());
            propertySpecs.add(propertySpecService
                    .stringSpec()
                    .named(PeakShaverDeviceMessageAttributes.PeakShaveChnNbrAttributeName)
                    .fromThesaurus(thesaurus)
                    .markRequired()
                    .finish());
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

    public final List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        this.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
        return propertySpecs;
    }

    protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        propertySpecs.add(
                propertySpecService
                        .bigDecimalSpec()
                        .named(DeviceMessageConstants.id, PeakShaverDeviceMessageAttributes.id)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish());
    };

}