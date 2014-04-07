package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.protocol.api.ConnectionType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates the {@link ComPortPoolIsCompatibleWithConnectionType} constraint against a {@link ConnectionMethodImpl}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-10 (16:14)
 */
public class ComPortPoolIsCompatibleWithConnectionTypeValidator implements ConstraintValidator<ComPortPoolIsCompatibleWithConnectionType, ConnectionMethodImpl> {

    @Override
    public void initialize(ComPortPoolIsCompatibleWithConnectionType constraintAnnotation) {
        // No need to keep track of the annotation for now
    }

    @Override
    public boolean isValid(ConnectionMethodImpl connectionMethod, ConstraintValidatorContext context) {
        ConnectionType connectionType = connectionMethod.getPluggableClass().getConnectionType();
        // No ComPortPool is validated by another annotation but not sure in which order they are executed
        if (connectionMethod.hasComPortPool()) {
            ComPortPool comPortPool = connectionMethod.getComPortPool();
            if (!connectionType.getSupportedComPortTypes().contains(comPortPool.getComPortType())) {
                context.disableDefaultConstraintViolation();
                context
                        .buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.COMPORT_TYPE_NOT_SUPPORTED_KEY + "}")
                        .addPropertyNode("comPortPool").addConstraintViolation();
                return false;
            }
        }
        return true;
    }

}