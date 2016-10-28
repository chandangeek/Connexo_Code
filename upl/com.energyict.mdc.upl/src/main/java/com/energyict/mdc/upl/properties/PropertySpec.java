package com.energyict.mdc.upl.properties;

import aQute.bnd.annotation.ConsumerType;

/**
 * Models the specification of a dynamic property of a protocol.
 * A property has a name that uniquely identifies it and
 * a displayName that makes it human readable.
 * A PropertySpec is capable of validating a value,
 * likely against a type that is internally managed/known.
 * When a PropertySpec is "required", a value will need to be provided.
 * When a value is invalid, a
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-10-28 (14:50)
 */
@ConsumerType
public interface PropertySpec {

    /**
     * Gets the name of this PropertySpec.
     *
     * @return The name
     */
    String getName();

    /**
     * Gets the human readable name of this PropertySpec
     * that could be used in UI screens.
     *
     * @return The display name
     */
    String getDisplayName();

    /**
     * A human readable description of this PropertySpec.
     * Likely explaining what the property is all about.
     * For measurement like values, this may also include
     * hard coded units.
     *
     * @return The human readable description
     */
    String getDescription();

    /**
     * Tests if a value will be required for this PropertySpec.
     *
     * @return true iff a value will be required
     */
    boolean isRequired();

    /**
     * Validates the specified value against this PropertySpec.
     * Examples of invalid values are:
     * <ul>
     * <li><code>null</code> for a required attribute, this will effectively produce a {@link PropertyValidationException}</li>
     * <li>"a string" for a numerical attribute, this will effectively produce an {@link InvalidPropertyException}</li>
     * </ul>
     *
     * @param value The value that needs validation
     * @return <code>true</code> iff the value is valid, all other cases will throw an InvalidValueException
     * @throws PropertyValidationException Thrown if the value is not valid for this attribute specification.
     */
    boolean validateValue(Object value) throws PropertyValidationException;

}