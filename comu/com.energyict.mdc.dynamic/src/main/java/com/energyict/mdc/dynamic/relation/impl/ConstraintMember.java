package com.energyict.mdc.dynamic.relation.impl;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.dynamic.relation.Constraint;

/**
 * Models the relation between {@link Constraint} and {@link com.energyict.mdc.dynamic.relation.RelationAttributeType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-18 (10:51)
 */
public class ConstraintMember {

    private final Reference<Constraint> constraint = ValueReference.absent();
    private int attributeTypeId;

    public ConstraintMember() {
        super();
    }

    public ConstraintMember(Constraint constraint, int attributeTypeId) {
        this();
        this.constraint.set(constraint);
        this.attributeTypeId = attributeTypeId;
    }

    public Constraint getConstraint() {
        return constraint.get();
    }

    public void setConstraint(Constraint constraint) {
        this.constraint.set(constraint);
    }

    public int getAttributeTypeId() {
        return attributeTypeId;
    }

    public void setAttributeTypeId(int attributeTypeId) {
        this.attributeTypeId = attributeTypeId;
    }

}