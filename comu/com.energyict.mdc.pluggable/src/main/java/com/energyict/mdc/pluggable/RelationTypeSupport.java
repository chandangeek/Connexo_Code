package com.energyict.mdc.pluggable;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.dynamic.relation.RelationParticipant;
import com.energyict.mdc.dynamic.relation.RelationType;

import java.sql.SQLException;

/**
 * Models the behavior of a component that provides access
 * to {@link RelationType}s that hold values for properties
 * that are provided by a {@link PluggableClass}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-12-17 (15:22)
 */
public interface RelationTypeSupport {

    /**
     * Finds or creates the {@link RelationType} that holds the properties of a {@link RelationParticipant}.
     * Note that if no properties are specified and therefore no property values need to be stored,
     * then <code>null</code> is returned because no RelationType is created in that case.
     *
     * @return The RelationType
     * @param activate A flag that indicates if the RelationType should be activated too
     */
    public RelationType findOrCreateRelationType(boolean activate);

    /**
     * Finds the {@link RelationType} that holds the properties of {@link RelationParticipant}.
     * This can only return <code>null</code> if no properties are specified.
     * If however, properties have been specified, a <code>null</code>
     * value indicates that the RelationType was not yet created and
     * that is an indication of a coding problem.
     *
     * @return The RelationType
     * @see #findOrCreateRelationType(boolean)
     */
    public RelationType findRelationType ();

    /**
     * Deletes the {@link RelationType} that holds the properties of {@link RelationParticipant}.
     * Note also that when another PluggableClass was registered with the same
     * java class name, that will use the same RelationType,
     * the RelationType is not actually deleted.
     *
     * @throws BusinessException Thrown if a business constraint was violated.
     *                           An example of such a constraint is when the properties are still
     *                           active on a {@link RelationParticipant}.
     * @throws SQLException Thrown if a database constraint was violated
     */
    public void deleteRelationType () throws BusinessException, SQLException;

}