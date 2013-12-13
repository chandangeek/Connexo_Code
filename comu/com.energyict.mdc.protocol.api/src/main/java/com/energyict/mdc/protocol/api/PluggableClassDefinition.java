package com.energyict.mdc.protocol.api;

/**
 * Holds information to define a pluggable class
 */
public interface PluggableClassDefinition<T extends Pluggable> {

    /**
     * Returns the type's name
     * @return type name
     */
    public String getName();

    /**
     * Returns the java type's class
     * @return the java type's class
     */
    public Class<? extends T> getProtocolTypeClass();

}
