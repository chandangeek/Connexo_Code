package com.elster.jupiter.orm.impl;

import java.util.*;

import com.elster.jupiter.orm.*;


class TableConstraintImpl implements TableConstraint  {
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

	TableConstraintImpl(Table table, String name, Table referencedTable, DeleteRule deleteRule,String fieldName , String reverseFieldName) {
		this(table,name,TableConstraintType.FOREIGNKEY);
		this.referencedTable = referencedTable;
		referencedComponentName = referencedTable.getComponentName();
		referencedTableName = referencedTable.getName();		
		this.deleteRule = deleteRule;
		this.fieldName = fieldName;		
		this.reverseFieldName = reverseFieldName;
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
					new String[] { "position" });
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

	private TableConstraintType getType() {
		return type;
	}

	@Override
	public Table getReferencedTable() {
		if (referencedTableName == null)
			return null;
		if (referencedTable == null) {
			referencedTable = getOrmClient().getTableFactory().get(referencedComponentName, referencedTableName);						
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

	void doSetTable(Table table) {	
		this.table = table;
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
	
	@Override
	public String getComponentName() {
		return componentName;
	}

	@Override
	public String getTableName() {
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

	
	public String getDdl() {
		StringBuilder base = new StringBuilder("constraint ");
		base.append(name);
		base.append(" ");
		base.append(getType().getDdl());
		base.append("(");
		String separator = "";
		for (Column column : getColumns(false)) {
			base.append(separator);
			base.append(column.getName());
			separator = ", ";
		}
		base.append(")");
		if (isForeignKeyConstraint()) {
			base.append(" references ");
			base.append(getReferencedTable().getQualifiedName());
			base.append("(");
			separator = "";
			for (Column column : getReferencedTable().getPrimaryKeyColumns()) {
				base.append(separator);
				base.append(column.getName());
				separator = ", ";
			}
			base.append(")");
			DeleteRule deleteRule = getDeleteRule();
			if (deleteRule != null) {
				base.append(deleteRule.getDdl());
			}
		}
		return base.toString();
	}
	
	String getJournalDdl(String extra) {
		StringBuilder base = new StringBuilder("constraint ");
		base.append(name + "_JRNL");
		base.append(" ");
		base.append(getType().getDdl());
		base.append("(");
		String separator = "";
		for (Column column : getColumns(false)) {
			base.append(separator);
			base.append(column.getName());
			separator = ", ";
		}
		base.append(separator);
		base.append(extra);
		base.append(")");	
		return base.toString();
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
}
