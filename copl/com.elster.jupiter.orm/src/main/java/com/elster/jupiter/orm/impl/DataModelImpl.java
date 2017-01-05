package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.DataDropper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DdlDifference;
import com.elster.jupiter.orm.Difference;
import com.elster.jupiter.orm.LifeCycleClass;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.PartitionCreator;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.orm.SqlDialect;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.orm.associations.RefAny;
import com.elster.jupiter.orm.associations.impl.ManagedPersistentList;
import com.elster.jupiter.orm.associations.impl.RefAnyImpl;
import com.elster.jupiter.orm.query.impl.QueryExecutorImpl;
import com.elster.jupiter.orm.query.impl.QueryStreamImpl;
import com.elster.jupiter.util.streams.Functions;

import com.google.common.collect.RangeSet;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import oracle.jdbc.OracleConnection;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import java.nio.file.FileSystem;
import java.security.Principal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Currying.perform;


public class DataModelImpl implements DataModel {

    private static final Logger LOGGER = Logger.getLogger(DataModelImpl.class.getName());

    // persistent fields
    private String name;
    private String description;

    // associations
    private final List<TableImpl<?>> tables = new ArrayList<>();

    // transient fields
    private Injector injector;
    private final OrmServiceImpl ormService;
    private boolean registered;
    private Optional<Boolean> isInstalled = Optional.empty();

    private Version version;

    @Inject
    DataModelImpl(OrmService ormService) {
        this.ormService = (OrmServiceImpl) ormService;
    }

    DataModelImpl init(String name, String description, Version version) {
        this.name = name;
        this.description = description;
        this.version = version;
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public List<TableImpl<?>> getTables() {
        return tables.stream()
                .filter(inVersion(getVersion()))
                .collect(Collectors.toList());
    }

    private Predicate<TableImpl<?>> inVersion(Version version) {
        return table -> table.isInVersion(version);
    }

    private Predicate<TableImpl<?>> overlaps(RangeSet<Version> versions) {
        return table -> table.overlaps(versions);
    }

    @Override
    public List<TableImpl<?>> getTables(Version version) {
        return tables.stream()
                .filter(inVersion(version))
                .collect(Collectors.toList());
    }

    @Override
    public TableImpl<?> getTable(String tableName) {
        return tables.stream()
                .filter(table -> table.getName().equals(tableName))
                .filter(inVersion(getVersion()))
                .findFirst()
                .orElse(null);
    }

    public TableImpl<?> getTable(String tableName, RangeSet<Version> versions) {
        return tables.stream()
                .filter(table -> table.getName().equals(tableName))
                .filter(overlaps(versions))
                .findFirst()
                .orElse(null);
    }

    @Override
    public TableImpl<?> getTable(String tableName, Version version) {
        return tables.stream()
                .filter(table -> table.getName().equals(tableName))
                .filter(inVersion(version))
                .findFirst()
                .orElse(null);
    }

    @Override
    public <T> Table<T> addTable(String tableName, Class<T> api) {
        return addTable(null, tableName, api);
    }

    private void checkActiveBuilder() {
        if (!getTables().isEmpty()) {
            tables.get(getTables().size() - 1).checkActiveBuilder();
        }
    }

    @Override
    public <T> TableImpl<T> addTable(String schema, String tableName, Class<T> api) {
        checkNotRegistered();
        checkActiveBuilder();
        if (getTable(tableName) != null) {
            throw new IllegalArgumentException("Component has already table " + tableName);
        }
        TableImpl<T> table = TableImpl.from(this, schema, tableName, api);
        add(table);
        return table;
    }

    @Override
    public String toString() {
        return String.join(" ", "DataModel", name, "(" + description + ")");
    }

    private void add(TableImpl<?> table) {
        tables.add(table);
    }

    @Override
    public <T> DataMapperImpl<T> mapper(Class<T> api) {
        checkRegistered();
        Optional<DataMapperImpl<T>> mapper = optionalMapper(api);
        if (mapper.isPresent()) {
            return mapper.get();
        } else {
            throw new IllegalArgumentException("Type " + api + " not configured in data model " + this.getName());
        }
    }

    <T> Optional<DataMapperImpl<T>> optionalMapper(Class<T> api) {
        for (TableImpl<?> table : tables) {
            if (table.maps(api)) {
                return Optional.of(table.getDataMapper(api));
            }
        }
        return Optional.empty();
    }

    @Override
    public void install(boolean executeDdl, boolean store) {
        if (executeDdl) {
            ormService.getUpgradeDataModel(this).upgradeTo(this, Version.latest());
        }
        if (store) {
            if (ormService.isInstalled(this)) {
                ormService.getDataModel(OrmService.COMPONENTNAME).get().remove(this);
            }
            ormService.getDataModel(OrmService.COMPONENTNAME).get().persist(this);
        }
        isInstalled = Optional.empty();

    }

    private void upgradeTo(DataModelImpl toDataModel, Version version) {
        try (Connection connection = getConnection(false);
             Statement statement = connection.createStatement()) {
            for (TableImpl<?> toTable : toDataModel.getTables()) {
                if (toTable.isAutoInstall()) {
                    TableImpl<?> fromTable = getTable(toTable.getName());
                    if (fromTable != null) {
                        List<Difference> upgradeDdl = TableDdlGenerator.cautious(fromTable, fromTable.getDataModel()
                                .getSqlDialect(), version).upgradeDdl(toTable);
                        for (ColumnImpl sequenceColumn : toTable.getAutoUpdateColumns()) {
                            if (sequenceColumn.getQualifiedSequenceName() != null) {
                                long sequenceValue = getLastSequenceValue(statement, sequenceColumn.getQualifiedSequenceName());
                                long maxColumnValue = fromTable.getColumn(sequenceColumn.getName()) != null ? maxColumnValue(sequenceColumn, statement) : 0;
                                if (maxColumnValue > sequenceValue) {
                                    upgradeDdl.add(TableDdlGenerator.cautious(toTable, toTable.getDataModel()
                                            .getSqlDialect(), version)
                                            .upgradeSequenceDdl(sequenceColumn, maxColumnValue + 1));
                                }
                            }
                        }
                        executeSqlStatements(statement, upgradeDdl);
                    } else {
                        toTable.getDdl().forEach(perform(this::executeSqlStatement).on(statement));
                    }
                }

            }
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }

    }

    private long maxColumnValue(ColumnImpl sequenceColumn, Statement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery("select nvl(max(" + sequenceColumn.getName() + ") ,0) from " + sequenceColumn.getTable().getName())) {
            if (resultSet.next()) {
                return resultSet.getLong(1);
            } else {
                return 0;
            }
        }
    }

    private long getLastSequenceValue(Statement statement, String sequenceName) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery("select last_number from user_sequences where sequence_name = '" + sequenceName + "'")) {
            if (resultSet.next()) {
                return resultSet.getLong(1);
            } else {
                // to indicate that the sequence is not there
                return -1;
            }
        }
    }

    private void executeSqlStatements(Statement statement, List<Difference> differences) throws SQLException {
        differences.stream()
                .filter(difference -> difference instanceof DdlDifference)
                .map(DdlDifference.class::cast)
                .map(DdlDifference::ddl)
                .flatMap(List::stream)
                .forEach(perform(this::executeSqlStatement).on(statement));
    }

    private void executeSqlStatement(Statement statement, String sqlStatement) {
        try {
            statement.execute(sqlStatement);
        } catch (SQLException sqe) {
            throw new UnderlyingSQLFailedException(sqe);
        }
    }

    @Override
    public Connection getConnection(boolean transactionRequired) throws SQLException {
        return ormService.getConnection(transactionRequired);
    }

    @Override
    public Principal getPrincipal() {
        return ormService.getPrincipal();
    }

    @Override
    public SqlDialect getSqlDialect() {
        try (Connection connection = getConnection(false)) {
            if (connection.isWrapperFor(OracleConnection.class)) {
                String product = connection.getMetaData().getDatabaseProductVersion();
                if (product.contains("Partitioning")) {
                    return SqlDialect.ORACLE_EE;
                } else {
                    return SqlDialect.ORACLE_SE;
                }
            }
            return SqlDialect.H2;
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    public Optional<TableImpl<?>> getTable(Class<?> clazz) {
        for (TableImpl<?> table : getTables()) {
            if (table.maps(clazz)) {
                return Optional.<TableImpl<?>>of(table);
            }
        }
        return Optional.empty();
    }

    Optional<TableImpl<?>> getTable(Class<?> clazz, RangeSet<Version> versions) {
        return tables.stream()
                .filter(table -> table.maps(clazz))
                .filter(overlaps(versions))
                .findAny();
    }

    @Override
    public RefAny asRefAny(Object reference) {
        checkRegistered();
        Class<?> clazz = Objects.requireNonNull(reference).getClass();
        return getOrmService()
                .getDataModels()
                .stream()
                .map(DataModelImpl.class::cast)
                .map(dataModel -> dataModel.getTable(clazz))
                .flatMap(Functions.asStream())
                .findFirst()
                .map(table -> getInstance(RefAnyImpl.class).init(reference, table))
                .orElseThrow(() -> new IllegalArgumentException("No table defined that maps " + reference.getClass()));
    }

    Injector getInjector() {
        return injector;
    }

    private void checkRegistered() {
        if (!registered) {
            throw new IllegalStateException("DataModel not registered");
        }
    }

    private void checkNotRegistered() {
        if (registered) {
            throw new IllegalStateException("DataModel already registered");
        }
    }

    @Override
    public void register(Module... modules) {
        checkNotRegistered();
        Module[] allModules = new Module[modules.length + 1];
        System.arraycopy(modules, 0, allModules, 0, modules.length);
        allModules[modules.length] = getModule();
        injector = Guice.createInjector(allModules);
        for (TableImpl<?> each : getTables(getVersion())) {
            each.prepare();
        }
        this.ormService.register(this);
        registered = true;
    }

    @Override
    public <T> T getInstance(Class<T> clazz) {
        return injector.getInstance(clazz);
    }

    private <T> void persist(Class<T> type, Object entity) {
        DataMapperImpl<T> mapper = mapper(type);
        mapper.persist(mapper.cast(entity));
    }

    private <T> void update(Class<T> type, Object entity, String... columns) {
        DataMapperImpl<T> mapper = mapper(type);
        mapper.update(mapper.cast(entity), columns);
    }

    private <T> void touch(Class<T> type, Object entity) {
        DataMapperImpl<T> mapper = mapper(type);
        mapper.touch(mapper.cast(entity));
    }

    private <T> void remove(Class<T> type, Object entity) {
        DataMapperImpl<T> mapper = mapper(type);
        mapper.remove(mapper.cast(entity));
    }

    @Override
    public void persist(Object entity) {
        checkRegistered();
        persist(Objects.requireNonNull(entity.getClass()), entity);
    }

    @Override
    public void update(Object entity, String... columns) {
        checkRegistered();
        update(Objects.requireNonNull(entity).getClass(), entity, columns);
    }

    @Override
    public void touch(Object entity) {
        checkRegistered();
        touch(Objects.requireNonNull(entity).getClass(), entity);
    }


    @Override
    public void remove(Object entity) {
        checkRegistered();
        remove(Objects.requireNonNull(entity).getClass(), entity);
    }

    public Clock getClock() {
        return ormService.getClock();
    }

    public FileSystem getFileSystem() {
        return ormService.getFileSystem();
    }

    public OrmServiceImpl getOrmService() {
        return ormService;
    }

    @Override
    public <T> QueryExecutor<T> query(Class<T> api, Class<?>... eagers) {
        return query(mapper(api), eagers);
    }

    public <T> QueryExecutorImpl<T> query(DataMapperImpl<T> root, Class<?>... eagers) {
        DataMapperImpl<?>[] mappers = new DataMapperImpl[eagers.length];
        for (int i = 0; i < eagers.length; i++) {
            Optional<?> mapper = optionalMapper(eagers[i]);
            if (!mapper.isPresent()) {
                mapper = ormService.optionalMapper(eagers[i]);
            }
            if (mapper.isPresent()) {
                mappers[i] = (DataMapperImpl<?>) mapper.get();
            } else {
                throw new IllegalArgumentException("" + eagers[i]);
            }
        }
        return root.with(mappers);
    }

    public <T> QueryStream<T> stream(Class<T> api) {
        return new QueryStreamImpl<>(mapper(api));
    }

    Module getModule() {
        return getOrmService().getModule(this);
    }

    @Override
    public Version getVersion() {
        if (version == null) {
            return Version.latest();
        }
        return version;
    }

    @Override
    public SortedSet<Version> changeVersions() {
        return tables.stream()
                .map(Table::changeVersions)
                .flatMap(Collection::stream)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public ValidatorFactory getValidatorFactory() {
        MessageInterpolator messageInterpolator = null;
        try {
            messageInterpolator = injector.getInstance(MessageInterpolator.class);
        } catch (ConfigurationException e) {
            LOGGER.log(Level.WARNING, "DataModel " + name + " has no registered a MessageInterpolator. Validation messages will not be translated.", e);
        }
        if (messageInterpolator == null) {
            messageInterpolator = new MessageInterpolator() {
                @Override
                public String interpolate(String messageTemplate, Context context) {
                    return messageTemplate;
                }

                @Override
                public String interpolate(String messageTemplate, Context context, Locale locale) {
                    return messageTemplate;
                }
            };
        }
        return Validation.byDefaultProvider()
                .providerResolver(ormService.getValidationProviderResolver())
                .configure()
                .constraintValidatorFactory(getConstraintValidatorFactory())
                .messageInterpolator(messageInterpolator)
                .buildValidatorFactory();
    }

    private ConstraintValidatorFactory getConstraintValidatorFactory() {
        return new ConstraintValidatorFactory() {

            @Override
            public void releaseInstance(ConstraintValidator<?, ?> arg0) {
            }

            @Override
            public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> arg0) {
                return DataModelImpl.this.getInstance(arg0);
            }
        };
    }

    public void renewCache(String tableName) {
        checkRegistered();
        TableImpl<?> table = getTable(tableName);
        if (table != null) {
            table.renewCache();
        }
    }

    @Override
    public boolean isInstalled() {
        if (!isInstalled.isPresent()) {
            isInstalled = Optional.of(ormService.isInstalled(this));
        }
        return isInstalled.get();
    }

    @Override
    public <T> void reorder(List<T> list, List<T> newOrder) {
        if (list.size() != newOrder.size()) {
            throw new IllegalArgumentException();
        }
        for (T each : list) {
            if (!newOrder.contains(each)) {
                throw new IllegalArgumentException();
            }
        }
        if (list instanceof ManagedPersistentList<?>) {
            ((ManagedPersistentList<T>) list).reorder(newOrder);
        } else {
            for (int i = 0; i < list.size(); i++) {
                list.set(i, newOrder.get(i));
            }
        }
    }

    public void dropJournal(Instant upTo, Logger logger) {
        getTables().forEach(table -> table.dropJournal(upTo, logger));
    }

    public void dropAuto(LifeCycleClass lifeCycleClass, Instant upTo, Logger logger) {
        getTables().stream()
                .filter(table -> table.lifeCycleClass() == lifeCycleClass)
                .forEach(table -> table.dropData(upTo, logger));
    }

    public void createPartitions(Instant upTo, Logger logger) {
        getTables().stream()
                .filter(table -> table.getPartitionMethod() == PartitionMethod.RANGE)
                .forEach(table -> partitionCreator(table.getName(), logger).create(upTo));
    }


    @Override
    public DataDropper dataDropper(String tableName, Logger logger) {
        return new DataDropperImpl(this, tableName, logger);
    }

    @Override
    public PartitionCreator partitionCreator(String tableName, Logger logger) {
        return new PartitionCreatorImpl(this, tableName, logger);
    }

    void addAllTables(DataModelImpl other) {
        other.getTables()
                .stream()
                .forEach(this::add);
    }
}
