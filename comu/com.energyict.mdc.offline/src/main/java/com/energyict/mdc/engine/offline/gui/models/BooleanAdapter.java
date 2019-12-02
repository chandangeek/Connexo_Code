/*
 * ValueAdapter.java
 *
 * Created on 8 september 2003, 15:31
 */

package com.energyict.mdc.engine.offline.gui.models;

/**
 * @author Karel
 */
public class BooleanAdapter extends ValueAdapter {

    public BooleanAdapter(DynamicAttributeOwner model, String aspect) {
        super(model, aspect);
    }

    public boolean getValue() {
        Boolean result = (Boolean) doGetValue();
        if (result == null) {
            return false;
        } else {
            return result.booleanValue();
        }
    }

    public void setValue(boolean value) {
        doSetValue(Boolean.valueOf(value));
    }

    public boolean isNull() {
        return doGetValue() == null;
    }
}
