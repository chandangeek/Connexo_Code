package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ForeignKeyConstraint;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.TableConstraint;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.associations.impl.PersistentReference;
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
	
	private String name;
	
	// associations
	private Reference<Table> table;
	private List<ColumnInConstraintImpl> columnHolders;
	
	TableConstraintImpl() {	
	}

	TableConstraintImpl(Table table, String name) {
		if (name.length() > Bus.CATALOGNAMELIMIT) {
			throw new IllegalArgumentException("Name " + name + " too long" );
		}
		this.table = ValueReference.of(table);
		this.name = name;
		this.columnHolders = new ArrayList<>();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public List<Column> getColumns() {
		ImmutableList.Builder<Column> builder = new ImmutableList.Builder<>();
		for (ColumnInConstraintImpl each : columnHolders) {
			builder.add(each.getColumn());
		}
		return builder.build();
	}

	@Override
	public Table getTable() {
		return table.get();
	}

	@Override
	public void postLoad() {	
		// do eager initialization in order to be thread safe
		getColumns();
	}
	
	void add(Column column) {
		columnHolders.add(new ColumnInConstraintImpl(this, column, columnHolders.size() + 1));
	}

	void add(Column[] columns) {
		for (Column column : columns) {
			add(column);
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

	void persist() {
		Bus.getOrmClient().getTableConstraintFactory().persist(this);
		Bus.getOrmClient().getColumnInConstraintFactory().persist(columnHolders);
	}
	
	@Override
	public boolean isNotNull() {
		for (Column each : getColumns()) {
			if (!each.isNotNull())
				return false;
		}
		return true;
	}

	@Override
	public Object[] getColumnValues(Object value) {
		List<Column> columns = getColumns();
		int columnCount = columns.size();		
		Object[] result = new Object[columnCount]; 
		for (int i = 0 ; i < columnCount ; i++) {
			Column column = columns.get(i);
			String fieldName = columns.get(i).getFieldName();
			if (fieldName == null) {
				for (ForeignKeyConstraint constraint : ((TableImpl) getTable()).getReferenceConstraints()) {
					if (constraint.hasColumn(column)) {
						Reference<?> reference = (Reference<?>) DomainMapper.FIELDSTRICT.get(value, constraint.getFieldName());
						if (reference == null || !reference.isPresent()) {
							result[i] = null;
						}
						int index = constraint.getColumns().indexOf(column);
						if (reference instanceof PersistentReference<?>) {
							result[i] = ((PersistentReference<?>) reference).getKeyPart(index);
						} else {
							result[i] = constraint.getReferencedTable().getPrimaryKey(reference.get())[index];
						}
						break;
					}
					throw new IllegalStateException("No mapping for Column " + column.getName());
				}
			} else {
				result[i] = DomainMapper.FIELDSTRICT.get(value, columns.get(i).getFieldName());
			}
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
		Objects.requireNonNull(getTable());
		Objects.requireNonNull(name);
		if (this.getColumns().isEmpty()) {
			throw new IllegalArgumentException("Column list should not be emty");
		}
	}
}
