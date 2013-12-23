package com.elster.jupiter.messaging.oracle.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;

import java.sql.Connection;
import java.sql.SQLException;

public class OrmClientImpl implements OrmClient {
	
	private final DataModel dataModel;
	
	public OrmClientImpl(DataModel dataModel) {
		this.dataModel = dataModel;
	}

	@Override
	public DataMapper<QueueTableSpec> getQueueTableSpecFactory() {
		return dataModel.mapper(QueueTableSpec.class);
	}

	@Override
	public DataMapper<DestinationSpec> getDestinationSpecFactory() {
		return dataModel.mapper(DestinationSpec.class);
	}
	
	@Override
	public DataMapper<SubscriberSpec> getConsumerSpecFactory() {
		return dataModel.mapper(SubscriberSpec.class);
	}
	@Override
	public Connection getConnection() throws SQLException {
		return dataModel.getConnection(false);
	}

	@Override
	public void install() {
		dataModel.install(true,true);		
	}
	
}
