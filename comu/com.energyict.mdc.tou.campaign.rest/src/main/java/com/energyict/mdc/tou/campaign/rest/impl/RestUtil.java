/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.DefaultState;

public class RestUtil {
    public static String getDeviceStatus(DefaultState defaultState, Thesaurus thesaurus) {
        switch (defaultState) {
            case REJECTED:
                return thesaurus.getString(TranslationKeys.STATUS_CONFIGURATION_ERROR.getKey(), TranslationKeys.STATUS_CONFIGURATION_ERROR.getDefaultFormat());
            default:
                return defaultState.getDisplayName(thesaurus);
        }
    }

    public static String getCampaignStatus(DefaultState defaultState, Thesaurus thesaurus) {
        switch (defaultState) {
            case SUCCESSFUL:
                return thesaurus.getString(TranslationKeys.STATUS_COMPLETED.getKey(), TranslationKeys.STATUS_COMPLETED.getDefaultFormat());
            default:
                return defaultState.getDisplayName(thesaurus);
        }
    }
}
