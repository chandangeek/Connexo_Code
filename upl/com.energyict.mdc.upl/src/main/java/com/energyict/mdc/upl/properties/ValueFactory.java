package com.energyict.mdc.upl.properties;

/**
 * Models the behavior of a component that
 * will provide persistency services for dynamic properties.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-12 (14:03)
 */
public interface ValueFactory {

    /**
     * Converts a String value to one of the values that is managed by this factory.
     *
     * @param stringValue The String
     * @return The Object
     */
    Object fromStringValue(String stringValue);

    /**
     * Converts a value managed by this factory to a String value.
     * Calling toStringValue and fromStringValue in sequence
     * should return an Object that is equals to the initial object.
     * <code>
     * <pre>
     *     Object initial = ...;
     *     ValueFactory factory = ...;
     *     Object copy = factory.fromStringValue(factory.tostringValue(initial));
     *     assert copy.equals(initial);
     * </pre>
     * </code>
     *
     * @param object The Object that will be converted to a String value
     * @return The String value
     */
    String toStringValue(Object object);

    /**
     * Gets the name of the value type that is managed by this factory.
     * As an example, a factory that manages BigDecimal values
     * will likely return <code>java.math.BigDecimal</code>.
     *
     * @return The name of the value type
     */
    String getValueTypeName();

    /**
     * Converts the value that is managed by this ValueFactory to
     * an object that can be passed to {@link java.sql.PreparedStatement#setObject(int, Object)}.
     *
     * @param object The Object that is managed by this ValueFactory
     * @return The database representation of the Object managed by this ValueFactory
     */
    Object valueToDatabase(Object object);

    /**
     * Converts a value obtained from the database to a value that is managed by this ValueFactory.
     * You will typically be using this for values that were read with {@link java.sql.ResultSet#getObject(int)}.
     * Calling valueFromDatabase and valueToDatabase in sequence
     * should return an Object that is equals to the initial object.
     * <code>
     * <pre>
     *     Object initial = ...;
     *     ValueFactory factory = ...;
     *     Object copy = factory.valueFromDatabase(factory.valueToDatabase(initial));
     *     assert copy.equals(initial);
     * </pre>
     * </code>
     *
     * @param databaseValue The value that was read from the database
     * @return The Object managed by this ValueFactory
     */
    Object valueFromDatabase(Object databaseValue);

    /**
     * Tests if the specified value represents the <code>null</code> value for this ValueFactory.
     *
     * @param value The value
     * @return A flag that indicates if the value represents the <code>null</code> value
     */
    default boolean isNull(Object value) {
        return value == null;
    }

    /**
     * Tests if the specified value is a valid value for this ValueFactory,
     * i.e. if the value will not cause any SQLExceptions.
     * The ValueFactory is or should not care about <code>null</code>
     * as that is the responsibility of {@link PropertySpec#isRequired()}.
     * Factories that are storing e.g. String values may check the length
     * of the String if the database is imposing length constraints for Strings.
     * Factories for complex data types could e.g. check that the
     * value is already persistent (i.e. it has a valid primary key).
     *
     * @param value The non-null value
     * @return A flag that indicates if the value is valid or not
     */
    default boolean isValid(Object value) {
        return true;
    }

}