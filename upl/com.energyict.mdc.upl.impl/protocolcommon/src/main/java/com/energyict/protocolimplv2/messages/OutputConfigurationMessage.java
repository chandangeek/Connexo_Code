package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.messages.nls.TranslationKeyImpl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Provides a summary of all messages related to general Device Actions.
 * <p/>
 * Copyrights EnergyICT
 * Date: 11/03/13
 * Time: 11:59
 */
public enum OutputConfigurationMessage implements DeviceMessageSpecSupplier {

    SetOutputOn(35001, "Set output on") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.OutputOn, DeviceMessageConstants.OutputOnDefaultTranslation));
        }
    },
    SetOutputOff(35002, "Set output off") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.OutputOff, DeviceMessageConstants.OutputOffDefaultTranslation));
        }
    },
    SetOutputToggle(35003, "Set output toggle") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.OutputToggle, DeviceMessageConstants.OutputToggleDefaultTranslation));
        }
    },
    SetOutputPulse(35004, "Set output pulse") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.OutputPulse, DeviceMessageConstants.OutputPulseDefaultTranslation));
        }
    },
    OutputOff(35005, "Output off") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.output, DeviceMessageConstants.outputDefaultTranslation));
        }
    },
    OutputOn(35006, "Output on") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.output, DeviceMessageConstants.outputDefaultTranslation));
        }
    },
    AbsoluteDOSwitchRule(35007, "Write absolute DO switch rule") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.startTime, DeviceMessageConstants.startTimeDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.endTime, DeviceMessageConstants.endTimeDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.outputBitMap, DeviceMessageConstants.outputBitMapDefaultTranslation)
            );
        }
    },
    DeleteDOSwitchRule(35008, "Delete a DO switch rule") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.delete, DeviceMessageConstants.deleteDefaultTranslation)
            );
        }
    },
    RelativeDOSwitchRule(35009, "Write a relative DO switch rule") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.duration, DeviceMessageConstants.durationDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.outputBitMap, DeviceMessageConstants.outputBitMapDefaultTranslation)
            );
        }
    },
    WriteOutputState(35010, "Write output state") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.outputId, DeviceMessageConstants.outputIdDefaultTranslation),
                    this.booleanSpec(service, DeviceMessageConstants.newState, DeviceMessageConstants.newStateDefaultTranslation)
            );
        }
    };

    private final long id;
    private final String defaultNameTranslation;

    OutputConfigurationMessage(long id, String defaultNameTranslation) {
        this.id = id;
        this.defaultNameTranslation = defaultNameTranslation;
    }

    protected abstract List<PropertySpec> getPropertySpecs(PropertySpecService service);

    protected PropertySpec booleanSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .booleanSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
    }

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
        return OutputConfigurationMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        return new DeviceMessageSpecImpl(
                id, new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation),
                DeviceMessageCategories.OUTPUT_CONFIGURATION,
                this.getPropertySpecs(propertySpecService),
                propertySpecService, nlsService);
    }

}