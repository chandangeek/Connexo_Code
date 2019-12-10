package com.energyict.mdc.engine.offline.gui.models;

public interface Navigator {

    /**
     * Navigate to a new Object
     *
     * @param obj
     */
    public void navigateTo(Object obj);

    /**
     * Navigate to previous object
     */
    public void goBack();

    /**
     * Revisits/refreshes the current object
     */
    public void revisit();

}
