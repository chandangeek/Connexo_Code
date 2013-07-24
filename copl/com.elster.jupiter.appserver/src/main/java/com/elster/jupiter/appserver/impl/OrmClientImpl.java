package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.SubscriberExecutionSpec;
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
	public Connection getConnection() throws SQLException {
		return dataModel.getConnection(false);
	}

	@Override
	public void install() {
		dataModel.install(true,true);		
	}

    @Override
    public DataMapper<AppServer> getAppServerFactory() {
        return dataModel.getDataMapper(AppServer.class, AppServerImpl.class, TableSpecs.APS_APPSERVER.name());
    }

    @Override
    public DataMapper<SubscriberExecutionSpec> getSubscriberExecutionSpecFactory() {
        return dataModel.getDataMapper(SubscriberExecutionSpec.class, SubscriberExecutionSpecImpl.class, TableSpecs.APS_SUBSCRIBEREXECUTIONSPEC.name());
    }

}
