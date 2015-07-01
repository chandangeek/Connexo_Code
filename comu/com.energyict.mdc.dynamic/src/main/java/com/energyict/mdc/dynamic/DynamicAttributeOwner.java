package com.energyict.mdc.dynamic;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface DynamicAttributeOwner extends ReadOnlyDynamicAttributeOwner {

    public void set(String key, Object newValue);

}