package com.elster.jupiter.metering.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.ServiceLocation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.cache.CacheService;
import com.elster.jupiter.orm.cache.ComponentCache;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Expression;
import com.elster.jupiter.util.time.Clock;
import com.google.common.base.Optional;
import org.osgi.service.component.annotations.*;

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
    public ReadingStorer createRegularStorer() {
        return new ReadingStorerImpl(false);
    }

    @Override
    public Query<UsagePoint> getUsagePointQuery() {
        return getQueryService().wrap(
                getOrmClient().getUsagePointFactory().with(
                        getOrmClient().getServiceLocationFactory(),
                        getOrmClient().getMeterActivationFactory(),
                        //	getOrmClient().getChannelFactory(),
                        getOrmClient().getMeterFactory()));
    }

    @Override
    public Query<MeterActivation> getMeterActivationQuery() {
        return getQueryService().wrap(
                getOrmClient().getMeterActivationFactory().with(
                        getOrmClient().getUsagePointFactory(),
                        getOrmClient().getMeterFactory(),
                        getOrmClient().getServiceLocationFactory()));
    }

    @Override
    public Query<ServiceLocation> getServiceLocationQuery() {
        return getQueryService().wrap(
                getOrmClient().getServiceLocationFactory().with(
                        getOrmClient().getUsagePointFactory(),
                        getOrmClient().getMeterActivationFactory(),
                        //getOrmClient().getChannelFactory(),
                        getOrmClient().getMeterFactory()));
    }

    @Override
    public List<ServiceLocation> findServiceLocationJournal(long id) {
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
        DataModel dataModel = ormService.newDataModel(COMPONENTNAME, "CIM Metering");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
        this.ormClient = new OrmClientImpl(dataModel);
    }

    @Reference(name = "ZCacheService")
    public void setCacheService(CacheService cacheService) {
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
        Bus.setServiceLocator(null);
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
}
