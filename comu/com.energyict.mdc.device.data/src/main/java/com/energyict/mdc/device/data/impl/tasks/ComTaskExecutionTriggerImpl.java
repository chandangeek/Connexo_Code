/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.data.impl.CreateEventType;
import com.energyict.mdc.device.data.impl.DeleteEventType;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.device.data.impl.UpdateEventType;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionTrigger;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.time.Instant;

/**
 * @author sva
 * @since 24/06/2016 - 16:59
 */
public class ComTaskExecutionTriggerImpl extends PersistentObject<ComTaskExecutionTrigger> implements ComTaskExecutionTrigger {

    public enum Fields {
        COMTASK_EXECUTION("comTaskExecution"),
        TRIGGER_TIMESTAMP("triggerTimeStamp");
        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    private Reference<ComTaskExecution> comTaskExecution = ValueReference.absent();
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    private Instant triggerTimeStamp;

    @Inject
    public ComTaskExecutionTriggerImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        super(ComTaskExecutionTrigger.class, dataModel, eventService, thesaurus);
    }

    public static ComTaskExecutionTriggerImpl from(DataModel dataModel, ComTaskExecution comTaskExecution, Instant triggerTimeStamp) {
        return dataModel.getInstance(ComTaskExecutionTriggerImpl.class).init(comTaskExecution, triggerTimeStamp);
    }

    private ComTaskExecutionTriggerImpl init(ComTaskExecution comTaskExecution, Instant triggerTimeStamp) {
        setComTaskExecution(comTaskExecution);
        setTriggerTimeStamp(triggerTimeStamp);
        return this;
    }

    @Override
    public ComTaskExecution getComTaskExecution() {
        return comTaskExecution.get();
    }

    private void setComTaskExecution(ComTaskExecution comTaskExecution) {
        this.comTaskExecution.set(comTaskExecution);
    }

    @Override
    public Instant getTriggerTimeStamp() {
        return triggerTimeStamp;
    }

    private void setTriggerTimeStamp(Instant triggerTimeStamp) {
        this.triggerTimeStamp = triggerTimeStamp;
    }

    @Override
    protected CreateEventType createEventType() {
        return CreateEventType.COMTASKEXECUTIONTRIGGER;
    }

    @Override
    protected UpdateEventType updateEventType() {
        return UpdateEventType.COMTASKEXECUTIONTRIGGER;
    }

    @Override
    protected DeleteEventType deleteEventType() {
        return DeleteEventType.COMTASKEXECUTIONTRIGGER;
    }

    @Override
    protected void doDelete() {
        this.getDataMapper().remove(this);
    }

    @Override
    protected void validateDelete() {
        // Nothing to validate
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ComTaskExecutionTriggerImpl that = (ComTaskExecutionTriggerImpl) o;
        return (comTaskExecution != null ? comTaskExecution.equals(that.comTaskExecution) : that.comTaskExecution == null) &&
                (triggerTimeStamp != null ? triggerTimeStamp.equals(that.triggerTimeStamp) : that.triggerTimeStamp == null);
    }

    @Override
    public int hashCode() {
        int result = comTaskExecution != null ? comTaskExecution.hashCode() : 0;
        result = 31 * result + (triggerTimeStamp != null ? triggerTimeStamp.hashCode() : 0);
        return result;
    }
}