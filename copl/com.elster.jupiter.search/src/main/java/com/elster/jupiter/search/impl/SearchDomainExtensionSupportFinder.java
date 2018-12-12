/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.search.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchDomainExtension;
import com.elster.jupiter.search.SearchablePropertyCondition;
import com.elster.jupiter.util.Holder;
import com.elster.jupiter.util.HolderBuilder;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Subquery;
import com.elster.jupiter.util.sql.Fetcher;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class SearchDomainExtensionSupportFinder<T> implements Finder<T> {
    private static final String RESERVED_ALIAS = "SD";
    private final DataModel dataModel;
    private final SearchDomain searchDomain;
    private final Finder<T> domainFinder;
    private final Map<SearchDomainExtension, List<SearchablePropertyCondition>> extensionConditions;

    private Pagination pagination = new NoPagination();
    private List<Order> orders = new ArrayList<>();

    public static Finder<?> getFinder(OrmService ormService, SearchDomain searchDomain,
                                      List<SearchablePropertyCondition> conditions) {
        List<SearchablePropertyCondition> domainConditions = new ArrayList<>(conditions);
        Map<SearchDomainExtension, List<SearchablePropertyCondition>> extensionsConditions = new HashMap<>();
        conditions.stream()
                .filter(condition -> condition.getProperty() instanceof SearchDomainExtensionSearchableProperty)
                .forEach(condition -> {
                    domainConditions.remove(condition);
                    SearchDomainExtension domainExtension =
                            ((SearchDomainExtensionSearchableProperty) condition.getProperty()).getDomainExtension();
                    List<SearchablePropertyCondition> singleExtensionConditions =
                            extensionsConditions.get(domainExtension);
                    if (singleExtensionConditions == null) {
                        singleExtensionConditions = new ArrayList<>();
                        extensionsConditions.put(domainExtension, singleExtensionConditions);
                    }
                    singleExtensionConditions.add(condition);
                });
        if (extensionsConditions.isEmpty()) {
            return searchDomain.finderFor(domainConditions);
        }
        DataModel dataModel = ormService.getDataModels()
                .stream()
                .filter(dm -> dm.getTables(Version.latest()).stream().anyMatch(table -> table.maps(searchDomain.getDomainClass())))
                .findAny()
                .get();
        return new SearchDomainExtensionSupportFinder<>(dataModel, searchDomain,
                searchDomain.finderFor(domainConditions), extensionsConditions);
    }

    private SearchDomainExtensionSupportFinder(DataModel dataModel, SearchDomain searchDomain, Finder<T> domainFinder,
                                               Map<SearchDomainExtension, List<SearchablePropertyCondition>> extensionConditions) {
        this.dataModel = dataModel;
        this.searchDomain = searchDomain;
        this.domainFinder = domainFinder;
        this.extensionConditions = extensionConditions;
    }

    @Override
    public Finder<T> paged(int from, int pageSize) {
        pagination = new WithPagination(from, from + pageSize);
        return this;
    }

    @Override
    public Finder<T> sorted(String sortColumn, boolean ascending) {
        orders.add(ascending ? Order.ascending(sortColumn) : Order.descending(sortColumn));
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> find() {
        try (Fetcher<T> fetcher = (Fetcher<T>) dataModel
                .mapper(searchDomain.getDomainClass())
                .fetcher(asFragment("*"))) {
            return StreamSupport.stream(fetcher.spliterator(), false)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public int count() {
        try (Connection connection = dataModel.getConnection(false)) {
            SqlBuilder countSqlBuilder = new SqlBuilder();
            countSqlBuilder.add(asFragment("count(*)"));
            try (PreparedStatement statement = countSqlBuilder.prepare(connection)) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    resultSet.next();
                    return resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    @Override
    public Subquery asSubQuery(String... strings) {
        return () -> asFragment(strings);
    }

    @Override
    public SqlBuilder asFragment(String... strings) {
        SqlBuilder sqlBuilder = new SqlBuilder("select "
                + Stream.of(strings).collect(Collectors.joining(", "))
                + " from ");
        sqlBuilder.openBracket();
        String[] allFields = dataModel.mapper(searchDomain.getDomainClass())
                .getQueryFields()
                .stream()
                .toArray(String[]::new);
        sqlBuilder.add(domainFinder.asFragment(allFields));
        sqlBuilder.closeBracket();
        sqlBuilder.append(" " + RESERVED_ALIAS + " where ");
        Holder<String> holder = HolderBuilder.first("").andThen(" AND ");
        for (Map.Entry<SearchDomainExtension, List<SearchablePropertyCondition>> extensionEntry :
                extensionConditions.entrySet()) {
            sqlBuilder.append(holder.get());
            appendSearchDomainPrimaryKey(sqlBuilder);
            sqlBuilder.openBracket();
            sqlBuilder.add(extensionEntry.getKey().asFragment(extensionEntry.getValue()));
            sqlBuilder.closeBracket();
        }
        if (!orders.isEmpty()) {
            sqlBuilder.append(" order by " + orders.stream()
                    .map(order -> order.getClause(order.getName()))
                    .collect(Collectors.joining(", ")));
        }
        sqlBuilder = pagination.addPaging(sqlBuilder);
        return sqlBuilder;
    }

    private void appendSearchDomainPrimaryKey(SqlBuilder sqlBuilder) {
        sqlBuilder.openBracket();
        sqlBuilder.append(dataModel.getTables(Version.latest())
                .stream()
                .filter(table -> table.maps(searchDomain.getDomainClass()))
                .flatMap(table -> table.getPrimaryKeyColumns().stream())
                .map(Column::getName)
                .collect(Collectors.joining(", " + RESERVED_ALIAS + ".", RESERVED_ALIAS + ".", "")));
        sqlBuilder.closeBracket();
        sqlBuilder.append(" IN ");
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
            return sqlBuilder.asPageBuilder(from + 1, to + 1);
        }
    }
}
