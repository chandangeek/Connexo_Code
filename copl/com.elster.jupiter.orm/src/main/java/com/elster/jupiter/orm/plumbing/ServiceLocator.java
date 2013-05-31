package com.elster.jupiter.orm.plumbing;

import com.elster.jupiter.util.time.Clock;

import java.security.Principal;
import java.sql.Connection;
import java.sql.SQLException;

public interface ServiceLocator {
	OrmClient getOrmClient();
	Connection getConnection(boolean transactionRequired) throws SQLException;
	Principal getPrincipal();
	void publish(Object event);
    Clock getClock();
}
