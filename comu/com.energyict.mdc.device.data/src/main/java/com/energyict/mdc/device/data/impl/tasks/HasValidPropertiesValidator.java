package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

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
        if (!connectionTask.isObsolete()) {
            PartialConnectionTask partialConnectionTask = connectionTask.getPartialConnectionTask();
            ConnectionType connectionType = partialConnectionTask.getPluggableClass().getConnectionType();
            TypedProperties properties = connectionTask.getTypedProperties();
            this.validatePropertiesAreLinkedToPropertySpecs(partialConnectionTask.getPluggableClass(), properties, context);
            if (!ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE.equals(connectionTask.getStatus())) {
                this.validateAllRequiredPropertiesHaveValues(connectionType, properties, context, connectionTask);
            }
            this.validatePropertyValues(connectionType, properties, context);
        }
        return this.valid;
    }

    private boolean isValidConnectionTaskInIncompleteState(ConnectionTaskImpl connectionTaskImpl) {
        ConnectionTask.ConnectionTaskLifecycleStatus tempStatus = connectionTaskImpl.getStatus();
        connectionTaskImpl.setStatus(ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE);
        boolean validConnectionTask = connectionTaskImpl.isValidConnectionTask();
        connectionTaskImpl.setStatus(tempStatus);
        return validConnectionTask;
    }

    private void validatePropertiesAreLinkedToPropertySpecs(ConnectionTypePluggableClass connectionTypePluggableClass, TypedProperties properties, ConstraintValidatorContext context) {
        ConnectionType connectionType = connectionTypePluggableClass.getConnectionType();
        if (!properties.localPropertyNames().isEmpty()) {
            for (String propertyName : properties.localPropertyNames()) {
                if (connectionType.getPropertySpec(propertyName) == null) {
                    context.disableDefaultConstraintViolation();
                    context
                        .buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.CONNECTION_TASK_PROPERTY_NOT_IN_SPEC + "}")
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
             * that there is no spec for the propertyName. */
            propertySpec = connectionType.getPropertySpec(propertyName);
            if (propertySpec != null) {
                /**
                 * Required properties can be left empty in an incomplete state.
                 * If a required property is filled in, then it should be valid.
                 * Other properties should always be valid.
                 */
                if (!(propertySpec.isRequired() && propertyValue == null)) {
                    propertySpec.validateValue(propertyValue);
                }
            }
        }
        catch (InvalidValueException e) {
            context
                .buildConstraintViolationWithTemplate(MessageFormat.format(e.getDefaultPattern(), e.getArguments()))
                .addPropertyNode("properties").addPropertyNode(propertySpec.getName()).addConstraintViolation()
                .disableDefaultConstraintViolation();
            this.valid = false;
        }
    }

    private void validateAllRequiredPropertiesHaveValues(ConnectionType connectionType, TypedProperties properties, ConstraintValidatorContext context, ConnectionTaskImpl connectionTask) {
        boolean validConnectionTaskInIncompleteState = isValidConnectionTaskInIncompleteState(connectionTask);
        for (PropertySpec propertySpec : this.getRequiredPropertySpecs(connectionType)) {
            String propertySpecName = propertySpec.getName();
            if (validConnectionTaskInIncompleteState &&
                    (hasEmptyLocalValue(properties, propertySpecName) || !hasValue(properties, propertySpecName))) {
                context.disableDefaultConstraintViolation();
                if (connectionTask.isAllowIncomplete()) {
                    context
                        .buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.CONNECTION_TASK_REQUIRED_PROPERTY_MISSING + "}")
                        .addPropertyNode("status").addConstraintViolation();
                } else {
                    context
                        .buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.CONNECTION_TASK_REQUIRED_PROPERTY_MISSING + "}")
                        .addPropertyNode("properties").addPropertyNode(propertySpec.getName()).addConstraintViolation();
                }
                this.valid = false;
            }
        }
    }

    private boolean hasEmptyLocalValue(TypedProperties properties, String propertyName) {
        return properties.hasLocalValueFor(propertyName) && properties.getLocalValue(propertyName) == null;
    }

    private boolean hasValue(TypedProperties properties, String propertyName) {
        return properties.getProperty(propertyName) != null;
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