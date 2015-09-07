package com.elster.jupiter.validation.security;

import com.elster.jupiter.nls.TranslationKey;
import java.util.Arrays;
import java.util.stream.Collectors;


public enum Privileges implements TranslationKey {

    ADMINISTRATE_VALIDATION_CONFIGURATION(Constants.ADMINISTRATE_VALIDATION_CONFIGURATION, "Administrate"),
    VIEW_VALIDATION_CONFIGURATION(Constants.VIEW_VALIDATION_CONFIGURATION, "View"),
    VALIDATE_MANUAL(Constants.VALIDATE_MANUAL, "Validate manual"),
    FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE(Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE, "Finetune validation configuration on device"),
    FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE_CONFIGURATION(Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE_CONFIGURATION, "Finetune validation configuration on device configuration");


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
        String ADMINISTRATE_VALIDATION_CONFIGURATION = "privilege.administrate.validationConfiguration";
        String VIEW_VALIDATION_CONFIGURATION = "privilege.view.validationConfiguration";
        String VALIDATE_MANUAL = "privilege.view.validateManual";
        String FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE = "privilege.view.fineTuneValidationConfiguration.onDevice";
        String FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE_CONFIGURATION = "privilege.view.fineTuneValidationConfiguration.onDeviceConfiguration";

    }
}
