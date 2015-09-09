package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.scheduling.NextExecutionSpecs;

import com.elster.jupiter.time.TimeDuration;
import org.joda.time.DateTimeConstants;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates the {@link ValidNextExecutionSpecsWithMinimizeConnectionsStrategy} constraint
 * against a {@link ScheduledConnectionTaskImpl}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-13 (14:50)
 */
public class NextExecutionSpecsWithMinimizeConnectionsStrategyValidator implements ConstraintValidator<ValidNextExecutionSpecsWithMinimizeConnectionsStrategy, ScheduledConnectionTaskImpl> {

    private boolean valid;
    private ConstraintValidatorContext context;
    private ViolationMode violationMode = ViolationMode.FIRST;

    @Override
    public void initialize(ValidNextExecutionSpecsWithMinimizeConnectionsStrategy constraintAnnotation) {
        // No need to keep track of the annotation for now
    }

    @Override
    public boolean isValid(ScheduledConnectionTaskImpl connectionTask, ConstraintValidatorContext context) {
        this.context = context;
        this.valid = true;  // Optimistic
        if (ConnectionStrategy.MINIMIZE_CONNECTIONS.equals(connectionTask.getConnectionStrategy())) {
            NextExecutionSpecs nextExecutionSpecs = connectionTask.getNextExecutionSpecs();
            if (nextExecutionSpecs == null) {
                this.addViolation(MessageSeeds.OUTBOUND_CONNECTION_TASK_NEXT_EXECUTION_SPECS_REQUIRED);
            }
            else {
                this.validateNotZero(nextExecutionSpecs);
                this.validateSecondsNotAllowed(nextExecutionSpecs);
                this.validateOffsetNotBiggerThenFrequency(nextExecutionSpecs);
                this.validateOffsetWithinComWindow(nextExecutionSpecs, connectionTask.getCommunicationWindow());
                this.validateNoSimultaneousConnections(connectionTask);
            }
        }
        return this.valid;
    }

    private void validateSecondsNotAllowed(NextExecutionSpecs nextExecutionSpecs) {
        if (nextExecutionSpecs.getTemporalExpression().getEvery().getTimeUnit() == TimeDuration.TimeUnit.SECONDS) {
            this.addViolation(MessageSeeds.OUTBOUND_CONNECTION_TASK_NEXT_EXECUTION_SPECS_REQUIRED);
        }
    }

    private void validateNotZero(NextExecutionSpecs nextExecutionSpecs) {
        if (nextExecutionSpecs.getTemporalExpression().getEvery().getSeconds() == 0) {
            this.addViolation(MessageSeeds.OUTBOUND_CONNECTION_TASK_NEXT_EXECUTION_SPECS_REQUIRED);
        }
    }

    private void addViolation(MessageSeeds messageSeed) {
        this.violationMode.addViolation(this.context, messageSeed);
        this.valid = false;
        this.violationMode = ViolationMode.MORE;
    }

    private void validateOffsetNotBiggerThenFrequency(NextExecutionSpecs nextExecutionSpecs) {
        if (this.isNotNull(nextExecutionSpecs.getTemporalExpression().getOffset())) {
            if (nextExecutionSpecs.getTemporalExpression().getEvery().getSeconds() < nextExecutionSpecs.getTemporalExpression().getOffset().getSeconds()) {
                this.addViolation(MessageSeeds.OUTBOUND_CONNECTION_TASK_OFFSET_IS_BIGGER_THEN_FREQUENCY);
            }
        }
    }

    private void validateOffsetWithinComWindow(NextExecutionSpecs nextExecutionSpecs, ComWindow comWindow) {
        TimeDuration offset = nextExecutionSpecs.getTemporalExpression().getOffset();
        if (offset == null) {
            offset = new TimeDuration(0, TimeDuration.TimeUnit.SECONDS); // Midnight
        }
        if (this.isNotNull(comWindow)) {
            /* Note that it's possible that the offset is 3 days, 16 hours and 30 min
             * allowing the communication expert to specify a weekly execution
             * of the connection task on Wednesday, 16:30:00
             * So we need to truncate the offset to be within one day.
             * In the above example we would get 16:30:00 as a result
             * and we check if that is still within the ComWindow. */
            TimeDuration offsetWithinDay;
            if (this.isWithinDay(offset)) {
                offsetWithinDay = offset;
            }
            else {
                offsetWithinDay = this.truncateToDay(offset);
            }
            if (!comWindow.includes(offsetWithinDay) && this.frequencyIsAtLeastADay(nextExecutionSpecs)) {
                if (this.isWithinDay(offset)) {
                    this.addViolation(MessageSeeds.OUTBOUND_CONNECTION_TASK_OFFSET_IS_NOT_WITHIN_WINDOW);
                }
                else {
                    this.addViolation(MessageSeeds.OUTBOUND_CONNECTION_TASK_LONG_OFFSET_IS_NOT_WITHIN_WINDOW);
                }
            }
        }

    }

    private boolean frequencyIsAtLeastADay(NextExecutionSpecs nextExecutionSpecs) {
        return nextExecutionSpecs.getTemporalExpression().getEvery().getSeconds() >= TimeDuration.days(1).getSeconds();
    }

    /**
     * Tests that the {@link OutboundConnectionTask} is NOT configured
     * to allow simultaneous connections when the strategy is {@link ConnectionStrategy#MINIMIZE_CONNECTIONS}.
     *
     * @param connectionTask The OutboundConnectionTaskShadow
     */
    private void validateNoSimultaneousConnections(ScheduledConnectionTaskImpl connectionTask) {
        if (connectionTask.isSimultaneousConnectionsAllowed()) {
            this.addViolation(MessageSeeds.OUTBOUND_CONNECTION_TASK_MINIMIZE_STRATEGY_NOT_COMPATIBLE_WITH_SIMULTANEOUS_CONNECTIONS);
        }
    }

    private boolean isNotNull(ComWindow comWindow) {
        return comWindow != null;
    }

    private boolean isNotNull(TimeDuration offset) {
        return offset != null && offset.getMilliSeconds() != 0;
    }

    private boolean isWithinDay(TimeDuration timeDuration) {
        return timeDuration.getSeconds() <= DateTimeConstants.SECONDS_PER_DAY;
    }

    private TimeDuration truncateToDay(TimeDuration timeDuration) {
        return new TimeDuration(timeDuration.getSeconds() % DateTimeConstants.SECONDS_PER_DAY, TimeDuration.TimeUnit.SECONDS);
    }

    private enum ViolationMode {
        FIRST {
            @Override
            protected void addViolation(ConstraintValidatorContext context, MessageSeeds messageSeed) {
                context.disableDefaultConstraintViolation();
                context
                        .buildConstraintViolationWithTemplate("{" + messageSeed.getKey() + "}")
                        .addPropertyNode("nextExecutionSpecs").addConstraintViolation();
            }
        },
        MORE {
            @Override
            protected void addViolation(ConstraintValidatorContext context, MessageSeeds messageSeed) {
                context.buildConstraintViolationWithTemplate("{" + messageSeed.getKey() + "}");
            }
        };

        protected abstract void addViolation(ConstraintValidatorContext context, MessageSeeds messageSeed);

    }
}