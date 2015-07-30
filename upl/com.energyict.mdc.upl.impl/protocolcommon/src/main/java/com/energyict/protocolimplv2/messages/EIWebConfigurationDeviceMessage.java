package com.energyict.protocolimplv2.messages;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cuo.core.UserEnvironment;
import com.energyict.mdc.messages.DeviceMessageCategory;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.messages.DeviceMessageSpecPrimaryKey;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum EIWebConfigurationDeviceMessage implements DeviceMessageSpec {

    SetEIWebPassword(0,
            PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.id, BigDecimal.valueOf(1), BigDecimal.valueOf(2)),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetEIWebPasswordAttributeName)),
    SetEIWebPage(1,
            PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.id, BigDecimal.valueOf(1), BigDecimal.valueOf(2)),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetEIWebPageAttributeName)),
    SetEIWebFallbackPage(2,
            PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.id, BigDecimal.valueOf(1), BigDecimal.valueOf(2)),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetEIWebFallbackPageAttributeName)),
    SetEIWebSendEvery(3,
            PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.id, BigDecimal.valueOf(1), BigDecimal.valueOf(2)),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetEIWebSendEveryAttributeName)),
    SetEIWebCurrentInterval(4,
            PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.id, BigDecimal.valueOf(1), BigDecimal.valueOf(2)),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetEIWebCurrentIntervalAttributeName)),
    SetEIWebDatabaseID(5,
            PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.id, BigDecimal.valueOf(1), BigDecimal.valueOf(2)),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetEIWebDatabaseIDAttributeName)),
    SetEIWebOptions(6,
            PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.id, BigDecimal.valueOf(1), BigDecimal.valueOf(2)),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetEIWebOptionsAttributeName)),
    UpdateEIWebSSLCertificate(7, PropertySpecFactory.userFileReferencePropertySpec(DeviceMessageConstants.sslCertificateUserFile)),
    EIWebSetOption(8, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.singleOptionAttributeName)),
    EIWebClrOption(21, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.singleOptionAttributeName));

    private static final DeviceMessageCategory eiWebCategory = DeviceMessageCategories.EIWEB_PARAMETERS;

    private final List<PropertySpec> deviceMessagePropertySpecs;
    private final int id;

    private EIWebConfigurationDeviceMessage(int id, PropertySpec... deviceMessagePropertySpecs) {
        this.id = id;
        this.deviceMessagePropertySpecs = Arrays.asList(deviceMessagePropertySpecs);
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return eiWebCategory;
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
        return EIWebConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
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