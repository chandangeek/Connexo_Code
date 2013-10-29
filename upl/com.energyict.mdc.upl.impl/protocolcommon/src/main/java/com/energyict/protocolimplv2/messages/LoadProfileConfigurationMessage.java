package com.energyict.protocolimplv2.messages;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cuo.core.UserEnvironment;
import com.energyict.mdc.messages.*;
import com.energyict.protocolimplv2.messages.enums.LoadProfileMode;

import java.util.Arrays;
import java.util.List;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;

/**
 * Provides a summary of all DeviceMessages related to configuration of LoadProfiles
 * <p/>
 * Copyrights EnergyICT
 * Date: 2/05/13
 * Time: 10:44
 */
public enum LoadProfileConfigurationMessage implements DeviceMessageSpec {

    WRITE_CAPTURE_PERIOD_LP1(PropertySpecFactory.timeDurationPropertySpecWithSmallUnits(capturePeriodAttributeName)),
    WRITE_CAPTURE_PERIOD_LP2(PropertySpecFactory.timeDurationPropertySpecWithSmallUnits(capturePeriodAttributeName)),
    WriteConsumerProducerMode(PropertySpecFactory.stringPropertySpecWithValues(consumerProducerModeAttributeName, LoadProfileMode.getAllDescriptions()));

    private static final DeviceMessageCategory loadProfileCategory = DeviceMessageCategories.LOAD_PROFILE_CONFIGURATION;

    private final List<PropertySpec> deviceMessagePropertySpecs;

    private LoadProfileConfigurationMessage(PropertySpec... deviceMessagePropertySpecs) {
        this.deviceMessagePropertySpecs = Arrays.asList(deviceMessagePropertySpecs);
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return loadProfileCategory;
    }


    @Override
    public String getName() {
        return UserEnvironment.getDefault().getTranslation(this.getNameResourceKey());
    }

    /**
     * Gets the resource key that determines the name
     * of this category to the user's language settings.
     *
     * @return The resource key
     */
    private String getNameResourceKey() {
        return LoadProfileConfigurationMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return deviceMessagePropertySpecs;
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
