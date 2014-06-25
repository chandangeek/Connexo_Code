package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.protocol.api.ConnectionType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates the {@link ComPortPoolValid} constraint against a {@link ConnectionMethodImpl}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-10 (16:14)
 */
public class ComPortPoolValidator implements ConstraintValidator<ComPortPoolValid, ConnectionTaskImpl> {

    @Override
    public void initialize(ComPortPoolValid constraintAnnotation) {
        // No need to keep track of the annotation for now
    }

    @Override
    public boolean isValid(ConnectionTaskImpl connectionTask, ConstraintValidatorContext context) {
        if(!connectionTask.isPaused()){
            if(connectionTask.getComPortPool()==null){
                context.disableDefaultConstraintViolation();
                context
                        .buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.CONNECTION_METHOD_COMPORT_POOL_REQUIRED_KEY + "}")
                        .addPropertyNode("comPortPool").addConstraintViolation();
                return false;
            } else {
                ComPortPool comPortPool = connectionTask.getComPortPool();
                ConnectionType connectionType = connectionTask.getPartialConnectionTask().getPluggableClass().getConnectionType();
                if (!connectionType.getSupportedComPortTypes().contains(comPortPool.getComPortType())) {
                    context.disableDefaultConstraintViolation();
                    context
                            .buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.COMPORT_TYPE_NOT_SUPPORTED_KEY + "}")
                            .addPropertyNode("comPortPool").addConstraintViolation();
                    return false;
                }
            }
        }
        return true;
    }

}