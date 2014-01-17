package com.energyict.mdc.dynamic;

/**
 * Provides building services for {@link PropertySpec}s.
 * All methods that contribute to aspects of the PropertySpec
 * under construction will return the same PropertySpecBuilder
 * to support method chaning.
 * Finally, the client code will call the finish method when all
 * aspects of the PropertySpec have been specified.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-17 (13:13)
 */
public interface PropertySpecBuilder<T> {

    /**
     * Sets the name of the {@link PropertySpec} that is being constructed.
     *
     * @param specName The name of the PropertySpec
     * @return This PropertySpecBuilder to support method chaining while constructing
     */
    public PropertySpecBuilder<T> name(String specName);

    /**
     * Sets a default value for the {@link PropertySpec} under construction.
     * Setting a default value implies that the default value
     * will become one of the possible values of the PropertySpec.
     * Note that there is only one default and calling this method
     * a second time will overrule the previous default.
     * The previous value will remain in the list of possible values though.
     *
     * @param defaultValue The default value
     * @return This PropertySpecBuilder to support method chaining while constructing
     */
    public PropertySpecBuilder<T> setDefaultValue (T defaultValue);

    /**
     * Marks the list of possible values of the {@link PropertySpec}
     * under construction as an exhaustive list.
     *
     * @return This PropertySpecBuilder to support method chaining while constructing
     */
    public PropertySpecBuilder<T> markExhaustive();

    /**
     * Marks the {@link PropertySpec} that is under construction as required.
     * The resulting PropertySpec will therefore return <code>true</code>
     * for the {@link PropertySpec#isRequired()} method.
     *
     * @return This PropertySpecBuilder to support method chaining while constructing
     */
    public PropertySpecBuilder<T> markRequired();

    /**
     * Adds the specified values to the PropertySpec under construction.
     *
     * @param values The possible values
     * @return This PropertySpecBuilder to support method chaining while constructing
     */
    public PropertySpecBuilder<T> addValues (T... values);

    /**
     * Finishes the building process and returns the
     * {@link PropertySpec} as it was constructed so far.
     * Note that this stops the building process and
     * attempts to reuse this building will fail
     * with an {@link IllegalStateException} being thrown.
     *
     * @return The PropertySpec
     */
    public PropertySpec<T> finish();

}