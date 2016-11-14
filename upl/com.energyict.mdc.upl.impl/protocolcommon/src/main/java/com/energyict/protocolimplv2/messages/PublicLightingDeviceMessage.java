package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageCategory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.DeviceMessageSpecPrimaryKey;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cuo.core.UserEnvironment;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.beginDatesAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.configUserFileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.endDatesAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.latitudeAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.longitudeAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.offOffsetsAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.onOffsetsAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.relayNumberAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.relayOperatingModeAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.threshold;

/**
 * Provides a summary of all <i>Public Lighting</i> related messages
 * <p/>
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:00
 */
public enum PublicLightingDeviceMessage implements DeviceMessageSpec {

    SET_RELAY_OPERATING_MODE(0,
            PropertySpecFactory.bigDecimalPropertySpecWithValues(relayNumberAttributeName, BigDecimal.valueOf(1), BigDecimal.valueOf(2)),
            PropertySpecFactory.bigDecimalPropertySpecWithValues(relayOperatingModeAttributeName, BigDecimal.valueOf(0), BigDecimal.valueOf(1), BigDecimal.valueOf(2), BigDecimal.valueOf(3))
    ),
    SET_TIME_SWITCHING_TABLE(1,
            PropertySpecFactory.bigDecimalPropertySpecWithValues(relayNumberAttributeName, BigDecimal.valueOf(1), BigDecimal.valueOf(2)),
            PropertySpecFactory.userFileReferencePropertySpec(configUserFileAttributeName)
    ),
    SET_THRESHOLD_OVER_CONSUMPTION(2,
            PropertySpecFactory.bigDecimalPropertySpec(threshold)
    ),
    SET_OVERALL_MINIMUM_THRESHOLD(3,
            PropertySpecFactory.bigDecimalPropertySpec(threshold)
    ),
    SET_OVERALL_MAXIMUM_THRESHOLD(4,
            PropertySpecFactory.bigDecimalPropertySpec(threshold)
    ),
    SET_RELAY_TIME_OFFSETS_TABLE(5,
            PropertySpecFactory.bigDecimalPropertySpecWithValues(relayNumberAttributeName, BigDecimal.valueOf(1), BigDecimal.valueOf(2)),
            PropertySpecFactory.stringPropertySpec(beginDatesAttributeName),
            PropertySpecFactory.stringPropertySpec(endDatesAttributeName),
            PropertySpecFactory.stringPropertySpec(offOffsetsAttributeName),
            PropertySpecFactory.stringPropertySpec(onOffsetsAttributeName)
    ),
    WRITE_GPS_COORDINATES(6,
            PropertySpecFactory.stringPropertySpec(latitudeAttributeName),
            PropertySpecFactory.stringPropertySpec(longitudeAttributeName)
    );

    private static final DeviceMessageCategory category = DeviceMessageCategories.PUBLIC_LIGHTING;

    private final List<PropertySpec> deviceMessagePropertySpecs;
    private final int id;

    private PublicLightingDeviceMessage(int id, PropertySpec... deviceMessagePropertySpecs) {
        this.id = id;
        this.deviceMessagePropertySpecs = Arrays.asList(deviceMessagePropertySpecs);
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return category;
    }

    private String translate(final String key) {
        return UserEnvironment.getDefault().getTranslation(key);
    }

    @Override
    public String getName() {
        return translate(this.getNameResourceKey());
    }

    /**
     * Gets the resource key that determines the name
     * of this category to the user's language settings.
     *
     * @return The resource key
     */
    private String getNameResourceKey() {
        return PublicLightingDeviceMessage.class.getSimpleName() + "." + this.toString();
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
