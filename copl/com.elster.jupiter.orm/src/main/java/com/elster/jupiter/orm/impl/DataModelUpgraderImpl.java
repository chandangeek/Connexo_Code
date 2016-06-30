package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.orm.schema.ExistingConstraint;
import com.elster.jupiter.orm.schema.ExistingTable;
import com.elster.jupiter.orm.schema.SchemaInfoProvider;
import com.elster.jupiter.util.streams.Functions;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.streams.Predicates.not;

public class DataModelUpgraderImpl implements DataModelUpgrader {

    private static final String EXISTING_TABLES_DATA_MODEL = "ORA";

    private final SchemaInfoProvider schemaInfoProvider;
    private final OrmServiceImpl ormService;
    private final Logger logger;
    private final State state;

    private interface State {
        Context createContext(DataModelImpl dataModel);

        void handleDifferences(Context context, List<Difference> differences);

        TableDdlGenerator ddlGenerator(TableImpl<?> table, Version version);

        Optional<Difference> removeTable(TableImpl<?> table);

        Stream<TableImpl<?>> dropCandidates(DataModelImpl dataModel);

    }

    private interface Context extends AutoCloseable {
        Statement getStatement();

        @Override
        void close();
    }

    private static class PerformCautiousUpgrade implements State {

        @Override
        public Context createContext(DataModelImpl dataModel) {
            return new LongContext(dataModel);
        }

        @Override
        public void handleDifferences(Context context, List<Difference> differences) {
            differences.stream()
                    .map(Difference::ddl)
                    .flatMap(List::stream)
                    .forEach(each -> execute(context, each));
        }

        private void execute(Context context, String each) {
            try {
                context.getStatement().execute(each);
            } catch (SQLException sqe) {
                throw new UnderlyingSQLFailedException(sqe);
            }
        }


        @Override
        public TableDdlGenerator ddlGenerator(TableImpl<?> table, Version version) {
            return TableDdlGenerator.cautious(table, table.getDataModel()
                    .getSqlDialect(), version);
        }

        @Override
        public Stream<TableImpl<?>> dropCandidates(DataModelImpl dataModel) {
            return Stream.empty();
        }

        @Override
        public Optional<Difference> removeTable(TableImpl<?> table) {
            // let it live
            return Optional.empty();
        }

    }

    private static class LongContext implements Context {
        private final Connection connection;
        private final Statement statement;

        public LongContext(DataModelImpl dataModel) {
            try {
                connection = dataModel.getConnection(false);
                statement = connection.createStatement();
            } catch (SQLException e) {
                throw new UnderlyingSQLFailedException(e);
            }
        }

        @Override
        public Statement getStatement() {
            return statement;
        }

        @Override
        public void close() {
            SQLException thrown = null;
            try {
                statement.close();
            } catch (SQLException e) {
                thrown = e;
            }
            try {
                connection.close();
            } catch (SQLException e) {
                if (thrown != null) {
                    e.addSuppressed(thrown);
                }
                throw new UnderlyingSQLFailedException(e);
            }
        }
    }

    private static class CollectStrictUpgrade implements State {
        @Override
        public Context createContext(DataModelImpl dataModel) {
            return new LongContext(dataModel);
        }

        @Override
        public void handleDifferences(Context context, List<Difference> differences) {
            // TODO
        }

        @Override
        public Stream<TableImpl<?>> dropCandidates(DataModelImpl dataModel) {
            return dataModel.getTables().stream();
        }

        @Override
        public TableDdlGenerator ddlGenerator(TableImpl<?> table, Version version) {
            return TableDdlGenerator.strict(table, table.getDataModel()
                    .getSqlDialect(), version);
        }

        @Override
        public Optional<Difference> removeTable(TableImpl<?> table) {
            return DifferenceImpl.builder("Table " + table.getName() + " : Removed table")
                    .add("drop table " + table.getName().toUpperCase() + " cascade constraints")
                    .build();
        }

    }

    public static DataModelUpgrader forUpgrade(SchemaInfoProvider schemaInfoProvider, OrmServiceImpl ormService, Logger logger) {
        return new DataModelUpgraderImpl(schemaInfoProvider, ormService, logger, new PerformCautiousUpgrade());
    }

    private DataModelUpgraderImpl(SchemaInfoProvider schemaInfoProvider, OrmServiceImpl ormService, Logger logger, State state) {
        this.schemaInfoProvider = schemaInfoProvider;
        this.ormService = ormService;
        this.logger = logger;
        this.state = state;
    }

    @Override
    public void upgrade(DataModel dataModel, Version version) {
        DataModelImpl current = getCurrentDataModel((DataModelImpl) dataModel, version);
        try (Context context = state.createContext(current)) {
            List<Difference> allDdl = determineDdl(current, (DataModelImpl) dataModel, version, context);
            state.handleDifferences(context, allDdl);
        }
    }

    private DataModelImpl getCurrentDataModel(DataModelImpl model, Version version) {
        DataModelImpl currentDataModel = ormService.newDataModel("UPG", "Upgrade  of " + model.getName());
        DataModel schemaMetaDataModel = getSchemaDataModel();

        Set<String> processedTables = new HashSet<>();
        if (schemaMetaDataModel != null) {
            for (TableImpl<?> table : model.getTables(version)) {
                String existingJournalTableName = table.hasJournal() ? getExistingJournalTableName(schemaMetaDataModel, table) : null;
                table.getHistoricalNames()
                        .stream()
                        .forEach(tableName -> addTableToExistingModel(currentDataModel, schemaMetaDataModel, tableName, existingJournalTableName, processedTables));
            }
        }
        return currentDataModel;
    }

    private String getExistingJournalTableName(DataModel schemaMetaDataModel, TableImpl<?> table) {
        return schemaMetaDataModel.mapper(ExistingTable.class)
                .getEager(table.getJournalTableName())
                .map(ExistingTable::getName)
                .orElse(null);
    }

    private DataModel getSchemaDataModel() {
        DataModel dataModel = ormService.newDataModel(EXISTING_TABLES_DATA_MODEL, "Oracle schema");
        for (SchemaInfoProvider.TableSpec each : schemaInfoProvider.getSchemaInfoTableSpec()) {
            each.addTo(dataModel);
        }
        dataModel.register();
        return dataModel;
    }

    private void addTableToExistingModel(DataModelImpl currentDataModel, DataModel schemaMetaDataModel, String tableName, String journalTableName, Set<String> processedTables) {
        if (processedTables.add(tableName)) {
            schemaMetaDataModel
                    .mapper(ExistingTable.class)
                    .getEager(tableName)
                    .ifPresent(userTable -> {
                        userTable.addColumnsTo(currentDataModel, journalTableName);
                        userTable.getConstraints()
                                .stream()
                                .filter(ExistingConstraint::isForeignKey)
                                .map(ExistingConstraint::getReferencedTableName)
                                .filter(referencedTableName -> !tableName.equalsIgnoreCase(referencedTableName) && currentDataModel
                                        .getTable(referencedTableName) == null)
                                .forEach(referencedTableName -> addTableToExistingModel(currentDataModel, schemaMetaDataModel, referencedTableName, null, processedTables));
                        userTable.addConstraintsTo(currentDataModel);
                    });
        }
    }

    private List<Difference> determineDdl(DataModelImpl fromDataModel, DataModelImpl toDataModel, Version version, Context context) {
        Stream<Difference> upgradeTableDdl = upgradeDdl(fromDataModel, toDataModel, version, context);
        Stream<Difference> dropTableDdl = dropTableDdl(fromDataModel, toDataModel, version);
        return Stream.of(upgradeTableDdl, dropTableDdl)
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }

    private Stream<Difference> dropTableDdl(DataModelImpl fromDataModel, DataModelImpl toDataModel, Version version) {
        Set<TableImpl<?>> stillExist = toDataModel.getTables()
                .stream()
                .map(table -> findFromTable(fromDataModel, table, version))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        return state.dropCandidates(fromDataModel)
                .filter(not(stillExist::contains))
                .map(state::removeTable)
                .flatMap(Functions.asStream());
    }

    private Stream<Difference> upgradeDdl(DataModelImpl fromDataModel, DataModelImpl toDataModel, Version version, Context context) {
        return toDataModel.getTables(version)
                .stream()
                .filter(Table::isAutoInstall)
                .map(toTable -> upgradeTo(fromDataModel, toTable, version, context))
                .flatMap(List::stream);
    }

    private List<Difference> upgradeTo(DataModelImpl fromDataModel, TableImpl<?> toTable, Version version, Context context) {
        try {
            return tryUpgradeTo(fromDataModel, toTable, version, context);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private List<Difference> tryUpgradeTo(DataModelImpl fromDataModel, TableImpl<?> toTable, Version version, Context context) throws
            SQLException {
        TableImpl<?> fromTable = findFromTable(fromDataModel, toTable, version);
        if (fromTable != null) {
            return upgradeTable(toTable, fromTable, version, context);
        } else {
            return Collections.singletonList(createTable(toTable, version));
        }
    }

    private TableImpl<?> findFromTable(DataModelImpl fromDataModel, TableImpl<?> toTable, Version version) {
        TableImpl<?> fromTable = fromDataModel.getTable(toTable.getName(version), version);
        if (fromTable != null) {
            return fromTable;
        }
        return toTable.previousTo(version)
                .map(previousVersion -> fromDataModel.getTable(toTable.getName(previousVersion), previousVersion))
                .orElse(null);
    }

    private Difference createTable(TableImpl<?> table, Version version) throws SQLException {
        DifferenceImpl.DifferenceBuilder difference = DifferenceImpl.builder("Table " + table.getName() + " : Added table");
        table.getDdl(version).forEach(difference::add);
        return difference.build().get();
    }

    private List<Difference> upgradeTable(TableImpl<?> toTable, TableImpl<?> fromTable, Version version, Context context) throws
            SQLException {
        List<Difference> upgradeDdl = state.ddlGenerator(fromTable, version).upgradeDdl(toTable);
        for (ColumnImpl sequenceColumn : toTable.getAutoUpdateColumns()) {
            if (sequenceColumn.getQualifiedSequenceName() != null) {
                long sequenceValue = getLastSequenceValue(context, sequenceColumn.getQualifiedSequenceName());
                long maxColumnValue = fromTable.getColumn(sequenceColumn.getName()) != null ? maxColumnValue(context, sequenceColumn) : 0;
                if (maxColumnValue > sequenceValue) {
                    upgradeDdl.add(state.ddlGenerator(toTable, version)
                            .upgradeSequenceDdl(sequenceColumn, maxColumnValue + 1));
                }
            }
        }
        return upgradeDdl;
    }

    private long maxColumnValue(Context context, ColumnImpl sequenceColumn) {
        try (ResultSet resultSet = context.getStatement()
                .executeQuery("select nvl(max(" + sequenceColumn.getName() + ") ,0) from " + sequenceColumn
                        .getTable()
                        .getName())) {
            if (resultSet.next()) {
                return resultSet.getLong(1);
            } else {
                return 0;
            }
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private long getLastSequenceValue(Context context, String sequenceName) {
        try (ResultSet resultSet = context.getStatement()
                .executeQuery("select last_number from user_sequences where sequence_name = '" + sequenceName + "'")) {
            if (resultSet.next()) {
                return resultSet.getLong(1);
            }
            // to indicate that the sequence is not there
            return -1;
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

}
