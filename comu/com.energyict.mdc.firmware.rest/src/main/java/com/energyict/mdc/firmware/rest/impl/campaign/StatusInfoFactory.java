/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl.campaign;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.servicecall.DefaultState;
import com.energyict.mdc.firmware.rest.impl.TranslationKeys;

public class StatusInfoFactory {
    public static IdWithNameInfo getDeviceStatus(DefaultState defaultState, Thesaurus thesaurus) {
        switch (defaultState) {
            case REJECTED:
                return new IdWithNameInfo(defaultState.name(), thesaurus.getFormat(TranslationKeys.STATUS_CONFIGURATION_ERROR).format());
            case ONGOING:
                return new IdWithNameInfo(defaultState.name(), thesaurus.getFormat(TranslationKeys.STATUS_ONGOIND).format());
            case CANCELLED:
                return new IdWithNameInfo(defaultState.name(), thesaurus.getFormat(TranslationKeys.STATUS_CANCELED).format());
            case PENDING:
                return new IdWithNameInfo(defaultState.name(), thesaurus.getFormat(TranslationKeys.STATUS_PENDING).format());
            case FAILED:
                return new IdWithNameInfo(defaultState.name(), thesaurus.getFormat(TranslationKeys.STATUS_FAILED).format());
            case SUCCESSFUL:
                return new IdWithNameInfo(defaultState.name(), thesaurus.getFormat(TranslationKeys.STATUS_SUCCESSFUL).format());
            default:
                return new IdWithNameInfo(defaultState.name(), defaultState.getDisplayName(thesaurus));
        }
    }

    public static IdWithNameInfo getCampaignStatus(DefaultState defaultState, Thesaurus thesaurus) {
        switch (defaultState) {
            case SUCCESSFUL:
                return new IdWithNameInfo(defaultState.name(), thesaurus.getFormat(TranslationKeys.STATUS_COMPLETED).format());
            case ONGOING:
                return new IdWithNameInfo(defaultState.name(), thesaurus.getFormat(TranslationKeys.STATUS_ONGOIND).format());
            case CANCELLED:
                return new IdWithNameInfo(defaultState.name(), thesaurus.getFormat(TranslationKeys.STATUS_CANCELED).format());
            default:
                return new IdWithNameInfo(defaultState.name(), defaultState.getDisplayName(thesaurus));
        }
    }
}
