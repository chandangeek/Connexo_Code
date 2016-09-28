package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EndDeviceGroupBuilder;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedUsagePointGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.metering.groups.QueryUsagePointGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.metering.groups.UsagePointGroupBuilder;
import com.elster.jupiter.metering.groups.spi.EndDeviceQueryProvider;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.V10_2SimpleUpgrader;
import com.elster.jupiter.util.concurrent.CopyOnWriteServiceContainer;
import com.elster.jupiter.util.concurrent.OptionalServiceContainer;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.time.ExecutionTimer;
import com.elster.jupiter.util.time.ExecutionTimerService;

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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
    private ExecutionTimer endDeviceGroupMemberCountTimer;
    private final OptionalServiceContainer<EndDeviceQueryProvider> endDeviceQueryProviders = new CopyOnWriteServiceContainer<>();

    public MeteringGroupsServiceImpl() {
    }

    @Inject
    public MeteringGroupsServiceImpl(OrmService ormService, MeteringService meteringService, QueryService queryService, EventService eventService, SearchService searchService, NlsService nlsService, ExecutionTimerService executionTimerService, UpgradeService upgradeService) {
        this();
        setOrmService(ormService);
        setMeteringService(meteringService);
        setQueryService(queryService);
        setEventService(eventService);
        setSearchService(searchService);
        setNlsService(nlsService);
        setExecutionTimerService(executionTimerService);
        setUpgradeService(upgradeService);
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
                }
            });
            upgradeService.register(InstallIdentifier.identifier("Pulse", COMPONENTNAME), dataModel, Installer.class, V10_2SimpleUpgrader.V10_2_UPGRADER);
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
    public UsagePointGroupBuilder.QueryUsagePointGroupBuilder createQueryUsagePointGroup(Condition condition) {
        return getUsagePointGroupBuilder().withConditions(condition);
    }

    @Override
    public Optional<QueryUsagePointGroup> findQueryUsagePointGroup(long id) {
        return dataModel.mapper(QueryUsagePointGroup.class).getOptional(id);
    }

    @Override
    public UsagePointGroupBuilder.EnumeratedUsagePointGroupBuilder createEnumeratedUsagePointGroup() {
        return getUsagePointGroupBuilder().enumerated();
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
        return dataModel.mapper(UsagePointGroup.class).select(Operator.EQUAL.compare("mRID", mRID)).stream().findFirst();
    }

    @Override
    public Optional<UsagePointGroup> findUsagePointGroupByName(String name) {
        return dataModel.mapper(UsagePointGroup.class).select(Operator.EQUALIGNORECASE.compare("name", name)).stream().findFirst();
    }

    private EndDeviceGroupBuilderImpl getEndDeviceGroupBuilder() {
        return dataModel.getInstance(EndDeviceGroupBuilderImpl.class);
    }

    private UsagePointGroupBuilderImpl getUsagePointGroupBuilder() {
        return dataModel.getInstance(UsagePointGroupBuilderImpl.class);
    }

    @Override
    public EndDeviceGroupBuilder.QueryEndDeviceGroupBuilder createQueryEndDeviceGroup(SearchablePropertyValue... conditions) {
        return getEndDeviceGroupBuilder().withConditions(conditions);
    }

    @Override
    public EndDeviceGroupBuilder.EnumeratedEndDeviceGroupBuilder createEnumeratedEndDeviceGroup(EndDevice... endDevices) {
        return getEndDeviceGroupBuilder().containing(endDevices);
    }

    @Override
    public Optional<EnumeratedEndDeviceGroup> findEnumeratedEndDeviceGroup(long id) {
        return dataModel.mapper(EnumeratedEndDeviceGroup.class).getOptional(id);
    }

    @Override
    public List<EnumeratedEndDeviceGroup> findEnumeratedEndDeviceGroupsContaining(EndDevice endDevice) {
        return this.dataModel
                .query(EnumeratedEndDeviceGroupImpl.EntryImpl.class)
                .select(
                         where("endDevice").isEqualTo(endDevice)
                    .and(where("interval").isEffective()))
                .stream()
                .map(EnumeratedEndDeviceGroupImpl.EntryImpl::getEndDeviceGroup)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<QueryEndDeviceGroup> findQueryEndDeviceGroup(long id) {
        return dataModel.mapper(QueryEndDeviceGroup.class).getOptional(id);
    }

    /*@Override
    public Finder<EndDeviceGroup> findAllEndDeviceGroups() {
        return DefaultFinder.of(EndDeviceGroup.class, dataModel).defaultSortColumn("lower(name)");
    }*/

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
    public List<EndDeviceGroup> findEndDeviceGroups() {
        return dataModel.mapper(EndDeviceGroup.class).find();
    }

    @Override
    public Optional<EndDeviceGroup> findEndDeviceGroup(String mRID) {
        return dataModel.mapper(EndDeviceGroup.class).select(Operator.EQUAL.compare("mRID", mRID)).stream().findFirst();
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
    public Optional<EndDeviceGroup> findEndDeviceGroupByName(String name) {
        return dataModel.mapper(EndDeviceGroup.class).select(Operator.EQUALIGNORECASE.compare("name", name)).stream().findFirst();
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
    public void addEndDeviceQueryProvider(EndDeviceQueryProvider endDeviceQueryProvider) {
        endDeviceQueryProviders.register(endDeviceQueryProvider);
    }

    public void removeEndDeviceQueryProvider(EndDeviceQueryProvider endDeviceQueryProvider) {
        endDeviceQueryProviders.unregister(endDeviceQueryProvider);
    }

    @Override
    public Optional<EndDeviceQueryProvider> pollEndDeviceQueryProvider(String name, Duration duration) throws InterruptedException {
        return endDeviceQueryProviders.get(withName(name), duration);
    }

    private Predicate<EndDeviceQueryProvider> withName(String name) {
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
        return Arrays.asList(MessageSeeds.values());
    }
}
