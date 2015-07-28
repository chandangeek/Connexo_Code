package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.LoadProfileSpec;

/**
 * Adds behavior to the {@link LoadProfileSpec.LoadProfileSpecBuilder} interface
 * that is specific to server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-28 (08:51)
 */
interface ServerLoadProfileSpecBuilder extends LoadProfileSpec.LoadProfileSpecBuilder {

    void notifyOnAdd(BuildingCompletionListener buildingCompletionListener);

}