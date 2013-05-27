package com.elster.jupiter.messaging.impl;

import java.sql.Connection;
import java.sql.SQLException;

import com.elster.jupiter.messaging.QueueTable;
import com.elster.jupiter.orm.DataMapper;

public interface OrmClient {
	DataMapper<QueueTable> getQueueTableFactory();
	Connection getConnection() throws SQLException;
	void install();
}
