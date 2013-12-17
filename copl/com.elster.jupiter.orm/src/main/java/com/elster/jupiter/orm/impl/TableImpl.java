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
import com.elster.jupiter.orm.UniqueConstraint;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.fields.impl.FieldMapping;
import com.elster.jupiter.orm.fields.impl.ForwardConstraintMapping;
import com.elster.jupiter.orm.fields.impl.MultiColumnMapping;
import com.elster.jupiter.orm.fields.impl.ReverseConstraintMapping;
import com.elster.jupiter.orm.internal.Bus;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.elster.jupiter.orm.ColumnConversion.*;
import static com.elster.jupiter.util.Checks.is;

public class TableImpl implements Table {
	
	static final String JOURNALTIMECOLUMNNAME = "JOURNALTIME";
	
	// persistent fields
	private String schema;
	private String name;
	@SuppressWarnings("unused")
	private int position;
	private String journalTableName;
	private boolean indexOrganized;
	
	// associations
	private Reference<DataModel> dataModel;
	private final List<Column> columns = new ArrayList<>();
	private final List<TableConstraint> constraints = new ArrayList<>();
	
	// mapping
	private DataMapperType mapperType;
	
	// transient, protection against forgetting to call add() on a builder
	private transient boolean activeBuilder;
	
	// cached fields , initialized when the table's datamodel is registered with the orm service.
	private List<ForeignKeyConstraint> referenceConstraints = ImmutableList.of();
	private List<ForeignKeyConstraint> reverseConstraints = ImmutableList.of();
		
	TableImpl(DataModel dataModel, String schema, String name, int position) {
        assert !is(name).emptyOrOnlyWhiteSpace();
		if (name.length() > Bus.CATALOGNAMELIMIT) {
			throw new IllegalArgumentException("Name " + name + " too long" );
		}
		this.dataModel = ValueReference.of(dataModel);
		this.schema = schema;
		this.name = name;
		this.position = position;
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
		return is(schema).emptyOrOnlyWhiteSpace() ? value : schema + "." + value;
	}
	
	@Override
	public List<Column> getColumns() {
		return ImmutableList.copyOf(doGetColumns());
	}
	
	private List<Column> doGetColumns() {
		return columns;
	}
	
	@Override
	public List<TableConstraint> getConstraints() {
		return ImmutableList.copyOf(doGetConstraints());
	}
	
	private List<TableConstraint> doGetConstraints() {
		return constraints;
	}
	
	@Override
	public String toString() {
		return "Table " + name;
	}

	@Override
	public DataModel getDataModel() {
		return dataModel.get();
	}

	@Override
	public String getComponentName() {		
		return getDataModel().getName();
	}

	Column add(ColumnImpl column) {
		activeBuilder = false;
		doGetColumns().add(column);
		column.setPosition(doGetColumns().size());
		return column;
	}

	@Override
	public Column getColumn(String name) {
		for (Column column : doGetColumns()) {
			if (column.getName().equalsIgnoreCase(name)) {
				return column;
			}
		}
		return null;
	}

	@Override 
	public PrimaryKeyConstraint getPrimaryKeyConstraint() {
		for (TableConstraint each : doGetConstraints()) {
			if (each.isPrimaryKey()) {	
				return (PrimaryKeyConstraint) each;
			}				
		}
		return null;
	}
	
	@Override
	public List<ForeignKeyConstraint> getForeignKeyConstraints() {
        ImmutableList.Builder<ForeignKeyConstraint> builder = ImmutableList.builder();
		for (TableConstraint each : doGetConstraints()) {
			if (each.isForeignKey()) {
				builder.add((ForeignKeyConstraint) each);
			}				
		}
		return builder.build();
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
		return primaryKeyConstraint != null && primaryKeyConstraint.getColumns().contains(column);
	}
	
	Column[] getVersionColumns() {
		List<Column> result = new ArrayList<>();
		for (Column column : doGetColumns()) {
			if (column.isVersion()) {
				result.add(column);
			}
		}		
		return result.toArray(new Column[result.size()]);		
	}

	List<Column> getInsertValueColumns() {
		List<Column> result = new ArrayList<>();
		for (Column column : doGetColumns()) {
			if (column.hasInsertValue()) {
				result.add(column);
			}
		}		
		return result;		
	}
	
	List<Column> getUpdateValueColumns() {
		List<Column> result = new ArrayList<>();
		for (Column column : doGetColumns()) {
			if (column.hasUpdateValue()) {
				result.add(column);
			}
		}		
		return result;		
	}
	
	List<Column> getStandardColumns() {
		List<Column> result = new ArrayList<>();
		for (Column column : doGetColumns()) {
			if (((ColumnImpl) column).isStandard()) {
				result.add(column);
			}
		}		
		return result;		
	}		
		
	Column[] getAutoUpdateColumns() {
		List<Column> result = new ArrayList<>();
		for (Column column : doGetColumns()) {
			if (((ColumnImpl) column).hasAutoValue(true)) {
				result.add(column);
			}
		}		
		return result.toArray(new Column[result.size()]);
	}
	
	@Override
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
		checkActiveBuilder();
		return add(new ColumnImpl(this,name,dbType, true, conversion,fieldName,sequence,false,null,null, skipOnUpdate));
	}
	
	@Override
	public Column addVersionCountColumn(String name, String dbType , String fieldName) {
		checkActiveBuilder();
		return add(new ColumnImpl(this,name,dbType, true , NUMBER2LONG,fieldName, null, true,null,null,false));
	}

	@Override
	public Column addDiscriminatorColumn(String name, String dbType) {
		checkActiveBuilder();
		return add(new ColumnImpl(this,name,dbType,true,NOCONVERSION,Column.TYPEFIELDNAME,null,false,null,null,false));
	}

	@Override
	public Column addCreateTimeColumn(String name , String fieldName) {
		checkActiveBuilder();
		return add(new ColumnImpl(this,name,"number", true , NUMBER2NOW, fieldName, null, false, null , null , true));
	}
	
	@Override
	public Column addModTimeColumn(String name , String fieldName) {
		checkActiveBuilder();
		return add(new ColumnImpl(this,name,"number", true , NUMBER2NOW, fieldName, null, false, null , null , false));
	}
	
	@Override
	public Column addUserNameColumn(String name , String fieldName) {
		checkActiveBuilder();
		return add(new ColumnImpl(this,name,"varchar2(80)", true , CHAR2PRINCIPAL, fieldName, null, false, null , null , false));
	}
	
	@Override
	public List<Column> addAuditColumns() {
        ImmutableList.Builder<Column> builder = ImmutableList.builder();
		builder.add(addVersionCountColumn("VERSIONCOUNT", "number" , "version"));
        builder.add(addCreateTimeColumn("CREATETIME", "createTime"));
        builder.add(addModTimeColumn("MODTIME", "modTime"));
        builder.add(addUserNameColumn("USERNAME", "userName"));
		return builder.build();
	}
	
	TableConstraint add(TableConstraint constraint) {
		activeBuilder = false;
		doGetConstraints().add(constraint);
		return constraint;
	}
	@Override
	public TableConstraint addPrimaryKeyConstraint(String name, Column... columns) {
		checkActiveBuilder();
		TableConstraintImpl constraint = new PrimaryKeyConstraintImpl(this, name );
		constraint.add(columns);
		return add(constraint);
	}

	@Override
	public TableConstraint addUniqueConstraint(String name, Column... columns) {
		checkActiveBuilder();
		TableConstraintImpl constraint = new UniqueConstraintImpl(this,  name);
		constraint.add(columns);
		return add(constraint);	
	}

	@Override
	public TableConstraint addForeignKeyConstraint(String name, Table referencedTable, DeleteRule deleteRule , AssociationMapping mapping , Column... columns) {
		checkActiveBuilder();
		TableConstraintImpl constraint = new ForeignKeyConstraintImpl(this , name, referencedTable , deleteRule, mapping );
		constraint.add(columns);
		return add(constraint);	
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
	public <T> DataMapper<T> getDataMapper(Class<T> api) {
		if (mapperType == null) {
			throw new IllegalStateException("Implementation not specified");
		}
		return new DataMapperImpl<>(api,mapperType,this);
	}
		
	@Override
	public <T> DataMapper<T> getDataMapper(Class<T> api , Class<? extends T> implementation) {
		return new DataMapperImpl<>(api, new SingleDataMapperType(implementation) ,this);
	}
		
	@Override
	public <T> DataMapper<T> getDataMapper(Class<T> api , Map<String, Class<? extends T>> implementations) {
		return new DataMapperImpl<>(api, new InheritanceDataMapperType<>(implementations), this);
	}

	@Override
	public Column getColumnForField(String name) {
		for (Column column : doGetColumns()) {
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
        ImmutableList.Builder<Column> builder = ImmutableList.builder();
		builder.add(addColumn("STARTTIME", "number", true, NUMBER2LONG, fieldName + ".start"));
		builder.add(addColumn("ENDTIME", "number", true, NUMBER2LONG, fieldName + ".end"));
		return builder.build();
	}
	
	@Override
	public List<Column> addQuantityColumns(String name , boolean notNull , String fieldName) {
        ImmutableList.Builder<Column> builder = ImmutableList.builder();
		builder.add(addColumn(name + "VALUE", "number", notNull, NOCONVERSION, fieldName + ".value"));
		builder.add(addColumn(name + "MULTIPLIER", "number", notNull, NUMBER2INTWRAPPER, fieldName + ".multiplier"));
		builder.add(addColumn(name + "UNIT", "varchar2(8)", notNull, CHAR2UNIT, fieldName + ".unit"));
		return builder.build();
	}
	
	@Override
	public ImmutableList<Column> addMoneyColumns(String name , boolean notNull , String fieldName) {
        ImmutableList.Builder<Column> builder = ImmutableList.builder();
		builder.add(addColumn(name + "VALUE", "number", notNull, NOCONVERSION, fieldName + ".value"));
		builder.add(addColumn(name + "CURRENCY", "number", notNull, CHAR2CURRENCY, fieldName + ".currency"));
		return builder.build();
	}


	@Override
	public List<Column> addRefAnyColumns(String name , boolean notNull , String fieldName) {
        ImmutableList.Builder<Column> builder = ImmutableList.builder();
		builder.add(addColumn(name + "CMP", "varchar2(3)", notNull, NOCONVERSION, fieldName + ".component"));
		builder.add(addColumn(name + "TABLE", "varchar2(28)", notNull, NOCONVERSION , fieldName + ".table"));
		builder.add(addColumn(name + "KEY", "varchar2(4000)", notNull, NOCONVERSION , fieldName + ".key"));
		builder.add(addColumn(name + "ID", "number", notNull, NUMBER2LONGNULLZERO, fieldName + ".id"));
		return builder.build();
	}
	void checkActiveBuilder() {
		if (activeBuilder) {
			throw new IllegalStateException("Builder in progress. Invoke add() first");
		}
	}
	
	@Override
	public Column.Builder column(String name) {
		checkActiveBuilder();
		activeBuilder = true;
		return new ColumnImpl.BuilderImpl(this,name);
	}
	@Override
	public PrimaryKeyConstraint.Builder primaryKey(String name) {
		checkActiveBuilder();
		activeBuilder = true;
		return new PrimaryKeyConstraintImpl.BuilderImpl(this,name);		
	}
	
	@Override
	public UniqueConstraint.Builder unique(String name) {
		checkActiveBuilder();
		activeBuilder = true;
		return new UniqueConstraintImpl.BuilderImpl(this,name);		
	}
	
	@Override
	public ForeignKeyConstraint.Builder foreignKey(String name) {
		checkActiveBuilder();
		activeBuilder = true;
		return new ForeignKeyConstraintImpl.BuilderImpl(this,name);		
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

        return name.equals(table.name) && this.getDataModel().equals(table.getDataModel());

    }

  	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public Object[] getPrimaryKey(Object value) {
		TableConstraint primaryKeyConstraint = getPrimaryKeyConstraint();
		if (primaryKeyConstraint == null) {
			throw new IllegalStateException("Table has no primary key");
		}
		return primaryKeyConstraint.getColumnValues(value);				
	}

	@Override
	public FieldType getFieldType(String fieldName) {
		if (fieldName == null) {
			return null;
		}
		for (Column each : doGetColumns()) {
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
	
	List<ForeignKeyConstraint> getReferenceConstraints() {
		return referenceConstraints;
	}
	
	List<ForeignKeyConstraint> getReverseConstraints() {
		return reverseConstraints;
	}
	
	public FieldMapping getFieldMapping(String fieldName) {
		if (fieldName == null) {
			return null;
		}
		for (Column each : doGetColumns()) {
			if (each.getFieldName() != null) {
				if (fieldName.equals(each.getFieldName())) {
					return new ColumnMapping(each);
				}
				if (each.getFieldName().startsWith(fieldName + ".")) {
					return new MultiColumnMapping(fieldName, getColumns());
				}
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

	@Override
	public Optional<Object> get(Object... primaryKeyValues) {
		return getDataMapper(Object.class).get(primaryKeyValues);
	}


	@Override
	public void map(Class<?> implementation) {
		if (this.mapperType != null) {
			throw new IllegalStateException("Implementer(s) already specified");
		}
		this.mapperType = new SingleDataMapperType(implementation);
	}

	@Override
	public <T> void map(Map<String, Class<? extends T>> implementations) {
		if (this.mapperType != null) {
			throw new IllegalStateException("Implementer(s) already specified");
		}
		this.mapperType = new InheritanceDataMapperType<>(implementations);
	}

	@Override
	public boolean maps(Class<?> clazz) {
		return mapperType != null && mapperType.maps(clazz);
	}

	void prepare() {
		checkActiveBuilder();
		if (mapperType != null) {
			buildReferenceConstraints();
			buildReverseConstraints();
		}
		for (Column column : getColumns()) {
			checkMapped(column);
		}
	}
	
	private void checkMapped (Column column) {
		if (column.getFieldName() != null) {
			return;
		}
		for (ForeignKeyConstraint constraint : getReferenceConstraints()) {
			if (constraint.hasColumn(column)) {
				return;
			}
		}
		throw new IllegalStateException("Column " + column.getName() + " is not mapped");
	}
	
	private void buildReferenceConstraints() {
		ImmutableList.Builder<ForeignKeyConstraint> builder = new ImmutableList.Builder<>();
		for (ForeignKeyConstraint constraint : getForeignKeyConstraints()) {
			if (mapperType.isReference(constraint.getFieldName())) {
				builder.add(constraint);
			}
		}
		this.referenceConstraints = builder.build();		
	}

	private void buildReverseConstraints() {
		ImmutableList.Builder<ForeignKeyConstraint> builder = new ImmutableList.Builder<>();
		for (Table table : getDataModel().getTables()) {
			if (!table.equals(this)) {
				for (ForeignKeyConstraint each : table.getForeignKeyConstraints()) {
					if (each.getReferencedTable().equals(this) && each.getReverseFieldName() != null) {
						builder.add(each);
					}
				}
			}
		}
		this.reverseConstraints = builder.build();	
	}
	
	boolean hasChildren() {
		for (ForeignKeyConstraint constraint : reverseConstraints) {
			if (constraint.isComposition()) {
				return true;
			}
		}
		return false;
	}
	
	boolean isChild() {
		for (ForeignKeyConstraint constraint : getForeignKeyConstraints()) {
			if (constraint.isComposition()) {
				return true;
			}
		}
		return false;
	}
	
	boolean hasAutoIncrementColumns() {
		for (Column column : getColumns()) {
			if (column.isAutoIncrement()) {
				return true;
			}
		}
		return false;
	}
}
	

