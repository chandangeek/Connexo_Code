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
import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.FieldType;
import com.elster.jupiter.orm.IllegalTableMappingException;
import com.elster.jupiter.orm.LifeCycleClass;
import com.elster.jupiter.orm.MappingException;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.TableConstraint;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.fields.impl.ColumnMapping;
import com.elster.jupiter.orm.fields.impl.FieldMapping;
import com.elster.jupiter.orm.fields.impl.ForwardConstraintMapping;
import com.elster.jupiter.orm.fields.impl.MultiColumnMapping;
import com.elster.jupiter.orm.fields.impl.ReverseConstraintMapping;
import com.elster.jupiter.orm.query.impl.QueryExecutorImpl;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

public class TableImpl<T> implements Table<T> {
	
	static final String JOURNALTIMECOLUMNNAME = "JOURNALTIME";
	static final String MODTIMECOLUMNAME = "MODTIME";
	
	// persistent fields
	private String schema;
	private String name;
	@SuppressWarnings("unused")
	private int position;
	private String journalTableName;
	private boolean cached;
    private boolean autoInstall = true;
    private int indexOrganized = -1;
	
	// associations
	private final Reference<DataModelImpl> dataModel = ValueReference.absent();
	private final List<ColumnImpl> columns = new ArrayList<>();
	private final List<TableConstraintImpl> constraints = new ArrayList<>();
	private final List<IndexImpl> indexes = new ArrayList<>();
	
	private Optional<Column> partitionColumn = Optional.empty();
	private LifeCycleClass lifeCycleClass = LifeCycleClass.NONE;
	
	// mapping
	private DataMapperType<T> mapperType;
	
	// transient, protection against forgetting to call add() on a builder
	private boolean activeBuilder;
	private Class<T> api; 
	private TableCache<T> cache;
	
	// cached fields , initialized when the table's datamodel is registered with the orm service.
	private List<ForeignKeyConstraintImpl> referenceConstraints;
	private List<ForeignKeyConstraintImpl> reverseMappedConstraints;
	private List<ColumnImpl> realColumns;
		
	TableImpl<T> init(DataModelImpl dataModel, String schema, String name, Class<T> api) {
        assert !is(name).emptyOrOnlyWhiteSpace();
        if (name.length() > ColumnConversion.CATALOGNAMELIMIT) {
            throw new IllegalArgumentException("Name " + name + " too long");
        }
        this.dataModel.set(Objects.requireNonNull(dataModel));
        this.schema = schema;
        this.name = Objects.requireNonNull(name);
        this.api = Objects.requireNonNull(api);
        return this;
    }

    static <T> TableImpl<T> from(DataModelImpl dataModel, String schema, String name, Class<T> api) {
        return new TableImpl<T>().init(dataModel, schema, name, api);
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

    @Override
    public boolean isAutoInstall() {
        return autoInstall;
    }

    @Override
    public void doNotAutoInstall() {
        this.autoInstall = false;
    }

    Column add(ColumnImpl column) {
        activeBuilder = false;
        if (column.getFieldName() != null) {
            for (ColumnImpl existing : columns) {
                if (is(existing.getFieldName()).equalTo(column.getFieldName())) {
                    throw new IllegalTableMappingException("Table " + getName() + ", column " + column.getName() + " : column " + existing.getName() + " already maps to field " + existing.getFieldName());
                }
            }
        }
        columns.add(column);
        return column;
    }

    @Override
    public java.util.Optional<ColumnImpl> getColumn(String name) {
    	return columns.stream().filter((column) -> column.getName().equalsIgnoreCase(name)).findFirst(); 
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
            if (fieldName.equals(each.getFieldName())) {
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

    List<ColumnImpl> getAutoUpdateColumns() {
        List<ColumnImpl> result = new ArrayList<>();
        for (ColumnImpl column : columns) {
            if (column.hasAutoValue(true)) {
                result.add(column);
            }
        }
        return result;
    }

    @Override
    public List<String> getDdl() {
        return new TableDdlGenerator(this,getDataModel().getSqlDialect()).getDdl();
    }

    public List<String> upgradeDdl(TableImpl<?> toTable) {
        return new TableDdlGenerator(this,getDataModel().getSqlDialect()).upgradeDdl(toTable);
    }

    public List<String> upgradeSequenceDdl(ColumnImpl column, long startValue) {
        return new TableDdlGenerator(this,getDataModel().getSqlDialect()).upgradeSequenceDdl(column, startValue);
    }


    String getExtraJournalPrimaryKeyColumnNames() {
    	String base = JOURNALTIMECOLUMNNAME;
        Column[] versionColumns = getVersionColumns();
        return versionColumns.length > 0 ? String.join(",", base, versionColumns[0].getName()) : base;
    }

    @Override
    public Column addVersionCountColumn(String name, String dbType, String fieldName) {
        return column(name).type(dbType).notNull().version().conversion(NUMBER2LONG).map(fieldName).add();
    }

    @Override
    public Column addDiscriminatorColumn(String name, String dbType) {
        return column(name).type(dbType).notNull().map(Column.TYPEFIELDNAME).add();
    }

    @Override
    public Column addCreateTimeColumn(String name, String fieldName) {
        return column(name).number().notNull().conversion(NUMBER2NOW).skipOnUpdate().map(fieldName).add();
    }

    @Override
    public Column addModTimeColumn(String name, String fieldName) {
        return column(name).number().notNull().conversion(NUMBER2NOW).map(fieldName).add();
    }

    @Override
    public Column addUserNameColumn(String name, String fieldName) {
        return column(name).varChar(80).notNull().conversion(CHAR2PRINCIPAL).map(fieldName).add();
    }

    @Override
    public List<Column> addAuditColumns() {
        ImmutableList.Builder<Column> builder = ImmutableList.builder();
        builder.add(addVersionCountColumn("VERSIONCOUNT", "number", "version"));
        builder.add(addCreateTimeColumn("CREATETIME", "createTime"));
        builder.add(addModTimeColumn(MODTIMECOLUMNAME, "modTime"));
        builder.add(addUserNameColumn("USERNAME", "userName"));
        return builder.build();
    }

    TableConstraint add(TableConstraintImpl constraint) {
        activeBuilder = false;
        constraints.add(constraint);
        return constraint;
    }

    public DataMapperImpl<T> getDataMapper() {
        return getDataMapper(api);
    }

    Class<?> getApi() {
        return api;
    }

    @SuppressWarnings("unchecked")
    <S> DataMapperImpl<S> getDataMapper(Class<S> api) {
        if (getMapperType().getInjector() == null) {
            throw new IllegalStateException("Datamodel not registered");
        }
        if (maps(api)) {
            return new DataMapperImpl<S>(api, (TableImpl<? super S>) this);
        } else {
            throw new IllegalArgumentException("Table " + getName() + " does not map " + api);
        }
    }

    public <S extends T> QueryExecutorImpl<S> getQuery(Class<S> type) {
        List<TableImpl<?>> related = new ArrayList<>();
        addAllRelated(related);
        related.remove(0);
        DataMapperImpl<?>[] mappers = new DataMapperImpl<?>[related.size()];
        for (int i = 0 ; i < related.size() ; i++) {
        	mappers[i] = related.get(i).getDataMapper();
        }
        return getDataMapper(type).with(mappers);
    }

    public QueryExecutorImpl<T> getQuery() {
        return getQuery(api);
    }

    private void addAllRelated(List<TableImpl<?>> related) {
        related.add(this);
        for (ForeignKeyConstraintImpl each : this.getForeignKeyConstraints()) {
            TableImpl<?> table = each.getReferencedTable();
            if (!related.contains(table)) {
                table.addAllRelated(related);
            }
        }
        for (ForeignKeyConstraintImpl each : this.getReverseMappedConstraints()) {
            TableImpl<?> table = each.getTable();
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
        if (hasAutoChange()) {
            throw new IllegalStateException(" A table with foreign key using cascading or set null delete rule cannot have a journal table ");
        }
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
    public Column addPositionColumn() {
        return column("POSITION").number().notNull().conversion(ColumnConversion.NUMBER2INT).map("position").add();
    }

    @Override
    public List<Column> addIntervalColumns(String fieldName) {
        ImmutableList.Builder<Column> builder = ImmutableList.builder();
        builder.add(column("STARTTIME").number().notNull().conversion(NUMBER2LONG).map(fieldName + ".start").add());
        builder.add(column("ENDTIME").number().notNull().conversion(NUMBER2LONG).map(fieldName + ".end").add());
        return builder.build();
    }

    @Override
    public List<Column> addQuantityColumns(String name, boolean notNull, String fieldName) {
        ImmutableList.Builder<Column> builder = ImmutableList.builder();
        builder.add(column(name + "VALUE").number().notNull(notNull).map(fieldName + ".value").add());
        builder.add(column(name + "MULTIPLIER").number().notNull(notNull).conversion(NUMBER2INTWRAPPER).map(fieldName + ".multiplier").add());
        builder.add(column(name + "UNIT").varChar(8).notNull(notNull).conversion(CHAR2UNIT).map(fieldName + ".unit").add());
        return builder.build();
    }

    @Override
    public ImmutableList<Column> addMoneyColumns(String name, boolean notNull, String fieldName) {
        ImmutableList.Builder<Column> builder = ImmutableList.builder();
        builder.add(column(name + "VALUE").number().notNull(notNull).map(fieldName + ".value").add());
        builder.add(column(name + "CURRENCY").number().notNull(notNull).conversion(CHAR2CURRENCY).map(fieldName + ".currency").add());
        return builder.build();
    }


    @Override
    public List<Column> addRefAnyColumns(String name, boolean notNull, String fieldName) {
        ImmutableList.Builder<Column> builder = ImmutableList.builder();
        builder.add(column(name + "CMP").varChar(23).notNull(notNull).map(fieldName + ".component").add());
        builder.add(column(name + "TABLE").varChar(28).notNull(notNull).map(fieldName + ".table").add());
        builder.add(column(name + "KEY").varChar(255).notNull(notNull).map(fieldName + ".key").add());
        builder.add(column(name + "ID").number().notNull(notNull).conversion(NUMBER2LONGNULLZERO).map(fieldName + ".id").add());
        return builder.build();
    }

    void checkActiveBuilder() {
        if (activeBuilder) {
            throw new IllegalTableMappingException("Builder in progress for table " + getName() + ", invoke add() first.");
        }
    }

    @Override
    public Column.Builder column(String name) {
        checkActiveBuilder();
        if (name == null) {
            throw new IllegalTableMappingException("Table " + getName() + " : column names cannot be null.");
        }
        if (name.length() > ColumnConversion.CATALOGNAMELIMIT) {
            throw new IllegalTableMappingException("Table " + getName() + " : column name '" + name + "' is too long, max length is " + ColumnConversion.CATALOGNAMELIMIT + " actual length is " + name.length() + ".");
        }
        activeBuilder = true;
        return new ColumnImpl.BuilderImpl(ColumnImpl.from(this, name));
    }

    @Override
    public PrimaryKeyConstraintImpl.BuilderImpl primaryKey(String name) {
        checkActiveBuilder();
        activeBuilder = true;
        return new PrimaryKeyConstraintImpl.BuilderImpl(this, name);
    }

    @Override
    public UniqueConstraintImpl.BuilderImpl unique(String name) {
        checkActiveBuilder();
        activeBuilder = true;
        return new UniqueConstraintImpl.BuilderImpl(this, name);
    }

    @Override
    public ForeignKeyConstraintImpl.BuilderImpl foreignKey(String name) {
        checkActiveBuilder();
        activeBuilder = true;
        return new ForeignKeyConstraintImpl.BuilderImpl(this, name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TableImpl)) {
            return false;
        }
        TableImpl<?> table = (TableImpl<?>) o;
        return name.equals(table.name) && this.getDataModel().equals(table.getDataModel());
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public KeyValue getPrimaryKey(Object value) {
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
        for (TableImpl<?> table : getDataModel().getTables()) {
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
        for (TableImpl<?> table : getDataModel().getTables()) {
            if (!table.equals(this)) {
                for (ForeignKeyConstraintImpl each : table.getForeignKeyConstraints()) {
                    if (fieldName.equals(each.getFieldName())) {
                        return new ReverseConstraintMapping(each);
                    }
                }
            }
        }
        for (ColumnImpl each : columns) {
            if (each.getFieldName() == null) {
                if (fieldName.equals(each.getName())) {
                    return new ColumnMapping(each);
                }
            }
        }
        return null;
    }

    @Override
    public void indexOrganized(int compressCount) {
    	if (compressCount < 0) {
    		throw new IllegalArgumentException();
    	}
        this.indexOrganized = compressCount;

    }

    public int getIotCompressCount() {
    	if (indexOrganized < 0) {
    		throw new IllegalStateException();
    	}
    	return indexOrganized;
    }
    
    @Override
    public boolean isIndexOrganized() {
        return indexOrganized >= 0;
    }

    public Optional<? extends T> getOptional(Object... primaryKeyValues) {
        return getDataMapper(api).getOptional(primaryKeyValues);
    }

    @Override
    public TableImpl<T> map(Class<? extends T> implementation) {
        if (this.mapperType != null) {
            throw new IllegalTableMappingException("Table : " + getName() + " : Implementer(s) already specified");
        }
        if (!api.isAssignableFrom(implementation)) {
            throw new IllegalTableMappingException("Table : " + getName() + " : " + implementation + " does not implement " + api);
        }
        this.mapperType = new SingleDataMapperType<T>(this, implementation);
        return this;
    }

    @Override
    public TableImpl<T> map(Map<String, Class<? extends T>> implementations) {
        if (this.mapperType != null) {
            throw new IllegalTableMappingException("Table : " + getName() + " : Implementer(s) already specified");
        }
        if (Objects.requireNonNull(implementations).isEmpty()) {
            throw new IllegalArgumentException("Table : " + getName() + " : Empty map of implementors");
        }
        for (Class<?> implementation : implementations.values()) {
            if (!api.isAssignableFrom(implementation)) {
                throw new IllegalArgumentException("Table : " + getName() + " : " + implementation + " does not implement " + api);
            }
        }
        this.mapperType = new InheritanceDataMapperType<>(this, implementations);
        return this;
    }

	void prepare() {
		checkActiveBuilder();
		checkMapperTypeIsSet();
		PrimaryKeyConstraintImpl primaryKey = Objects.requireNonNull(getPrimaryKeyConstraint(),"Table '" + getName() + "' : No primary key defined");
		List<ColumnImpl> primaryKeyColumns = primaryKey.getColumns();
		for (int i = 0 ; i < primaryKeyColumns.size() ; i++) {
			if (!primaryKeyColumns.get(i).equals(columns.get(i))) {
				throw new IllegalStateException(MessageFormat.format("Table '" + getName() + "' : Primary key columns must be defined first and in order in table {0}", getName()));
			}
		}
		for (ForeignKeyConstraintImpl constraint : getForeignKeyConstraints()) {
			constraint.prepare();
		}
		buildReferenceConstraints();
		buildReverseMappedConstraints();
		realColumns = new ArrayList<>();
		for (ColumnImpl column : getColumns()) {
			if (!column.isVirtual()) {
				checkMapped(column);
				realColumns.add(column);
			}
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
            try {
                if (getMapperType().getType(column.getFieldName()) == null) {
                    throw new IllegalStateException(
                        Joiner.on(" ").
                            join("Table " + getName() + " : No field available for column", column.getName(), "mapped by", column.getFieldName()));
                } else {
                    return;
                }
            } catch (MappingException e) {
                throw new IllegalStateException("Table " + getName() + " Column " + column.getName() + " : " + e.toString(), e);
            }
        }
		throw new IllegalTableMappingException("Table " + getName() + " : Column " + column.getName() + " has no mapping");
	}
	
	private void buildReferenceConstraints() {
		ImmutableList.Builder<ForeignKeyConstraintImpl> builder = new ImmutableList.Builder<>();
		for (ForeignKeyConstraintImpl constraint : getForeignKeyConstraints()) {
			if (getMapperType().isReference(constraint.getFieldName())) {
				getMapperType().getField(constraint.getFieldName());
				builder.add(constraint);
			}
		}
		this.referenceConstraints = builder.build();		
	}
	
	private List<ForeignKeyConstraintImpl> getReverseConstraints() {
		ImmutableList.Builder<ForeignKeyConstraintImpl> builder = new ImmutableList.Builder<>();
		for (TableImpl<?> table : getDataModel().getTables()) {
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
	
    @Override
    public boolean maps(Class<?> clazz) {
        return api.isAssignableFrom(clazz);
    }

    public DomainMapper getDomainMapper() {
        return getMapperType().getDomainMapper();
    }

    private void buildReverseMappedConstraints() {
        ImmutableList.Builder<ForeignKeyConstraintImpl> builder = new ImmutableList.Builder<>();
        for (ForeignKeyConstraintImpl each : getReverseConstraints()) {
            if (each.getReferencedTable().equals(this) && each.getReverseFieldName() != null) {
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

    TableCache<T> getCache() {
        return cache;
    }

    public Field getField(String fieldName) {
        return getMapperType().getField(fieldName);
    }

    void renewCache() {
        getCache().renew();
    }

    boolean isAutoId() {
        List<ColumnImpl> columns = getPrimaryKeyColumns();
        return (columns.size() == 1 && columns.get(0).isAutoIncrement());
    }

    DataMapperType<T> getMapperType() {
        checkMapperTypeIsSet();
        return mapperType;
    }

    private void checkMapperTypeIsSet() {
        if (mapperType == null) {
            throw new IllegalTableMappingException("Table : " + getName() + " Implementation class not (yet?) specified");
        }
    }

    Optional<ColumnImpl> getDiscriminator() {
		for (ColumnImpl column : columns) {
			if (column.isDiscriminator()) {
				return Optional.of(column);
			}
		}
		return Optional.empty();
	}
	
	Class<? extends T> classCast(Class<?> in) {
		return in.asSubclass(api);
	}
	
	private boolean hasAutoChange() {
		for (ForeignKeyConstraintImpl constraint : getForeignKeyConstraints()) {
			DeleteRule rule = constraint.getDeleteRule();
			if (!rule.equals(DeleteRule.RESTRICT)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public IndexImpl.BuilderImpl index(String name) {
		checkActiveBuilder();
		activeBuilder = true;
		return new IndexImpl.BuilderImpl(this,name);		
	}
	
	IndexImpl add(IndexImpl index) {
		activeBuilder = false;
		this.indexes.add(index);
		return index;
	}
	
	List<IndexImpl> getIndexes() {
		return Collections.unmodifiableList(indexes);
	}
	
	public List<ColumnImpl> getRealColumns() {
		return realColumns;
	}

    IndexImpl getIndex(String name) {
        for (IndexImpl index : indexes) {
            if (index.getName().equals(name)) {
                return index;
            }
        }
        return null;
    }

    TableConstraintImpl getConstraint(String name) {
        for (TableConstraintImpl tableConstraint : getConstraints()) {
            if (tableConstraint.getName().equals(name)) {
                return tableConstraint;
            }
        }
        return null;
    }

    TableConstraintImpl findMatchingConstraint(TableConstraintImpl other) {
        for (TableConstraintImpl tableConstraint : getConstraints()) {
            if (tableConstraint.matches(other)) {
                return tableConstraint;
            }
        }
        return null;
    }

	@Override
	public void dropJournal(Instant upTo, Logger logger) {		
		if (getJournalTableName() == null) {
			return;
		}
		drop(getJournalTableName(), upTo, logger);
	}

	@Override
	public void dropData(Instant upTo, Logger logger) {
		drop(getName(),upTo, logger);		
	}

	private void drop(String tableName, Instant upTo, Logger logger) {		
		if (!getDataModel().getSqlDialect().hasPartitioning()) {
			// todo sql delete
			return;
		}
		getDataModel().dataDropper(tableName,logger).drop(upTo);		
	}

	@Override
	public void partitionOn(Column column) {	
		this.partitionColumn = Optional.of(column);
	}
	
	@Override
	public void autoPartitionOn(Column column, LifeCycleClass lifeCycleClass) {
		if (Objects.requireNonNull(lifeCycleClass) == LifeCycleClass.NONE) {
			throw new IllegalArgumentException();
		}
		partitionOn(column);
		this.lifeCycleClass = lifeCycleClass;
	}
	
	public Optional<Column> partitionColumn() {	
		return partitionColumn.filter(column -> getDataModel().getSqlDialect().hasPartitioning());
	}
	
	@Override
	public LifeCycleClass lifeCycleClass() {
		return lifeCycleClass;
	}

	Optional<ForeignKeyConstraintImpl> refPartitionConstraint() {
		return getForeignKeyConstraints().stream()
			.filter(ForeignKeyConstraintImpl::isRefPartition)
			.findFirst()
			.filter(constraint -> getDataModel().getSqlDialect().hasPartitioning());
	}
	
	PartitionMethod getPartitionMethod() {
		if (partitionColumn().isPresent()) {
			return (isIndexOrganized() || !getReverseConstraints().isEmpty()) ? PartitionMethod.RANGE : PartitionMethod.INTERVAL;			
		}
		if (refPartitionConstraint().isPresent()) {
			return PartitionMethod.REFERENCE;			
		}
		return PartitionMethod.NONE;
	}
}


