package com.energyict.mdc.dynamic.relation;

import com.energyict.mdc.common.NamedObjectShadow;
import com.energyict.mdc.dynamic.ValueFactory;

public class RelationAttributeTypeShadow extends NamedObjectShadow {

    private Class valueFactoryClass;
    private String displayName;
    private String roleName;
    private int relationTypeId;
    private int objectFactoryId;
    private boolean required = false;
    private boolean navigatable = true;
    private boolean isDefault = false;
    private boolean hidden = false;
    private boolean essentialAttributesChanged = false;

    public RelationAttributeTypeShadow() {
        super();
    }

    public RelationAttributeTypeShadow(RelationAttributeType relationAttributeType) {
        super(relationAttributeType.getId(), relationAttributeType.getName());
        ValueFactory valueFactory = relationAttributeType.getValueFactory();
        this.valueFactoryClass = valueFactory.getClass();
        this.roleName = relationAttributeType.getRoleName();
        this.relationTypeId = relationAttributeType.getRelationTypeId();
        this.objectFactoryId = valueFactory.getObjectFactoryId();
        this.navigatable = relationAttributeType.isNavigatable();
        this.isDefault = relationAttributeType.isDefault();
        this.hidden = relationAttributeType.isHidden();
    }

    public boolean getEssentialAttributesChanged() {
        return essentialAttributesChanged;
    }

    /**
     * Marks this shadow in a way that it indicates
     * that one of the essential attributes have changed.
     */
    protected void markEssentialAttributeChange () {
        this.essentialAttributesChanged = true;
    }

    public String getValueFactoryClassName() {
        if (this.valueFactoryClass == null) {
            return null;
        }
        else {
            return this.valueFactoryClass.getName();
        }
    }

    /**
     * Gets the Class that determines how values of the attribute
     * are persistend to the database.
     *
     * @return The Class
     */
    @SuppressWarnings("unchecked")
    public <T extends ValueFactory> Class<T> getValueFactoryClass () {
        return valueFactoryClass;
    }

    public <T extends ValueFactory> void setValueFactoryClass (Class<T> valueFactoryClass) {
        this.valueFactoryClass = valueFactoryClass;
        this.markDirty();
    }
    public int getRelationTypeId() {
        return relationTypeId;
    }

    public void setRelationTypeId(int relationTypeId) {
        this.relationTypeId = relationTypeId;
        markDirty();
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
        this.markDirty();
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String rName) {
        roleName = rName;
        markDirty();
    }

    public int getObjectFactoryId() {
        return objectFactoryId;
    }

    public void setObjectFactoryId(int objectFactoryId) {
        if (objectFactoryId != this.objectFactoryId) {
            this.markEssentialAttributeChange();
        }
        this.objectFactoryId = objectFactoryId;
        this.markDirty();
    }

    public boolean isRequired() {
        return required;
    }

    public boolean getRequired() {
        return isRequired();
    }

    public void setRequired(boolean required) {
        this.essentialAttributesChanged = (required != this.required);
        this.required = required;
        this.markDirty();
    }

    public boolean isNavigatable() {
        return navigatable;
    }

    public void setNavigatable(boolean navigatable) {
        this.navigatable = navigatable;
        this.markDirty();
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
        this.markDirty();
    }

    public boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(boolean def) {
        if (def != this.isDefault) {
            this.markEssentialAttributeChange();
        }
        this.isDefault = def;
        this.markDirty();
    }

    public void doCopy(RelationAttributeTypeShadow source) {
        setId(source.getId());
        setName(source.getName());
        setValueFactoryClass(source.getValueFactoryClass());
        setRelationTypeId(source.getRelationTypeId());
        setObjectFactoryId(source.getObjectFactoryId());
        setRequired(source.getRequired());
        setNavigatable(source.isNavigatable());
        setRoleName(source.getRoleName());
        setHidden(source.isHidden());
    }

}