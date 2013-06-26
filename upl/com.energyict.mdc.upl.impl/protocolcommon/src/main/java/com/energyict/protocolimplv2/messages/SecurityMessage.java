package com.energyict.protocolimplv2.messages;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cuo.core.UserEnvironment;
import com.energyict.mdc.messages.DeviceMessageCategory;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.messages.DeviceMessageSpecPrimaryKey;

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
    CHANGE_DLMS_AUTHENTICATION_LEVEL(PropertySpecFactory.stringPropertySpecWithValues(
            DeviceMessageConstants.authenticationLevelAttributeName,
            DlmsAuthenticationLevelMessageValues.getNames())),
    CHANGE_ENCRYPTION_KEY,
    CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY(PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newEncryptionKeyAttributeName)),
    CHANGE_AUTHENTICATION_KEY,
    CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEY(PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newAuthenticationKeyAttributeName)),
    CHANGE_PASSWORD,
    CHANGE_PASSWORD_WITH_NEW_PASSWORD(PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.newPasswordAttributeName)),
    ACTIVATE_DEACTIVATE_TEMPORARY_ENCRYPTION_KEY(
            PropertySpecFactory.booleanPropertySpecWithoutThreeState(DeviceMessageConstants.keyTActivationStatusAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.timeDurationAttributeName)),
    CHANGE_EXECUTION_KEY(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.executionKeyAttributeName)),
    CHANGE_TEMPORARY_KEY(PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.temporaryKeyAttributeName)),
    BREAK_OR_RESTORE_SEALS(
            PropertySpecFactory.booleanPropertySpec(DeviceMessageConstants.eventLogResetSealAttributeName),
            PropertySpecFactory.booleanPropertySpec(DeviceMessageConstants.restoreFactorySettingsSealAttributeName),
            PropertySpecFactory.booleanPropertySpec(DeviceMessageConstants.restoreDefaultSettingsSealAttributeName),
            PropertySpecFactory.booleanPropertySpec(DeviceMessageConstants.statusChangeSealAttributeName),
            PropertySpecFactory.booleanPropertySpec(DeviceMessageConstants.remoteConversionParametersConfigSealAttributeName),
            PropertySpecFactory.booleanPropertySpec(DeviceMessageConstants.remoteAnalysisParametersConfigSealAttributeName),
            PropertySpecFactory.booleanPropertySpec(DeviceMessageConstants.downloadProgramSealAttributeName),
            PropertySpecFactory.booleanPropertySpec(DeviceMessageConstants.restoreDefaultPasswordSealAttributeName)),
    TEMPORARY_BREAK_SEALS(
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.eventLogResetSealBreakTimeAttributeName, new BigDecimal(0)),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.restoreFactorySettingsSealBreakTimeAttributeName, new BigDecimal(0)),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.restoreDefaultSettingsSealBreakTimeAttributeName, new BigDecimal(0)),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.statusChangeSealBreakTimeAttributeName, new BigDecimal(0)),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.remoteConversionParametersConfigSealBreakTimeAttributeName, new BigDecimal(0)),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.remoteAnalysisParametersConfigSealBreakTimeAttributeName, new BigDecimal(0)),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.downloadProgramSealBreakTimeAttributeName, new BigDecimal(0)),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.restoreDefaultPasswordSealBreakTimeAttributeName, new BigDecimal(0)))
    ;

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
