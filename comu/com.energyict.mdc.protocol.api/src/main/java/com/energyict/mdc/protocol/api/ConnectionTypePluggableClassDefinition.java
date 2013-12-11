package com.energyict.mdc.protocol.api;

/**
 * Holds information to define a connection type pluggable class
 */
public interface ConnectionTypePluggableClassDefinition {

    /**
     * Returns the connection type's name
     * @return connection type name
     */
    public String getName();

    /**
     * Returns the connection type's java class
     * @return the connection type's java class
     */
    public Class<? extends ConnectionType> getConnectionTypeClass();

}
