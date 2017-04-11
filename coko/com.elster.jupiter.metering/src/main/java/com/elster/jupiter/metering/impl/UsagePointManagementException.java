/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.List;

public class UsagePointManagementException extends com.elster.jupiter.metering.UsagePointManagementException {

    private UsagePointManagementException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

    public static UsagePointManagementException incorrectApplyTime(Thesaurus thesaurus, String installationTime) {
        return new UsagePointManagementException(thesaurus, PrivateMessageSeeds.METROLOGY_CONFIG_INVALID_START_DATE, installationTime);
    }

    public static UsagePointManagementException incorrectMetrologyConfigStartDate(Thesaurus thesaurus, String metroloyConfigurationName, String usagePointName, String when) {
        return new UsagePointManagementException(thesaurus, PrivateMessageSeeds.METROLOGY_CONFIGURATION_INVALID_START_DATE, metroloyConfigurationName, usagePointName, when);
    }

    public static UsagePointManagementException incorrectMetersSpecification(Thesaurus thesaurus, List<String> meterRoles) {
        return new UsagePointManagementException(thesaurus, PrivateMessageSeeds.METERS_ARE_NOT_SPECIFIED_FOR_CONFIGURATION, meterRoles);
    }

    public static UsagePointManagementException incorrectEndDeviceStage(Thesaurus thesaurus, String when) {
        return new UsagePointManagementException(thesaurus, PrivateMessageSeeds.INVALID_END_DEVICE_STAGE, when);
    }

}