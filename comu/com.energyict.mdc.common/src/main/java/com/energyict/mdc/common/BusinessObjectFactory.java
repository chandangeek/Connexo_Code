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

    /**
     * Returns the id that uniquely identifies this factory or object type.
     * The management of all factory ids is done in the MeteringWarehouse
     *
     * @return the id value
     */
    int getId();

}