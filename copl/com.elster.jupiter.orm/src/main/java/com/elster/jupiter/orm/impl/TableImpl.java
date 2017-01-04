package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Encrypter;
import com.elster.jupiter.orm.FieldType;
import com.elster.jupiter.orm.IllegalTableMappingException;
import com.elster.jupiter.orm.LifeCycleClass;
import com.elster.jupiter.orm.MappingException;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.TableConstraint;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.fields.impl.ColumnMapping;
import com.elster.jupiter.orm.fields.impl.FieldMapping;
import com.elster.jupiter.orm.fields.impl.ForwardConstraintMapping;
import com.elster.jupiter.orm.fields.impl.MultiColumnMapping;
import com.elster.jupiter.orm.fields.impl.ReverseConstraintMapping;
import com.elster.jupiter.orm.query.impl.QueryExecutorImpl;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.util.streams.Predicates;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeMap;
import com.google.common.collect.TreeRangeSet;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.orm.ColumnConversion.CHAR2CURRENCY;
import static com.elster.jupiter.orm.ColumnConversion.CHAR2PRINCIPAL;
import static com.elster.jupiter.orm.ColumnConversion.CHAR2UNIT;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INTWRAPPER;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONGNULLZERO;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2NOW;
import static com.elster.jupiter.util.Checks.is;
import static com.elster.jupiter.util.Ranges.intersection;
import static com.elster.jupiter.util.Ranges.lowerBound;
import static com.elster.jupiter.util.Ranges.upperBound;
import static com.elster.jupiter.util.streams.Currying.test;

public class TableImpl<T> implements Table<T> {

    static final String JOURNALTIMECOLUMNNAME = "JOURNALTIME";
    static final String MODTIMECOLUMNAME = "MODTIME";

    // persistent fields
    private String schema;
    private String name;
    @SuppressWarnings("unused")
    private int position;
    private boolean cached;
    private boolean autoInstall = true;
    private int indexOrganized = -1;
    private transient RangeSet<Version> versions = TreeRangeSet.<Version>create().complement();
    private RangeMap<Version, String> nameHistory = TreeRangeMap.create();
    private RangeMap<Version, String> journalNameHistory = TreeRangeMap.create();

    // associations
    private final Reference<DataModelImpl> dataModel = ValueReference.absent();
    private final List<ColumnImpl> columns = new ArrayList<>();
    private final List<TableConstraintImpl<?>> constraints = new ArrayList<>();
    private final List<IndexImpl> indexes = new ArrayList<>();

    private Optional<Column> partitionColumn = Optional.empty();
    private LifeCycleClass lifeCycleClass = LifeCycleClass.NONE;

    // mapping
    private DataMapperType<T> mapperType;

    //encrypting
    private Encrypter encrypter;

    // transient, protection against forgetting to call add() on a builder
    private boolean activeBuilder;
    private Class<T> api;
    private List<Class> alternativeApis = new ArrayList<>();
    private TableCache<T> cache;

    // cached fields , initialized when the table's datamodel is registered with the orm service.
    private List<ForeignKeyConstraintImpl> referenceConstraints;
    private List<ForeignKeyConstraintImpl> reverseMappedConstraints;
    private List<ColumnImpl> realColumns;

    private boolean shouldRecalculateMac = false;

    private TableImpl<T> init(DataModelImpl dataModel, String schema, String name, Class<T> api) {
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

    @Override
    public String getQualifiedName(Version version) {
        return getQualifiedName(getName(version));
    }

    String getQualifiedName(String value) {
        return is(schema).emptyOrOnlyWhiteSpace() ? value : schema + "." + value;
    }

    @Override
    public List<ColumnImpl> getColumns() {
        return getColumns(getDataModel().getVersion());
    }

    @Override
    public List<ColumnImpl> getColumns(Version version) {
        return columns
                .stream()
                .filter(test(Column::isInVersion).with(version))
                .collect(Collectors.toList());
    }

    @Override
    public List<TableConstraintImpl> getConstraints() {
        return getConstraints(getDataModel().getVersion());
    }

    @Override
    public List<TableConstraintImpl> getConstraints(Version version) {
        return constraints
                .stream()
                .filter(tableConstraint -> tableConstraint.isInVersion(version))
                .collect(Collectors.toList());
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

    protected void recalculateMacs() {
        if(shouldRecalculateMac) {
            getDataMapper().persist((getDataMapper().findWithoutMacCheck()));
        }
        shouldRecalculateMac = false;
    }

    protected void macColumnAdded() {
        shouldRecalculateMac = true;
    }

    Column add(ColumnImpl column) {
        activeBuilder = false;
        if (column.getFieldName() != null) {
            for (ColumnImpl existing : columns) {
                if (is(existing.getFieldName()).equalTo(column.getFieldName()) && !intersection(existing.versions(), column
                        .versions()).isEmpty()) {
                    throw new IllegalTableMappingException("Table " + getName() + ", column " + column.getName() + " : column " + existing
                            .getName() + " already maps to field " + existing.getFieldName());
                }
            }
        }
        columns.add(column);
        return column;
    }

    @Override
    public java.util.Optional<ColumnImpl> getColumn(String name) {
        return columns
                .stream()
                .filter((column) -> column.getName().equalsIgnoreCase(name))
//                .filter(test(Column::isInVersion).with(getDataModel().getVersion()))
                .findFirst();
    }

    @Override
    public PrimaryKeyConstraintImpl getPrimaryKeyConstraint() {
        return getConstraints()
                .stream()
                .filter(TableConstraint::isPrimaryKey)
                .map(PrimaryKeyConstraintImpl.class::cast)
                .findAny()
                .orElse(null);
    }

    @Override
    public PrimaryKeyConstraintImpl getPrimaryKeyConstraint(Version version) {
        return getConstraints(version)
                .stream()
                .filter(TableConstraint::isPrimaryKey)
                .map(PrimaryKeyConstraintImpl.class::cast)
                .findAny()
                .orElse(null);
    }

    @Override
    public List<ForeignKeyConstraintImpl> getForeignKeyConstraints(Version version) {
        return getConstraints(version)
                .stream()
                .filter(TableConstraint::isForeignKey)
                .map(ForeignKeyConstraintImpl.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public List<ForeignKeyConstraintImpl> getForeignKeyConstraints() {
        return getForeignKeyConstraints(getDataModel().getVersion());
    }

    ForeignKeyConstraintImpl getConstraintForField(String fieldName) {
        for (ForeignKeyConstraintImpl each : getForeignKeyConstraints()) {
            if (fieldName.equals(each.getFieldName())) {
                return each;
            }
        }
        return null;
    }

    @Override
    public List<ColumnImpl> getPrimaryKeyColumns() {
        PrimaryKeyConstraintImpl primaryKeyConstraint = getPrimaryKeyConstraint();
        return primaryKeyConstraint == null ? null : primaryKeyConstraint.getColumns()
                .stream()
                .filter(test(Column::isInVersion).with(getDataModel().getVersion()))
                .collect(Collectors.toList());
    }

    boolean isPrimaryKeyColumn(Column column) {
        TableConstraint primaryKeyConstraint = getPrimaryKeyConstraint();
        return primaryKeyConstraint != null && primaryKeyConstraint.getColumns().contains(column);
    }

    ColumnImpl[] getVersionColumns() {
        return this.getRealColumns().filter(ColumnImpl::isVersion).toArray(ColumnImpl[]::new);
    }

    List<ColumnImpl> getColumnsThatMandateRefreshAfterInsert() {
        return this.getRealColumns().filter(ColumnImpl::mandatesRefreshAfterInsert).collect(Collectors.toList());
    }

    List<ColumnImpl> getUpdateValueColumns() {
        return this.getRealColumns().filter(ColumnImpl::hasUpdateValue).collect(Collectors.toList());
    }

    List<ColumnImpl> getStandardColumns() {
        return getColumns(dataModel.get().getVersion()).stream().filter(ColumnImpl::isStandard).collect(Collectors.toList());
    }

    List<ColumnImpl> getAutoUpdateColumns() {
        return this.getRealColumns().filter(column -> column.hasAutoValue(true)).collect(Collectors.toList());
    }

    @Override
    public List<String> getDdl() {
        return TableDdlGenerator.cautious(this, getDataModel().getSqlDialect(), getDataModel().getVersion()).getDdl();
    }

    @Override
    public List<String> getDdl(Version version) {
        return TableDdlGenerator.cautious(this, getDataModel().getSqlDialect(), version).getDdl();
    }

    String getExtraJournalPrimaryKeyColumnNames() {
        String base = JOURNALTIMECOLUMNNAME;
        Column[] versionColumns = getVersionColumns();
        return versionColumns.length > 0 ? String.join(",", base, versionColumns[0].getName()) : base;
    }

    @Override
    public Column addVersionCountColumn(String name, String dbType, String fieldName) {
        return column(name).type(dbType).notNull().version().conversion(NUMBER2LONG).map(fieldName).installValue("1").add();
    }

    @Override
    public Column addDiscriminatorColumn(String name, String dbType) {
        return column(name).type(dbType).notNull().map(Column.TYPEFIELDNAME).add();
    }

    @Override
    public Column addCreateTimeColumn(String name, String fieldName) {
        return column(name).number().notNull().conversion(NUMBER2NOW).skipOnUpdate().map(fieldName).installValue("0").add();
    }

    @Override
    public Column addModTimeColumn(String name, String fieldName) {
        return column(name).number().notNull().conversion(NUMBER2NOW).map(fieldName).installValue("0").add();
    }

    @Override
    public Column addUserNameColumn(String name, String fieldName) {
        return column(name).varChar(80).notNull().conversion(CHAR2PRINCIPAL).map(fieldName).installValue("'install/upgrade'").add();
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

    boolean supportsApi(Class<?> potentialApi) {
        return potentialApi.equals(this.getApi())
                || this.alternativeApis.stream().anyMatch(alternativeApi -> alternativeApi.equals(potentialApi));
    }

    @Override
    public Table<T> alsoReferredToAs(Class<? super T> alternativeApi) {
        if (!alternativeApi.isAssignableFrom(this.api)) {
            throw new IllegalTableMappingException("Table : " + getName() + " : alternative api " + alternativeApi + " is not a super class of " + api);
        }
        this.alternativeApis.add(alternativeApi);
        return this;
    }

    @SuppressWarnings("unchecked")
    <S> DataMapperImpl<S> getDataMapper(Class<S> api) {
        if (getMapperType().getInjector() == null) {
            throw new IllegalStateException("Datamodel not registered");
        }
        if (maps(api)) {
            return new DataMapperImpl<>(api, (TableImpl<? super S>) this);
        } else {
            throw new IllegalArgumentException("Table " + getName() + " does not map " + api);
        }
    }

    DataMapperImpl<? extends T> getDataMapper(List<Class<?>> apiFragments) {
        if (getMapperType().getInjector() == null) {
            throw new IllegalStateException("Datamodel not registered");
        }
        return getMapperType().streamImplementations(apiFragments)
                .unordered()
                .limit(2)
                .reduce((imp1, imp2) -> api)
                .map(resultApi -> new DataMapperImpl<>(resultApi, this))
                .orElseThrow(() -> new IllegalArgumentException("Table " + getName()
                        + " does not map any implementation extending all of the classes: "
                        + apiFragments.stream().map(Class::toString).collect(Collectors.joining(", "))));
    }

    <S extends T> QueryExecutorImpl<S> getQuery(Class<S> type) {
        List<TableImpl<?>> related = new ArrayList<>();
        addAllRelated(related);
        related.remove(0);
        DataMapperImpl<?>[] mappers = new DataMapperImpl<?>[related.size()];
        for (int i = 0; i < related.size(); i++) {
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
        return this.getRealColumns()
                .filter(column -> name.equals(column.getFieldName()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getJournalTableName() {
        return this.journalNameHistory.get(this.dataModel.get().getVersion());
    }

    @Override
    public String getJournalTableName(Version version) {
        return this.journalNameHistory.get(version);
    }

    @Override
    public JournalTableVersionOptions setJournalTableName(String journalTableName) {
        if (hasAutoChange()) {
            throw new IllegalStateException(" A table with foreign key using cascading or set null delete rule cannot have a journal table ");
        }
        this.journalNameHistory.put(Range.all(), journalTableName);
        return new JournalTableVersionOptionsImpl(journalTableName);
    }

    @Override
    public boolean hasJournal() {
        return this.getJournalTableName() != null;
    }

    @Override
    public boolean hasJournal(Version version) {
        return this.getJournalTableName(version) != null;
    }

    @Override
    public Column addAutoIdColumn() {
        String sequence = name + "ID";
        if (sequence.length() > ColumnConversion.CATALOGNAMELIMIT) {
            throw new IllegalStateException("Name " + sequence + " too long");
        }
        return column("ID").number()
                .notNull()
                .conversion(ColumnConversion.NUMBER2LONG)
                .sequence(name + "ID")
                .skipOnUpdate()
                .map("id")
                .add();
    }

    @Override
    public Column addPositionColumn() {
        return column("POSITION").number().notNull().conversion(ColumnConversion.NUMBER2INT).map("position").add();
    }

    @Override
    public Column addMessageAuthenticationCodeColumn(Encrypter encrypter) {
        if (this.encrypter != null) {
            throw new IllegalStateException("Table " + getName() + " : already has a MAC column.");
        }
        this.encrypter = Objects.requireNonNull(encrypter);
        return column("MAC")
                .varChar(4000)
                .notNull()
                .map(Column.MACFIELDNAME)
                .add();
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
        builder.add(column(name + "MULTIPLIER").number()
                .notNull(notNull)
                .conversion(NUMBER2INTWRAPPER)
                .map(fieldName + ".multiplier")
                .add());
        builder.add(column(name + "UNIT").varChar(8)
                .notNull(notNull)
                .conversion(CHAR2UNIT)
                .map(fieldName + ".unit")
                .add());
        return builder.build();
    }

    @Override
    public List<Column> addQuantityColumns(String name, boolean notNull, String fieldName, Range... versions) {
        ImmutableList.Builder<Column> builder = ImmutableList.builder();
        builder.add(column(name + "VALUE").number().notNull(notNull).map(fieldName + ".value").during(versions).add());
        builder.add(column(name + "MULTIPLIER").number()
                .notNull(notNull)
                .conversion(NUMBER2INTWRAPPER)
                .map(fieldName + ".multiplier")
                .during(versions)
                .add());
        builder.add(column(name + "UNIT").varChar(8)
                .notNull(notNull)
                .conversion(CHAR2UNIT)
                .map(fieldName + ".unit")
                .during(versions)
                .add());
        return builder.build();
    }

    @Override
    public ImmutableList<Column> addMoneyColumns(String name, boolean notNull, String fieldName) {
        ImmutableList.Builder<Column> builder = ImmutableList.builder();
        builder.add(column(name + "VALUE").number().notNull(notNull).map(fieldName + ".value").add());
        builder.add(column(name + "CURRENCY").number()
                .notNull(notNull)
                .conversion(CHAR2CURRENCY)
                .map(fieldName + ".currency")
                .add());
        return builder.build();
    }


    @Override
    public List<Column> addRefAnyColumns(String name, boolean notNull, String fieldName) {
        ImmutableList.Builder<Column> builder = ImmutableList.builder();
        builder.add(column(name + "CMP").varChar(23).notNull(notNull).map(fieldName + ".component").add());
        builder.add(column(name + "TABLE").varChar(28).notNull(notNull).map(fieldName + ".table").add());
        builder.add(column(name + "KEY").varChar(255).notNull(notNull).map(fieldName + ".key").add());
        builder.add(column(name + "ID").number()
                .notNull(notNull)
                .conversion(NUMBER2LONGNULLZERO)
                .map(fieldName + ".id")
                .add());
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
            throw new IllegalTableMappingException("Table " + getName() + " : column name '" + name + "' is too long, max length is " + ColumnConversion.CATALOGNAMELIMIT + " actual length is " + name
                    .length() + ".");
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

    int getIotCompressCount() {
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
        return getDataMapper().getOptional(primaryKeyValues);
    }

    @Override
    public TableImpl<T> map(Class<? extends T> implementation) {
        if (this.mapperType != null) {
            throw new IllegalTableMappingException("Table : " + getName() + " : Implementer(s) already specified");
        }
        checkCompatibleImplementation(implementation);
        this.mapperType = new SingleDataMapperType<>(this, implementation);
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
        implementations.values().forEach(this::checkCompatibleImplementation);
        this.mapperType = new InheritanceDataMapperType<>(this, implementations);
        return this;
    }

    private void checkCompatibleImplementation(Class<?> implementation) {
        if (!api.isAssignableFrom(implementation)) {
            throw new IllegalTableMappingException("Table : " + getName() + " : " + implementation + " does not implement " + api);
        }
    }

    void prepare() {
        checkActiveBuilder();
        checkMapperTypeIsSet();
        PrimaryKeyConstraintImpl primaryKey = Objects.requireNonNull(getPrimaryKeyConstraint(), "Table '" + getName() + "' : No primary key defined");
        List<ColumnImpl> primaryKeyColumns = primaryKey.getColumns();
        for (int i = 0; i < primaryKeyColumns.size(); i++) {
            if (!primaryKeyColumns.get(i).equals(columns.get(i))) {
                throw new IllegalStateException(MessageFormat.format("Table ''{0}'' : Primary key columns must be defined first and in order", getName()));
            }
        }
        getForeignKeyConstraints().forEach(ForeignKeyConstraintImpl::prepare);
        buildReferenceConstraints();
        buildReverseMappedConstraints();
        this.getRealColumns().forEach(this::checkMapped);
        cache = isCached() ? new TableCache.TupleCache<>(this) : new TableCache.NoCache<>();
    }

    private void checkMapped(Column column) {
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
                                    join("Table " + getName() + " : No field available for column", column.getName(), "mapped by", column
                                            .getFieldName()));
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
            //if (!table.equals(this)) {
            for (ForeignKeyConstraintImpl each : table.getForeignKeyConstraints()) {
                if (each.getReferencedTable().equals(this)) {
                    builder.add(each);
                }
            }
            //}
        }
        return builder.build();
    }

    @Override
    public boolean maps(Class<?> clazz) {
        return api.isAssignableFrom(clazz)
                || this.alternativeApis.stream().anyMatch(alternativeApi -> alternativeApi.isAssignableFrom(clazz));
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

    public void renewCache() {
        getCache().renew();
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
        return new IndexImpl.BuilderImpl(this, name);
    }

    IndexImpl add(IndexImpl index) {
        activeBuilder = false;
        this.indexes.add(index);
        return index;
    }

    List<IndexImpl> getIndexes() {
        return getIndexes(getDataModel().getVersion());
    }

    List<IndexImpl> getIndexes(Version version) {
        return indexes.stream()
                .filter(index -> index.isInVersion(version))
                .collect(Collectors.toList());
    }

    @Override
    public Stream<ColumnImpl> getRealColumns() {
        if (this.realColumns == null) {
            this.realColumns =
                    this.getColumns(Version.latest())
                            .stream()
                            .filter(Predicates.not(ColumnImpl::isVirtual))
                            .collect(Collectors.toList());
        }
        return realColumns.stream();
    }

    Optional<IndexImpl> getIndex(String name) {
        return indexes.stream()
                .filter(index -> index.getName().equals(name))
                .findAny();
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
        drop(getName(), upTo, logger);
    }

    private void drop(String tableName, Instant upTo, Logger logger) {
        if (!getDataModel().getSqlDialect().hasPartitioning()) {
            // todo sql delete
            return;
        }
        getDataModel().dataDropper(tableName, logger).drop(upTo);
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

    Optional<Column> partitionColumn() {
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

    @Override
    public Table<T> since(Version version) {
        versions = ImmutableRangeSet.of(Range.atLeast(version));
        return this;
    }

    @Override
    public Table<T> upTo(Version version) {
        versions = ImmutableRangeSet.of(Range.lessThan(version));
        return this;
    }

    @Override
    public Table<T> during(Range... ranges) {
        ImmutableRangeSet.Builder<Version> builder = ImmutableRangeSet.builder();
        Arrays.stream(ranges).forEach(builder::add);
        versions = builder.build();
        return this;
    }

    @Override
    public boolean isInVersion(Version version) {
        return versions.contains(version);
    }

    boolean overlaps(RangeSet<Version> versions) {
        return !Ranges.intersection(this.versions, versions).isEmpty();
    }

    private boolean intersects(RangeSet<Version> versions) {
        return Ranges.intersection(this.versions, versions).isEmpty();
    }

    public RangeSet<Version> getVersions() {
        return versions;
    }

    @Override
    public SortedSet<Version> changeVersions() {
        SortedSet<Version> versions = getVersions()
                .asRanges()
                .stream()
                .flatMap(range -> Stream.of(Ranges.lowerBound(range), Ranges.upperBound(range)).flatMap(Functions.asStream()))
                .collect(Collectors.toCollection(TreeSet::new));
        columns.forEach(column -> versions.addAll(column.changeVersions()));
        constraints.forEach(constraint -> versions.addAll(constraint.changeVersions()));
        indexes.forEach(index -> versions.addAll(index.changeVersions()));
        return versions;
    }

    @Override
    public void previouslyNamed(Range<Version> versionRange, String name) {
        nameHistory.put(versionRange, name);
    }

    @Override
    public String getName(Version version) {
        String previousName = nameHistory.get(version);
        return previousName != null ? previousName : name;
    }

    Optional<Version> previousTo(Version version) {
        SortedSet<Version> knownVersions = new TreeSet<>();
        versions.asRanges()
                .stream()
                .flatMap(range -> Stream.of(lowerBound(range), upperBound(range)))
                .flatMap(Functions.asStream())
                .forEach(knownVersions::add);
        nameHistory.asMapOfRanges()
                .keySet()
                .stream()
                .flatMap(range -> Stream.of(lowerBound(range), upperBound(range)))
                .flatMap(Functions.asStream())
                .forEach(knownVersions::add);
        addToKnownVersions(knownVersions, columns.stream().map(ColumnImpl::versions));
        addToKnownVersions(knownVersions, constraints.stream().map(TableConstraintImpl::versions));
        addToKnownVersions(knownVersions, indexes.stream().map(IndexImpl::versions));
        SortedSet<Version> previousVersions = knownVersions.headSet(version);
        return previousVersions.isEmpty() ? Optional.empty() : Optional.of(previousVersions.last());
    }

    private void addToKnownVersions(SortedSet<Version> knownVersions, Stream<RangeSet<Version>> rangeSetStream) {
        rangeSetStream
                .map(RangeSet::asRanges)
                .flatMap(Collection::stream)
                .flatMap(range -> Stream.of(lowerBound(range), upperBound(range)))
                .flatMap(Functions.asStream())
                .forEach(knownVersions::add);
    }

    Set<String> getHistoricalNames() {
        Set<String> set = new HashSet<>(nameHistory.asMapOfRanges().values());
        set.add(name);
        return set;
    }

    String previousJournalTableName(Version version) {
        return journalNameHistory.subRangeMap(Range.lessThan(version))
                .asMapOfRanges()
                .entrySet()
                .stream()
                .max(Comparator.comparing(Map.Entry::getKey, Comparator.comparing(Range::upperEndpoint)))
                .map(Map.Entry::getValue)
                .orElse(null);
    }

    Set<String> getJournalTableNames() {
        return journalNameHistory.asMapOfRanges()
                .values()
                .stream()
                .collect(Collectors.toSet());
    }

    Encrypter getEncrypter() {
        return encrypter;
    }

    private class JournalTableVersionOptionsImpl implements JournalTableVersionOptions {
        private final String tableName;

        private JournalTableVersionOptionsImpl(String tableName) {
            this.tableName = tableName;
        }

        @Override
        public void since(Version version) {
            journalNameHistory.clear();
            journalNameHistory.put(Range.atLeast(version), this.tableName);
        }

        @Override
        public void upTo(Version version) {
            journalNameHistory.clear();
            journalNameHistory.put(Range.lessThan(version), this.tableName);
        }

        @Override
        @SafeVarargs
        public final void during(Range<Version>... ranges) {
            journalNameHistory.clear();
            Stream.of(ranges).forEach(range -> journalNameHistory.put(range, this.tableName));
        }

    }

}


