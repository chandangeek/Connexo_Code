package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.messages.enums.DSTAlgorithm;
import com.energyict.protocolimplv2.messages.nls.TranslationKeyImpl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Provides a summary of all <i>Clock</i> related messages.
 * <p/>
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum ClockDeviceMessage implements DeviceMessageSpecSupplier {

    SET_TIME(15001, "Set time") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.dateTimeSpec(service, DeviceMessageConstants.meterTimeAttributeName, DeviceMessageConstants.meterTimeAttributeDefaultTranslation));
        }
    },
    SET_TIMEZONE(15002, "Set time zone") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.TimeZoneOffsetInHoursAttributeName, DeviceMessageConstants.TimeZoneOffsetInHoursAttributeDefaultTranslation));
        }
    },
    EnableOrDisableDST(15003, "Enable or disable DST") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.booleanSpec(service, DeviceMessageConstants.enableDSTAttributeName, DeviceMessageConstants.enableDSTAttributeDefaultTranslation));
        }
    },
    SetEndOfDST(15004, "Set end of DST") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.month, DeviceMessageConstants.monthDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.dayOfMonth, DeviceMessageConstants.dayOfMonthDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.dayOfWeek, DeviceMessageConstants.dayOfWeekDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.hour, DeviceMessageConstants.hourDefaultTranslation)
            );
        }
    },
    SetStartOfDST(15005, "Set start of DST") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.month, DeviceMessageConstants.monthDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.dayOfMonth, DeviceMessageConstants.dayOfMonthDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.dayOfWeek, DeviceMessageConstants.dayOfWeekDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.hour, DeviceMessageConstants.hourDefaultTranslation)
            );
        }
    },
    SetStartOfDSTWithoutHour(15006, "Set start of DST without hour") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.month, DeviceMessageConstants.monthDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.dayOfMonth, DeviceMessageConstants.dayOfMonthDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.dayOfWeek, DeviceMessageConstants.dayOfWeekDefaultTranslation)
            );
        }
    },
    SetEndOfDSTWithoutHour(15007, "Set end of DST without hour") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.month, DeviceMessageConstants.monthDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.dayOfMonth, DeviceMessageConstants.dayOfMonthDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.dayOfWeek, DeviceMessageConstants.dayOfWeekDefaultTranslation)
            );
        }
    },
    SetDSTAlgorithm(15008, "Set DST algorithm") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpecBuilder(service, DeviceMessageConstants.dstStartAlgorithmAttributeName, DeviceMessageConstants.dstStartAlgorithmAttributeDefaultTranslation).addValues(DSTAlgorithm.getAllDescriptions()).finish(),
                    this.stringSpecBuilder(service, DeviceMessageConstants.dstEndAlgorithmAttributeName, DeviceMessageConstants.dstEndAlgorithmAttributeDefaultTranslation).addValues(DSTAlgorithm.getAllDescriptions()).finish()
            );
        }
    },

    //EIWeb messages
    SetDST(15009, "Set DST") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.SetDSTAttributeName, DeviceMessageConstants.SetDSTAttributeDefaultTranslation));
        }
    },
    SetTimezone(15010, "Set time zone") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.SetTimezoneAttributeName, DeviceMessageConstants.SetTimezoneAttributeDefaultTranslation));
        }
    },
    SetTimeAdjustment(15011, "Set time adjustment") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.SetTimeAdjustmentAttributeName, DeviceMessageConstants.SetTimeAdjustmentAttributeDefaultTranslation));
        }
    },
    SetNTPServer(15012, "Set NTP server") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.SetNTPServerAttributeName, DeviceMessageConstants.SetNTPServerAttributeDefaultTranslation));
        }
    },
    SetRefreshClockEvery(15013, "Set refresh clock every") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.SetRefreshClockEveryAttributeName, DeviceMessageConstants.SetRefreshClockEveryAttributeDefaultTranslation));
        }
    },
    SetNTPOptions(15014, "Set NTP options") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.SetNTPOptionsAttributeName, DeviceMessageConstants.SetNTPOptionsAttributeDefaultTranslation));
        }
    },
    FTIONForceTimeSync(15018, "Force time synchronization") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    SyncTime(15015, "Force synchronize clock") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    ConfigureDST(15016, "Configure DST") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.booleanSpec(service, DeviceMessageConstants.enableDSTAttributeName, DeviceMessageConstants.enableDSTAttributeDefaultTranslation),
                    this.dateTimeSpec(service, DeviceMessageConstants.StartOfDSTAttributeName, DeviceMessageConstants.StartOfDSTAttributeDefaultTranslation),
                    this.dateTimeSpec(service, DeviceMessageConstants.EndOfDSTAttributeName, DeviceMessageConstants.EndOfDSTAttributeDefaultTranslation)
            );
        }
    },
    ConfigureDSTWithoutHour(15017, "Configure DST without hour") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.booleanSpec(service, DeviceMessageConstants.enableDSTAttributeName, DeviceMessageConstants.enableDSTAttributeDefaultTranslation),
                    this.dateTimeSpec(service, DeviceMessageConstants.StartOfDSTAttributeName, DeviceMessageConstants.StartOfDSTAttributeDefaultTranslation),
                    this.dateTimeSpec(service, DeviceMessageConstants.EndOfDSTAttributeName, DeviceMessageConstants.EndOfDSTAttributeDefaultTranslation)
            );
        }
    },
    NTPSetOption(15019, "NTP - Set an option") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.singleOptionAttributeName, DeviceMessageConstants.singleOptionAttributeDefaultTranslation));
        }
    },
    NTPClrOption(15020, "NTP - Clear an option") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.singleOptionAttributeName, DeviceMessageConstants.singleOptionAttributeDefaultTranslation));
        }
    },
    ConfigureClock(15021, "Configure clock") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.timeZoneSpec(service, DeviceMessageConstants.TimeZoneOffsetInHoursAttributeName, DeviceMessageConstants.TimeZoneOffsetInHoursAttributeDefaultTranslation),
                    this.booleanSpec(service, DeviceMessageConstants.enableDSTAttributeName, DeviceMessageConstants.enableDSTAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.DSTDeviationAttributeName, DeviceMessageConstants.DSTDeviationAttributeDefaultTranslation)
            );
        }
    },
    ;

    private final long id;
    private final String defaultNameTranslation;

    ClockDeviceMessage(long id, String defaultNameTranslation) {
        this.id = id;
        this.defaultNameTranslation = defaultNameTranslation;
    }

    protected PropertySpecBuilder<String> stringSpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .stringSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired();
    }

    protected PropertySpec stringSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return this.stringSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation).finish();
    }

    protected PropertySpec booleanSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .booleanSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
    }

    private PropertySpecBuilder<BigDecimal> bigDecimalSpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .bigDecimalSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired();
    }

    protected PropertySpec bigDecimalSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return this.bigDecimalSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation).finish();
    }

    protected PropertySpec dateTimeSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .dateTimeSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
    }

    protected PropertySpec timeZoneSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .timeZoneSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
    }

    private String getNameResourceKey() {
        return ClockDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    protected abstract List<PropertySpec> getPropertySpecs(PropertySpecService service);

    @Override
    public DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        return new DeviceMessageSpecImpl(
                id, new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation),
                DeviceMessageCategories.CLOCK,
                this.getPropertySpecs(propertySpecService),
                propertySpecService, nlsService, converter);
    }

}