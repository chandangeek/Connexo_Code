/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

enum EIWebConfigurationDeviceMessage implements DeviceMessageSpecEnum {

    SetEIWebPassword(DeviceMessageId.EIWEB_SET_PASSWORD, "Set EIWeb password") {
        @Override
        protected EIWebConfigurationDeviceMessageAttributes translationKey() {
            return EIWebConfigurationDeviceMessageAttributes.SetEIWebPasswordAttributeName;
        }
    },
    SetEIWebPage(DeviceMessageId.EIWEB_SET_PAGE, "Set EIWeb web page") {
        @Override
        protected EIWebConfigurationDeviceMessageAttributes translationKey() {
            return EIWebConfigurationDeviceMessageAttributes.SetEIWebPageAttributeName;
        }
    },
    SetEIWebFallbackPage(DeviceMessageId.EIWEB_SET_FALLBACK_PAGE, "Set EIWeb fallback page") {
        @Override
        protected EIWebConfigurationDeviceMessageAttributes translationKey() {
            return EIWebConfigurationDeviceMessageAttributes.SetEIWebFallbackPageAttributeName;
        }
    },
    SetEIWebSendEvery(DeviceMessageId.EIWEB_SET_SEND_EVERY, "Set EIWeb send every") {
        @Override
        protected EIWebConfigurationDeviceMessageAttributes translationKey() {
            return EIWebConfigurationDeviceMessageAttributes.SetEIWebSendEveryAttributeName;
        }
    },
    SetEIWebCurrentInterval(DeviceMessageId.EIWEB_SET_CURRENT_INTERVAL, "Set EIWeb current interval") {
        @Override
        protected EIWebConfigurationDeviceMessageAttributes translationKey() {
            return EIWebConfigurationDeviceMessageAttributes.SetEIWebCurrentIntervalAttributeName;
        }
    },
    SetEIWebDatabaseID(DeviceMessageId.EIWEB_SET_DATABASE_ID, "Set EIWeb database ID") {
        @Override
        protected EIWebConfigurationDeviceMessageAttributes translationKey() {
            return EIWebConfigurationDeviceMessageAttributes.SetEIWebDatabaseIDAttributeName;
        }
    },
    SetEIWebOptions(DeviceMessageId.EIWEB_SET_OPTIONS, "Set EIWeb web options") {
        @Override
        protected EIWebConfigurationDeviceMessageAttributes translationKey() {
            return EIWebConfigurationDeviceMessageAttributes.SetEIWebOptionsAttributeName;
        }
    };

    private DeviceMessageId id;
    private String defaultTranslation;

    EIWebConfigurationDeviceMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }

    @Override
    public String getKey() {
        return EIWebConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
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
                        .bigDecimalSpec()
                        .named(EIWebConfigurationDeviceMessageAttributes.Id)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .addValues(BigDecimal.ONE, BigDecimals.TWO)
                        .markExhaustive()
                        .finish());
        propertySpecs.add(
                propertySpecService
                        .stringSpec()
                        .named(this.translationKey())
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish());
        return propertySpecs;
    }

    protected abstract EIWebConfigurationDeviceMessageAttributes translationKey();

}