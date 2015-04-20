package com.energyict.mdc.device.lifecycle.config.impl.constraints;

import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.impl.DeviceLifeCycleImpl;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

/**
 * Validates the {@link Unique} constraint against a {@link DeviceLifeCycle}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-19 (13:08)
 */
public class UniqueDeviceLifeCycleNameValidator implements ConstraintValidator<Unique, DeviceLifeCycle> {

    private final DeviceLifeCycleConfigurationService service;

    @Inject
    public UniqueDeviceLifeCycleNameValidator(DeviceLifeCycleConfigurationService service) {
        super();
        this.service = service;
    }

    @Override
    public void initialize(Unique constraintAnnotation) {
        // No need to extract information from the annotation
    }

    @Override
    public boolean isValid(DeviceLifeCycle deviceLifeCycle, ConstraintValidatorContext context) {
        Optional<DeviceLifeCycle> lifeCycle = this.service.findDeviceLifeCycleByName(deviceLifeCycle.getName());
        if (lifeCycle.isPresent() && lifeCycle.get().getId() != deviceLifeCycle.getId()){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("name")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }

}