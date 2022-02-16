/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.messages.nls.TranslationKeyImpl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contactorActivationDateAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contactorActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contactorModeAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contactorModeAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contactorValveEnablePassword;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.digitalOutputAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.digitalOutputAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.relayNumberAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.relayNumberAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.scriptNumber;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.scriptNumberDefaultTranslation;

/**
 * Provides a summary of all <i>Contactor</i> related messages
 * <p/>
 * Date: 28/02/13
 * Time: 9:00
 */
public enum ContactorDeviceMessage implements DeviceMessageSpecSupplier {

    CONTACTOR_OPEN(1001, "Contactor open") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    CONTACTOR_OPEN_WITH_OUTPUT(1002, "Contactor open with output") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(
                    this.bigDecimalSpec(
                            service,
                            digitalOutputAttributeName, digitalOutputAttributeDefaultTranslation,
                            BigDecimal.ONE, BigDecimal.valueOf(2)));
        }
    },
    CONTACTOR_OPEN_WITH_ACTIVATION_DATE(1003, "Contactor open with activation date") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.dateTimeSpec(service, contactorActivationDateAttributeName, contactorActivationDateAttributeDefaultTranslation));
        }
    },
    CONTACTOR_ARM(1004, "Contactor arm") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    CONTACTOR_ARM_WITH_ACTIVATION_DATE(1005, "Contactor arm with activation date") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.dateTimeSpec(service, contactorActivationDateAttributeName, contactorActivationDateAttributeDefaultTranslation));
        }
    },
    CONTACTOR_CLOSE(1006, "Contactor close") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    CONTACTOR_CLOSE_WITH_OUTPUT(1007, "Contactor close with output") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(
                    this.bigDecimalSpec(
                            service,
                            digitalOutputAttributeName, digitalOutputAttributeDefaultTranslation,
                            BigDecimal.ONE, BigDecimal.valueOf(2)));
        }
    },
    CONTACTOR_CLOSE_WITH_ACTIVATION_DATE(1008, "Contactor close with activation date") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.dateTimeSpec(service, contactorActivationDateAttributeName, contactorActivationDateAttributeDefaultTranslation));
        }
    },
    CHANGE_CONNECT_CONTROL_MODE(1009, "Change the connect control mode") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(
                    this.bigDecimalSpec(
                            service,
                            contactorModeAttributeName, contactorModeAttributeDefaultTranslation,
                            BigDecimal.ZERO, BigDecimal.ONE, new BigDecimal("2"), new BigDecimal("3"),
                            new BigDecimal("4"), new BigDecimal("5"), new BigDecimal("6"))
            );
        }
    },
    CLOSE_RELAY(1010, "Close relay") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(
                    this.bigDecimalSpec(
                            service,
                            relayNumberAttributeName, relayNumberAttributeDefaultTranslation,
                            BigDecimal.ONE, new BigDecimal("2")));
        }
    },
    OPEN_RELAY(1011, "Open relay") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(
                    this.bigDecimalSpec(
                            service,
                            relayNumberAttributeName, relayNumberAttributeDefaultTranslation,
                            BigDecimal.ONE, new BigDecimal("2")));
        }
    },
    SET_RELAY_CONTROL_MODE(1012, "Set relay control mode") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(
                            service,
                            relayNumberAttributeName, relayNumberAttributeDefaultTranslation,
                            BigDecimal.ONE, new BigDecimal("2")),
                    this.bigDecimalSpec(
                            service,
                            contactorModeAttributeName, contactorModeAttributeDefaultTranslation,
                            BigDecimal.ZERO, BigDecimal.ONE, new BigDecimal("2"), new BigDecimal("3"),
                            new BigDecimal("4"), new BigDecimal("5"), new BigDecimal("6"))
            );
        }
    },
    CONTACTOR_OPEN_WITH_OUTPUT_AND_ACTIVATION_DATE(1013, "Open contactor with output ID and activation date") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, digitalOutputAttributeName, digitalOutputAttributeDefaultTranslation, BigDecimal.ONE, BigDecimal.valueOf(2)),
                    this.dateTimeSpec(service, contactorActivationDateAttributeName, contactorActivationDateAttributeDefaultTranslation)
            );
        }
    },
    CONTACTOR_CLOSE_WITH_OUTPUT_AND_ACTIVATION_DATE(1014, "Close contactor with output ID and activation date") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, digitalOutputAttributeName, digitalOutputAttributeDefaultTranslation, BigDecimal.ONE, BigDecimal.valueOf(2)),
                    this.dateTimeSpec(service, contactorActivationDateAttributeName, contactorActivationDateAttributeDefaultTranslation)
            );
        }
    },
    CONTACTOR_OPEN_WITH_DATA_PROTECTION(1015, "Contactor open with data protection") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    CONTACTOR_CLOSE_WITH_DATA_PROTECTION(1016, "Contactor close with data protection") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    CONTACTOR_ACTION_WITH_ACTIVATION(1017, "Contactor action with activation date") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.dateTimeSpec(service, contactorActivationDateAttributeName, contactorActivationDateAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, scriptNumber, scriptNumberDefaultTranslation)
            );
        }
    },
    REMOTE_DISCONNECT_WITH_DATA_PROTECTION_AND_ACTIVATION(1018, "Remote disconnect with data protection and activation date") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.dateTimeSpec(service, contactorActivationDateAttributeName, contactorActivationDateAttributeDefaultTranslation)
            );
        }
    },
    REMOTE_CONNECT_WITH_DATA_PROTECTION_AND_ACTIVATION(1019, "Remote connect with data protection and activation date") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.dateTimeSpec(service, contactorActivationDateAttributeName, contactorActivationDateAttributeDefaultTranslation)
            );
        }
    },
    CONTACTOR_CLOSE_AND_CLOSE_INVOICING_PERIOD_WITH_ACTIVATION_DATE(1020, "Contactor close with simultaneous closure of the invoicing period, with activation date") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.dateTimeSpec(service, contactorActivationDateAttributeName, contactorActivationDateAttributeDefaultTranslation)
            );
        }
    },
    CHANGE_VALVE_ENABLE_PASSWORD(1021, "Change valve enable password") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, contactorValveEnablePassword, contactorActivationDateAttributeDefaultTranslation)
            );
        }
    };
    private final long id;
    private final String defaultNameTranslation;

    ContactorDeviceMessage(long id, String defaultNameTranslation) {
        this.id = id;
        this.defaultNameTranslation = defaultNameTranslation;
    }

    @Override
    public long id() {
        return this.id;
    }

    protected abstract List<PropertySpec> getPropertySpecs(PropertySpecService service);

    private String getNameResourceKey() {
        return ContactorDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        return new DeviceMessageSpecImpl(
                id, new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation),
                DeviceMessageCategories.CONTACTOR,
                this.getPropertySpecs(propertySpecService),
                propertySpecService, nlsService, converter);
    }
}