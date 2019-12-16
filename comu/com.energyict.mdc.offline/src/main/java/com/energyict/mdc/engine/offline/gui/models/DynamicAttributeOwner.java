package com.energyict.mdc.engine.offline.gui.models;

public interface DynamicAttributeOwner extends ReadOnlyDynamicAttributeOwner {

    public void set(String key, Object newValue);
}
