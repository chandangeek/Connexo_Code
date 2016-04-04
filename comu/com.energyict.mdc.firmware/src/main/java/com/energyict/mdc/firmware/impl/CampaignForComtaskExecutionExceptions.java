package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;

/**
 * Copyrights EnergyICT
 * Date: 4/04/2016
 * Time: 15:01
 */
public class CampaignForComtaskExecutionExceptions {

    public static LocalizedException campaigNotFound(Thesaurus thesaurus, ComTaskExecution comTaskExecution){
        return new CampaignForComTaskExecutionNotFoundException(thesaurus, comTaskExecution);
    }

    public static LocalizedException campaignNotUnambiguouslyDefined(Thesaurus thesaurus, ComTaskExecution comTaskExecution){
        return new CampaignForComTaskExecutionNotUnambiguouslayDeterminedException(thesaurus, comTaskExecution);
    }

    private static class CampaignForComTaskExecutionNotFoundException extends LocalizedException{
        CampaignForComTaskExecutionNotFoundException(Thesaurus thesaurus, ComTaskExecution comTaskExecution){
            super(thesaurus, MessageSeeds.NOT_FOUND_CAMPAIGN_FOR_COMTASK_EXECUTION, comTaskExecution.getId());
        }
    }

    private static class CampaignForComTaskExecutionNotUnambiguouslayDeterminedException extends LocalizedException{
        CampaignForComTaskExecutionNotUnambiguouslayDeterminedException(Thesaurus thesaurus, ComTaskExecution comTaskExecution){
            super(thesaurus, MessageSeeds.NO_CAMPAIGN_UNAMBIGUOUSLY_DETERMINED_FOR_COMTASK_EXECUTION, comTaskExecution.getId());
        }
    }
}
