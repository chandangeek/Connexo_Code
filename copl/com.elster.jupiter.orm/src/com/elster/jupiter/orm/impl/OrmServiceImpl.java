package com.elster.jupiter.orm.impl;

import java.security.Principal;
import java.sql.*;

import javax.sql.DataSource;

import org.osgi.service.component.annotations.*;

import com.elster.jupiter.orm.*;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.orm.plumbing.*;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.security.thread.ThreadPrincipalService;

@Component (name = "com.elster.jupiter.orm" , service = { OrmService.class , InstallService.class } , property="name=" + Bus.COMPONENTNAME)
public class OrmServiceImpl implements OrmService , InstallService , ServiceLocator {
	
	private volatile OrmClient ormClient;
	private volatile DataSource dataSource;
	private volatile ThreadPrincipalService threadPrincipalService;
	private volatile Publisher publisher;
	
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
	public void install() {
		 getOrmClient().install(true,true);
	}
	 
	
	@Override
	public OrmClient getOrmClient() {
		return ormClient;
	}
	
	@Override
	public void publish(Object event) {
		if (publisher != null) { 
			publisher.publish(event);
		}
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
	
	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
	public void setPublisher(Publisher publisher) {
		this.publisher = publisher;
	}
	
	public void unsetPublisher(Publisher publisher) {
		this.publisher = null;
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
