package com.energyict.mdw.cpo;

import com.energyict.mdw.dynamicattributes.ReferenceFactory;
import com.energyict.mdc.common.IdBusinessObject;
import com.energyict.mdc.common.IdBusinessObjectFactory;
import com.energyict.mdc.dynamic.DynamicAttributeOwner;
import com.energyict.mdc.protocol.api.legacy.dynamic.AttributeValueSelectionMode;
import com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpec;
import com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpecPossibleValues;
import com.energyict.mdc.protocol.api.legacy.dynamic.Seed;
import com.energyict.mdc.protocol.api.legacy.dynamic.ValueDomain;

/**
 * Provides an implementation for the {@link PropertySpec}
 * interface that models a reference to another object.
 * The {@link ValueDomain} that is specified at construction
 * time determines the objects that are referred to
 * and also which sub-types are allowed.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-06-05 (13:59)
 */
public class ReferencePropertySpec<T extends IdBusinessObject> extends BasicPropertySpec<T> {

    private AttributeValueSelectionMode selectionMode;

    public ReferencePropertySpec (String name, ValueDomain valueDomain) {
        this(name, valueDomain, AttributeValueSelectionMode.SEARCH_AND_SELECT);
    }

    public ReferencePropertySpec (String name, ValueDomain valueDomain, AttributeValueSelectionMode selectionMode) {
        super(name, new ReferenceFactory(), valueDomain);
        this.selectionMode = selectionMode;
    }

    @Override
    public AttributeValueSelectionMode getSelectionMode () {
        return this.selectionMode;
    }

    @Override
    public IdBusinessObjectFactory<T> getObjectFactory () {
        return (IdBusinessObjectFactory<T>) this.getDomain().getAnyTypeId().getFactory();
    }

    @Override
    public PropertySpecPossibleValues<T> getPossibleValues () {
        PropertySpecPossibleValues<T> superiorOpinion = super.getPossibleValues();
        if (superiorOpinion == null && this.selectionModeIsExhaustive()) {
            PropertySpecPossibleValues<T> newPropertySpecPossibleValues = new PropertySpecPossibleValuesImpl<>(true, this.getObjectFactory().findAll());
            this.setPossibleValues(newPropertySpecPossibleValues);
            return newPropertySpecPossibleValues;
        }
        return superiorOpinion;
    }

    private boolean selectionModeIsExhaustive () {
        return AttributeValueSelectionMode.COMBOBOX.equals(this.getSelectionMode())
                || AttributeValueSelectionMode.LIST.equals(this.getSelectionMode());
    }

    @Override
    public Seed getEditorSeed(DynamicAttributeOwner model, boolean required) {
        return ((ReferenceFactory)getValueFactory()).getEditorSeed(model, this, required);
    }

}