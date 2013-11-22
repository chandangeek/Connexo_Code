package com.energyict.mdc.common;

import java.io.Serializable;
import java.util.List;

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
     * Find all the BusinessObject instances in the application
     *
     * @return a <CODE>List</CODE> of <CODE>BusinessObject</CODE> objects in undefined order
     */
    public List<S> findAll();

    /**
     * gets the shadow class of the BusinessObjects created by this factory
     *
     * @return the shadow class
     */
    public Class getShadowClass();

    /**
     * gets the PropertiesMetaData
     *
     * @return a PropertiesMetaData object
     */
    public PropertiesMetaData getPropertiesMetaData();

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
     * Returns the sub type factory if that's applicable.
     *
     * @return The sub type factory if that's applicable.
     */
    BusinessObjectFactory getSubtypeFactory();

    /**
     * Returns the id that uniquely identifies this factory or object type.
     * The management of all factory ids is done in the MeteringWarehouse
     *
     * @return the id value
     */
    int getId();

    /**
     * Returns the type ID of the business objects it manages.
     *
     * @return The type ID of the business objects it manages.
     */
    TypeId getTargetTypeId();

    /**
     * Returns true if the factory is a meta type factory or not. This will return true when the BusinessObjects returned by the factory are also
     * factories themselves for other types. This is the case for VirtualMeterTypeFactory and FolderTypeFactory. The FolderType and VirtualMeterType
     * are both BusinessObjects and factories for other objects types, and are the product of a meta type factory.
     *
     * @return True if the factory is a meta type factory, false if it is not.
     */
    boolean isMetaTypeFactory();

}