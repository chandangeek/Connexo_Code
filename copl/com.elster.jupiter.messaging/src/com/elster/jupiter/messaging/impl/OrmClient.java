package com.elster.jupiter.messaging.impl;

import java.sql.Connection;
import java.sql.SQLException;

import com.elster.jupiter.messaging.*;
import com.elster.jupiter.orm.DataMapper;


public interface OrmClient {
	DataMapper<QueueTableSpec> getQueueTableSpecFactory();
	DataMapper<DestinationSpec> getDestinationSpecFactory();
	DataMapper<ConsumerSpec> getConsumerSpecFactory();
	Connection getConnection() throws SQLException;
	void install();
}
