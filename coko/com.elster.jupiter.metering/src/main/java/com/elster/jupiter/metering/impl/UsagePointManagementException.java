/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.List;

public class UsagePointManagementException extends UsagePointMeterActivationException {

    private UsagePointManagementException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

    public static UsagePointManagementException incorrectStage(Thesaurus thesaurus) {
        return new UsagePointManagementException(thesaurus, PrivateMessageSeeds.USAGE_POINT_INCORRECT_STAGE);
    }

    public static UsagePointManagementException incorrectApplyTime(Thesaurus thesaurus, String installationTime) {
        return new UsagePointManagementException(thesaurus, MessageSeeds.METROLOGY_CONFIG_INVALID_START_DATE, installationTime);
    }

    public static UsagePointManagementException incorrectMetrologyConfigStartDate(Thesaurus thesaurus, String metroloyConfigurationName, String usagePointName, String when) {
        return new UsagePointManagementException(thesaurus, MessageSeeds.METROLOGY_CONFIGURATION_INVALID_START_DATE, metroloyConfigurationName, usagePointName, when);
    }

    public static UsagePointManagementException incorrectMeterActivationRequirements(Thesaurus thesaurus, List<String> purposes) {
        return new UsagePointManagementException(thesaurus, MessageSeeds.METER_ACTIVATION_INVALID_REQUIREMENTS, purposes);
    }

    public static UsagePointManagementException incorrectMetersSpecification(Thesaurus thesaurus, List<String> meterRoles) {
        return new UsagePointManagementException(thesaurus, MessageSeeds.METERS_ARE_NOT_SPECIFIED_FOR_CONFIGURATION, meterRoles);
    }

    public static UsagePointManagementException incorrectEndDeviceStage(Thesaurus thesaurus, String when) {
        return new UsagePointManagementException(thesaurus, MessageSeeds.INVALID_END_DEVICE_STAGE, when);
    }
}
