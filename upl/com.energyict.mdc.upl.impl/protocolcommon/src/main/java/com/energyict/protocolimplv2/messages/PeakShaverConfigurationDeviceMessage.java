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

    SetActiveChannel(16001, "Set active channel") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.SetActiveChannelAttributeName, DeviceMessageConstants.SetActiveChannelAttributeDefaultTranslation)
            );
        }
    },
    SetReactiveChannel(16002, "Set reactive channel") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.SetReactiveChannelAttributeName, DeviceMessageConstants.SetReactiveChannelAttributeDefaultTranslation)
            );
        }
    },
    SetTimeBase(16003, "Set time base") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.SetTimeBaseAttributeName, DeviceMessageConstants.SetTimeBaseAttributeDefaultTranslation)
            );
        }
    },
    SetPOut(16004, "Set POut") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.SetPOutAttributeName, DeviceMessageConstants.SetPOutAttributeDefaultTranslation)
            );
        }
    },
    SetPIn(16005, "Set PIn") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.SetPInAttributeName, DeviceMessageConstants.SetPInAttributeDefaultTranslation)
            );
        }
    },
    SetDeadTime(16006, "Set dead time") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.SetDeadTimeAttributeName, DeviceMessageConstants.SetDeadTimeAttributeDefaultTranslation)
            );
        }
    },
    SetAutomatic(16007, "Set automatic") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.SetAutomaticAttributeName, DeviceMessageConstants.SetAutomaticAttributeDefaultTranslation)
            );
        }
    },
    SetCyclic(16008, "Set cyclic") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.SetCyclicAttributeName, DeviceMessageConstants.SetCyclicAttributeDefaultTranslation)
            );
        }
    },
    SetInvert(16009, "Set invert") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.SetInvertAttributeName, DeviceMessageConstants.SetInvertAttributeDefaultTranslation)
            );
        }
    },
    SetAdaptSetpoint(16010, "Set adapt setpoint") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.SetAdaptSetpointAttributeName, DeviceMessageConstants.SetAdaptSetpointAttributeDefaultTranslation)
            );
        }
    },
    SetInstantAnalogOut(16011, "Set instant analog out") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.SetInstantAnalogOutAttributeName, DeviceMessageConstants.SetInstantAnalogOutAttributeDefaultTranslation)
            );
        }
    },
    SetPredictedAnalogOut(16012, "Set predicted analog out") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.SetPredictedAnalogOutAttributeName, DeviceMessageConstants.SetPredictedAnalogOutAttributeDefaultTranslation)
            );
        }
    },
    SetpointAnalogOut(16013, "Set setpoint analog out") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.SetpointAnalogOutAttributeName, DeviceMessageConstants.SetpointAnalogOutAttributeDefaultTranslation)
            );
        }
    },
    SetDifferenceAnalogOut(16014, "Set difference analog out") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.SetDifferenceAnalogOutAttributeName, DeviceMessageConstants.SetDifferenceAnalogOutAttributeDefaultTranslation)
            );
        }
    },
    SetTariff(16015, "Set tariff") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.SetTariffAttributeName, DeviceMessageConstants.SetTariffAttributeDefaultTranslation)
            );
        }
    },
    SetResetLoads(16016, "Set reset loads") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.SetResetLoadsAttributeName, DeviceMessageConstants.SetResetLoadsAttributeDefaultTranslation)
            );
        }
    },
    SetSetpoint(16017, "Set setpoint") {
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
    SetSwitchTime(16018, "Set switch time") {
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
    SetLoad(16019, "Set load") {
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
                .markRequired()
                .finish();
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

    private String getNameResourceKey() {
        return PeakShaverConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        return new DeviceMessageSpecImpl(
                id, new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation),
                DeviceMessageCategories.PEAK_SHAVER_CONFIGURATION,
                this.getPropertySpecs(propertySpecService),
                propertySpecService, nlsService, converter);
    }

}