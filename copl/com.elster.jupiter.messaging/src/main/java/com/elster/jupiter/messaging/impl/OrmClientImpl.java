package com.elster.jupiter.messaging.impl;

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
		return dataModel.getDataMapper(QueueTableSpec.class, QueueTableSpecImpl.class, TableSpecs.MSG_QUEUETABLESPEC.name());
	}

	@Override
	public DataMapper<DestinationSpec> getDestinationSpecFactory() {
		return dataModel.getDataMapper(DestinationSpec.class, DestinationSpecImpl.class, TableSpecs.MSG_DESTINATIONSPEC.name());
	}
	
	@Override
	public DataMapper<SubscriberSpec> getConsumerSpecFactory() {
		return dataModel.getDataMapper(SubscriberSpec.class, SubscriberSpecImpl.class, TableSpecs.MSG_SUBSCRIBERSPEC.name());
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
