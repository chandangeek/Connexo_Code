package com.elster.jupiter.orm.impl;

import java.util.*;
import java.security.Principal;
import java.sql.*;

import javax.sql.DataSource;

import org.osgi.service.component.annotations.*;
import org.osgi.service.event.EventAdmin;

import com.elster.jupiter.orm.*;
import com.elster.jupiter.orm.Component;
import com.elster.jupiter.orm.plumbing.Bus;
import com.elster.jupiter.orm.plumbing.OrmClient;
import com.elster.jupiter.orm.plumbing.OrmClientImpl;
import com.elster.jupiter.orm.plumbing.ServiceLocator;
import com.elster.jupiter.security.thread.ThreadPrincipalService;

@org.osgi.service.component.annotations.Component (name = "com.elster.jupiter.orm" , service = OrmService.class)
public class OrmServiceImpl implements OrmService , ServiceLocator {
	
	private Map<String,Component> components = Collections.synchronizedMap(new HashMap<String,Component>());
	private volatile OrmClient ormClient;
	private volatile DataSource dataSource;
	private volatile ThreadPrincipalService threadPrincipalService;
	private volatile EventAdmin eventAdmin;
	
	public OrmServiceImpl() {
	}
	
	@Override
	synchronized public boolean add(Component component) {
		if (components.containsKey(component.getName())) {
			return false;			
		}
		components.put(component.getName(), component);
		return true;
	}

	@Override
	public <T,S extends T> DataMapper<T> getDataMapper(Class<T> api, Class<S> implementation , String componentName , String tableName ) {
		return getTable(componentName,tableName).getDataMapper(api,implementation);
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
	public Component getComponent(String name) {
		Component result = components.get(name);
		if (result == null) {
			result = getOrmClient().getComponentFactory().get(name);
			if (result != null) {
				components.put(name, result);
			}
		}
		return result;
	}
	
	@Override
	public Table getTable(String componentName , String tableName) {
		Component component = getComponent(componentName);
		return (component == null) ? null : component.getTable(tableName);
	}
	
	// install time api
	
	@Override
	public Component newComponent(String name,String description) {
		// performs check only on component cache , not in database. 
		if (components.containsKey(name)) {
			throw new IllegalArgumentException("Component " + name + " exists already");
		}
		return new ComponentImpl(name, description);
	}

	@Override
	public void install(Component component, boolean executeDdl, boolean storeMappings) {
		if (executeDdl) {
			executeDdl(component);
		}
		if (storeMappings) {
			((ComponentImpl) component).persist();
		}
	}
	
	 @Override
	public void install(boolean executeDdl,boolean storeMappings) {
		 install(components.get(Bus.COMPONENTNAME) , executeDdl,storeMappings);
	}
	 
	private void executeDdl(Component component) {
		try {
			doExecuteDdl(component);
		} catch (SQLException ex) {
			throw new PersistenceException(ex);
		}
	}
	
	private void doExecuteDdl(Component component) throws SQLException {
		Connection connection = getConnection(false);
		try {
			Statement statement = connection.createStatement();
			try {
				for (Table table : component.getTables()) {									
					for (String each : ((TableImpl) table).getDdl()) {
						System.out.println(each);
						statement.execute(each);
					}
				}
			} finally {
				statement.close();
			}
		} finally {
			connection.close();
		}
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
		this.ormClient = new OrmClientImpl(this);
		Bus.setServiceLocator(this);
	}
	
	@Deactivate
	public void deActivate() {
		Bus.setServiceLocator(null);
	}
}
