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

    Class<T> getValueType();

    boolean isReference();

    String getDatabaseTypeName();

    int getJdbcType();

    T valueFromDatabase(Object object);

    Object valueToDatabase(T object);

    void bind(PreparedStatement statement, int offset, T value) throws SQLException;

    void bind(SqlBuilder builder, T value);

    int getObjectFactoryId();

    /**
     * Test if the specified value is persistent.
     *
     * @param value The value
     * @return A flag that indicates if the specified value is persistent
     */
    boolean isPersistent(T value);

}