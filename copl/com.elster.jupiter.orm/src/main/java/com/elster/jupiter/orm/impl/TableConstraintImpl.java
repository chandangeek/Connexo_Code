package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.TableConstraint;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.orm.internal.Bus;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class TableConstraintImpl implements TableConstraint , PersistenceAware {
	
	public static final Map<String,Class<? extends TableConstraint>> implementers =  ImmutableMap.<String,Class<? extends TableConstraint>>of(
			"PRIMARYKEY",PrimaryKeyConstraintImpl.class,
			"UNIQUE",  UniqueConstraintImpl.class,
			"FOREIGNKEY" , ForeignKeyConstraintImpl.class);
	
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
		return ImmutableList.copyOf(doGetColumns());
	}
	
	private List<Column> doGetColumns() {
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
		return columns;
	}

	@Override
	public Table getTable() {
		if (table == null) {
			table = Bus.getOrmClient().getTableFactory().getExisting(componentName, tableName);
		}
		return table;
	}

	@Override
	public void postLoad() {	
		// do eager initialization in order to be thread safe
		doGetColumns();
	}
	
	void add(Column column) {
		doGetColumns().add(column);
	}

	void add(Column[] columns) {
		for (Column column : columns) {
			doGetColumns().add(column);
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
	
	@Override
	public boolean hasColumn(Column column) {
		return getColumns().contains(column);
	}

	void persist() {
		Bus.getOrmClient().getTableConstraintFactory().persist(this);		
		int position = 1;
		for (Column column : doGetColumns()) {
			new ColumnInConstraintImpl(this, column, position++).persist();
		}
	}
	
	@Override
	public boolean isNotNull() {
		for (Column each : doGetColumns()) {
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

	void validate() {
		Objects.requireNonNull(componentName);
		Objects.requireNonNull(name);
		if (this.getColumns().isEmpty()) {
			throw new IllegalArgumentException("Column list should not be emty");
		}
	}
}
