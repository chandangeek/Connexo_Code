package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.protocol.api.ConnectionType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Copyrights EnergyICT
 * Date: 6/17/14
 * Time: 10:52 AM
 */
public class ConnectionTaskTypeDirectionValidator implements ConstraintValidator<ConnectionTypeDirectionValidForConnectionTask, PartialConnectionTask> {

    private ConnectionType.Direction direction;

    @Override
    public void initialize(ConnectionTypeDirectionValidForConnectionTask connectionTypeDirectionValidForConnectionTask) {
        direction = connectionTypeDirectionValidForConnectionTask.direction();
    }

    @Override
    public boolean isValid(PartialConnectionTask partialConnectionTask, ConstraintValidatorContext context) {
        if (partialConnectionTask.getConnectionType().getDirection() != direction) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.INCORRECT_CONNECTION_TYPE_FOR_CONNECTION_METHOD + "}")
                    .addPropertyNode("connectionType").addConstraintViolation();

            return false;
        }
        return true;
    }
}
