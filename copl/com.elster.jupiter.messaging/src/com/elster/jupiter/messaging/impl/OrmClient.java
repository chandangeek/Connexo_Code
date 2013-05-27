package com.elster.jupiter.messaging.impl;

import java.sql.Connection;
import java.sql.SQLException;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.orm.DataMapper;

public interface OrmClient {
	DataMapper<QueueTableSpec> getQueueTableSpecFactory();
	DataMapper<DestinationSpec> getDestinationSpecFactory();
	Connection getConnection() throws SQLException;
	void install();
}
