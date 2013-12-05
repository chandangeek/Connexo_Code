package com.energyict.mdc.protocol;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.NamedPropertyBusinessObject;
import com.energyict.mdc.protocol.dynamic.HasDynamicProperties;

/**
 * Registers a java class that implements some kind of {@link Pluggable} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-27 (14:56)
 */
public interface PluggableClass<T extends Pluggable> extends NamedPropertyBusinessObject, HasDynamicProperties {

    public PluggableClassType getPluggableClassType ();

    /**
     * Returns the name of the java implementation class.
     *
     * @return the java class name
     */
    public String getJavaClassName ();

    /**
     * Returns the pluggable version of the {@link Pluggable} class.
     *
     * @return the version or null if the java class could not be instantiated
     * @see Pluggable#getVersion()
     */
    public String getVersion ();

    /**
     * Creates a new instances of this pluggable class.
     *
     * @return an instance of the pluggable class.
     * @throws BusinessException if a business error occured.
     */
    public T newInstance () throws BusinessException;

    /**
     * Creates a new instances of this pluggable class
     * but without any of the properties that have
     * been defined.
     *
     * @return an instance of the pluggable class.
     * @throws BusinessException if a business error occured.
     */
    public T newInstanceWithoutProperties () throws BusinessException;

}