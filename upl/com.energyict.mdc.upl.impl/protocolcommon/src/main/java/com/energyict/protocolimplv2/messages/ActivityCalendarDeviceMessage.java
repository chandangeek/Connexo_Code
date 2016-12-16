package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.DeviceMessageFile;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TariffCalender;

import com.energyict.protocolimplv2.messages.enums.ActivityCalendarType;
import com.energyict.protocolimplv2.messages.nls.TranslationKeyImpl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.XmlUserFileAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.XmlUserFileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarActivationDateAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarCodeTableAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarCodeTableAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarNameAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarNameAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarTypeAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarTypeAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contractAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contractAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contractsXmlUserFileAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contractsXmlUserFileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.defaultTariffCodeAttrributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.defaultTariffCodeAttrributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.specialDaysCodeTableAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.specialDaysCodeTableAttributeName;

/**
 * Provides a summary of all <i>ActivityCalendar</i> related messages.
 * <p/>
 * Copyrights EnergyICT
 * Date: 7/02/13
 * Time: 12:01
 */
public enum ActivityCalendarDeviceMessage implements DeviceMessageSpecSupplier {

    ACTIVITY_CALENDAR_READ(1, "Read activity calendar") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    WRITE_CONTRACTS_FROM_XML_USERFILE(2, "Write contracts from XML user file") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.deviceMessageFileSpec(service, contractsXmlUserFileAttributeName, contractsXmlUserFileAttributeDefaultTranslation));
        }
    },
    ACTIVITY_CALENDER_SEND(3, "Send activity calendar") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, activityCalendarNameAttributeName, activityCalendarNameAttributeDefaultTranslation),
                    this.codeTableSpec(service, activityCalendarCodeTableAttributeName, activityCalendarCodeTableAttributeDefaultTranslation)
            );
        }
    },
    ACTIVITY_CALENDER_SEND_WITH_DATETIME(4, "Send activity calendar with activation date") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, activityCalendarNameAttributeName, activityCalendarNameAttributeDefaultTranslation),
                    this.codeTableSpec(service, activityCalendarCodeTableAttributeName, activityCalendarCodeTableAttributeDefaultTranslation),
                    this.dateTimeSpec(service, activityCalendarActivationDateAttributeName, activityCalendarActivationDateAttributeDefaultTranslation)
            );
        }
    },
    ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_TYPE(5, "Send activity calendar with activation date and type") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, activityCalendarTypeAttributeName, activityCalendarTypeAttributeDefaultTranslation, ActivityCalendarType.getAllDescriptions()),
                    this.stringSpec(service, activityCalendarNameAttributeName, activityCalendarNameAttributeDefaultTranslation),
                    this.codeTableSpec(service, activityCalendarCodeTableAttributeName, activityCalendarCodeTableAttributeDefaultTranslation),
                    this.dateTimeSpec(service, activityCalendarActivationDateAttributeName, activityCalendarActivationDateAttributeDefaultTranslation)
            );
        }
    },
    ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_CONTRACT(11, "Send activity calendar with activation date and contract") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, contractAttributeName, contractAttributeDefaultTranslation),
                    this.stringSpec(service, activityCalendarNameAttributeName, activityCalendarNameAttributeDefaultTranslation),
                    this.codeTableSpec(service, activityCalendarCodeTableAttributeName, activityCalendarCodeTableAttributeDefaultTranslation),
                    this.dateTimeSpec(service, activityCalendarActivationDateAttributeName, activityCalendarActivationDateAttributeDefaultTranslation)
            );
        }
    },
    ACTIVITY_CALENDER_SEND_WITH_DATE(6, "Send activity calendar with activation date") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.codeTableSpec(service, activityCalendarCodeTableAttributeName, activityCalendarCodeTableAttributeDefaultTranslation),
                    this.dateTimeSpec(service, activityCalendarActivationDateAttributeName, activityCalendarActivationDateAttributeDefaultTranslation)
            );
        }
    },
    SPECIAL_DAY_CALENDAR_SEND(7, "Send special days calendar") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.codeTableSpec(service, specialDaysCodeTableAttributeName, specialDaysCodeTableAttributeDefaultTranslation));
        }
    },
    SPECIAL_DAY_CALENDAR_SEND_WITH_TYPE(8, "Send special days calendar with type") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, activityCalendarTypeAttributeName, activityCalendarTypeAttributeDefaultTranslation, ActivityCalendarType.getAllDescriptions()),
                    this.codeTableSpec(service, specialDaysCodeTableAttributeName, specialDaysCodeTableAttributeDefaultTranslation)
            );
        }
    },
    SPECIAL_DAY_CALENDAR_SEND_WITH_CONTRACT_AND_DATETIME(12, "Send special days calendar with contract and activation date") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, contractAttributeName, contractAttributeDefaultTranslation),
                    this.codeTableSpec(service, specialDaysCodeTableAttributeName, specialDaysCodeTableAttributeDefaultTranslation),
                    this.dateTimeSpec(service, activityCalendarActivationDateAttributeName, activityCalendarActivationDateAttributeDefaultTranslation)
            );
        }
    },
    CLEAR_AND_DISABLE_PASSIVE_TARIFF(9, "Clear and disable passive tariff") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    ACTIVATE_PASSIVE_CALENDAR(10, "Activate passive calendar") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.dateTimeSpec(service, activityCalendarActivationDateAttributeName, activityCalendarActivationDateAttributeDefaultTranslation));
        }
    },
    SPECIAL_DAY_CALENDAR_SEND_FROM_XML_USER_FILE(13, "Send special days calendar from XLM user file") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.deviceMessageFileSpec(service, XmlUserFileAttributeName, XmlUserFileAttributeDefaultTranslation));
        }
    },
    ACTIVITY_CALENDAR_SEND_WITH_DATETIME_FROM_XML_USER_FILE(14, "Send activity calendar with activation date from XML user file") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.deviceMessageFileSpec(service, XmlUserFileAttributeName, XmlUserFileAttributeDefaultTranslation),
                    this.dateTimeSpec(service, activityCalendarActivationDateAttributeName, activityCalendarActivationDateAttributeDefaultTranslation)
            );
        }
    },
    ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_DEFAULT_TARIFF_CODE(15, "Send activity calendar with activation date and default tariff code") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, activityCalendarNameAttributeName, activityCalendarNameAttributeDefaultTranslation),
                    this.deviceMessageFileSpec(service, activityCalendarCodeTableAttributeName, activityCalendarCodeTableAttributeDefaultTranslation),
                    this.dateTimeSpec(service, activityCalendarActivationDateAttributeName, activityCalendarActivationDateAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, defaultTariffCodeAttrributeName, defaultTariffCodeAttrributeDefaultTranslation, BigDecimal.ONE, BigDecimal.valueOf(2), BigDecimal.valueOf(3))
            );
        }
    };

    private final long id;
    private final String defaultNameTranslation;

    ActivityCalendarDeviceMessage(long id, String defaultNameTranslation) {
        this.id = id;
        this.defaultNameTranslation = defaultNameTranslation;
    }

    protected abstract List<PropertySpec> getPropertySpecs(PropertySpecService service);

    private PropertySpecBuilder<String> stringSpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
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

    protected PropertySpec stringSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, String... possibleValues) {
        return this.stringSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .addValues(possibleValues)
                .markExhaustive()
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

    protected PropertySpec bigDecimalSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, BigDecimal... possibleValues) {
        return this.bigDecimalSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .addValues(possibleValues)
                .markExhaustive()
                .finish();
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

    protected <T> PropertySpec referenceSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, Class<T> apiClass) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .referenceSpec(apiClass.getName())
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
    }

    protected PropertySpec deviceMessageFileSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return this.referenceSpec(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation, DeviceMessageFile.class);
    }

    protected PropertySpec codeTableSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return this.referenceSpec(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation, TariffCalender.class);
    }

    private String getNameResourceKey() {
        return ActivityCalendarDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        return new DeviceMessageSpecImpl(
                id, new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation),
                DeviceMessageCategories.ACTIVITY_CALENDAR,
                this.getPropertySpecs(propertySpecService),
                propertySpecService, nlsService);
    }

}