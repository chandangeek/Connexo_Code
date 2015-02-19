package com.energyict.mdc.scheduling.model.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.elster.jupiter.time.TemporalExpression;
import com.energyict.mdc.scheduling.events.CreateEventType;
import com.energyict.mdc.scheduling.events.DeleteEventType;
import com.energyict.mdc.scheduling.events.UpdateEventType;
import java.util.Calendar;
import java.util.Date;
import javax.inject.Inject;

/**
 * Provides an implementation for the {@link NextExecutionSpecs} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-12 (09:06)
 */
@HasValidTemporalExpression(groups = { Save.Create.class, Save.Update.class })
@OffsetNotGreaterThanFrequency(groups = { Save.Create.class, Save.Update.class })
@OnlyRegularFrequencies(groups = { Save.Create.class, Save.Update.class })
public class NextExecutionSpecsImpl extends PersistentIdObject<NextExecutionSpecs> implements NextExecutionSpecs {

    enum Fields {
        TEMPORAL_EXPRESSION("temporalExpression");
        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    private TemporalExpression temporalExpression;

    @Inject
    public NextExecutionSpecsImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        super(NextExecutionSpecs.class, dataModel, eventService, thesaurus);
    }

    NextExecutionSpecs initialize(TemporalExpression temporalExpression) {
        this.setTemporalExpression(temporalExpression);
        return this;
    }

    protected CreateEventType createEventType() {
        return CreateEventType.NEXTEXECUTIONSPECS;
    }

    protected UpdateEventType updateEventType() {
        return UpdateEventType.NEXTEXECUTIONSPECS;
    }

    protected DeleteEventType deleteEventType() {
        return DeleteEventType.NEXTEXECUTIONSPECS;
    }

    protected void validateDelete() {
        // Nothing to validate
    }

    @Override
    protected void doDelete() {
        this.getDataMapper().remove(this);
    }

    @Override
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

}