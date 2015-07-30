package com.energyict.protocolimplv2.messages;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cuo.core.UserEnvironment;
import com.energyict.mdc.messages.*;

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

    WRITE_RAW_IEC1107_CLASS( 0,
            PropertySpecFactory.boundedDecimalPropertySpec(DeviceMessageConstants.IEC1107ClassIdAttributeName, BigDecimal.valueOf(0), BigDecimal.valueOf(9999)),
            PropertySpecFactory.boundedDecimalPropertySpec(DeviceMessageConstants.OffsetAttributeName, BigDecimal.valueOf(0), BigDecimal.valueOf(9999)),
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.RawDataAttributeName)),
    WRITE_FULL_CONFIGURATION(1,PropertySpecFactory.userFileReferencePropertySpec(DeviceMessageConstants.configUserFileAttributeName)),
    SEND_XML_MESSAGE(2, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.xmlMessageAttributeName));

    private static final DeviceMessageCategory generalCategory = DeviceMessageCategories.GENERAL;

    private final List<PropertySpec> deviceMessagePropertySpecs;
    private final int id;

    private GeneralDeviceMessage(int id, PropertySpec... deviceMessagePropertySpecs) {
        this.id = id;
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

    @Override
    public int getMessageId() {
        return id;
    }
}
