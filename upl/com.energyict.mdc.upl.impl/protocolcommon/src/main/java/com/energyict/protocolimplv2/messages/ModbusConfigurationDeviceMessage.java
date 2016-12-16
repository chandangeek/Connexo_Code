package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.messages.nls.TranslationKeyImpl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum ModbusConfigurationDeviceMessage implements DeviceMessageSpecSupplier {

    SetMmEvery(22001, "Set Mm every", DeviceMessageConstants.SetMmEveryAttributeName, DeviceMessageConstants.SetMmEveryAttributeDefaultTranslation),
    SetMmTimeout(22002, "Set Mm timeout", DeviceMessageConstants.SetMmTimeoutAttributeName, DeviceMessageConstants.SetMmTimeoutAttributeDefaultTranslation),
    SetMmInstant(22003, "Set Mm instant", DeviceMessageConstants.SetMmInstantAttributeName, DeviceMessageConstants.SetMmInstantAttributeDefaultTranslation),
    SetMmOverflow(22004, "Set Mm overflow", DeviceMessageConstants.SetMmOverflowAttributeName, DeviceMessageConstants.SetMmOverflowAttributeDefaultTranslation),
    SetMmConfig(22005, "Set Mm configuration", DeviceMessageConstants.SetMmConfigAttributeName, DeviceMessageConstants.SetMmConfigAttributeDefaultTranslation),
    WriteSingleRegisters(22006, "Write single registers") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.RadixFormatAttributeName, DeviceMessageConstants.RadixFormatAttributeDefaultTranslation, "DEC", "HEX"),
                    this.hexStringSpec(service, DeviceMessageConstants.RegisterAddressAttributeName, DeviceMessageConstants.RegisterAddressAttributeDefaultTranslation),
                    this.hexStringSpec(service, DeviceMessageConstants.RegisterValueAttributeName, DeviceMessageConstants.RegisterValueAttributeDefaultTranslation)
            );
        }
    },
    WriteMultipleRegisters(22007, "Write multiple registers") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.RadixFormatAttributeName, DeviceMessageConstants.RadixFormatAttributeDefaultTranslation, "DEC", "HEX"),
                    this.hexStringSpec(service, DeviceMessageConstants.RegisterAddressAttributeName, DeviceMessageConstants.RegisterAddressAttributeDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.RegisterValueAttributeName, DeviceMessageConstants.RegisterValueAttributeDefaultTranslation)
            );
        }
    },
    MmSetOption(22008, "Modbus - Set an option", DeviceMessageConstants.singleOptionAttributeName, DeviceMessageConstants.singleOptionAttributeDefaultTranslation),
    MmClrOption(22009, "Modbus - Clear an option", DeviceMessageConstants.singleOptionAttributeName, DeviceMessageConstants.singleOptionAttributeDefaultTranslation),
    WriteMultipleCoils(22010, "Write multiple coils") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.RadixFormatAttributeName, DeviceMessageConstants.RadixFormatAttributeDefaultTranslation, "DEC", "HEX"),
                    this.hexStringSpec(service, DeviceMessageConstants.AddressAttributeName, DeviceMessageConstants.AddressAttributeDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.ValueAttributeName, DeviceMessageConstants.ValueAttributeDefaultTranslation)
            );
        }
    },
    WriteSingleCoil(22011, "Write single coil") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.RadixFormatAttributeName, DeviceMessageConstants.RadixFormatAttributeDefaultTranslation, "DEC", "HEX"),
                    this.hexStringSpec(service, DeviceMessageConstants.AddressAttributeName, DeviceMessageConstants.AddressAttributeDefaultTranslation),
                    this.hexStringSpec(service, DeviceMessageConstants.ValueAttributeName, DeviceMessageConstants.ValueAttributeDefaultTranslation)
            );
        }
    };

    private final long id;
    private final String defaultNameTranslation;
    private final String propertyName;
    private final String propertyNameDefaultTranslation;

    ModbusConfigurationDeviceMessage(long id, String defaultNameTranslation) {
        this(id, defaultNameTranslation, "NA" , "NA");
    }

    ModbusConfigurationDeviceMessage(long id, String defaultNameTranslation, String propertyName, String propertyNameDefaultTranslation) {
        this.id = id;
        this.defaultNameTranslation = defaultNameTranslation;
        this.propertyName = propertyName;
        this.propertyNameDefaultTranslation = propertyNameDefaultTranslation;
    }

    private String getNameResourceKey() {
        return ModbusConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    private PropertySpecBuilder<String> stringSpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .stringSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired();
    }

    protected PropertySpec stringSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return this.stringSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation).finish();
    }

    protected PropertySpec stringSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, String... exhaustiveValues) {
        return this.stringSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                    .addValues(exhaustiveValues)
                    .markExhaustive()
                    .finish();
    }

    protected PropertySpec hexStringSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .hexStringSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
    }

    protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
        return Collections.singletonList(this.stringSpec(service, this.propertyName, propertyNameDefaultTranslation));
    }

    @Override
    public DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        return new DeviceMessageSpecImpl(
                id, new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation),
                DeviceMessageCategories.MODBUS_CONFIGURATION,
                this.getPropertySpecs(propertySpecService),
                propertySpecService, nlsService);
    }

}