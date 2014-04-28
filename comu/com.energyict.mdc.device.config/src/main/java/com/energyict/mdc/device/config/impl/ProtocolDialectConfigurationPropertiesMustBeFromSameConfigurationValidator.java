package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.google.common.base.Optional;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static com.elster.jupiter.util.Checks.is;

/**
 * Validates the {@link ProtocolDialectConfigurationPropertiesMustBeFromSameConfiguration} constraint against a {@link ComTaskEnablementImpl}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-18 (11:36)
 */
public class ProtocolDialectConfigurationPropertiesMustBeFromSameConfigurationValidator implements ConstraintValidator<ProtocolDialectConfigurationPropertiesMustBeFromSameConfiguration, ComTaskEnablementImpl> {

    @Override
    public void initialize(ProtocolDialectConfigurationPropertiesMustBeFromSameConfiguration constraintAnnotation) {
        // No need to keep track of the annotation for now
    }

    @Override
    public boolean isValid(ComTaskEnablementImpl comTaskEnablement, ConstraintValidatorContext context) {
        Optional<ProtocolDialectConfigurationProperties> dialectConfigurationProperties = comTaskEnablement.getProtocolDialectConfigurationProperties();
        if (dialectConfigurationProperties.isPresent() && this.notSameConfiguration(dialectConfigurationProperties.get(), comTaskEnablement)) {
            context.disableDefaultConstraintViolation();
            context
                .buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode(ComTaskEnablementImpl.Fields.PROTOCOL_DIALECT_CONFIGURATION_PROPERTIES.fieldName()).addConstraintViolation();
        }
        return true;
    }

    private boolean notSameConfiguration(ProtocolDialectConfigurationProperties dialectConfigurationProperties, ComTaskEnablementImpl comTaskEnablement) {
        long dialectConfigurationId = dialectConfigurationProperties.getDeviceCommunicationConfiguration().getCommunicationConfiguration().getId();
        long configurationId = comTaskEnablement.getDeviceConfiguration().getId();
        return !is(configurationId).equalTo(dialectConfigurationId);
    }

}