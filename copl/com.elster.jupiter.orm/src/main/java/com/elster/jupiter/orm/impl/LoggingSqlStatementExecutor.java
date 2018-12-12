/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.impl;

import com.elster.jupiter.util.LoggingDecorated;

import javax.inject.Named;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Logger;

public class LoggingSqlStatementExecutor implements SqlStatementExecutor {

    private final SqlStatementExecutor decorated;
    private final Logger logger;

    public LoggingSqlStatementExecutor(@LoggingDecorated SqlStatementExecutor decorated, @Named("Logger") Logger logger) {
        this.decorated = decorated;
        this.logger = logger;
    }

    @Override
    public void executeSqlStatements(Statement statement, List<String> sqlStatements) throws SQLException {

    }

}
