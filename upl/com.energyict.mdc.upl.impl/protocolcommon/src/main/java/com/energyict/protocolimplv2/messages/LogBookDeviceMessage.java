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
 * Provides a summary of all DeviceMessages related to configuration/readout of LogBooks.
 * <p/>
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum LogBookDeviceMessage implements DeviceMessageSpecFactory {

    SetInputChannel(0, "Set input channel") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.SetInputChannelAttributeName, DeviceMessageConstants.SetInputChannelAttributeDefaultTranslation));
        }
    },
    SetCondition(1, "Set condition") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.SetConditionAttributeName, DeviceMessageConstants.SetConditionAttributeDefaultTranslation));
        }
    },
    SetConditionValue(2, "Set condition value") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.SetConditionValueAttributeName, DeviceMessageConstants.SetConditionValueAttributeDefaultTranslation));
        }
    },
    SetTimeTrue(3, "Set time true") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.SetTimeTrueAttributeName, DeviceMessageConstants.SetTimeTrueAttributeDefaultTranslation));
        }
    },
    SetTimeFalse(4, "Set time false") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.SetTimeFalseAttributeName, DeviceMessageConstants.SetTimeFalseAttributeDefaultTranslation));
        }
    },
    SetOutputChannel(5, "Set output channel") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.SetOutputChannelAttributeName, DeviceMessageConstants.SetOutputChannelAttributeDefaultTranslation));
        }
    },
    SetAlarm(6, "Set alarm") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.SetAlarmAttributeName, DeviceMessageConstants.SetAlarmAttributeDefaultTranslation));
        }
    },
    SetTag(7, "Set tag") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.SetTagAttributeName, DeviceMessageConstants.SetTagAttributeDefaultTranslation));
        }
    },
    SetInverse(8, "Set inverse") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.SetInverseAttributeName, DeviceMessageConstants.SetInverseAttributeDefaultTranslation));
        }
    },
    SetImmediate(9, "Set immediate") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.SetImmediateAttributeName, DeviceMessageConstants.SetImmediateAttributeDefaultTranslation));
        }
    },
    ReadDebugLogBook(10, "Read debug logbook") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.dateTimeSpec(service, DeviceMessageConstants.fromDateAttributeName, DeviceMessageConstants.fromDateAttributeNameDefaultTranslation),
                    this.dateTimeSpec(service, DeviceMessageConstants.toDateAttributeName, DeviceMessageConstants.toDateAttributeNameDefaultTranslation)
            );
        }
    },
    ReadManufacturerSpecificLogBook(11, "Read manufacturer specific logbook") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.dateTimeSpec(service, DeviceMessageConstants.fromDateAttributeName, DeviceMessageConstants.fromDateAttributeNameDefaultTranslation),
                    this.dateTimeSpec(service, DeviceMessageConstants.toDateAttributeName, DeviceMessageConstants.toDateAttributeNameDefaultTranslation)
            );
        }
    },
    ResetMainLogbook(12, "Reset main logbook") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    ResetCoverLogbook(13, "Reset cover logbook") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    ResetBreakerLogbook(14, "Reset breaker logbook") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    ResetCommunicationLogbook(15, "Reset communication logbook") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    ResetLQILogbook(16, "Reset LQI logbook") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    ResetVoltageCutLogbook(17, "Reset voltage cut logbook") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    ReadLogBook(18, "Read logbook") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    ResetSecurityLogbook(19, "Reset security logbook") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    };

    private final long id;
    private final String defaultNameTranslation;

    LogBookDeviceMessage(long id, String defaultNameTranslation) {
        this.id = id;
        this.defaultNameTranslation = defaultNameTranslation;
    }

    protected abstract List<PropertySpec> getPropertySpecs(PropertySpecService service);

    protected PropertySpec stringSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .stringSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .finish();
    }

    protected PropertySpec dateTimeSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .dateTimeSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .finish();
    }

    private String getNameResourceKey() {
        return LogBookDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        return new DeviceMessageSpecImpl(
                this.id,
                new EnumBasedDeviceMessageSpecPrimaryKey(this, name()),
                new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation),
                DeviceMessageCategories.LOG_BOOKS,
                this.getPropertySpecs(propertySpecService),
                propertySpecService, nlsService);
    }

}