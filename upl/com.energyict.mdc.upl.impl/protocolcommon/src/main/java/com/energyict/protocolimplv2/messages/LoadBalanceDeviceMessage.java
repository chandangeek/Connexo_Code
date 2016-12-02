package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.NumberLookup;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.messages.enums.LoadControlActions;
import com.energyict.protocolimplv2.messages.enums.MonitoredValue;
import com.energyict.protocolimplv2.messages.nls.TranslationKeyImpl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.actionWhenUnderThresholdAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.actionWhenUnderThresholdAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activateNowAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activateNowAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activationDatedAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activationDatedAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contractualPowerLimitAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contractualPowerLimitAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.controlThreshold1dAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.controlThreshold1dAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.controlThreshold2dAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.controlThreshold2dAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.controlThreshold3dAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.controlThreshold3dAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.controlThreshold4dAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.controlThreshold4dAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.controlThreshold5dAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.controlThreshold5dAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.controlThreshold6dAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.controlThreshold6dAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyProfileActivationDateAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyProfileActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyProfileDurationAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyProfileDurationAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyProfileGroupIdListAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyProfileGroupIdListAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyProfileIdAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyProfileIdAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyThresholdAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyThresholdAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.invertDigitalOutput1AttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.invertDigitalOutput1AttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.invertDigitalOutput2AttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.invertDigitalOutput2AttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.loadLimitEndDateAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.loadLimitEndDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.loadLimitGroupIDAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.loadLimitGroupIDAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.loadLimitStartDateAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.loadLimitStartDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.monitorInstanceAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.monitorInstanceAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.monitoredValueAttributeDefaultName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.monitoredValueAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.negativeThresholdInAmpereAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.negativeThresholdInAmpereAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.normalThresholdAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.normalThresholdAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.overThresholdDurationAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.overThresholdDurationAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.phaseAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.phaseAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.positiveThresholdInAmpereAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.positiveThresholdInAmpereAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.powerLimitThresholdAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.powerLimitThresholdAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.readFrequencyInMinutesAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.readFrequencyInMinutesAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.thresholdInAmpereAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.thresholdInAmpereAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.underThresholdDurationAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.underThresholdDurationAttributeName;

/**
 * Provides a summary of all messages related to
 * <ul>
 * <li>Load limiting</li>
 * <li>Energy balance</li>
 * <li>Grid stability</li>
 * </ul>
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/04/13
 * Time: 9:43
 */
public enum LoadBalanceDeviceMessage implements DeviceMessageSpecFactory {

    WriteControlThresholds(0, "Write control thresholds") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, controlThreshold1dAttributeName, controlThreshold1dAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, controlThreshold2dAttributeName, controlThreshold2dAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, controlThreshold3dAttributeName, controlThreshold3dAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, controlThreshold4dAttributeName, controlThreshold4dAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, controlThreshold5dAttributeName, controlThreshold5dAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, controlThreshold6dAttributeName, controlThreshold6dAttributeDefaultTranslation),
                    this.dateTimeSpec(service, activationDatedAttributeName, activationDatedAttributeDefaultTranslation)
            );
        }
    },
    SetDemandCloseToContractPowerThreshold(1, "Set threshold for demand close to contract power") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.DemandCloseToContractPowerThresholdAttributeName, DeviceMessageConstants.DemandCloseToContractPowerThresholdAttributeDefaultTranslation));
        }
    },
    CONFIGURE_LOAD_LIMIT_PARAMETERS(2, "Configure the load limit parameters") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, normalThresholdAttributeName, normalThresholdAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, emergencyThresholdAttributeName, emergencyThresholdAttributeDefaultTranslation),
                    this.temporalAmountSpec(service, overThresholdDurationAttributeName, overThresholdDurationAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, emergencyProfileIdAttributeName, emergencyProfileIdAttributeDefaultTranslation),
                    this.dateTimeSpec(service, emergencyProfileActivationDateAttributeName, emergencyProfileActivationDateAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, emergencyProfileDurationAttributeName, emergencyProfileDurationAttributeDefaultTranslation)
            );
        }
    },
    CONFIGURE_LOAD_LIMIT_PARAMETERS_Z3(3, "Configure load limit parameters") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.durationSpec(service, readFrequencyInMinutesAttributeName, readFrequencyInMinutesAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, normalThresholdAttributeName, normalThresholdAttributeDefaultTranslation),
                    this.durationSpec(service, overThresholdDurationAttributeName, overThresholdDurationAttributeDefaultTranslation),
                    this.booleanSpec(service, invertDigitalOutput1AttributeName, invertDigitalOutput1AttributeDefaultTranslation),
                    this.booleanSpec(service, invertDigitalOutput2AttributeName, invertDigitalOutput2AttributeDefaultTranslation),
                    this.booleanSpec(service, activateNowAttributeName, activateNowAttributeDefaultTranslation)
            );
        }
    },
    CONFIGURE_ALL_LOAD_LIMIT_PARAMETERS(4, "Configure all load limit parameters") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpecBuilder(service, monitoredValueAttributeName, monitoredValueAttributeDefaultName)
                            .setDefaultValue(MonitoredValue.TotalInstantCurrent.getDescription())
                            .addValues(MonitoredValue.getAllDescriptions())
                            .finish(),
                    this.bigDecimalSpec(service, normalThresholdAttributeName, normalThresholdAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, emergencyThresholdAttributeName, emergencyThresholdAttributeDefaultTranslation),
                    this.temporalAmountSpec(service, overThresholdDurationAttributeName, overThresholdDurationAttributeDefaultTranslation),
                    this.temporalAmountSpec(service, underThresholdDurationAttributeName, underThresholdDurationAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, emergencyProfileIdAttributeName, emergencyProfileIdAttributeDefaultTranslation),
                    this.dateTimeSpec(service, emergencyProfileActivationDateAttributeName, emergencyProfileActivationDateAttributeDefaultTranslation),
                    this.temporalAmountSpec(service, emergencyProfileDurationAttributeName, emergencyProfileDurationAttributeDefaultTranslation),
                    this.stringSpec(service, emergencyProfileGroupIdListAttributeName, emergencyProfileGroupIdListAttributeDefaultTranslation),      //List of values, comma separated
                    this.stringSpecBuilder(service, actionWhenUnderThresholdAttributeName, actionWhenUnderThresholdAttributeDefaultTranslation)
                            .setDefaultValue(LoadControlActions.Nothing.getDescription())
                            .addValues(LoadControlActions.getAllDescriptions())
                            .finish()
            );
        }
    },
    CONFIGURE_LOAD_LIMIT_PARAMETERS_FOR_GROUP(5, "Configure load limit parameters for group") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, loadLimitGroupIDAttributeName, loadLimitGroupIDAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, powerLimitThresholdAttributeName, powerLimitThresholdAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, contractualPowerLimitAttributeName, contractualPowerLimitAttributeDefaultTranslation)
            );
        }
    },
    SET_EMERGENCY_PROFILE_GROUP_IDS(6, "Set the load limit emergency profiles") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.numberLookupSpec(service, emergencyProfileGroupIdListAttributeName, emergencyProfileGroupIdListAttributeDefaultTranslation));
        }
    },
    CLEAR_LOAD_LIMIT_CONFIGURATION(7, "Clear the load limit configuration") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    CLEAR_LOAD_LIMIT_CONFIGURATION_FOR_GROUP(8, "Clear load limit configuration for group") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.loadLimitGroupIDAttributeName, DeviceMessageConstants.loadLimitGroupIDAttributeDefaultTranslation));
        }
    },
    ENABLE_LOAD_LIMITING(9, "Enable load limiting") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    ENABLE_LOAD_LIMITING_FOR_GROUP(10,"Enable load limiting for group") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, loadLimitGroupIDAttributeName, loadLimitGroupIDAttributeDefaultTranslation),
                    this.dateTimeSpec(service, loadLimitStartDateAttributeName, loadLimitStartDateAttributeDefaultTranslation),
                    this.dateTimeSpec(service, loadLimitEndDateAttributeName, loadLimitEndDateAttributeDefaultTranslation)
            );
        }
    },
    DISABLE_LOAD_LIMITING(11, "Disable load limiting") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    CONFIGURE_SUPERVISION_MONITOR(12, "Configure supervision monitor") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, phaseAttributeName, phaseAttributeDefaultTranslation, BigDecimal.ONE, BigDecimal.valueOf(2), BigDecimal.valueOf(3)),
                    this.bigDecimalSpec(service, thresholdInAmpereAttributeName, thresholdInAmpereAttributeDefaultTranslation)
            );
        }
    },
    SET_LOAD_LIMIT_DURATION(13, "Set load limit duration") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.temporalAmountSpec(service, overThresholdDurationAttributeName, overThresholdDurationAttributeDefaultTranslation));
        }
    },
    SET_LOAD_LIMIT_THRESHOLD(14, "Set load limit threshold") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, normalThresholdAttributeName, normalThresholdAttributeDefaultTranslation));
        }
    },
    UPDATE_SUPERVISION_MONITOR(15, "Update supervision monitor") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, monitorInstanceAttributeName, monitorInstanceAttributeDefaultTranslation, BigDecimal.ONE, BigDecimal.valueOf(2), BigDecimal.valueOf(3)),
                    this.bigDecimalSpec(service, thresholdInAmpereAttributeName, thresholdInAmpereAttributeDefaultTranslation)
            );
        }
    },
    CONFIGURE_SUPERVISION_MONITOR_FOR_IMPORT_EXPORT(16, "Configure supervision monitor thresholds") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, phaseAttributeName, phaseAttributeDefaultTranslation, BigDecimal.ONE, BigDecimal.valueOf(2), BigDecimal.valueOf(3)),
                    this.bigDecimalSpec(service, positiveThresholdInAmpereAttributeName, positiveThresholdInAmpereAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, negativeThresholdInAmpereAttributeName, negativeThresholdInAmpereAttributeDefaultTranslation)
            );
        }
    };

    private final long id;
    private final String defaultNameTranslation;

    LoadBalanceDeviceMessage(long id, String defaultNameTranslation) {
        this.id = id;
        this.defaultNameTranslation = defaultNameTranslation;
    }

    protected abstract List<PropertySpec> getPropertySpecs(PropertySpecService service);

    protected PropertySpecBuilder<String> stringSpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .stringSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description());
    }

    protected PropertySpec stringSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return this.stringSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation).finish();
    }

    private PropertySpecBuilder<BigDecimal> bigDecimalSpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .bigDecimalSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description());
    }

    protected PropertySpec bigDecimalSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return this.bigDecimalSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation).finish();
    }

    protected PropertySpec bigDecimalSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, BigDecimal... possibleValues) {
        return this.bigDecimalSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation).addValues(possibleValues).finish();
    }

    protected PropertySpec dateTimeSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .dateTimeSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .finish();
    }

    protected PropertySpec booleanSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .booleanSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .finish();
    }

    protected PropertySpec durationSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .durationSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .finish();
    }

    protected PropertySpec temporalAmountSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .temporalAmountSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .finish();
    }

    protected PropertySpec numberLookupSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .referenceSpec(NumberLookup.class)
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .finish();
    }

    private String getNameResourceKey() {
        return LoadBalanceDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        return new DeviceMessageSpecImpl(
                this.id,
                new EnumBasedDeviceMessageSpecPrimaryKey(this, name()),
                new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation),
                DeviceMessageCategories.LOAD_BALANCE,
                this.getPropertySpecs(propertySpecService),
                propertySpecService, nlsService);
    }

}