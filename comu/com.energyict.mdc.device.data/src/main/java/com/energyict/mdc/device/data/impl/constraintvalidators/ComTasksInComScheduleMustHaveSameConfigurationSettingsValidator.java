package com.energyict.mdc.device.data.impl.constraintvalidators;

import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.impl.tasks.ScheduledComTaskExecutionImpl;
import com.energyict.mdc.tasks.ComTask;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

import static com.elster.jupiter.util.Checks.is;

/**
 * Validates the {@link ComTasksInComScheduleMustHaveSameConfigurationSettings} constraint.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-05 (16:58)
 */
public class ComTasksInComScheduleMustHaveSameConfigurationSettingsValidator implements ConstraintValidator<ComTasksInComScheduleMustHaveSameConfigurationSettings, ScheduledComTaskExecutionImpl> {

    private Settings settings;

    @Override
    public void initialize(ComTasksInComScheduleMustHaveSameConfigurationSettings annotation) {
        // No need to keep track of the annotation for now
        this.settings = new Settings();
    }

    @Override
    public boolean isValid(ScheduledComTaskExecutionImpl scheduledComTaskExecution, ConstraintValidatorContext context) {
        DeviceConfiguration configuration = this.getDeviceConfiguration(scheduledComTaskExecution);
        for (ComTask comTask : scheduledComTaskExecution.getComTasks()) {
            Optional<ComTaskEnablement> comTaskEnablement = configuration.getComTaskEnablementFor(comTask);
            if (comTaskEnablement.isPresent()) {
                if (!this.settings.areSameFor(comTaskEnablement.get())) {
                    return false;
                }
            }
            // Note that ComTasks that are not enabled is validated by another component
        }
        return true;
    }

    private DeviceConfiguration getDeviceConfiguration (ScheduledComTaskExecutionImpl scheduledComTaskExecution) {
        return scheduledComTaskExecution.getDevice().getDeviceConfiguration();
    }

    private enum SettingsComparatorStrategy {
        FIRST {
            @Override
            protected boolean areSameFor(Settings settings, ComTaskEnablement comTaskEnablement) {
                if (comTaskEnablement.getPartialConnectionTask().isPresent()) {
                    settings.partialConnectionTaskId = comTaskEnablement.getPartialConnectionTask().get().getId();
                }
                settings.protocolDialectId = comTaskEnablement.getProtocolDialectConfigurationProperties().getId();
                settings.securitySetId = comTaskEnablement.getSecurityPropertySet().getId();
                settings.priority = comTaskEnablement.getPriority();
                settings.strategy = REST;
                return true;
            }
        },

        REST {
            @Override
            protected boolean areSameFor(Settings settings, ComTaskEnablement comTaskEnablement) {
                return this.samePartialConnectionTask(settings, comTaskEnablement)
                    && this.sameProtocolDialect(settings, comTaskEnablement)
                    && this.sameSecurityPropertySet(settings, comTaskEnablement)
                    && this.samePriority(settings, comTaskEnablement);
            }

            private boolean samePartialConnectionTask(Settings settings, ComTaskEnablement comTaskEnablement) {
                if (comTaskEnablement.getPartialConnectionTask().isPresent()) {
                    return is(settings.partialConnectionTaskId).equalTo(comTaskEnablement.getPartialConnectionTask().get().getId());
                }
                else {
                    return settings.partialConnectionTaskId == null;
                }
            }

            private boolean sameProtocolDialect(Settings settings, ComTaskEnablement comTaskEnablement) {
                return is(settings.protocolDialectId).equalTo(comTaskEnablement.getProtocolDialectConfigurationProperties().getId());
            }

            private boolean sameSecurityPropertySet(Settings settings, ComTaskEnablement comTaskEnablement) {
                return is(settings.securitySetId).equalTo(comTaskEnablement.getSecurityPropertySet().getId());
            }

            private boolean samePriority(Settings settings, ComTaskEnablement comTaskEnablement) {
                return is(settings.priority).equalTo(comTaskEnablement.getPriority());
            }
        };

        protected abstract boolean areSameFor(Settings settings, ComTaskEnablement comTaskEnablement);

    }

    private class Settings {
        private SettingsComparatorStrategy strategy = SettingsComparatorStrategy.FIRST;
        private Long partialConnectionTaskId = null;
        private Long protocolDialectId = null;
        private long securitySetId;
        private int priority;

        private boolean areSameFor(ComTaskEnablement comTaskEnablement) {
            return this.strategy.areSameFor(this, comTaskEnablement);
        }

    }
}