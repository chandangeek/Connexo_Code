package com.energyict.mdc.dynamic.relation;

import com.energyict.mdc.common.BusinessException;

/**
 * Exception thrown when creating/updating a Relation of a <RelationType> for which a <code>Constraint</code>
 * has been defined, and for which constraint is violated.
 */
public class ConstraintViolationException extends BusinessException {

    private Constraint constraint;

    public ConstraintViolationException(Constraint constraint) {
        super("constraintViolationX", "Constraint \"{0}\" has been violated", constraint.getName());
        this.constraint = constraint;
    }

    public Constraint getConstraint() {
        return constraint;
    }

}