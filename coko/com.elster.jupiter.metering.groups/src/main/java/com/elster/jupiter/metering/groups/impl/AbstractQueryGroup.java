package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.groups.Membership;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.QueryGroup;
import com.elster.jupiter.metering.groups.spi.QueryProvider;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.search.SearchBuilder;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyCondition;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Subquery;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;
import com.elster.jupiter.util.time.ExecutionTimer;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;

import javax.validation.constraints.NotNull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

abstract class AbstractQueryGroup<T extends HasId & IdentifiedObject> extends AbstractGroup<T> implements QueryGroup<T> {

    enum Fields {
        QUERY_PROVIDER_NAME("queryProviderName"),
        SEARCH_DOMAIN("searchDomain"),
        CONDITIONS("conditions");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    @NotNull
    private String queryProviderName;
    @NotNull
    private String searchDomain;

    private List<QueryGroupCondition> conditions = new ArrayList<>();

    private final MeteringGroupsService meteringGroupService;
    private final SearchService searchService;
    private final ExecutionTimer groupMembersCountTimer;
    private final Thesaurus thesaurus;

    AbstractQueryGroup(DataModel dataModel, EventService eventService, MeteringGroupsService meteringGroupService,
                       SearchService searchService, ExecutionTimer groupMembersCountTimer, Thesaurus thesaurus) {
        super(eventService, dataModel);
        this.meteringGroupService = meteringGroupService;
        this.searchService = searchService;
        this.groupMembersCountTimer = groupMembersCountTimer;
        this.thesaurus = thesaurus;
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    @Override
    public List<T> getMembers(Instant instant) {
        return getQueryProvider().executeQuery(instant, getSearchablePropertyConditions());
    }

    @Override
    public long getMemberCount(Instant instant) {
        try (Connection connection = this.getDataModel().getConnection(true)) {
            return this.groupMembersCountTimer.time(() -> this.doMemberCount(connection));
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        } catch (Exception e) {
            // actual Caller implementation is only throwing SQLException anf that is already handled
            return 0;
        }
    }

    private long doMemberCount(Connection connection) throws SQLException {
        SqlBuilder countSqlBuilder = new SqlBuilder();
        countSqlBuilder.add(getSearchBuilder().toFinder().asFragment("count(*)"));
        try (PreparedStatement statement = countSqlBuilder.prepare(connection)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        }
    }

    @Override
    public List<T> getMembers(Instant instant, int start, int limit) {
        return getQueryProvider().executeQuery(instant, getSearchablePropertyConditions(), start, limit);
    }

    @Override
    public QueryProvider<T> getQueryProvider() {
        try {
            return meteringGroupService.pollQueryProvider(queryProviderName, Duration.ofMinutes(1))
                    .map(queryProvider -> (QueryProvider<T>) queryProvider)
                    .map(queryProvider -> queryProvider.init(getBasicQuerySupplier()))
                    .orElseThrow(() -> new InvalidQueryGroupException(thesaurus, MessageSeeds.NO_QUERY_PROVIDER_FOUND, queryProviderName));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InvalidQueryGroupException(thesaurus, MessageSeeds.NO_QUERY_PROVIDER_FOUND, e, queryProviderName);
        }
    }

    @Override
    public List<Membership<T>> getMembers(Range<Instant> range) {
        RangeSet<Instant> ranges = ImmutableRangeSet.of(range);
        return getMembers((Instant) null).stream()
                .map(member -> new MembershipImpl<>(member, ranges))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isMember(T object, Instant instant) {
        return getMembers(instant).contains(object);
    }

    void save() {
        Save.CREATE.save(getDataModel(), this);
    }

    @Override
    public void update() {
        Save.UPDATE.save(getDataModel(), this);
    }

    @Override
    public List<SearchablePropertyCondition> getSearchablePropertyConditions() {
        return getSearchBuilder().getConditions();
    }

    @Override
    public SqlFragment toFragment() {
        return getSearchBuilder().toFinder().asFragment("id");
    }

    @Override
    public Subquery toSubQuery(String... fields) {
        Query<T> query = getQueryProvider().getQuery(getSearchablePropertyConditions());
        return query.asSubquery(Condition.TRUE, fields);
    }

    @Override
    public List<SearchablePropertyValue> getSearchablePropertyValues() {
        return getSearchDomain().getPropertiesValues(this::mapper);
    }

    private SearchBuilder<?> getSearchBuilder() {
        SearchDomain searchDomain = getSearchDomain();
        SearchBuilder<?> searchBuilder = this.searchService.search(searchDomain);
        searchDomain.getPropertiesValues(this::mapper).forEach(value -> {
            try {
                value.addAsCondition(searchBuilder);
            } catch (InvalidValueException e) {
                throw new InvalidQueryGroupException(thesaurus, MessageSeeds.INVALID_SEARCH_CRITERIA, e);
            }
        });
        return searchBuilder;
    }

    private SearchablePropertyValue mapper(SearchableProperty searchableProperty) {
        return this.conditions.stream()
                .filter(condition -> condition.getSearchableProperty().equals(searchableProperty.getName()))
                .findFirst()
                .map(condition -> {
                    SearchablePropertyValue searchablePropertyValue = new SearchablePropertyValue(searchableProperty);
                    searchablePropertyValue.setValueBean(condition.toValueBean());
                    return searchablePropertyValue;
                }).orElse(null);
    }

    void addQueryGroupCondition(SearchablePropertyValue searchablePropertyValue) {
        QueryGroupCondition condition =
                this.getDataModel()
                        .getInstance(getConditionApiClass())
                        .init(this,
                                searchablePropertyValue.getValueBean().propertyName,
                                searchablePropertyValue.getValueBean().operator,
                                searchablePropertyValue.getValueBean().values);
        Save.CREATE.validate(this.getDataModel(), condition);
        this.getDataModel().persist(condition);
        this.conditions.add(condition);
    }

    @Override
    public void setConditions(List<SearchablePropertyValue> conditions) {
        this.conditions.forEach(QueryGroupCondition::delete);
        conditions.forEach(this::addQueryGroupCondition);
    }

    void setSearchDomain(SearchDomain searchDomain) {
        this.searchDomain = searchDomain.getId();
    }

    @Override
    public SearchDomain getSearchDomain() {
        try {
            return this.searchService.pollDomain(this.searchDomain, Duration.ofMinutes(1))
                    .orElseThrow(() -> new InvalidQueryGroupException(thesaurus, MessageSeeds.SEARCH_DOMAIN_NOT_FOUND, this.searchDomain));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InvalidQueryGroupException(thesaurus, MessageSeeds.SEARCH_DOMAIN_NOT_FOUND, e, this.searchDomain);
        }
    }

    List<QueryGroupCondition> getConditions() {
        return Collections.unmodifiableList(conditions);
    }

    void setQueryProviderName(String queryProviderName) {
        this.queryProviderName = queryProviderName;
    }

    @Override
    public void delete() {
        this.conditions.forEach(QueryGroupCondition::delete);
        super.delete();
    }

    abstract Class<? extends QueryGroupCondition> getConditionApiClass();

    final Thesaurus getThesaurus() {
        return this.thesaurus;
    }
}
