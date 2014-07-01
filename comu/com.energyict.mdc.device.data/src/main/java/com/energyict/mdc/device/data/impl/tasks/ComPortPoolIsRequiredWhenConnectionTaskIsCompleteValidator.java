package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.device.data.tasks.ConnectionTask;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Copyrights EnergyICT
 * Date: 7/1/14
 * Time: 10:26 AM
 */
public class ComPortPoolIsRequiredWhenConnectionTaskIsCompleteValidator implements ConstraintValidator<ComPortPoolIsRequiredWhenConnectionTaskIsComplete, ConnectionMethodImpl> {

    @Override
    public void initialize(ComPortPoolIsRequiredWhenConnectionTaskIsComplete comPortPoolIsRequiredWhenConnectionTaskIsComplete) {

    }

    @Override
    public boolean isValid(ConnectionMethodImpl connectionMethod, ConstraintValidatorContext context) {
        //TODO fix this, it is not completely correct!
        if (connectionMethod.getConnectionTask() != null && !connectionMethod.getConnectionTask().getStatus().equals(ConnectionTask.ConnectionTaskLifecycleState.INCOMPLETE) && !connectionMethod.hasComPortPool()) {
            context.disableDefaultConstraintViolation();
            context
                    .buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.CONNECTION_METHOD_COMPORT_POOL_REQUIRED_KEY + "}")
                    .addPropertyNode("comPortPool").addConstraintViolation();
            return false;
        }
        return true;
    }

}
