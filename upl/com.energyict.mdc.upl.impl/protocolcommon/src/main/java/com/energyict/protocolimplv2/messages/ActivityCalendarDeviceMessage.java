package com.energyict.protocolimplv2.messages;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cuo.core.UserEnvironment;
import com.energyict.mdc.messages.DeviceMessageAttributeSpecFactoryImpl;
import com.energyict.mdc.messages.DeviceMessageCategory;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.messages.DeviceMessageSpecPrimaryKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 7/02/13
 * Time: 12:01
 */
public enum ActivityCalendarDeviceMessage implements DeviceMessageSpec {

    ACTIVITY_CALENDER_SEND(PropertySpecFactory.codeTableReferencePropertySpec("ActivityCalendarDeviceMessage.activitycalendar.codetable")),
    ACTIVITY_CALENDER_SEND_WITH_DATE(PropertySpecFactory.codeTableReferencePropertySpec("ActivityCalendarDeviceMessage.activitycalendar.codetable"),
            PropertySpecFactory.dateTimePropertySpec("ActivityCalendarDeviceMessage.activitycalendar.activationdate"));

    private static final DeviceMessageCategory activityCalendarCategory = StandardDeviceMessageCategory.ACTIVITY_CALENDAR;

    private List<PropertySpec> deviceMessagePropertySpecs;

    private ActivityCalendarDeviceMessage(PropertySpec... deviceMessagePropertySpecs) {
        this.deviceMessagePropertySpecs = new ArrayList<>(deviceMessagePropertySpecs.length);
        for (PropertySpec deviceMessagePropertySpec : deviceMessagePropertySpecs) {
            this.deviceMessagePropertySpecs.add(DeviceMessageAttributeSpecFactoryImpl.createFor(this, deviceMessagePropertySpec));
        }
        this.deviceMessagePropertySpecs = Arrays.asList(deviceMessagePropertySpecs);
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return activityCalendarCategory;
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
        return ActivityCalendarDeviceMessage.class.getSimpleName() + "." + this.toString();
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
