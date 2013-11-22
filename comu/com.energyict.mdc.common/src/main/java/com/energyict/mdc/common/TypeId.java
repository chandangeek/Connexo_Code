package com.energyict.mdc.common;

import java.io.Externalizable;

/**
 * Uniquely identifies a type.
 *
 * @author alex
 */
public interface TypeId extends Externalizable {

    /**
     * Returns the factory responsible for managing types identified by this type ID.
     *
     * @return The BusinessObjectFactory
     */
    public BusinessObjectFactory getFactory ();

    /**
     * Returns true if this TypeId is in fact a meta type,
     * i.e. the returned factory in it's turn produces other factories.
     *
     * @return True if the type is a meta type, false if not.
     */
    public boolean isMetaType ();

    /**
     * Returns the type name.
     * This is an identifier (the classname) of the type identified by this type identifier.
     *
     * @return The type name of the type.
     */
    public String getTypeName ();

    public int getSubTypeId ();

}