package com.energyict.mdc.autoreschedule.security;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.autoreschedule.impl.AutoRescheduleTaskFactory;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum Privileges implements TranslationKey {
    //Resources
    RESOURCE_RETRY_FAILED_COMTASKS(AutoRescheduleTaskFactory.NAME, "Retry failed communication tasks"),
    RESOURCE_RETRY_FAILED_COMTASKS_DESCRIPTION(AutoRescheduleTaskFactory.NAME + ".description", "Manage Retry failed communication tasks"),

    //Privileges
    ADMINISTRATE_RETRY_FAILED_COMTASKS(Constants.ADMINISTRATE_RETRY_FAILED_COMTASKS, "Administrate"),
    VIEW_RETRY_FAILED_COMTASKS(Constants.VIEW_RETRY_FAILED_COMTASKS, "View"),
    RUN_RETRY_FAILED_COMTASKS(Constants.RUN_RETRY_FAILED_COMTASKS, "Run");

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
        String ADMINISTRATE_RETRY_FAILED_COMTASKS = "privilege.administrate." + AutoRescheduleTaskFactory.NAME;
        String VIEW_RETRY_FAILED_COMTASKS = "privilege.view." + AutoRescheduleTaskFactory.NAME;
        String RUN_RETRY_FAILED_COMTASKS = "privilege.run." + AutoRescheduleTaskFactory.NAME;
    }
}
