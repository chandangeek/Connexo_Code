package com.energyict.mdc.device.data.security;

public interface Privileges {

    String ADMINISTRATE_DEVICE = "privilege.administrate.device";
    String VIEW_DEVICE = "privilege.view.device";
    String VALIDATE_MANUAL = "privilege.view.validateManual";
    String FINE_TUNE_VALIDATION_CONFIGURATION = "privilege.view.fineTuneValidationConfiguration";
    String SCHEDULE_DEVICE = "privilege.view.scheduleDevice";

    String ADMINISTRATE_DEVICE_GROUP = "privilege.administrate.deviceGroup";
    String ADMINISTRATE_DEVICE_ENUMERATED_GROUP = "privilege.administrate.deviceOfEnumeratedGroup";
    String VIEW_DEVICE_GROUP_DETAIL = "privilege.view.deviceGroupDetail";

    String IMPORT_INVENTORY_MANAGEMENT = "privilege.import.inventoryManagement";
    String REVOKE_INVENTORY_MANAGEMENT = "privilege.revoke.inventoryManagement";
    String CREATE_INVENTORY_MANAGEMENT = "privilege.create.inventoryManagement";

    String ADMINISTRATE_DEVICE_SECURITY = "privilege.administrate.deviceSecurity";
    String VIEW_DEVICE_SECURITY = "privilege.view.deviceSecurity";

}
