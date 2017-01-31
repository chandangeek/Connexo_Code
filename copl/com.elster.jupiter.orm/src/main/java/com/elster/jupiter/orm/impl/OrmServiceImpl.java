/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelDifferencesLister;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.LifeCycleClass;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.TransactionRequiredException;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.orm.associations.RefAny;
import com.elster.jupiter.orm.associations.impl.RefAnyImpl;
import com.elster.jupiter.orm.internal.TableSpecs;
import com.elster.jupiter.orm.schema.ExistingConstraint;
import com.elster.jupiter.orm.schema.ExistingTable;
import com.elster.jupiter.orm.schema.SchemaInfoProvider;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.pubsub.Subscriber;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionEvent;
import com.elster.jupiter.util.Registration;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.streams.Functions;

import com.google.common.collect.RangeSet;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.sql.DataSource;
import javax.validation.ValidationProviderResolver;
import java.nio.file.FileSystem;
import java.security.Principal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

@Component(name = "com.elster.jupiter.orm", immediate = true, service = {OrmService.class}, property = "name=" + OrmService.COMPONENTNAME)
public final class OrmServiceImpl implements OrmService {

    private volatile DataSource dataSource;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile Clock clock;
    private volatile FileSystem fileSystem;
    private volatile Publisher publisher;
    private volatile JsonService jsonService;
    private volatile ValidationProviderResolver validationProviderResolver;
    private final Map<String, DataModelImpl> dataModels = Collections.synchronizedMap(new HashMap<>());
    private volatile SchemaInfoProvider schemaInfoProvider;
    private Registration clearCacheOnRollBackRegistration;

    // For OSGi purposes
    public OrmServiceImpl() {
    }

    // For testing purposes
    @Inject
    public OrmServiceImpl(Clock clock, DataSource dataSource, JsonService jsonService, ThreadPrincipalService threadPrincipalService, Publisher publisher, ValidationProviderResolver validationProviderResolver, FileSystem fileSystem, SchemaInfoProvider schemaInfoProvider) {
        this();
        setClock(clock);
        setThreadPrincipalService(threadPrincipalService);
        setDataSource(dataSource);
        setJsonService(jsonService);
        setPublisher(publisher);
        setValidationProviderResolver(validationProviderResolver);
        setFileSystem(fileSystem);
        setSchemaInfoProvider(schemaInfoProvider);
        activate();
    }

    public Connection getConnection(boolean transactionRequired) throws SQLException {
        Connection result = dataSource.getConnection();
        if (transactionRequired && result.getAutoCommit()) {
            result.close();
            throw new TransactionRequiredException();
        }
        return result;
    }

    @Override
    public Optional<DataModel> getDataModel(String name) {
        return Optional.ofNullable(dataModels.get(name));
    }

    public Optional<DataModelImpl> getDataModelImpl(String name) {
        return Optional.ofNullable(dataModels.get(name));
    }

    @Override
    public DataModelImpl newDataModel(String name, String description) {
        return new DataModelImpl(this).init(name, description, Version.latest());
    }

    public void register(DataModelImpl dataModel) {
        dataModels.put(dataModel.getName(), dataModel);
    }

    private DataModel getOrmDataModel() {
        return dataModels.get(COMPONENTNAME);
    }

    private DataModel getExistingTablesDataModel() {
        return dataModels.get(EXISTING_TABLES_DATA_MODEL);
    }

    public Clock getClock() {
        return clock;
    }

    public Principal getPrincipal() {
        return threadPrincipalService.getPrincipal();
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setValidationProviderResolver(ValidationProviderResolver validationProviderResolver) {
        this.validationProviderResolver = validationProviderResolver;
    }

    @Reference
    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    public JsonService getJsonService() {
        return jsonService;
    }

    @Reference
    public void setSchemaInfoProvider(SchemaInfoProvider schemaInfoProvider) {
        this.schemaInfoProvider = schemaInfoProvider;
    }

    @Reference
    public void setFileSystem(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    private DataModel createDataModel(boolean register) {
        DataModelImpl result = newDataModel(OrmService.COMPONENTNAME, "Object Relational Mapper");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(result);
        }
        if (register) {
            result.register();
        }
        return result;
    }

    @Activate
    public void activate() {
        createDataModel(false);
        createExistingTableDataModel();
        clearCacheOnRollBackRegistration = publisher.addSubscriber(new ClearCachesOnTransactionRollBack());
    }

    @Deactivate
    public void deactivate() {
        clearCacheOnRollBackRegistration.unregister();
    }

    private void createExistingTableDataModel() {
        if (this.schemaInfoProvider != null) {
            DataModel dataModel = newDataModel(EXISTING_TABLES_DATA_MODEL, "Oracle schema");
            for (SchemaInfoProvider.TableSpec each : schemaInfoProvider.getSchemaInfoTableSpec()) {
                each.addTo(dataModel);
            }
            dataModel.register();
        }
    }

    public TableImpl<?> getTable(String componentName, String tableName) {
        DataModelImpl dataModel = dataModels.get(componentName);
        if (dataModel == null) {
            throw new IllegalArgumentException("DataModel " + componentName + " not found");
        } else {
            TableImpl<?> result = dataModel.getTable(tableName);
            if (result == null) {
                throw new IllegalArgumentException("Table " + tableName + " not found in component " + componentName);
            } else {
                return result;
            }
        }
    }

    public TableImpl<?> getTable(String componentName, String tableName, RangeSet<Version> versions) {
        DataModelImpl dataModel = dataModels.get(componentName);
        if (dataModel == null) {
            throw new IllegalArgumentException("DataModel " + componentName + " not found");
        } else {
            TableImpl<?> result = dataModel.getTable(tableName, versions);
            if (result == null) {
                throw new IllegalArgumentException("Table " + tableName + " not found in component " + componentName);
            } else {
                return result;
            }
        }
    }

    public TableImpl<?> getTable(Class<?> apiClass) {
        return this.dataModels
                .values()
                .stream()
                .flatMap(dataModel -> dataModel.getTables().stream())
                .filter(table -> table.supportsApi(apiClass))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("No table found that persists the api class " + apiClass.getName()));
    }

    public TableImpl<?> getTable(Class<?> apiClass, RangeSet<Version> versions) {
        return this.dataModels
                .values()
                .stream()
                .map(dataModel -> dataModel.getTable(apiClass, versions))
                .flatMap(Functions.asStream())
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("No table found that persists the api class " + apiClass.getName()));
    }

    @Override
    public List<DataModel> getDataModels() {
        synchronized (dataModels) {
            return new ArrayList<>(dataModels.values());
        }
    }

    Module getModule(final DataModel dataModel) {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(Clock.class).toInstance(clock);
                bind(JsonService.class).toInstance(jsonService);
                bind(ThreadPrincipalService.class).toInstance(threadPrincipalService);
                bind(OrmService.class).toInstance(OrmServiceImpl.this);
            }
        };
    }

    @Override
    public void invalidateCache(String componentName, String tableName) {
        DataModelImpl dataModel = dataModels.get(componentName);
        if (dataModel != null) {
        	dataModel.renewCache(tableName);
        }
    }

    ValidationProviderResolver getValidationProviderResolver() {
        return validationProviderResolver;
    }

    Publisher getPublisher() {
        return publisher;
    }

    public boolean isInstalled(DataModelImpl dataModel) {
        DataModel orm = getOrmDataModel();
        try {
            return orm.mapper(DataModel.class).getOptional(dataModel.getName()).isPresent();
        } catch (UnderlyingSQLFailedException ex) {
            return false;
        }
    }

    @Override
    public RefAny createRefAny(String component, String table, Object... key) {
        return new RefAnyImpl(this, jsonService).init(component, table, key);
    }

    <T> Optional<DataMapperImpl<T>> optionalMapper(Class<T> iface) {
        return getDataModels()
                .stream()
                .map(DataModelImpl.class::cast)
                .map(model -> model.optionalMapper(iface))
                .flatMap(Functions.asStream())
                .findFirst();
    }

    DataModelImpl getUpgradeDataModel(DataModel model) {
        DataModelImpl existingDataModel = newDataModel("UPG", "Upgrade  of " + model.getName());
        DataModel existingTablesDataModel = getExistingTablesDataModel();

        Set<String> processedTables = new HashSet<>();
        if (existingTablesDataModel != null) {
            for (Table<?> table : model.getTables()) {
                Optional<ExistingTable> existingJournalTable = Optional.empty();
                if (table.hasJournal()) {
                    existingJournalTable = existingTablesDataModel.mapper(ExistingTable.class).getEager(table.getJournalTableName());
                }
                addTableToExistingModel(existingDataModel, existingTablesDataModel, table.getName(), (existingJournalTable.isPresent() ? existingJournalTable.get().getName() : null), processedTables, model.getTables());
            }
        }
        return existingDataModel;
    }

    private void addTableToExistingModel(DataModelImpl existingModel, DataModel databaseTablesModel, String tableName, String journalTableName, Set<String> processedTables, List<? extends Table> tablesToBeProcessed) {
        if (processedTables.add(tableName)) {
            Optional<ExistingTable> existingTable = databaseTablesModel.mapper(ExistingTable.class).getEager(tableName);
            if (existingTable.isPresent()) {
                ExistingTable userTable = existingTable.get();

                userTable.addColumnsTo(existingModel, journalTableName);

                for (ExistingConstraint existingConstraint : userTable.getConstraints()) {
                    if (existingConstraint.isForeignKey()) {
                        String referencedTableName = existingConstraint.getReferencedTableName();
                        if (!tableName.equalsIgnoreCase(referencedTableName) && existingModel.getTable(referencedTableName) == null) {
                            String refJournalTableName = tablesToBeProcessed.stream()
                                    .filter(table -> table.getName().equals(referencedTableName))
                                    .map(Table::getJournalTableName)
                                    .filter(Objects::nonNull)
                                    .findAny()
                                    .orElse(null);

                            addTableToExistingModel(existingModel, databaseTablesModel, referencedTableName, refJournalTableName, processedTables, tablesToBeProcessed);
                        }
                    }
                }
                userTable.addConstraintsTo(existingModel);
            }
        }
    }

	@Override
	public void dropJournal(Instant upTo, Logger logger) {
		dataModels.values().forEach(dataModel -> dataModel.dropJournal(upTo, logger));
	}

	@Override
	public void dropAuto(LifeCycleClass lifeCycleClass, Instant upTo, Logger logger) {
		dataModels.values().forEach(dataModel -> dataModel.dropAuto(lifeCycleClass, upTo, logger));
	}

	@Override
	public void createPartitions(Instant upTo, Logger logger) {
		dataModels.values().forEach(dataModel -> dataModel.createPartitions(upTo, logger));
	}

    @Override
    public DataModelUpgrader getDataModelUpgrader(Logger logger) {
        return DataModelUpgraderImpl.forUpgrade(schemaInfoProvider, this, logger);
    }

    @Override
    public DataModelDifferencesLister getDataModelDifferences(Logger logger) {
        return DataModelUpgraderImpl.forDifferences(schemaInfoProvider, this, fileSystem, logger);
    }

    public FileSystem getFileSystem() {
        return fileSystem;
    }

    public DataModel getFullModel() {
        DataModelImpl fullModel = new DataModelImpl(this);
        dataModels.values().forEach(fullModel::addAllTables);
        return fullModel;
    }

    private class ClearCachesOnTransactionRollBack implements Subscriber {
        @Override
        public void handle(Object notification, Object... notificationDetails) {
            if (((TransactionEvent) notification).hasFailed()) {
                getDataModels()
                        .stream()
                        .map(DataModel::getTables)
                        .flatMap(List::stream)
                        .map(TableImpl.class::cast)
                        .forEach(TableImpl::renewCache);
            }
        }

        @Override
        public Class<?>[] getClasses() {
            return new Class<?>[]{TransactionEvent.class};
        }
    }
}
