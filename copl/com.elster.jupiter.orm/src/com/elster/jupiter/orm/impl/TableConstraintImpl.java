package com.elster.jupiter.orm.impl;

import java.util.*;

import com.elster.jupiter.orm.*;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.orm.plumbing.Bus;
import com.elster.jupiter.orm.plumbing.OrmClient;

public class TableConstraintImpl implements TableConstraint , PersistenceAware {
	// persistent fields
	private String componentName;
	private String tableName;	
	private String name;
	private TableConstraintType type;
	private DeleteRule deleteRule;
	private String referencedComponentName;
	private String referencedTableName;	
	private String fieldName;
	private String reverseFieldName;
	private String reverseCurrentName;
	
	// associations
	private Table table;
	private List<Column> columns;
	private Table referencedTable;
	
	
	@SuppressWarnings("unused")
	private TableConstraintImpl() {	
	}

	TableConstraintImpl(Table table, String name, TableConstraintType type) {
		if (name.length() > Bus.CATALOGNAMELIMIT) {
			throw new IllegalArgumentException("Name " + name + " too long" );
		}
		this.table = table;
		this.componentName = table.getComponentName();
		this.tableName = table.getName();
		this.name = name;
		this.type = type;
		this.columns = new ArrayList<>();
	}

	TableConstraintImpl(Table table, String name, Table referencedTable, DeleteRule deleteRule,String fieldName , String reverseFieldName, String reverseCurrentName) {
		this(table,name,TableConstraintType.FOREIGNKEY);
		this.referencedTable = referencedTable;
		referencedComponentName = referencedTable.getComponentName();
		referencedTableName = referencedTable.getName();		
		this.deleteRule = deleteRule;
		this.fieldName = fieldName;		
		this.reverseFieldName = reverseFieldName;
		this.reverseCurrentName = reverseCurrentName;
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
			List<ColumnInConstraintImpl> columnsInConstraint = getOrmClient().getColumnInConstraintFactory().find(
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
			table = getOrmClient().getTableFactory().get(componentName, tableName);
		}
		return table;
	}

	TableConstraintType getType() {
		return type;
	}

	@Override
	public Table getReferencedTable() {
		if (referencedTableName == null || referencedComponentName == null) {
			return null;
		}
		if (referencedTable == null) {
			if (referencedComponentName.equals(componentName)) {
				referencedTable = getTable().getDataModel().getTable(referencedTableName);
			} else {
				referencedTable = getOrmClient().getTableFactory().get(referencedComponentName, referencedTableName);
			}
		}
		return referencedTable;
	}
	
	@Override
	public DeleteRule getDeleteRule() {
		return deleteRule;
	}
	
	@Override 
	public String getFieldName() {
		return fieldName;
	}
	
	@Override 
	public String getReverseFieldName() {
		return reverseFieldName;
	}
	
	@Override 
	public String getReverseCurrentName() {
		return reverseCurrentName;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Constraint ");
		sb.append(name);
		sb.append(" on ");
		sb.append(getTable().getQualifiedName());
		sb.append(" ");
		sb.append(getType());
		sb.append(" ("); 
		String separator = "";
		for (Column each : getColumns(false)) {
			sb.append(separator);
			sb.append(each.getName());
			separator = ", ";
		}
		sb.append(")");
		return sb.toString();
	}

	@Override
	public void postLoad() {	
		// do eager initialization in order to be thread safe
		getColumns(false);
		if (referencedComponentName != null && !referencedComponentName.equals(componentName)) {
			getReferencedTable();
		}
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
	public boolean isPrimaryKeyConstraint() {
		return getType().isPrimaryKey();		
	}

	@Override
	public boolean isUniqueConstraint() {
		return getType().isUnique();
	}

	@Override
	public boolean isForeignKeyConstraint() {
		return getType().isForeignKey();
	}

	void persist() {
		getOrmClient().getTableConstraintFactory().persist(this);		
		int position = 1;
		for (Column column : getColumns(false)) {
			new ColumnInConstraintImpl(this, column, position++).persist();
		}
	}

	private OrmClient getOrmClient() {
		return Bus.getOrmClient();
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
			result[i] = DomainMapper.FIELD.get(value, getColumns().get(i).getFieldName());
		}
		return result;		
	}
	
	boolean needsIndex() {
		if (type.hasAutoIndex())
			return false;
		for (TableConstraint constraint : getTable().getConstraints()) {
			if (constraint.isPrimaryKeyConstraint() || constraint.isUniqueConstraint()) {
				if (this.isSubset(constraint)) {
					return false;
				}
 			}
		}
		return true;
	}
	
	private boolean isSubset(TableConstraint other) {
		if (other.getColumns().size() < this.getColumns().size()) {
			return false;
		}
		for (int i = 0 ; i < getColumns().size() ; i++) {
			if (!this.getColumns().get(i).equals(other.getColumns().get(i))) {
				return false;
			}
		}
		return true;
	}
}
