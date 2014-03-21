package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.device.config.NextExecutionSpecs;
import com.energyict.mdc.device.config.TemporalExpression;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Calendar;
import java.util.Date;

/**
 * Provides an implementation for the {@link NextExecutionSpecs} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-12 (09:06)
 */
@HasValidTemporalExpression(groups = { Save.Create.class, Save.Update.class })
@OffsetNotGreaterThanFrequency(groups = { Save.Create.class, Save.Update.class })
public class NextExecutionSpecsImpl extends PersistentIdObject<NextExecutionSpecs> implements NextExecutionSpecs {

    private TemporalExpression temporalExpression;

    @Inject
    public NextExecutionSpecsImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        super(NextExecutionSpecs.class, dataModel, eventService, thesaurus);
    }

    NextExecutionSpecs initialize(TemporalExpression temporalExpression) {
        this.setTemporalExpression(temporalExpression);
        return this;
    }

    @Override
    protected CreateEventType createEventType() {
        return CreateEventType.NEXTEXECUTIONSPECS;
    }

    @Override
    protected UpdateEventType updateEventType() {
        return UpdateEventType.NEXTEXECUTIONSPECS;
    }

    @Override
    protected DeleteEventType deleteEventType() {
        return DeleteEventType.NEXTEXECUTIONSPECS;
    }

    @Override
    protected void validateDelete() {
        // Nothing to validate
    }

    @Override
    protected void doDelete() {
        this.getDataMapper().remove(this);
    }

    public TemporalExpression getTemporalExpression () {
        return temporalExpression;
    }

    @Override
    public void setTemporalExpression(TemporalExpression temporalExpression) {
        this.temporalExpression = temporalExpression;
    }

    @Override
    public Date getNextTimestamp (Calendar lastExecution) {
        return this.getTemporalExpression().nextOccurrence(lastExecution);
    }

    public static class OffsetVsFrequencyValidator implements ConstraintValidator<OffsetNotGreaterThanFrequency, NextExecutionSpecs> {


        @Override
        public void initialize(OffsetNotGreaterThanFrequency constraintAnnotation) {
        }

        @Override
        public boolean isValid(NextExecutionSpecs value, ConstraintValidatorContext context) {
            TimeDuration offset = value.getTemporalExpression().getOffset();
            TimeDuration every = value.getTemporalExpression().getEvery();
            return offset == null || every.getSeconds() >= offset.getSeconds();
        }
    }

}