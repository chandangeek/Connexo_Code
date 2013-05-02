package com.elster.jupiter.orm.impl;

import java.security.Principal;
import java.sql.*;

import javax.sql.DataSource;

import org.osgi.service.component.annotations.*;
import org.osgi.service.event.EventAdmin;

import com.elster.jupiter.orm.*;
import com.elster.jupiter.orm.plumbing.*;
import com.elster.jupiter.security.thread.ThreadPrincipalService;

@org.osgi.service.component.annotations.Component (name = "com.elster.jupiter.orm" , service = OrmService.class)
public class OrmServiceImpl implements OrmService , ServiceLocator {
	
	private volatile OrmClient ormClient;
	private volatile DataSource dataSource;
	private volatile ThreadPrincipalService threadPrincipalService;
	private volatile EventAdmin eventAdmin;
	
	public OrmServiceImpl() {
	}
	
	
	@Override
	public Connection getConnection(boolean transactionRequired) throws SQLException {
		Connection result = dataSource.getConnection();
		if (transactionRequired && result.getAutoCommit()) {
			result.close();
			throw new TransactionRequiredException();
		}
		return result;	
	}
	
	@Override
	public DataModel getDataModel(String name) {
		try {
			return getOrmClient().getDataModelFactory().get(name);
		} catch (PersistenceException ex) {
			return null;
		}
	}
	
	// install time api
	
	@Override
	public DataModel newDataModel(String name,String description) {		
		return new DataModelImpl(name, description);
	}

	 @Override
	public void install(boolean executeDdl,boolean storeMappings) {
		 getOrmClient().install(executeDdl,storeMappings);
	}
	 
	
	@Override
	public OrmClient getOrmClient() {
		return ormClient;
	}
	
	@Override
	public EventAdmin getEventAdmin() {
		return eventAdmin;
	}

	@Override
	public Principal getPrincipal()  {
		return threadPrincipalService.getPrincipal();
	}

	@Reference
	public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
		this.threadPrincipalService = threadPrincipalService;
	}
	
	@Reference
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	@Reference
	public void setEventAdmin(EventAdmin eventAdmin) {
		this.eventAdmin = eventAdmin;
	}
	
	@Activate
	public void activate() {
		this.ormClient = new OrmClientImpl();
		Bus.setServiceLocator(this);
	}
	
	@Deactivate
	public void deActivate() {
		Bus.setServiceLocator(null);
	}
}
