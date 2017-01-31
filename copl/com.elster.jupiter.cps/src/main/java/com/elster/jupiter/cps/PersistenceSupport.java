/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cps;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;

import aQute.bnd.annotation.ConsumerType;
import com.google.inject.Module;

import java.util.List;
import java.util.Optional;

/**
 * Models the persistence aspects of a {@link CustomPropertySet}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-08-11 (10:18)
 */
@ConsumerType
public interface PersistenceSupport<D, T extends PersistentDomainExtension<D>> {

    /**
     * Unique identifier for the {@link DataModel} that will be created
     * for this CustomPropertySet. The String should be exactly 3 chars in length.
     *
     * @return The unique DataModel identifier
     */
    String componentName();

    /**
     * Gets the name of the table that will hold the persistent properties.
     *
     * @return The name of the database table
     */
    String tableName();

    /**
     * Gets the name of the table that will hold the journal of the persistent properties.
     *
     * @return The name of the database table
     */
    default String journalTableName() {
        return tableName() + "JRNL";
    }

    /**
     * Gets the name of the column that will hold the reference
     * to the domain class is being extended by the CustomPropertySet.
     *
     * @return The name of the column
     */
    default String domainColumnName() {
        return domainFieldName();
    }

    /**
     * Gets the name of the field of the persistence class
     * that holds the reference to the domain class
     * that is being extended by the CustomPropertySet.
     *
     * @return The name of the field of the persistence class
     */
    String domainFieldName();

    /**
     * Gets the name of the foreign key that references the
     * domain class that is being extended by the CustomPropertySet.
     *
     * @return The name of the foreign key
     */
    String domainForeignKeyName();

    /**
     * Gets the class that provides persistence support for the attributes
     * and in addition it uses the javax.validation framework for any
     * business constraints that relate to one or some of the attributes.
     *
     * @return The persistence class
     */
    Class<T> persistenceClass();

    /**
     * Gets the Module that will provide additional bindings
     * that are required by the persistence class.
     * Note that the DataModel is provided by the CustomPropertySetService
     * but will be available for injection in the persistence class.
     *
     * @return The Module
     */
    Optional<Module> module();

    /**
     * Adds and returns the columns for each of the custom properties
     * that should be part of the primary key or an empty List.
     * Use the various builders that are provided by the Table.
     * Note that is part of the generation of the primary key.
     * Attempting to start the builder for the primary key will produce a error.
     * Note also that this is not the place to add foreign keys,
     * uniqueness constraints and/or indexes.
     * <p>
     * Because we are not expecting this to be used very frequently,
     * this has been defined as a default method for your convenience.
     *
     * @param table The Table
     * @return The columns that need to be part of the primary key
     * @see Table#column(String)
     * @see Table#foreignKey(String)
     * @see Table#unique(String)
     * @see Table#index(String)
     */
    List<Column> addCustomPropertyPrimaryKeyColumnsTo(Table table);

    /**
     * Adds the columns for each of the custom properties
     * using the various builders that are provided by the Table.
     * Note that primary key is generated for you.
     * Attempting to add another primary key one will produce a error.
     * Feel free to add as many foreign keys, uniqueness constraints
     * and/or indexes as you want or need.
     * The List of primary key columns that was returned before
     * is passed back to allow for stateless implementation classes.
     *
     * @param table The Table
     * @param customPrimaryKeyColumns The List of primary key columns previously returned by this component
     * @see Table#column(String)
     * @see Table#foreignKey(String)
     * @see Table#unique(String)
     * @see Table#index(String)
     */
    void addCustomPropertyColumnsTo(Table table, List<Column> customPrimaryKeyColumns);

    /**
     * Gets the name of the column that holds the values of the specified {@link PropertySpec}.
     * Implementation classes are allowed to throw an IllegalArgumentException if the
     * PropertySpec is not one that was returned by {@link CustomPropertySet#getPropertySpecs()}
     * of the related CustomPropertySet.
     *
     * @param propertySpec The PropertySpec
     * @return The name of the column
     */
    default String columnNameFor(PropertySpec propertySpec) {
        return propertySpec.getName();
    }

    String application();

}