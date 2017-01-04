package com.energyict.mdc.upl.security;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Holds the value of a single security property for a Device.
 * The complete list of security properties is determined
 * by the {@link SecurityPropertySet}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-04 (10:55)
 */
public interface SecurityProperty {

    /**
     * Gets the name of this property as defined by its specification.
     *
     * @return The name of this property
     */
    String getName();

    /**
     * Gets the value of this property.
     *
     * @return The value
     */
    Object getValue();

    /**
     * Gets the {@link SecurityPropertySet} that defines the context
     * and also the definition of the other available security properties.
     *
     * @return The SecurityPropertySet
     */
    @XmlAttribute
    SecurityPropertySet getSecurityPropertySet();

}