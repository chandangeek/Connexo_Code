package com.energyict.mdc.device.data.impl.constraintvalidators;

import com.energyict.mdc.device.config.ComTaskEnablement;
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
 * Validates the {@link com.energyict.mdc.device.data.impl.constraintvalidators.ComScheduleAllowedOnDeviceConfiguration} constraint.
 */
public class ComScheduleAllowedOnDeviceConfigurationValidator implements ConstraintValidator<ComScheduleAllowedOnDeviceConfiguration, Device> {

    @Override
    public void initialize(ComScheduleAllowedOnDeviceConfiguration annotation) {}

    @Override
    public boolean isValid(Device device, ConstraintValidatorContext context) {
        List<Long> allowedComTaskIds = getAllowedComTaskIds(device);
        for (ComTaskExecution comTaskExecution : this.getValidationTargets(device)) {
            for (ComTask comTask : comTaskExecution.getComTasks()) {
                if (!allowedComTaskIds.contains(comTask.getId())) {
                    return false;
                }
            }
        }
        return true;
    }

    private List<Long> getAllowedComTaskIds(Device device) {
        List<ComTaskEnablement> comTaskEnablements = device.getDeviceConfiguration().getComTaskEnablements();
        List<Long> allowedComTaskIds = new ArrayList<>(comTaskEnablements.size());
        for (ComTaskEnablement comTaskEnablement : comTaskEnablements) {
            allowedComTaskIds.add(comTaskEnablement.getComTask().getId());
        }
        return allowedComTaskIds;
    }

    private List<ComTaskExecution> getValidationTargets (Device device) {
        List<ComTaskExecution> all = device.getComTaskExecutions();
        List<ComTaskExecution> targets = new ArrayList<>(all.size());
        for (ComTaskExecution comTaskExecution : all) {
            targets.add(comTaskExecution);
        }
        return targets;
    }
}