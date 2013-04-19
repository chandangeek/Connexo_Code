package com.elster.jupiter.orm.impl;

import java.util.*;

import com.elster.jupiter.orm.*;

import static com.elster.jupiter.orm.ColumnConversion.*;

class TableImpl implements Table  {
	
	static final String JOURNALTIMECOLUMNNAME = "JOURNALTIME";
	
	// persistent fields
	private String componentName;
	private String schema;
	private String name;
	private String journalTableName;
	
	// associations
	private Component component;
	private List<Column> columns;
	private List<TableConstraint> constraints;
		
	TableImpl(Component component, String schema, String name) {
		if (name.length() > Bus.CATALOGNAMELIMIT) {
			throw new IllegalArgumentException("Name " + name + " too long" );
		}
		this.component = component;
		this.componentName = component.getName();
		this.schema = schema;
		this.name = name;
		this.columns = new ArrayList<>();
		this.constraints = new ArrayList<>();
	}
	
	@SuppressWarnings("unused")
	private TableImpl() {
	}
	
	@Override 
	public String getSchema() {
		return schema;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public String getQualifiedName() {
		return getQualifiedName(name);
	}
	
	String getQualifiedName(String value) {
		return schema == null || schema.length() == 0 ? value : schema + "." + value;
	}
	
	@Override
	public List<Column> getColumns() {
		return getColumns(true);
	}
	
	private List<Column> getColumns(boolean protect) {
		if (columns ==  null) {
			columns = Bus.getOrmClient().getColumnFactory().find("componentName",getComponentName(),"tableName",getName(),"position");
			for (Column column : columns) {
				((ColumnImpl) column).doSetTable(this);
			}			
		}
		return protect ? Collections.unmodifiableList(columns) : columns;
	}
	
	@Override
	public List<TableConstraint> getConstraints() {
		return getConstraints(true);
	}
	
	private List<TableConstraint> getConstraints(boolean protect) {
		if (constraints ==  null) {
			constraints = Bus.getOrmClient().getTableConstraintFactory().find("componentName", getComponentName() , "tableName" , getName());
			for (TableConstraint each : constraints) {
				((TableConstraintImpl) each).doSetTable(this);
			}
		}
		return protect ? Collections.unmodifiableList(constraints) : constraints;
	}
	
	@Override
	public String toString() {
		return "Table " + name;
	}

	@Override
	public Component getComponent() {
		if (component == null) {
			component = Bus.getOrmClient().getComponentFactory().get(componentName);
		}
		return component;
	}

	@Override
	public String getComponentName() {		
		return componentName;
	}

	void doSetComponent(Component component) {
		this.component = component;
		// do eager initialization in order to be thread safe
		getColumns(false);
		getConstraints(false);
	}
	
	Column add(ColumnImpl column) {
		getColumns(false).add(column);
		column.doSetPosition(getColumns(false).size());
		return column;
	}

	@Override
	public Column getColumn(String name) {
		for (Column column : getColumns(false)) {		
			if (column.getName().equalsIgnoreCase(name)) {
				return column;
			}
		}
		return null;
	}

	
	@Override
	public Column[] getPrimaryKeyColumns() {		
		List<Column> result = null;
		for (TableConstraint each : getConstraints(false)) {
			if (each.isPrimaryKeyConstraint()) {					
				result = each.getColumns();
				break;
			}				
		}
		return (result == null) ? new Column[0] : result.toArray(new Column[result.size()]);
	}
	
	boolean isPrimaryKeyColumn(Column column) {
		for (TableConstraint tableConstraint : getConstraints(false)) {
			if (tableConstraint.isPrimaryKeyConstraint()) {
				return tableConstraint.getColumns().contains(column);
			}							
		}
		return false;
	}
	
	Column[] getVersionColumns() {
		List<Column> result = new ArrayList<>();
		for (Column column : getColumns(false)) {
			if (column.isVersion()) {
				result.add(column);
			}
		}		
		return result.toArray(new Column[result.size()]);		
	}

	Column[] getInsertValueColumns() {
		List<Column> result = new ArrayList<>();
		for (Column column : getColumns(false)) {
			if (column.hasInsertValue()) {
				result.add(column);
			}
		}		
		return result.toArray(new Column[result.size()]);		
	}
	
	Column[] getUpdateValueColumns() {
		List<Column> result = new ArrayList<>();
		for (Column column : getColumns(false)) {
			if (column.hasUpdateValue()) {
				result.add(column);
			}
		}		
		return result.toArray(new Column[result.size()]);		
	}
	
	Column[] getStandardColumns() {
		List<Column> result = new ArrayList<>();
		for (Column column : getColumns(false)) {
			if (((ColumnImpl) column).isStandard()) {
				result.add(column);
			}
		}		
		return result.toArray(new Column[result.size()]);		
	}		
		
	Column[] getAutoUpdateColumns() {
		List<Column> result = new ArrayList<>();
		for (Column column : getColumns(false)) {
			if (((ColumnImpl) column).hasAutoValue(true)) {
				result.add(column);
			}
		}		
		return result.toArray(new Column[result.size()]);
	}
	
	public List<String> getDdl() {
		List<String> result = new ArrayList<>();
		result.add(getTableDdl());
		if (journalTableName != null) {
			result.add(getJournalTableDdl());
		}
		Set<String> sequenceNames = new HashSet<String>();
		for (Column column : getColumns(false)) {
			if (column.isAutoIncrement()) {
				sequenceNames.add(column.getSequenceName());
			}
		}
		for (String sequenceName : sequenceNames) {
			// cache 1000 for performance in RAC environments
			result.add("create sequence " + sequenceName + " cache 1000");
		}
		return result;
	}
	
	public String getTableDdl() {
		StringBuilder sb = new StringBuilder("create table ");
		sb.append(getQualifiedName());
		sb.append(" (");
		String separator = "";
		for (Column column : getColumns(false)) {
			sb.append(separator);
			sb.append(((ColumnImpl) column).getDdl());
			separator = ", ";
		}
		for (TableConstraint constraint : getConstraints(false)) {
			sb.append(separator);
			sb.append(((TableConstraintImpl) constraint).getDdl());			
		}
		sb.append(")");
		return sb.toString();
	}
	
	public String getJournalTableDdl() {
		StringBuilder sb = new StringBuilder("create table ");
		sb.append(getQualifiedName(journalTableName));
		sb.append(" (");
		String separator = "";
		for (Column column : getColumns(false)) {
			sb.append(separator);
			sb.append(((ColumnImpl) column).getDdl());
			separator = ", ";
		}
		sb.append(separator);
		sb.append(JOURNALTIMECOLUMNNAME);
		sb.append(" NUMBER NOT NULL");				
		for (TableConstraint constraint : getConstraints(false)) {
			if (constraint.isPrimaryKeyConstraint()) {
				TableConstraintImpl pkConstraint = (TableConstraintImpl) constraint;
				sb.append(separator);
				sb.append(pkConstraint.getJournalDdl(getExtraJournalPrimaryKeyColumnName()));			
			}
		}
		sb.append(")");
		return sb.toString();
	}

	String getExtraJournalPrimaryKeyColumnName() {		
		Column[] versionColumns = getVersionColumns();
		return versionColumns.length > 0 ? versionColumns[0].getName() : JOURNALTIMECOLUMNNAME;			
	}
	
	@Override
	public Column addColumn(String name, String dbType, boolean notnull , ColumnConversion conversion,String fieldName) {
		return add(new ColumnImpl(this,name,dbType,notnull, conversion,fieldName,null,false, null,null,false));
	}

	@Override
	public Column addColumn(String name, String dbType, boolean notnull , ColumnConversion conversion,String fieldName, String insertValue , String updateValue) {
		return add(new ColumnImpl(this,name,dbType,notnull, conversion,fieldName,null,false,insertValue,updateValue,false));
	}
	
	@Override
	public Column addColumn(String name, String dbType, boolean notnull , ColumnConversion conversion,String fieldName, String insertValue , boolean skipOnUpdate ) {
		return add(new ColumnImpl(this,name,dbType,notnull, conversion,fieldName,null,false,insertValue,null,skipOnUpdate));
	}
	@Override
	public Column addAutoIncrementColumn(String name, String dbType, ColumnConversion conversion,String fieldName,String sequence, boolean skipOnUpdate) {
		if (sequence.length() > Bus.CATALOGNAMELIMIT) {
			throw new IllegalArgumentException("Name " + sequence + " too long");
		}
		return add(new ColumnImpl(this,name,dbType, true, conversion,fieldName,sequence,false,null,null, skipOnUpdate));
	}
	
	@Override
	public Column addVersionCountColumn(String name, String dbType , String fieldName) {
		return add(new ColumnImpl(this,name,dbType, true , NUMBER2LONG,fieldName, null, true,null,null,false));
	}
	
	@Override
	public Column addCreateTimeColumn(String name , String fieldName) {
		return add(new ColumnImpl(this,name,"number", true , NUMBER2NOW, fieldName, null, false, null , null , true));
	}
	
	@Override
	public Column addModTimeColumn(String name , String fieldName) {
		return add(new ColumnImpl(this,name,"number", true , NUMBER2NOW, fieldName, null, false, null , null , false));
	}
	
	@Override
	public Column addUserNameColumn(String name , String fieldName) {
		return add(new ColumnImpl(this,name,"varchar2(80)", true , CHAR2PRINCIPAL, fieldName, null, false, null , null , false));
	}
	
	@Override
	public void addAuditColumns() {
		addVersionCountColumn("VERSIONCOUNT", "number" , "version");
		addCreateTimeColumn("CREATETIME", "createTime");
		addModTimeColumn("MODTIME", "modTime");
		addUserNameColumn("USERNAME", "userName");
	}
	
	@Override
	public TableConstraint addPrimaryKeyConstraint(String name, Column... columns) {
		TableConstraintImpl constraint = new TableConstraintImpl(this ,  name , TableConstraintType.PRIMARYKEY );
		constraint.add(columns);
		getConstraints(false).add(constraint);
		return constraint;
	}

	@Override
	public TableConstraint addUniqueConstraint(String name, Column... columns) {
		TableConstraintImpl constraint = new TableConstraintImpl(this ,  name , TableConstraintType.UNIQUE);
		constraint.add(columns);
		getConstraints(false).add(constraint);
		return constraint;	
	}

	@Override
	public TableConstraint addForeignKeyConstraint(String name, Table referencedTable, DeleteRule deleteRule , String fieldName , String reverseFieldName , Column... columns) {
		TableConstraintImpl constraint = new TableConstraintImpl(this , name, referencedTable , deleteRule, fieldName , reverseFieldName);
		constraint.add(columns);
		getConstraints(false).add(constraint);
		return constraint;	
	}

	@Override
	public TableConstraint addForeignKeyConstraint(String name, String referencedTableName, DeleteRule deleteRule , String fieldName , String reverseFieldName , Column... columns) {
		Table referencedTable = getComponent().getTable(referencedTableName);		
		return addForeignKeyConstraint(name, referencedTable, deleteRule , fieldName , reverseFieldName ,columns); 					
	}
	
	@Override
	public TableConstraint addForeignKeyConstraint(String name, String component , String referencedTableName, DeleteRule deleteRule , String fieldName , String reverseFieldName , Column... columns) {
		Table referencedTable = Bus.getOrmClient().getTable(component, referencedTableName);		
		return addForeignKeyConstraint(name, referencedTable, deleteRule , fieldName, reverseFieldName, columns); 					
	}


	@Override
	public <T,S extends T> DataMapper<T> getDataMapper(Class<T> api , Class<S> implementation) {
		return new DataMapperImpl<T,S>(api, implementation,this);
	}
		
	void persist() {
		Bus.getOrmClient().getTableFactory().persist(this);
		for (Column column : getColumns(false)) {
			((ColumnImpl) column).persist();
		}
		for (TableConstraint tableConstraint : getConstraints(false)) {
			((TableConstraintImpl) tableConstraint).persist();
		}
	}

	@Override
	public Column getColumnForField(String name) {
		for (Column column : getColumns(false)) {		
			if (name.equals(column.getFieldName())) {
				return column;
			}
		}
		return null;
	}

	@Override
	public String getJournalTableName() {
		return journalTableName;
	}

	@Override
	public void setJournalTableName(String journalTableName) {
		this.journalTableName = journalTableName;
	}
	
	@Override
	public boolean hasJournal() {
		return journalTableName != null;
	}

	@Override
	public Column addAutoIdColumn() {
		return addAutoIncrementColumn("ID", "number" , NUMBER2LONG, "id" , name + "ID" , true);
	}

	@Override
	public void addQuantityColumns(String name , boolean notNull , String fieldName) {		
		addColumn(name + "VALUE", "number" , notNull , NOCONVERSION , fieldName + ".value");
		addColumn(name +  "MULTIPLIER", "number" , notNull , NUMBER2INTWRAPPER,  fieldName + ".multiplier");
		addColumn(name +  "UNIT", "varchar2(8)" , notNull , CHAR2UNIT, fieldName + ".unit");
	}
	
	@Override
	public void addMoneyColumns(String name , boolean notNull , String fieldName) {		
		addColumn(name + "VALUE", "number" , notNull , NOCONVERSION , fieldName + ".value");
		addColumn(name +  "CURRENCY", "number" , notNull , CHAR2CURRENCY,  fieldName + ".currency");		
	}
	
	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		try {
			TableImpl o = (TableImpl) other;
			return this.componentName.equals(o.componentName) && this.name.equals(o.name);
		} catch (ClassCastException e) {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return component.hashCode() ^ name.hashCode();
	}

}
	

