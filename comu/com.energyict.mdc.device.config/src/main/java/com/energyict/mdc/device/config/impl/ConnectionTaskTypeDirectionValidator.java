/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.common.protocol.ConnectionType;
import com.energyict.mdc.common.tasks.PartialConnectionTask;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ConnectionTaskTypeDirectionValidator implements ConstraintValidator<ConnectionTypeDirectionValidForConnectionTask, PartialConnectionTask> {

    private ConnectionType.ConnectionTypeDirection connectionTypeDirection;

    @Override
    public void initialize(ConnectionTypeDirectionValidForConnectionTask connectionTypeDirectionValidForConnectionTask) {
        connectionTypeDirection = connectionTypeDirectionValidForConnectionTask.direction();
    }

    @Override
    public boolean isValid(PartialConnectionTask partialConnectionTask, ConstraintValidatorContext context) {
        if (partialConnectionTask.getConnectionType().getDirection() != connectionTypeDirection) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.INCORRECT_CONNECTION_TYPE_FOR_CONNECTION_METHOD + "}")
                    .addPropertyNode("connectionType").addConstraintViolation();

            return false;
        }
        return true;
    }
}
