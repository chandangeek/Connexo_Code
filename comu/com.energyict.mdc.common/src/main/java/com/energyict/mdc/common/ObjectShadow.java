package com.energyict.mdc.common;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Represents a shadow object for a BusinessObject.
 * Shadow objects are used to take a temporary copy of the BusinessObject's state
 *
 * @author Karel
 * @see BusinessObject
 */
public class ObjectShadow implements java.io.Serializable, Cloneable {

    private boolean dirty = false;
    private Permission permission = ShadowPermission.NORMAL;

    private transient PropertyChangeSupport propertyChangeSupport;

    /**
     * Creates a new instance of ShadowObject
     */
    public ObjectShadow() {
    }

    protected PropertyChangeSupport getPropertyChangeSupport() {
        if (propertyChangeSupport == null) {
            propertyChangeSupport = new PropertyChangeSupport(this);
        }
        return propertyChangeSupport;
    }

    /**
     * marks the receiver as modified.
     */
    public void markDirty() {
        dirty = true;
        firePropertyChange();
    }

    /**
     * test if the receiver has been modified.
     *
     * @return true if the receiver has been modified.
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * tests if the receiver as up to date.
     *
     * @return return true if the receiver is up to date.
     * @deprecated true if the receiver has not been modified.
     */
    public boolean isClean() {
        return !isDirty();
    }

    /**
     * marks the receiver as up to date.
     */
    public void markClean() {
        dirty = false;
    }

    /**
     * Returns a clone of the receiver
     *
     * @return a clone
     * @throws CloneNotSupportedException should not be thrown
     */
    public Object clone() throws java.lang.CloneNotSupportedException {
        return super.clone();
    }


    /**
     * Adds a property change listener.
     *
     * @param l the listener to add.
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        getPropertyChangeSupport().addPropertyChangeListener(l);
    }

    /**
     * Removes the given property change listener.
     *
     * @param l the listener to remove.
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        getPropertyChangeSupport().removePropertyChangeListener(l);
    }

    protected void firePropertyChange() {
        getPropertyChangeSupport().firePropertyChange(null, null, null);
    }

    /**
     * initializes the receiver for multi edit
     */
    public void initializeForMultiEdit() {
    }

    /**
     * prepares the receiver for cloning
     */
    public void prepareCloning() {
    }

    public Permission permission() {
        return permission;
    }

    public void grantPermission(Permission permission) {
        this.permission = permission;
    }
}
