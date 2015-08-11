package com.elster.jupiter.orm;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.orm.associations.RefAny;
import com.google.inject.Module;

import javax.validation.ValidatorFactory;

import java.security.Principal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

/**
 * DataModel is a container for a component's table description objects.
 */
@ProviderType
public interface DataModel {

    /*
     * persist the argument by inserting a new tuple
     */
    void persist(Object entity);

    /*
     * updates the tuple mapped to the first argument.
     * if fieldNames is empty, update all fields, otherwise only passed fields
     * note than ORM managed fields (user, modification time, version ...) are always updated
     */
    void update(Object entity, String... fieldNames);

    /*
     * updates the ORM managed fields in the tuple mapped to the argument
     */
    void touch(Object entity);

    /*
     * deletes the tuple mapped to the argument
     */
    void remove(Object entity);

    /*
     * creates a new instance of the argument by delegating to the DataModel's injector
     */
    <T> T getInstance(Class<T> clazz);

    /*
     * obtain a DataMapper for the given type
     */
    <T> DataMapper<T> mapper(Class<T> api);

    /*
     * obtain a QueryExecutor to search for instances of the first argument
     * Additional types, associated with the main type, can be passes to allow additional search conditions,
     * or to eagerly fetch associations.
     * New code should prefer the stream api.
     */
    <T> QueryExecutor<T> query(Class<T> api, Class<?>... eagers);

    /*
     * Provides a fluent API alternative for query
     */
    <T> QueryStream<T> stream(Class<T> api);

    /*
     * create a RefAny for the argument, that can be mapped to a set of RefAny columns
     */
    RefAny asRefAny(Object object);

    /*
     * Adds a table in the default schema of the database user to the DataModel that maps to the type specified by the second argument
     */
    <T> Table<T> addTable(String name, Class<T> api);

    /*
     * Also allows you to specify the schema.
     */
    <T> Table<T> addTable(String schema, String tableName, Class<T> api);

    /*
     * Registers the dataModel with the ORM Service.
     * The modules in the argument are added to the DataModel's Guice injector
     */
    void register(Module... modules);

    /*
     * Tests whether the dataModel is installed (DDL has been executed)
     */
    boolean isInstalled();

    Connection getConnection(boolean transactionRequired) throws SQLException;

    /*
     * gets the current principal from the ThreadPrincipalService
     */
    Principal getPrincipal();

    SqlDialect getSqlDialect();

    ValidatorFactory getValidatorFactory();

    String getName();

    String getDescription();

    List<? extends Table<?>> getTables();

    Table<?> getTable(String name);

    void install(boolean executeDdl, boolean store);

    /*
     * Allows to reorder a persistent list that automatically maintains the position field for its elements
     */
    <T> void reorder(List<T> list, List<T> newOrder);

    /*
     * obtain a DataDropper to remove old data from the table with the given name
     */
	DataDropper dataDropper(String tableName, Logger logger);
	/*
	 * obtain a PartitionCreator for the table with the given name
	 */
	PartitionCreator partitionCreator(String tableName, Logger logger);
}
