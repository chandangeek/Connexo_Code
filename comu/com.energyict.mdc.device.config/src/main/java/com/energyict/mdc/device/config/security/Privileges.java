package com.energyict.mdc.device.config.security;

import com.elster.jupiter.nls.TranslationKey;
import java.util.Arrays;
import java.util.stream.Collectors;

public enum Privileges implements TranslationKey {

    ADMINISTRATE_MASTER_DATA(Constants.ADMINISTRATE_MASTER_DATA, "Administrate"),
    VIEW_MASTER_DATA(Constants.VIEW_MASTER_DATA, "View"),
    VIEW_DEVICE_SECURITY_PROPERTIES_1(Constants.VIEW_DEVICE_SECURITY_PROPERTIES_1, "View level 1"),
    VIEW_DEVICE_SECURITY_PROPERTIES_2(Constants.VIEW_DEVICE_SECURITY_PROPERTIES_2, "View level 2"),
    VIEW_DEVICE_SECURITY_PROPERTIES_3(Constants.VIEW_DEVICE_SECURITY_PROPERTIES_3, "View level 3"),
    VIEW_DEVICE_SECURITY_PROPERTIES_4(Constants.VIEW_DEVICE_SECURITY_PROPERTIES_4, "View level 4"),
    EDIT_DEVICE_SECURITY_PROPERTIES_1(Constants.EDIT_DEVICE_SECURITY_PROPERTIES_1, "Edit level 1"),
    EDIT_DEVICE_SECURITY_PROPERTIES_2(Constants.EDIT_DEVICE_SECURITY_PROPERTIES_2, "Edit level 2"),
    EDIT_DEVICE_SECURITY_PROPERTIES_3(Constants.EDIT_DEVICE_SECURITY_PROPERTIES_3, "Edit level 3"),
    EDIT_DEVICE_SECURITY_PROPERTIES_4(Constants.EDIT_DEVICE_SECURITY_PROPERTIES_4, "Edit level 4"),
    EXECUTE_COM_TASK_1(Constants.EXECUTE_COM_TASK_1, "Execute com task (level 1)"),
    EXECUTE_COM_TASK_2(Constants.EXECUTE_COM_TASK_2, "Execute com task (level 2)"),
    EXECUTE_COM_TASK_3(Constants.EXECUTE_COM_TASK_3, "Execute com task (level 3)"),
    EXECUTE_COM_TASK_4(Constants.EXECUTE_COM_TASK_4, "Execute com task (level 4)"),
    EXECUTE_DEVICE_MESSAGE_1(Constants.EXECUTE_DEVICE_MESSAGE_1, "Execute level 1"),
    EXECUTE_DEVICE_MESSAGE_2(Constants.EXECUTE_DEVICE_MESSAGE_2, "Execute level 2"),
    EXECUTE_DEVICE_MESSAGE_3(Constants.EXECUTE_DEVICE_MESSAGE_3, "Execute level 3"),
    EXECUTE_DEVICE_MESSAGE_4(Constants.EXECUTE_DEVICE_MESSAGE_4, "Execute level 4");


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
        String ADMINISTRATE_MASTER_DATA = "privilege.administrate.masterData";
        String VIEW_MASTER_DATA = "privilege.view.masterData";

        String ADMINISTRATE_DEVICE_TYPE = "privilege.administrate.deviceType";
        String VIEW_DEVICE_TYPE = "privilege.view.deviceType";

        String VIEW_DEVICE_SECURITY_PROPERTIES_1 = "view.device.security.properties.level1";
        String VIEW_DEVICE_SECURITY_PROPERTIES_2 = "view.device.security.properties.level2";
        String VIEW_DEVICE_SECURITY_PROPERTIES_3 = "view.device.security.properties.level3";
        String VIEW_DEVICE_SECURITY_PROPERTIES_4 = "view.device.security.properties.level4";

        String EDIT_DEVICE_SECURITY_PROPERTIES_1 = "edit.device.security.properties.level1";
        String EDIT_DEVICE_SECURITY_PROPERTIES_2 = "edit.device.security.properties.level2";
        String EDIT_DEVICE_SECURITY_PROPERTIES_3 = "edit.device.security.properties.level3";
        String EDIT_DEVICE_SECURITY_PROPERTIES_4 = "edit.device.security.properties.level4";

        String EXECUTE_COM_TASK_1 = "execute.com.task.level1";
        String EXECUTE_COM_TASK_2 = "execute.com.task.level2";
        String EXECUTE_COM_TASK_3 = "execute.com.task.level3";
        String EXECUTE_COM_TASK_4 = "execute.com.task.level4";

        String EXECUTE_DEVICE_MESSAGE_1 = "execute.device.message.level1";
        String EXECUTE_DEVICE_MESSAGE_2 = "execute.device.message.level2";
        String EXECUTE_DEVICE_MESSAGE_3 = "execute.device.message.level3";
        String EXECUTE_DEVICE_MESSAGE_4 = "execute.device.message.level4";
    }
}
