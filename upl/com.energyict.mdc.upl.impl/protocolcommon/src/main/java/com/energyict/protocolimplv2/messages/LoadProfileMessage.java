package com.energyict.protocolimplv2.messages;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cuo.core.UserEnvironment;
import com.energyict.mdc.messages.DeviceMessageCategory;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.messages.DeviceMessageSpecPrimaryKey;
import com.energyict.protocolimplv2.messages.enums.LoadProfileMode;

import java.util.Arrays;
import java.util.List;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;

/**
 * Provides a summary of all DeviceMessages related to LoadProfiles and their configuration
 * <p/>
 * Copyrights EnergyICT
 * Date: 2/05/13
 * Time: 10:44
 */
public enum LoadProfileMessage implements DeviceMessageSpec {

    PARTIAL_LOAD_PROFILE_REQUEST(0,
            PropertySpecFactory.loadProfilePropertySpec(loadProfileAttributeName),
            PropertySpecFactory.dateTimePropertySpec(fromDateAttributeName),
            PropertySpecFactory.dateTimePropertySpec(toDateAttributeName)
    ),
    ResetActiveImportLP(1),
    ResetActiveExportLP(2),
    ResetDailyProfile(3),
    ResetMonthlyProfile(4),
    WRITE_CAPTURE_PERIOD_LP1(5, PropertySpecFactory.timeDurationPropertySpecWithSmallUnits(capturePeriodAttributeName)),
    WRITE_CAPTURE_PERIOD_LP2(6,PropertySpecFactory.timeDurationPropertySpecWithSmallUnits(capturePeriodAttributeName)),
    WriteConsumerProducerMode(7,PropertySpecFactory.stringPropertySpecWithValues(consumerProducerModeAttributeName, LoadProfileMode.getAllDescriptions())),
    LOAD_PROFILE_REGISTER_REQUEST(8,
            PropertySpecFactory.loadProfilePropertySpec(loadProfileAttributeName),
            PropertySpecFactory.dateTimePropertySpec(fromDateAttributeName)
    ),
    READ_PROFILE_DATA(9,
            PropertySpecFactory.dateTimePropertySpec(fromDateAttributeName),
            PropertySpecFactory.dateTimePropertySpec(toDateAttributeName)
    );

    private static final DeviceMessageCategory loadProfileCategory = DeviceMessageCategories.LOAD_PROFILES;

    private final List<PropertySpec> deviceMessagePropertySpecs;
    private final int id;

    private LoadProfileMessage(int id, PropertySpec... deviceMessagePropertySpecs) {
        this.id = id;
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

    @Override
    public int getMessageId() {
        return id;
    }
}
