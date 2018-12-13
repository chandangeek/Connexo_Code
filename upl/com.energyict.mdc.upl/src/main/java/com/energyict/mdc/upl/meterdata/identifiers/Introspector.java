package com.energyict.mdc.upl.meterdata.identifiers;

import java.util.Set;

/**
 * Introspects identifier type information.
 * This was designed with a maximum backwards/forwards compatibility
 * in mind and therefore NO enumerations were introduced but String is used instead.
 * With enum classes, each addition of another type would require a new release
 * of the universal protocol bundle.
 * <br>
 * The name of the identifier type is exposed as well as the composing parts of the identifier.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-14 (10:47)
 */
public interface Introspector {

    /**
     * Gets the name of the identifier type that is under inspection.
     *
     * @return The name of the identifier type
     */
    String getTypeName();

    /**
     * Gets the different role that are supported by this Introspector.
     *
     * @return The Set of roles
     * @see #getValue(String)
     */
    Set<String> getRoles();

    /**
     * Gets the value of the part of the identifier that matches the specified role.
     *
     * @param role The role of the identifier part
     * @return The value
     * @throws IllegalArgumentException Thrown when the role is not supported by the identifier that is being introspected
     */
    Object getValue(String role);

    /**
     * Tests if the values of the roles of this and the other Introspector are equals.
     *
     * @param other The other Introspector
     * @param roles The roles
     * @return <code>true</code> iff the values of all the roles of this and the other Introspector are equals
     */
    default boolean roleEqualsTo(Introspector other, Set<String> roles) {
        try {
            return roles.stream().allMatch(role -> this.getValue(role).equals(other.getValue(role)));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

}