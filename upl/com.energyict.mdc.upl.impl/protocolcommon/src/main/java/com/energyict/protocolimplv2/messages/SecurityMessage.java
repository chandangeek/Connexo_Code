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

    ACTIVATE_DLMS_ENCRYPTION(PropertySpecFactory.stringPropertySpecWithValues(
            DeviceMessageConstants.encryptionLevelAttributeName,
            DlmsEncryptionLevelMessageValues.getNames())),
    CHANGE_DLMS_AUTHENTICATION_LEVEL(
            PropertySpecFactory.stringPropertySpecWithValues(
                    DeviceMessageConstants.authenticationLevelAttributeName,
                    DlmsAuthenticationLevelMessageValues.getNames())
    ),
    CHANGE_ENCRYPTION_KEY,
    CHANGE_CLIENT_PASSWORDS(
            PropertySpecFactory.fixedLengthStringPropertySpec(DeviceMessageConstants.newReadingClientPasswordAttributeName, 8),
            PropertySpecFactory.fixedLengthStringPropertySpec(DeviceMessageConstants.newManagementClientPasswordAttributeName, 8),
            PropertySpecFactory.fixedLengthStringPropertySpec(DeviceMessageConstants.newFirmwareClientPasswordAttributeName, 8)
    ),
    WRITE_PSK(PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.pskAttributeName)),
    CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY(PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newEncryptionKeyAttributeName)),
    CHANGE_AUTHENTICATION_KEY,
    CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEY(PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newAuthenticationKeyAttributeName)),
    CHANGE_PASSWORD,
    CHANGE_PASSWORD_WITH_NEW_PASSWORD(PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newPasswordAttributeName)),   //ASCII password
    CHANGE_LLS_SECRET,
    CHANGE_LLS_SECRET_HEX(PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.newHexPasswordAttributeName)),               //Hex string
    CHANGE_HLS_SECRET,
    CHANGE_HLS_SECRET_HEX(PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.newHexPasswordAttributeName)),               //Hex string
    ACTIVATE_DEACTIVATE_TEMPORARY_ENCRYPTION_KEY(
            PropertySpecFactory.stringPropertySpecWithValues(
                    DeviceMessageConstants.keyTActivationStatusAttributeName,
                    DeviceMessageConstants.enableKeyTEncryptionAttributeName,
                    DeviceMessageConstants.disableKeyTEncryptionAttributeName),
            PropertySpecFactory.boundedDecimalPropertySpec(DeviceMessageConstants.SecurityTimeDurationAttributeName, new BigDecimal(0), new BigDecimal(255))),
    CHANGE_EXECUTION_KEY(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.executionKeyAttributeName)),
    CHANGE_TEMPORARY_KEY(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.temporaryKeyAttributeName)),
    BREAK_OR_RESTORE_SEALS(
            PropertySpecFactory.stringPropertySpecWithValuesAndDefaultValue(DeviceMessageConstants.eventLogResetSealAttributeName, Constants.UNCHANGED, Constants.ENABLE_SEAL, Constants.DISABLE_SEAL),
            PropertySpecFactory.stringPropertySpecWithValuesAndDefaultValue(DeviceMessageConstants.restoreFactorySettingsSealAttributeName, Constants.UNCHANGED, Constants.ENABLE_SEAL, Constants.DISABLE_SEAL),
            PropertySpecFactory.stringPropertySpecWithValuesAndDefaultValue(DeviceMessageConstants.restoreDefaultSettingsSealAttributeName, Constants.UNCHANGED, Constants.ENABLE_SEAL, Constants.DISABLE_SEAL),
            PropertySpecFactory.stringPropertySpecWithValuesAndDefaultValue(DeviceMessageConstants.statusChangeSealAttributeName, Constants.UNCHANGED, Constants.ENABLE_SEAL, Constants.DISABLE_SEAL),
            PropertySpecFactory.stringPropertySpecWithValuesAndDefaultValue(DeviceMessageConstants.remoteConversionParametersConfigSealAttributeName, Constants.UNCHANGED, Constants.ENABLE_SEAL, Constants.DISABLE_SEAL),
            PropertySpecFactory.stringPropertySpecWithValuesAndDefaultValue(DeviceMessageConstants.remoteAnalysisParametersConfigSealAttributeName, Constants.UNCHANGED, Constants.ENABLE_SEAL, Constants.DISABLE_SEAL),
            PropertySpecFactory.stringPropertySpecWithValuesAndDefaultValue(DeviceMessageConstants.downloadProgramSealAttributeName, Constants.UNCHANGED, Constants.ENABLE_SEAL, Constants.DISABLE_SEAL),
            PropertySpecFactory.stringPropertySpecWithValuesAndDefaultValue(DeviceMessageConstants.restoreDefaultPasswordSealAttributeName, Constants.UNCHANGED, Constants.ENABLE_SEAL, Constants.DISABLE_SEAL)),
    TEMPORARY_BREAK_SEALS(
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.eventLogResetSealBreakTimeAttributeName, new BigDecimal(0)),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.restoreFactorySettingsSealBreakTimeAttributeName, new BigDecimal(0)),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.restoreDefaultSettingsSealBreakTimeAttributeName, new BigDecimal(0)),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.statusChangeSealBreakTimeAttributeName, new BigDecimal(0)),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.remoteConversionParametersConfigSealBreakTimeAttributeName, new BigDecimal(0)),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.remoteAnalysisParametersConfigSealBreakTimeAttributeName, new BigDecimal(0)),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.downloadProgramSealBreakTimeAttributeName, new BigDecimal(0)),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.restoreDefaultPasswordSealBreakTimeAttributeName, new BigDecimal(0))),
    GENERATE_NEW_PUBLIC_KEY,
    GENERATE_NEW_PUBLIC_KEY_FROM_RANDOM(PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.randomBytesAttributeName)),
    SET_PUBLIC_KEYS_OF_AGGREGATION_GROUP(PropertySpecFactory.groupReferencePropertySpec(DeviceMessageConstants.deviceGroupAttributeName)),
    DISABLE_DLMS_AUTHENTICATION_LEVEL_P0(
            PropertySpecFactory.stringPropertySpecWithValues(
                    DeviceMessageConstants.authenticationLevelAttributeName,
                    DlmsAuthenticationLevelMessageValues.getNames())
    ),
    DISABLE_DLMS_AUTHENTICATION_LEVEL_P1(
            PropertySpecFactory.stringPropertySpecWithValues(
                    DeviceMessageConstants.authenticationLevelAttributeName,
                    DlmsAuthenticationLevelMessageValues.getNames())
    ),
    ENABLE_DLMS_AUTHENTICATION_LEVEL_P0(
            PropertySpecFactory.stringPropertySpecWithValues(
                    DeviceMessageConstants.authenticationLevelAttributeName,
                    DlmsAuthenticationLevelMessageValues.getNames())
    ),
    ENABLE_DLMS_AUTHENTICATION_LEVEL_P1(
            PropertySpecFactory.stringPropertySpecWithValues(
                    DeviceMessageConstants.authenticationLevelAttributeName,
                    DlmsAuthenticationLevelMessageValues.getNames())
    ),
    CHANGE_HLS_SECRET_USING_SERVICE_KEY(
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.preparedDataAttributeName),
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.signatureAttributeName),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.verificationKeyAttributeName)
    ),
    CHANGE_AUTHENTICATION_KEY_USING_SERVICE_KEY(
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.preparedDataAttributeName),
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.signatureAttributeName),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.verificationKeyAttributeName)
    ),
    CHANGE_ENCRYPTION_KEY_USING_SERVICE_KEY(
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.preparedDataAttributeName),
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.signatureAttributeName),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.verificationKeyAttributeName)
    );

    private static final DeviceMessageCategory securityCategory = DeviceMessageCategories.SECURITY;

    private final List<PropertySpec> deviceMessagePropertySpecs;

    private SecurityMessage(PropertySpec... deviceMessagePropertySpecs) {
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

    private static class Constants {
        public static final String UNCHANGED = "Unchanged";
        public static final String ENABLE_SEAL = "Enable seal";
        public static final String DISABLE_SEAL = "Disable seal";
    }
}
