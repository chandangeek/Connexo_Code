/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.messages.nls.TranslationKeyImpl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public enum UmiwanDeviceMessage implements DeviceMessageSpecSupplier {


    WRITE_UMIWAN_CONFIGURATION(42001, "Write umiwan configuration") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.primHost, DeviceMessageConstants.primHostDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.secHost, DeviceMessageConstants.secHostDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.gateHost, DeviceMessageConstants.gateHostDefaultTranslation),
                    this.integerSpec(service, DeviceMessageConstants.inactiveTimeout, DeviceMessageConstants.inactiveTimeoutDefaultTranslation),
                    this.integerSpec(service, DeviceMessageConstants.sessionTimeout, DeviceMessageConstants.sessionTimeoutDefaultTranslation),
                    this.integerSpec(service, DeviceMessageConstants.preferredTimeOfDay, DeviceMessageConstants.preferredTimeOfDayDefaultTranslation),
                    this.integerSpec(service, DeviceMessageConstants.callDistance, DeviceMessageConstants.callDistanceDefaultTranslation),
                    this.integerSpec(service, DeviceMessageConstants.shortRetryDistance, DeviceMessageConstants.shortRetryDistanceDefaultTranslation),
                    this.integerSpec(service, DeviceMessageConstants.longRetryDistance, DeviceMessageConstants.longRetryDistanceDefaultTranslation),
                    this.integerSpec(service, DeviceMessageConstants.randomZone, DeviceMessageConstants.randomZoneDefaultTranslation),
                    this.integerSpec(service, DeviceMessageConstants.controlFlags, DeviceMessageConstants.controlFlagsDefaultTranslation),
                    this.integerSpec(service, DeviceMessageConstants.primPort, DeviceMessageConstants.primPortDefaultTranslation),
                    this.integerSpec(service, DeviceMessageConstants.secPort, DeviceMessageConstants.secPortDefaultTranslation),
                    this.integerSpec(service, DeviceMessageConstants.gatePort, DeviceMessageConstants.gatePortDefaultTranslation),
                    this.integerSpec(service, DeviceMessageConstants.maxShortRetries, DeviceMessageConstants.maxShortRetriesDefaultTranslation),
                    this.integerSpec(service, DeviceMessageConstants.maxLongRetries, DeviceMessageConstants.maxLongRetriesDefaultTranslation)
            );
        }
    },
    WRITE_UMIWAN_PROFILE_CONTROL(42002, "Write umiwan profile control") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(
                    this.dateTimeSpec(service, DeviceMessageConstants.startTime, DeviceMessageConstants.startTimeDefaultTranslation)
            );
        }
    },
    WRITE_UMIWAN_EVENT_CONTROL(42003, "Write umiwan event control") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(
                    this.dateTimeSpec(service, DeviceMessageConstants.startTime, DeviceMessageConstants.startTimeDefaultTranslation)
            );
        }
    },
    READ_UMIWAN_STD_STATUS(42004, "Read umiwan std status") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    READ_GSM_STD_STATUS(42005, "Read gsm std status") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    };

    private final long id;
    private final String defaultNameTranslation;


    UmiwanDeviceMessage(long id, String defaultNameTranslation) {
        this.id = id;
        this.defaultNameTranslation = defaultNameTranslation;
    }

    @Override
    public long id() {
        return this.id;
    }

    private String getNameResourceKey() {
        return PowerConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    protected abstract List<PropertySpec> getPropertySpecs(PropertySpecService service);

    @Override
    public DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        return new DeviceMessageSpecImpl(
                id, new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation),
                DeviceMessageCategories.UMIWAN,
                this.getPropertySpecs(propertySpecService),
                propertySpecService, nlsService, converter);
    }

    protected PropertySpec integerSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .integerSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
    }
}