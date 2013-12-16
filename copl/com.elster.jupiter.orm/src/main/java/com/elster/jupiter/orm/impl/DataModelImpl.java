package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.RefAny;
import com.elster.jupiter.orm.SqlDialect;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.orm.internal.*;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import oracle.jdbc.OracleConnection;

import java.security.Principal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class DataModelImpl implements DataModel, PersistenceAware {

    // persistent fields
    private String name;
    private String description;

    // associations
    private List<Table> tables;

    @SuppressWarnings("unused")
    private DataModelImpl() {
    }

    public DataModelImpl(String name, String description) {
        this.name = name;
        this.description = description;
        this.tables = new ArrayList<>();
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
    public List<Table> getTables() {
        return ImmutableList.copyOf(doGetTables());
    }

    public List<Table> doGetTables() {
        if (tables == null) {
            tables = getOrmClient().getTableFactory().find("component", this);
        }
        return tables;
    }

    @Override
    public Table getTable(String tableName) {
        for (Table table : getTables()) {
            if (table.getName().equals(tableName)) {
                return table;
            }
        }
        return null;
    }

    @Override
    public Table addTable(String tableName) {
        return addTable(null, tableName);
    }
    
    private void checkActiveBuilder() {
    	if (!getTables().isEmpty()) {
    		downCast(getTables().get(getTables().size()-1)).checkActiveBuilder();
    	}
    }

    @Override
    public Table addTable(String schema, String tableName) {
    	checkActiveBuilder();
        if (getTable(tableName) != null) {
            throw new IllegalArgumentException("Component has already table " + tableName);
        }
        Table table = new TableImpl(this, schema, tableName);
        add(table);
        return table;
    }

    @Override
    public String toString() {
        return "Component " + name;
    }

    private void add(Table table) {
        doGetTables().add(table);
    }

    public void persist() {
        getOrmClient().getDataModelFactory().persist(this);
        for (Table table : doGetTables()) {
            downCast(table).persist();
        }
    }

    private OrmClient getOrmClient() {
        return Bus.getOrmClient();
    }

    @Override
    public void postLoad() {
        doGetTables();
    }

    @Override
    public <T> DataMapper<T> getDataMapper(Class<T> api, String tableName) {
    	return getTable(tableName).getDataMapper(api);
    }
    
    @Override
    public <T> DataMapper<T> getDataMapper(Class<T> api, Class<? extends T> implementation, String tableName) {
        return getTable(tableName).getDataMapper(api, implementation);
    }

    @Override
    public <T> DataMapper<T> getDataMapper(Class<T> api, Map<String, Class<? extends T>> implementations, String tableName) {
        return getTable(tableName).getDataMapper(api, implementations);
    }

    @Override
    public void install(boolean executeDdl, boolean store) {
        if (executeDdl) {
            executeDdl();
        }
        if (store) {
            persist();
        }

    }

    private void executeDdl() {
        try {
            doExecuteDdl();
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
    }

    private void doExecuteDdl() throws SQLException {
        try (
                Connection connection = Bus.getConnection(false);
                Statement statement = connection.createStatement()
        ) {
            doExecuteDdl(statement);
        }
    }

    private void doExecuteDdl(Statement statement) throws SQLException {
        for (Table table : getTables()) {
            executeTableDdl(statement, downCast(table));
        }
    }

    private void executeTableDdl(Statement statement, TableImpl table) throws SQLException {
        for (String each : table.getDdl()) {            
            statement.execute(each);
        }
    }

    @Override
    public Connection getConnection(boolean transactionRequired) throws SQLException {
        return Bus.getConnection(transactionRequired);
    }

    @Override
    public Principal getPrincipal() {
        return Bus.getPrincipal();
    }

    @Override
    public SqlDialect getSqlDialect() {
        try (Connection connection = getConnection(false)) {
            if (connection.isWrapperFor(OracleConnection.class)) {
                return SqlDialect.ORACLE;
            }
            return SqlDialect.ANSI;
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }
    
    @Override
    public Optional<Table> getTable(Class<?> clazz) {
    	for (Table table : getTables()) {
    		if (table.maps(clazz)) {
    			return Optional.of(table);
    		}
    	}
    	return Optional.absent();
    }
    
    @Override
    public RefAny asRefAny(Object reference) {
    	return RefAnyImpl.of(reference);
    }
    
    private TableImpl downCast(Table table) {
    	return (TableImpl) table;
    }
    
    void prepare() {
    	checkActiveBuilder();
    	for (Table each : getTables()) {
    		downCast(each).prepare();
    	}
    }
}
