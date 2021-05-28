package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.ProtocolSupportedCalendarOptions;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.DeviceMessageFile;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TariffCalendar;
import com.energyict.protocolimplv2.messages.enums.ActivityCalendarType;
import com.energyict.protocolimplv2.messages.nls.TranslationKeyImpl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.XmlUserFileAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.XmlUserFileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarActivationDateAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarCodeTableAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarCodeTableAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarNameAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarNameAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarObiscodeAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarObiscodeAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarTypeAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarTypeAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contractAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contractAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contractsXmlUserFileAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contractsXmlUserFileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.dayProfileXmlUserFileAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.dayProfileXmlUserFileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.defaultTariffCodeAttrributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.defaultTariffCodeAttrributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.fullActivityCalendarAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.fullActivityCalendarCodeTableAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.seasonProfileXmlUserFileAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.seasonProfileXmlUserFileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.specialDaysAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.specialDaysCodeTableAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.specialDaysTableObiscodeAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.specialDaysTableObiscodeAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.specialDaysXmlUserFileAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.specialDaysXmlUserFileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.weekProfileXmlUserFileAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.weekProfileXmlUserFileAttributeName;

/**
 * Provides a summary of all <i>ActivityCalendar</i> related messages.
 * <p>
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

        @Override
        public Optional<ProtocolSupportedCalendarOptions> getProtocolSupportedCalendarOption() {
            return Optional.of(ProtocolSupportedCalendarOptions.VERIFY_ACTIVE_CALENDAR);
        }
    },
    WRITE_CONTRACTS_FROM_XML_USERFILE(2, "Write contracts from XML file") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.deviceMessageFileSpec(service, contractsXmlUserFileAttributeName, contractsXmlUserFileAttributeDefaultTranslation));
        }

        @Override
        public Optional<ProtocolSupportedCalendarOptions> getProtocolSupportedCalendarOption() {
            return Optional.of(ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR);
        }
    },
    ACTIVITY_CALENDER_SEND(3, "Send activity calendar") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, activityCalendarNameAttributeName, activityCalendarNameAttributeDefaultTranslation),
                    this.codeTableSpec(service, activityCalendarAttributeName, activityCalendarCodeTableAttributeDefaultTranslation)
            );
        }

        @Override
        public Optional<ProtocolSupportedCalendarOptions> getProtocolSupportedCalendarOption() {
            return Optional.of(ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR);
        }
    },
    ACTIVITY_CALENDER_SEND_WITH_DATETIME(4, "Send activity calendar with activation date") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, activityCalendarNameAttributeName, activityCalendarNameAttributeDefaultTranslation),
                    this.codeTableSpec(service, activityCalendarAttributeName, activityCalendarCodeTableAttributeDefaultTranslation),
                    this.dateTimeSpec(service, activityCalendarActivationDateAttributeName, activityCalendarActivationDateAttributeDefaultTranslation)
            );
        }

        @Override
        public Optional<ProtocolSupportedCalendarOptions> getProtocolSupportedCalendarOption() {
            return Optional.of(ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR_WITH_DATETIME);
        }
    },
    ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_TYPE(5, "Send activity calendar with activation date and type") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, activityCalendarTypeAttributeName, activityCalendarTypeAttributeDefaultTranslation, ActivityCalendarType.getAllDescriptions()),
                    this.stringSpec(service, activityCalendarNameAttributeName, activityCalendarNameAttributeDefaultTranslation),
                    this.codeTableSpec(service, activityCalendarAttributeName, activityCalendarCodeTableAttributeDefaultTranslation),
                    this.dateTimeSpec(service, activityCalendarActivationDateAttributeName, activityCalendarActivationDateAttributeDefaultTranslation)
            );
        }

        @Override
        public Optional<ProtocolSupportedCalendarOptions> getProtocolSupportedCalendarOption() {
            return Optional.of(ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR_WITH_DATE_AND_TYPE);
        }
    },
    ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_CONTRACT(11, "Send activity calendar with activation date and contract") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, contractAttributeName, contractAttributeDefaultTranslation),
                    this.stringSpec(service, activityCalendarNameAttributeName, activityCalendarNameAttributeDefaultTranslation),
                    this.codeTableSpec(service, activityCalendarAttributeName, activityCalendarCodeTableAttributeDefaultTranslation),
                    this.dateTimeSpec(service, activityCalendarActivationDateAttributeName, activityCalendarActivationDateAttributeDefaultTranslation)
            );
        }

        @Override
        public Optional<ProtocolSupportedCalendarOptions> getProtocolSupportedCalendarOption() {
            return Optional.of(ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR_WITH_DATE_AND_CONTRACT);
        }
    },
    ACTIVITY_CALENDER_SEND_WITH_DATE(6, "Send activity calendar with activation date") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.codeTableSpec(service, activityCalendarAttributeName, activityCalendarCodeTableAttributeDefaultTranslation),
                    this.dateTimeSpec(service, activityCalendarActivationDateAttributeName, activityCalendarActivationDateAttributeDefaultTranslation)
            );
        }

        @Override
        public Optional<ProtocolSupportedCalendarOptions> getProtocolSupportedCalendarOption() {
            return Optional.of(ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR_WITH_DATE);
        }
    },
    SPECIAL_DAY_CALENDAR_SEND(7, "Send special days calendar") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.codeTableSpec(service, specialDaysAttributeName, specialDaysCodeTableAttributeDefaultTranslation));
        }

        @Override
        public Optional<ProtocolSupportedCalendarOptions> getProtocolSupportedCalendarOption() {
            return Optional.of(ProtocolSupportedCalendarOptions.SEND_SPECIAL_DAYS_CALENDAR);
        }
    },
    SPECIAL_DAY_CALENDAR_SEND_WITH_TYPE(8, "Send special days calendar with type") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, activityCalendarTypeAttributeName, activityCalendarTypeAttributeDefaultTranslation, ActivityCalendarType.getAllDescriptions()),
                    this.codeTableSpec(service, specialDaysAttributeName, specialDaysCodeTableAttributeDefaultTranslation)
            );
        }

        @Override
        public Optional<ProtocolSupportedCalendarOptions> getProtocolSupportedCalendarOption() {
            return Optional.of(ProtocolSupportedCalendarOptions.SEND_SPECIAL_DAYS_CALENDAR_WITH_TYPE);
        }
    },
    SPECIAL_DAY_CALENDAR_SEND_WITH_CONTRACT_AND_DATETIME(12, "Send special days calendar with contract and activation date") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, contractAttributeName, contractAttributeDefaultTranslation),
                    this.codeTableSpec(service, specialDaysAttributeName, specialDaysCodeTableAttributeDefaultTranslation),
                    this.dateTimeSpec(service, activityCalendarActivationDateAttributeName, activityCalendarActivationDateAttributeDefaultTranslation)
            );
        }

        @Override
        public Optional<ProtocolSupportedCalendarOptions> getProtocolSupportedCalendarOption() {
            return Optional.of(ProtocolSupportedCalendarOptions.SEND_SPECIAL_DAYS_CALENDAR_WITH_CONTRACT_AND_DATE);
        }
    },
    CLEAR_AND_DISABLE_PASSIVE_TARIFF(9, "Clear and disable passive tariff") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }

        @Override
        public Optional<ProtocolSupportedCalendarOptions> getProtocolSupportedCalendarOption() {
            return Optional.of(ProtocolSupportedCalendarOptions.CLEAR_AND_DISABLE_PASSIVE_TARIFF);
        }
    },
    ACTIVATE_PASSIVE_CALENDAR(10, "Activate passive calendar") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.dateTimeSpec(service, activityCalendarActivationDateAttributeName, activityCalendarActivationDateAttributeDefaultTranslation));
        }

        @Override
        public Optional<ProtocolSupportedCalendarOptions> getProtocolSupportedCalendarOption() {
            return Optional.of(ProtocolSupportedCalendarOptions.ACTIVATE_PASSIVE_CALENDAR);
        }
    },
    SPECIAL_DAY_CALENDAR_SEND_FROM_XML_USER_FILE(13, "Send special days calendar from XML file") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.deviceMessageFileSpec(service, XmlUserFileAttributeName, XmlUserFileAttributeDefaultTranslation));
        }

        @Override
        public Optional<ProtocolSupportedCalendarOptions> getProtocolSupportedCalendarOption() {
            return Optional.of(ProtocolSupportedCalendarOptions.SEND_SPECIAL_DAYS_CALENDAR);
        }
    },
    ACTIVITY_CALENDAR_SEND_WITH_DATETIME_FROM_XML_USER_FILE(14, "Send activity calendar with activation date from XML file") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.deviceMessageFileSpec(service, XmlUserFileAttributeName, XmlUserFileAttributeDefaultTranslation),
                    this.dateTimeSpec(service, activityCalendarActivationDateAttributeName, activityCalendarActivationDateAttributeDefaultTranslation)
            );
        }

        @Override
        public Optional<ProtocolSupportedCalendarOptions> getProtocolSupportedCalendarOption() {
            return Optional.of(ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR_WITH_DATE);
        }
    },
    ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_DEFAULT_TARIFF_CODE(15, "Send activity calendar with activation date and default tariff code") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, activityCalendarNameAttributeName, activityCalendarNameAttributeDefaultTranslation),
                    this.deviceMessageFileSpec(service, activityCalendarAttributeName, activityCalendarCodeTableAttributeDefaultTranslation),
                    this.dateTimeSpec(service, activityCalendarActivationDateAttributeName, activityCalendarActivationDateAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, defaultTariffCodeAttrributeName, defaultTariffCodeAttrributeDefaultTranslation, BigDecimal.ONE, BigDecimal.valueOf(2), BigDecimal.valueOf(3))
            );
        }

        @Override
        public Optional<ProtocolSupportedCalendarOptions> getProtocolSupportedCalendarOption() {
            return Optional.of(ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR_WITH_DATE_AND_CONTRACT);
        }
    },
    ACTIVITY_CALENDAR_WITH_DATETIME_FROM_XML(16, "Send activity calendar from XML, with activation date") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, activityCalendarNameAttributeName, activityCalendarNameAttributeDefaultTranslation),
                    this.obisCodeSpecBuilder(service, activityCalendarObiscodeAttributeName, activityCalendarObiscodeAttributeDefaultTranslation).finish(),
                    this.deviceMessageFileSpec(service, dayProfileXmlUserFileAttributeName, dayProfileXmlUserFileAttributeDefaultTranslation),
                    this.deviceMessageFileSpec(service, weekProfileXmlUserFileAttributeName, weekProfileXmlUserFileAttributeDefaultTranslation),
                    this.deviceMessageFileSpec(service, seasonProfileXmlUserFileAttributeName, seasonProfileXmlUserFileAttributeDefaultTranslation),
                    this.dateTimeSpec(service, activityCalendarActivationDateAttributeName, activityCalendarActivationDateAttributeDefaultTranslation)
            );
        }

        @Override
        public Optional<ProtocolSupportedCalendarOptions> getProtocolSupportedCalendarOption() {
            return Optional.of(ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR_WITH_DATE);
        }
    },
    SPECIAL_DAY_CALENDAR_WITH_GIVEN_TABLE_OBIS_FROM_XML(17, "Send special days from XML, with obiscode ") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, specialDaysTableObiscodeAttributeName, specialDaysTableObiscodeAttributeDefaultTranslation),
                    this.deviceMessageFileSpec(service, specialDaysXmlUserFileAttributeName, specialDaysXmlUserFileAttributeDefaultTranslation)
            );
        }

        @Override
        public Optional<ProtocolSupportedCalendarOptions> getProtocolSupportedCalendarOption() {
            return Optional.of(ProtocolSupportedCalendarOptions.SEND_SPECIAL_DAYS_CALENDAR);
        }
    },
    SELECTION_OF_12_LINES_IN_TOU_TABLE(18, "Send 12 lines from TOU table") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, activityCalendarNameAttributeName, activityCalendarNameAttributeDefaultTranslation),
                    this.codeTableSpec(service, activityCalendarCodeTableAttributeName, activityCalendarCodeTableAttributeDefaultTranslation)
            );
        }

        @Override
        public Optional<ProtocolSupportedCalendarOptions> getProtocolSupportedCalendarOption() {
            return Optional.of(ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR);
        }
    },
    // Support for 'Full' Calendar = Combining Activity calendar with special days calendar
    ACTIVITY_CALENDER_FULL_CALENDAR_SEND(19, "Send full calendar") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, activityCalendarNameAttributeName, activityCalendarNameAttributeDefaultTranslation),
                    this.codeTableSpec(service, fullActivityCalendarAttributeName, fullActivityCalendarCodeTableAttributeDefaultTranslation)
            );
        }

        @Override
        public Optional<ProtocolSupportedCalendarOptions> getProtocolSupportedCalendarOption() {
            return Optional.of(ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR);
        }

    },
    ACTIVITY_CALENDER_FULL_CALENDAR_WITH_DATETIME(20, "Send full calendar with activation date") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, activityCalendarNameAttributeName, activityCalendarNameAttributeDefaultTranslation),
                    this.codeTableSpec(service, fullActivityCalendarAttributeName, fullActivityCalendarCodeTableAttributeDefaultTranslation),
                    this.dateTimeSpec(service, activityCalendarActivationDateAttributeName, activityCalendarActivationDateAttributeDefaultTranslation)
            );
        }

        @Override
        public Optional<ProtocolSupportedCalendarOptions> getProtocolSupportedCalendarOption() {
            return Optional.of(ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR_WITH_DATETIME);
        }
    },
    ACTIVITY_CALENDER_FULL_CALENDAR_WITH_DATETIME_AND_TYPE(21, "Send full calendar with activation date and type") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, activityCalendarTypeAttributeName, activityCalendarTypeAttributeDefaultTranslation, ActivityCalendarType.getAllDescriptions()),
                    this.stringSpec(service, activityCalendarNameAttributeName, activityCalendarNameAttributeDefaultTranslation),
                    this.codeTableSpec(service, fullActivityCalendarAttributeName, fullActivityCalendarCodeTableAttributeDefaultTranslation),
                    this.dateTimeSpec(service, activityCalendarActivationDateAttributeName, activityCalendarActivationDateAttributeDefaultTranslation)
            );
        }

        @Override
        public Optional<ProtocolSupportedCalendarOptions> getProtocolSupportedCalendarOption() {
            return Optional.of(ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR_WITH_DATE_AND_TYPE);
        }
    },
    ACTIVITY_CALENDER_FULL_CALENDAR_WITH_DATE(22, "Send full calendar with activation date") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.codeTableSpec(service, fullActivityCalendarAttributeName, fullActivityCalendarCodeTableAttributeDefaultTranslation),
                    this.dateTimeSpec(service, activityCalendarActivationDateAttributeName, activityCalendarActivationDateAttributeDefaultTranslation)
            );
        }

        @Override
        public Optional<ProtocolSupportedCalendarOptions> getProtocolSupportedCalendarOption() {
            return Optional.of(ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR_WITH_DATE);
        }
    },
    ACTIVITY_CALENDER_FULL_CALENDAR_WITH_DATETIME_AND_CONTRACT(23, "Send full calendar with activation date and contract") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, contractAttributeName, contractAttributeDefaultTranslation),
                    this.stringSpec(service, activityCalendarNameAttributeName, activityCalendarNameAttributeDefaultTranslation),
                    this.codeTableSpec(service, fullActivityCalendarAttributeName, fullActivityCalendarCodeTableAttributeDefaultTranslation),
                    this.dateTimeSpec(service, activityCalendarActivationDateAttributeName, activityCalendarActivationDateAttributeDefaultTranslation)
            );
        }

        @Override
        public Optional<ProtocolSupportedCalendarOptions> getProtocolSupportedCalendarOption() {
            return Optional.of(ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR_WITH_DATE_AND_CONTRACT);
        }
    };

    private final long id;
    private final String defaultNameTranslation;

    ActivityCalendarDeviceMessage(long id, String defaultNameTranslation) {
        this.id = id;
        this.defaultNameTranslation = defaultNameTranslation;
    }

    @Override
    public long id() {
        return this.id;
    }

    protected abstract List<PropertySpec> getPropertySpecs(PropertySpecService service);

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
        return this.referenceSpec(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation, TariffCalendar.class);
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
                propertySpecService, nlsService, converter);
    }

    public abstract Optional<ProtocolSupportedCalendarOptions> getProtocolSupportedCalendarOption();

}