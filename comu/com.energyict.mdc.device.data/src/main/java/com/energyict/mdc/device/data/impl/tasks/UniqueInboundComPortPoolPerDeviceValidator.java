package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.engine.config.InboundComPortPool;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Validates the {@link UniqueInboundComPortPoolPerDevice} constraint against an {@link InboundConnectionTaskImpl}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-13 (11:25)
 */
public class UniqueInboundComPortPoolPerDeviceValidator implements ConstraintValidator<UniqueInboundComPortPoolPerDevice, InboundConnectionTaskImpl> {

    private final ConnectionTaskService connectionTaskService;

    @Inject
    public UniqueInboundComPortPoolPerDeviceValidator(ConnectionTaskService connectionTaskService) {
        super();
        this.connectionTaskService = connectionTaskService;
    }

    @Override
    public void initialize(UniqueInboundComPortPoolPerDevice constraintAnnotation) {
        // No need to keep track of the annotation for now
    }

    @Override
    public boolean isValid(InboundConnectionTaskImpl connectionTask, ConstraintValidatorContext context) {
        if (!this.onlyOneForComPortPool(connectionTask)) {
            context.disableDefaultConstraintViolation();
            context
                    .buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.CONNECTION_TASK_UNIQUE_INBOUND_COMPORT_POOL_PER_DEVICE + "}")
                    .addPropertyNode("comPortPool").addConstraintViolation();
            return false;
        } else {
            return true;
        }
    }

    private boolean onlyOneForComPortPool(InboundConnectionTaskImpl connectionTask) {
        InboundComPortPool comPortPool = connectionTask.getComPortPool();
        if (comPortPool != null) {
            Set<Long> comportPoolIds = this.getOtherComPortPoolIds(connectionTask);
            return !comportPoolIds.contains(comPortPool.getId());
        } else {
            /* Although ComPortPool is a required attribute,
             * it is not the responsibility of this component
             * to validate that. */
            return true;
        }
    }

    private Set<Long> getOtherComPortPoolIds(InboundConnectionTaskImpl connectionTask) {
        List<InboundConnectionTask> all = this.connectionTaskService.findInboundConnectionTasksByDevice(connectionTask.getDevice());
        Set<Long> poolIds = new HashSet<>();
        for (InboundConnectionTask each : all) {
            if (each.getId() != connectionTask.getId()) {
                poolIds.add(each.getComPortPool().getId());
            }
        }
        return poolIds;
    }

}