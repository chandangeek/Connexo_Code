package com.elster.jupiter.mdm.usagepoint.data;

import aQute.bnd.annotation.ProviderType;

import java.util.Map;

@ProviderType
public interface ChannelDataValidationSummary {
    Map<ChannelDataValidationSummaryFlag, Integer> getValues();

    int getSum();
}
