package com.energyict.mdc.device.data.impl.constraintvalidators;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceFields;
import com.energyict.mdc.device.data.DeviceService;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

/**
 * Validates that each device has a unique external name.
 * <p>
 * Copyrights EnergyICT
 * Date: 10/03/14
 * Time: 16:13
 */
public class DeviceMridValidator implements ConstraintValidator<UniqueMrid, Device> {

    private final DeviceService deviceService;

    @Inject
    public DeviceMridValidator(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Override
    public void initialize(UniqueMrid uniqueName) {
    }

    @Override
    public boolean isValid(Device device, ConstraintValidatorContext constraintValidatorContext) {
        Optional<Device> other = this.deviceService.findDeviceByMrid(device.getmRID());
        if (other.isPresent() && other.get().getId() != device.getId()) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(constraintValidatorContext.getDefaultConstraintMessageTemplate()).addPropertyNode(DeviceFields.MRID.fieldName()).addConstraintViolation();
            return false;
        }
        return true;
    }
}
