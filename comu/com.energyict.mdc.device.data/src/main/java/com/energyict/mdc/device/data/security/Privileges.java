package com.energyict.mdc.device.data.security;

import com.elster.jupiter.nls.TranslationKey;
import java.util.Arrays;
import java.util.stream.Collectors;

public enum Privileges implements TranslationKey {

    ADD_DEVICE(Constants.ADD_DEVICE, "Add"),
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
    ADMINISTER_DECOMMISSIONED_DEVICE_DATA(Constants.ADMINISTER_DECOMMISSIONED_DEVICE_DATA, "Administrate decomissioned device data");


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
                .collect(Collectors.toList())
                .toArray(new String[Privileges.values().length]);
    }

    public interface Constants {
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
        String ADMINISTER_DECOMMISSIONED_DEVICE_DATA = "privilege.administer.decommissioned.deviceData";
    }
}
