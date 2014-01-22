package com.energyict.mdc.protocol.pluggable.impl;

/**
 * Simple POJO class that models the fact that a {@link com.energyict.mdc.pluggable.PluggableClass}
 * is using a {@link com.energyict.mdc.dynamic.relation.RelationAttributeType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-23 (17:06)
 */
public class PluggableClassRelationAttributeTypeUsage {
    public long pluggableClassId;
    public long relationAttributeTypeId;

    // For ORMService only
    public PluggableClassRelationAttributeTypeUsage() {
        super();
    }

    public PluggableClassRelationAttributeTypeUsage(long pluggableClassId, long relationAttributeTypeId) {
        super();
        this.pluggableClassId = pluggableClassId;
        this.relationAttributeTypeId = relationAttributeTypeId;
    }

}