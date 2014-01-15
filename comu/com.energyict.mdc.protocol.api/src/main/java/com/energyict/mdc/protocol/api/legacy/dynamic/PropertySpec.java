package com.energyict.mdc.protocol.api.legacy.dynamic;

import com.energyict.mdc.common.IdBusinessObjectFactory;
import com.energyict.mdc.dynamic.DynamicAttributeOwner;
import com.energyict.mdc.common.InvalidValueException;

/**
 * Models a specification for a dynamic property that can be associated
 * with any type of object. Extracted from the legacy custom properties
 * and from the old ConfigurationSupport which is why the name
 * of the property is alternatively called the key instead of 'name'.
 * <p/>
 * User: jbr
 * Date: 7/05/12
 * Time: 8:59
 */
public interface PropertySpec<T> {

    /**
     * Return the key for this property.
     *
     * @return the key as String
     * @see #getName()
     * @deprecated Use getName instead.
     */
    public String getKey ();

    /**
     * Gets the name of this PropertySpec.
     *
     * @return The name
     */
    public String getName ();

    /**
     * Return the value factory for this property, defining the sql mapping
     *
     * @return a ValueFactory instance
     */
    public ValueFactory<T> getValueFactory ();

    /**
     * Returns the value domain describing the possible content of the field
     *
     * @return a ValueDomain object
     */
    public ValueDomain getDomain ();

    /**
     * Validates the specified value against this PropertySpec.
     * Examples of invalid values are:
     * <ul>
     * <li><code>null</code> for a required attribute</li>
     * <li>"a string" for a numerical attribute</li>
     * <li>&lt;rtu&gt; for a &lt;Folder&gt;</li>
     * </ul>
     *
     * @param value The value that needs validation
     * @param isRequired A flag that indicates if this PropertySpec is assumed to be required
     * @return <code>true</code> iff the value is valid, all other cases will throw an InvalidValueException
     * @throws InvalidValueException Thrown if the value is not valid for this attribute specification.
     * Note that {@link com.energyict.mdc.common.ValueRequiredException} will be thrown for <code>null</code>
     * and required attributes.
     */
    public boolean validateValue (T value, boolean isRequired) throws InvalidValueException;

    /**
     * Get the {@link PropertySpecPossibleValues possible values}
     * for this PropertySpec or <code>null</code> if no such
     * list can be constructed.
     *
     * @return The PropertySpecPossibleValues
     */
    public PropertySpecPossibleValues<T> getPossibleValues ();

    /**
     * Tests if this PropertySpec is actually a specification
     * for a property that references another object.
     *
     * @return <code>true</code> iff this is a specification for
     *         a reference to another object
     */
    public boolean isReference ();

    /**
     * Returns the {@link IdBusinessObjectFactory} that is responsible
     * for the {@link ValueDomain} when this is a reference PropertySpec.
     * Note that this will return <code>null</code> when the PropertySpec
     * is not a specification for a reference to another object.
     *
     * @return The IdBusinessObjectFactory
     * @see #isReference()
     */
    public IdBusinessObjectFactory getObjectFactory ();

    /**
     * Gets the {@link AttributeValueSelectionMode} that will determine
     * how the user will select values for this PropertySpec.
     *
     * @return The AttributeValueSelectionMode
     */
    public AttributeValueSelectionMode getSelectionMode ();


    public Seed getEditorSeed(DynamicAttributeOwner model, boolean required);
}