package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EndDeviceQueryProvider;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedUsagePointGroup;
import com.elster.jupiter.metering.groups.EventType;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.metering.groups.QueryUsagePointGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.util.concurrent.CopyOnWriteServiceContainer;
import com.elster.jupiter.util.concurrent.OptionalServiceContainer;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.conditions.Where;
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

@Component(
        name = "com.elster.jupiter.metering",
        service = {MeteringGroupsService.class, InstallService.class, TranslationKeyProvider.class},
        property = "name=" + MeteringGroupsService.COMPONENTNAME,
        immediate = true)
public class MeteringGroupsServiceImpl implements MeteringGroupsService, InstallService, TranslationKeyProvider {

    private static final Logger LOGGER = Logger.getLogger(MeteringGroupsServiceImpl.class.getSimpleName());

    private volatile DataModel dataModel;
    private volatile MeteringService meteringService;
    private volatile QueryService queryService;
    private volatile EventService eventService;

    private final OptionalServiceContainer<EndDeviceQueryProvider> endDeviceQueryProviders = new CopyOnWriteServiceContainer<>();

    public MeteringGroupsServiceImpl() {
    }

    @Inject
    public MeteringGroupsServiceImpl(OrmService ormService, MeteringService meteringService, QueryService queryService, EventService eventService) {
        setOrmService(ormService);
        setMeteringService(meteringService);
        setQueryService(queryService);
        setEventService(eventService);
        activate();
        if (!dataModel.isInstalled()) {
            install();
        }
    }

    @Override
    public void install() {
        dataModel.install(true, true);
        createEventTypes();
    }

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            try {
                eventType.install(eventService);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Could not create event type : " + eventType.name(), e);
            }
        }
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("ORM", "MTR");
    }

    @Activate
    public void activate() {
        try {
            dataModel.register(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(MeteringGroupsService.class).toInstance(MeteringGroupsServiceImpl.this);
                    bind(MeteringService.class).toInstance(meteringService);
                    bind(DataModel.class).toInstance(dataModel);
                    bind(EventService.class).toInstance(eventService);
                    bind(QueryService.class).toInstance(queryService);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Deactivate
    public void deactivate() {
    }

    @Override
    public QueryUsagePointGroup createQueryUsagePointGroup(Condition condition) {
        QueryUsagePointGroupImpl queryUsagePointGroup = new QueryUsagePointGroupImpl(dataModel, meteringService);
        queryUsagePointGroup.setCondition(condition);
        return queryUsagePointGroup;
    }

    @Override
    public Optional<QueryUsagePointGroup> findQueryUsagePointGroup(long id) {
        return dataModel.mapper(QueryUsagePointGroup.class).getOptional(id);
    }

    @Override
    public EnumeratedUsagePointGroup createEnumeratedUsagePointGroup(String name) {
        EnumeratedUsagePointGroup group = new EnumeratedUsagePointGroupImpl(dataModel);
        group.setName(name);
        return group;
    }

    @Override
    public Optional<EnumeratedUsagePointGroup> findEnumeratedUsagePointGroup(long id) {
        return dataModel.mapper(EnumeratedUsagePointGroup.class).getOptional(id);
    }

    @Override
    public Optional<UsagePointGroup> findUsagePointGroup(String mRID) {
        return dataModel.mapper(UsagePointGroup.class).select(Operator.EQUAL.compare("mRID", mRID)).stream().findFirst();
    }

    @Override
    public QueryEndDeviceGroup createQueryEndDeviceGroup(Condition condition) {
        QueryEndDeviceGroupImpl queryUsagePointGroup = new QueryEndDeviceGroupImpl(dataModel, this, eventService);
        queryUsagePointGroup.setCondition(condition);
        return queryUsagePointGroup;
    }

    @Override
    public EnumeratedEndDeviceGroup createEnumeratedEndDeviceGroup(String name) {
        EnumeratedEndDeviceGroup group = new EnumeratedEndDeviceGroupImpl(dataModel, eventService, queryService);
        group.setName(name);
        return group;
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
