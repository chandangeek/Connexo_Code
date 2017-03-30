/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties;

import com.elster.jupiter.util.sql.SqlBuilder;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Models the behavior of a component that
 * will provide persistency services for dynamic properties.
 *
 * @param <T> The type of values that are supported by this factory
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-27 (14:03)
 */
public interface ValueFactory<T> {

    T fromStringValue(String stringValue);

    String toStringValue(T object);

    default boolean isReference() {
        return false;
    }

    Class<T> getValueType();

    T valueFromDatabase(Object object);

    Object valueToDatabase(T object);

    void bind(PreparedStatement statement, int offset, T value) throws SQLException;

    void bind(SqlBuilder builder, T value);

    /**
     * Tests if the specified value represents the <code>null</code> value for this ValueFactory.
     *
     * @param value The value
     * @return A flag that indicates if the value represents the <code>null</code> value
     */
    default boolean isNull(T value) {
        return value == null;
    }

    /**
     * Tests if the specified value is a valid value for this ValueFactory.
     * Factories for simple data types will likely have no additional stuff
     * to validate because the value is guaranteed not to be null.
     * Factories for complex data types could e.g. check that the
     * value is persisted in the database.
     *
     * @param value The non-null value
     * @return A flag that indicates if the value is valid or not
     */
    default boolean isValid(T value) {
        return true;
    }

}