package com.energyict.mdc.engine.offline.gui.models;

import com.energyict.mdc.upl.TypedProperties;

import java.beans.PropertyChangeListener;

/**
 * Models the behavior of the shadow class
 * for an object that has dynamic attributes.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-01-07 (15:49)
 */
public interface DynamicAttributeOwnerShadow extends DynamicAttributeOwner {

    public TypedProperties getProperties();

    public Object get(String key);

    public void set(String key, Object newValue);

    public void remove(String key);

    public void markDirty();

    public boolean isDirty();

    public void markClean();

    public boolean isClean();

    public void addPropertyChangeListener(PropertyChangeListener listener);

    public void removePropertyChangeListener(PropertyChangeListener listener);

}