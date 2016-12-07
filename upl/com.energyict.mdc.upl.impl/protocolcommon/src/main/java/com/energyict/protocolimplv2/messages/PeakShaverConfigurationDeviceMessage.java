package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.messages.nls.TranslationKeyImpl;

import java.util.Arrays;
import java.util.List;

/**
 * Provides a summary of all <i>Clock</i> related messages
 * <p/>
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum PeakShaverConfigurationDeviceMessage implements DeviceMessageSpecSupplier {

    SetActiveChannel(0, "Set active channel") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.SetActiveChannelAttributeName, DeviceMessageConstants.SetActiveChannelAttributeDefaultTranslation)
            );
        }
    },
    SetReactiveChannel(1, "Set reactive channel") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.SetReactiveChannelAttributeName, DeviceMessageConstants.SetReactiveChannelAttributeDefaultTranslation)
            );
        }
    },
    SetTimeBase(2, "Set time base") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.SetTimeBaseAttributeName, DeviceMessageConstants.SetTimeBaseAttributeDefaultTranslation)
            );
        }
    },
    SetPOut(3, "Set POut") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.SetPOutAttributeName, DeviceMessageConstants.SetPOutAttributeDefaultTranslation)
            );
        }
    },
    SetPIn(4, "Set PIn") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.SetPInAttributeName, DeviceMessageConstants.SetPInAttributeDefaultTranslation)
            );
        }
    },
    SetDeadTime(5, "Set dead time") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.SetDeadTimeAttributeName, DeviceMessageConstants.SetDeadTimeAttributeDefaultTranslation)
            );
        }
    },
    SetAutomatic(6, "Set automatic") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.SetAutomaticAttributeName, DeviceMessageConstants.SetAutomaticAttributeDefaultTranslation)
            );
        }
    },
    SetCyclic(7, "Set cyclic") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.SetCyclicAttributeName, DeviceMessageConstants.SetCyclicAttributeDefaultTranslation)
            );
        }
    },
    SetInvert(8, "Set invert") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.SetInvertAttributeName, DeviceMessageConstants.SetInvertAttributeDefaultTranslation)
            );
        }
    },
    SetAdaptSetpoint(9, "Set adapt setpoint") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.SetAdaptSetpointAttributeName, DeviceMessageConstants.SetAdaptSetpointAttributeDefaultTranslation)
            );
        }
    },
    SetInstantAnalogOut(10, "Set instant analog out") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.SetInstantAnalogOutAttributeName, DeviceMessageConstants.SetInstantAnalogOutAttributeDefaultTranslation)
            );
        }
    },
    SetPredictedAnalogOut(11, "Set predicted analog out") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.SetPredictedAnalogOutAttributeName, DeviceMessageConstants.SetPredictedAnalogOutAttributeDefaultTranslation)
            );
        }
    },
    SetpointAnalogOut(12, "Set setpoint analog out") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.SetpointAnalogOutAttributeName, DeviceMessageConstants.SetpointAnalogOutAttributeDefaultTranslation)
            );
        }
    },
    SetDifferenceAnalogOut(13, "Set difference analog out") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.SetDifferenceAnalogOutAttributeName, DeviceMessageConstants.SetDifferenceAnalogOutAttributeDefaultTranslation)
            );
        }
    },
    SetTariff(14, "Set tariff") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.SetTariffAttributeName, DeviceMessageConstants.SetTariffAttributeDefaultTranslation)
            );
        }
    },
    SetResetLoads(15, "Set reset loads") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.SetResetLoadsAttributeName, DeviceMessageConstants.SetResetLoadsAttributeDefaultTranslation)
            );
        }
    },
    SetSetpoint(16, "Set setpoint") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.tariff, DeviceMessageConstants.tariffDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.CurrentValueAttributeName, DeviceMessageConstants.CurrentValueAttributeDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.NewValueAttributeName, DeviceMessageConstants.NewValueAttributeDefaultTranslation)
            );
        }
    },
    SetSwitchTime(17, "Set switch time") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.day, DeviceMessageConstants.dayDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.month, DeviceMessageConstants.monthDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.year, DeviceMessageConstants.yearDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.hour, DeviceMessageConstants.hourDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.minute, DeviceMessageConstants.minuteDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.second, DeviceMessageConstants.secondDefaultTranslation)
            );
        }
    },
    SetLoad(18, "Set load") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.loadIdAttributeName, DeviceMessageConstants.loadIdAttributeDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.MaxOffAttributeName, DeviceMessageConstants.MaxOffAttributeDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.DelayAttributeName, DeviceMessageConstants.DelayAttributeDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.ManualAttributeName, DeviceMessageConstants.ManualAttributeDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.StatusAttributeName, DeviceMessageConstants.StatusAttributeDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.PeakShaverIPAddressAttributeName, DeviceMessageConstants.PeakShaverIPAddressAttributeDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.PeakShaveChnNbrAttributeName, DeviceMessageConstants.PeakShaveChnNbrAttributeDefaultTranslation)
            );
        }
    };

    private final long id;
    private final String defaultNameTranslation;

    PeakShaverConfigurationDeviceMessage(long id, String defaultNameTranslation) {
        this.id = id;
        this.defaultNameTranslation = defaultNameTranslation;
    }

    protected abstract List<PropertySpec> getPropertySpecs(PropertySpecService service);

    protected PropertySpec bigDecimalSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .bigDecimalSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .finish();
    }

    protected PropertySpec stringSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .stringSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .finish();
    }

    private String getNameResourceKey() {
        return PeakShaverConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        return new DeviceMessageSpecImpl(
                this.id,
                new EnumBasedDeviceMessageSpecPrimaryKey(this, name()),
                new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation),
                DeviceMessageCategories.PEAK_SHAVER_CONFIGURATION,
                this.getPropertySpecs(propertySpecService),
                propertySpecService, nlsService);
    }

}