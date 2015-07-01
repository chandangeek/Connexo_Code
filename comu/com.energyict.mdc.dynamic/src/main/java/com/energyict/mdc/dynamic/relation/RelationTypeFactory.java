package com.energyict.mdc.dynamic.relation;

import com.energyict.mdc.common.BusinessException;

import aQute.bnd.annotation.ProviderType;

import java.sql.SQLException;
import java.util.List;

/**
 * Provides factory services for {@link RelationType}s.
 */
@ProviderType
public interface RelationTypeFactory {

    /**
     * Finds the {@link RelationType} with the given id.
     *
     * @param id id to match
     * @return the RelationType or <code>null</code> if no such RelationType exists.
     */
    public RelationType find(int id);

    /**
     * Finds the {@link RelationType} with the given name.
     *
     * @param name the name to match
     * @return the RelationType or <code>null</code> if no such RelationType exists.
     */
    public RelationType find(String name);

    /**
     * Finds the list of {@link RelationType}s that can be used
     * to create a {@link Relation} with the given {@link RelationParticipant}.
     *
     * @param participant the RelationParticipant
     * @return the List of RelationType
     */
    public List<RelationType> findByParticipant(RelationParticipant participant);

    /**
     * Creates a new {@link RelationType} from the specifications
     * laid out in the {@link RelationTypeShadow}.
     *
     * @param shadow contains the new relation type's attribute values
     * @return the new RelationType
     * @throws BusinessException Thrown if a business constraint was violated
     * @throws SQLException Thrown if a database constraint was violated
     */
    public RelationType create(RelationTypeShadow shadow) throws SQLException, BusinessException;

}