package com.energyict.mdc.device.data.security;

public interface Privileges {

    String ADD_DEVICE = "privilege.add.device";
    String VIEW_DEVICE = "privilege.view.device";
    //Dummy privilege used to cover a REST call which is still not use in front-end
    String REMOVE_DEVICE = "privilege.remove.device";

    String ADMINISTRATE_DEVICE_DATA = "privilege.administrate.deviceData";

    String ADMINISTRATE_DEVICE_COMMUNICATION = "privilege.administrate.deviceCommunication";
    String OPERATE_DEVICE_COMMUNICATION = "privilege.operate.deviceCommunication";

    String ADMINISTRATE_DEVICE_GROUP = "privilege.administrate.deviceGroup";
    String ADMINISTRATE_DEVICE_ENUMERATED_GROUP = "privilege.administrate.deviceOfEnumeratedGroup";
    String VIEW_DEVICE_GROUP_DETAIL = "privilege.view.deviceGroupDetail";

    String IMPORT_INVENTORY_MANAGEMENT = "privilege.import.inventoryManagement";
    String REVOKE_INVENTORY_MANAGEMENT = "privilege.revoke.inventoryManagement";

    String ADMINISTRATE_DEVICE_ATTRIBUTE = "privilege.administrate.attribute.device";
    String ADMINISTRATE_DECOMMISSIONED_DEVICE_DATA = "privilege.administer.decommissioned.deviceData";

}
