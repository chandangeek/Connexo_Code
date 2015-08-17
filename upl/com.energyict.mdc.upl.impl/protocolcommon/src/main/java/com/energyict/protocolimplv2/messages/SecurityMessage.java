package com.energyict.protocolimplv2.messages;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cuo.core.UserEnvironment;
import com.energyict.mdc.messages.DeviceMessageCategory;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.messages.DeviceMessageSpecPrimaryKey;
import com.energyict.protocolimplv2.messages.enums.DlmsAuthenticationLevelMessageValues;
import com.energyict.protocolimplv2.messages.enums.DlmsEncryptionLevelMessageValues;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;


/**
 * Provides a summary of all <i>Security</i> related messages
 * <p/>
 * Copyrights EnergyICT
 * Date: 13/03/13
 * Time: 15:18
 */
public enum SecurityMessage implements DeviceMessageSpec {

    ACTIVATE_DLMS_ENCRYPTION(0, PropertySpecFactory.stringPropertySpecWithValues(
            DeviceMessageConstants.encryptionLevelAttributeName,
            DlmsEncryptionLevelMessageValues.getNames())),
    CHANGE_DLMS_AUTHENTICATION_LEVEL(1,
            PropertySpecFactory.stringPropertySpecWithValues(
                    DeviceMessageConstants.authenticationLevelAttributeName,
                    DlmsAuthenticationLevelMessageValues.getNames())
    ),
    CHANGE_ENCRYPTION_KEY_WITH_NEW_KEYS(2,
            PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newEncryptionKeyAttributeName),
            PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newWrappedEncryptionKeyAttributeName)
    ),
    CHANGE_CLIENT_PASSWORDS(3,
            PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newReadingClientPasswordAttributeName),
            PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newManagementClientPasswordAttributeName),
            PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newFirmwareClientPasswordAttributeName)
    ),
    WRITE_PSK(4, PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.pskAttributeName)),
    CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY(5, PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newEncryptionKeyAttributeName)),
    CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEYS(6,
            PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newAuthenticationKeyAttributeName),
            PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newWrappedAuthenticationKeyAttributeName)
    ),
    CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEY(7, PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newAuthenticationKeyAttributeName)),
    CHANGE_PASSWORD(8),
    CHANGE_PASSWORD_WITH_NEW_PASSWORD(9, PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newPasswordAttributeName)),   //ASCII password
    CHANGE_LLS_SECRET(10),
    CHANGE_LLS_SECRET_HEX(11, PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newHexPasswordAttributeName)),          //Password value parsed by protocols as hex string
    @Deprecated
    /**
     * For backwards compatibility
     */
    CHANGE_HLS_SECRET(12),
    CHANGE_HLS_SECRET_HEX(13, PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newHexPasswordAttributeName)),          //Password value parsed by protocols as hex string
    ACTIVATE_DEACTIVATE_TEMPORARY_ENCRYPTION_KEY(14,
            PropertySpecFactory.stringPropertySpecWithValuesAndDefaultValue(
                    DeviceMessageConstants.keyTActivationStatusAttributeName,
                    KeyTUsage.ENABLE.getDescription(),
                    KeyTUsage.getAllDescriptions()
            ),
            PropertySpecFactory.boundedDecimalPropertySpec(DeviceMessageConstants.SecurityTimeDurationAttributeName, new BigDecimal(0), new BigDecimal(255))),
    CHANGE_EXECUTION_KEY(15, PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.executionKeyAttributeName)),
    CHANGE_TEMPORARY_KEY(16, PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.temporaryKeyAttributeName)),
    BREAK_OR_RESTORE_SEALS(17,
            PropertySpecFactory.stringPropertySpecWithValuesAndDefaultValue(DeviceMessageConstants.eventLogResetSealAttributeName, SealActions.UNCHANGED.getDescription(), SealActions.getAllDescriptions()),
            PropertySpecFactory.stringPropertySpecWithValuesAndDefaultValue(DeviceMessageConstants.restoreFactorySettingsSealAttributeName, SealActions.UNCHANGED.getDescription(), SealActions.getAllDescriptions()),
            PropertySpecFactory.stringPropertySpecWithValuesAndDefaultValue(DeviceMessageConstants.restoreDefaultSettingsSealAttributeName, SealActions.UNCHANGED.getDescription(), SealActions.getAllDescriptions()),
            PropertySpecFactory.stringPropertySpecWithValuesAndDefaultValue(DeviceMessageConstants.statusChangeSealAttributeName, SealActions.UNCHANGED.getDescription(), SealActions.getAllDescriptions()),
            PropertySpecFactory.stringPropertySpecWithValuesAndDefaultValue(DeviceMessageConstants.remoteConversionParametersConfigSealAttributeName, SealActions.UNCHANGED.getDescription(), SealActions.getAllDescriptions()),
            PropertySpecFactory.stringPropertySpecWithValuesAndDefaultValue(DeviceMessageConstants.remoteAnalysisParametersConfigSealAttributeName, SealActions.UNCHANGED.getDescription(), SealActions.getAllDescriptions()),
            PropertySpecFactory.stringPropertySpecWithValuesAndDefaultValue(DeviceMessageConstants.downloadProgramSealAttributeName, SealActions.UNCHANGED.getDescription(), SealActions.getAllDescriptions()),
            PropertySpecFactory.stringPropertySpecWithValuesAndDefaultValue(DeviceMessageConstants.restoreDefaultPasswordSealAttributeName, SealActions.UNCHANGED.getDescription(), SealActions.getAllDescriptions())),
    TEMPORARY_BREAK_SEALS(18,
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.eventLogResetSealBreakTimeAttributeName, new BigDecimal(0)),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.restoreFactorySettingsSealBreakTimeAttributeName, new BigDecimal(0)),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.restoreDefaultSettingsSealBreakTimeAttributeName, new BigDecimal(0)),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.statusChangeSealBreakTimeAttributeName, new BigDecimal(0)),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.remoteConversionParametersConfigSealBreakTimeAttributeName, new BigDecimal(0)),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.remoteAnalysisParametersConfigSealBreakTimeAttributeName, new BigDecimal(0)),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.downloadProgramSealBreakTimeAttributeName, new BigDecimal(0)),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.restoreDefaultPasswordSealBreakTimeAttributeName, new BigDecimal(0))),
    GENERATE_NEW_PUBLIC_KEY(19),
    GENERATE_NEW_PUBLIC_KEY_FROM_RANDOM(20, PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.randomBytesAttributeName)),
    SET_PUBLIC_KEYS_OF_AGGREGATION_GROUP(21, PropertySpecFactory.groupReferencePropertySpec(DeviceMessageConstants.deviceGroupAttributeName)),
    DISABLE_DLMS_AUTHENTICATION_LEVEL_P0(22,
            PropertySpecFactory.stringPropertySpecWithValues(
                    DeviceMessageConstants.authenticationLevelAttributeName,
                    DlmsAuthenticationLevelMessageValues.getNames())
    ),
    DISABLE_DLMS_AUTHENTICATION_LEVEL_P3(23,
            PropertySpecFactory.stringPropertySpecWithValues(
                    DeviceMessageConstants.authenticationLevelAttributeName,
                    DlmsAuthenticationLevelMessageValues.getNames())
    ),
    ENABLE_DLMS_AUTHENTICATION_LEVEL_P0(24,
            PropertySpecFactory.stringPropertySpecWithValues(
                    DeviceMessageConstants.authenticationLevelAttributeName,
                    DlmsAuthenticationLevelMessageValues.getNames())
    ),
    ENABLE_DLMS_AUTHENTICATION_LEVEL_P3(25,
            PropertySpecFactory.stringPropertySpecWithValues(
                    DeviceMessageConstants.authenticationLevelAttributeName,
                    DlmsAuthenticationLevelMessageValues.getNames())
    ),
    CHANGE_HLS_SECRET_USING_SERVICE_KEY(26,
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.preparedDataAttributeName),
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.signatureAttributeName),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.verificationKeyAttributeName)
    ),
    CHANGE_AUTHENTICATION_KEY_USING_SERVICE_KEY(27,
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.preparedDataAttributeName),
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.signatureAttributeName),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.verificationKeyAttributeName)
    ),
    CHANGE_ENCRYPTION_KEY_USING_SERVICE_KEY(28,
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.preparedDataAttributeName),
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.signatureAttributeName),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.verificationKeyAttributeName)
    ),
    CHANGE_WEBPORTAL_PASSWORD1(29, PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newPasswordAttributeName)),  //ASCII password
    CHANGE_WEBPORTAL_PASSWORD2(30, PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newPasswordAttributeName)),
    CHANGE_HLS_SECRET_PASSWORD(31, PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newPasswordAttributeName)), //Password field
    CHANGE_SECURITY_KEYS(32,
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.clientMacAddress),
            PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.masterKey),
            PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newAuthenticationKeyAttributeName),
            PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newEncryptionKeyAttributeName)
    ),
    ;

    private static final DeviceMessageCategory securityCategory = DeviceMessageCategories.SECURITY;

    private final List<PropertySpec> deviceMessagePropertySpecs;
    private final int id;

    private SecurityMessage(int id, PropertySpec... deviceMessagePropertySpecs) {
        this.id = id;
        this.deviceMessagePropertySpecs = Arrays.asList(deviceMessagePropertySpecs);
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return securityCategory;
    }

    @Override
    public String getName() {
        return UserEnvironment.getDefault().getTranslation(this.getNameResourceKey());
    }

    /**
     * Gets the resource key that determines the name
     * of this category to the user's language settings.
     *
     * @return The resource key
     */
    private String getNameResourceKey() {
        return SecurityMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return this.deviceMessagePropertySpecs;
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        for (PropertySpec securityProperty : getPropertySpecs()) {
            if (securityProperty.getName().equals(name)) {
                return securityProperty;
            }
        }
        return null;
    }

    @Override
    public DeviceMessageSpecPrimaryKey getPrimaryKey() {
        return new DeviceMessageSpecPrimaryKey(this, name());
    }

    @Override
    public int getMessageId() {
        return id;
    }

    public enum SealActions {

        UNCHANGED(null, "Unchanged"),
        ENABLE_SEAL(true, "Enable seal"),
        DISABLE_SEAL(false, "Disable seal");

        private final Boolean action;
        private final String description;

        SealActions(Boolean action, String description) {
            this.action = action;
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public static Boolean fromDescription(String description) {
            for (SealActions actions : values()) {
                if (actions.getDescription().equals(description)) {
                    return actions.getAction();
                }
            }
            return null;
        }

        public Boolean getAction() {
            return action;
        }

        public static String[] getAllDescriptions() {
            String[] result = new String[values().length];
            for (int index = 0; index < values().length; index++) {
                result[index] = values()[index].getDescription();
            }
            return result;
        }
    }

    public enum KeyTUsage {

        DISABLE(false, "Disabled"),
        ENABLE(true, "Enabled");

        private final boolean status;
        private final String description;

        KeyTUsage(boolean status, String description) {
            this.status = status;
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public static Boolean fromDescription(String description) {
            for (KeyTUsage usage : values()) {
                if (usage.getDescription().equals(description)) {
                    return usage.getStatus();
                }
            }
            return null;
        }

        public boolean getStatus() {
            return status;
        }

        public static String[] getAllDescriptions() {
            String[] result = new String[values().length];
            for (int index = 0; index < values().length; index++) {
                result[index] = values()[index].getDescription();
            }
            return result;
        }
    }
}
