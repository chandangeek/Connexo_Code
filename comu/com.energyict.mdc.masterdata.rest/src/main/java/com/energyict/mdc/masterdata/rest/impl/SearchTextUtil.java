package com.energyict.mdc.masterdata.rest.impl;

import com.elster.jupiter.metering.ReadingTypeFilter;
import com.elster.jupiter.util.conditions.Condition;

import java.util.Optional;

class SearchTextUtil {

    static ReadingTypeFilter getFilter(String searchText) {
        ReadingTypeFilter filter = new ReadingTypeFilter();
        filter.addCondition(getCondition(searchText).orElse(Condition.TRUE));
        return filter;
    }

    static Optional<Condition> getCondition(String text) {
        return (text == null || text.isEmpty()) ? Optional.empty() : Optional.of(ReadingTypeConditionUtil.searchTextMatch(text));
    }

}
