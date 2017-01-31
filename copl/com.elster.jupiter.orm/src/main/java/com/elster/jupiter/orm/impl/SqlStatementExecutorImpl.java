/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.UnderlyingSQLFailedException;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static com.elster.jupiter.util.streams.Currying.perform;

public enum SqlStatementExecutorImpl implements SqlStatementExecutor {

    INSTANCE;

    @Override
    public void executeSqlStatements(Statement statement, List<String> sqlStatements) throws SQLException {
        sqlStatements.forEach(perform(this::executeStatement).on(statement));
    }

    private void executeStatement(Statement statement, String each) {
        try {
            statement.execute(each);
        } catch (SQLException sqe) {
            throw new UnderlyingSQLFailedException(sqe);
        }
    }

}
