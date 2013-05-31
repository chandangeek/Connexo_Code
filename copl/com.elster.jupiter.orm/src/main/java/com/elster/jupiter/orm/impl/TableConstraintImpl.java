package com.elster.jupiter.orm.impl;

import java.util.*;

import com.elster.jupiter.orm.*;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.orm.plumbing.Bus;

abstract public class TableConstraintImpl implements TableConstraint , PersistenceAware {
	
	public final static Map<String,Class<? extends TableConstraint>> implementers =  createImplementers();
	
	static Map<String,Class<? extends TableConstraint>> createImplementers() {
		Map<String,Class<? extends TableConstraint>> result = new HashMap<>();
		result.put("PRIMARYKEY",PrimaryKeyConstraintImpl.class);
		result.put("UNIQUE",  UniqueConstraintImpl.class);
		result.put("FOREIGNKEY" , ForeignKeyConstraintImpl.class);
		return result;
	}
	
	// persistent fields
	private String componentName;
	private String tableName;	
	private String name;
	
	// associations
	private Table table;
	private List<Column> columns;
	
	TableConstraintImpl() {	
	}

	TableConstraintImpl(Table table, String name) {
		if (name.length() > Bus.CATALOGNAMELIMIT) {
			throw new IllegalArgumentException("Name " + name + " too long" );
		}
		this.table = table;
		this.componentName = table.getComponentName();
		this.tableName = table.getName();
		this.name = name;
		this.columns = new ArrayList<>();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public List<Column> getColumns() {
		return getColumns(true);
	}
	
	private List<Column> getColumns(boolean protect) {
		if (columns == null) {
			List<ColumnInConstraintImpl> columnsInConstraint = Bus.getOrmClient().getColumnInConstraintFactory().find(
					new String[] {"componentName","tableName","constraintName"} ,
					new Object[] { getComponentName(), getTableName() , getName() } ,
					"position");
			columns = new ArrayList<>();
			for (ColumnInConstraintImpl columnInConstraint : columnsInConstraint) {
				columnInConstraint.doSetConstraint(this);
				columns.add(columnInConstraint.getColumn());
			}
		}
		return protect ? Collections.unmodifiableList(columns) : columns;
	}

	@Override
	public Table getTable() {
		if (table == null) {
			table = Bus.getOrmClient().getTableFactory().get(componentName, tableName);
		}
		return table;
	}

	@Override
	public void postLoad() {	
		// do eager initialization in order to be thread safe
		getColumns(false);
	}
	
	void add(Column column) {
		getColumns(false).add(column);
	}

	void add(Column[] columns) {
		for (Column column : columns) {
			getColumns(false).add(column);
		}
	}
	
	String getComponentName() {
		return componentName;
	}


	String getTableName() {
		return tableName;
	}

	@Override
	public boolean isPrimaryKey() {
		return false;		
	}

	@Override
	public boolean isUnique() {
		return false;
	}

	@Override
	public boolean isForeignKey() {
		return false;
	}

	void persist() {
		Bus.getOrmClient().getTableConstraintFactory().persist(this);		
		int position = 1;
		for (Column column : getColumns(false)) {
			new ColumnInConstraintImpl(this, column, position++).persist();
		}
	}
	
	@Override
	public boolean isNotNull() {
		for (Column each : getColumns(false)) {
			if (!each.isNotNull())
				return false;
		}
		return true;
	}

	@Override
	public Object[] getColumnValues(Object value) {
		int columnCount = getColumns().size();		
		Object[] result = new Object[columnCount]; 
		for (int i = 0 ; i < columnCount ; i++) {
			result[i] = DomainMapper.FIELDSTRICT.get(value, getColumns().get(i).getFieldName());
		}
		return result;		
	}
	
	boolean needsIndex() {		
		return false;		
	}
	
	abstract String getTypeString();
	
	void appendDdlTrailer(StringBuilder builder) {
		// do nothing by default;
	}
	
	final public String getDdl() {
		StringBuilder sb = new StringBuilder("constraint ");
		sb.append(name);
		sb.append(" ");
		sb.append(getTypeString());
		sb.append(" (");
		String separator = "";
		for (Column column : getColumns()) {
			sb.append(separator);
			sb.append(column.getName());
			separator = ", ";			
		}
		sb.append(") ");
		appendDdlTrailer(sb);
		return sb.toString();			
	}
	
}
