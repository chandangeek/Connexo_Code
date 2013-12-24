package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.SqlDialect;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.associations.RefAny;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.associations.impl.RefAnyImpl;
import com.elster.jupiter.util.time.Clock;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Types;

import oracle.jdbc.OracleConnection;

import javax.inject.Inject;
import javax.validation.ValidatorFactory;

import java.lang.reflect.Type;
import java.security.Principal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;


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

    @Override
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
    @Deprecated
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
    
    void preSave() {
    	injector = Guice.createInjector();
    }
    
    Injector getInjector() {
    	return injector;
    }
    
    @Override
    public void register(Module ... modules) {
    	if (registered) {
    		throw new IllegalStateException();
    	}
    	Module[] allModules = new Module[modules.length + 1];
    	System.arraycopy(modules, 0, allModules, 0, modules.length);
    	allModules[modules.length] = getModule();
    	injector = Guice.createInjector(allModules);
        for (TableImpl each : tables) {
        	each.prepare();
       	}
    	this.ormService.register(this);
    	registered = true;
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
	
	@Override 
	public <T> QueryExecutor<T> query(Class<T> api, Class<?> ... eagers) {
		DataMapper<?>[] mappers = new DataMapper[eagers.length];
		for (int i = 0; i < eagers.length; i++) {
			mappers[i] = mapper(eagers[i]);
		}
 		return mapper(api).with(mappers);
	}
	
	Module getModule() {
    	return new AbstractModule() {	
			@SuppressWarnings("unchecked")
			@Override
			public void configure() {
				Set<TypeLiteral<Reference<?>>> referenceTypeLiterals = new HashSet<>(); 
				Set<TypeLiteral<List<?>>> listTypeLiterals = new HashSet<>();
				for (TableImpl table : getTables()) {
					for (ForeignKeyConstraintImpl constraint : table.getForeignKeyConstraints()) {
						Optional<Type> referenceParameterType = constraint.getReferenceParameterType();
						if (referenceParameterType.isPresent()) {
							Type referenceType = Types.newParameterizedType(Reference.class, referenceParameterType.get());
							TypeLiteral<Reference<?>> typeLiteral = (TypeLiteral<Reference<?>>) TypeLiteral.get(referenceType);
							referenceTypeLiterals.add(typeLiteral);
						}
						Optional<Type> listParameterType = constraint.getListParameterType();
						if (listParameterType.isPresent()) {
							Type referenceType = Types.newParameterizedType(List.class, listParameterType.get());
							TypeLiteral<List<?>> typeLiteral = (TypeLiteral<List<?>>) TypeLiteral.get(referenceType);
							listTypeLiterals.add(typeLiteral);
						}
 					}
				}
				for (TypeLiteral<Reference<?>> each : referenceTypeLiterals) {
					bind(each).toProvider(getReferenceProvider());
				}
				for (TypeLiteral<List<?>> each : listTypeLiterals) {
					bind(each).toProvider(getListProvider());
				}
			}
		}; 	
    } 
	
	Provider<? extends Reference<?>> getReferenceProvider() {
		return new Provider<Reference<?>> () {

			@Override
			public Reference<?> get() {
				return ValueReference.absent();
			}
			
		};
	}
    
	Provider<? extends List<?>> getListProvider() {
		return new Provider<List<?>> () {

			@Override
			public List<?> get() {
				return new ArrayList<>();
			}
			
		};
	}

	@Override
	public ValidatorFactory getValidatorFactory() {
		return ormService.getValidatorFactory();
	}
	
	
	
}
