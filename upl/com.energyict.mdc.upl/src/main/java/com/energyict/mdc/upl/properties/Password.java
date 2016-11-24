package com.energyict.mdc.upl.properties;

/**
 * Models a password as a simple String.
 * Note that the implementation classes are not required
 * to contain the actual password but are allowed
 * to contact other services to compute the actual
 * password from statefull values.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-24 (16:26)
 */
public interface Password {
    String getValue();
}