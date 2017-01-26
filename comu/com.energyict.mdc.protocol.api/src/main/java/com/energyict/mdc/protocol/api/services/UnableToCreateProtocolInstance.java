package com.energyict.mdc.protocol.api.services;

/**
 * Models the exceptional situation that occurs when the creation of an instance
 * of some protocol related class could not be created due to some exception
 * reported by the java reflection layer.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-24 (10:33)
 */
public class UnableToCreateProtocolInstance extends RuntimeException {

    public UnableToCreateProtocolInstance(Throwable cause, String javaClassName) {
        super("Unable to create instance for protocol related class " + javaClassName, cause);
    }

}