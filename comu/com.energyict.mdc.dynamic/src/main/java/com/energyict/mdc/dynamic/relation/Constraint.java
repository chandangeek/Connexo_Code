package com.energyict.mdc.dynamic.relation;

import com.energyict.mdc.common.NamedBusinessObject;
import com.energyict.mdc.common.SqlBuilder;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

/**
 * Represents a uniqueness constraint for a <code>List</code> of Attributes within a
 * <code>RelationType</code>
 */
@ProviderType
public interface Constraint extends NamedBusinessObject {

    /**
     * returns the attribute types for this folder type
     *
     * @return a List of RelationAttributeType objects
     */
    public List getAttributeTypes();

    /**
     * return the relation type id
     *
     * @return the relation type id
     */
    public int getRelationTypeId();

    /**
     * return whether this constraint should reject violations (true) or
     * resolve (false) them
     *
     * @return true if the constraint rejects violations
     */
    public boolean isRejectViolations();

    /**
     * Gets the list of {@link Relation}s that would cause a violation when adding the new
     * Relation as described by the transaction.
     *
     * @param transaction The transaction
     * @return list of Relations
     */
    public List<Relation> getViolatedRelations(RelationTransaction transaction);

    /**
     * returns the constraint shadow for this constraint
     *
     * @return a ConstraintShadow object
     */
    public ConstraintShadow getShadow();

    /**
     * Update the constraint using the given <code>ConstraintShadow</code>
     *
     * @param shadow the values to update the receiver with
     */
    public void update(ConstraintShadow shadow);

    /**
     * append the sql expression for this constraint to the SqlBuilder
     *
     * @param builder
     * @param transaction
     * @return true if sql has been added, false if the constraint is to be ignored and no sql has been added
     */
    public boolean appendAttributeSql(SqlBuilder builder, RelationTransaction transaction);

}