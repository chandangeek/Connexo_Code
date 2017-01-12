package com.elster.jupiter.mdm.usagepoint.data;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

public interface IChannelDataValidationSummaryFlag extends TranslationKey {

    default String getDisplayName(Thesaurus thesaurus) {
        return thesaurus.getFormat(this).format();
    };
}
