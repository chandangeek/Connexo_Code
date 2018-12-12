/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.upgrade.impl;

import com.elster.jupiter.orm.DataDropper;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.PartitionCreator;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.orm.SqlDialect;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.orm.associations.RefAny;

import com.google.common.collect.ImmutableSortedSet;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import javax.validation.ValidatorFactory;
import java.security.Principal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.SortedSet;
import java.util.logging.Logger;

//import com.elster.jupiter.orm.impl.TableImpl;

public class InjectOnly implements DataModel {

    private Injector injector;

    @Override
    public void persist(Object entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void update(Object entity, String... fieldNames) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void touch(Object entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(Object entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T getInstance(Class<T> clazz) {
        return injector.getInstance(clazz);
    }

    @Override
    public <T> DataMapper<T> mapper(Class<T> api) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> QueryExecutor<T> query(Class<T> api, Class<?>... eagers) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> QueryStream<T> stream(Class<T> api) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RefAny asRefAny(Object object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Table<?> getTable(String tableName, Version version) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Table<T> addTable(String name, Class<T> api) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Table<T> addTable(String schema, String tableName, Class<T> api) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void register(Module... modules) {
        injector = Guice.createInjector(modules);
    }

    @Override
    public boolean isInstalled() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Connection getConnection(boolean transactionRequired) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Principal getPrincipal() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SqlDialect getSqlDialect() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ValidatorFactory getValidatorFactory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDescription() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<? extends Table<?>> getTables() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<? extends Table<?>> getTables(Version version) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Table<?> getTable(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void install(boolean executeDdl, boolean store) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> void reorder(List<T> list, List<T> newOrder) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DataDropper dataDropper(String tableName, Logger logger) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PartitionCreator partitionCreator(String tableName, Logger logger) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Version getVersion() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SortedSet<Version> changeVersions() {
        return ImmutableSortedSet.of();
    }
}
