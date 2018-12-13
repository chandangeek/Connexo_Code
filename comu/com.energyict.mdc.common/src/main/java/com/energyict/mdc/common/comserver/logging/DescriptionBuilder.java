/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.comserver.logging;

/**
 * Assists in building descriptions for objects.
 * Supports single and multi-value properties and has
 * special support for boolean properties that merely
 * require a simple label to indicate that the flag is set.<br>
 * The properties are added to the description in the order
 * in which they are requested from this builder.<br>
 * Implementation classes should make sure that properties
 * and values of multi-value properties are clearly distinguishable
 * by e.g. using different separators.<br>
 * Example of how a Person object that has the following attributes
 * might be described:
 * <ul>
 * <li>Male/female: boolean flag, printed as Male/Female</li>
 * <li>Name: simple String</li>
 * <li>Hobbies: List of Strings</li>
 * </ul>
 * Person {Male; Name: John Doe; Hobbies: Watching tv, fishing, curling}
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-09-25 (09:06)
 */
public interface DescriptionBuilder {

    /**
     * Adds a simple label to the description.
     *
     * @param label The label
     */
    public void addLabel (String label);

    /**
     * Returns a StringBuilder that allows building the contents
     * of the property with the specified name.
     *
     * @param propertyName The name of the property
     * @return The StringBuilder that will build the value of the property
     */
    public StringBuilder addProperty (String propertyName);

    /**
     * Adds the description of the property with the specified name
     * from a format pattern and a number of parameters
     * that will be passed to the MessageFormat class.
     *
     * @param propertyName The name of the property
     * @param propertyFormatPattern The pattern of how the property description should look
     * @param propertyValueParameters The parameters of the pattern that will be injected in the pattern by the MessageFormat class
     */
    public void addFormattedProperty (String propertyName, String propertyFormatPattern, Object... propertyValueParameters);

    /**
     * Returns a {@link PropertyDescriptionBuilder} that allows building the contents
     * of the multi-value property with the specified name.
     *
     * @param propertyName The name of the property
     * @return The PropertyDescriptionBuilder that will build the value of the property
     */
    public PropertyDescriptionBuilder addListProperty (String propertyName);

}