package com.elster.jupiter.orm.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.TableConstraint;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public abstract class TableConstraintImpl implements TableConstraint {
	
	public static final Map<String,Class<? extends TableConstraint>> implementers =  ImmutableMap.<String,Class<? extends TableConstraint>>of(
			"PRIMARYKEY",PrimaryKeyConstraintImpl.class,
			"UNIQUE",  UniqueConstraintImpl.class,
			"FOREIGNKEY" , ForeignKeyConstraintImpl.class);
	
	private String name;
	
	// associations
	private final Reference<TableImpl<?>> table = ValueReference.absent();
	private final List<ColumnInConstraintImpl> columnHolders = new ArrayList<>();
	

	TableConstraintImpl init(TableImpl<?> table, String name) {
		if (name.length() > ColumnConversion.CATALOGNAMELIMIT) {
			throw new IllegalArgumentException("Name " + name + " too long" );
		}
		this.table.set(table);
		this.name = name;
		return this;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public List<ColumnImpl> getColumns() {
		ImmutableList.Builder<ColumnImpl> builder = new ImmutableList.Builder<>();
		for (ColumnInConstraintImpl each : columnHolders) {
			builder.add(each.getColumn());
		}
		return builder.build();
	}

	@Override
	public TableImpl<?> getTable() {
		return table.get();
	}

	void add(ColumnImpl column) {
		columnHolders.add(new ColumnInConstraintImpl(this, column, columnHolders.size() + 1));
	}

	void add(Column[] columns) {
		for (Column column : columns) {
			add((ColumnImpl) column);
		}
	}
	
	String getComponentName() {
		return getTable().getComponentName();
	}


	String getTableName() {
		return getTable().getName();
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
	
	@Override
	public boolean isNotNull() {
		for (Column each : getColumns()) {
			if (!each.isNotNull())
				return false;
		}
		return true;
	}
	
	public KeyValue getColumnValues(Object value) {
		List<ColumnImpl> columns = getColumns();
		int columnCount = columns.size();		
		Object[] result = new Object[columnCount]; 
		for (int i = 0 ; i < columnCount ; i++) {
			result[i] = columns.get(i).getDomainValue(value);
		}
		return KeyValue.of(result);		
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
		Objects.requireNonNull(getTable());
		Objects.requireNonNull(name);
		if (this.getColumns().isEmpty()) {
			throw new IllegalArgumentException("Column list should not be emty");
		}
	}
}
