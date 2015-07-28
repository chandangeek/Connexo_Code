package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.LoadProfileSpec;

/**
 * Defines the behavior for a component that is interested
 * to know about the completion of the building
 * process of a LoadProfileSpec.
 */
public interface BuildingCompletionListener {
    /**
     * Notifies the listener that the building process completed.
     *
     * @param loadProfileSpec The LoadProfileSpec that was completed by the building process
     */
    public void loadProfileSpecBuildingProcessCompleted(LoadProfileSpec loadProfileSpec);
}