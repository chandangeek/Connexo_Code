/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.impl.PrivateMessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class CannotDeleteMetrologyPurposeException extends LocalizedException {

    public CannotDeleteMetrologyPurposeException(Thesaurus thesaurus, String metrologyPurposeName) {
        super(thesaurus, PrivateMessageSeeds.CAN_NOT_DELETE_METROLOGY_PURPOSE_IN_USE, metrologyPurposeName);
    }

}