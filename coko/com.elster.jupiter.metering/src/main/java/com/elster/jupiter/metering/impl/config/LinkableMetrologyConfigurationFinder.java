package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.util.Holder;
import com.elster.jupiter.util.HolderBuilder;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Subquery;
import com.elster.jupiter.util.sql.Fetcher;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class LinkableMetrologyConfigurationFinder implements Finder<UsagePointMetrologyConfiguration> {
    private final ServerMeteringService meteringService;
    private final Set<LinkableMetrologyConfigurationSqlBuilder> builders;
    private Pagination pagination;
    private List<Order> orders;

    @Inject
    public LinkableMetrologyConfigurationFinder(ServerMeteringService meteringService) {
        this.meteringService = meteringService;
        this.builders = new HashSet<>();
        this.pagination = new NoPagination();
        this.orders = new ArrayList<>();
    }

    LinkableMetrologyConfigurationFinder addBuilder(LinkableMetrologyConfigurationSqlBuilder builder) {
        this.builders.add(builder);
        return this;
    }

    @Override
    public Finder<UsagePointMetrologyConfiguration> paged(int start, int pageSize) {
        this.pagination = new RealPagination(start, pageSize);
        return this;
    }

    @Override
    public Finder<UsagePointMetrologyConfiguration> sorted(String sortColumn, boolean ascending) {
        this.orders.add(ascending ? Order.ascending(sortColumn) : Order.descending(sortColumn));
        return this;
    }

    @Override
    public List<UsagePointMetrologyConfiguration> find() {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.add(asFragment());
        if (this.orders.isEmpty()) {
            sqlBuilder.append(" ORDER BY MC.name");
        } else {
            sqlBuilder.append(" ORDER BY " + this.orders.stream()
                    .map(order -> order.getClause(order.getName()))
                    .collect(Collectors.joining(", ")));
        }
        sqlBuilder = this.pagination.apply(sqlBuilder);
        List<UsagePointMetrologyConfiguration> matchedMetrologyConfigurations = new ArrayList<>();
        try (Fetcher<UsagePointMetrologyConfiguration> fetcher = this.meteringService.getDataModel().mapper(UsagePointMetrologyConfiguration.class).fetcher(sqlBuilder)) {
            StreamSupport.stream(fetcher.spliterator(), false).forEach(matchedMetrologyConfigurations::add);
        }
        return matchedMetrologyConfigurations;
    }

    @Override
    public Subquery asSubQuery(String... fieldNames) {
        return () -> asFragment(fieldNames);
    }

    @Override
    public SqlFragment asFragment(String... fieldNames) {
        SqlBuilder sqlBuilder = this.meteringService.getDataModel().mapper(UsagePointMetrologyConfiguration.class).builder("MC");
        if (!this.builders.isEmpty()) {
            sqlBuilder.append(" WHERE");
            Holder<String> separator = HolderBuilder.first(" ").andThen(" OR ");
            for (LinkableMetrologyConfigurationSqlBuilder builder : this.builders) {
                sqlBuilder.append(separator.get());
                sqlBuilder.append("(MC.ID IN (");
                sqlBuilder.add(builder.toFragment());
                sqlBuilder.append("))");
            }
        }
        return sqlBuilder;
    }


    private interface Pagination {
        SqlBuilder apply(SqlBuilder sqlBuilder);
    }

    private class NoPagination implements Pagination {
        @Override
        public SqlBuilder apply(SqlBuilder sqlBuilder) {
            return sqlBuilder;
        }
    }

    private class RealPagination implements Pagination {
        private final int from;
        private final int to;

        private RealPagination(int from, int pageSize) {
            this.from = from;
            this.to = from + pageSize;
        }

        @Override
        public SqlBuilder apply(SqlBuilder sqlBuilder) {
            return sqlBuilder.asPageBuilder(this.from + 1, this.to + 1);
        }
    }
}
