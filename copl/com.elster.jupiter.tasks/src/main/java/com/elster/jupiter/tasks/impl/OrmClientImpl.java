package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;

import java.sql.Connection;
import java.sql.SQLException;

class OrmClientImpl implements OrmClient {

    private final DataModel dataModel;

    public OrmClientImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public DataMapper<RecurrentTask> getRecurrentTaskFactory() {
        return dataModel.mapper(RecurrentTask.class);
    }

    @Override
    public DataMapper<TaskOccurrence> getTaskOccurrenceFactory() {
        return dataModel.mapper(TaskOccurrence.class);
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
