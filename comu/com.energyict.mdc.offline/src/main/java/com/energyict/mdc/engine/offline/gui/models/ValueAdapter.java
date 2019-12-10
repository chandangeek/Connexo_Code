/*
 * ValueAdapter.java
 *
 * Created on 8 september 2003, 15:31
 */

package com.energyict.mdc.engine.offline.gui.models;

/**
 * @author Karel
 */
public class ValueAdapter {

    private DynamicAttributeOwner model;
    private String aspect;

    /**
     * Creates a new instance of ValueAdapter
     */
    protected ValueAdapter(DynamicAttributeOwner model, String aspect) {
        this.model = model;
        this.aspect = aspect;
    }

    protected Object doGetValue() {
        return model.get(aspect);
    }

    protected void doSetValue(Object value) {
        model.set(aspect, value);
    }
}
