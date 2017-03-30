/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.constraintvalidators;

import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.impl.DeviceMessageImpl;
import com.energyict.mdc.device.data.impl.MessageSeeds;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

class UserHasTheMessagePrivilegeValidator implements ConstraintValidator<UserHasTheMessagePrivilege, DeviceMessageImpl> {

    private final DeviceMessageService deviceMessageService;

    @Inject
    UserHasTheMessagePrivilegeValidator(DeviceMessageService deviceMessageService) {
        this.deviceMessageService = deviceMessageService;
    }

    @Override
    public void initialize(UserHasTheMessagePrivilege userHasTheMessagePrivilege) {
        // nothing to initialize
    }

    @Override
    public boolean isValid(DeviceMessageImpl deviceMessage, ConstraintValidatorContext context) {
        if (!deviceMessageService.canUserAdministrateDeviceMessage(deviceMessage.getDevice().getDeviceConfiguration(), deviceMessage.getDeviceMessageId())) {
            context.disableDefaultConstraintViolation();
            context.
                    buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.DEVICE_MESSAGE_USER_NOT_ALLOWED + "}").
                    addPropertyNode(DeviceMessageImpl.Fields.USER.fieldName()).
                    addPropertyNode("device").
                    addConstraintViolation();
            return false;
        }
        return true;
    }

}