package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.AssociationMapping;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.FieldType;
import com.elster.jupiter.orm.ForeignKeyConstraint;
import com.elster.jupiter.orm.PrimaryKeyConstraint;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.TableConstraint;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.orm.fields.impl.FieldMapping;
import com.elster.jupiter.orm.fields.impl.ForwardConstraintMapping;
import com.elster.jupiter.orm.fields.impl.MultiColumnMapping;
import com.elster.jupiter.orm.fields.impl.ReverseConstraintMapping;
import com.elster.jupiter.orm.plumbing.Bus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.elster.jupiter.orm.ColumnConversion.*;
import static com.elster.jupiter.util.Checks.is;

public class TableImpl implements Table , PersistenceAware  {
	
	static final String JOURNALTIMECOLUMNNAME = "JOURNALTIME";
	
	// persistent fields
	private String componentName;
	private String schema;
	private String name;
	private String journalTableName;
	private boolean indexOrganized;
	
	// associations
	private DataModel component;
	private List<Column> columns;
	private List<TableConstraint> constraints;
		
	TableImpl(DataModel component, String schema, String name) {
        assert component != null;
        assert !is(name).emptyOrOnlyWhiteSpace();
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
			columns = Bus.getOrmClient().getColumnFactory().find("table",this);				
		}
		return protect ? Collections.unmodifiableList(columns) : columns;
	}
	
	@Override
	public List<TableConstraint> getConstraints() {
		return getConstraints(true);
	}
	
	private List<TableConstraint> getConstraints(boolean protect) {
		if (constraints ==  null) {
			constraints = Bus.getOrmClient().getTableConstraintFactory().find("table",this);			
		}
		return protect ? Collections.unmodifiableList(constraints) : constraints;
	}
	
	@Override
	public String toString() {
		return "Table " + name;
	}

	@Override
	public DataModel getDataModel() {
		if (component == null) {
			component = Bus.getOrmClient().getDataModelFactory().getExisting(componentName);
		}
		return component;
	}

	@Override
	public String getComponentName() {		
		return componentName;
	}

	@Override
	public void postLoad() {
		// do eager initialization in order to be thread safe
		getColumns(false);
		getConstraints(false);
	}
	
	Column add(ColumnImpl column) {
		getColumns(false).add(column);
		column.setPosition(getColumns(false).size());
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
	public PrimaryKeyConstraint getPrimaryKeyConstraint() {
		for (TableConstraint each : getConstraints(false)) {
			if (each.isPrimaryKey()) {	
				return (PrimaryKeyConstraint) each;
			}				
		}
		return null;
	}
	
	@Override
	public List<ForeignKeyConstraint> getForeignKeyConstraints() {
		List<ForeignKeyConstraint> result = new ArrayList<>();
		for (TableConstraint each : getConstraints(false)) {
			if (each.isForeignKey()) {
				result.add((ForeignKeyConstraint) each);
			}				
		}
		return result;
	}
	
	@Override
	public ForeignKeyConstraint getConstraintForField(String fieldName) {
		for (ForeignKeyConstraint each : getForeignKeyConstraints()) {
			if (fieldName.equals(each.getFieldName()))  {
				return each;
			}
		}
		return null;
	}
	
	@Override
	public List<Column> getPrimaryKeyColumns() {		
		TableConstraint primaryKeyConstraint = getPrimaryKeyConstraint();
		return primaryKeyConstraint == null ? null : primaryKeyConstraint.getColumns();				
	}
	
	boolean isPrimaryKeyColumn(Column column) {
		TableConstraint primaryKeyConstraint = getPrimaryKeyConstraint();
		return primaryKeyConstraint == null ? false : primaryKeyConstraint.getColumns().contains(column);	
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
		return new TableDdlGenerator(this).getDdl();
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
	public Column addDiscriminatorColumn(String name, String dbType) {		
		return add(new ColumnImpl(this,name,dbType,true,NOCONVERSION,Column.TYPEFIELDNAME,null,false,null,null,false));
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
	public List<Column> addAuditColumns() {
		List<Column> result = new ArrayList<>();
		result.add(addVersionCountColumn("VERSIONCOUNT", "number" , "version"));
		result.add(addCreateTimeColumn("CREATETIME", "createTime"));
		result.add(addModTimeColumn("MODTIME", "modTime"));
		result.add(addUserNameColumn("USERNAME", "userName"));
		return result;
	}
	
	@Override
	public TableConstraint addPrimaryKeyConstraint(String name, Column... columns) {
		TableConstraintImpl constraint = new PrimaryKeyConstraintImpl(this, name );
		constraint.add(columns);
		getConstraints(false).add(constraint);
		return constraint;
	}

	@Override
	public TableConstraint addUniqueConstraint(String name, Column... columns) {
		TableConstraintImpl constraint = new UniqueConstraintImpl(this,  name);
		constraint.add(columns);
		getConstraints(false).add(constraint);
		return constraint;	
	}

	@Override
	public TableConstraint addForeignKeyConstraint(String name, Table referencedTable, DeleteRule deleteRule , AssociationMapping mapping , Column... columns) {
		TableConstraintImpl constraint = new ForeignKeyConstraintImpl(this , name, referencedTable , deleteRule, mapping );
		constraint.add(columns);
		getConstraints(false).add(constraint);
		return constraint;	
	}

	@Override
	public TableConstraint addForeignKeyConstraint(String name, String referencedTableName, DeleteRule deleteRule, AssociationMapping mapping, Column... columns) {
		Table referencedTable = getDataModel().getTable(referencedTableName);		
		return addForeignKeyConstraint(name, referencedTable, deleteRule , mapping, columns); 					
	}
	
	@Override
	public TableConstraint addForeignKeyConstraint(String name, String component , String referencedTableName, DeleteRule deleteRule , String fieldName, Column... columns) {
		Table referencedTable = Bus.getTable(component, referencedTableName);	
		return addForeignKeyConstraint(name, referencedTable, deleteRule , new AssociationMapping(fieldName), columns); 					
	}


	@Override
	public <T> DataMapper<T> getDataMapper(Class<T> api , Class<? extends T> implementation) {
		return new DataMapperImpl<>(api, implementation,this);
	}
		
	@Override
	public <T> DataMapper<T> getDataMapper(Class<T> api , Map<String, Class<? extends T>> implementations) {
		return new DataMapperImpl<>(api, implementations,this);
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
	public List<Column> addIntervalColumns(String fieldName) {
		List<Column> result = new ArrayList<>();
		result.add(addColumn("STARTTIME", "number",true, NUMBER2LONG , fieldName + ".start"));
		result.add(addColumn("ENDTIME", "number",true, NUMBER2LONG , fieldName + ".end"));
		return result;
	}
	
	@Override
	public List<Column> addQuantityColumns(String name , boolean notNull , String fieldName) {
		List<Column> result = new ArrayList<>();
		result.add(addColumn(name + "VALUE", "number" , notNull , NOCONVERSION , fieldName + ".value"));
		result.add(addColumn(name +  "MULTIPLIER", "number" , notNull , NUMBER2INTWRAPPER,  fieldName + ".multiplier"));
		result.add(addColumn(name +  "UNIT", "varchar2(8)" , notNull , CHAR2UNIT, fieldName + ".unit"));
		return result;
	}
	
	@Override
	public List<Column> addMoneyColumns(String name , boolean notNull , String fieldName) {	
		List<Column> result = new ArrayList<>();
		result.add(addColumn(name + "VALUE", "number" , notNull , NOCONVERSION , fieldName + ".value"));
		result.add(addColumn(name +  "CURRENCY", "number" , notNull , CHAR2CURRENCY,  fieldName + ".currency"));		
		return result;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TableImpl)) {
            return false;
        }

        TableImpl table = (TableImpl) o;

        return componentName.equals(table.componentName) && name.equals(table.name);

    }

  	@Override
	public int hashCode() {
		return componentName.hashCode() ^ name.hashCode();
	}

	@Override
	public Object getPrimaryKey(Object value) {
		TableConstraint primaryKeyConstraint = getPrimaryKeyConstraint();
		if (primaryKeyConstraint == null) {
			throw new IllegalStateException("Table has no primary key");
		}
		Object result[] = primaryKeyConstraint.getColumnValues(value);
		return primaryKeyConstraint.getColumns().size() == 1 ? result[0] : result;
		
	}

	@Override
	public FieldType getFieldType(String fieldName) {
		if (fieldName == null) {
			return null;
		}
		for (Column each : getColumns(false)) {
			if (fieldName.equals(each.getFieldName())) {
				return FieldType.SIMPLE;
			}
			if (each.getFieldName().startsWith(fieldName + ".")) {
				return FieldType.COMPLEX;
			}
		}
		for (ForeignKeyConstraint each : getForeignKeyConstraints()) {
			if (fieldName.equals(each.getFieldName())) {
				return FieldType.ASSOCIATION;
			}
		}
		for (Table table : getDataModel().getTables()) {
			if (!table.equals(this)) {
				for (ForeignKeyConstraint each : table.getForeignKeyConstraints()) {
					if (fieldName.equals(each.getReverseFieldName())) {
						return FieldType.REVERSEASSOCIATION;
					} 
					if (fieldName.equals(each.getReverseCurrentFieldName())) {
						return FieldType.CURRENTASSOCIATION;
					}
				}
			}
		}
		return null;
	}
	
	public FieldMapping getFieldMapping(String fieldName) {
		if (fieldName == null) {
			return null;
		}
		for (Column each : getColumns(false)) {
			if (fieldName.equals(each.getFieldName())) {
				return new ColumnMapping(each);
			}
			if (each.getFieldName().startsWith(fieldName + ".")) {
				return new MultiColumnMapping(fieldName, getColumns());
			}
		}
		for (ForeignKeyConstraint each : getForeignKeyConstraints()) {
			if (fieldName.equals(each.getFieldName())) {
				return new ForwardConstraintMapping(each);
			}
		}
		for (Table table : getDataModel().getTables()) {
			if (!table.equals(this)) {
				for (ForeignKeyConstraint each : table.getForeignKeyConstraints()) {
					if (fieldName.equals(each.getFieldName())) {
						return new ReverseConstraintMapping(each);
					}
				}
			}
		}
		return null;
	}

	@Override
	public void makeIndexOrganized() {
		indexOrganized = true;
		
	}

	@Override
	public boolean isIndexOrganized() {
		return indexOrganized;
	}

}
	

