package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.ReadingTypeFilter;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.conditions.Where;

import java.util.Arrays;

public class ReadingTypeFilterFactory {

    public static ReadingTypeFilter from(JsonQueryFilter jsonQueryFilter) {

        ReadingTypeFilter filter = new ReadingTypeFilter();

        if (jsonQueryFilter.hasProperty("fullAliasName")) {
            filter.addFullAliasNameCondition(jsonQueryFilter.getString("fullAliasName"));
        }

        if (jsonQueryFilter.hasProperty("active")) {
            filter.addActiveCondition(jsonQueryFilter.getBoolean("active"));
        }

        Arrays.stream(ReadingTypeFilter.ReadingTypeFields.values()).filter(e -> jsonQueryFilter.hasProperty(e.getName()))
                .forEach(e -> filter.addCodedValueCondition(e.getName(), jsonQueryFilter.getPropertyList(e.getName())));

        return filter;
    }

}
