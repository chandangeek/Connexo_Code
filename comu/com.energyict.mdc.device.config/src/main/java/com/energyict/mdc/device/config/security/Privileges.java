package com.energyict.mdc.device.config.security;

public interface Privileges {

    String CREATE_DEVICE_CONFIGURATION = "privilege.create.deviceConfiguration";
    String UPDATE_DEVICE_CONFIGURATION = "privilege.update.deviceConfiguration";
    String DELETE_DEVICE_CONFIGURATION = "privilege.delete.deviceConfiguration";
    String VIEW_DEVICE_CONFIGURATION = "privilege.view.deviceConfiguration";
    String ACTIVATE_DEVICE_CONFIGURATION = "privilege.activate.deviceConfiguration";

    String CREATE_DEVICE_TYPE = "privilege.create.deviceType";
    String UPDATE_DEVICE_TYPE = "privilege.update.deviceType";
    String DELETE_DEVICE_TYPE = "privilege.delete.deviceType";
    String VIEW_DEVICE_TYPE = "privilege.view.deviceType";

    String CREATE_LOAD_PROFILE_CONFIG = "privilege.create.loadProfileConfiguration";
    String UPDATE_LOAD_PROFILE_CONFIG = "privilege.update.loadProfileConfiguration";
    String DELETE_LOAD_PROFILE_CONFIG = "privilege.delete.loadProfileConfiguration";
    String VIEW_LOAD_PROFILE_CONFIG = "privilege.view.loadProfileConfiguration";

    String CREATE_REGISTER_CONFIG = "privilege.create.registerConfiguration";
    String UPDATE_REGISTER_CONFIG = "privilege.update.registerConfiguration";
    String DELETE_REGISTER_CONFIG = "privilege.delete.registerConfiguration";
    String VIEW_REGISTER_CONFIG = "privilege.view.registerConfiguration";
}
