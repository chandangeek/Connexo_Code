package com.energyict.mdc.sap.soap.webservices.impl.custompropertyset;

import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

public class UniqueDeviceIdentifierValidator implements ConstraintValidator<UniqueDeviceIdentifier, DeviceSAPInfoDomainExtension> {
    private final SAPCustomPropertySets sapCustomPropertySets;

    @Inject
    public UniqueDeviceIdentifierValidator(SAPCustomPropertySets sapCustomPropertySets) {
        this.sapCustomPropertySets = sapCustomPropertySets;
    }

    @Override
    public void initialize(UniqueDeviceIdentifier constraintAnnotation) {
    }

    @Override
    public boolean isValid(DeviceSAPInfoDomainExtension device, ConstraintValidatorContext context) {
        Optional<String> deviceIdentifier = device.getDeviceIdentifier();
        if (deviceIdentifier.isPresent()) {
            Optional<Device> other = sapCustomPropertySets.getDevice(deviceIdentifier.get());
            if(other.isPresent() && other.get().getId() != device.getDevice().getId()) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate(this.sapCustomPropertySets.getThesaurus().getSimpleFormat(MessageSeeds.DEVICE_IDENTIFIER_MUST_BE_UNIQUE).format())
                            .addPropertyNode("deviceIdentifier")
                            .addConstraintViolation();
                    return false;
            }
        }
        return true;
    }

}