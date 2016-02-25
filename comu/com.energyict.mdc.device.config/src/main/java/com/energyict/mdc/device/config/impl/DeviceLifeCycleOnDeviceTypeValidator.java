package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.MicroCategory;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Copyrights EnergyICT
 * Date: 25.02.16
 * Time: 09:08
 */
public class DeviceLifeCycleOnDeviceTypeValidator implements ConstraintValidator<DeviceLifeCycleOnDeviceTypeValidation, DeviceTypeImpl> {

    @Override
    public void initialize(DeviceLifeCycleOnDeviceTypeValidation deviceLifeCycleOnDeviceTypeValidation) {

    }

    @Override
    public boolean isValid(DeviceTypeImpl deviceType, ConstraintValidatorContext constraintValidatorContext) {
        if (deviceType.isDataloggerSlave()) {
            DeviceLifeCycle deviceLifeCycle = deviceType.getDeviceLifeCycle();
            if (deviceLifeCycle != null) {
                if (deviceLifecycleContainsCommunicationRelatedActions(deviceLifeCycle) ||
                        deviceLifecycleContainsCommunicationRelatedChecks(deviceLifeCycle)) {
                    constraintValidatorContext.buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.DATALOGGER_SLAVE_LIFECYCLE_WITH_COMMUNICATION + "}")
                            .addPropertyNode(DeviceTypeImpl.Fields.DEVICE_LIFE_CYCLE.fieldName())
                            .addConstraintViolation()
                            .disableDefaultConstraintViolation();
                    return false;
                }
            }
        }
        return true;
    }

    private boolean deviceLifecycleContainsCommunicationRelatedChecks(DeviceLifeCycle deviceLifeCycle) {
        return deviceLifeCycle.getAuthorizedActions()
                .stream()
                .filter(authorizedAction -> authorizedAction instanceof AuthorizedTransitionAction)
                .flatMap(authorizedAction -> ((AuthorizedTransitionAction) authorizedAction).getChecks()
                        .stream())
                .filter(microCheck -> microCheck.getCategory().equals(MicroCategory.COMMUNICATION))
                .findAny()
                .isPresent();
    }

    private boolean deviceLifecycleContainsCommunicationRelatedActions(DeviceLifeCycle deviceLifeCycle) {
        return deviceLifeCycle.getAuthorizedActions()
                .stream()
                .filter(authorizedAction -> authorizedAction instanceof AuthorizedTransitionAction)
                .flatMap(authorizedAction1 -> ((AuthorizedTransitionAction) authorizedAction1).getActions().stream())
                .filter(microAction -> microAction.getCategory().equals(MicroCategory.COMMUNICATION))
                .findAny()
                .isPresent();
    }
}
