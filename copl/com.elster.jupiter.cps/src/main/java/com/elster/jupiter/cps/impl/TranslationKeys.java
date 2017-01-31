/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cps.impl;


import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {

    RANGE_OVERLAP_UPDATE_START(Keys.RANGE_OVERLAP_UPDATE_START, "Updated"),
    RANGE_OVERLAP_UPDATE_END(Keys.RANGE_OVERLAP_UPDATE_END, "Updated"),
    RANGE_OVERLAP_DELETE(Keys.RANGE_OVERLAP_DELETE, "Removed"),
    RANGE_GAP_BEFORE(Keys.RANGE_GAP_BEFORE, "Gap"),
    RANGE_GAP_AFTER(Keys.RANGE_GAP_AFTER, "Gap"),
    RANGE_INSERT(Keys.RANGE_INSERT, "Insert");

    private String key;
    private String defaultFormat;

    TranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

    public String getDisplayName(Thesaurus thesaurus) {
        return thesaurus.getFormat(this).format();
    }

    static final class Keys {
        public static final String RANGE_OVERLAP_UPDATE_START = "edit.historical.values.overlap.can.update.start";
        public static final String RANGE_OVERLAP_UPDATE_END = "edit.historical.values.overlap.can.update.end";
        public static final String RANGE_OVERLAP_DELETE = "edit.historical.values.overlap.can.delete";
        public static final String RANGE_GAP_BEFORE = "edit.historical.values.gap.before";
        public static final String RANGE_GAP_AFTER = "edit.historical.values.gap.after";
        public static final String RANGE_INSERT = "edit.historical.values.insert";
    }
}
