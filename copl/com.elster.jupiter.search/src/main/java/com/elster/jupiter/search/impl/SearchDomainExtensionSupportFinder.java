package com.elster.jupiter.search.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.search.SearchDomainExtension;
import com.elster.jupiter.search.SearchablePropertyCondition;
import com.elster.jupiter.util.Holder;
import com.elster.jupiter.util.HolderBuilder;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Subquery;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SearchDomainExtensionSupportFinder<T> implements Finder<T> {
    private final Finder<T> originalFinder;
    private final Map<SearchDomainExtension, List<SearchablePropertyCondition>> extensionConditions;

    private Pagination pagination = new NoPagination();
    private List<Order> orders = new ArrayList<>();

    public SearchDomainExtensionSupportFinder(Finder<T> originalFinder, Map<SearchDomainExtension, List<SearchablePropertyCondition>> extensionConditions) {
        this.originalFinder = originalFinder;
        this.extensionConditions = extensionConditions;
    }

    @Override
    public Finder<T> paged(int from, int pageSize) {
        this.pagination = new WithPagination(from, from + pageSize);
        return this;
    }

    @Override
    public Finder<T> sorted(String sortColumn, boolean ascending) {
        orders.add(ascending ? Order.ascending(sortColumn) : Order.descending(sortColumn));
        return this;
    }

    @Override
    public List<T> find() {
        return null;

    }

    @Override
    public Subquery asSubQuery(String... strings) {
        return () -> asFragment(strings);
    }

    @Override
    public SqlBuilder asFragment(String... strings) {
        SqlBuilder sqlBuilder = new SqlBuilder("select " + Stream.of(strings).collect(Collectors.joining(", ")) + " from ");
        sqlBuilder.openBracket();
        sqlBuilder.add(this.originalFinder.asFragment("*"));
        sqlBuilder.closeBracket();
        sqlBuilder.append(" sd where ");
        Holder<String> holder = HolderBuilder.first("").andThen(" AND ");
        for (Map.Entry<SearchDomainExtension, List<SearchablePropertyCondition>> extensionEntry : this.extensionConditions.entrySet()) {
            sqlBuilder.append(holder.get());
            sqlBuilder.openBracket();
            sqlBuilder.add(extensionEntry.getKey().asFragment(extensionEntry.getValue()));
            sqlBuilder.closeBracket();
        }
        if (!this.orders.isEmpty()) {
            sqlBuilder.append(" order by " + this.orders.stream()
                    .map(order -> order.getClause(order.getName()))
                    .collect(Collectors.joining(", ")));
        }
        sqlBuilder = this.pagination.addPaging(sqlBuilder);
        return sqlBuilder;
    }

    private interface Pagination {
        SqlBuilder addPaging(SqlBuilder sqlBuilder);
    }

    private class NoPagination implements Pagination {
        @Override
        public SqlBuilder addPaging(SqlBuilder sqlBuilder) {
            return sqlBuilder;
        }
    }

    private class WithPagination implements Pagination {
        private final int from;
        private final int to;

        private WithPagination(int from, int to) {
            super();
            this.from = from;
            this.to = to;
        }

        @Override
        public SqlBuilder addPaging(SqlBuilder sqlBuilder) {
            return sqlBuilder.asPageBuilder(this.from + 1, this.to + 1);
        }
    }
}
