package com.energyict.mdc.common;

import java.sql.SQLException;

/**
 * Represents a business object with a name.
 * The name is not necessarily unique in the system.
 * Most often the name is unique within a given context,
 * e.g Device names are unique within a folder.
 */
public interface NamedBusinessObject extends IdBusinessObject {

    /**
     * Returns the object's name
     *
     * @return the name
     */
    public String getName();

    /**
     * Returns the object's external name
     * An external name should be unique within the object's type
     *
     * @return the external name
     */
    public String getExternalName();

    /**
     * Renames the object
     *
     * @param name the object's new name
     * @throws BusinessException if a business exception occured,
     *                           typically because the new name is not unique within a certain context
     * @throws SQLException      if a database error occured
     */
    public void rename(String name) throws BusinessException, SQLException;

}
