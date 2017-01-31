/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;

public class CampaignForComTaskExecutionExceptions {

    static final String NO_CAMPAIGN_FOUND_FOR_COMTASKEXECUTION = "NoCampaignFoundForComtaskExecutionX";
    static final String CAMPAIGN_NOT_UNAMBIGOUSLY_DETERMINED_FOR_COMTASKEXECUTION = "CampaignCouldNotBeDeterminedUnambiguouslyForComtaskExecutionX";

    public static LocalizedException campaignNotFound(Thesaurus thesaurus, ComTaskExecution comTaskExecution){
        return new InvalidCampaignForComTaskExecutionException(thesaurus, MessageSeeds.NOT_FOUND_CAMPAIGN_FOR_COMTASK_EXECUTION, comTaskExecution);
    }

    public static LocalizedException campaignNotUnambiguouslyDefined(Thesaurus thesaurus, ComTaskExecution comTaskExecution){
        return new InvalidCampaignForComTaskExecutionException(thesaurus, MessageSeeds.NO_CAMPAIGN_UNAMBIGUOUSLY_DETERMINED_FOR_COMTASK_EXECUTION, comTaskExecution);
    }

    private static class InvalidCampaignForComTaskExecutionException extends LocalizedException{
        InvalidCampaignForComTaskExecutionException(Thesaurus thesaurus, MessageSeed messageSeed, ComTaskExecution comTaskExecution){
            super(thesaurus, messageSeed, comTaskExecution.getId());
        }
    }

}
