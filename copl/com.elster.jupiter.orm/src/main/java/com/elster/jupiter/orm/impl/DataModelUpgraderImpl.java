package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.orm.schema.ExistingConstraint;
import com.elster.jupiter.orm.schema.ExistingTable;
import com.elster.jupiter.orm.schema.SchemaInfoProvider;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class DataModelUpgraderImpl implements DataModelUpgrader {

    private static final String EXISTING_TABLES_DATA_MODEL = "ORA";

    private final SchemaInfoProvider schemaInfoProvider;
    private final OrmServiceImpl ormService;
    private final Logger logger;

    public DataModelUpgraderImpl(SchemaInfoProvider schemaInfoProvider, OrmServiceImpl ormService, Logger logger) {
        this.schemaInfoProvider = schemaInfoProvider;
        this.ormService = ormService;
        this.logger = logger;
    }

    @Override
    public void upgrade(DataModel dataModel, Version version) {
        DataModelImpl current = getCurrentDataModel((DataModelImpl) dataModel, version);
        upgradeTo(current, (DataModelImpl) dataModel, version);
    }

    private DataModelImpl getCurrentDataModel(DataModelImpl model, Version version) {
        DataModelImpl currentDataModel = ormService.newDataModel("UPG", "Upgrade  of " + model.getName());
        DataModel schemaMetaDataModel = getSchemaDataModel();

        Set<String> processedTables = new HashSet<>();
        if (schemaMetaDataModel != null) {
            for (TableImpl<?> table : model.getTables(version)) {
                String existingJournalTableName =
                        table.hasJournal()
                                ? schemaMetaDataModel.mapper(ExistingTable.class).getEager(table.getJournalTableName()).map(ExistingTable::getName).orElse(null)
                                : null;
                table.getHistoricalNames()
                        .stream()
                        .forEach(tableName -> addTableToExistingModel(currentDataModel, schemaMetaDataModel, tableName, existingJournalTableName, processedTables));
            }
        }
        return currentDataModel;
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
                                .filter(referencedTableName -> !tableName.equalsIgnoreCase(referencedTableName) && currentDataModel.getTable(referencedTableName) == null)
                                .forEach(referencedTableName -> addTableToExistingModel(currentDataModel, schemaMetaDataModel, referencedTableName, null, processedTables));
                        userTable.addConstraintsTo(currentDataModel);
                    });
        }
    }

    private void upgradeTo(DataModelImpl fromDataModel, DataModelImpl toDataModel, Version version) {
        try (Connection connection = fromDataModel.getConnection(false)) {
            upgradeTo(fromDataModel, toDataModel, version, connection);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }

    }

    private void upgradeTo(DataModelImpl fromDataModel, DataModelImpl toDataModel, Version version, Connection connection) throws
            SQLException {
        try (Statement statement = connection.createStatement()) {
            toDataModel.getTables(version)
                    .stream()
                    .filter(Table::isAutoInstall)
                    .forEach(toTable -> upgradeTo(fromDataModel, toTable, version, statement));
        }
    }

    private void upgradeTo(DataModelImpl fromDataModel, TableImpl<?> toTable, Version version, Statement statement) {
        try {
            tryUpgradeTo(fromDataModel, toTable, version, statement);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private void tryUpgradeTo(DataModelImpl fromDataModel, TableImpl<?> toTable, Version version, Statement statement) throws SQLException {
        TableImpl<?> fromTable = fromDataModel.getTable(toTable.getName(version), version);
        if (fromTable == null) {
            fromTable = toTable.previousTo(version)
                    .map(previousVersion -> fromDataModel.getTable(toTable.getName(previousVersion), previousVersion))
                    .orElse(null);
        }
        if (fromTable != null) {
            upgradeTable(toTable, statement, fromTable, version);
        } else {
            createTable(toTable, statement, version);
        }
    }

    private void createTable(TableImpl<?> toTable, Statement statement, Version version) throws SQLException {
        List<String> ddl = toTable.getDdl(version);
        executeSqlStatements(statement, ddl);
    }

    private void upgradeTable(TableImpl<?> toTable, Statement statement, TableImpl<?> fromTable, Version version) throws SQLException {
        List<String> upgradeDdl = fromTable.upgradeDdl(toTable, version);
        for (ColumnImpl sequenceColumn : toTable.getAutoUpdateColumns()) {
            if (sequenceColumn.getQualifiedSequenceName() != null) {
                long sequenceValue = getLastSequenceValue(statement, sequenceColumn.getQualifiedSequenceName());
                long maxColumnValue = fromTable.getColumn(sequenceColumn.getName()) != null ? maxColumnValue(sequenceColumn, statement) : 0;
                if (maxColumnValue > sequenceValue) {
                    upgradeDdl.addAll(toTable.upgradeSequenceDdl(sequenceColumn, maxColumnValue + 1, version));
                }
            }
        }
        executeSqlStatements(statement, upgradeDdl);
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
            }
            // to indicate that the sequence is not there
            return -1;
        }
    }

    private void executeSqlStatements(Statement statement, List<String> sqlStatements) throws SQLException {
        for (String each : sqlStatements) {
            try {
                statement.execute(each);
            } catch (SQLException sqe) {
                throw new SQLException("SQL error while executing '" + each + "' : " + sqe.getMessage(), sqe);
            }
        }
    }

}
