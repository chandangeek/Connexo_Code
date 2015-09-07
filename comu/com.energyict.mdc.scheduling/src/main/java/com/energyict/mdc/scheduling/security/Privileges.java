package com.energyict.mdc.scheduling.security;

import com.elster.jupiter.nls.TranslationKey;
import java.util.Arrays;
import java.util.stream.Collectors;

public enum Privileges implements TranslationKey {

    ADMINISTRATE_SHARED_COMMUNICATION_SCHEDULE(Constants.ADMINISTRATE_SHARED_COMMUNICATION_SCHEDULE, "Administrate"),
    VIEW_SHARED_COMMUNICATION_SCHEDULE(Constants.VIEW_SHARED_COMMUNICATION_SCHEDULE, "View");

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
        String ADMINISTRATE_SHARED_COMMUNICATION_SCHEDULE = "privilege.administrate.sharedCommunicationSchedule";
        String VIEW_SHARED_COMMUNICATION_SCHEDULE = "privilege.view.sharedCommunicationSchedule";
    }
}
