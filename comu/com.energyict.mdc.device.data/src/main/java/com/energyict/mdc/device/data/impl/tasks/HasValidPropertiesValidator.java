package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.common.InvalidValueException;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates the {@link HasValidProperties} constraint against a {@link ConnectionTaskImpl}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-12 (17:32)
 */
public class HasValidPropertiesValidator implements ConstraintValidator<HasValidProperties, ConnectionTaskImpl> {

    private boolean valid;

    @Override
    public void initialize(HasValidProperties constraintAnnotation) {
        // No need to keep track of the annotation for now
        this.valid = true;  // Optimistic approach
    }

    @Override
    public boolean isValid(ConnectionTaskImpl connectionTask, ConstraintValidatorContext context) {
        PartialConnectionTask partialConnectionTask = connectionTask.getPartialConnectionTask();
        ConnectionType connectionType = partialConnectionTask.getPluggableClass().getConnectionType();
        TypedProperties properties = connectionTask.getTypedProperties();
        this.validatePropertiesAreLinkedToPropertySpecs(partialConnectionTask.getPluggableClass(), properties, context);
        this.validateAllRequiredPropertiesHaveValues(connectionType, properties, partialConnectionTask.getTypedProperties(), context);
        this.validatePropertyValues(connectionType, properties, context);
        return this.valid;
    }

    private void validatePropertiesAreLinkedToPropertySpecs(ConnectionTypePluggableClass connectionTypePluggableClass, TypedProperties properties, ConstraintValidatorContext context) {
        ConnectionType connectionType = connectionTypePluggableClass.getConnectionType();
        if (!properties.localPropertyNames().isEmpty()) {
            for (String propertyName : properties.localPropertyNames()) {
                if (connectionType.getPropertySpec(propertyName) == null) {
                    context.disableDefaultConstraintViolation();
                    context
                        .buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.CONNECTION_TASK_PROPERTY_NOT_IN_SPEC_KEY + "}")
                        .addPropertyNode("properties").addConstraintViolation();
                    this.valid = false;
                }
            }
        }
    }

    private void validatePropertyValues(ConnectionType connectionType, TypedProperties properties, ConstraintValidatorContext context) {
        if (!properties.localPropertyNames().isEmpty()) {
            for (String propertyName : properties.localPropertyNames()) {
                this.validatePropertyValue(connectionType, propertyName, properties.getProperty(propertyName), context);
            }
        }
    }

    private void validatePropertyValue(ConnectionType connectionType, String propertyName, Object propertyValue, ConstraintValidatorContext context) {
        PropertySpec propertySpec=null;
        try {
            /* Not using fail-fast anymore so it is possible
             * that there is not spec for the propertyName. */
            propertySpec = connectionType.getPropertySpec(propertyName);
            if (propertySpec != null) {
                propertySpec.validateValue(propertyValue);
            }
        }
        catch (InvalidValueException e) {
            context.disableDefaultConstraintViolation();
            context
                .buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.CONNECTION_TASK_INVALID_PROPERTY_KEY + "}")
                .addPropertyNode("properties").addPropertyNode(propertySpec.getName()).addConstraintViolation();
            this.valid = false;
        }
    }

    private void validateAllRequiredPropertiesHaveValues(ConnectionType connectionType, TypedProperties properties, TypedProperties partialConnectionTaskProperties, ConstraintValidatorContext context) {
        if (!properties.localPropertyNames().isEmpty()) {
            for (PropertySpec propertySpec : this.getRequiredPropertySpecs(connectionType)) {
                String propertySpecName = propertySpec.getName();
                if (((properties.getProperty(propertySpecName) == null) && !properties.hasInheritedValueFor(propertySpecName)) && !partialConnectionTaskProperties.hasValueFor(propertySpecName)) {
                    context.disableDefaultConstraintViolation();
                    context
                        .buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.CONNECTION_TASK_REQUIRED_PROPERTY_MISSING_KEY + "}")
                        .addPropertyNode("status").addPropertyNode(propertySpecName).addConstraintViolation();
                    this.valid = false;
                }
            }
        }
    }

    private List<PropertySpec> getRequiredPropertySpecs(ConnectionType connectionType) {
        List<PropertySpec> allPropertySpecs = connectionType.getPropertySpecs();
        List<PropertySpec> requiredProperties = new ArrayList<>(allPropertySpecs.size());   // Worst case: all specs are required
        for (PropertySpec propertySpec : allPropertySpecs) {
            if (propertySpec.isRequired()) {
                requiredProperties.add(propertySpec);
            }
        }
        return requiredProperties;
    }

}