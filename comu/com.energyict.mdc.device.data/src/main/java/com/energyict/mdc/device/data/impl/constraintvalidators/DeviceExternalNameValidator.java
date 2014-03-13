package com.energyict.mdc.device.data.impl.constraintvalidators;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;

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
public class DeviceExternalNameValidator implements ConstraintValidator<UniqueName, Device> {

    private final DeviceDataService deviceDataService;
    private String message;

    @Inject
    public DeviceExternalNameValidator(DeviceDataService deviceDataService) {
        this.deviceDataService = deviceDataService;
    }

    @Override
    public void initialize(UniqueName uniqueName) {
        message = uniqueName.message();
    }

    @Override
    public boolean isValid(Device device, ConstraintValidatorContext constraintValidatorContext) {
        Device other = this.deviceDataService.findDeviceByExternalName(device.getExternalName());
        if (other != null && other.getId() != device.getId()) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(message).addPropertyNode("externalName").addConstraintViolation();
            return false;
        }
        return true;
    }
}
