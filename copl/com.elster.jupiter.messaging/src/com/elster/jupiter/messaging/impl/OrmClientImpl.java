package com.elster.jupiter.messaging.impl;

import java.sql.Connection;
import java.sql.SQLException;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;

public class OrmClientImpl implements OrmClient {
	
	final private DataModel dataModel;
	
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
	public Connection getConnection() throws SQLException {
		return dataModel.getConnection(false);
	}

	
	@Override
	public void install() {
		dataModel.install(true,true);		
	}
	
}
