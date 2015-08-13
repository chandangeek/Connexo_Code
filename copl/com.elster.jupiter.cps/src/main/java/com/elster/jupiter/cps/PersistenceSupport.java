package com.elster.jupiter.cps;

import aQute.bnd.annotation.ConsumerType;
import com.elster.jupiter.orm.Table;
import com.google.inject.Module;

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
     * Gets the name of the table that will hold the persistent properties.
     *
     * @return The name of the database table
     */
    String tableName();

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
    Class<T> getPersistenceClass();

    /**
     * Gets the Module that will provide additional bindings
     * that are required by the persistence class.
     * Note that the DataModel is provided by the CustomPropertySetService
     * but will be available for injection in the persistence class.
     *
     * @return The Module
     */
    Optional<Module> getModule();

    /**
     * Adds the columns for each of the custom properties
     * using the various builders that are provided by the Table.
     * Note that primary key is generated for you.
     * Attempting to add another one will produce a error.
     * Feel free to add as many foreign keys, uniqueness constraints
     * and/or indexes as you want or need.
     *
     * @param table The Table
     * @see Table#column(String)
     * @see Table#foreignKey(String)
     * @see Table#unique(String)
     * @see Table#index(String)
     */
    void addCustomPropertyColumnsTo(Table table);

}