package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.messages.DeviceMessageCategory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.DeviceMessageSpecPrimaryKey;
import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.protocolimplv2.messages.nls.Thesaurus;
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
public enum LogBookDeviceMessage implements DeviceMessageSpec {

    SetInputChannel(0, "Set input channel") {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.singletonList(this.stringSpec(DeviceMessageConstants.SetInputChannelAttributeName, DeviceMessageConstants.SetInputChannelAttributeDefaultTranslation));
        }
    },
    SetCondition(1, "Set condition") {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.singletonList(this.stringSpec(DeviceMessageConstants.SetConditionAttributeName, DeviceMessageConstants.SetConditionAttributeDefaultTranslation));
        }
    },
    SetConditionValue(2, "Set condition value") {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.singletonList(this.stringSpec(DeviceMessageConstants.SetConditionValueAttributeName, DeviceMessageConstants.SetConditionValueAttributeDefaultTranslation));
        }
    },
    SetTimeTrue(3, "Set time true") {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.singletonList(this.stringSpec(DeviceMessageConstants.SetTimeTrueAttributeName, DeviceMessageConstants.SetTimeTrueAttributeDefaultTranslation));
        }
    },
    SetTimeFalse(4, "Set time false") {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.singletonList(this.stringSpec(DeviceMessageConstants.SetTimeFalseAttributeName, DeviceMessageConstants.SetTimeFalseAttributeDefaultTranslation));
        }
    },
    SetOutputChannel(5, "Set output channel") {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.singletonList(this.stringSpec(DeviceMessageConstants.SetOutputChannelAttributeName, DeviceMessageConstants.SetOutputChannelAttributeDefaultTranslation));
        }
    },
    SetAlarm(6, "Set alarm") {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.singletonList(this.stringSpec(DeviceMessageConstants.SetAlarmAttributeName, DeviceMessageConstants.SetAlarmAttributeDefaultTranslation));
        }
    },
    SetTag(7, "Set tag") {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.singletonList(this.stringSpec(DeviceMessageConstants.SetTagAttributeName, DeviceMessageConstants.SetTagAttributeDefaultTranslation));
        }
    },
    SetInverse(8, "Set inverse") {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.singletonList(this.stringSpec(DeviceMessageConstants.SetInverseAttributeName, DeviceMessageConstants.SetInverseAttributeDefaultTranslation));
        }
    },
    SetImmediate(9, "Set immediate") {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.singletonList(this.stringSpec(DeviceMessageConstants.SetImmediateAttributeName, DeviceMessageConstants.SetImmediateAttributeDefaultTranslation));
        }
    },
    ReadDebugLogBook(10, "Read debug logbook") {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Arrays.asList(
                    this.dateTimeSpec(DeviceMessageConstants.fromDateAttributeName, DeviceMessageConstants.fromDateAttributeNameDefaultTranslation),
                    this.dateTimeSpec(DeviceMessageConstants.toDateAttributeName, DeviceMessageConstants.toDateAttributeNameDefaultTranslation)
            );
        }
    },
    ReadManufacturerSpecificLogBook(11, "Read manufacturer specific logbook") {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Arrays.asList(
                    this.dateTimeSpec(DeviceMessageConstants.fromDateAttributeName, DeviceMessageConstants.fromDateAttributeNameDefaultTranslation),
                    this.dateTimeSpec(DeviceMessageConstants.toDateAttributeName, DeviceMessageConstants.toDateAttributeNameDefaultTranslation)
            );
        }
    },
    ResetMainLogbook(12, "Reset main logbook") {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.emptyList();
        }
    },
    ResetCoverLogbook(13, "Reset cover logbook") {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.emptyList();
        }
    },
    ResetBreakerLogbook(14, "Reset breaker logbook") {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.emptyList();
        }
    },
    ResetCommunicationLogbook(15, "Reset communication logbook") {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.emptyList();
        }
    },
    ResetLQILogbook(16, "Reset LQI logbook") {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.emptyList();
        }
    },
    ResetVoltageCutLogbook(17, "Reset voltage cut logbook") {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.emptyList();
        }
    },
    ReadLogBook(18, "Read logbook") {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.emptyList();
        }
    },
    ResetSecurityLogbook(19, "Reset security logbook") {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.emptyList();
        }
    };

    private final long id;
    private final String defaultNameTranslation;

    LogBookDeviceMessage(long id, String defaultNameTranslation) {
        this.id = id;
        this.defaultNameTranslation = defaultNameTranslation;
    }

    protected PropertySpec stringSpec(String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return Services
                .propertySpecService()
                .stringSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
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

    @Override
    public DeviceMessageCategory getCategory() {
        return DeviceMessageCategories.LOG_BOOKS;
    }

    @Override
    public String getName() {
        return Services
                .nlsService()
                .getThesaurus(Thesaurus.ID.toString())
                .getFormat(this.getNameTranslationKey())
                .format();
    }

    private String getNameResourceKey() {
        return LogBookDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public TranslationKeyImpl getNameTranslationKey() {
        return new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation);
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