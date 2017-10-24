/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskAdHocExecution;

import javax.inject.Inject;
import java.time.Instant;

class TaskAdHocExecutionImpl implements TaskAdHocExecution {

    @SuppressWarnings("unused") // Managed by ORM
    private long id;
    private long recurrentTaskId;
    private RecurrentTask recurrentTask;
    private Instant nextExecution;
    private Instant triggerTime;

    private final DataModel dataModel;
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

    @Inject
    TaskAdHocExecutionImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    static TaskAdHocExecutionImpl create(DataModel dataModel, RecurrentTask recurrentTask, Instant triggerTime, Instant nextExecution) {
        return dataModel.getInstance(TaskAdHocExecutionImpl.class).init(recurrentTask, triggerTime, nextExecution);
    }

    @Override
    public Instant getTriggerTime() {
        return triggerTime;
    }

    @Override
    public Instant getNextExecution() {
        return nextExecution;
    }

    @Override
    public RecurrentTask getRecurrentTask() {
        if (recurrentTask == null) {
            recurrentTask = dataModel.mapper(RecurrentTask.class).getExisting(recurrentTaskId);
        }
        return recurrentTask;
    }

    @Override
    public long getId() {
        return id;
    }

    void setId(long id) {
        this.id = id;
    }

    @Override
    public void save() {
        if (id == 0) {
            dataModel.mapper(TaskAdHocExecution.class).persist(this);
        } else {
            dataModel.mapper(TaskAdHocExecution.class).update(this);
        }
    }

    @Override
    public void delete() {
        dataModel.mapper(TaskAdHocExecution.class).remove(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TaskAdHocExecutionImpl that = (TaskAdHocExecutionImpl) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    TaskAdHocExecutionImpl init(RecurrentTask recurrentTask, Instant nextExecution, Instant triggerTime) {
        this.recurrentTask = recurrentTask;
        this.recurrentTaskId = recurrentTask.getId();
        this.triggerTime = triggerTime;
        this.nextExecution = nextExecution;
        return this;
    }
}
