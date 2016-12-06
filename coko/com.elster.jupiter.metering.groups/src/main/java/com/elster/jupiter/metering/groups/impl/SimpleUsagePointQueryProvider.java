package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.spi.QueryProvider;
import com.elster.jupiter.search.SearchablePropertyCondition;
import com.elster.jupiter.util.conditions.Condition;

import org.osgi.service.component.annotations.Component;

import java.util.List;

import static com.elster.jupiter.util.conditions.Where.where;

@Component(
        name = "com.elster.jupiter.metering.groups.impl.SimpleUsagePointQueryProvider",
        service = {QueryProvider.class},
        property = "name=" + MeteringGroupsService.COMPONENTNAME,
        immediate = true)
public class SimpleUsagePointQueryProvider extends SimpleQueryProvider<UsagePoint> implements QueryProvider<UsagePoint> {

    public static final String SIMPLE_USAGE_POINT_QUERY_PROVIDER = SimpleUsagePointQueryProvider.class.getName();

    @Override
    public String getName() {
        return SIMPLE_USAGE_POINT_QUERY_PROVIDER;
    }

    @Override
    Condition wrapConditions(List<SearchablePropertyCondition> conditions) {
        return super.wrapConditions(conditions).and(where("obsoleteTime").isNull());
    }
}
