package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.dynamic.DateAndTimeFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.TimeDurationValueFactory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecPrimaryKey;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.protocolimplv2.messages.enums.LoadProfileMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.capturePeriodAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.consumerProducerModeAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.fromDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.loadProfileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.toDateAttributeName;

/**
 * Provides a summary of all DeviceMessages related to LoadProfiles and their configuration
 * <p/>
 * Copyrights EnergyICT
 * Date: 2/05/13
 * Time: 10:44
 */
public enum LoadProfileMessage implements DeviceMessageSpec {

    PARTIAL_LOAD_PROFILE_REQUEST {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            PropertySpecService propertySpecService = PropertySpecService.INSTANCE.get();
            return Arrays.asList(
                    propertySpecService.referencePropertySpec(loadProfileAttributeName, true, FactoryIds.LOADPROFILE),
                    propertySpecService.basicPropertySpec(fromDateAttributeName, true, new DateAndTimeFactory()),
                    propertySpecService.basicPropertySpec(toDateAttributeName, true, new DateAndTimeFactory()));
        }
    },
    ResetActiveImportLP() {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.emptyList();
        }
    },
    ResetActiveExportLP() {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.emptyList();
        }
    },
    ResetDailyProfile() {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.emptyList();
        }
    },
    ResetMonthlyProfile() {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.emptyList();
        }
    },
    WRITE_CAPTURE_PERIOD_LP1 {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            PropertySpecService propertySpecService = PropertySpecService.INSTANCE.get();
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(propertySpecService.basicPropertySpec(capturePeriodAttributeName, true, new TimeDurationValueFactory()));
            return propertySpecs;
        }
    },
    WRITE_CAPTURE_PERIOD_LP2 {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            PropertySpecService propertySpecService = PropertySpecService.INSTANCE.get();
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(propertySpecService.basicPropertySpec(capturePeriodAttributeName, true, new TimeDurationValueFactory()));
            return propertySpecs;
        }
    },
    WriteConsumerProducerMode {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            PropertySpecService propertySpecService = PropertySpecService.INSTANCE.get();
            List<PropertySpec> propertySpecs = new ArrayList<>();
            propertySpecs.add(propertySpecService.stringPropertySpecWithValues(consumerProducerModeAttributeName, true, LoadProfileMode.getAllDescriptions()));
            return propertySpecs;
        }
    },
    LOAD_PROFILE_REGISTER_REQUEST {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            PropertySpecService propertySpecService = PropertySpecService.INSTANCE.get();
            return Arrays.asList(
                    propertySpecService.referencePropertySpec(loadProfileAttributeName, true, FactoryIds.LOADPROFILE),
                    propertySpecService.basicPropertySpec(fromDateAttributeName, true, new DateAndTimeFactory()));
        }
    };

    private static final DeviceMessageCategory loadProfileCategory = DeviceMessageCategories.LOAD_PROFILES;

    @Override
    public DeviceMessageCategory getCategory() {
        return loadProfileCategory;
    }

    @Override
    public String getName() {
        return this.getNameResourceKey();
    }

    /**
     * Gets the resource key that determines the name
     * of this category to the user's language settings.
     *
     * @return The resource key
     */
    private String getNameResourceKey() {
        return LoadProfileMessage.class.getSimpleName() + "." + this.toString();
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
}
