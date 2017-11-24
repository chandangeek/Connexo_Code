/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.tasks.NextRecurrentTask;
import com.elster.jupiter.tasks.RecurrentTask;

import javax.inject.Inject;
import java.time.Instant;

class NextRecurrentTaskImpl implements NextRecurrentTask {

    @SuppressWarnings("unused") // Managed by ORM
    private long id;
    private long recurrentTaskId;
    private long nextRecurrentTaskId;
    private RecurrentTask recurrentTask;
    private RecurrentTask nextRecurrentTask;

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
    NextRecurrentTaskImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    static NextRecurrentTaskImpl create(DataModel dataModel, RecurrentTask recurrentTask, RecurrentTask nextRecurrentTask) {
        return dataModel.getInstance(NextRecurrentTaskImpl.class).init(recurrentTask, nextRecurrentTask);
    }

    @Override
    public RecurrentTask getRecurrentTask() {
        if (recurrentTask == null) {
            recurrentTask = dataModel.mapper(RecurrentTask.class).getExisting(recurrentTaskId);
        }
        return recurrentTask;
    }

    @Override
    public RecurrentTask getNextRecurrentTask() {
        if (nextRecurrentTask == null) {
            nextRecurrentTask = dataModel.mapper(RecurrentTask.class).getExisting(nextRecurrentTaskId);
        }
        return nextRecurrentTask;
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
            Save.CREATE.save(dataModel, this);
        } else {
            Save.UPDATE.save(dataModel, this);
        }
    }

    @Override
    public void delete() {
        dataModel.mapper(NextRecurrentTask.class).remove(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NextRecurrentTaskImpl that = (NextRecurrentTaskImpl) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    NextRecurrentTaskImpl init(RecurrentTask recurrentTask, RecurrentTask nextRecurrentTask) {
        this.recurrentTask = recurrentTask;
        this.recurrentTaskId = recurrentTask.getId();
        this.nextRecurrentTask = nextRecurrentTask;
        this.nextRecurrentTaskId = nextRecurrentTask.getId();
        return this;
    }
}
