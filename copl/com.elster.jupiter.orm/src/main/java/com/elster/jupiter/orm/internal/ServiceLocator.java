package com.elster.jupiter.orm.internal;

import com.elster.jupiter.orm.*;
import com.elster.jupiter.util.time.Clock;

import java.security.Principal;
import java.sql.Connection;
import java.sql.SQLException;

public interface ServiceLocator {
	OrmClient getOrmClient();
	Connection getConnection(boolean transactionRequired) throws SQLException;
	Principal getPrincipal();
    Clock getClock();
	Table getTable(String component, String tableName);
}
