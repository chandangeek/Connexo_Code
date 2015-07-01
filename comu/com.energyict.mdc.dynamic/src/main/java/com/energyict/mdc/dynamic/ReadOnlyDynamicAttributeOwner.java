package com.energyict.mdc.dynamic;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface ReadOnlyDynamicAttributeOwner {

    public Object get(String key);

}