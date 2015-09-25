package com.elster.jupiter.properties;


/**
 * Models the specification of a dynamic property of an Object.
 * A property has a name and a type and is capable of returning
 * a {@link ValueFactory} which will support persistency of property values.
 * When a PropertySpec is "required", a value will need to be provided.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-27 (13:50)
 */
public interface PropertySpec {

    /**
     * Gets the name of this PropertySpec.
     *
     * @return The name
     */
    String getName();

    /**
     * Gets the human readable name of this PropertySpec
     * that could be used in UI screens. Note that
     * ìmplementation classes should consider using
     * a Thesaurus to translate e.g. the name to all the
     * language that are supported by the Connexo platform.
     *
     * Todo: drop default as part of COPL-1151
     * @return The display name
     */
    default String getDisplayName() {
        return getName();
    }

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
     * Gets the {@link ValueFactory} that will support persistency of property values.
     *
     * @return The ValueFactory
     */
   ValueFactory getValueFactory ();

    /**
     * Tests if a value will be required for this PropertySpec.
     *
     * @return true iff a value will be required
     */
   boolean isRequired ();

    /**
     * Tests if a value for this PropertySpec is actually
     * a reference to a persistent business object.
     *
     * @return true iff values of this PropertySpec are persistent business objects
     */
   boolean isReference ();

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
     * Note that {@link ValueRequiredException} will be thrown for <code>null</code>
     * and required attributes.
     */
   boolean validateValue (Object value) throws InvalidValueException;

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
   boolean validateValueIgnoreRequired (Object value) throws InvalidValueException;

    /**
     * Get the {@link PropertySpecPossibleValues possible values}
     * for this PropertySpec or <code>null</code> if no such
     * list can be constructed.
     *
     * @return The PropertySpecPossibleValues
     */
   PropertySpecPossibleValues getPossibleValues ();

}