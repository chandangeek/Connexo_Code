package com.elster.jupiter.metering.config;

import com.elster.jupiter.search.SearchablePropertyValue;

public interface UsagePointRequirement {
    String getSearchablePropertyName();

    SearchablePropertyValue.ValueBean toValueBean();
}
