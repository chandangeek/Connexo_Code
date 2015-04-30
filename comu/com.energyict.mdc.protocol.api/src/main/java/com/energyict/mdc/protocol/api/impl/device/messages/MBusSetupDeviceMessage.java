package com.energyict.mdc.protocol.api.impl.device.messages;

import com.energyict.mdc.dynamic.HexStringFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.elster.jupiter.properties.PropertySpec;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.defaultKeyAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.openKeyAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.transferKeyAttributeName;

/**
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum MBusSetupDeviceMessage implements DeviceMessageSpecEnum {

    Decommission(DeviceMessageId.MBUS_SETUP_DECOMMISSION, "Decommission"),
    DataReadout(DeviceMessageId.MBUS_SETUP_DATA_READOUT, "Data readout"),
    Commission(DeviceMessageId.MBUS_SETUP_COMMISSION, "Commission"),
    DecommissionAll(DeviceMessageId.MBUS_SETUP_DECOMMISSION_ALL, "Decommission all"),
    SetEncryptionKeys(DeviceMessageId.MBUS_SETUP_SET_ENCRYPTION_KEYS, "Set encryption key") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(this.hexStringProperty(openKeyAttributeName, propertySpecService));
            propertySpecs.add(this.hexStringProperty(transferKeyAttributeName, propertySpecService));
        }
    },
    SetEncryptionKeysUsingCryptoserver(DeviceMessageId.MBUS_SETUP_SET_ENCRYPTION_KEYS_USING_CRYPTOSERVER, "Set encryption key using cryptoserver") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(this.hexStringProperty(defaultKeyAttributeName, propertySpecService));
        }
    },
    UseCorrectedValues(DeviceMessageId.MBUS_SETUP_USE_CORRECTED_VALUES, "Use corrected values"),
    UseUncorrectedValues(DeviceMessageId.MBUS_SETUP_USE_UNCORRECTED_VALUES, "Use uncorrected values"),
    Commission_With_Channel(DeviceMessageId.MBUS_SETUP_COMMISSION_WITH_CHANNEL, "Commission wih channel"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpecWithValues(DeviceMessageConstants.mbusChannel, true, BigDecimal.ONE, BigDecimals.TWO,  BigDecimals.THREE, BigDecimals.FOUR));
        }
    },
    ;

    private DeviceMessageId id;
    private String defaultTranslation;

    MBusSetupDeviceMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }

    @Override
    public String getKey() {
        return MBusSetupDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultTranslation;
    }

    @Override
    public DeviceMessageId getId() {
        return this.id;
    }

    public final List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService) {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        this.addPropertySpecs(propertySpecs, propertySpecService);
        return propertySpecs;
    }

    protected void addPropertySpecs (List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
        // Default behavior is not to add anything
    };

    protected PropertySpec hexStringProperty(String name, PropertySpecService propertySpecService) {
        return propertySpecService.basicPropertySpec(name, true, new HexStringFactory());
    }

    public final PropertySpec getPropertySpec(String name, PropertySpecService propertySpecService) {
        for (PropertySpec securityProperty : getPropertySpecs(propertySpecService)) {
            if (securityProperty.getName().equals(name)) {
                return securityProperty;
            }
        }
        return null;
    }

}