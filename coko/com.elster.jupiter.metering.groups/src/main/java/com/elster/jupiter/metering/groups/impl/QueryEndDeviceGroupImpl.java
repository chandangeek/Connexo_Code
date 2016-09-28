package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EndDeviceMembership;
import com.elster.jupiter.metering.groups.EndDeviceQueryProvider;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.NoSuchQueryProvider;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
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
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Subquery;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;
import com.elster.jupiter.util.time.ExecutionTimer;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;

import javax.inject.Inject;
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
import java.util.function.Supplier;

class QueryEndDeviceGroupImpl extends AbstractEndDeviceGroup implements QueryEndDeviceGroup {

    enum Fields {
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

    private List<QueryEndDeviceGroupCondition> conditions = new ArrayList<>();

    private final MeteringGroupsService meteringGroupService;
    private final MeteringService meteringService;
    private final SearchService searchService;
    private final ExecutionTimer endDeviceGroupMemberCountTimer;
    private final Thesaurus thesaurus;

    @Inject
    QueryEndDeviceGroupImpl(DataModel dataModel, MeteringGroupsService meteringGroupService, MeteringService meteringService, EventService eventService, SearchService searchService, ExecutionTimer endDeviceGroupMemberCountTimer, Thesaurus thesaurus) {
        super(eventService, dataModel);
        this.meteringGroupService = meteringGroupService;
        this.meteringService = meteringService;
        this.searchService = searchService;
        this.endDeviceGroupMemberCountTimer = endDeviceGroupMemberCountTimer;
        this.thesaurus = thesaurus;
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    @Override
    public List<EndDevice> getMembers(Instant instant) {
        return getEndDeviceQueryProvider().findEndDevices(instant, getSearchablePropertyConditions());
    }

    @Override
    public long getMemberCount(Instant instant) {
        try (Connection connection = this.getDataModel().getConnection(true)) {
            return this.endDeviceGroupMemberCountTimer.time(() -> this.doMemberCount(connection));
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
    public List<EndDevice> getMembers(Instant instant, int start, int limit) {
        return getEndDeviceQueryProvider().findEndDevices(instant, getSearchablePropertyConditions(), start, limit);
    }

    @Override
    public EndDeviceQueryProvider getEndDeviceQueryProvider() {
        try {
            return meteringGroupService.pollEndDeviceQueryProvider(queryProviderName, Duration.ofMinutes(1)).orElseThrow(() -> new NoSuchQueryProvider(queryProviderName));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new NoSuchQueryProvider(queryProviderName);
        }
    }

    @Override
    public List<EndDeviceMembership> getMembers(Range<Instant> range) {
        RangeSet<Instant> ranges = ImmutableRangeSet.of(range);
        List<EndDeviceMembership> memberships = new ArrayList<>();
        for (EndDevice endDevice : getMembers((Instant) null)) {
            memberships.add(new EndDeviceMembershipImpl(endDevice, ranges));
        }
        return memberships;
    }

    @Override
    public boolean isMember(EndDevice endDevice, Instant instant) {
        return getMembers(instant).contains(endDevice);
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
        Query<EndDevice> endDeviceQuery = getEndDeviceQueryProvider().getEndDeviceQuery(getSearchablePropertyConditions());
        return endDeviceQuery.asSubquery(Condition.TRUE, fields);
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
                throw new InvalidQueryDeviceGroupException(thesaurus, MessageSeeds.INVALID_SEARCH_CRITERIA, e);
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

    void addQueryEndDeviceGroupCondition(SearchablePropertyValue searchablePropertyValue) {
        QueryEndDeviceGroupCondition condition =
                this.getDataModel()
                        .getInstance(QueryEndDeviceGroupCondition.class)
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
        this.conditions.forEach(QueryEndDeviceGroupCondition::delete);
        conditions.forEach(this::addQueryEndDeviceGroupCondition);
    }

    public void setSearchDomain(SearchDomain searchDomain) {
        this.searchDomain = searchDomain.getId();
    }

    @Override
    public SearchDomain getSearchDomain() {
        Supplier<InvalidQueryDeviceGroupException> noSuchDomainException =
                () -> new InvalidQueryDeviceGroupException(thesaurus, MessageSeeds.SEARCH_DOMAIN_NOT_FOUND, this.searchDomain);
        try {
            return this.searchService.pollDomain(this.searchDomain, Duration.ofMinutes(1)).orElseThrow(noSuchDomainException);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw noSuchDomainException.get();
        }
    }

    List<QueryEndDeviceGroupCondition> getConditions() {
        return Collections.unmodifiableList(conditions);
    }

    void setQueryProviderName(String queryProviderName) {
        this.queryProviderName = queryProviderName;
    }

    @Override
    public void delete() {
        this.conditions.forEach(QueryEndDeviceGroupCondition::delete);
        super.delete();
    }
}