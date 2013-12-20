package com.energyict.mdc.dynamic.relation;

import com.energyict.mdc.common.NamedObjectShadow;
import com.energyict.mdc.common.ShadowList;

import java.util.List;
import java.util.Map;


public class ConstraintShadow extends NamedObjectShadow {

    private ShadowList<RelationAttributeTypeShadow> attributeTypeShadows = new ShadowList<>();
    private int relationTypeId;
    private boolean rejectViolations = false;

    public ConstraintShadow() {}

    public ConstraintShadow(Constraint constraint) {
        super(constraint.getId(), constraint.getName());
        this.relationTypeId = constraint.getRelationTypeId();
        this.rejectViolations = constraint.isRejectViolations();
        for (Object o : constraint.getAttributeTypes()) {
            RelationAttributeType aType = (RelationAttributeType) o;
            attributeTypeShadows.basicAdd(aType.getShadow());
        }
        markClean();
    }

    public ConstraintShadow(Constraint constraint, Map attributeMap) {
        super(constraint.getId(), constraint.getName());
        this.relationTypeId = constraint.getRelationTypeId();
        this.rejectViolations = constraint.isRejectViolations();
        for (Object o : constraint.getAttributeTypes()) {
            RelationAttributeType aType = (RelationAttributeType) o;
            attributeTypeShadows.basicAdd((RelationAttributeTypeShadow) attributeMap.get(aType.getId()));
        }
        markClean();
    }

    public void setAttributeTypeShadows(List<RelationAttributeTypeShadow> attributeTypeShadows) {
        this.attributeTypeShadows.clear();
        for (RelationAttributeTypeShadow attributeTypeShadow : attributeTypeShadows) {
            this.attributeTypeShadows.add(attributeTypeShadow);
        }
    }

    public ShadowList<RelationAttributeTypeShadow> getAttributeTypeShadows() {
        return attributeTypeShadows;
    }

    public void add(RelationAttributeTypeShadow shadow) {
        attributeTypeShadows.add(shadow);
    }

    public void remove(RelationAttributeTypeShadow shadow) {
        attributeTypeShadows.remove(shadow);
    }

    @Override
    public boolean isDirty() {
        return super.isDirty() || this.attributeTypeShadows.isDirty();
    }

    public int getRelationTypeId() {
        return relationTypeId;
    }

    public void setRelationTypeId(int relationTypeId) {
        this.relationTypeId = relationTypeId;
    }

    public boolean isRejectViolations() {
        return getRejectViolations();
    }

    public boolean getRejectViolations() {
        return rejectViolations;
    }

    public void setRejectViolations(boolean rejectViolations) {
        this.rejectViolations = rejectViolations;
        markDirty();
    }

    public void doCopy(ConstraintShadow source) {
        setId(source.getId());
        setName(source.getName());
        setRelationTypeId(source.getRelationTypeId());
        setRejectViolations(source.getRejectViolations());
        setAttributeTypeShadows(source.getAttributeTypeShadows());
    }

}