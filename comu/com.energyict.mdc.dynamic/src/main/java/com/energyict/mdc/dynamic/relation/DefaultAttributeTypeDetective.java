package com.energyict.mdc.dynamic.relation;

import com.energyict.mdc.common.ApplicationComponent;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;

/**
 * Models the interface of an {@link ApplicationComponent}
 * that is capable of figuring out if a {@link RelationAttributeType}
 * serves as the default attribute for at least one
 * of the objects it manages.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-07-04 (13:24)
 */
public interface DefaultAttributeTypeDetective {

    /**
     * Tests if the specified {@link RelationAttributeType} serves
     * as the default attribute for at least one of
     * the objects managed by th {@link ApplicationComponent}
     * that implements this interface.
     *
     * @param attributeType The RelationAttributeType
     * @return <code>true</code> iff at least one of the managed objects uses
     *         the RelationAttributeType as a default attribute
     */
    public boolean isDefaultAttribute (RelationAttributeType attributeType);

}