package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;

import java.sql.Connection;
import java.sql.SQLException;

interface OrmClient {
    DataMapper<RecurrentTask> getRecurrentTaskFactory();
    DataMapper<TaskOccurrence> getTaskOccurrenceFactory();
    Connection getConnection() throws SQLException;

    void install();
}
