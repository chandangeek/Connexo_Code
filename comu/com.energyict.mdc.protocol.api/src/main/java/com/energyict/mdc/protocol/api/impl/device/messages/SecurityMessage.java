/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.device.messages.DlmsAuthenticationLevelMessageValues;
import com.energyict.mdc.protocol.api.device.messages.DlmsEncryptionLevelMessageValues;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.messaging.KeyTUsage;
import com.energyict.mdc.protocol.api.messaging.SealActions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public enum SecurityMessage implements DeviceMessageSpecEnum {

    ACTIVATE_DLMS_ENCRYPTION(DeviceMessageId.SECURITY_ACTIVATE_DLMS_ENCRYPTION, "Activate encryption") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService.
                            stringSpec()
                            .named(DeviceMessageAttributes.encryptionLevelAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .addValues(DlmsEncryptionLevelMessageValues.getNames())
                            .markExhaustive()
                            .finish());
        }
    },
    CHANGE_DLMS_AUTHENTICATION_LEVEL(DeviceMessageId.SECURITY_CHANGE_DLMS_AUTHENTICATION_LEVEL, "Change authentication level") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.authenticationLevelAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .addValues(DlmsAuthenticationLevelMessageValues.getNames())
                            .markExhaustive()
                            .finish());
        }
    },
    CHANGE_ENCRYPTION_KEY(DeviceMessageId.SECURITY_CHANGE_ENCRYPTION_KEY, "Change encryption key"),
    CHANGE_CLIENT_PASSWORDS(DeviceMessageId.SECURITY_CHANGE_CLIENT_PASSWORDS, "Change client passwords") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            Stream.of(DeviceMessageAttributes.newReadingClientPasswordAttributeName, DeviceMessageAttributes.newManagementClientPasswordAttributeName, DeviceMessageAttributes.newFirmwareClientPasswordAttributeName)
                    .map(attributeName -> propertySpecService
                            .stringSpec()
                            .named(attributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish())
                .forEach(propertySpecs::add);
        }
    },
    WRITE_PSK(DeviceMessageId.SECURITY_WRITE_PSK, "Write PSK") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .hexStringSpec()
                            .named(DeviceMessageAttributes.pskAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY(DeviceMessageId.SECURITY_CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY, "Change encryption key with the value") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .passwordSpec()
                            .named(DeviceMessageAttributes.newEncryptionKeyAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    }, CHANGE_ENCRYPTION_KEY_WITH_NEW_KEYS(DeviceMessageId.SECURITY_CHANGE_ENCRYPTION_KEY_WITH_NEW_KEYS, "Change encryption key with new values") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            Stream.of(DeviceMessageAttributes.newEncryptionKeyAttributeName, DeviceMessageAttributes.newWrappedEncryptionKeyAttributeName)
                .map(attributeName -> propertySpecService
                        .passwordSpec()
                        .named(attributeName)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish());
        }
    },
    CHANGE_AUTHENTICATION_KEY(DeviceMessageId.SECURITY_CHANGE_AUTHENTICATION_KEY, "Change authentication key"),
    CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEY(DeviceMessageId.SECURITY_CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEY, "Change authentication key with the value") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .passwordSpec()
                            .named(DeviceMessageAttributes.newAuthenticationKeyAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    }, CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEYS(DeviceMessageId.SECURITY_CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEYS, "Change authentication key with new values") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            Stream.of(DeviceMessageAttributes.newAuthenticationKeyAttributeName, DeviceMessageAttributes.newWrappedAuthenticationKeyAttributeName)
                    .map(attributeName -> propertySpecService
                            .passwordSpec()
                            .named(attributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    CHANGE_PASSWORD(DeviceMessageId.SECURITY_CHANGE_PASSWORD, "Change password"),
    CHANGE_PASSWORD_WITH_NEW_PASSWORD(DeviceMessageId.SECURITY_CHANGE_PASSWORD_WITH_NEW_PASSWORD, "Change password with value") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .passwordSpec()
                            .named(DeviceMessageAttributes.newPasswordAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    CHANGE_LLS_SECRET(DeviceMessageId.SECURITY_CHANGE_LLS_SECRET, "Change LLS secret"),
    CHANGE_LLS_SECRET_HEX(DeviceMessageId.SECURITY_CHANGE_LLS_SECRET_HEX, "Change LLS secret with value") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .hexStringSpec()
                            .named(DeviceMessageAttributes.newHexPasswordAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    CHANGE_HLS_SECRET(DeviceMessageId.SECURITY_CHANGE_HLS_SECRET, "Change HLS secret"),
    CHANGE_HLS_SECRET_HEX(DeviceMessageId.SECURITY_CHANGE_HLS_SECRET_HEX, "Change HLS secret with value") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .hexStringSpec()
                            .named(DeviceMessageAttributes.newHexPasswordAttributeName)
                                .fromThesaurus(thesaurus)
                                .markRequired()
                                .finish());
        }
    },
    CHANGE_HLS_SECRET_PASSWORD(DeviceMessageId.SECURITY_CHANGE_HLS_SECRET_WITH_PASSWORD, "Change HLS secret with new secret"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .passwordSpec()
                            .named(DeviceMessageAttributes.newPasswordAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    ACTIVATE_DEACTIVATE_TEMPORARY_ENCRYPTION_KEY(DeviceMessageId.SECURITY_ACTIVATE_DEACTIVATE_TEMPORARY_ENCRYPTION_KEY, "Enable/disable temporary encryption key") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.keyTActivationStatusAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .setDefaultValue(KeyTUsage.ENABLE.getDescription())
                            .addValues(KeyTUsage.getAllDescriptions())
                            .markExhaustive()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .boundedBigDecimalSpec(BigDecimal.ZERO, new BigDecimal(255))
                            .named(DeviceMessageAttributes.SecurityTimeDurationAttributeName)
                            .fromThesaurus(thesaurus)
                            .finish());
        }
    },
    CHANGE_EXECUTION_KEY(DeviceMessageId.SECURITY_CHANGE_EXECUTION_KEY, "Change execution key") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.executionKeyAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    CHANGE_TEMPORARY_KEY(DeviceMessageId.SECURITY_CHANGE_TEMPORARY_KEY, "Change temporary key") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.temporaryKeyAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    BREAK_OR_RESTORE_SEALS(DeviceMessageId.SECURITY_BREAK_OR_RESTORE_SEALS, "Break or restore seals") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            Stream.of(DeviceMessageAttributes.eventLogResetSealAttributeName,
                    DeviceMessageAttributes.restoreFactorySettingsSealAttributeName,
                    DeviceMessageAttributes.restoreDefaultSettingsSealAttributeName,
                    DeviceMessageAttributes.statusChangeSealAttributeName,
                    DeviceMessageAttributes.remoteConversionParametersConfigSealAttributeName,
                    DeviceMessageAttributes.remoteAnalysisParametersConfigSealAttributeName,
                    DeviceMessageAttributes.downloadProgramSealAttributeName,
                    DeviceMessageAttributes.restoreDefaultPasswordSealAttributeName)
                .map(attributeName -> propertySpecService
                        .stringSpec()
                        .named(attributeName)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .setDefaultValue(SealActions.UNCHANGED.getDescription())
                        .addValues(SealActions.getAllDescriptions())
                        .markExhaustive()
                        .finish())
                .forEach(propertySpecs::add);
        }
    },
    TEMPORARY_BREAK_SEALS(DeviceMessageId.SECURITY_TEMPORARY_BREAK_SEALS, "Temporary break the seals") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            Stream.of(DeviceMessageAttributes.eventLogResetSealBreakTimeAttributeName,
                    DeviceMessageAttributes.restoreFactorySettingsSealBreakTimeAttributeName,
                    DeviceMessageAttributes.restoreDefaultSettingsSealBreakTimeAttributeName,
                    DeviceMessageAttributes.statusChangeSealBreakTimeAttributeName,
                    DeviceMessageAttributes.remoteConversionParametersConfigSealBreakTimeAttributeName,
                    DeviceMessageAttributes.remoteAnalysisParametersConfigSealBreakTimeAttributeName,
                    DeviceMessageAttributes.downloadProgramSealBreakTimeAttributeName,
                    DeviceMessageAttributes.restoreDefaultPasswordSealBreakTimeAttributeName)
                .map(attributeName -> propertySpecService
                        .bigDecimalSpec()
                        .named(attributeName)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .setDefaultValue(BigDecimal.ZERO)
                        .finish())
                .forEach(propertySpecs::add);
        }
    },
    GENERATE_NEW_PUBLIC_KEY(DeviceMessageId.SECURITY_GENERATE_NEW_PUBLIC_KEY, "Generate new public key"),
    GENERATE_NEW_PUBLIC_KEY_FROM_RANDOM(DeviceMessageId.SECURITY_GENERATE_NEW_PUBLIC_KEY_FROM_RANDOM, "Generate new public key from random value") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .hexStringSpec()
                            .named(DeviceMessageAttributes.randomBytesAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    SET_PUBLIC_KEYS_OF_AGGREGATION_GROUP(DeviceMessageId.SECURITY_SET_PUBLIC_KEYS_OF_AGGREGATION_GROUP, "Set public key of aggreagation group") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.deviceListAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    DISABLE_DLMS_AUTHENTICATION_LEVEL_P0(DeviceMessageId.SECURITY_DISABLE_DLMS_AUTHENTICATION_LEVEL_P0, "Disable authentication level P0") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.authenticationLevelAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .addValues(DlmsAuthenticationLevelMessageValues.getNames())
                            .markExhaustive()
                            .finish());
        }
    },
    DISABLE_DLMS_AUTHENTICATION_LEVEL_P1(DeviceMessageId.SECURITY_DISABLE_DLMS_AUTHENTICATION_LEVEL_P1, "Disable authentication level P1") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.authenticationLevelAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired().addValues(DlmsAuthenticationLevelMessageValues.getNames())
                            .finish());
        }
    },
    DISABLE_DLMS_AUTHENTICATION_LEVEL_P3(DeviceMessageId.SECURITY_DISABLE_DLMS_AUTHENTICATION_LEVEL_P3, "Disable authentication level P3") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.authenticationLevelAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired().addValues(DlmsAuthenticationLevelMessageValues.getNames())
                            .finish());
        }
    },
    ENABLE_DLMS_AUTHENTICATION_LEVEL_P0(DeviceMessageId.SECURITY_ENABLE_DLMS_AUTHENTICATION_LEVEL_P0, "Enable authentication level P0") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.authenticationLevelAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired().addValues(DlmsAuthenticationLevelMessageValues.getNames())
                            .finish());
        }
    },
    ENABLE_DLMS_AUTHENTICATION_LEVEL_P1(DeviceMessageId.SECURITY_ENABLE_DLMS_AUTHENTICATION_LEVEL_P1, "Enable authentication level P1") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.authenticationLevelAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired().addValues(DlmsAuthenticationLevelMessageValues.getNames())
                            .finish());
        }
    },
    ENABLE_DLMS_AUTHENTICATION_LEVEL_P3(DeviceMessageId.SECURITY_ENABLE_DLMS_AUTHENTICATION_LEVEL_P3, "Enable authentication level P3") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.authenticationLevelAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired().addValues(DlmsAuthenticationLevelMessageValues.getNames())
                            .finish());
        }
    },
    CHANGE_HLS_SECRET_USING_SERVICE_KEY(DeviceMessageId.SECURITY_CHANGE_HLS_SECRET_USING_SERVICE_KEY, "Change HLS secret using service key") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            Stream.of(DeviceMessageAttributes.preparedDataAttributeName, DeviceMessageAttributes.signatureAttributeName)
                .map(attributeName -> propertySpecService
                        .hexStringSpec()
                        .named(attributeName)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish())
                .forEach(propertySpecs::add);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.verificationKeyAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    CHANGE_AUTHENTICATION_KEY_USING_SERVICE_KEY(DeviceMessageId.SECURITY_CHANGE_AUTHENTICATION_KEY_USING_SERVICE_KEY, "Change authentication key using service key") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            Stream.of(DeviceMessageAttributes.preparedDataAttributeName, DeviceMessageAttributes.signatureAttributeName)
                    .map(attributeName -> propertySpecService
                            .hexStringSpec()
                            .named(attributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish())
                    .forEach(propertySpecs::add);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.verificationKeyAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    CHANGE_ENCRYPTION_KEY_USING_SERVICE_KEY(DeviceMessageId.SECURITY_CHANGE_ENCRYPTION_KEY_USING_SERVICE_KEY, "Change encryption key using service key") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            Stream.of(DeviceMessageAttributes.preparedDataAttributeName, DeviceMessageAttributes.signatureAttributeName)
                    .map(attributeName -> propertySpecService
                            .hexStringSpec()
                            .named(attributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish())
                    .forEach(propertySpecs::add);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.verificationKeyAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    CHANGE_WEBPORTAL_PASSWORD1(DeviceMessageId.SECURITY_CHANGE_WEBPORTAL_PASSWORD, "Change the webportal password"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .passwordSpec()
                            .named(DeviceMessageAttributes.newPasswordAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    CHANGE_WEBPORTAL_PASSWORD2(DeviceMessageId.SECURITY_CHANGE_WEBPORTAL_PASSWORD2, "Change the webportal password 2"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .passwordSpec()
                            .named(DeviceMessageAttributes.newPasswordAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    };

    private DeviceMessageId id;
    private String defaultTranslation;

    SecurityMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultTranslation;
    }

    @Override
    public DeviceMessageId getId() {
        return this.id;
    }

    @Override
    public String getKey() {
        return SecurityMessage.class.getSimpleName() + "." + this.toString();
    }

    public final List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        this.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
        return propertySpecs;
    }

    protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        // Default behavior is not to add anything
    };

}