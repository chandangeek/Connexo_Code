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
        ConnectionTask connectionTask = connectionMethod.getConnectionTask();
        if (!connectionTask.getStatus().equals(ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE) && !connectionMethod.hasComPortPool()) {
            context.disableDefaultConstraintViolation();
            if (((ConnectionTaskImpl) connectionTask).isAllowIncomplete() && isValidConnectionTaskInIncompleteState((ConnectionTaskImpl) connectionTask)) {
                    context.buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.CONNECTION_METHOD_COMPORT_POOL_REQUIRED_KEY + "}")
                            .addPropertyNode("status").addConstraintViolation();
                } else {
                    context
                            .buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.CONNECTION_METHOD_COMPORT_POOL_REQUIRED_KEY + "}")
                            .addPropertyNode("comPortPool").addConstraintViolation();
            }
            return false;
        }
        return true;
    }

    private boolean isValidConnectionTaskInIncompleteState(ConnectionTaskImpl connectionTaskImpl) {
        ConnectionTask.ConnectionTaskLifecycleStatus tempStatus = connectionTaskImpl.getStatus();
        connectionTaskImpl.setStatus(ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE);
        boolean validConnectionTask = connectionTaskImpl.isValidConnectionTask();
        connectionTaskImpl.setStatus(tempStatus);
        return validConnectionTask;
    }

}
