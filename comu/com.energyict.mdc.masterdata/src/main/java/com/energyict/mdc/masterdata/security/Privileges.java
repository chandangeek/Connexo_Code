package com.energyict.mdc.masterdata.security;

/**
 * Created by bvn on 9/22/14.
 */
import com.elster.jupiter.nls.TranslationKey;
import java.util.Arrays;
import java.util.stream.Collectors;

public enum Privileges implements TranslationKey {

    VIEW_LOAD_PROFILE_TYPE(Constants.VIEW_LOAD_PROFILE_TYPE, "View Load Profile");

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
        public static final String VIEW_LOAD_PROFILE_TYPE = "x";
    }
}
