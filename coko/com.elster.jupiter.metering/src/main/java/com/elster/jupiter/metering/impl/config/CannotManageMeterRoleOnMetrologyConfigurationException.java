/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

public class CannotManageMeterRoleOnMetrologyConfigurationException extends LocalizedException {

    private CannotManageMeterRoleOnMetrologyConfigurationException(Thesaurus thesaurus, MessageSeed messageSeed, String... args) {
        super(thesaurus, messageSeed, args);
    }

    public static CannotManageMeterRoleOnMetrologyConfigurationException canNotDeleteMeterRoleFromMetrologyConfiguration(Thesaurus thesaurus, String meterRoleName, String metrologyConfigurationName) {
        return new CannotManageMeterRoleOnMetrologyConfigurationException(thesaurus, MessageSeeds.CAN_NOT_DELETE_METER_ROLE_FROM_METROLOGY_CONFIGURATION, meterRoleName, metrologyConfigurationName);
    }

    public static CannotManageMeterRoleOnMetrologyConfigurationException canNotAddMeterRoleWhichIsNotAssignedToServiceCategory(Thesaurus thesaurus, String meterRoleName, String serviceCategoryName) {
        return new CannotManageMeterRoleOnMetrologyConfigurationException(thesaurus, MessageSeeds.CAN_NOT_ADD_METER_ROLE_TO_METROLOGY_CONFIGURATION, meterRoleName, serviceCategoryName);
    }

    public static CannotManageMeterRoleOnMetrologyConfigurationException canNotAddRequirementWithThatMeterRole(Thesaurus thesaurus, String meterRoleName, String metrologyConfigurationName) {
        return new CannotManageMeterRoleOnMetrologyConfigurationException(thesaurus, MessageSeeds.CAN_NOT_ADD_METER_ROLE_TO_METROLOGY_CONFIGURATION, meterRoleName, metrologyConfigurationName);
    }
}
