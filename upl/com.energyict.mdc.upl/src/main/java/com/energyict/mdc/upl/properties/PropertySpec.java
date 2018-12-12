package com.energyict.mdc.upl.properties;

import aQute.bnd.annotation.ConsumerType;

import java.util.Optional;

/**
 * Models the specification of a dynamic property of a protocol.
 * A property has a name that uniquely identifies it and
 * a displayName that makes it human readable.
 * A PropertySpec is capable of validating a value,
 * likely against a type that is internally managed/known.
 * When a PropertySpec is "required", a value will need to be provided.
 * When a value is invalid, an exception from the {@link PropertyValidationException}
 * class hierarchy is thrown.
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
     * @return true if a value will be required
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

    /**
     * @return the Default value defined for this PropertySpec
     */
    Optional<?> getDefaultValue();

    /**
     * Provides a list of possible values for the PropertySpec. Every value should only occur once.
     * If a default value is applicable for this PropertySpec, then it should also be included in this list.
     * This list can be empty.
     *
     * @return the list of possible values for this PropertySpec
     */
    PropertySpecPossibleValues getPossibleValues();

    /**
     * Tests if this PropertySpec has support for
     * multiple values at the same time, i.e. client
     * code can specify a Collection of its value type
     * instead of a single value.
     * The class returning a PropertySpec that has
     * support for multi values should be prepared
     * to accept both Collection&lt;ValueType&gt;
     * and ValueType when receiving values for that PropertySpec.
     *
     * @return A flag that indicates if this PropertySpec supports multi values
     */
    boolean supportsMultiValues();

    /**
     * Gets the {@link ValueFactory} that will support persistency of property values.
     *
     * @return The ValueFactory
     */
    ValueFactory getValueFactory();

}