package com.energyict.mdc.dynamic.relation;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.NamedBusinessObject;
import com.energyict.mdc.common.SqlBuilder;

import java.sql.SQLException;
import java.util.List;

/**
 * Represents a uniqueness constraint for a <code>List</code> of Attributes within a
 * <code>RelationType</code>
 */
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
     * return the list with the relations that would cause a violation when adding the new
     * relation as described by the transaction
     *
     * @param transaction
     * @return list of Relations
     */
    public List getViolatedRelations(RelationTransaction transaction);

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
     * @throws BusinessException
     * @throws SQLException
     */
    public void update(ConstraintShadow shadow) throws BusinessException, SQLException;

    /**
     * append the sql expression for this constraint to the SqlBuilder
     *
     * @param builder
     * @param transaction
     * @return true if sql has been added, false if the constraint is to be ignored and no sql has been added
     */
    public boolean appendAttributeSql(SqlBuilder builder, RelationTransaction transaction);

}