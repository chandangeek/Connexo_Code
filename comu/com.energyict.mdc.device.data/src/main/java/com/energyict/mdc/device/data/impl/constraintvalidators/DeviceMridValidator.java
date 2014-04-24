package com.energyict.mdc.device.data.impl.constraintvalidators;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.DeviceFields;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates that each device has a unique external name
 * <p/>
 * Copyrights EnergyICT
 * Date: 10/03/14
 * Time: 16:13
 */
public class DeviceMridValidator implements ConstraintValidator<UniqueMrid, Device> {

    private final DeviceDataService deviceDataService;
    private String message;

    @Inject
    public DeviceMridValidator(DeviceDataService deviceDataService) {
        this.deviceDataService = deviceDataService;
    }

    @Override
    public void initialize(UniqueMrid uniqueName) {
        message = uniqueName.message();
    }

    @Override
    public boolean isValid(Device device, ConstraintValidatorContext constraintValidatorContext) {
        Device other = this.deviceDataService.findByUniqueMrid(device.getmRID());
        if (other != null && other.getId() != device.getId()) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(message).addPropertyNode(DeviceFields.MRID.fieldName()).addConstraintViolation();
            return false;
        }
        return true;
    }
}
