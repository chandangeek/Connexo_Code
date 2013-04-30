package com.elster.jupiter.orm.plumbing;

import java.security.Principal;
import java.sql.Connection;
import java.sql.SQLException;

import org.osgi.service.event.EventAdmin;

public interface ServiceLocator {
	OrmClient getOrmClient();
	Connection getConnection(boolean transactionRequired) throws SQLException;
	Principal getPrincipal();
	EventAdmin getEventAdmin();
}
