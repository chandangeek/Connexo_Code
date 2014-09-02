package com.energyict.mdc.device.data.security;

public interface Privileges {

    String CREATE_DEVICE = "privilege.create.device";
    String UPDATE_DEVICE = "privilege.update.device";
    String DELETE_DEVICE = "privilege.delete.device";
    String VIEW_DEVICE = "privilege.view.device";
    String IMPORT_DEVICE = "privilege.view.importDevice";
    String REVOKE_DEVICE = "privilege.view.revokeDevice";
    String VALIDATE_DEVICE = "privilege.view.validateDevice";
    String SCHEDULE_DEVICE = "privilege.view.scheduleDevice";

    String CREATE_LOAD_PROFILE = "privilege.create.loadProfile";
    String UPDATE_LOAD_PROFILE = "privilege.update.loadProfile";
    String DELETE_LOAD_PROFILE = "privilege.delete.loadProfile";
    String VIEW_LOAD_PROFILE = "privilege.view.loadProfile";

    String CREATE_LOGBOOK = "privilege.create.logBook";
    String UPDATE_LOGBOOK = "privilege.update.logBook";
    String DELETE_LOGBOOK = "privilege.delete.logBook";
    String VIEW_LOGBOOK = "privilege.view.logBook";

    String CREATE_SECURITY_PROPERTY_SET = "privilege.create.securityPropertySet";
    String UPDATE_SECURITY_PROPERTY_SET = "privilege.update.securityPropertySet";
    String DELETE_SECURITY_PROPERTY_SET = "privilege.delete.securityPropertySet";
    String VIEW_SECURITY_PROPERTY_SET = "privilege.view.securityPropertySet";

}
