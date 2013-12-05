package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.protocol.dynamic.PropertySpec;
import com.energyict.mdc.common.UserEnvironment;
import com.energyict.mdc.protocol.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.device.messages.DeviceMessageSpecPrimaryKey;
import com.energyict.mdc.protocol.dynamic.RequiredPropertySpecFactory;
import com.energyict.protocolimplv2.messages.enums.LoadProfileMode;

import java.util.Arrays;
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

    PARTIAL_LOAD_PROFILE_REQUEST(
            RequiredPropertySpecFactory.newInstance().loadProfilePropertySpec(loadProfileAttributeName),
            RequiredPropertySpecFactory.newInstance().dateTimePropertySpec(fromDateAttributeName),
            RequiredPropertySpecFactory.newInstance().dateTimePropertySpec(toDateAttributeName)
    ),
    ResetActiveImportLP(),
    ResetActiveExportLP(),
    ResetDailyProfile(),
    ResetMonthlyProfile(),
    WRITE_CAPTURE_PERIOD_LP1(RequiredPropertySpecFactory.newInstance().timeDurationPropertySpec(capturePeriodAttributeName)),
    WRITE_CAPTURE_PERIOD_LP2(RequiredPropertySpecFactory.newInstance().timeDurationPropertySpec(capturePeriodAttributeName)),
    WriteConsumerProducerMode(RequiredPropertySpecFactory.newInstance().stringPropertySpecWithValues(consumerProducerModeAttributeName, LoadProfileMode.getAllDescriptions())),
    LOAD_PROFILE_REGISTER_REQUEST(
            RequiredPropertySpecFactory.newInstance().loadProfilePropertySpec(loadProfileAttributeName),
            RequiredPropertySpecFactory.newInstance().dateTimePropertySpec(fromDateAttributeName)
    );

    private static final DeviceMessageCategory loadProfileCategory = DeviceMessageCategories.LOAD_PROFILES;

    private final List<PropertySpec> deviceMessagePropertySpecs;

    private LoadProfileMessage(PropertySpec... deviceMessagePropertySpecs) {
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
        return LoadProfileMessage.class.getSimpleName() + "." + this.toString();
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
