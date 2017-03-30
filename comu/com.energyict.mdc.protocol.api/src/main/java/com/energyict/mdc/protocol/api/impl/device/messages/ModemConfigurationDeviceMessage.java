/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.util.ArrayList;
import java.util.List;

enum ModemConfigurationDeviceMessage implements DeviceMessageSpecEnum {

    SetDialCommand(DeviceMessageId.MODEM_CONFIGURATION_SET_DIAL_COMMAND, "Set dial command") {
        @Override
        protected TranslationKey translationKey() {
            return ModemDeviceMessageAttributes.SetDialCommandAttributeName;
        }
    },
    SetModemInit1(DeviceMessageId.MODEM_CONFIGURATION_SET_MODEM_INIT_1, "Set modem init1") {
        @Override
        protected TranslationKey translationKey() {
            return ModemDeviceMessageAttributes.SetModemInit1AttributeName;
        }
    },
    SetModemInit2(DeviceMessageId.MODEM_CONFIGURATION_SET_MODEM_INIT_2, "Set modem init2") {
        @Override
        protected TranslationKey translationKey() {
            return ModemDeviceMessageAttributes.SetModemInit2AttributeName;
        }
    },
    SetModemInit3(DeviceMessageId.MODEM_CONFIGURATION_SET_MODEM_INIT_3, "Set modem init3") {
        @Override
        protected TranslationKey translationKey() {
            return ModemDeviceMessageAttributes.SetModemInit3AttributeName;
        }
    },
    SetPPPBaudRate(DeviceMessageId.MODEM_CONFIGURATION_SET_PPP_BAUD_RATE, "Set PPP baud rate") {
        @Override
        protected TranslationKey translationKey() {
            return ModemDeviceMessageAttributes.SetPPPBaudRateAttributeName;
        }
    },
    SetModemtype(DeviceMessageId.MODEM_CONFIGURATION_SET_MODEMTYPE, "Set modem type") {
        @Override
        protected TranslationKey translationKey() {
            return ModemDeviceMessageAttributes.SetModemtypeAttributeName;
        }
    },
    SetResetCycle(DeviceMessageId.MODEM_CONFIGURATION_SET_RESET_CYCLE, "Set reset cycle") {
        @Override
        protected TranslationKey translationKey() {
            return ModemDeviceMessageAttributes.SetResetCycleAttributeName;
        }
    };

    private DeviceMessageId id;
    private String defaultTranslation;

    ModemConfigurationDeviceMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }

    @Override
    public String getKey() {
        return ModemConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
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
        propertySpecs.add(
                propertySpecService
                        .stringSpec()
                        .named(translationKey())
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish());
        return propertySpecs;
    }

    protected abstract TranslationKey translationKey();

}