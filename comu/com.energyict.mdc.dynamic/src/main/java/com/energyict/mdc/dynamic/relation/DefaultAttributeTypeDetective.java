package com.energyict.mdc.dynamic.relation;

/**
 * Models the interface of a component that is capable of figuring out
 * if a {@link RelationAttributeType} serves as the default attribute
 * for at least one of the objects managed by the bundle that provides that component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-07-04 (13:24)
 */
public interface DefaultAttributeTypeDetective {

    /**
     * Tests if the specified {@link RelationAttributeType} serves
     * as the default attribute for at least one of
     * the objects managed by the bundle that implements this interface.
     *
     * @param attributeType The RelationAttributeType
     * @return <code>true</code> iff at least one of the managed objects uses
     *         the RelationAttributeType as a default attribute
     */
    public boolean isDefaultAttribute (RelationAttributeType attributeType);

}