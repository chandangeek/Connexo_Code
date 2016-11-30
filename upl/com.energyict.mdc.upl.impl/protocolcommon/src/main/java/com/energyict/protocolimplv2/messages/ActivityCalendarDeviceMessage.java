package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.messages.DeviceMessageCategory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.DeviceMessageSpecPrimaryKey;
import com.energyict.mdc.upl.properties.DeviceMessageFile;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.TariffCalender;

import com.energyict.protocolimplv2.messages.enums.ActivityCalendarType;
import com.energyict.protocolimplv2.messages.nls.Thesaurus;
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
public enum ActivityCalendarDeviceMessage implements DeviceMessageSpec {

    ACTIVITY_CALENDAR_READ(0) {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.emptyList();
        }
    },
    WRITE_CONTRACTS_FROM_XML_USERFILE(1) {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.singletonList(this.deviceMessageFileSpec(contractsXmlUserFileAttributeName, contractsXmlUserFileAttributeDefaultTranslation));
        }
    },
    ACTIVITY_CALENDER_SEND(2) {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Arrays.asList(
                    this.stringSpec(activityCalendarNameAttributeName, activityCalendarNameAttributeDefaultTranslation),
                    this.codeTableSpec(activityCalendarCodeTableAttributeName, activityCalendarCodeTableAttributeDefaultTranslation)
            );
        }
    },
    ACTIVITY_CALENDER_SEND_WITH_DATETIME(3) {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Arrays.asList(
                    this.stringSpec(activityCalendarNameAttributeName, activityCalendarNameAttributeDefaultTranslation),
                    this.codeTableSpec(activityCalendarCodeTableAttributeName, activityCalendarCodeTableAttributeDefaultTranslation),
                    this.dateTimeSpec(activityCalendarActivationDateAttributeName, activityCalendarActivationDateAttributeDefaultTranslation)
            );
        }
    },
    ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_TYPE(4) {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Arrays.asList(
                    this.stringSpec(activityCalendarTypeAttributeName, activityCalendarTypeAttributeDefaultTranslation, ActivityCalendarType.getAllDescriptions()),
                    this.stringSpec(activityCalendarNameAttributeName, activityCalendarNameAttributeDefaultTranslation),
                    this.codeTableSpec(activityCalendarCodeTableAttributeName, activityCalendarCodeTableAttributeDefaultTranslation),
                    this.dateTimeSpec(activityCalendarActivationDateAttributeName, activityCalendarActivationDateAttributeDefaultTranslation)
            );
        }
    },
    ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_CONTRACT(5) {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Arrays.asList(
                    this.bigDecimalSpec(contractAttributeName, contractAttributeDefaultTranslation),
                    this.stringSpec(activityCalendarNameAttributeName, activityCalendarNameAttributeDefaultTranslation),
                    this.codeTableSpec(activityCalendarCodeTableAttributeName, activityCalendarCodeTableAttributeDefaultTranslation),
                    this.dateTimeSpec(activityCalendarActivationDateAttributeName, activityCalendarActivationDateAttributeDefaultTranslation)
            );
        }
    },
    ACTIVITY_CALENDER_SEND_WITH_DATE(6) {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Arrays.asList(
                    this.codeTableSpec(activityCalendarCodeTableAttributeName, activityCalendarCodeTableAttributeDefaultTranslation),
                    this.dateTimeSpec(activityCalendarActivationDateAttributeName, activityCalendarActivationDateAttributeDefaultTranslation)
            );
        }
    },
    SPECIAL_DAY_CALENDAR_SEND(7) {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.singletonList(this.codeTableSpec(specialDaysCodeTableAttributeName, specialDaysCodeTableAttributeDefaultTranslation));
        }
    },
    SPECIAL_DAY_CALENDAR_SEND_WITH_TYPE(8) {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Arrays.asList(
                    this.stringSpec(activityCalendarTypeAttributeName, activityCalendarTypeAttributeDefaultTranslation, ActivityCalendarType.getAllDescriptions()),
                    this.codeTableSpec(specialDaysCodeTableAttributeName, specialDaysCodeTableAttributeDefaultTranslation)
            );
        }
    },
    SPECIAL_DAY_CALENDAR_SEND_WITH_CONTRACT_AND_DATETIME(9) {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Arrays.asList(
                    this.bigDecimalSpec(contractAttributeName, contractAttributeDefaultTranslation),
                    this.codeTableSpec(specialDaysCodeTableAttributeName, specialDaysCodeTableAttributeDefaultTranslation),
                    this.dateTimeSpec(activityCalendarActivationDateAttributeName, activityCalendarActivationDateAttributeDefaultTranslation)
            );
        }
    },
    CLEAR_AND_DISABLE_PASSIVE_TARIFF(10) {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.emptyList();
        }
    },
    ACTIVATE_PASSIVE_CALENDAR(11) {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.singletonList(this.dateTimeSpec(activityCalendarActivationDateAttributeName, activityCalendarActivationDateAttributeDefaultTranslation));
        }
    },
    SPECIAL_DAY_CALENDAR_SEND_FROM_XML_USER_FILE(12) {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.singletonList(this.deviceMessageFileSpec(XmlUserFileAttributeName, XmlUserFileAttributeDefaultTranslation));
        }
    },
    ACTIVITY_CALENDAR_SEND_WITH_DATETIME_FROM_XML_USER_FILE(13) {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Arrays.asList(
                    this.deviceMessageFileSpec(XmlUserFileAttributeName, XmlUserFileAttributeDefaultTranslation),
                    this.dateTimeSpec(activityCalendarActivationDateAttributeName, activityCalendarActivationDateAttributeDefaultTranslation)
            );
        }
    },
    ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_DEFAULT_TARIFF_CODE(14) {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Arrays.asList(
                    this.stringSpec(activityCalendarNameAttributeName, activityCalendarNameAttributeDefaultTranslation),
                    this.deviceMessageFileSpec(activityCalendarCodeTableAttributeName, activityCalendarCodeTableAttributeDefaultTranslation),
                    this.dateTimeSpec(activityCalendarActivationDateAttributeName, activityCalendarActivationDateAttributeDefaultTranslation),
                    this.bigDecimalSpec(defaultTariffCodeAttrributeName, defaultTariffCodeAttrributeDefaultTranslation, BigDecimal.ONE, BigDecimal.valueOf(2), BigDecimal.valueOf(3))
            );
        }
    },
    ;

    private final long id;

    ActivityCalendarDeviceMessage(int id) {
        this.id = id;
    }

    private PropertySpecBuilder<String> stringSpecBuilder(String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return Services
                .propertySpecService()
                .stringSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description());
    }

    protected PropertySpec stringSpec(String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return this.stringSpecBuilder(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation).finish();
    }

    protected PropertySpec stringSpec(String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, String... possibleValues) {
        return this.stringSpecBuilder(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .addValues(possibleValues)
                .markExhaustive()
                .finish();
    }

    private PropertySpecBuilder<BigDecimal> bigDecimalSpecBuilder(String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return Services
                .propertySpecService()
                .bigDecimalSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description());
    }

    protected PropertySpec bigDecimalSpec(String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return this.bigDecimalSpecBuilder(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation).finish();
    }

    protected PropertySpec bigDecimalSpec(String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, BigDecimal... possibleValues) {
        return this.bigDecimalSpecBuilder(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .addValues(possibleValues)
                .markExhaustive()
                .finish();
    }

    protected PropertySpec dateTimeSpec(String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return Services
                .propertySpecService()
                .dateTimeSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .finish();
    }

    protected PropertySpec deviceMessageFileSpec(String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return Services
                .propertySpecService()
                .referenceSpec(DeviceMessageFile.class)
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .finish();
    }

    protected PropertySpec codeTableSpec(String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return Services
                .propertySpecService()
                .referenceSpec(TariffCalender.class)
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .finish();
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return DeviceMessageCategories.ACTIVITY_CALENDAR;
    }

    @Override
    public String getName() {
        return Services
                .nlsService()
                .getThesaurus(Thesaurus.ID.toString())
                .getFormat(this.getNameTranslationKey())
                .format();
    }

    @Override
    public String getNameResourceKey() {
        return ActivityCalendarDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    private TranslationKeyImpl getNameTranslationKey() {
        return new TranslationKeyImpl(this.getNameResourceKey(), "MR" + this.getNameResourceKey());
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        for (PropertySpec securityProperty : getPropertySpecs()) {
            if (securityProperty.getName().equals(name)) {
                return securityProperty;
            }
        }
        return null;
    }

    @Override
    public DeviceMessageSpecPrimaryKey getPrimaryKey() {
        return new DeviceMessageSpecPrimaryKey(this, name());
    }

    @Override
    public long getMessageId() {
        return id;
    }

}