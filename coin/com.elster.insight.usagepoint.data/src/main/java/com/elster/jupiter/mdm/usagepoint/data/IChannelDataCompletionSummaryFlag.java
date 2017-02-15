/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

public interface IChannelDataCompletionSummaryFlag extends TranslationKey {

    default String getDisplayName(Thesaurus thesaurus) {
        return thesaurus.getFormat(this).format();
    };
}
