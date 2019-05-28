/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl.campaign;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.DefaultState;
import com.energyict.mdc.firmware.rest.impl.TranslationKeys;

public class CampaignRestUtil {
    public static String getDeviceStatus(DefaultState defaultState, Thesaurus thesaurus) {
        switch (defaultState) {
            case REJECTED:
                return thesaurus.getFormat(TranslationKeys.STATUS_CONFIGURATION_ERROR).format();
            default:
                return defaultState.getDisplayName(thesaurus);
        }
    }

    public static String getCampaignStatus(DefaultState defaultState, Thesaurus thesaurus) {
        switch (defaultState) {
            case SUCCESSFUL:
                return thesaurus.getFormat(TranslationKeys.STATUS_COMPLETED).format();
            default:
                return defaultState.getDisplayName(thesaurus);
        }
    }
}
