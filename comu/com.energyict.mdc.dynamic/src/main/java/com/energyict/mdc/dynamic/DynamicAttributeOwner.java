package com.energyict.mdc.dynamic;

public interface DynamicAttributeOwner extends ReadOnlyDynamicAttributeOwner {

    public void set(String key, Object newValue);

}