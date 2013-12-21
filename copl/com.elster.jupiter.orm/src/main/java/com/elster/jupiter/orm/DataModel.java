package com.elster.jupiter.orm;

import java.security.Principal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.google.inject.Injector;

/**
 * @author kha
 * DataModel is a container for a component's table description objects.
 *
 */
public interface DataModel {
	// operational api
	void persist(Object entity);
    void update(Object entity);
    void remove(Object entity);
    <T> T getInstance(Class<T> clazz);
    <T> DataMapper<T> mapper(Class<T> api);
    RefAny asRefAny(Object object);
    // creation api 
    Table addTable(String name,Class<?> api);
	Table addTable(String schema, String tableName, Class<?> api);
    void setInjector(Injector injector);
	void register();
	// courtesy methods
    Connection getConnection(boolean transactionRequired) throws SQLException;
	Principal getPrincipal();
	SqlDialect getSqlDialect();
	// meta data api
	String getName();
	String getDescription();
    List<? extends Table> getTables();
    // installation
	void install(boolean executeDdl, boolean store);
    @Deprecated
	<T> DataMapper<T> getDataMapper(Class<T> api,String tableName);
	
}
