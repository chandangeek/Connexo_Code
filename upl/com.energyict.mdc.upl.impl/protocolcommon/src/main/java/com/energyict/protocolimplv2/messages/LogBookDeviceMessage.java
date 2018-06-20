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
public enum LogBookDeviceMessage implements DeviceMessageSpecSupplier {

    SetInputChannel(14001, "Set input channel") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.SetInputChannelAttributeName, DeviceMessageConstants.SetInputChannelAttributeDefaultTranslation));
        }
    },
    SetCondition(14002, "Set condition") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.SetConditionAttributeName, DeviceMessageConstants.SetConditionAttributeDefaultTranslation));
        }
    },
    SetConditionValue(14003, "Set condition value") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.SetConditionValueAttributeName, DeviceMessageConstants.SetConditionValueAttributeDefaultTranslation));
        }
    },
    SetTimeTrue(14004, "Set time true") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.SetTimeTrueAttributeName, DeviceMessageConstants.SetTimeTrueAttributeDefaultTranslation));
        }
    },
    SetTimeFalse(14005, "Set time false") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.SetTimeFalseAttributeName, DeviceMessageConstants.SetTimeFalseAttributeDefaultTranslation));
        }
    },
    SetOutputChannel(14006, "Set output channel") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.SetOutputChannelAttributeName, DeviceMessageConstants.SetOutputChannelAttributeDefaultTranslation));
        }
    },
    SetAlarm(14007, "Set alarm") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.SetAlarmAttributeName, DeviceMessageConstants.SetAlarmAttributeDefaultTranslation));
        }
    },
    SetTag(14008, "Set tag") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.SetTagAttributeName, DeviceMessageConstants.SetTagAttributeDefaultTranslation));
        }
    },
    SetInverse(14009, "Set inverse") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.SetInverseAttributeName, DeviceMessageConstants.SetInverseAttributeDefaultTranslation));
        }
    },
    SetImmediate(14010, "Set immediate") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.SetImmediateAttributeName, DeviceMessageConstants.SetImmediateAttributeDefaultTranslation));
        }
    },
    ReadDebugLogBook(14011, "Read debug logbook") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.dateTimeSpec(service, DeviceMessageConstants.fromDateAttributeName, DeviceMessageConstants.fromDateAttributeNameDefaultTranslation),
                    this.dateTimeSpec(service, DeviceMessageConstants.toDateAttributeName, DeviceMessageConstants.toDateAttributeNameDefaultTranslation)
            );
        }
    },
    ReadManufacturerSpecificLogBook(14012, "Read manufacturer specific logbook") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.dateTimeSpec(service, DeviceMessageConstants.fromDateAttributeName, DeviceMessageConstants.fromDateAttributeNameDefaultTranslation),
                    this.dateTimeSpec(service, DeviceMessageConstants.toDateAttributeName, DeviceMessageConstants.toDateAttributeNameDefaultTranslation)
            );
        }
    },
    ResetMainLogbook(14013, "Reset main logbook") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    ResetCoverLogbook(14014, "Reset cover logbook") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    ResetBreakerLogbook(14015, "Reset breaker logbook") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    ResetCommunicationLogbook(14016, "Reset communication logbook") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    ResetLQILogbook(14017, "Reset LQI logbook") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    ResetVoltageCutLogbook(14018, "Reset voltage cut logbook") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    ReadLogBook(14019, "Read logbook") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    ResetSecurityLogbook(14020, "Reset security logbook") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    ResetSecurityGroupEventCounterObjects(14021, "Reset security group event counter objects") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpecWithValues(service, DeviceMessageConstants.securityGroupEventCounters, DeviceMessageConstants.securityGroupEventCountersDefaultTranslation, SecurityEventCounter.getEventCounters())
            );
        }
    },
    ResetAllSecurityGroupEventCounters(14022, "Reset all security group event counters") {
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

    @Override
    public long id() {
        return this.id;
    }

    protected abstract List<PropertySpec> getPropertySpecs(PropertySpecService service);

    protected PropertySpec stringSpecWithValues(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, String[] values) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .stringSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .addValues(values)
                .markRequired()
                .finish();
    }

    private String getNameResourceKey() {
        return LogBookDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        return new DeviceMessageSpecImpl(
                id, new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation),
                DeviceMessageCategories.LOG_BOOKS,
                this.getPropertySpecs(propertySpecService),
                propertySpecService, nlsService, converter);
    }

    public enum SecurityEventCounter {
        G_EC_01("0.0.96.15.21.255"),
        G_EC_02("0.0.96.15.22.255"),
        G_EC_03("0.0.96.15.23.255"),
        G_EC_04("0.0.96.15.24.255"),
        G_EC_05("0.0.96.15.25.255"),
        G_EC_06("0.0.96.15.26.255"),
        G_EC_07("0.0.96.15.27.255"),
        G_EC_08("0.0.96.15.28.255"),
        G_EC_09("0.0.96.15.29.255"),
        G_EC_10("0.0.96.15.30.255");

        private final String obis;

        private SecurityEventCounter(String obis) {
            this.obis = obis;
        }

        public static String[] getEventCounters() {
            SecurityEventCounter[] allCounters = values();
            String[] result = new String[allCounters.length];
            for (int index = 0; index < allCounters.length; index++) {
                result[index] = allCounters[index].name();
            }
            return result;
        }

        public String getObis() {
            return obis;
        }
    }
}