package com.energyict.mdc.device.data.impl.constraintvalidators;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.tasks.ComTask;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Validates the {@link UniqueComTaskScheduling} constraint.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-01 (16:47)
 */
public class UniqueComTaskSchedulingValidator implements ConstraintValidator<UniqueComTaskScheduling, Device> {

    @Override
    public void initialize(UniqueComTaskScheduling annotation) {
        // No need to keep track of the annotation for now
    }

    @Override
    public boolean isValid(Device device, ConstraintValidatorContext context) {
        boolean valid = true;   // Optimistic approach ;-)
        Set<Long> comTaskIds = new HashSet<>();
        for (ComTaskExecution comTaskExecution : this.getValidationTargets(device)) {
            for (ComTask comTask : comTaskExecution.getComTasks()) {
                if (comTaskIds.contains(comTask.getId())) {
                    valid = false;
                }
                else {
                    comTaskIds.add(comTask.getId());
                }
            }
        }
        return valid;
    }

    private List<ComTaskExecution> getValidationTargets (Device device) {
        List<ComTaskExecution> all = device.getComTaskExecutions();
        List<ComTaskExecution> targets = new ArrayList<>(all.size());
        for (ComTaskExecution comTaskExecution : all) {
            if (!comTaskExecution.isAdHoc()) {
                targets.add(comTaskExecution);
            }
        }
        return targets;
    }

}