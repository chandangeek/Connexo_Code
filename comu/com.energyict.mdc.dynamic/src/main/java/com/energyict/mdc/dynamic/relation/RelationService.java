package com.energyict.mdc.dynamic.relation;

import com.energyict.mdc.dynamic.PropertySpecService;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Optional;

/**
 * Provides services that relate to {@link RelationType}s,
 * their {@link RelationAttributeType attributes}
 * and instances of the RelationTypes.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-17 (10:27)
 */
@ProviderType
public interface RelationService {

    public static final String COMPONENT_NAME = "CDR";

    /**
     * Finds all the active {@link RelationType}s.
     *
     * @return the List of RelationType.
     * @see RelationType#isActive()
     */
    public List<RelationType> findAllActiveRelationTypes();

    /**
     * Finds the {@link RelationType} with the specified unique identifier.
     *
     * @param id the unique identifier
     * @return the RelationType or <code>null</code> if no such RelationType exists.
     */
    public Optional<RelationType> findRelationType(int id);

    /**
     * Creates a new {@link RelationType} from the specifications
     * laid out in the {@link RelationTypeShadow}.
     *
     * @param shadow The RelationTypeShadow
     * @param propertySpecService The PropertySpecService
     * @return the newly created RelationType
     */
    public RelationType createRelationType(RelationTypeShadow shadow, PropertySpecService propertySpecService);

    /**
     * Finds the {@link RelationType} with the specified name.
     *
     * @param name the name to match
     * @return the RelationType or <code>null</code> if no such RelationType exists.
     */
    public Optional<RelationType> findRelationType(String name);

    /**
     * Finds the {@link RelationAttributeType} that is uniquely
     * identified by the specified number.
     *
     * @param id The unique identifier
     * @return The RelationAttributeType or <code>null</code> if no such RelationAttributeType exists
     */
    public Optional<RelationAttributeType> findRelationAttributeType(int id);

    /**
     * Finds the List of {@link RelationType}s that can be used
     * to create a {@link Relation} with the given {@link RelationParticipant}.
     *
     * @param participant The RelationParticipant
     * @return the List of RelationType
     */
    public List<RelationType> findRelationTypesByParticipant(RelationParticipant participant);

}