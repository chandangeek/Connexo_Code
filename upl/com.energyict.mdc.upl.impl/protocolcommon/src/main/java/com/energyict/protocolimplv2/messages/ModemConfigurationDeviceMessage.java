package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.messages.DeviceMessageCategory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.DeviceMessageSpecPrimaryKey;
import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.protocolimplv2.messages.nls.Thesaurus;
import com.energyict.protocolimplv2.messages.nls.TranslationKeyImpl;

import java.util.Collections;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum ModemConfigurationDeviceMessage implements DeviceMessageSpec {

    SetDialCommand(0, DeviceMessageConstants.SetDialCommandAttributeName, DeviceMessageConstants.SetDialCommandAttributeDefaultTranslation),
    SetModemInit1(1, DeviceMessageConstants.SetModemInit1AttributeName, DeviceMessageConstants.SetModemInit1AttributeDefaultTranslation),
    SetModemInit2(2, DeviceMessageConstants.SetModemInit2AttributeName, DeviceMessageConstants.SetModemInit2AttributeDefaultTranslation),
    SetModemInit3(3, DeviceMessageConstants.SetModemInit3AttributeName, DeviceMessageConstants.SetModemInit3AttributeDefaultTranslation),
    SetPPPBaudRate(4, DeviceMessageConstants.SetPPPBaudRateAttributeName, DeviceMessageConstants.SetPPPBaudRateAttributeDefaultTranslation),
    SetModemtype(5, DeviceMessageConstants.SetModemtypeAttributeName, DeviceMessageConstants.SetModemtypeAttributeDefaultTranslation),
    SetResetCycle(6, DeviceMessageConstants.SetResetCycleAttributeName, DeviceMessageConstants.SetResetCycleAttributeDefaultTranslation);

    private final long id;
    private final String deviceMessageConstantKey;
    private final String deviceMessageConstantDefaultTranslation;

    ModemConfigurationDeviceMessage(long id, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        this.id = id;
        this.deviceMessageConstantKey = deviceMessageConstantKey;
        this.deviceMessageConstantDefaultTranslation = deviceMessageConstantDefaultTranslation;
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return DeviceMessageCategories.MODEM_CONFIGURATION;
    }

    @Override
    public String getName() {
        return Services
                .nlsService()
                .getThesaurus(Thesaurus.ID.toString())
                .getFormat(this.getNameTranslationKey())
                .format();
    }

    private String getNameResourceKey() {
        return ModemConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public TranslationKeyImpl getNameTranslationKey() {
        return new TranslationKeyImpl(this.getNameResourceKey(), this.deviceMessageConstantDefaultTranslation);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.singletonList(this.getPropertySpec());
    }

    private PropertySpec getPropertySpec() {
        return Services
                .propertySpecService()
                .stringSpec()
                .named(this.deviceMessageConstantKey, this.getNameTranslationKey())
                .describedAs(this.getNameTranslationKey().description())
                .finish();
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
    public long getMessageId() {
        return id;
    }

}