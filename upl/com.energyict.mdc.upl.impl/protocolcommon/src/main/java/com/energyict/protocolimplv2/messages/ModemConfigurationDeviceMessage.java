package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.messages.nls.TranslationKeyImpl;

import java.util.Collections;

/**
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum ModemConfigurationDeviceMessage implements DeviceMessageSpecFactory {

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

    private String getNameResourceKey() {
        return ModemConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    private TranslationKeyImpl getNameTranslationKey() {
        return new TranslationKeyImpl(this.getNameResourceKey(), this.deviceMessageConstantDefaultTranslation);
    }

    private PropertySpec getPropertySpec(PropertySpecService service) {
        return service
                .stringSpec()
                .named(this.deviceMessageConstantKey, this.getNameTranslationKey())
                .describedAs(this.getNameTranslationKey().description())
                .finish();
    }

    @Override
    public DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        return new DeviceMessageSpecImpl(
                this.id,
                new EnumBasedDeviceMessageSpecPrimaryKey(this, name()),
                new TranslationKeyImpl(this.getNameResourceKey(), this.deviceMessageConstantDefaultTranslation),
                DeviceMessageCategories.MODEM_CONFIGURATION,
                Collections.singletonList(this.getPropertySpec(propertySpecService)),
                propertySpecService, nlsService);
    }

}