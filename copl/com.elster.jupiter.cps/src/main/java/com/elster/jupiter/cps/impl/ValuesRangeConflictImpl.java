/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cps.impl;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.ValuesRangeConflict;
import com.elster.jupiter.cps.ValuesRangeConflictType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;
import com.google.common.collect.Range;

import java.time.Instant;

public class ValuesRangeConflictImpl implements ValuesRangeConflict {

    private ValuesRangeConflictType conflictType;
    private Range<Instant> conflictingRange;
    private CustomPropertySetValues values;
    private TranslationKey translationKey;
    private Thesaurus thesaurus;

    public ValuesRangeConflictImpl(ValuesRangeConflictType conflictType, Range<Instant> conflictingRange, CustomPropertySetValues values, TranslationKey translationKey, Thesaurus thesaurus) {
        this.conflictType = conflictType;
        this.conflictingRange = conflictingRange;
        this.values = values;
        this.translationKey = translationKey;
        this.thesaurus = thesaurus;
    }

    @Override
    public ValuesRangeConflictType getType() {
        return conflictType;
    }

    @Override
    public String getMessage() {
        return thesaurus.getFormat(translationKey).format();
    }

    @Override
    public Range<Instant> getConflictingRange() {
        return conflictingRange;
    }

    @Override
    public CustomPropertySetValues getValues() {
        return values;
    }
}
