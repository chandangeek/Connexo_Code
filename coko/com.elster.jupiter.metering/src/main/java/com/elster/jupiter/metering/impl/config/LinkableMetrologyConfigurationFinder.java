/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.conditions.*;
import com.elster.jupiter.util.sql.SqlFragment;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LinkableMetrologyConfigurationFinder implements Finder<UsagePointMetrologyConfiguration> {
    private final DataModel dataModel;
    private final Set<UsagePointRequirementSqlBuilder> builders;
    private int start = -1;
    private int pageSize = 10;
    private List<Order> orders;
    private List<Hint> hints;

    @Inject
    public LinkableMetrologyConfigurationFinder(DataModel dataModel) {
        this.dataModel = dataModel;
        this.builders = new HashSet<>();
        this.orders = new ArrayList<>();
        this.hints = new ArrayList<>();
    }

    LinkableMetrologyConfigurationFinder addBuilder(UsagePointRequirementSqlBuilder builder) {
        this.builders.add(builder);
        return this;
    }

    @Override
    public Finder<UsagePointMetrologyConfiguration> paged(int start, int pageSize) {
        this.start = start;
        this.pageSize = pageSize;
        return this;
    }

    @Override
    public Finder<UsagePointMetrologyConfiguration> sorted(String sortColumn, boolean ascending) {
        this.orders.add(ascending ? Order.ascending(sortColumn) : Order.descending(sortColumn));
        return this;
    }

    @Override
    public Finder<UsagePointMetrologyConfiguration> sorted(Order order) {
        orders.add(order);
        return this;
    }

    @Override
    public Finder<UsagePointMetrologyConfiguration> withHint(Hint hint) {
        hints.add(hint);
        return this;
    }

    @Override
    public List<UsagePointMetrologyConfiguration> find() {
        Order[] orders = this.orders.isEmpty()
                ? new Order[]{Order.ascending("name")}
                : this.orders.toArray(new Order[this.orders.size()]);
        Hint[] hintsArray = hints.toArray(new Hint[0]);
        Condition condition = Condition.FALSE;
        for (UsagePointRequirementSqlBuilder builder : builders) {
            condition = condition.or(ListOperator.IN.contains(builder, "id"));
        }
        if (this.start >= 0) {
            return this.dataModel.query(UsagePointMetrologyConfiguration.class)
                    .select(condition, hintsArray, orders, true, new String[0], this.start + 1, this.start + this.pageSize + 1);
        }
        return this.dataModel.query(UsagePointMetrologyConfiguration.class)
                .select(condition, hintsArray, orders, false, null, 0, 0);
    }

    @Override
    public Subquery asSubQuery(String... fieldNames) {
        return () -> asFragment(fieldNames);
    }

    @Override
    public SqlFragment asFragment(String... fieldNames) {
        Condition condition = Condition.FALSE;
        for (UsagePointRequirementSqlBuilder builder : builders) {
            condition = condition.or(ListOperator.IN.contains(builder, "id"));
        }
        return this.dataModel
                .query(UsagePointMetrologyConfiguration.class)
                .asFragment(condition, fieldNames);
    }
}
