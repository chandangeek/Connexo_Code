package com.elster.jupiter.orm;

import com.elster.jupiter.orm.associations.RefAny;
import com.google.inject.Module;

import java.security.Principal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.validation.ValidatorFactory;

/**
 * @author kha
 *         DataModel is a container for a component's table description objects.
 */
public interface DataModel {

    // operational api
    void persist(Object entity);

    void update(Object entity, String... fieldNames);
    
    void touch(Object entity);
    
    void remove(Object entity);
    
     <T> T getInstance(Class<T> clazz);

    <T> DataMapper<T> mapper(Class<T> api);

    <T> QueryExecutor<T> query(Class<T> api, Class<?> ... eagers);

    RefAny asRefAny(Object object);

    // creation api
    <T> Table<T> addTable(String name, Class<T> api);
    <T> Table<T> addTable(String schema, String tableName, Class<T> api);
    void register(Module... modules);
    boolean isInstalled();

    // courtesy methods
    Connection getConnection(boolean transactionRequired) throws SQLException;

    Principal getPrincipal();

    SqlDialect getSqlDialect();
    
    ValidatorFactory getValidatorFactory();

    // meta data api
    String getName();

    String getDescription();

    List<? extends Table<?>> getTables();

    Table<?> getTable(String name);

    // installation
    void install(boolean executeDdl, boolean store);

    <T> void reorder(List<T> list, List<T> newOrder);
    
    @Deprecated
    <T> DataMapper<T> getDataMapper(Class<T> api, String tableName);

}
