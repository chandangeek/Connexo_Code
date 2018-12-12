/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.impl;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public interface SqlStatementExecutor {
    void executeSqlStatements(Statement statement, List<String> sqlStatements) throws SQLException;
}
