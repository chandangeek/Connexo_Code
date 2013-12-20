package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.common.UserEnvironment;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecPrimaryKey;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.protocol.dynamic.RequiredPropertySpecFactory;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Provides a summary of all messages that have no unique goal.
 * For example, this can be a message that writes a general value to a certain DLMS object, chosen by the user (obiscode)
 * <p/>
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/04/13
 * Time: 8:38
 */
public enum GeneralDeviceMessage implements DeviceMessageSpec {

    WRITE_RAW_IEC1107_CLASS(
            RequiredPropertySpecFactory.newInstance().boundedDecimalPropertySpec(DeviceMessageConstants.IEC1107ClassIdAttributeName, BigDecimal.valueOf(0), BigDecimal.valueOf(9999)),
            RequiredPropertySpecFactory.newInstance().boundedDecimalPropertySpec(DeviceMessageConstants.OffsetAttributeName, BigDecimal.valueOf(0), BigDecimal.valueOf(9999)),
            RequiredPropertySpecFactory.newInstance().hexStringPropertySpec(DeviceMessageConstants.RawDataAttributeName)),
    WRITE_FULL_CONFIGURATION(RequiredPropertySpecFactory.newInstance().userFileReferencePropertySpec(DeviceMessageConstants.configUserFileAttributeName));

    private static final DeviceMessageCategory generalCategory = DeviceMessageCategories.GENERAL;

    private final List<PropertySpec> deviceMessagePropertySpecs;

    private GeneralDeviceMessage(PropertySpec... deviceMessagePropertySpecs) {
        this.deviceMessagePropertySpecs = Arrays.asList(deviceMessagePropertySpecs);
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return generalCategory;
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
        return GeneralDeviceMessage.class.getSimpleName() + "." + this.toString();
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
