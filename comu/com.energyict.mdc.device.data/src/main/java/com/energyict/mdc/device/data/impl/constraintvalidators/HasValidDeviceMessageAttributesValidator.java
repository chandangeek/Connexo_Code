package com.energyict.mdc.device.data.impl.constraintvalidators;

import com.energyict.mdc.device.data.impl.DeviceMessageImpl;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageAttribute;

import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;

/**
 * This validator will check:
 * <ul>
 *     <li>If all given attributes exist on the DeviceMessageSpec</li>
 *     <li>If all attributes for the DeviceMessageSpec are present</li>
 *     <li>If all attributes are from the correct type</li>
 * </ul>
 *
 */
public class HasValidDeviceMessageAttributesValidator implements ConstraintValidator<HasValidDeviceMessageAttributes, DeviceMessageImpl> {

    private boolean valid;

    @Override
    public void initialize(HasValidDeviceMessageAttributes hasValidDeviceMessageAttributes) {
        // No need to keep track of the annotation for now
        this.valid = true;  // Optimistic approach
    }

    @Override
    public boolean isValid(DeviceMessageImpl deviceMessage, ConstraintValidatorContext context) {
        if(deviceMessage.getSpecification() != null){
            this.validatePropertiesAreLinkedToPropertySpecs(deviceMessage, context);
            this.validateAllAttributesArePresent(deviceMessage, context);
            this.validatePropertyValues(deviceMessage, context);
        }
        return this.valid;
    }

    private void validatePropertyValues(DeviceMessageImpl deviceMessage, ConstraintValidatorContext context) {
        List<DeviceMessageAttribute> deviceMessageAttributes = deviceMessage.getAttributes();

        deviceMessageAttributes.stream().forEach(deviceMessageAttribute -> {
            try {
                if (deviceMessageAttribute.getSpecification() != null) {
                    deviceMessageAttribute.getSpecification().validateValue(deviceMessageAttribute.getValue());
                }
            } catch (InvalidValueException e) {
                context
                    .buildConstraintViolationWithTemplate(MessageFormat.format(e.getDefaultPattern(), e.getArguments()))
                    .addPropertyNode("properties").addPropertyNode(deviceMessageAttribute.getName()).addConstraintViolation()
                    .disableDefaultConstraintViolation();
                this.valid = false;
            }
        });
    }

    private void validatePropertiesAreLinkedToPropertySpecs(DeviceMessageImpl deviceMessage, ConstraintValidatorContext context) {
        List<PropertySpec> propertySpecs = deviceMessage.getSpecification().getPropertySpecs();
        List<DeviceMessageAttribute> deviceMessageAttributes = deviceMessage.getAttributes();
        deviceMessageAttributes.stream().forEach(deviceMessageAttribute -> {
            Optional<PropertySpec> propertySpecExists = propertySpecs.stream().filter(propertySpec -> propertySpec.getName().equals(deviceMessageAttribute.getName())).findFirst();
            if (!propertySpecExists.isPresent()) {
                this.valid = false;
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.DEVICE_MESSAGE_ATTRIBUTE_NOT_IN_SPEC + "}")
                        .addPropertyNode("deviceMessageAttributes")
                        .addPropertyNode(deviceMessageAttribute.getName())
                        .addConstraintViolation();
            }
        });
    }

    private void validateAllAttributesArePresent(DeviceMessageImpl deviceMessage, ConstraintValidatorContext context) {
        List<PropertySpec> propertySpecs = deviceMessage.getSpecification().getPropertySpecs();
        List<DeviceMessageAttribute> deviceMessageAttributes = deviceMessage.getAttributes();
        if (!propertySpecs.isEmpty()) {
            propertySpecs.stream().forEach(propertySpec -> {
                Optional<DeviceMessageAttribute> deviceMessageAttributeExists = deviceMessageAttributes.stream().filter(deviceMessageAttribute -> deviceMessageAttribute.getName().equals(propertySpec.getName())).findFirst();
                if (!deviceMessageAttributeExists.isPresent()) {
                    this.valid = false;
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.DEVICE_MESSAGE_ATTRIBUTE_IS_REQUIRED + "}")
                            .addPropertyNode("deviceMessageAttributes")
                            .addPropertyNode(propertySpec.getName())
                            .addConstraintViolation();
                }
            });
        }
    }
}
