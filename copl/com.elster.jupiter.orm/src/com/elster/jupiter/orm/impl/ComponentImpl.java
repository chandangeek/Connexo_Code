package com.elster.jupiter.orm.impl;

import java.util.*;

import com.elster.jupiter.orm.*;
import com.elster.jupiter.orm.plumbing.Bus;
import com.elster.jupiter.orm.plumbing.OrmClient;


public class ComponentImpl implements Component , PersistenceAware {
	// persistent fields
	private String name;
	private String description;
	
	// associations
	private List<Table> tables;
	
	@SuppressWarnings("unused")
	private ComponentImpl() {
	}
	
	public ComponentImpl(String name , String description) {
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
	public void setDescription(String description) {		
		this.description = description;				
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
		getOrmClient().getComponentFactory().persist(this);
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
	
}
