package com.elster.jupiter.messaging.impl;

import java.sql.Connection;
import java.sql.SQLException;

public interface ServiceLocator {
	Connection getConnection() throws SQLException;
	OrmClient getOrmClient();
}
