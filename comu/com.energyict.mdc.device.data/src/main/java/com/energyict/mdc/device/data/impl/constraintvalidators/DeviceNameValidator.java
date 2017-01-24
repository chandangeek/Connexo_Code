package com.energyict.mdc.device.data.impl.constraintvalidators;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceFields;
import com.energyict.mdc.device.data.DeviceService;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

/**
 * Validates that each device has a unique internal name.
 */
public class DeviceNameValidator implements ConstraintValidator<UniqueName, Device> {

    private final DeviceService deviceService;

    @Inject
    public DeviceNameValidator(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Override
    public void initialize(UniqueName uniqueName) {
    }

    @Override
    public boolean isValid(Device device, ConstraintValidatorContext constraintValidatorContext) {
        Optional<Device> other = this.deviceService.findDeviceByName(device.getName());
        if (other.isPresent() && other.get().getId() != device.getId()) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(constraintValidatorContext.getDefaultConstraintMessageTemplate())
                    .addPropertyNode(DeviceFields.NAME.fieldName())
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}