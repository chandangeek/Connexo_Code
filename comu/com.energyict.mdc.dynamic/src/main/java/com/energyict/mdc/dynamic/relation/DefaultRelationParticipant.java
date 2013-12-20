package com.energyict.mdc.dynamic.relation;

import java.util.Date;

/**
 * Marker Interface for objects like <code>Device</code>, <code>VirtualMeter</code> who are participants
 * to a default <code>Relation<code>.
 */
public interface DefaultRelationParticipant extends RelationParticipant {

    /**
     * returns the default relation valid at this very moment
     *
     * @return the Relation object
     */
    public Relation getDefaultRelation();

    /**
     * returns the default relation valid on the specified date
     *
     * @param date the date
     * @return the Relation object
     */
    public Relation getDefaultRelation(Date date);

    /**
     * returns the attached default relation attribute type for this object
     *
     * @return the AttributeType
     */
    public RelationAttributeType getDefaultAttributeType();

    /**
     * returns the attached default relation type for this object
     *
     * @return the RelationType object
     */
    public RelationType getDefaultRelationType();

    /**
     * returns the value for the attribute with the given name valid on the specified date
     *
     * @param attributeName the name of the attribute
     * @param date          the date
     * @return the attribute's value
     */
    public Object get(String attributeName, Date date);

    /**
     * returns the value for the attribute with the given name valid on this very moment
     *
     * @param attributeName
     * @return the attribute's value
     */
    public Object get(String attributeName);

    /**
     * returns the value for the specified attribute on the specified date
     *
     * @param attributeType
     * @param date
     * @return the attribute's value
     */
    public Object get(RelationAttributeType attributeType, Date date);

    /**
     * returns the value for the specified attribute on this very moment
     *
     * @param attributeType
     * @return the attribute's value
     */
    public Object get(RelationAttributeType attributeType);
}
