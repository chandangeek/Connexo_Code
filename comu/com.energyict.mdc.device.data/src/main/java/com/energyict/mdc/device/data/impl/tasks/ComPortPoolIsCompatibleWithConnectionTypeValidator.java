/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.common.comserver.ComPortPool;
import com.energyict.mdc.common.protocol.ConnectionType;
import com.energyict.mdc.device.data.impl.MessageSeeds;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates the {@link ComPortPoolIsCompatibleWithConnectionType} constraint against a {@link ConnectionTaskImpl}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-10 (16:14)
 */
public class ComPortPoolIsCompatibleWithConnectionTypeValidator implements ConstraintValidator<ComPortPoolIsCompatibleWithConnectionType, ConnectionTaskImpl> {

    @Override
    public void initialize(ComPortPoolIsCompatibleWithConnectionType constraintAnnotation) {
        // No need to keep track of the annotation for now
    }

    @Override
    public boolean isValid(ConnectionTaskImpl connectionTask, ConstraintValidatorContext context) {
        ConnectionType connectionType = connectionTask.getPluggableClass().getConnectionType();
        // No ComPortPool is validated by another annotation but not sure in which order they are executed
        if (connectionTask.hasComPortPool()) {
            ComPortPool comPortPool = connectionTask.getComPortPool();
            if (!connectionType.getSupportedComPortTypes().contains(comPortPool.getComPortType())) {
                context.disableDefaultConstraintViolation();
                context
                        .buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.COMPORT_TYPE_NOT_SUPPORTED + "}")
                        .addPropertyNode("comPortPool").addConstraintViolation();
                return false;
            }
        }
        return true;
    }

}