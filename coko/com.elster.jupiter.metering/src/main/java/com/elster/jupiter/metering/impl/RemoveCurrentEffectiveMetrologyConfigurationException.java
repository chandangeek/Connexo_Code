/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Created by akuryuk on 21.07.2016.
 */
public class RemoveCurrentEffectiveMetrologyConfigurationException extends LocalizedException {
    protected RemoveCurrentEffectiveMetrologyConfigurationException(Thesaurus thesaurus) {
        super(thesaurus, PrivateMessageSeeds.CURRENT_EFFECTIVE_METROLOGY_CONFIG_CANT_BE_REMOVED);
    }
}