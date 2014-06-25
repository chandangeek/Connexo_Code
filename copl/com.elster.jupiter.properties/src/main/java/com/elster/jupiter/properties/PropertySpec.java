package com.elster.jupiter.properties;


/**
 * Models the specification of a dynamic property of an Object.
 * A property has a name and a type and is capable of returning
 * a {@link ValueFactory} which will support persistency of property values.
 * When a PropertySpec is "required", a value will need to be provided.
 *
 * @param <T> The type of the property that is modelled by this PropertySpec
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-27 (13:50)
 */
public interface PropertySpec<T> {

    /**
     * Gets the name of this PropertySpec.
     *
     * @return The name
     */
    public String getName ();

    /**
     * Gets the {@link ValueFactory} that will support persistency of property values.
     *
     * @return The ValueFactory
     */
    public ValueFactory<T> getValueFactory ();

    /**
     * Tests if a value will be required for this PropertySpec.
     *
     * @return true iff a value will be required
     */
    public boolean isRequired ();

    /**
     * Tests if a value for this PropertySpec is actually
     * a reference to {@link com.energyict.mdc.common.BusinessObject}.
     *
     * @return true iff values of this PropertySpec are BusinessObjects
     */
    public boolean isReference ();

    /**
     * Validates the specified value against this PropertySpec.
     * Examples of invalid values are:
     * <ul>
     * <li><code>null</code> for a required attribute</li>
     * <li>"a string" for a numerical attribute</li>
     * </ul>
     *
     * @param value The value that needs validation
     * @return <code>true</code> iff the value is valid, all other cases will throw an InvalidValueException
     * @throws InvalidValueException Thrown if the value is not valid for this attribute specification.
     * Note that {@link com.elster.jupiter.properties.common.ValueRequiredException} will be thrown for <code>null</code>
     * and required attributes.
     */
    public boolean validateValue (T value) throws InvalidValueException;

    /**
     * Validates the specified value against this PropertySpec,
     * ignoring the "required" aspect of this PropertySpec.
     * Examples of invalid values are:
     * <ul>
     * <li>"a string" for a numerical attribute</li>
     * </ul>
     *
     * @param value The value that needs validation
     * @return <code>true</code> iff the value is valid, all other cases will throw an InvalidValueException
     * @throws InvalidValueException Thrown if the value is not valid for this attribute specification.
     */
    public boolean validateValueIgnoreRequired (T value) throws InvalidValueException;

    /**
     * Get the {@link PropertySpecPossibleValues possible values}
     * for this PropertySpec or <code>null</code> if no such
     * list can be constructed.
     *
     * @return The PropertySpecPossibleValues
     */
    public PropertySpecPossibleValues<T> getPossibleValues ();

}