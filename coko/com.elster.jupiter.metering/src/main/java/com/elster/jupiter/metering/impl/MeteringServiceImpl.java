package com.elster.jupiter.metering.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EnumeratedUsagePointGroup;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.QueryUsagePointGroup;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.ServiceLocation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.JournalEntry;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.cache.CacheService;
import com.elster.jupiter.orm.cache.ComponentCache;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Expression;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.time.Clock;
import com.google.common.base.Optional;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;

import static com.elster.jupiter.metering.impl.Bus.COMPONENTNAME;

@Component(name = "com.elster.jupiter.metering", service = {MeteringService.class, InstallService.class}, property = "name=" + Bus.COMPONENTNAME)
public class MeteringServiceImpl implements MeteringService, InstallService, ServiceLocator {

    private volatile OrmClient ormClient;
    private volatile ComponentCache componentCache;
    private volatile IdsService idsService;
    private volatile QueryService queryService;
    private volatile PartyService partyService;
    private volatile Clock clock;
    private volatile UserService userService;
    private volatile EventService eventService;

    public MeteringServiceImpl() {
    }

    @Inject
    public MeteringServiceImpl(Clock clock, OrmService ormService, IdsService idsService, CacheService cacheService, EventService eventService, PartyService partyService, QueryService queryService, UserService userService) {
        this.clock = clock;
        initOrmClient(ormService);
        this.idsService = idsService;
        initComponentCache(cacheService);
        this.eventService = eventService;
        this.partyService = partyService;
        this.queryService = queryService;
        this.userService = userService;
        activate();
        install();
    }

    @Override
    public Optional<ServiceCategory> getServiceCategory(ServiceKind kind) {
        return getOrmClient().getServiceCategoryFactory().get(kind);
    }

    @Override
    public Optional<ReadingType> getReadingType(String mRid) {
        return getOrmClient().getReadingTypeFactory().get(mRid);
    }

    @Override
    public void install() {
        new InstallerImpl().install(true, true, true);
    }

    @Override
    public ServiceLocation newServiceLocation() {
        return new ServiceLocationImpl();
    }

    @Override
    public Optional<ServiceLocation> findServiceLocation(String mRID) {
        return getOrmClient().getServiceLocationFactory().getUnique("mRID", mRID);
    }

    @Override
    public Optional<ServiceLocation> findServiceLocation(long id) {
        return getOrmClient().getServiceLocationFactory().get(id);
    }

    @Override
    public Optional<UsagePoint> findUsagePoint(long id) {
        return getOrmClient().getUsagePointFactory().get(id);
    }

    @Override
    public ReadingStorer createOverrulingStorer() {
        return new ReadingStorerImpl(true);
    }

    @Override
    public ReadingStorer createNonOverrulingStorer() {
        return new ReadingStorerImpl(false);
    }

    @Override
    public EndDevice createEndDevice(AmrSystem amrSystem, String amrId, String mRID) {
        return new EndDeviceImpl(amrSystem, amrId, mRID);
    }

    @Override
    public Query<UsagePoint> getUsagePointQuery() {
        return getQueryService().wrap(
                getOrmClient().getUsagePointFactory().with(
                        getOrmClient().getServiceLocationFactory(),
                        getOrmClient().getMeterActivationFactory(),
                        //	getOrmClient().getChannelFactory(),?
                        getOrmClient().getEndDeviceFactory()));
    }
    
    @SuppressWarnings("unchecked")
	@Override
    public Query<Meter> getMeterQuery() {
    	QueryExecutor<?> executor = getOrmClient().getEndDeviceFactory().with(
    			getOrmClient().getMeterActivationFactory(),
    			getOrmClient().getUsagePointFactory(),
    			getOrmClient().getServiceLocationFactory(),
    			getOrmClient().getChannelFactory());
    	executor.setRestriction(Operator.EQUAL.compare("class", Meter.TYPE_IDENTIFIER));
    	return getQueryService().wrap((QueryExecutor<Meter>) executor);
    }

    @Override
    public Query<MeterActivation> getMeterActivationQuery() {
        return getQueryService().wrap(
                getOrmClient().getMeterActivationFactory().with(
                        getOrmClient().getUsagePointFactory(),
                        getOrmClient().getEndDeviceFactory(),
                        getOrmClient().getServiceLocationFactory()));
    }

    @Override
    public Query<ServiceLocation> getServiceLocationQuery() {
        return getQueryService().wrap(
                getOrmClient().getServiceLocationFactory().with(
                        getOrmClient().getUsagePointFactory(),
                        getOrmClient().getMeterActivationFactory(),
                        //getOrmClient().getChannelFactory(),
                        getOrmClient().getEndDeviceFactory()));
    }

    @Override
    public List<JournalEntry<ServiceLocation>> findServiceLocationJournal(long id) {
        return getOrmClient().getServiceLocationFactory().getJournal(id);
    }

    @Override
    public OrmClient getOrmClient() {
        return ormClient;
    }

    @Override
    public ComponentCache getComponentCache() {
        return componentCache;
    }

    @Override
    public IdsService getIdsService() {
        return idsService;
    }

    @Override
    public QueryService getQueryService() {
        return queryService;
    }

    @Override
    public PartyService getPartyService() {
        return partyService;
    }

	@Override
	public UserService getUserService() {
		return userService;
	}
	

    @Reference
    public void setOrmService(OrmService ormService) {
        initOrmClient(ormService);
    }

    private void initOrmClient(OrmService ormService) {
        DataModel dataModel = ormService.newDataModel(COMPONENTNAME, "CIM Metering");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
        ormService.register(dataModel);
        this.ormClient = new OrmClientImpl(dataModel);
    }

    @Reference(name = "ZCacheService")
    public void setCacheService(CacheService cacheService) {
        initComponentCache(cacheService);
    }

    private void initComponentCache(CacheService cacheService) {
        this.componentCache = cacheService.createComponentCache(ormClient.getDataModel());
    }

    @Reference
    public void setIdsService(IdsService idsService) {
        this.idsService = idsService;
    }

    @Reference
    public void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }

    @Reference
    public void setPartyService(PartyService partyService) {
        this.partyService = partyService;
    }

    @Reference
    public void setUserService(UserService userService) {
    	this.userService = userService;
    }

    public EventService getEventService() {
        return eventService;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Activate
    public void activate() {
        Bus.setServiceLocator(this);
    }

    @Deactivate
    public void deactivate() {
        Bus.clearServiceLocator(this);
    }

    @Override
    public Condition hasAccountability() {
        return hasAccountability(new Date());
    }

    @Override
    public Condition hasAccountability(Date when) {
        return Expression.create(new HasAccountabilitiyFragment(when));
    }

    public Clock getClock() {
        return clock;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Override
    public ChannelBuilder getChannelBuilder() {
        return new ChannelBuilderImpl();
    }

    @Override
    public QueryUsagePointGroup createQueryUsagePointGroup(Condition condition) {
        QueryUsagePointGroupImpl queryUsagePointGroup = new QueryUsagePointGroupImpl();
        queryUsagePointGroup.setCondition(condition);
        return queryUsagePointGroup;
    }

    @Override
    public Optional<QueryUsagePointGroup> findQueryUsagePointGroup(long id) {
        return getOrmClient().getQueryUsagePointGroupFactory().get(id);
    }

    @Override
    public EnumeratedUsagePointGroup createEnumeratedUsagePointGroup(String name) {
        EnumeratedUsagePointGroup group = new EnumeratedUsagePointGroupImpl();
        group.setName(name);
        return group;
    }

    @Override
    public Optional<EnumeratedUsagePointGroup> findEnumeratedUsagePointGroup(long id) {
        return getOrmClient().getEnumeratedUsagePointGroupFactory().get(id);
    }

    @Override
    public Optional<MeterActivation> findMeterActivation(long id) {
        return getOrmClient().getMeterActivationFactory().get(id);
    }

    @Override
    public Optional<Channel> findChannel(long id) {
        return getOrmClient().getChannelFactory().get(id);
    }

    @Override
    public List<ReadingType> getAvailableReadingTypes() {
        return getOrmClient().getReadingTypeFactory().find();
    }

    @Override
	public MeteringService getMeteringService() {
		return this;
	}

    @Override
    public Optional<AmrSystem> findAmrSystem(long id) {
        return getOrmClient().getAmrSystemFactory().get(id);
    }

}
