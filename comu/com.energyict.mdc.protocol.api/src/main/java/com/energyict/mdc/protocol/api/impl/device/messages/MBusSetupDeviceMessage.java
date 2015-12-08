package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.HexString;
import com.energyict.mdc.dynamic.HexStringFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

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
    Commission_With_Channel(DeviceMessageId.MBUS_SETUP_COMMISSION_WITH_CHANNEL, "Commission wih channel") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpecWithValues(DeviceMessageConstants.mbusChannel, true, BigDecimal.ONE, BigDecimals.TWO, BigDecimals.THREE, BigDecimals.FOUR));
        }
    },
    WriteCaptureDefinitionForAllInstances(DeviceMessageId.MBUS_SETUP_WRITE_CAPTURE_DEFINITION_FOR_ALL_INSTANCES, "Write the capture definition for all instances") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.hexStringPropertySpec(DeviceMessageConstants.dibInstance1, "dib instance 1", true, getCaptureDefinitionDefaultValue()));
            propertySpecs.add(propertySpecService.hexStringPropertySpec(DeviceMessageConstants.vibInstance1, "vib instance 1", true, getCaptureDefinitionDefaultValue()));
            propertySpecs.add(propertySpecService.hexStringPropertySpec(DeviceMessageConstants.dibInstance2, "dib instance 2", true, getCaptureDefinitionDefaultValue()));
            propertySpecs.add(propertySpecService.hexStringPropertySpec(DeviceMessageConstants.vibInstance2, "vib instance 2", true, getCaptureDefinitionDefaultValue()));
            propertySpecs.add(propertySpecService.hexStringPropertySpec(DeviceMessageConstants.dibInstance3, "dib instance 3", true, getCaptureDefinitionDefaultValue()));
            propertySpecs.add(propertySpecService.hexStringPropertySpec(DeviceMessageConstants.vibInstance3, "vib instance 3", true, getCaptureDefinitionDefaultValue()));
            propertySpecs.add(propertySpecService.hexStringPropertySpec(DeviceMessageConstants.dibInstance4, "dib instance 4", true, getCaptureDefinitionDefaultValue()));
            propertySpecs.add(propertySpecService.hexStringPropertySpec(DeviceMessageConstants.vibInstance4, "vib instance 4", true, getCaptureDefinitionDefaultValue()));
        }
    },
    WriteMBusCapturePeriod(DeviceMessageId.MBUS_SETUP_WRITE_CAPTURE_PERIOD, "Write mbus capture period"),;

    private static HexString getCaptureDefinitionDefaultValue() {
        return new HexString("FFFFFFFFFFFFFFFFFFFFFF");
    }

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

    public final List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        this.addPropertySpecs(propertySpecs, propertySpecService);
        return propertySpecs;
    }

    protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
        // Default behavior is not to add anything
    }

    ;

    protected PropertySpec hexStringProperty(String name, PropertySpecService propertySpecService) {
        return propertySpecService.basicPropertySpec(name, true, new HexStringFactory());
    }

}