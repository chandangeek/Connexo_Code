package com.elster.jupiter.metering.config;

import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyValue;

public interface UsagePointRequirement {

    SearchableProperty getSearchableProperty();

    SearchablePropertyValue.ValueBean toValueBean();
}
