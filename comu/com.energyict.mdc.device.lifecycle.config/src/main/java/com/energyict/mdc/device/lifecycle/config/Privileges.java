package com.energyict.mdc.device.lifecycle.config;

/**
 * Models the privileges of the device life cycle bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-11 (11:02)
 */
import com.elster.jupiter.nls.TranslationKey;
import java.util.Arrays;
import java.util.stream.Collectors;

public enum Privileges implements TranslationKey {

    VIEW_DEVICE_LIFE_CYCLE(Constants.VIEW_DEVICE_LIFE_CYCLE, "View"),
    CONFIGURE_DEVICE_LIFE_CYCLE(Constants.CONFIGURE_DEVICE_LIFE_CYCLE, "Administrate"),
    INITIATE_ACTION_1(Constants.INITIATE_ACTION_1, "Initiate level 1"),
    INITIATE_ACTION_2(Constants.INITIATE_ACTION_2, "Initiate level 2"),
    INITIATE_ACTION_3(Constants.INITIATE_ACTION_3, "Initiate level 3"),
    INITIATE_ACTION_4(Constants.INITIATE_ACTION_4, "Initiate level 4");

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
        public String VIEW_DEVICE_LIFE_CYCLE = "privilege.view.deviceLifeCycle";
        public String CONFIGURE_DEVICE_LIFE_CYCLE = "privilege.configure.deviceLifeCycle";
        public String INITIATE_ACTION_1 = "device.lifecycle.initiate.action.level1";
        public String INITIATE_ACTION_2 = "device.lifecycle.initiate.action.level2";
        public String INITIATE_ACTION_3 = "device.lifecycle.initiate.action.level3";
        public String INITIATE_ACTION_4 = "device.lifecycle.initiate.action.level4";
    }
}