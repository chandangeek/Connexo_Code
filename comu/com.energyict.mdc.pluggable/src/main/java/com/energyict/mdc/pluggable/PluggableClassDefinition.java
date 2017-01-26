package com.energyict.mdc.pluggable;

/**
 * Holds information to define a pluggable class.
 */
public interface PluggableClassDefinition<T> {

    /**
     * Returns the type's name
     * @return type name
     */
    String getName();

    /**
     * Returns the java type's class
     * @return the java type's class
     */
    Class<? extends T> getProtocolTypeClass();

}