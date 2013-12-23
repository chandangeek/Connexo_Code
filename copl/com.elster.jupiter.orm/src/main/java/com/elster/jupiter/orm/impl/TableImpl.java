package com.elster.jupiter.orm.impl;

import static com.elster.jupiter.orm.ColumnConversion.CHAR2CURRENCY;
import static com.elster.jupiter.orm.ColumnConversion.CHAR2PRINCIPAL;
import static com.elster.jupiter.orm.ColumnConversion.CHAR2UNIT;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INTWRAPPER;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONGNULLZERO;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2NOW;
import static com.elster.jupiter.util.Checks.is;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.FieldType;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.TableConstraint;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.fields.impl.FieldMapping;
import com.elster.jupiter.orm.fields.impl.ForwardConstraintMapping;
import com.elster.jupiter.orm.fields.impl.MultiColumnMapping;
import com.elster.jupiter.orm.fields.impl.ReverseConstraintMapping;
import com.elster.jupiter.orm.query.impl.QueryExecutorImpl;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class TableImpl implements Table {
	
	static final String JOURNALTIMECOLUMNNAME = "JOURNALTIME";
	
	// persistent fields
	private String schema;
	private String name;
	@SuppressWarnings("unused")
	private int position;
	private String journalTableName;
	private boolean cached;
	
	private boolean indexOrganized;
	
	// associations
	private final Reference<DataModelImpl> dataModel = ValueReference.absent();
	private final List<ColumnImpl> columns = new ArrayList<>();
	private final List<TableConstraintImpl> constraints = new ArrayList<>();
	
	// mapping
	private DataMapperType mapperType;
	
	// transient, protection against forgetting to call add() on a builder
	private boolean activeBuilder;
	private Class<?> api; 
	private TableCache<?> cache;
	
	// cached fields , initialized when the table's datamodel is registered with the orm service.
	private List<ForeignKeyConstraintImpl> referenceConstraints;
	private List<ForeignKeyConstraintImpl> reverseMappedConstraints;
		
	TableImpl init(DataModelImpl dataModel, String schema, String name, Class<?> api,int position) {
        assert !is(name).emptyOrOnlyWhiteSpace();
		if (name.length() > ColumnConversion.CATALOGNAMELIMIT) {
			throw new IllegalArgumentException("Name " + name + " too long" );
		}
		this.dataModel.set(Objects.requireNonNull(dataModel));
		this.schema = schema;
		this.name = Objects.requireNonNull(name);
		this.api = Objects.requireNonNull(api);
		this.position = position;
		return this;
	}
	
	static TableImpl from(DataModelImpl dataModel,String schema,String name, Class<?> api, int position) {
		return new TableImpl().init(dataModel,schema,name,api,position);
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
	public List<ColumnImpl> getColumns() {
		return ImmutableList.copyOf(columns);
	}
	
	@Override
	public List<TableConstraintImpl> getConstraints() {
		return ImmutableList.copyOf(constraints);
	}
	
	@Override
	public String toString() {
		return "Table " + name;
	}

	@Override
	public DataModelImpl getDataModel() {
		return dataModel.get();
	}

	@Override
	public String getComponentName() {		
		return getDataModel().getName();
	}
	
	@Override
	public boolean isCached() {
		return cached;
	}

	@Override
	public void cache() {
		this.cached = true;
	}

	Column add(ColumnImpl column) {
		activeBuilder = false;
		column.setPosition(columns.size()+1);
		columns.add(column);
		return column;
	}

	public ColumnImpl getColumn(String name) {
		for (ColumnImpl column : columns) {
			if (column.getName().equalsIgnoreCase(name)) {
				return column;
			}
		}
		return null;
	}

	@Override 
	public PrimaryKeyConstraintImpl getPrimaryKeyConstraint() {
		for (TableConstraintImpl each : constraints) {
			if (each.isPrimaryKey()) {	
				return (PrimaryKeyConstraintImpl) each;
			}				
		}
		return null;
	}
	
	@Override
	public List<ForeignKeyConstraintImpl> getForeignKeyConstraints() {
        ImmutableList.Builder<ForeignKeyConstraintImpl> builder = ImmutableList.builder();
		for (TableConstraintImpl each : constraints) {
			if (each.isForeignKey()) {
				builder.add((ForeignKeyConstraintImpl) each);
			}				
		}
		return builder.build();
	}
	
	public ForeignKeyConstraintImpl getConstraintForField(String fieldName) {
		for (ForeignKeyConstraintImpl each : getForeignKeyConstraints()) {
			if (fieldName.equals(each.getFieldName()))  {
				return each;
			}
		}
		return null;
	}
	
	@Override
	public List<ColumnImpl> getPrimaryKeyColumns() {
		TableConstraintImpl primaryKeyConstraint = getPrimaryKeyConstraint();
		return primaryKeyConstraint == null ? null : primaryKeyConstraint.getColumns();				
	}
	
	boolean isPrimaryKeyColumn(Column column) {
		TableConstraint primaryKeyConstraint = getPrimaryKeyConstraint();
		return primaryKeyConstraint != null && primaryKeyConstraint.getColumns().contains(column);
	}
	
	ColumnImpl[] getVersionColumns() {
		List<Column> result = new ArrayList<>();
		for (ColumnImpl column : columns) {
			if (column.isVersion()) {
				result.add(column);
			}
		}		
		return result.toArray(new ColumnImpl[result.size()]);		
	}

	List<ColumnImpl> getInsertValueColumns() {
		List<ColumnImpl> result = new ArrayList<>();
		for (ColumnImpl column : columns) {
			if (column.hasInsertValue()) {
				result.add(column);
			}
		}		
		return result;		
	}
	
	List<ColumnImpl> getUpdateValueColumns() {
		List<ColumnImpl> result = new ArrayList<>();
		for (ColumnImpl column : columns) {
			if (column.hasUpdateValue()) {
				result.add(column);
			}
		}		
		return result;		
	}
	
	List<ColumnImpl> getStandardColumns() {
		List<ColumnImpl> result = new ArrayList<>();
		for (ColumnImpl column : columns) {
			if (column.isStandard()) {
				result.add(column);
			}
		}		
		return result;		
	}		
		
	ColumnImpl[] getAutoUpdateColumns() {
		List<Column> result = new ArrayList<>();
		for (ColumnImpl column : columns) {
			if (column.hasAutoValue(true)) {
				result.add(column);
			}
		}		
		return result.toArray(new ColumnImpl[result.size()]);
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
	public Column addVersionCountColumn(String name, String dbType , String fieldName) {
		return column(name).type(dbType).notNull().version().conversion(NUMBER2LONG).map(fieldName).add();
	}

	@Override
	public Column addDiscriminatorColumn(String name, String dbType) {
		return column(name).type(dbType).notNull().map(Column.TYPEFIELDNAME).add();
	}

	@Override
	public Column addCreateTimeColumn(String name , String fieldName) {
		return column(name).number().notNull().conversion(NUMBER2NOW).skipOnUpdate().map(fieldName).add();
	}
	
	@Override
	public Column addModTimeColumn(String name , String fieldName) {
		return column(name).number().notNull().conversion(NUMBER2NOW).map(fieldName).add();
	}
	
	@Override
	public Column addUserNameColumn(String name , String fieldName) {
		return column(name).varChar(80).notNull().conversion(CHAR2PRINCIPAL).map(fieldName).add();
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
	
	TableConstraint add(TableConstraintImpl constraint) {
		activeBuilder = false;
		constraints.add(constraint);
		return constraint;
	}
	
	public DataMapperImpl<?> getDataMapper() {
		return getDataMapper(api);
	}
	
	Class<?> getApi() {
		return api;
	}
	
	<T> DataMapperImpl<T> getDataMapper(Class<T> api) {
		if (mapperType == null) {
			throw new IllegalStateException("Implementation not specified");
		}
		return new DataMapperImpl<>(api,mapperType,this);
	}
	
	@SuppressWarnings("unchecked")
	public <T> QueryExecutorImpl<T> getQuery() {
		List<TableImpl> related = new ArrayList<>();
		addAllRelated(related);
		related.remove(0);
		List<DataMapperImpl<?>> mappers = new ArrayList<>(related.size());
		for (TableImpl each : related) {
			mappers.add(each.getDataMapper());
		}
		return (QueryExecutorImpl<T>) getDataMapper(api).with(mappers.toArray(new DataMapperImpl<?>[mappers.size()]));
	}

	private void addAllRelated(List<TableImpl> related) {
		related.add(this);
		for (ForeignKeyConstraintImpl each : this.getForeignKeyConstraints()) {
			TableImpl table = each.getReferencedTable();
			if (!related.contains(table)) {
				table.addAllRelated(related);
			}
		}
		for (ForeignKeyConstraintImpl each : this.getReverseMappedConstraints()) {
			TableImpl table = each.getTable();
			if (!related.contains(table)) {
				table.addAllRelated(related);
			}
		}
	}
	
	public ColumnImpl getColumnForField(String name) {
		for (ColumnImpl column : columns) {
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
		String sequence = name + "ID";
		if (sequence.length() > ColumnConversion.CATALOGNAMELIMIT) {
			throw new IllegalStateException("Name " + sequence + " too long");
		}
		return column("ID").number().notNull().conversion(ColumnConversion.NUMBER2LONG).sequence(name + "ID").skipOnUpdate().map("id").add();
	}

	@Override
	public List<Column> addIntervalColumns(String fieldName) {
        ImmutableList.Builder<Column> builder = ImmutableList.builder();
		builder.add(column("STARTTIME").number().notNull().conversion(NUMBER2LONG).map(fieldName + ".start").add());
		builder.add(column("ENDTIME").number().notNull().conversion(NUMBER2LONG).map(fieldName + ".end").add());
		return builder.build();
	}
	
	@Override
	public List<Column> addQuantityColumns(String name , boolean notNull , String fieldName) {
        ImmutableList.Builder<Column> builder = ImmutableList.builder();
		builder.add(column(name + "VALUE").number().notNull(notNull).map(fieldName + ".value").add());
		builder.add(column(name + "MULTIPLIER").number().notNull(notNull).conversion(NUMBER2INTWRAPPER).map(fieldName + ".multiplier").add());
		builder.add(column(name + "UNIT").varChar(8).notNull(notNull).conversion(CHAR2UNIT).map(fieldName + ".unit").add());
		return builder.build();
	}
	
	@Override
	public ImmutableList<Column> addMoneyColumns(String name , boolean notNull , String fieldName) {
        ImmutableList.Builder<Column> builder = ImmutableList.builder();
		builder.add(column(name + "VALUE").number().notNull(notNull).map(fieldName + ".value").add());
		builder.add(column(name + "CURRENCY").number(). notNull(notNull).conversion(CHAR2CURRENCY).map(fieldName + ".currency").add());
		return builder.build();
	}


	@Override
	public List<Column> addRefAnyColumns(String name , boolean notNull , String fieldName) {
        ImmutableList.Builder<Column> builder = ImmutableList.builder();
		builder.add(column(name + "CMP").varChar(23).notNull(notNull).map(fieldName + ".component").add());
		builder.add(column(name + "TABLE").varChar(28).notNull(notNull).map(fieldName + ".table").add());
		builder.add(column(name + "KEY").varChar(255).notNull(notNull).map(fieldName + ".key").add());
		builder.add(column(name + "ID").number().notNull(notNull).conversion(NUMBER2LONGNULLZERO).map(fieldName + ".id").add());
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
		if (name.length() > ColumnConversion.CATALOGNAMELIMIT) {
			throw new IllegalArgumentException(
				Joiner.on(" ").join("Column name",name,"too long(",name.length()," > ",ColumnConversion.CATALOGNAMELIMIT,")")); 
		}
		activeBuilder = true;
		return new ColumnImpl.BuilderImpl(ColumnImpl.from(this,name));
	}
	
	@Override
	public PrimaryKeyConstraintImpl.BuilderImpl primaryKey(String name) {
		checkActiveBuilder();
		activeBuilder = true;
		return new PrimaryKeyConstraintImpl.BuilderImpl(this,name);		
	}
	
	@Override
	public UniqueConstraintImpl.BuilderImpl unique(String name) {
		checkActiveBuilder();
		activeBuilder = true;
		return new UniqueConstraintImpl.BuilderImpl(this,name);		
	}
	
	@Override
	public ForeignKeyConstraintImpl.BuilderImpl foreignKey(String name) {
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

	public Object[] getPrimaryKey(Object value) {
		TableConstraintImpl primaryKeyConstraint = getPrimaryKeyConstraint();
		if (primaryKeyConstraint == null) {
			throw new IllegalStateException("Table has no primary key");
		}
		return primaryKeyConstraint.getColumnValues(value);				
	}
	
	public FieldType getFieldType(String fieldName) {
		if (fieldName == null) {
			return null;
		}
		for (Column each : columns) {
			if (fieldName.equals(each.getFieldName())) {
				return FieldType.SIMPLE;
			}
			if (each.getFieldName().startsWith(fieldName + ".")) {
				return FieldType.COMPLEX;
			}
		}
		for (ForeignKeyConstraintImpl each : getForeignKeyConstraints()) {
			if (fieldName.equals(each.getFieldName())) {
				return FieldType.ASSOCIATION;
			}
		}
		for (TableImpl table : getDataModel().getTables()) {
			if (!table.equals(this)) {
				for (ForeignKeyConstraintImpl each : table.getForeignKeyConstraints()) {
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
	
	List<ForeignKeyConstraintImpl> getReferenceConstraints() {
		return referenceConstraints;
	}
	
	List<ForeignKeyConstraintImpl> getReverseMappedConstraints() {
		return reverseMappedConstraints;
	}
	
	public FieldMapping getFieldMapping(String fieldName) {
		if (fieldName == null) {
			return null;
		}
		for (ColumnImpl each : columns) {
			if (each.getFieldName() != null) {
				if (fieldName.equals(each.getFieldName())) {
					return new ColumnMapping(each);
				}
				if (each.getFieldName().startsWith(fieldName + ".")) {
					return new MultiColumnMapping(fieldName, getColumns());
				}
			}
		}
		for (ForeignKeyConstraintImpl each : getForeignKeyConstraints()) {
			if (fieldName.equals(each.getFieldName())) {
				return new ForwardConstraintMapping(each);
			}
		}
		for (TableImpl table : getDataModel().getTables()) {
			if (!table.equals(this)) {
				for (ForeignKeyConstraintImpl each : table.getForeignKeyConstraints()) {
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

	public Optional<?> getOptional(Object... primaryKeyValues) {
		return getDataMapper(api).getOptional(primaryKeyValues);
	}

	@Override
	public void map(Class<?> implementation) {
		if (this.mapperType != null) {
			throw new IllegalStateException("Implementer(s) already specified");
		}
		if (!api.isAssignableFrom(implementation)) {
			throw new IllegalArgumentException("" + implementation + " does not implement " + api);
		}
		this.mapperType = new SingleDataMapperType(implementation);
	}

	@Override
	public <T> void map(Map<String, Class<? extends T>> implementations) {
		if (this.mapperType != null) {
			throw new IllegalStateException("Implementer(s) already specified");
		}
		if (Objects.requireNonNull(implementations).isEmpty()) {
			throw new IllegalArgumentException("Empty map");
		}
		for (Class<?> implementation : implementations.values()) {
			if (!api.isAssignableFrom(implementation)) {
				throw new IllegalArgumentException("" + implementation + " does not implement " + api);
			}
		}
		this.mapperType = new InheritanceDataMapperType<>(implementations);
	}

	@Override
	public boolean maps(Class<?> clazz) {
		return api.isAssignableFrom(clazz);
	}

	void prepare() {
		checkActiveBuilder();
		Objects.requireNonNull(mapperType,"No implementation has been set");
		mapperType.init(getDataModel().getInjector());
		buildReferenceConstraints();
		buildReverseMappedConstraints();
		for (Column column : getColumns()) {
			checkMapped(column);
		}
		cache = isCached() ? new TableCache.TupleCache<>(this) : new TableCache.NoCache<>();
	}
	
	private void checkMapped (Column column) {
		if (column.getFieldName() == null) {
			for (ForeignKeyConstraintImpl constraint : getReferenceConstraints()) {
				if (constraint.hasColumn(column)) {
					return;
				}
			}
		} else {
			if (mapperType.getType(column.getFieldName()) == null) {
				throw new IllegalStateException(
					Joiner.on(" ").
						join("No field available for column",column.getName(),"mapped by",column.getFieldName()));
			} else {
				return;
			}
		}
		throw new IllegalStateException("Column " + column.getName() + " has no mapping");
	}
	
	@SuppressWarnings("unused")
	private void buildReferenceConstraints() {
		ImmutableList.Builder<ForeignKeyConstraintImpl> builder = new ImmutableList.Builder<>();
		for (ForeignKeyConstraintImpl constraint : getForeignKeyConstraints()) {
			if (mapperType.isReference(constraint.getFieldName())) {
				Field field = mapperType.getField(constraint.getFieldName());
				//if (Modifier.isFinal(field.getModifiers())) {
				if (true) {
					builder.add(constraint);
				} else {
					throw new IllegalStateException(
						Joiner.on(" ").join(
								"Reference field", constraint.getFieldName(), 
								"for constraint", constraint.getName() , "is not final"));
				}
			}
		}
		this.referenceConstraints = builder.build();		
	}
	
	private List<ForeignKeyConstraintImpl> getReverseConstraints() {
		ImmutableList.Builder<ForeignKeyConstraintImpl> builder = new ImmutableList.Builder<>();
		for (TableImpl table : getDataModel().getTables()) {
			if (!table.equals(this)) {
				for (ForeignKeyConstraintImpl each : table.getForeignKeyConstraints()) {
					if (each.getReferencedTable().equals(this)) {
						builder.add(each);
					}
				}
			}
		}
		return builder.build();
	}

	private void buildReverseMappedConstraints() {
		ImmutableList.Builder<ForeignKeyConstraintImpl> builder = new ImmutableList.Builder<>();
		for (ForeignKeyConstraintImpl each : getReverseConstraints()) {
			if (each.getReferencedTable().equals(this) && each.getReverseFieldName() != null) {
				if (each.isComposition()) {
					/*
					Field field = mapperType.getField(each.getReverseFieldName());
					if (!Modifier.isFinal(field.getModifiers())) {
						throw new IllegalStateException(
							Joiner.on(" ").join(
								"Reverse Field", each.getReverseFieldName(), 
								"for composition constraint", each.getName() , "is not final"));
					}
					*/
				}
				builder.add(each);
			}
		}
		this.reverseMappedConstraints = builder.build();	
	}
	
	boolean hasChildren() {
		for (ForeignKeyConstraintImpl constraint : reverseMappedConstraints) {
			if (constraint.isComposition()) {
				return true;
			}
		}
		return false;
	}
	
	boolean isChild() {
		for (ForeignKeyConstraintImpl constraint : getForeignKeyConstraints()) {
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
	
	@SuppressWarnings("unchecked")
	<T> TableCache<T> getCache() {
		return (TableCache<T>) cache;
	}
	
	Field getField(String fieldName) {
		return mapperType.getField(fieldName);
	}
}
	

