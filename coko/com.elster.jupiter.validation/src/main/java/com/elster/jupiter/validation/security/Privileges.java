package com.elster.jupiter.validation.security;

public interface Privileges {

    String ADMINISTRATE_VALIDATION_CONFIGURATION = "privilege.administrate.validationConfiguration";
    String VIEW_VALIDATION_CONFIGURATION = "privilege.view.validationConfiguration";

    String VALIDATE_MANUAL = "privilege.view.validateManual";

    String FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE = "privilege.view.fineTuneValidationConfiguration.onDevice";
    String FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE_CONFIGURATION = "privilege.view.fineTuneValidationConfiguration.onDeviceConfiguration";

}
