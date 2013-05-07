package com.elster.jupiter.orm.impl;

import java.security.Principal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import com.elster.jupiter.orm.*;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.orm.plumbing.Bus;
import com.elster.jupiter.orm.plumbing.OrmClient;


public class DataModelImpl implements DataModel , PersistenceAware {
	// persistent fields
	private String name;
	private String description;
	
	// associations
	private List<Table> tables;
	
	@SuppressWarnings("unused")
	private DataModelImpl() {
	}
	
	public DataModelImpl(String name , String description) {
		this.name = name;
		this.description = description;
		this.tables = new ArrayList<>();
	}

	@Override
	public String getName() {		
		return name;
	}
	
	@Override
	public String getDescription() {	
		return description;
	}
	
	@Override 
	public List<Table> getTables() {
		return getTables(true);
	}
	
	public List<Table> getTables(boolean protect) {
		if (tables == null) {
			tables = getOrmClient().getTableFactory().find("component", this);			
		}
		return protect ? Collections.unmodifiableList(tables) : tables;
	}

	@Override 
	public Table getTable(String tableName) {
		for (Table table : getTables()) {
			if (table.getName().equals(tableName)) {
				return table;
			}
		}
		return null;
	}
		
	@Override
	public Table addTable(String tableName) {		
		return addTable(null,tableName);
	}

	@Override
	public Table addTable(String schema , String tableName) {
		if (getTable(tableName) != null) {
			throw new IllegalArgumentException("Component has already table " + tableName);
		}
		Table table = new TableImpl(this,schema,tableName);
		add(table);
		return table;
	}
	
	@Override
	public String toString() {
		return "Component " + name; 
	}
	
	private void add(Table table) {
		getTables(false).add(table);
	}

	public void persist() {
		getOrmClient().getDataModelFactory().persist(this);
		for ( Table table: getTables(false)) {
			((TableImpl) table).persist();
		}
	}
	
	private OrmClient getOrmClient() {
		return Bus.getOrmClient();
	}

	@Override
	public void postLoad() {
		getTables(false);		
	}

	@Override
	public <T> DataMapper<T> getDataMapper(Class<T> api, Class<? extends T> implementation, String tableName) {		
		return getTable(tableName).getDataMapper(api, implementation);
	}

	@Override
	public <T> DataMapper<T> getDataMapper(Class<T> api, Map<String,Class<? extends T>> implementations, String tableName) {		
		return getTable(tableName).getDataMapper(api, implementations);
	}
	
	@Override
	public void install(boolean executeDdl, boolean store) {
		if (executeDdl) {
			executeDdl();
		} 
		if (store) {
			persist();
		}
		
	}
	
	private void executeDdl() {
		try {
			doExecuteDdl();
		} catch (SQLException ex) {
			throw new PersistenceException(ex);
		}
	}
	
	private void doExecuteDdl() throws SQLException {
		Connection connection = Bus.getConnection(false);
		try {
			Statement statement = connection.createStatement();
			try {
				for (Table table : getTables()) {									
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
	public Connection getConnection(boolean transactionRequired) throws SQLException {
		return Bus.getConnection(transactionRequired);
	}

	@Override
	public Principal getPrincipal() {
		return Bus.getPrincipal();		
	}

}
