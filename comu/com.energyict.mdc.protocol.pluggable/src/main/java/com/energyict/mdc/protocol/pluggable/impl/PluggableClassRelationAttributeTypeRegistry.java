package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.orm.DataMapper;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;
import com.energyict.mdc.dynamic.relation.RelationType;
import com.energyict.mdc.pluggable.PluggableClass;

import java.util.List;

/**
 * Manages the registration of the usage of {@link RelationType}s
 * by {@link PluggableClass}es
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-07-04 (16:19)
 */
public class PluggableClassRelationAttributeTypeRegistry {

    private DataMapper<PluggableClassRelationAttributeTypeUsage> factory;

    public PluggableClassRelationAttributeTypeRegistry(DataMapper<PluggableClassRelationAttributeTypeUsage> factory) {
        super();
        this.factory = factory;
    }

    /**
     * Registers the fact that the specified PluggableClass
     * uses the {@link RelationType}
     * to hold attribute values.
     *
     * @param relationAttributeType The RelationType
     */
    public void register(PluggableClass pluggableClass, RelationAttributeType relationAttributeType) {
        PluggableClassRelationAttributeTypeUsage pcRatUsage = new PluggableClassRelationAttributeTypeUsage(pluggableClass.getId(), relationAttributeType.getId());
        this.factory.persist(pcRatUsage);
    }

    /**
     * Undoes the registration of the fact that the specified PluggableClass
     * uses the {@link RelationType}
     * to hold attribute values.
     *
     * @param relationAttributeType The RelationType
     */
    public void unRegister(PluggableClass pluggableClass, RelationAttributeType relationAttributeType) {
        List<PluggableClassRelationAttributeTypeUsage> pcRatUsages =
                this.factory.find(
                        "pluggableClassId", pluggableClass.getId(),
                        "relationAttributeTypeId", relationAttributeType.getId());
        if (!pcRatUsages.isEmpty()) {
            this.factory.remove(pcRatUsages.get(0));
        }
    }

    /**
     * Tests if the {@link RelationAttributeType} has been registered before
     * for a {@link PluggableClass}.
     *
     * @param attributeType The RelationAttributeType
     * @return <code>true</code> iff the RelationAttributeType has been registered before
     */
    public boolean isDefaultAttribute(RelationAttributeType attributeType) {
        List<PluggableClassRelationAttributeTypeUsage> pcRatUsages =
                this.factory.find("relationAttributeTypeId", attributeType.getId());
        return !pcRatUsages.isEmpty();
    }

    /**
     * Tests if the {@link RelationAttributeType} has been registered before
     * for the specified {@link PluggableClass}.
     *
     * @param attributeType The RelationAttributeType
     * @return <code>true</code> iff the RelationAttributeType has been registered before
     */
    public boolean isRegistered(PluggableClass pluggableClass, RelationAttributeType attributeType) {
        List<PluggableClassRelationAttributeTypeUsage> pcRatUsages =
                this.factory.find(
                        "pluggableClassId", pluggableClass.getId(),
                        "relationAttributeTypeId", attributeType.getId());
        return !pcRatUsages.isEmpty();
    }

}