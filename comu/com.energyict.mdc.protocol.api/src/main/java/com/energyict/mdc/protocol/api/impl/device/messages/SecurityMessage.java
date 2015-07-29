package com.energyict.mdc.protocol.api.impl.device.messages;

import com.energyict.mdc.dynamic.HexStringFactory;
import com.energyict.mdc.dynamic.PasswordFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.device.messages.DlmsAuthenticationLevelMessageValues;
import com.energyict.mdc.protocol.api.device.messages.DlmsEncryptionLevelMessageValues;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.energyict.mdc.protocol.api.messaging.KeyTUsage;
import com.energyict.mdc.protocol.api.messaging.SealActions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.*;


/**
 * Provides a summary of all <i>Security</i> related messages.
 * <p/>
 * Copyrights EnergyICT
 * Date: 13/03/13
 * Time: 15:18
 */
public enum SecurityMessage implements DeviceMessageSpecEnum {

    ACTIVATE_DLMS_ENCRYPTION(DeviceMessageId.SECURITY_ACTIVATE_DLMS_ENCRYPTION, "Activate encryption") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.stringPropertySpecWithValues(encryptionLevelAttributeName, true, DlmsEncryptionLevelMessageValues.getNames()));
        }
    },
    CHANGE_DLMS_AUTHENTICATION_LEVEL(DeviceMessageId.SECURITY_CHANGE_DLMS_AUTHENTICATION_LEVEL, "Change authentication level") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.stringPropertySpecWithValues(authenticationLevelAttributeName, true, DlmsAuthenticationLevelMessageValues.getNames()));
        }
    },
    CHANGE_ENCRYPTION_KEY(DeviceMessageId.SECURITY_CHANGE_ENCRYPTION_KEY, "Change encryption key"),
    CHANGE_CLIENT_PASSWORDS(DeviceMessageId.SECURITY_CHANGE_CLIENT_PASSWORDS, "Change client passwords") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(newReadingClientPasswordAttributeName, true, new StringFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(newManagementClientPasswordAttributeName, true, new StringFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(newFirmwareClientPasswordAttributeName, true, new StringFactory()));
        }
    },
    WRITE_PSK(DeviceMessageId.SECURITY_WRITE_PSK, "Write PSK") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(pskAttributeName, true, new HexStringFactory()));
        }
    },
    CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY(DeviceMessageId.SECURITY_CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY, "Change encryption key with the value") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(newEncryptionKeyAttributeName, true, PasswordFactory.class));
        }
    }, CHANGE_ENCRYPTION_KEY_WITH_NEW_KEYS(DeviceMessageId.SECURITY_CHANGE_ENCRYPTION_KEY_WITH_NEW_KEYS, "Change encryption key with new values") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.newEncryptionKeyAttributeName, true, PasswordFactory.class));
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.newWrappedEncryptionKeyAttributeName, true, PasswordFactory.class));
        }
    },
    CHANGE_AUTHENTICATION_KEY(DeviceMessageId.SECURITY_CHANGE_AUTHENTICATION_KEY, "Change authentication key"),
    CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEY(DeviceMessageId.SECURITY_CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEY, "Change authentication key with the value") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(newAuthenticationKeyAttributeName, true, PasswordFactory.class));
        }
    }, CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEYS(DeviceMessageId.SECURITY_CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEYS, "Change authentication key with new values") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.newAuthenticationKeyAttributeName, true, PasswordFactory.class));
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.newWrappedAuthenticationKeyAttributeName, true, PasswordFactory.class));
        }
    },
    CHANGE_PASSWORD(DeviceMessageId.SECURITY_CHANGE_PASSWORD, "Change password"),
    CHANGE_PASSWORD_WITH_NEW_PASSWORD(DeviceMessageId.SECURITY_CHANGE_PASSWORD_WITH_NEW_PASSWORD, "Change password with value") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(newPasswordAttributeName, true, PasswordFactory.class));
        }
    },
    CHANGE_LLS_SECRET(DeviceMessageId.SECURITY_CHANGE_LLS_SECRET, "Change LLS secret"),
    CHANGE_LLS_SECRET_HEX(DeviceMessageId.SECURITY_CHANGE_LLS_SECRET_HEX, "Change LLS secret with value") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(newHexPasswordAttributeName, true, new HexStringFactory()));
        }
    },
    CHANGE_HLS_SECRET(DeviceMessageId.SECURITY_CHANGE_HLS_SECRET, "Change HLS secret"),
    CHANGE_HLS_SECRET_HEX(DeviceMessageId.SECURITY_CHANGE_HLS_SECRET_HEX, "Change HLS secret with value") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(newHexPasswordAttributeName, true, new HexStringFactory()));
        }
    },
    CHANGE_HLS_SECRET_PASSWORD(DeviceMessageId.SECURITY_CHANGE_HLS_SECRET_WITH_PASSWORD, "Change HLS secret with new secret"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(newPasswordAttributeName, true, PasswordFactory.class));
        }
    },
    ACTIVATE_DEACTIVATE_TEMPORARY_ENCRYPTION_KEY(DeviceMessageId.SECURITY_ACTIVATE_DEACTIVATE_TEMPORARY_ENCRYPTION_KEY, "Enable/disable temporary encryption key") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.stringPropertySpecWithValuesAndDefaultValue(keyTActivationStatusAttributeName, true, KeyTUsage.ENABLE.getDescription(), KeyTUsage.getAllDescriptions()));
            propertySpecs.add(propertySpecService.boundedDecimalPropertySpec(SecurityTimeDurationAttributeName, true, new BigDecimal(0), new BigDecimal(255)));
        }
    },
    CHANGE_EXECUTION_KEY(DeviceMessageId.SECURITY_CHANGE_EXECUTION_KEY, "Change execution key") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(executionKeyAttributeName, true, new StringFactory()));
        }
    },
    CHANGE_TEMPORARY_KEY(DeviceMessageId.SECURITY_CHANGE_TEMPORARY_KEY, "Change temporary key") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(temporaryKeyAttributeName, true, new StringFactory()));
        }
    },
    BREAK_OR_RESTORE_SEALS(DeviceMessageId.SECURITY_BREAK_OR_RESTORE_SEALS, "Break or restore seals") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.stringPropertySpecWithValuesAndDefaultValue(DeviceMessageConstants.eventLogResetSealAttributeName, true, SealActions.UNCHANGED.getDescription(), SealActions.getAllDescriptions()));
            propertySpecs.add(propertySpecService.stringPropertySpecWithValuesAndDefaultValue(restoreFactorySettingsSealAttributeName, true, SealActions.UNCHANGED.getDescription(), SealActions.getAllDescriptions()));
            propertySpecs.add(propertySpecService.stringPropertySpecWithValuesAndDefaultValue(restoreDefaultSettingsSealAttributeName, true, SealActions.UNCHANGED.getDescription(), SealActions.getAllDescriptions()));
            propertySpecs.add(propertySpecService.stringPropertySpecWithValuesAndDefaultValue(statusChangeSealAttributeName, true, SealActions.UNCHANGED.getDescription(), SealActions.getAllDescriptions()));
            propertySpecs.add(propertySpecService.stringPropertySpecWithValuesAndDefaultValue(remoteConversionParametersConfigSealAttributeName, true, SealActions.UNCHANGED.getDescription(), SealActions.getAllDescriptions()));
            propertySpecs.add(propertySpecService.stringPropertySpecWithValuesAndDefaultValue(remoteAnalysisParametersConfigSealAttributeName, true, SealActions.UNCHANGED.getDescription(), SealActions.getAllDescriptions()));
            propertySpecs.add(propertySpecService.stringPropertySpecWithValuesAndDefaultValue(downloadProgramSealAttributeName, true, SealActions.UNCHANGED.getDescription(), SealActions.getAllDescriptions()));
            propertySpecs.add(propertySpecService.stringPropertySpecWithValuesAndDefaultValue(restoreDefaultPasswordSealAttributeName, true, SealActions.UNCHANGED.getDescription(), SealActions.getAllDescriptions()));
        }
    },
    TEMPORARY_BREAK_SEALS(DeviceMessageId.SECURITY_TEMPORARY_BREAK_SEALS, "Temporary break the seals") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(eventLogResetSealBreakTimeAttributeName, true, BigDecimal.ZERO));
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(restoreFactorySettingsSealBreakTimeAttributeName, true, BigDecimal.ZERO));
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(restoreDefaultSettingsSealBreakTimeAttributeName, true, BigDecimal.ZERO));
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(statusChangeSealBreakTimeAttributeName, true, BigDecimal.ZERO));
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(remoteConversionParametersConfigSealBreakTimeAttributeName, true, BigDecimal.ZERO));
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(remoteAnalysisParametersConfigSealBreakTimeAttributeName, true, BigDecimal.ZERO));
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(downloadProgramSealBreakTimeAttributeName, true, BigDecimal.ZERO));
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(restoreDefaultPasswordSealBreakTimeAttributeName, true, BigDecimal.ZERO));
        }
    },
    GENERATE_NEW_PUBLIC_KEY(DeviceMessageId.SECURITY_GENERATE_NEW_PUBLIC_KEY, "Generate new public key"),
    GENERATE_NEW_PUBLIC_KEY_FROM_RANDOM(DeviceMessageId.SECURITY_GENERATE_NEW_PUBLIC_KEY_FROM_RANDOM, "Generate new public key from random value") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(randomBytesAttributeName, true, new HexStringFactory()));
        }
    },
    SET_PUBLIC_KEYS_OF_AGGREGATION_GROUP(DeviceMessageId.SECURITY_SET_PUBLIC_KEYS_OF_AGGREGATION_GROUP, "Set public key of aggreagation group") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(deviceListAttributeName, true, new StringFactory()));
        }
    },
    DISABLE_DLMS_AUTHENTICATION_LEVEL_P0(DeviceMessageId.SECURITY_DISABLE_DLMS_AUTHENTICATION_LEVEL_P0, "Disable authentication level P0") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.stringPropertySpecWithValues(authenticationLevelAttributeName, true, DlmsAuthenticationLevelMessageValues.getNames()));
        }
    },
    DISABLE_DLMS_AUTHENTICATION_LEVEL_P1(DeviceMessageId.SECURITY_DISABLE_DLMS_AUTHENTICATION_LEVEL_P1, "Disable authentication level P1") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.stringPropertySpecWithValues(authenticationLevelAttributeName, true, DlmsAuthenticationLevelMessageValues.getNames()));
        }
    },
    DISABLE_DLMS_AUTHENTICATION_LEVEL_P3(DeviceMessageId.SECURITY_DISABLE_DLMS_AUTHENTICATION_LEVEL_P3, "Disable authentication level P3") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.stringPropertySpecWithValues(authenticationLevelAttributeName, true, DlmsAuthenticationLevelMessageValues.getNames()));
        }
    },
    ENABLE_DLMS_AUTHENTICATION_LEVEL_P0(DeviceMessageId.SECURITY_ENABLE_DLMS_AUTHENTICATION_LEVEL_P0, "Enable authentication level P0") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.stringPropertySpecWithValues(authenticationLevelAttributeName, true, DlmsAuthenticationLevelMessageValues.getNames()));
        }
    },
    ENABLE_DLMS_AUTHENTICATION_LEVEL_P1(DeviceMessageId.SECURITY_ENABLE_DLMS_AUTHENTICATION_LEVEL_P1, "Enable authentication level P1") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.stringPropertySpecWithValues(authenticationLevelAttributeName, true, DlmsAuthenticationLevelMessageValues.getNames()));
        }
    },
    ENABLE_DLMS_AUTHENTICATION_LEVEL_P3(DeviceMessageId.SECURITY_ENABLE_DLMS_AUTHENTICATION_LEVEL_P3, "Enable authentication level P3") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.stringPropertySpecWithValues(authenticationLevelAttributeName, true, DlmsAuthenticationLevelMessageValues.getNames()));
        }
    },
    CHANGE_HLS_SECRET_USING_SERVICE_KEY(DeviceMessageId.SECURITY_CHANGE_HLS_SECRET_USING_SERVICE_KEY, "Change HLS secret using service key") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            HexStringFactory factory = new HexStringFactory();
            propertySpecs.add(propertySpecService.basicPropertySpec(preparedDataAttributeName, true, factory));
            propertySpecs.add(propertySpecService.basicPropertySpec(signatureAttributeName, true, factory));
            propertySpecs.add(propertySpecService.basicPropertySpec(verificationKeyAttributeName, true, new StringFactory()));
        }
    },
    CHANGE_AUTHENTICATION_KEY_USING_SERVICE_KEY(DeviceMessageId.SECURITY_CHANGE_AUTHENTICATION_KEY_USING_SERVICE_KEY, "Change authentication key using service key") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            HexStringFactory factory = new HexStringFactory();
            propertySpecs.add(propertySpecService.basicPropertySpec(preparedDataAttributeName, true, factory));
            propertySpecs.add(propertySpecService.basicPropertySpec(signatureAttributeName, true, factory));
            propertySpecs.add(propertySpecService.basicPropertySpec(verificationKeyAttributeName, true, new StringFactory()));
        }
    },
    CHANGE_ENCRYPTION_KEY_USING_SERVICE_KEY(DeviceMessageId.SECURITY_CHANGE_ENCRYPTION_KEY_USING_SERVICE_KEY, "Change encryption key using service key") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            HexStringFactory factory = new HexStringFactory();
            propertySpecs.add(propertySpecService.basicPropertySpec(preparedDataAttributeName, true, factory));
            propertySpecs.add(propertySpecService.basicPropertySpec(signatureAttributeName, true, factory));
            propertySpecs.add(propertySpecService.basicPropertySpec(verificationKeyAttributeName, true, new StringFactory()));
        }
    },
    CHANGE_WEBPORTAL_PASSWORD1(DeviceMessageId.SECURITY_CHANGE_WEBPORTAL_PASSWORD, "Change the webportal password"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(newPasswordAttributeName, true, PasswordFactory.class));
        }
    },
    CHANGE_WEBPORTAL_PASSWORD2(DeviceMessageId.SECURITY_CHANGE_WEBPORTAL_PASSWORD2, "Change the webportal password 2"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(newPasswordAttributeName, true, PasswordFactory.class));
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

    public final List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService) {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        this.addPropertySpecs(propertySpecs, propertySpecService);
        return propertySpecs;
    }

    protected void addPropertySpecs (List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
        // Default behavior is not to add anything
    };

    public final PropertySpec getPropertySpec(String name, PropertySpecService propertySpecService) {
        for (PropertySpec securityProperty : getPropertySpecs(propertySpecService)) {
            if (securityProperty.getName().equals(name)) {
                return securityProperty;
            }
        }
        return null;
    }

}