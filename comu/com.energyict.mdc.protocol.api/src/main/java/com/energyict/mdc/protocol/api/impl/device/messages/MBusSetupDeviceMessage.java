/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.HexString;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

enum MBusSetupDeviceMessage implements DeviceMessageSpecEnum {

    Decommission(DeviceMessageId.MBUS_SETUP_DECOMMISSION, "Decommission"),
    DataReadout(DeviceMessageId.MBUS_SETUP_DATA_READOUT, "Data readout"),
    Commission(DeviceMessageId.MBUS_SETUP_COMMISSION, "Commission"),
    DecommissionAll(DeviceMessageId.MBUS_SETUP_DECOMMISSION_ALL, "Decommission all"),
    SetEncryptionKeys(DeviceMessageId.MBUS_SETUP_SET_ENCRYPTION_KEYS, "Set encryption key") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.hexStringProperty(MBusSetupDeviceMessageAttributes.openKeyAttributeName, propertySpecService, thesaurus));
            propertySpecs.add(this.hexStringProperty(MBusSetupDeviceMessageAttributes.transferKeyAttributeName, propertySpecService, thesaurus));
        }
    },
    SetEncryptionKeysUsingCryptoserver(DeviceMessageId.MBUS_SETUP_SET_ENCRYPTION_KEYS_USING_CRYPTOSERVER, "Set encryption key using cryptoserver") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.hexStringProperty(MBusSetupDeviceMessageAttributes.defaultKeyAttributeName, propertySpecService, thesaurus));
        }
    },
    UseCorrectedValues(DeviceMessageId.MBUS_SETUP_USE_CORRECTED_VALUES, "Use corrected values"),
    UseUncorrectedValues(DeviceMessageId.MBUS_SETUP_USE_UNCORRECTED_VALUES, "Use uncorrected values"),
    Commission_With_Channel(DeviceMessageId.MBUS_SETUP_COMMISSION_WITH_CHANNEL, "Commission wih channel") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(MBusSetupDeviceMessageAttributes.mbusChannel)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .addValues(BigDecimal.ONE, BigDecimals.TWO, BigDecimals.THREE, BigDecimals.FOUR)
                            .markExhaustive()
                            .finish());
        }
    },
    WriteCaptureDefinitionForAllInstances(DeviceMessageId.MBUS_SETUP_WRITE_CAPTURE_DEFINITION_FOR_ALL_INSTANCES, "Write the capture definition for all instances") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            Stream.of(
                    MBusSetupDeviceMessageAttributes.dibInstance1,
                    MBusSetupDeviceMessageAttributes.vibInstance1,
                    MBusSetupDeviceMessageAttributes.dibInstance2,
                    MBusSetupDeviceMessageAttributes.vibInstance2,
                    MBusSetupDeviceMessageAttributes.dibInstance3,
                    MBusSetupDeviceMessageAttributes.vibInstance3,
                    MBusSetupDeviceMessageAttributes.dibInstance4,
                    MBusSetupDeviceMessageAttributes.vibInstance4)
                .map(name -> propertySpecService
                                .hexStringSpec()
                                .named(name)
                                .fromThesaurus(thesaurus)
                                .markRequired()
                                .addValues(getCaptureDefinitionDefaultValue())
                                .markExhaustive().finish())
                .forEach(propertySpecs::add);
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
        this.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
        return propertySpecs;
    }

    protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        // Default behavior is not to add anything
    }

    protected PropertySpec hexStringProperty(MBusSetupDeviceMessageAttributes name, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        return propertySpecService
                .hexStringSpec()
                .named(name)
                .fromThesaurus(thesaurus)
                .markRequired()
                .finish();
    }

}