package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.groups.EndDeviceMembership;
import com.elster.jupiter.metering.groups.EndDeviceQueryProvider;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.NoSuchQueryProvider;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.search.SearchBuilder;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyCondition;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.elster.jupiter.util.sql.SqlFragment;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class QueryEndDeviceGroupImpl extends AbstractEndDeviceGroup implements QueryEndDeviceGroup {

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
    private String searchDomain;

    @Valid
    private List<QueryEndDeviceGroupCondition> conditions = new ArrayList<>();

    private final MeteringGroupsService meteringGroupService;
    private final SearchService searchService;
    private final Thesaurus thesaurus;

    @Inject
    public QueryEndDeviceGroupImpl(DataModel dataModel, MeteringGroupsService meteringGroupService, EventService eventService, SearchService searchService, Thesaurus thesaurus) {
        super(eventService, dataModel);
        this.meteringGroupService = meteringGroupService;
        this.searchService = searchService;
        this.thesaurus = thesaurus;
    }

    @Override
    public List<EndDevice> getMembers(Instant instant) {
        return getEndDeviceQueryProvider().findEndDevices(instant, getSearchablePropertyConditions());
    }

    @Override
    public List<EndDevice> getMembers(Instant instant, int start, int limit) {
        return getEndDeviceQueryProvider().findEndDevices(instant, getSearchablePropertyConditions(), start, limit);
    }

    @Override
    public EndDeviceQueryProvider getEndDeviceQueryProvider() {
        try {
            return meteringGroupService.pollEndDeviceQueryProvider(getQueryProviderName(), Duration.ofMinutes(1)).orElseThrow(() -> new NoSuchQueryProvider(getQueryProviderName()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new NoSuchQueryProvider(getQueryProviderName());
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

    private DataMapper<QueryEndDeviceGroup> groupFactory() {
        return dataModel.mapper(QueryEndDeviceGroup.class);
    }

    void save() {
        groupFactory().persist(this);
    }

    @Override
    public void update() {
        groupFactory().update(this);
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
    public List<SearchablePropertyValue> getSearchablePropertyValues() {
        return findSearchDomainOrThrowException().getPropertiesValues(this::mapper);
    }

    private SearchBuilder<?> getSearchBuilder() {
        SearchDomain searchDomain = findSearchDomainOrThrowException();
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

    private SearchDomain findSearchDomainOrThrowException() {
        return this.searchService.pollSearchDomain(this.searchDomain, Duration.ofMinutes(1))
                .orElseThrow(() -> new InvalidQueryDeviceGroupException(thesaurus, MessageSeeds.SEARCH_DOMAIN_NOT_FOUND, this.searchDomain));
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

    public void addQueryEndDeviceGroupCondition(SearchablePropertyValue searchablePropertyValue) {
        conditions.add(new QueryEndDeviceGroupCondition().init(
                        this,
                        searchablePropertyValue.getValueBean().propertyName,
                        searchablePropertyValue.getValueBean().operator,
                        searchablePropertyValue.getValueBean().values)
        );
    }

    @Override
    public void setConditions(List<SearchablePropertyValue> conditions) {
        this.conditions.clear();
        conditions.forEach(this::addQueryEndDeviceGroupCondition);
    }

    public void setSearchDomain(SearchDomain searchDomain) {
        this.searchDomain = searchDomain.getId();
    }

    @Override
    public SearchDomain getSearchDomain() {
        return this.searchService.pollSearchDomain(this.searchDomain, Duration.ofMinutes(1)).get();
    }

    List<QueryEndDeviceGroupCondition> getConditions() {
        return conditions;
    }
}