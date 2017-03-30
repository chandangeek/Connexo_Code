/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Map;
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
public class FirmwareCampaignsAttributesValidator implements ConstraintValidator<HasValidFirmwareCampaignAttributes, FirmwareCampaignImpl> {

    private boolean valid;

    @Override
    public void initialize(HasValidFirmwareCampaignAttributes annotation) {
        this.valid = true;  // Optimistic approach
    }

    @Override
    public boolean isValid(FirmwareCampaignImpl firmwareCampaign, ConstraintValidatorContext context) {
        Optional<DeviceMessageSpec> firmwareMessageSpec = firmwareCampaign.getFirmwareMessageSpec();
        if (firmwareMessageSpec.isPresent()) {
            List<PropertySpec> propertySpecs = firmwareMessageSpec.get().getPropertySpecs();
            Map<String, Object> savedProperties = firmwareCampaign.getProperties();
            this.validatePropertiesAreLinkedToAttributeSpecs(savedProperties, propertySpecs, context);
            this.validateRequiredPropertiesArePresent(savedProperties, propertySpecs, context);
            this.validatePropertyValues(savedProperties, propertySpecs, context);
        }
        return this.valid;
    }

    private void validatePropertiesAreLinkedToAttributeSpecs(Map<String, Object> properties, List<PropertySpec> propertySpecs, ConstraintValidatorContext context) {
        for (String propertyName : properties.keySet()) {
            if (!getPropertySpec(propertySpecs, propertyName).isPresent()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                        .addPropertyNode("properties")
                        .addPropertyNode(propertyName)
                        .addConstraintViolation();
                this.valid = false;
            }
        }
    }

    private void validateRequiredPropertiesArePresent(Map<String, Object> properties, List<PropertySpec> propertySpecs, ConstraintValidatorContext context) {
        for (PropertySpec propertySpec : propertySpecs) {
            if (propertySpec.isRequired() && !properties.containsKey(propertySpec.getName())) {
                context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                        .addPropertyNode("properties")
                        .addPropertyNode(propertySpec.getName())
                        .addConstraintViolation()
                        .disableDefaultConstraintViolation();
                this.valid = false;
            }
        }
    }

    private void validatePropertyValues(Map<String, Object> properties, List<PropertySpec> propertySpecs, ConstraintValidatorContext context) {
        for (Map.Entry<String, Object> entry: properties.entrySet()) {
            validatePropertyValue(entry.getKey(), entry.getValue(), propertySpecs, context);
        }
    }

    private void validatePropertyValue(String propertyName, Object propertyValue, List<PropertySpec> propertySpecs, ConstraintValidatorContext context) {
        Optional<PropertySpec> propertySpecRef = getPropertySpec(propertySpecs, propertyName);
        if (propertySpecRef.isPresent()) {
            try {
                propertySpecRef.get().validateValue(propertyValue);
            } catch (InvalidValueException e) {
                context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                        .addPropertyNode("properties")
                        .addPropertyNode(propertySpecRef.get().getName())
                        .addConstraintViolation()
                        .disableDefaultConstraintViolation();
                this.valid = false;
            }
        }
    }

    private Optional<PropertySpec> getPropertySpec(List<PropertySpec> propertySpecs, String name) {
        return propertySpecs
                .stream()
                .filter(propertySpec -> propertySpec.getName().equals(name))
                .findFirst();
    }
}
