/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.security;

import com.elster.jupiter.nls.TranslationKey;

import java.util.Arrays;

public enum Privileges implements TranslationKey {
    //Resources
    RESOURCE_DEVICES("device.devices", "Devices"),
    RESOURCE_DEVICES_DESCRIPTION("device.devices.description", "Manage devices"),
    RESOURCE_DEVICE_DATA("deviceData.deviceData", "Device data"),
    RESOURCE_DEVICE_DATA_DESCRIPTION("deviceData.deviceData.description", "Manage device data"),
    RESOURCE_DEVICE_COMMUNICATIONS("deviceCommunication.deviceCommunications", "Device communications"),
    RESOURCE_DEVICE_COMMUNICATIONS_DESCRIPTION("deviceCommunication.deviceCommunications.description", "Manage device communications"),
    RESOURCE_DEVICE_GROUPS("deviceGroup.deviceGroups", "Device groups"),
    RESOURCE_DEVICE_GROUPS_DESCRIPTION("deviceGroup.deviceGroups.description", "Manage device groups"),
    RESOURCE_INVENTORY_MANAGEMENT("inventoryManagement.inventoryManagements", "Assets inventory"),
    RESOURCE_INVENTORY_MANAGEMENT_DESCRIPTION("inventoryManagement.inventoryManagements.description", "Manage assets inventory"),
    RESOURCE_DATA_COLLECTION_KPI("dataCollectionKpi.dataCollectionKpis", "Data collection KPI"),
    RESOURCE_DATA_COLLECTION_KPI_DESCRIPTION("dataCollectionKpi.dataCollectionKpis.description", "Manage data collection KPI"),

    //Privileges
    ADMINISTRATE_DEVICE(Constants.ADMINISTRATE_DEVICE, "Administrate"),
    VIEW_DEVICE(Constants.VIEW_DEVICE, "View"),
    REMOVE_DEVICE(Constants.REMOVE_DEVICE, "Remove"),
    ADMINISTRATE_DEVICE_DATA(Constants.ADMINISTRATE_DEVICE_DATA, "Administrate"),
    ADMINISTRATE_DEVICE_COMMUNICATION(Constants.ADMINISTRATE_DEVICE_COMMUNICATION, "Administrate"),
    OPERATE_DEVICE_COMMUNICATION(Constants.OPERATE_DEVICE_COMMUNICATION, "Operate"),
    ADMINISTRATE_DEVICE_GROUP(Constants.ADMINISTRATE_DEVICE_GROUP, "Administrate"),
    ADMINISTRATE_DEVICE_ENUMERATED_GROUP(Constants.ADMINISTRATE_DEVICE_ENUMERATED_GROUP, "Administrate static device groups"),
    VIEW_DEVICE_GROUP_DETAIL(Constants.VIEW_DEVICE_GROUP_DETAIL, "View detail"),
    IMPORT_INVENTORY_MANAGEMENT(Constants.IMPORT_INVENTORY_MANAGEMENT, "Import"),
    REVOKE_INVENTORY_MANAGEMENT(Constants.REVOKE_INVENTORY_MANAGEMENT, "Revoke"),
    ADMINISTRATE_DEVICE_ATTRIBUTE(Constants.ADMINISTRATE_DEVICE_ATTRIBUTE, "Administrate attribute"),
    ADMINISTER_DECOMMISSIONED_DEVICE_DATA(Constants.ADMINISTER_DECOMMISSIONED_DEVICE_DATA, "Administrate decomissioned device data"),
    VIEW_DATA_COLLECTION_KPI(Constants.VIEW_DATA_COLLECTION_KPI, "View data collection KPI"),
    ADMINISTER_DATA_COLLECTION_KPI(Constants.ADMINISTER_DATA_COLLECTION_KPI, "Administrate data collection KPI"),
    ADMINISTER_DEVICE_TIME_SLICED_CPS(Constants.ADMINISTER_DEVICE_TIME_SLICED_CPS, "Administer device time-sliced CAS"),
    ;

    private final String key;
    private final String description;

    Privileges(String key, String description) {
        this.key = key;
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return getDescription();
    }

    public String getDescription() {
        return description;
    }

    public static String[] keys() {
        return Arrays.stream(Privileges.values())
                .map(Privileges::getKey)
                .toArray(String[]::new);
    }

    public interface Constants {
        String ADMINISTRATE_DEVICE = "privilege.administrate.device";
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
        String ADMINISTER_DECOMMISSIONED_DEVICE_DATA = "privilege.administer.decommissioned.deviceData";

        String VIEW_DATA_COLLECTION_KPI = "privilege.view.dataCollectionKpi";
        String ADMINISTER_DATA_COLLECTION_KPI = "privilege.administer.dataCollectionKpi";

        String ADMINISTER_DEVICE_TIME_SLICED_CPS = "privilege.administer.device.time.sliced.cps";
    }
}
