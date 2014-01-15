package com.energyict.mdc.protocol.api.legacy.dynamic;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.IdBusinessObjectFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Provides all the different supported ways to select a value for
 * attributes that are not simple in nature. Such attributes are
 * references to other objects or attributes that are backed
 * by some kind of lookup mechanism.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-07-03 (09:01)
 */
public enum AttributeValueSelectionMode {
    /**
     * Represents <code>null</code> meaning that no selection
     * mechanism is to be used.
     */
    UNSPECIFIED(0),

    /**
     * Represents the AttributeValueSelectionMode that will
     * use a combobox mechanism to allow users to select a value
     * for an attribute.
     * Note that this will query all available values for the attribute
     * type from the database as all of those will be added to the combobox.
     */
    COMBOBOX(1),

    /**
     * Represents the AttributeValueSelectionMode that will
     * use a "search and select" mechanism to allow users to
     * search for an appropriate value first with all of the supported
     * match conditions for the attribute type and then
     * finally select a value.
     */
    SEARCH_AND_SELECT(2),

    /**
     * Represents the AttributeValueSelectionMode that will
     * use a list mechanism to allow users to select a value
     * for an attribute.
     * Note that this will query all available values for the attribute
     * type from the database as all of those will be added to the combobox.
     */
    LIST(3);

    private int code = 0;

    AttributeValueSelectionMode (int code) {
        this.code = code;
    }

    public int getCode () {
        return code;
    }

    /**
     * Returns a value that can be used to store this AttributeValueSelectionMode
     * in a database.
     *
     * @return The value that uniquely identifies this AttributeValueSelectionMode in a database
     */
    public int dbValue () {
        return this.getCode();
    }

    /**
     * Returns the AttributeValueSelectionMode with the specified database value.
     *
     * @param dbValue The value that was stored previously in a database
     * @return The AttributeValueSelectionMode
     */
    public static AttributeValueSelectionMode fromDb (int dbValue) {
        for (AttributeValueSelectionMode attributeValueSelectionMode : values()) {
            if (dbValue == attributeValueSelectionMode.dbValue()) {
                return attributeValueSelectionMode;
            }
        }
        throw new ApplicationException("Unknown or unsupported AttributeValueSelectionMode: " + dbValue);
    }

    /**
     * Gets all available AttributeValueSelectionModes.
     *
     * @return All the AttributeValueSelectionModes
     */
    public static List<AttributeValueSelectionMode> getAll () {
        return Arrays.asList(values());
    }

    /**
     * Gets all the AttributeValueSelectionModes that are applicable
     * to reference attribute types.
     *
     * @return All the AttributeValueSelectionModes for reference attribute types
     */
    public static List<AttributeValueSelectionMode> getReferenceSelectionModes () {
        return Arrays.asList(COMBOBOX, SEARCH_AND_SELECT, LIST);
    }

    /**
     * Gets all the AttributeValueSelectionModes that are applicable
     * to reference attribute types that relate to the specified {@link IdBusinessObjectFactory}.
     *
     * @param factory The IdBusinessObjectFactory
     * @return All the AttributeValueSelectionModes for reference attribute types
     */
    public static List<AttributeValueSelectionMode> getReferenceSelectionModes (IdBusinessObjectFactory factory) {
        List<AttributeValueSelectionMode> result = new ArrayList<>();
        result.add(COMBOBOX);
        result.add(LIST);
        return result;
    }

    /**
     * Gets all the AttributeValueSelectionModes that are applicable
     * to attribute types that are backed by some kind of lookup mechanism.
     *
     * @return All the AttributeValueSelectionModes for attribute types backed by lookup mechanisms
     */
    public static List<AttributeValueSelectionMode> getLookupSelectionModes () {
        return Arrays.asList(COMBOBOX, LIST);
    }

}