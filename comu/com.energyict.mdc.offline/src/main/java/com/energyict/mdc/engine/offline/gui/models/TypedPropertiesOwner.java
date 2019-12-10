package com.energyict.mdc.engine.offline.gui.models;

import com.energyict.mdc.upl.TypedProperties;

/**
 * Interface for classes owning TypedProperties
 * Date: 22/08/13
 * Time: 8:40
 */
public interface TypedPropertiesOwner {
    public TypedProperties getProperties();

    public void setProperties(TypedProperties peoperties);
}
