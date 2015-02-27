package com.energyict.mdc.dynamic.relation;

import java.util.List;

/**
 * Provides factory services for {@link RelationAttributeType}s.
 */
public interface RelationAttributeTypeFactory {

    /**
     * Finds the {@link RelationAttributeType} with the given id.
     *
     * @param id id to match
     * @return the relation attriubte type or null.
     */
    public RelationAttributeType find(int id);

    /**
     * Finds a List of {@link RelationAttributeType}s that can be used by a given relation participant.
     *
     * @param participant The RelationParticipant
     * @return the List of RelationAttributeType
     */
    public List<RelationAttributeType> findByParticipant(RelationParticipant participant);

}