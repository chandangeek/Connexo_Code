package com.energyict.protocolimplv2.messages;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cuo.core.UserEnvironment;
import com.energyict.mdc.messages.DeviceMessageCategory;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.messages.DeviceMessageSpecPrimaryKey;

import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum MailConfigurationDeviceMessage implements DeviceMessageSpec {

    // Read Mail (POP3) Parameters
    SetPOPUsername(0, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetPOPUsernameAttributeName)),
    SetPOPPassword(1, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetPOPPasswordAttributeName)),
    SetPOPHost(2, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetPOPHostAttributeName)),
    SetPOPReadMailEvery(3, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetPOPReadMailEveryAttributeName)),
    SetPOP3Options(4, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetPOP3OptionsAttributeName)),

    // Send Mail (SMTP) Parameters
    SetSMTPFrom(5, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetSMTPFromAttributeName)),
    SetSMTPTo(6, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetSMTPToAttributeName)),
    SetSMTPConfigurationTo(7, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetSMTPConfigurationToAttributeName)),
    SetSMTPServer(8, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetSMTPServerAttributeName)),
    SetSMTPDomain(9, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetSMTPDomainAttributeName)),
    SetSMTPSendMailEvery(10, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetSMTPSendMailEveryAttributeName)),
    SetSMTPCurrentInterval(11, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetSMTPCurrentIntervalAttributeName)),
    SetSMTPDatabaseID(12, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetSMTPDatabaseIDAttributeName)),
    SetSMTPOptions(13, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetSMTPOptionsAttributeName)),
    POP3SetOption(14, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.singleOptionAttributeName)),
    POP3ClrOption(15, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.singleOptionAttributeName)),
    SMTPSetOption(16, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.singleOptionAttributeName)),
    SMTPClrOption(17, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.singleOptionAttributeName));

    private static final DeviceMessageCategory mailConfigurationCategory = DeviceMessageCategories.MAIL_CONFIGURATION;

    private final List<PropertySpec> deviceMessagePropertySpecs;
    private final int id;

    private MailConfigurationDeviceMessage(int id, PropertySpec... deviceMessagePropertySpecs) {
        this.id = id;
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

    @Override
    public int getMessageId() {
        return id;
    }
}