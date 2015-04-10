package com.energyict.mdc.device.lifecycle.config;

/**
 * Models the privileges of the device life cycle bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-11 (11:02)
 */
public interface Privileges {

    public String VIEW_DEVICE_LIFE_CYCLES = "privilege.view.deviceLifeCycles";
    public String CONFIGURE_DEVICE_LIFE_CYCLE = "privilege.configure.deviceLifeCycles";

    public String INITIATE_ACTION_1 = "device.lifecycle.initiate.action.level1";
    public String INITIATE_ACTION_2 = "device.lifecycle.initiate.action.level2";
    public String INITIATE_ACTION_3 = "device.lifecycle.initiate.action.level3";
    public String INITIATE_ACTION_4 = "device.lifecycle.initiate.action.level4";

}