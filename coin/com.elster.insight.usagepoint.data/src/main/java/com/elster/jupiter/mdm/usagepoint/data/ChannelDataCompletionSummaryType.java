/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data;

public enum ChannelDataCompletionSummaryType implements IChannelDataCompletionSummaryFlag {
    GENERAL("statisticsGeneral", "General"),
    EDITED("statisticsEdited", "Edited"),
    ESTIMATED("statisticsEstimated", "Estimated"),
    VALID("statisticsValid", "Valid");

    private String key, translation;

    ChannelDataCompletionSummaryType(String key, String translation) {
        this.key = key;
        this.translation = translation;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return translation;
    }
}
