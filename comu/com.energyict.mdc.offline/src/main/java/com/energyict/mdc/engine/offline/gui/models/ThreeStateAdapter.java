package com.energyict.mdc.engine.offline.gui.models;

public class ThreeStateAdapter extends ValueAdapter {

    public ThreeStateAdapter(DynamicAttributeOwner model, String aspect) {
        super(model, aspect);
    }

    public Boolean getValue() {
        return (Boolean) doGetValue();
    }

    public void setValue(Boolean value) {
        doSetValue(value);
    }

}
