package com.energyict.protocolimplv2.messages;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cuo.core.UserEnvironment;
import com.energyict.mdc.messages.*;

import java.util.Arrays;
import java.util.List;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;

/**
 * Provides a summary of all <i>Clock</i> related messages
 * <p/>
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum ClockDeviceMessage implements DeviceMessageSpec {

    SET_TIME(PropertySpecFactory.datePropertySpec(meterTimeAttributeName)),

    //EIWeb messages
    SetDST(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetDSTAttributeName)),
    SetTimezone(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetTimezoneAttributeName)),
    SetTimeAdjustment(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetTimeAdjustmentAttributeName)),
    SetNTPServer(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetNTPServerAttributeName)),
    SetRefreshClockEvery(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetRefreshClockEveryAttributeName)),
    SetNTPOptions(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetNTPOptionsAttributeName));

    private static final DeviceMessageCategory clockCategory = DeviceMessageCategories.CLOCK;

    private final List<PropertySpec> deviceMessagePropertySpecs;

    private ClockDeviceMessage(PropertySpec... deviceMessagePropertySpecs) {
        this.deviceMessagePropertySpecs = Arrays.asList(deviceMessagePropertySpecs);
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return clockCategory;
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
        return ClockDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return this.deviceMessagePropertySpecs;
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
