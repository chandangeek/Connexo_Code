/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.security;

import com.elster.jupiter.nls.TranslationKey;

import java.util.Arrays;

import static com.elster.jupiter.metering.security.Privileges.Constants.ADMINISTRATE_ZONE;
import static com.elster.jupiter.metering.security.Privileges.Constants.VIEW_ZONE;

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
    RESOURCE_CRL_REQUEST("crlRequest.crlRequests", "CRL request"),
    RESOURCE_CRL_REQUEST_DESCRIPTION("crlRequest.crlRequests.description", "Manage CRL request"),
    RESOURCE_DEVICE_ZONES("deviceZone.deviceZones", "Device zones"),
    RESOURCE_DEVICE_ZONES_DESCRIPTION("deviceZone.deviceZones.description", "Manage device zones"),
    //  RESOURCE_AUDIT_LOG("privilege.view.auditLog.resource", "Audit log"),
    //  RESOURCE_AUDIT_LOG_DESCRIPTION("privilege.view.auditLog.description", "Manage audit log"),

    //Privileges
    ADMINISTRATE_DEVICE(Constants.ADMINISTRATE_DEVICE, "Administrate"),
    VIEW_DEVICE(Constants.VIEW_DEVICE, "View"),
    REMOVE_DEVICE(Constants.REMOVE_DEVICE, "Remove"),
    ADMINISTRATE_DEVICE_DATA(Constants.ADMINISTRATE_DEVICE_DATA, "Administrate"),
    ADMINISTRATE_DEVICE_COMMUNICATION(Constants.ADMINISTRATE_DEVICE_COMMUNICATION, "Administrate"),
    OPERATE_DEVICE_COMMUNICATION(Constants.OPERATE_DEVICE_COMMUNICATION, "Operate"),
    RUN_WITH_PRIO(Constants.RUN_WITH_PRIO, "Run with priority"),
    ADMINISTRATE_DEVICE_GROUP(Constants.ADMINISTRATE_DEVICE_GROUP, "Administrate"),
    ADMINISTRATE_DEVICE_ENUMERATED_GROUP(Constants.ADMINISTRATE_DEVICE_ENUMERATED_GROUP, "Administrate static device groups"),
    VIEW_DEVICE_GROUP_DETAIL(Constants.VIEW_DEVICE_GROUP_DETAIL, "View detail"),
    IMPORT_INVENTORY_MANAGEMENT(Constants.IMPORT_INVENTORY_MANAGEMENT, "Import"),
    REVOKE_INVENTORY_MANAGEMENT(Constants.REVOKE_INVENTORY_MANAGEMENT, "Revoke"),
    ADMINISTRATE_DEVICE_ATTRIBUTE(Constants.ADMINISTRATE_DEVICE_ATTRIBUTE, "Administrate attribute"),
    ADMINISTER_DECOMMISSIONED_DEVICE_DATA(Constants.ADMINISTER_DECOMMISSIONED_DEVICE_DATA, "Administrate decomissioned device data"),
    VIEW_CRL_REQUEST(Constants.VIEW_CRL_REQUEST, "View CRL request"),
    ADMINISTER_CRL_REQUEST(Constants.ADMINISTER_CRL_REQUEST, "Administrate CRL request"),
    VIEW_DATA_COLLECTION_KPI(Constants.VIEW_DATA_COLLECTION_KPI, "View data collection KPI"),
    ADMINISTER_DATA_COLLECTION_KPI(Constants.ADMINISTER_DATA_COLLECTION_KPI, "Administrate data collection KPI"),
    ADMINISTER_DEVICE_TIME_SLICED_CPS(Constants.ADMINISTER_DEVICE_TIME_SLICED_CPS, "Administrate device time-sliced CAS"),
    ESTIMATE_WITH_RULE(Constants.ESTIMATE_WITH_RULE, "Estimate with rule"),
    EDIT_WITH_ESTIMATOR(Constants.EDIT_WITH_ESTIMATOR, "Edit with estimator"),
    ADMINISTER_VALIDATION_CONFIGURATION(Constants.ADMINISTER_VALIDATION_CONFIGURATION, "Administrate validation configuration"),
    ADMINISTER_ESTIMATION_CONFIGURATION(Constants.ADMINISTER_ESTIMATION_CONFIGURATION, "Administrate estimation configuration"),
    ADMINISTRATE_DEVICE_ZONE(ADMINISTRATE_ZONE, "Administrate"),
    VIEW_DEVICE_ZONE(VIEW_ZONE, "View"),
    VIEW_CUSTOM_PROPERTIES_LEVEL1(Constants.VIEW_CUSTOM_PROPERTIES_LEVEL1, "View"),
    VIEW_CUSTOM_PROPERTIES_LEVEL2(Constants.VIEW_CUSTOM_PROPERTIES_LEVEL2, "View"),
    VIEW_CUSTOM_PROPERTIES_LEVEL3(Constants.VIEW_CUSTOM_PROPERTIES_LEVEL3, "View"),
    VIEW_CUSTOM_PROPERTIES_LEVEL4(Constants.VIEW_CUSTOM_PROPERTIES_LEVEL4, "View")

    //   VIEW_AUDIT_LOG_DATA(VIEW_AUDIT_LOG, "View")
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
        String RUN_WITH_PRIO = "privilege.operate.deviceCommunication.runWithPriority";

        String ADMINISTRATE_DEVICE_GROUP = "privilege.administrate.deviceGroup";
        String ADMINISTRATE_DEVICE_ENUMERATED_GROUP = "privilege.administrate.deviceOfEnumeratedGroup";
        String VIEW_DEVICE_GROUP_DETAIL = "privilege.view.deviceGroupDetail";

        String IMPORT_INVENTORY_MANAGEMENT = "privilege.import.inventoryManagement";
        String REVOKE_INVENTORY_MANAGEMENT = "privilege.revoke.inventoryManagement";

        String ADMINISTRATE_DEVICE_ATTRIBUTE = "privilege.administrate.attribute.device";
        String ADMINISTER_DECOMMISSIONED_DEVICE_DATA = "privilege.administer.decommissioned.deviceData";

        String VIEW_DATA_COLLECTION_KPI = "privilege.view.dataCollectionKpi";
        String ADMINISTER_DATA_COLLECTION_KPI = "privilege.administer.dataCollectionKpi";

        String VIEW_CRL_REQUEST = "privilege.view.crlRequest";
        String ADMINISTER_CRL_REQUEST = "privilege.administer.crlRequest";

        String ADMINISTER_DEVICE_TIME_SLICED_CPS = "privilege.administer.device.time.sliced.cps";

        String ESTIMATE_WITH_RULE = "privilege.estimate.with.rule";
        String EDIT_WITH_ESTIMATOR = "privilege.edit.with.estimator";

        String ADMINISTER_VALIDATION_CONFIGURATION = "privilege.administer.device.validationConfiguration";
        String ADMINISTER_ESTIMATION_CONFIGURATION = "privilege.administer.device.estimationConfiguration";

        String ADMINISTRATE_ZONE = "privilege.administrate.zone";

        String VIEW_CUSTOM_PROPERTIES_LEVEL1 = "view.custom.properties.level1";
        String VIEW_CUSTOM_PROPERTIES_LEVEL2 = "view.custom.properties.level2";
        String VIEW_CUSTOM_PROPERTIES_LEVEL3 = "view.custom.properties.level3";
        String VIEW_CUSTOM_PROPERTIES_LEVEL4 = "view.custom.properties.level4";


    }
}
