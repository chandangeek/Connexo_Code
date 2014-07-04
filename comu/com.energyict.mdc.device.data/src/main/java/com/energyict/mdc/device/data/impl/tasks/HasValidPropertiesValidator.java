package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.common.InvalidValueException;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import java.util.ArrayList;
import java.util.List;
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
        if(!connectionTask.isObsolete()){
            PartialConnectionTask partialConnectionTask = connectionTask.getPartialConnectionTask();
            ConnectionType connectionType = partialConnectionTask.getPluggableClass().getConnectionType();
            TypedProperties properties = connectionTask.getTypedProperties();
            this.validatePropertiesAreLinkedToPropertySpecs(partialConnectionTask.getPluggableClass(), properties, context);
            if(!ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE.equals(connectionTask.getStatus())){
                this.validateAllRequiredPropertiesHaveValues(connectionType, properties, partialConnectionTask.getTypedProperties(), context, connectionTask);
            }
            this.validatePropertyValues(connectionType, properties, context, connectionTask);
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
                        .buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.CONNECTION_TASK_PROPERTY_NOT_IN_SPEC_KEY + "}")
                        .addPropertyNode("properties").addConstraintViolation();
                    this.valid = false;
                }
            }
        }
    }

    private void validatePropertyValues(ConnectionType connectionType, TypedProperties properties, ConstraintValidatorContext context, ConnectionTask connectionTask) {
        if (!properties.localPropertyNames().isEmpty()) {
            for (String propertyName : properties.localPropertyNames()) {
                this.validatePropertyValue(connectionType, propertyName, properties.getProperty(propertyName), context, connectionTask);
            }
        }
    }

    private void validatePropertyValue(ConnectionType connectionType, String propertyName, Object propertyValue, ConstraintValidatorContext context, ConnectionTask connectionTask) {
        PropertySpec propertySpec=null;
        try {

            /* Not using fail-fast anymore so it is possible
             * that there is not spec for the propertyName. */
            propertySpec = connectionType.getPropertySpec(propertyName);

            if (propertySpec != null) {

                /**
                 * Required properties can be left empty in an incomplete state.
                 * If a required property is filled in, then it should be valid.
                 * Other properties should always be valid.
                 */
                if(!(propertySpec.isRequired() && propertyValue == null)){
                    propertySpec.validateValue(propertyValue);
                }
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

    private void validateAllRequiredPropertiesHaveValues(ConnectionType connectionType, TypedProperties properties, TypedProperties partialConnectionTaskProperties, ConstraintValidatorContext context, ConnectionTaskImpl connectionTask) {
        boolean validConnectionTaskInIncompleteState = isValidConnectionTaskInIncompleteState(connectionTask);
        for (PropertySpec propertySpec : this.getRequiredPropertySpecs(connectionType)) {
            String propertySpecName = propertySpec.getName();
            if (validConnectionTaskInIncompleteState && ((properties.getProperty(propertySpecName) == null) && !properties.hasInheritedValueFor(propertySpecName))) {
                context.disableDefaultConstraintViolation();
                if(connectionTask.isAllowIncomplete()){
                    context
                            .buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.CONNECTION_TASK_REQUIRED_PROPERTY_MISSING_KEY + "}")
                            .addPropertyNode("status").addConstraintViolation();
                } else {
                    context
                            .buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.CONNECTION_TASK_REQUIRED_PROPERTY_MISSING_KEY + "}")
                            .addPropertyNode("properties").addPropertyNode(propertySpec.getName()).addConstraintViolation();
                }
                this.valid = false;
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