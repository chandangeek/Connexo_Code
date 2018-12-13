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
public enum ModemConfigurationDeviceMessage implements DeviceMessageSpecSupplier {

    SetDialCommand(20001, DeviceMessageConstants.SetDialCommandAttributeName, DeviceMessageConstants.SetDialCommandAttributeDefaultTranslation),
    SetModemInit1(20002, DeviceMessageConstants.SetModemInit1AttributeName, DeviceMessageConstants.SetModemInit1AttributeDefaultTranslation),
    SetModemInit2(20003, DeviceMessageConstants.SetModemInit2AttributeName, DeviceMessageConstants.SetModemInit2AttributeDefaultTranslation),
    SetModemInit3(20004, DeviceMessageConstants.SetModemInit3AttributeName, DeviceMessageConstants.SetModemInit3AttributeDefaultTranslation),
    SetPPPBaudRate(20005, DeviceMessageConstants.SetPPPBaudRateAttributeName, DeviceMessageConstants.SetPPPBaudRateAttributeDefaultTranslation),
    SetModemtype(20006, DeviceMessageConstants.SetModemtypeAttributeName, DeviceMessageConstants.SetModemtypeAttributeDefaultTranslation),
    SetResetCycle(20007, DeviceMessageConstants.SetResetCycleAttributeName, DeviceMessageConstants.SetResetCycleAttributeDefaultTranslation);

    private final long id;
    private final String deviceMessageConstantKey;
    private final String deviceMessageConstantDefaultTranslation;

    ModemConfigurationDeviceMessage(long id, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        this.id = id;
        this.deviceMessageConstantKey = deviceMessageConstantKey;
        this.deviceMessageConstantDefaultTranslation = deviceMessageConstantDefaultTranslation;
    }

    @Override
    public long id() {
        return this.id;
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
                .markRequired()
                .finish();
    }

    @Override
    public DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        return new DeviceMessageSpecImpl(
                id, new TranslationKeyImpl(this.getNameResourceKey(), this.deviceMessageConstantDefaultTranslation),
                DeviceMessageCategories.MODEM_CONFIGURATION,
                Collections.singletonList(this.getPropertySpec(propertySpecService)),
                propertySpecService, nlsService, converter);
    }

}