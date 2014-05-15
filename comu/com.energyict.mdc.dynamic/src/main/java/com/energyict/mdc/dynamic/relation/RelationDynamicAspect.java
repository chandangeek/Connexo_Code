package com.energyict.mdc.dynamic.relation;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-05-14 (16:54)
 */
public class RelationDynamicAspect implements FilterAspect {

    private RelationAttributeType attributeType;

    public RelationDynamicAspect(RelationAttributeType attributeType) {
        this.attributeType = attributeType;
    }

    public int getJdbcType() {
        return attributeType.getJdbcType();
    }

    public Object valueToDb(Object object) {
        return attributeType.valueToDb(object);
    }

    public String getColumnName() {
        return attributeType.getName();
    }

    public String getDisplayName() {
        return attributeType.displayString();
    }

    public String getName() {
        return attributeType.getName();
    }

    public Class getValueType() {
        return attributeType.getValueType();
    }

}