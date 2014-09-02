package com.energyict.mdc.common;

import java.io.Serializable;

/**
 * Instances of BusinessObjectFactory implementors are used to create and
 * find BusinessObjects. BusinessObjectFactory also provides a meta data interface for its
 * corresponding BusinessObject type. The meta data interface is not a part of the EIServer API,
 * and may be changed in future releases without notice
 *
 * @author Joost
 */
public interface BusinessObjectFactory<S extends BusinessObject> {

    /**
     * Returns the object type represented by this factory,
     * either the main interface implemented (minus 'Factory')
     * or the class name if no interfaces are implemented
     *
     * @return the type name
     */
    public String getType();

    public S findByPrimaryKey(Serializable key);

    public S findByHandle(byte[] handle);

    /**
     * Returns the id that uniquely identifies this factory or object type.
     * The management of all factory ids is done in the MeteringWarehouse
     *
     * @return the id value
     */
    int getId();

    /**
     * Returns true if the factory is a meta type factory or not. This will return true when the BusinessObjects returned by the factory are also
     * factories themselves for other types. This is the case for VirtualMeterTypeFactory and FolderTypeFactory. The FolderType and VirtualMeterType
     * are both BusinessObjects and factories for other objects types, and are the product of a meta type factory.
     *
     * @return True if the factory is a meta type factory, false if it is not.
     */
    boolean isMetaTypeFactory();

}