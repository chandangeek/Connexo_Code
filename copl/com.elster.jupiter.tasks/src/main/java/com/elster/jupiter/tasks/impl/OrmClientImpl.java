package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;

import java.sql.Connection;
import java.sql.SQLException;

public class OrmClientImpl implements OrmClient {

    private final DataModel dataModel;

    public OrmClientImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public DataMapper<RecurrentTask> getRecurrentTaskFactory() {
        return dataModel.getDataMapper(RecurrentTask.class, RecurrentTaskImpl.class, TableSpecs.TSK_RECURRENT_TASK.name());
    }

    @Override
    public DataMapper<TaskOccurrence> getTaskOccurrenceFactory() {
        return dataModel.getDataMapper(TaskOccurrence.class, TaskOccurrenceImpl.class, TableSpecs.TSK_TASK_OCCURRENCE.name());
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataModel.getConnection(false);
    }

    @Override
    public void install() {
        dataModel.install(true, true);
    }
}
