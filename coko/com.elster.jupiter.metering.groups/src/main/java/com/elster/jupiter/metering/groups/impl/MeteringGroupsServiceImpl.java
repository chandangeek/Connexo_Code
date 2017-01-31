/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedUsagePointGroup;
import com.elster.jupiter.metering.groups.Group;
import com.elster.jupiter.metering.groups.GroupBuilder;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.metering.groups.QueryUsagePointGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.metering.groups.impl.search.PropertyTranslationKeys;
import com.elster.jupiter.metering.groups.spi.QueryProvider;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.V10_2SimpleUpgrader;
import com.elster.jupiter.util.concurrent.CopyOnWriteServiceContainer;
import com.elster.jupiter.util.concurrent.OptionalServiceContainer;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.time.ExecutionTimer;
import com.elster.jupiter.util.time.ExecutionTimerService;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.elster.jupiter.orm.Version.version;
import static com.elster.jupiter.util.conditions.Where.where;

@Component(
        name = "com.elster.jupiter.metering.groups",
        service = {MeteringGroupsService.class, TranslationKeyProvider.class},
        property = "name=" + MeteringGroupsService.COMPONENTNAME,
        immediate = true)
public class MeteringGroupsServiceImpl implements MeteringGroupsService, TranslationKeyProvider {

    private static final Logger LOGGER = Logger.getLogger(MeteringGroupsServiceImpl.class.getSimpleName());

    private volatile DataModel dataModel;
    private volatile MeteringService meteringService;
    private volatile QueryService queryService;
    private volatile EventService eventService;
    private volatile SearchService searchService;
    private volatile NlsService nlsService;
    private volatile Thesaurus thesaurus;
    private volatile ExecutionTimerService executionTimerService;
    private volatile UpgradeService upgradeService;
    private volatile PropertySpecService propertySpecService;
    private ExecutionTimer endDeviceGroupMemberCountTimer;
    private final OptionalServiceContainer<QueryProvider<?>> queryProviders = new CopyOnWriteServiceContainer<>();

    public MeteringGroupsServiceImpl() {
    }

    @Inject
    public MeteringGroupsServiceImpl(OrmService ormService, MeteringService meteringService, QueryService queryService,
                                     EventService eventService, SearchService searchService, NlsService nlsService,
                                     ExecutionTimerService executionTimerService, UpgradeService upgradeService,
                                     PropertySpecService propertySpecService) {
        this();
        setOrmService(ormService);
        setMeteringService(meteringService);
        setQueryService(queryService);
        setEventService(eventService);
        setSearchService(searchService);
        setNlsService(nlsService);
        setExecutionTimerService(executionTimerService);
        setUpgradeService(upgradeService);
        setPropertySpecService(propertySpecService);
        activate();
    }

    @Activate
    public void activate() {
        try {
            this.endDeviceGroupMemberCountTimer = this.executionTimerService.newTimer("EndDeviceGroupMemberCountMonitor", Duration.ofMinutes(2));
            dataModel.register(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(MeteringGroupsService.class).toInstance(MeteringGroupsServiceImpl.this);
                    bind(MeteringService.class).toInstance(meteringService);
                    bind(DataModel.class).toInstance(dataModel);
                    bind(EventService.class).toInstance(eventService);
                    bind(QueryService.class).toInstance(queryService);
                    bind(SearchService.class).toInstance(searchService);
                    bind(NlsService.class).toInstance(nlsService);
                    bind(Thesaurus.class).toInstance(thesaurus);
                    bind(MessageInterpolator.class).toInstance(thesaurus);
                    bind(ExecutionTimer.class).toInstance(endDeviceGroupMemberCountTimer);
                    bind(PropertySpecService.class).toInstance(propertySpecService);
                }
            });
            upgradeService.register(InstallIdentifier.identifier("Pulse", COMPONENTNAME),
                    dataModel,
                    Installer.class,
                    ImmutableMap.of(
                            version(10, 2), V10_2SimpleUpgrader.class,
                            version(10, 2, 1), UpgraderV10_2_1.class,
                            UpgraderV10_3.VERSION, UpgraderV10_3.class
                    ));

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Deactivate
    public void deactivate() {
        this.endDeviceGroupMemberCountTimer.deactivate();
    }

    @Override
    public GroupBuilder.QueryGroupBuilder<UsagePoint, ? extends QueryUsagePointGroup> createQueryUsagePointGroup(SearchablePropertyValue... conditions) {
        return getUsagePointGroupBuilder().withConditions(conditions);
    }

    @Override
    public Optional<QueryUsagePointGroup> findQueryUsagePointGroup(long id) {
        return dataModel.mapper(QueryUsagePointGroup.class).getOptional(id);
    }

    @Override
    public GroupBuilder.EnumeratedGroupBuilder<UsagePoint, ? extends EnumeratedUsagePointGroup> createEnumeratedUsagePointGroup(UsagePoint... usagePoints) {
        return getUsagePointGroupBuilder().containing(usagePoints);
    }

    @Override
    public Optional<EnumeratedUsagePointGroup> findEnumeratedUsagePointGroup(long id) {
        return dataModel.mapper(EnumeratedUsagePointGroup.class).getOptional(id);
    }

    @Override
    public List<UsagePointGroup> findUsagePointGroups() {
        return dataModel.mapper(UsagePointGroup.class).find();
    }

    @Override
    public Optional<UsagePointGroup> findUsagePointGroup(long id) {
        return dataModel.mapper(UsagePointGroup.class).getOptional(id);
    }

    @Override
    public Optional<UsagePointGroup> findUsagePointGroup(String mRID) {
        return dataModel.stream(UsagePointGroup.class).filter(Operator.EQUAL.compare("mRID", mRID)).findFirst();
    }

    private GroupBuilder<EndDevice, ? extends EnumeratedEndDeviceGroup, ? extends QueryEndDeviceGroup> getEndDeviceGroupBuilder() {
        return new GroupBuilderImpl<>(() -> dataModel.getInstance(EnumeratedEndDeviceGroupImpl.class),
                () -> dataModel.getInstance(QueryEndDeviceGroupImpl.class));
    }

    private GroupBuilder<UsagePoint, ? extends EnumeratedUsagePointGroup, ? extends QueryUsagePointGroup> getUsagePointGroupBuilder() {
        return new GroupBuilderImpl<>(() -> dataModel.getInstance(EnumeratedUsagePointGroupImpl.class),
                () -> dataModel.getInstance(QueryUsagePointGroupImpl.class));
    }

    @Override
    public GroupBuilder.QueryGroupBuilder<EndDevice, ? extends QueryEndDeviceGroup> createQueryEndDeviceGroup(SearchablePropertyValue... conditions) {
        return getEndDeviceGroupBuilder().withConditions(conditions);
    }

    @Override
    public GroupBuilder.EnumeratedGroupBuilder<EndDevice, ? extends EnumeratedEndDeviceGroup> createEnumeratedEndDeviceGroup(EndDevice... endDevices) {
        return getEndDeviceGroupBuilder().containing(endDevices);
    }

    @Override
    public Optional<EnumeratedEndDeviceGroup> findEnumeratedEndDeviceGroup(long id) {
        return dataModel.mapper(EnumeratedEndDeviceGroup.class).getOptional(id);
    }

    @Override
    public List<EnumeratedEndDeviceGroup> findEnumeratedEndDeviceGroupsContaining(EndDevice endDevice) {
        return this.dataModel
                .query(EnumeratedEndDeviceGroupImpl.EndDeviceEntryImpl.class)
                .select(where("member").isEqualTo(endDevice)
                        .and(where("interval").isEffective()))
                .stream()
                .map(EnumeratedEndDeviceGroupImpl.EndDeviceEntryImpl::getGroup)
                .collect(Collectors.toList());
    }

    @Override
    public List<EnumeratedUsagePointGroup> findEnumeratedUsagePointGroupsContaining(UsagePoint usagePoint) {
        return this.dataModel
                .query(EnumeratedUsagePointGroupImpl.UsagePointEntryImpl.class)
                .select(where("member").isEqualTo(usagePoint)
                        .and(where("interval").isEffective()))
                .stream()
                .map(EnumeratedUsagePointGroupImpl.UsagePointEntryImpl::getGroup)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<QueryEndDeviceGroup> findQueryEndDeviceGroup(long id) {
        return dataModel.mapper(QueryEndDeviceGroup.class).getOptional(id);
    }

    @Override
    public Query<EndDeviceGroup> getEndDeviceGroupQuery() {
        return queryService.wrap(dataModel.query(EndDeviceGroup.class));
    }

    @Override
    public Query<UsagePointGroup> getUsagePointGroupQuery() {
        return queryService.wrap(dataModel.query(UsagePointGroup.class));
    }

    @Override
    public Query<EndDeviceGroup> getQueryEndDeviceGroupQuery() {
        Query<EndDeviceGroup> endDeviceGroupQuery = queryService.wrap(dataModel.query(EndDeviceGroup.class));
        endDeviceGroupQuery.setRestriction(where("class").isEqualTo(QueryEndDeviceGroup.TYPE_IDENTIFIER).and(where("label").isEqualTo("MDC")));
        return endDeviceGroupQuery;
    }

    @Override
    public Query<UsagePointGroup> getQueryUsagePointGroupQuery() {
        Query<UsagePointGroup> usagePointGroupQuery = queryService.wrap(dataModel.query(UsagePointGroup.class));
        usagePointGroupQuery.setRestriction(where("class").isEqualTo(QueryUsagePointGroup.TYPE_IDENTIFIER).and(where("label").isEqualTo("MDM")));
        return usagePointGroupQuery;
    }

    @Override
    public List<EndDeviceGroup> findEndDeviceGroups() {
        return dataModel.mapper(EndDeviceGroup.class).find();
    }

    @Override
    public Optional<EndDeviceGroup> findEndDeviceGroup(String mRID) {
        return dataModel.stream(EndDeviceGroup.class).filter(Operator.EQUAL.compare("mRID", mRID)).findFirst();
    }

    @Override
    public Optional<EndDeviceGroup> findEndDeviceGroup(long id) {
        return dataModel.mapper(EndDeviceGroup.class).getOptional(id);
    }

    @Override
    public Optional<EndDeviceGroup> findAndLockEndDeviceGroupByIdAndVersion(long id, long version) {
        return dataModel.mapper(EndDeviceGroup.class).lockObjectIfVersion(version, id);
    }

    @Override
    public Optional<UsagePointGroup> findAndLockUsagePointGroupByIdAndVersion(long id, long version) {
        return dataModel.mapper(UsagePointGroup.class).lockObjectIfVersion(version, id);
    }

    @Override
    public <T extends Group<?>> Optional<T> findGroupByName(String name, Class<T> api) {
        return dataModel.stream(api).filter(Operator.EQUALIGNORECASE.compare("name", name)).findFirst();
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(COMPONENTNAME, "CIM Metering Groups");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }

    @Reference
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(COMPONENTNAME, Layer.DOMAIN);
    }

    @Reference
    public void setExecutionTimerService(ExecutionTimerService executionTimerService) {
        this.executionTimerService = executionTimerService;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addQueryProvider(QueryProvider<?> queryProvider) {
        queryProviders.register(queryProvider);
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    public void removeQueryProvider(QueryProvider<?> queryProvider) {
        queryProviders.unregister(queryProvider);
    }

    @Override
    public Optional<QueryProvider<?>> pollQueryProvider(String name, Duration duration) throws InterruptedException {
        return queryProviders.get(withName(name), duration);
    }

    private Predicate<QueryProvider<?>> withName(String name) {
        return p -> p.getName().equals(name);
    }

    @Override
    public String getComponentName() {
        return COMPONENTNAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> translationKeys = new ArrayList<>();
        translationKeys.addAll(Arrays.asList(MessageSeeds.values()));
        translationKeys.addAll(Arrays.asList(PropertyTranslationKeys.values()));
        return translationKeys;
    }

    public DataModel getDataModel() {
        return dataModel;
    }
}
