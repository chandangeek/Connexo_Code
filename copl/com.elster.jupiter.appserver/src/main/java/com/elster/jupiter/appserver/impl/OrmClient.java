package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.SubscriberExecutionSpec;
import com.elster.jupiter.orm.DataMapper;

import java.sql.Connection;
import java.sql.SQLException;


public interface OrmClient {

	Connection getConnection() throws SQLException;

	void install();

    DataMapper<AppServer> getAppServerFactory();

    DataMapper<SubscriberExecutionSpec> getSubscriberExecutionSpecFactory();
}
