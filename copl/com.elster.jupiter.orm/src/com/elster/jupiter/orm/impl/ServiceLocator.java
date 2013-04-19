package com.elster.jupiter.orm.impl;

import java.security.Principal;
import java.sql.Connection;
import java.sql.SQLException;

interface ServiceLocator {
	OrmClient getOrmClient();
	Connection getConnection(boolean transactionRequired) throws SQLException;
	Principal getPrincipal();
}
