package com.elster.jupiter.messaging.impl;

import java.sql.Connection;
import java.sql.SQLException;

import com.elster.jupiter.messaging.QueueTable;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;

public class OrmClientImpl implements OrmClient {
	
	final private DataModel dataModel;
	
	public OrmClientImpl(DataModel dataModel) {
		this.dataModel = dataModel;
	}

	@Override
	public DataMapper<QueueTable> getQueueTableFactory() {
		return dataModel.getDataMapper(QueueTable.class, QueueTableImpl.class, "TO DO");
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
