/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Provides an implementation for the {@link com.elster.jupiter.properties.ValueFactory} interface
 * for references to objects. Uses the {@link OrmService} to find the {@link DataMapper} for
 * the related api class. Finding the DataModel/DataMapper is only done when the factory
 * is actually required to load or store values. This "lazy" approach supports
 * creating reference {@link com.elster.jupiter.properties.PropertySpec}s
 * for api-classes whose DataModel has not actually been activated yet.
 * <p>
 * Note that this class does not support multi value primary keys
 * and will throw an IllegalArgumentException when that is the case.
 * Because of the "lazy" behavior of this component, this exception
 * is only thrown when the factory is required to load or store values.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-12-09 (10:15)
 */
class ReferenceValueFactory<T> implements ValueFactory<T> {

    private final OrmService ormService;
    private final BeanService beanService;
    private Class<T> domainClass;
    private Mapping mapping;

    ReferenceValueFactory(OrmService ormService, BeanService beanService) {
        super();
        this.ormService = ormService;
        this.beanService = beanService;
    }

    ReferenceValueFactory<T> init(Class<T> domainClass) {
        this.domainClass = domainClass;
        return this;
    }

    ReferenceValueFactory<T> initWithValidation(Class<T> domainClass) {
        this.init(domainClass).ensureMappingInitializedOrThrowException();
        return this;
    }

    @Override
    public boolean isReference () {
        return true;
    }

    @Override
    public Class<T> getValueType () {
        return this.domainClass;
    }

    @Override
    public T fromStringValue(String stringValue) {
        this.ensureMappingInitializedOrThrowException();
        return this.mapping.fromStringValue(stringValue);
    }

    @Override
    public String toStringValue(T object) {
        this.ensureMappingInitializedOrThrowException();
        return this.mapping.toStringValue(object);
    }

    @Override
    public T valueFromDatabase(Object value) {
        this.ensureMappingInitializedOrThrowException();
        return this.mapping.valueFromDatabase(value);
    }

    @Override
    public Object valueToDatabase(T object) {
        this.ensureMappingInitializedOrThrowException();
        return this.mapping.valueToDatabase(object);
    }

    @Override
    public void bind(PreparedStatement statement, int offset, T value) throws SQLException {
        this.ensureMappingInitializedOrThrowException();
        this.mapping.bind(statement, offset, value);
    }

    @Override
    public void bind(SqlBuilder builder, T value) {
        this.ensureMappingInitializedOrThrowException();
        this.mapping.bind(builder, value);
    }

    @Override
    public boolean isValid(T value) {
        this.ensureMappingInitializedOrThrowException();
        return this.mapping.isPersistent(value);
    }

    private void ensureMappingInitializedOrThrowException() {
        if (this.mapping == null) {
            Mapping mapping = new Mapping();
            if (this.ormService
                    .getDataModels()
                    .stream()
                    .anyMatch(mapping::initIfSupported)) {
                this.mapping = mapping;
            }
            else {
                throw new IllegalArgumentException("Type " + domainClass + " not configured in any of the ORM data models");
            }
        }
    }

    private interface StringConverter<T> {
        T fromString(String stringValue);
    }

    private class IntegerConverter implements StringConverter<Integer> {
        @Override
        public Integer fromString(String stringValue) {
            return Integer.parseInt(stringValue);
        }
    }

    private class LongConverter implements StringConverter<Long> {
        @Override
        public Long fromString(String stringValue) {
            return Long.parseLong(stringValue);
        }
    }

    private class NoConversion implements StringConverter<String> {
        @Override
        public String fromString(String stringValue) {
            return stringValue;
        }
    }

    private interface PrimaryKeyChecker {
        boolean indicatesNull(Object value);
    }

    private class IntegerPrimaryKeyChecker implements PrimaryKeyChecker {
        @Override
        public boolean indicatesNull(Object value) {
            return value == null || ((Integer) value).intValue() == 0;
        }
    }

    private class LongPrimaryKeyChecker implements PrimaryKeyChecker {
        @Override
        public boolean indicatesNull(Object value) {
            return value == null || ((Long) value).intValue() == 0;
        }
    }

    private class StringPrimaryKeyChecker implements PrimaryKeyChecker {
        @Override
        public boolean indicatesNull(Object value) {
            return value == null || ((String) value).isEmpty();
        }
    }

    private class Mapping {
        private Table<T> table;
        private DataMapper<T> dataMapper;
        private Column primaryKeyColumn;
        private Class primaryKeyType;
        private StringConverter converter;
        private PrimaryKeyChecker primaryKeyChecker;

        @SuppressWarnings("unchecked")
        private boolean initIfSupported(DataModel dataModel) {
            try {
                this.dataMapper = dataModel.mapper(domainClass);
                // Would be nice if DataMapper#getTable() would exists or be public
                this.table = (Table<T>) dataModel.getTables().stream().filter(table -> table.maps(domainClass)).findAny().get();
            }
            catch (IllegalArgumentException e) {
                // DataModel throws IllegalArgumentException when domainClass is not mapped by any of its Tables
                return false;
            }
            if (this.table.getPrimaryKeyColumns().size() > 1) {
                throw new IllegalArgumentException(ReferenceValueFactory.class.getSimpleName() + " does not support persistent entities with multi valued primary keys");
            }
            this.primaryKeyColumn = this.table.getPrimaryKeyColumns().get(0);
            this.primaryKeyType = this.getPropertyType(domainClass, this.primaryKeyColumn.getFieldName());
            this.initConverterAndPrimaryKeyCheckerIfSupported();
            return true;
        }

        private void initConverterAndPrimaryKeyCheckerIfSupported() {
            switch (this.primaryKeyType.getName()) {
                case "java.lang.Long": {
                    this.converter = new LongConverter();
                    this.primaryKeyChecker = new LongPrimaryKeyChecker();
                    break;
                }
                case "long": {
                    this.converter = new LongConverter();
                    this.primaryKeyChecker = new LongPrimaryKeyChecker();
                    break;
                }
                case "java.lang.Integer": {
                    this.converter = new IntegerConverter();
                    this.primaryKeyChecker = new IntegerPrimaryKeyChecker();
                    break;
                }
                case "int": {
                    this.converter = new IntegerConverter();
                    this.primaryKeyChecker = new IntegerPrimaryKeyChecker();
                    break;
                }
                case "java.lang.String": {
                    this.converter = new NoConversion();
                    this.primaryKeyChecker = new StringPrimaryKeyChecker();
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Primary key column type " + this.primaryKeyType.getName() + " is currently not supported by " + ReferenceValueFactory.class.getSimpleName());
                }
            }
        }

        private Class<?> getPropertyType(Class<?> clazz, String fieldName) {
            return beanService.getPropertyType(clazz, fieldName);
        }

        public T fromStringValue(String stringValue) {
            if (Checks.is(stringValue).emptyOrOnlyWhiteSpace()) {
                return null;
            }
            Object primaryKey = this.converter.fromString(stringValue);
            return this.valueFromDatabase(primaryKey);
        }

        public String toStringValue(T object) {
            return String.valueOf(this.valueToDatabase(object));
        }

        public T valueFromDatabase(Object value) {
            return this.dataMapper.getOptional(value).orElse(null);
        }

        public Object valueToDatabase(T object) {
            return this.primaryKeyColumn.getDatabaseValue(object);
        }

        public boolean isPersistent(T object) {
            Object primaryKey = this.valueToDatabase(object);
            return !this.primaryKeyChecker.indicatesNull(primaryKey);
        }

        public void bind(PreparedStatement statement, int offset, T value) throws SQLException {
            statement.setObject(offset, valueToDatabase(value));
        }

        public void bind(SqlBuilder builder, T value) {
            builder.addObject(valueToDatabase(value));
        }
    }

}