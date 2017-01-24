package com.energyict.mdc.device.data.impl.constraintvalidators;

import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.tasks.ComTaskExecutionImpl;
import com.energyict.mdc.tasks.ComTask;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Validates the {@link ComTasksMustBeEnabledByDeviceConfiguration} constraint.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-07 (09:53)
 */
public class ComTasksMustBeEnabledByDeviceConfigurationValidator implements ConstraintValidator<ComTasksMustBeEnabledByDeviceConfiguration, ComTaskExecutionImpl> {

    @Override
    public void initialize(ComTasksMustBeEnabledByDeviceConfiguration constraintAnnotation) {
        // No need to store any of the settings for now
    }

    @Override
    public boolean isValid(ComTaskExecutionImpl comTaskExecution, ConstraintValidatorContext context) {
        Set<Long> enabledComTaskIds = this.getEnabledComTaskIds(comTaskExecution.getDevice());
        Set<Long> scheduledComTaskIds = this.getComTaskIds(comTaskExecution);
        return this.isSubset(scheduledComTaskIds, enabledComTaskIds);
    }

    private Set<Long> getEnabledComTaskIds(Device device) {
        Set<Long> comTaskIds = new HashSet<>();
        List<ComTaskEnablement> comTaskEnablements = device.getDeviceConfiguration().getComTaskEnablements();
        for (ComTaskEnablement comTaskEnablement : comTaskEnablements) {
            comTaskIds.add(comTaskEnablement.getComTask().getId());
        }
        return comTaskIds;
    }

    private Set<Long> getComTaskIds(ComTaskExecutionImpl comTaskExecution) {
        Set<Long> comTaskIds = new HashSet<>();
        comTaskIds.add(comTaskExecution.getComTask().getId());
        return comTaskIds;
    }

    /**
     * Tests if the first set is a subset of the second.
     *
     * @param first The first set
     * @param second The second set
     * @return true<br> iff the first set is a subset of the second
     */
    private boolean isSubset(Set<Long> first, Set<Long> second) {
        Set<Long> copyOfFirst = new HashSet<>(first);
        copyOfFirst.removeAll(second);
        return copyOfFirst.isEmpty();
    }

}