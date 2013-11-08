package com.energyict.protocolimplv2.messages;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cuo.core.UserEnvironment;
import com.energyict.mdc.messages.*;

import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum MailConfigurationDeviceMessage implements DeviceMessageSpec {

    // Read Mail (POP3) Parameters
    SetPOPUsername(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetPOPUsernameAttributeName)),
    SetPOPPassword(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetPOPPasswordAttributeName)),
    SetPOPHost(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetPOPHostAttributeName)),
    SetPOPReadMailEvery(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetPOPReadMailEveryAttributeName)),
    SetPOP3Options(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetPOP3OptionsAttributeName)),

    // Send Mail (SMTP) Parameters
    SetSMTPFrom(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetSMTPFromAttributeName)),
    SetSMTPTo(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetSMTPToAttributeName)),
    SetSMTPConfigurationTo(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetSMTPConfigurationToAttributeName)),
    SetSMTPServer(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetSMTPServerAttributeName)),
    SetSMTPDomain(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetSMTPDomainAttributeName)),
    SetSMTPSendMailEvery(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetSMTPSendMailEveryAttributeName)),
    SetSMTPCurrentInterval(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetSMTPCurrentIntervalAttributeName)),
    SetSMTPDatabaseID(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetSMTPDatabaseIDAttributeName)),
    SetSMTPOptions(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetSMTPOptionsAttributeName));

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