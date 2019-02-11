/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.DefaultState;

public class RestUtil {
    public static String getStatus(DefaultState defaultState, Thesaurus thesaurus) {
        switch (defaultState) {
            case REJECTED:
                return thesaurus.getString(TranslationKeys.STATUS_CONFIGURATION_ERROR.getKey(), TranslationKeys.STATUS_CONFIGURATION_ERROR.getDefaultFormat());
            default:
                return defaultState.getDisplayName(thesaurus);
        }
    }
}
