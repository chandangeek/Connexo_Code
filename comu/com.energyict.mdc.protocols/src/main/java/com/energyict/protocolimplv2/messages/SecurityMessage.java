package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.common.UserEnvironment;
import com.energyict.mdc.protocol.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.device.messages.DeviceMessageSpecPrimaryKey;
import com.energyict.mdc.protocol.dynamic.PropertySpec;
import com.energyict.mdc.protocol.dynamic.RequiredPropertySpecFactory;
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

    ACTIVATE_DLMS_ENCRYPTION(RequiredPropertySpecFactory.newInstance().stringPropertySpecWithValues(
            DeviceMessageConstants.encryptionLevelAttributeName,
            DlmsEncryptionLevelMessageValues.getNames())),
    CHANGE_DLMS_AUTHENTICATION_LEVEL(
            RequiredPropertySpecFactory.newInstance().stringPropertySpecWithValues(
                    DeviceMessageConstants.authenticationLevelAttributeName,
                    DlmsAuthenticationLevelMessageValues.getNames())
    ),
    CHANGE_ENCRYPTION_KEY,
    CHANGE_CLIENT_PASSWORDS(
            RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.newReadingClientPasswordAttributeName),
            RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.newManagementClientPasswordAttributeName),
            RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.newFirmwareClientPasswordAttributeName)
    ),
    WRITE_PSK(RequiredPropertySpecFactory.newInstance().hexStringPropertySpec(DeviceMessageConstants.pskAttributeName)),
    CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY(RequiredPropertySpecFactory.newInstance().passwordPropertySpec(DeviceMessageConstants.newEncryptionKeyAttributeName)),
    CHANGE_AUTHENTICATION_KEY,
    CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEY(RequiredPropertySpecFactory.newInstance().passwordPropertySpec(DeviceMessageConstants.newAuthenticationKeyAttributeName)),
    CHANGE_PASSWORD,
    CHANGE_PASSWORD_WITH_NEW_PASSWORD(RequiredPropertySpecFactory.newInstance().passwordPropertySpec(DeviceMessageConstants.newPasswordAttributeName)),   //ASCII password
    CHANGE_LLS_SECRET,
    CHANGE_LLS_SECRET_HEX(RequiredPropertySpecFactory.newInstance().hexStringPropertySpec(DeviceMessageConstants.newHexPasswordAttributeName)),               //Hex string
    CHANGE_HLS_SECRET,
    CHANGE_HLS_SECRET_HEX(RequiredPropertySpecFactory.newInstance().hexStringPropertySpec(DeviceMessageConstants.newHexPasswordAttributeName)),               //Hex string
    ACTIVATE_DEACTIVATE_TEMPORARY_ENCRYPTION_KEY(
            RequiredPropertySpecFactory.newInstance().notNullableBooleanPropertySpec(DeviceMessageConstants.keyTActivationStatusAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.SecurityTimeDurationAttributeName)),
    CHANGE_EXECUTION_KEY(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.executionKeyAttributeName)),
    CHANGE_TEMPORARY_KEY(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.temporaryKeyAttributeName)),
    BREAK_OR_RESTORE_SEALS(
            RequiredPropertySpecFactory.newInstance().booleanPropertySpec(DeviceMessageConstants.eventLogResetSealAttributeName),
            RequiredPropertySpecFactory.newInstance().booleanPropertySpec(DeviceMessageConstants.restoreFactorySettingsSealAttributeName),
            RequiredPropertySpecFactory.newInstance().booleanPropertySpec(DeviceMessageConstants.restoreDefaultSettingsSealAttributeName),
            RequiredPropertySpecFactory.newInstance().booleanPropertySpec(DeviceMessageConstants.statusChangeSealAttributeName),
            RequiredPropertySpecFactory.newInstance().booleanPropertySpec(DeviceMessageConstants.remoteConversionParametersConfigSealAttributeName),
            RequiredPropertySpecFactory.newInstance().booleanPropertySpec(DeviceMessageConstants.remoteAnalysisParametersConfigSealAttributeName),
            RequiredPropertySpecFactory.newInstance().booleanPropertySpec(DeviceMessageConstants.downloadProgramSealAttributeName),
            RequiredPropertySpecFactory.newInstance().booleanPropertySpec(DeviceMessageConstants.restoreDefaultPasswordSealAttributeName)),
    TEMPORARY_BREAK_SEALS(
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.eventLogResetSealBreakTimeAttributeName, new BigDecimal(0)),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.restoreFactorySettingsSealBreakTimeAttributeName, new BigDecimal(0)),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.restoreDefaultSettingsSealBreakTimeAttributeName, new BigDecimal(0)),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.statusChangeSealBreakTimeAttributeName, new BigDecimal(0)),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.remoteConversionParametersConfigSealBreakTimeAttributeName, new BigDecimal(0)),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.remoteAnalysisParametersConfigSealBreakTimeAttributeName, new BigDecimal(0)),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.downloadProgramSealBreakTimeAttributeName, new BigDecimal(0)),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.restoreDefaultPasswordSealBreakTimeAttributeName, new BigDecimal(0))),
    GENERATE_NEW_PUBLIC_KEY,
    GENERATE_NEW_PUBLIC_KEY_FROM_RANDOM(RequiredPropertySpecFactory.newInstance().hexStringPropertySpec(DeviceMessageConstants.randomBytesAttributeName)),
    SET_PUBLIC_KEYS_OF_AGGREGATION_GROUP(RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.deviceListAttributeName)),
    DISABLE_DLMS_AUTHENTICATION_LEVEL_P0(
            RequiredPropertySpecFactory.newInstance().stringPropertySpecWithValues(
                    DeviceMessageConstants.authenticationLevelAttributeName,
                    DlmsAuthenticationLevelMessageValues.getNames())
    ),
    DISABLE_DLMS_AUTHENTICATION_LEVEL_P1(
            RequiredPropertySpecFactory.newInstance().stringPropertySpecWithValues(
                    DeviceMessageConstants.authenticationLevelAttributeName,
                    DlmsAuthenticationLevelMessageValues.getNames())
    ),
    ENABLE_DLMS_AUTHENTICATION_LEVEL_P0(
            RequiredPropertySpecFactory.newInstance().stringPropertySpecWithValues(
                    DeviceMessageConstants.authenticationLevelAttributeName,
                    DlmsAuthenticationLevelMessageValues.getNames())
    ),
    ENABLE_DLMS_AUTHENTICATION_LEVEL_P1(
            RequiredPropertySpecFactory.newInstance().stringPropertySpecWithValues(
                    DeviceMessageConstants.authenticationLevelAttributeName,
                    DlmsAuthenticationLevelMessageValues.getNames())
    ),
    CHANGE_HLS_SECRET_USING_SERVICE_KEY(
            RequiredPropertySpecFactory.newInstance().hexStringPropertySpec(DeviceMessageConstants.preparedDataAttributeName),
            RequiredPropertySpecFactory.newInstance().hexStringPropertySpec(DeviceMessageConstants.signatureAttributeName),
            RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.verificationKeyAttributeName)
    ),
    CHANGE_AUTHENTICATION_KEY_USING_SERVICE_KEY(
            RequiredPropertySpecFactory.newInstance().hexStringPropertySpec(DeviceMessageConstants.preparedDataAttributeName),
            RequiredPropertySpecFactory.newInstance().hexStringPropertySpec(DeviceMessageConstants.signatureAttributeName),
            RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.verificationKeyAttributeName)
    ),
    CHANGE_ENCRYPTION_KEY_USING_SERVICE_KEY(
            RequiredPropertySpecFactory.newInstance().hexStringPropertySpec(DeviceMessageConstants.preparedDataAttributeName),
            RequiredPropertySpecFactory.newInstance().hexStringPropertySpec(DeviceMessageConstants.signatureAttributeName),
            RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.verificationKeyAttributeName)
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
}
