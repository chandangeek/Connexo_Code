package com.energyict.mdc.dynamic.relation;

import com.energyict.mdc.common.NamedObjectShadow;
import com.energyict.mdc.common.ShadowList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Relation Type Shadow
 */
public class RelationTypeShadow extends NamedObjectShadow {

    private ShadowList<RelationAttributeTypeShadow> attributeTypeShadows = new ShadowList<>();
    private ShadowList<ConstraintShadow> constraintShadows = new ShadowList<>();
    private boolean hasTimeResolution = true;
    private String displayName;
    private RelationAttributeTypeShadow lockAttributeTypeShadow;
    private boolean system;

    /**
     * creates a new instance
     */
    public RelationTypeShadow() {
    }

    /**
     * Creates a new instance
     *
     * @param relationType object to shadow
     */
    public RelationTypeShadow(RelationType relationType) {
        super(relationType.getId(), relationType.getName());
        hasTimeResolution = relationType.hasTimeResolution();
        displayName = relationType.getDisplayName();
        Map<Integer, RelationAttributeTypeShadow> attributeMap = new HashMap<>();
        Iterator it = relationType.getAttributeTypes().iterator();
        while (it.hasNext()) {
            RelationAttributeType aType = (RelationAttributeType) it.next();
            RelationAttributeTypeShadow shadow = aType.getShadow();
            attributeTypeShadows.basicAdd(shadow);
            attributeMap.put(aType.getId(), shadow);
        }
        it = relationType.getConstraints().iterator();
        while (it.hasNext()) {
            Constraint constraint = (Constraint) it.next();
            constraintShadows.basicAdd(new ConstraintShadow(constraint, attributeMap));
        }
        RelationAttributeType lockAttrib = relationType.getLockAttributeType();
        if (lockAttrib != null) {
            lockAttributeTypeShadow = attributeMap.get(lockAttrib.getId());
        }
        markClean();
    }

    /**
     * sets the attribute type shadows
     *
     * @param attributeTypeShadows a List of RelationAttributeTypeShadow objects
     */
    public void setAttributeTypeShadows(ShadowList<RelationAttributeTypeShadow> attributeTypeShadows) {
        this.attributeTypeShadows = attributeTypeShadows;
    }

    /**
     * Returns the attribute type shadows
     *
     * @return a (Shadow)List of RelationAttributeTypeShadow
     *         objects
     */
    public ShadowList<RelationAttributeTypeShadow> getAttributeTypeShadows() {
        return attributeTypeShadows;
    }

    /**
     * Adds an attribute type shadow
     *
     * @param shadow shadow to add
     */
    public void add(RelationAttributeTypeShadow shadow) {
        attributeTypeShadows.add(attributeTypeShadows.size(), shadow);
        markDirty();
    }

    /**
     * Removes an attribute type shadow
     *
     * @param shadow shadow to remove
     */
    public void remove(RelationAttributeTypeShadow shadow) {
        attributeTypeShadows.remove(shadow);
        markDirty();
    }

    /**
     * Adds a constraint shadow
     *
     * @param shadow shadow to add
     */
    public void add(ConstraintShadow shadow) {
        constraintShadows.add(constraintShadows.size(), shadow);
        markDirty();
    }

    /**
     * Removes a constraint shadow
     *
     * @param shadow shadow to remove
     */
    public void remove(ConstraintShadow shadow) {
        constraintShadows.remove(shadow);
        markDirty();
    }

    /**
     * prepares the receiver for cloning
     */
    public void prepareCloning() {
        super.prepareCloning();

        // 1) Make sure all attributes are considered as to be newly created:
        attributeTypeShadows.makeAllNew();

        // 2a) Make sure the constraints contain the exact RelationAttributeTypeShadow instances
        //     as in attributeTypeShadows:
        Map<String, RelationAttributeTypeShadow> attTypeShadowsByName = new HashMap<>();
        for (RelationAttributeTypeShadow each : attributeTypeShadows) {
            attTypeShadowsByName.put(each.getName(), each);
        }
        for (ConstraintShadow each : constraintShadows) {
            List<RelationAttributeTypeShadow> newAttTypeShadows = new ArrayList<>();
            for (RelationAttributeTypeShadow raTypeShadow : each.getAttributeTypeShadows()) {
                newAttTypeShadows.add(attTypeShadowsByName.get(raTypeShadow.getName()));
            }
            each.setAttributeTypeShadows(newAttTypeShadows);
        }
        // 2b) Make sure all constraints are considered as to be newly created:
        constraintShadows.makeAllNew();
    }

    public boolean hasTimeResolution() {
        return hasTimeResolution;
    }

    public boolean getHasTimeResolution() {
        return hasTimeResolution();
    }

    public void setHasTimeResolution(boolean hasTimeResolution) {
        this.hasTimeResolution = hasTimeResolution;
        this.markDirty();
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String name) {
        this.displayName = name;
        this.markDirty();
    }


    public ShadowList<ConstraintShadow> getConstraintShadows() {
        return constraintShadows;
    }

    public void setConstraintShadows(ShadowList<ConstraintShadow> constraintShadows) {
        this.constraintShadows = constraintShadows;
        this.markDirty();
    }


    public RelationAttributeTypeShadow getLockAttributeTypeShadow() {
        return lockAttributeTypeShadow;
    }

    public void setLockAttributeTypeShadow(RelationAttributeTypeShadow lockAttributeTypeShadow) {
        this.lockAttributeTypeShadow = lockAttributeTypeShadow;
        this.markDirty();
    }

    /**
     * Tests if the shadow has been modified.
     *
     * @return true if the shadow has been modified.
     */
    public boolean isDirty() {
        return super.isDirty() ||
                getAttributeTypeShadows().isDirty();
    }

    public boolean isSystem() {
        return system;
    }

    public void setSystem(boolean system) {
        this.system = system;
    }


}