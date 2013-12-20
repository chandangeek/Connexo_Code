package com.energyict.mdc.dynamic;

import com.energyict.mdc.common.SqlBuilder;

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

    public String getDatabaseTypeName ();

    public int getJdbcType ();

    public T valueFromDatabase (Object object) throws SQLException;

    public Object valueToDatabase (T object);

    public void bind (PreparedStatement statement, int offset, T value) throws SQLException;

    public void bind (SqlBuilder builder, T value);

    public T fromStringValue (String stringValue);

    public String toStringValue (T object);

    public Class<T> getValueType ();

    public String getStructType();

    public int getObjectFactoryId ();

    public boolean isReference ();

    /**
     * Test if the specified value is persistent.
     *
     * @param value The value
     * @return A flag that indicates if the specified value is persistent
     */
    public boolean isPersistent (T value);

    public boolean requiresIndex ();

    public String getIndexType ();

}