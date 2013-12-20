package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.common.UserEnvironment;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecPrimaryKey;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.protocol.dynamic.RequiredPropertySpecFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum MailConfigurationDeviceMessage implements DeviceMessageSpec {

    // Read Mail (POP3) Parameters
    SetPOPUsername(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetPOPUsernameAttributeName)),
    SetPOPPassword(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetPOPPasswordAttributeName)),
    SetPOPHost(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetPOPHostAttributeName)),
    SetPOPReadMailEvery(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetPOPReadMailEveryAttributeName)),
    SetPOP3Options(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetPOP3OptionsAttributeName)),

    // Send Mail (SMTP) Parameters
    SetSMTPFrom(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetSMTPFromAttributeName)),
    SetSMTPTo(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetSMTPToAttributeName)),
    SetSMTPConfigurationTo(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetSMTPConfigurationToAttributeName)),
    SetSMTPServer(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetSMTPServerAttributeName)),
    SetSMTPDomain(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetSMTPDomainAttributeName)),
    SetSMTPSendMailEvery(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetSMTPSendMailEveryAttributeName)),
    SetSMTPCurrentInterval(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetSMTPCurrentIntervalAttributeName)),
    SetSMTPDatabaseID(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetSMTPDatabaseIDAttributeName)),
    SetSMTPOptions(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetSMTPOptionsAttributeName));

    private static final DeviceMessageCategory mailConfigurationCategory = DeviceMessageCategories.MAIL_CONFIGURATION;

    private final List<PropertySpec> deviceMessagePropertySpecs;

    private MailConfigurationDeviceMessage(PropertySpec... deviceMessagePropertySpecs) {
        this.deviceMessagePropertySpecs = Arrays.asList(deviceMessagePropertySpecs);
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return mailConfigurationCategory;
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
        return MailConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
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