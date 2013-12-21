package com.elster.jupiter.orm.impl;

import java.security.Principal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import oracle.jdbc.OracleConnection;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.RefAny;
import com.elster.jupiter.orm.SqlDialect;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.util.time.Clock;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Injector;


public class DataModelImpl implements DataModel {

    // persistent fields
    private String name;
    private String description;

    // associations
    private final List<TableImpl> tables = new ArrayList<>();
    
    // transient fields
    private Injector injector;
    private final OrmServiceImpl ormService;
    
    private boolean registered;

    @Inject
    DataModelImpl (OrmService ormService) {
    	this.ormService = (OrmServiceImpl) ormService;
    }
    
    DataModelImpl init(String name, String description) {
        this.name = name;
        this.description = description;
        return this;
    }

    static DataModelImpl from(OrmServiceImpl ormService, String name, String description) {
    	return new DataModelImpl(ormService).init(name, description);
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
    public List<TableImpl> getTables() {
        return ImmutableList.copyOf(tables);
    }

    public TableImpl getTable(String tableName) {
        for (TableImpl table : tables) {
            if (table.getName().equals(tableName)) {
                return table;
            }
        }
        return null;
    }

    @Override
    public Table addTable(String tableName,Class<?> api) {
        return addTable(null, tableName,api);
    }
    
    private void checkActiveBuilder() {
    	if (!getTables().isEmpty()) {
    		tables.get(getTables().size()-1).checkActiveBuilder();
    	}
    }

    @Override
    public TableImpl addTable(String schema, String tableName, Class<?> api) {
    	checkActiveBuilder();
        if (getTable(tableName) != null) {
            throw new IllegalArgumentException("Component has already table " + tableName);
        }
        TableImpl table = TableImpl.from(this, schema, tableName, api, getTables().size() + 1);
        add(table);
        return table;
    }

    @Override
    public String toString() {
        return Joiner.on(" ").join("DataModel",name,"(" + description + ")");
    }

    private void add(TableImpl table) {
        tables.add(table);
    }

    @Override
    public <T> DataMapperImpl<T> mapper(Class<T> api) {
    	for (TableImpl table : tables) {
    		if (table.maps(api)) {
    			return table.getDataMapper(api);
    		}
    	}
    	throw new IllegalArgumentException("Type " + api + " not configured in data model " + this.getName());
    }
    
    @Override
    public <T> DataMapperImpl<T> getDataMapper(Class<T> api, String tableName) {
    	return getTable(tableName).getDataMapper(api);
    }

    @Override
    public void install(boolean executeDdl, boolean store) {
        if (executeDdl) {
            executeDdl();
        }
        if (store) {
            ormService.getDataModel(OrmService.COMPONENTNAME).get().persist(this);
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
                Connection connection = getConnection(false);
                Statement statement = connection.createStatement()
        ) {
            doExecuteDdl(statement);
        }
    }

    private void doExecuteDdl(Statement statement) throws SQLException {
        for (TableImpl table : tables) {
            executeTableDdl(statement, table);
        }
    }

    private void executeTableDdl(Statement statement, TableImpl table) throws SQLException {
        for (String each : table.getDdl()) {            
            statement.execute(each);
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
                return SqlDialect.ORACLE;
            }
            return SqlDialect.ANSI;
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }
    
    public Optional<TableImpl> getTable(Class<?> clazz) {
    	for (TableImpl table : getTables()) {
    		if (table.maps(clazz)) {
    			return Optional.of(table);
    		}
    	}
    	return Optional.absent();
    }
    
    @Override
    public RefAny asRefAny(Object reference) {
    	Class<?> clazz = Objects.requireNonNull(reference).getClass();
		for (DataModelImpl dataModel : getOrmService().getDataModels()) {
			Optional<TableImpl> tableHolder = dataModel.getTable(clazz);
			if (tableHolder.isPresent()) {
				return getInstance(RefAnyImpl.class).init(reference,tableHolder.get());
			}
		} 
		throw new IllegalArgumentException("No table defined that maps " + reference.getClass());
    }
    
    void prepare() {
    	if (registered) {
    		throw new IllegalStateException();
    	}
    	if (injector == null) {
    		injector = Guice.createInjector();
    	}
    	for (TableImpl each : tables) {
    		each.prepare();
    	}
    	registered = true;
    }
    
    @Override
    public void setInjector(Injector injector) {
    	this.injector = Objects.requireNonNull(injector);
    }
    

    Injector getInjector() {
    	return injector;
    }
    
    @Override
    public void register() {
    	this.ormService.register(this);
    }

	@Override
	public <T> T getInstance(Class<T> clazz) {
		return injector.getInstance(clazz);
	}

	@SuppressWarnings("unchecked")
	private <T> void persist(Class<T> type , Object entity) {
		DataMapper<T> mapper = mapper(type);
		mapper.persist((T) entity);
	}
	
	@SuppressWarnings("unchecked")
	private <T> void update(Class<T> type , Object entity) {
		DataMapper<T> mapper = mapper(type);
		mapper.update((T) entity);
	}
	
	@SuppressWarnings("unchecked")
	private <T> void remove(Class<T> type, Object entity) {
		DataMapper<T> mapper = mapper(type);
		mapper.remove((T) entity);
	}
	
	@Override
	public void persist(Object entity) {
		persist(Objects.requireNonNull(entity.getClass()),entity);
	}

	@Override
	public void update(Object entity) {
		update(Objects.requireNonNull(entity).getClass(),entity);
	}
	
	@Override
	public void remove(Object entity) {
		remove(Objects.requireNonNull(entity).getClass(),entity);	
	}
	
	public Clock getClock() {
		return ormService.getClock();
	}

	public OrmServiceImpl getOrmService() {
		return ormService;
	}

    
}
