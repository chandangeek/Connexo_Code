package com.energyict.protocolimplv2.messages;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.mdc.common.UserEnvironment;
import com.energyict.mdc.messages.DeviceMessageCategory;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.messages.DeviceMessageSpecPrimaryKey;

import java.util.Arrays;
import java.util.List;

/**
 * Provides a summary of all messages related to pricing
 * <p/>
 * Copyrights EnergyICT
 * Date: 11/03/13
 * Time: 11:59
 */
public enum PricingInformationMessage implements DeviceMessageSpec {

    ReadPricingInformation,
    SetPricingInformation(
            PropertySpecFactory.userFileReferencePropertySpec(DeviceMessageConstants.PricingInformationUserFileAttributeName),
            PropertySpecFactory.dateTimePropertySpec(DeviceMessageConstants.PricingInformationActivationDateAttributeName)
    ),
    SetStandingCharge(
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.StandingChargeAttributeName),
            PropertySpecFactory.dateTimePropertySpec(DeviceMessageConstants.PricingInformationActivationDateAttributeName)
    ),
    UpdatePricingInformation(
            PropertySpecFactory.userFileReferencePropertySpec(DeviceMessageConstants.PricingInformationUserFileAttributeName)
    );

    private static final DeviceMessageCategory category = DeviceMessageCategories.PRICING_INFORMATION;

    private final List<PropertySpec> deviceMessagePropertySpecs;

    private PricingInformationMessage(PropertySpec... deviceMessagePropertySpecs) {
        this.deviceMessagePropertySpecs = Arrays.asList(deviceMessagePropertySpecs);
    }

    private static String translate(final String key) {
        return UserEnvironment.getDefault().getTranslation(key);
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return category;
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
        return PricingInformationMessage.class.getSimpleName() + "." + this.toString();
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
