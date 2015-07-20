package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.interval.PartialTime;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.PartialConnectionInitiationTask;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTaskBuilder;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.SchedulingService;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import org.joda.time.DateTimeConstants;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.NotNull;


/**
 * Provides an implementation for an {@link com.energyict.mdc.device.config.PartialScheduledConnectionTask}
 *
 * @author sva
 * @since 22/01/13 - 17:27
 */
@NextExecutionSpecsRequiredForMinimizeConnections(groups = {Save.Create.class, Save.Update.class})
@NextExecutionSpecsValidForComWindow(groups = {Save.Create.class, Save.Update.class})
public class PartialScheduledConnectionTaskImpl extends PartialOutboundConnectionTaskImpl implements PartialConnectionTask, PartialScheduledConnectionTask {

    private ComWindow comWindow;
    private int comWindowStart;
    private int comWindowEnd;
    @NotNull(message = '{' + MessageSeeds.Keys.CONNECTION_STRATEGY_REQUIRED + '}', groups = {Save.Create.class, Save.Update.class})
    private ConnectionStrategy connectionStrategy;
    private boolean allowSimultaneousConnections;
    private Reference<PartialConnectionInitiationTask> initiator = ValueReference.absent();

    @Inject
    PartialScheduledConnectionTaskImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, ProtocolPluggableService protocolPluggableService, SchedulingService schedulingService) {
        super(dataModel, eventService, thesaurus, protocolPluggableService, schedulingService);
    }

    static PartialScheduledConnectionTaskImpl from(DataModel dataModel, DeviceConfiguration configuration) {
        return dataModel.getInstance(PartialScheduledConnectionTaskImpl.class).init(configuration);
    }

    private PartialScheduledConnectionTaskImpl init(DeviceConfiguration configuration) {
        setConfiguration(configuration);
        return this;
    }

    @Override
    public ComWindow getCommunicationWindow() {
        if (comWindow == null) {
            comWindow = new ComWindow(PartialTime.fromMilliSeconds(comWindowStart), PartialTime.fromMilliSeconds(comWindowEnd));
        }
        return this.comWindow;
    }

    public ConnectionStrategy getConnectionStrategy() {
        return connectionStrategy;
    }

    @Override
    public PartialConnectionInitiationTask getInitiatorTask() {
        return initiator.orNull();
    }

    @Override
    public boolean isSimultaneousConnectionsAllowed() {
        return allowSimultaneousConnections;
    }

    @Override
    protected CreateEventType createEventType() {
        return CreateEventType.PARTIAL_SCHEDULED_CONNECTION_TASK;
    }

    @Override
    protected UpdateEventType updateEventType() {
        return UpdateEventType.PARTIAL_SCHEDULED_CONNECTION_TASK;
    }

    @Override
    protected ValidateDeleteEventType validateDeleteEventType() {
        return ValidateDeleteEventType.PARTIAL_SCHEDULED_CONNECTION_TASK;
    }

    @Override
    protected DeleteEventType deleteEventType() {
        return DeleteEventType.PARTIAL_SCHEDULED_CONNECTION_TASK;
    }

    @Override
    protected void doDelete() {
        this.getDataModel().mapper(PartialScheduledConnectionTaskImpl.class).remove(this);
    }

    public static PartialScheduledConnectionTaskImpl from(DataModel dataModel, ConnectionStrategy connectionStrategy) {
        return dataModel.getInstance(PartialScheduledConnectionTaskImpl.class).init(connectionStrategy);
    }

    private PartialScheduledConnectionTaskImpl init(ConnectionStrategy connectionStrategy) {
        this.connectionStrategy = connectionStrategy;
        return this;
    }

    @Override
    public void setComWindow(ComWindow comWindow) {
        this.comWindow = comWindow;
        this.comWindowStart = comWindow.getStart().getMillis();
        this.comWindowEnd = comWindow.getEnd().getMillis();
    }

    @Override
    public void setConnectionStrategy(ConnectionStrategy connectionStrategy) {
        this.connectionStrategy = connectionStrategy;
    }

    @Override
    public void setAllowSimultaneousConnections(boolean allowSimultaneousConnections) {
        this.allowSimultaneousConnections = allowSimultaneousConnections;
    }

    @Override
    public void setInitiationTask(PartialConnectionInitiationTask partialConnectionInitiationTask) {
        this.initiator.set(partialConnectionInitiationTask);
    }

    @Override
    public void setInitiationTask(PartialConnectionInitiationTaskImpl partialConnectionInitiationTask) {
        this.initiator.set(partialConnectionInitiationTask);
    }

    public static class NextExecutionSpecValidator implements ConstraintValidator<NextExecutionSpecsRequiredForMinimizeConnections, PartialScheduledConnectionTaskImpl> {

        @Inject
        public NextExecutionSpecValidator() {
        }

        @Override
        public void initialize(NextExecutionSpecsRequiredForMinimizeConnections constraintAnnotation) {
        }

        @Override
        public boolean isValid(PartialScheduledConnectionTaskImpl value, ConstraintValidatorContext context) {
            if (value.getConnectionStrategy()!=null) {
                if (ConnectionStrategy.AS_SOON_AS_POSSIBLE.equals(value.connectionStrategy) && value.getNextExecutionSpecs()!=null) {
                    context.buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.NEXT_EXECUTION_SPEC_NOT_ALLOWED_FOR_ASAP + "}").
                        addPropertyNode(Fields.NEXT_EXECUTION_SPECS.fieldName()).addConstraintViolation().disableDefaultConstraintViolation();
                    return false;
                }
                if (ConnectionStrategy.MINIMIZE_CONNECTIONS.equals(value.connectionStrategy) && value.getNextExecutionSpecs()==null) {
                    context.buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.NEXT_EXECUTION_SPEC_REQUIRED_FOR_MINIMIZE_CONNECTIONS + "}").
                        addPropertyNode(Fields.NEXT_EXECUTION_SPECS.fieldName()).addConstraintViolation().disableDefaultConstraintViolation();
                    return false;
                }
            }
            return true;
        }
    }

    public static class NextExecutionSpecVsComWindowValidator implements ConstraintValidator<NextExecutionSpecsValidForComWindow, PartialScheduledConnectionTaskImpl> {

        @Inject
        public NextExecutionSpecVsComWindowValidator() {
        }

        @Override
        public void initialize(NextExecutionSpecsValidForComWindow constraintAnnotation) {
        }

        @Override
        public boolean isValid(PartialScheduledConnectionTaskImpl value, ConstraintValidatorContext context) {
            if (value.getNextExecutionSpecs() == null || value.getCommunicationWindow() == null) {
                return true;
            }
            TemporalExpression temporalExpression = value.getNextExecutionSpecs().getTemporalExpression();
            TimeDuration offset = temporalExpression.getOffset();
            /* Note that it's possible that the offset is 3 days, 16 hours and 30 min
            * allowing the communication expert to specify a weekly execution
            * of the connection task on Wednesday, 16:30:00
            * So we need to truncate the offset to be within one day.
            * In the above example we would get 16:30:00 as a result
            * and we check if that is still within the ComWindow. */
            return offset == null || value.getCommunicationWindow().includes(truncateToDay(offset)) || isMoreFrequentThanDaily(temporalExpression);
        }

        private boolean isMoreFrequentThanDaily(TemporalExpression temporalExpression) {
            return temporalExpression.getEvery().getSeconds() > DateTimeConstants.SECONDS_PER_DAY;
        }

        private TimeDuration truncateToDay(TimeDuration timeDuration) {
            int secondsWithinDay = timeDuration.getSeconds() % DateTimeConstants.SECONDS_PER_DAY;
            return timeDuration.getSeconds() == secondsWithinDay ? timeDuration : TimeDuration.seconds(secondsWithinDay);
        }

    }

    @Override
    public PartialConnectionTask cloneForDeviceConfig(DeviceConfiguration deviceConfiguration) {
        PartialScheduledConnectionTaskBuilder builder = deviceConfiguration.newPartialScheduledConnectionTask(getName(), getPluggableClass(), getRescheduleDelay(), getConnectionStrategy());
        builder.allowSimultaneousConnections(isSimultaneousConnectionsAllowed());
        builder.asDefault(isDefault());
        builder.comWindow(new ComWindow(getCommunicationWindow().getStart(), getCommunicationWindow().getEnd()));
        builder.initiationTask(getCorrespondingConnectionInitiationTaskForDeviceConfig(deviceConfiguration));
        if (getNextExecutionSpecs() != null) {
            builder.nextExecutionSpec().temporalExpression(getNextExecutionSpecs().getTemporalExpression().getEvery(), getNextExecutionSpecs().getTemporalExpression().getOffset()).set();
        }
        getProperties().stream().forEach(partialConnectionTaskProperty -> builder.addProperty(partialConnectionTaskProperty.getName(), partialConnectionTaskProperty.getValue()));
        builder.comPortPool(getComPortPool());
        return builder.build();
    }

    private PartialConnectionInitiationTaskImpl getCorrespondingConnectionInitiationTaskForDeviceConfig(DeviceConfiguration deviceConfiguration) {
        if(getInitiatorTask() != null){
            return (PartialConnectionInitiationTaskImpl) deviceConfiguration.getPartialConnectionInitiationTasks().
                    stream().filter(partialConnectionInitiationTask ->
                    getInitiatorTask().getConnectionType().equals(partialConnectionInitiationTask.getConnectionType())).
                    findFirst().orElse(null);
        } else {
            return null;
        }
    }
}
