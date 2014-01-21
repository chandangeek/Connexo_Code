package com.elster.jupiter.metering.impl;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EnumeratedUsagePointGroup;
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
import com.elster.jupiter.metering.UsagePointAccountability;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.JournalEntry;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRepresentation;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.time.Clock;
import com.google.common.base.Optional;
import com.google.inject.AbstractModule;

@Component(name = "com.elster.jupiter.metering", service = {MeteringService.class, InstallService.class}, property = "name=" + MeteringService.COMPONENTNAME)
public class MeteringServiceImpl implements MeteringService, InstallService {

    private volatile IdsService idsService;
    private volatile QueryService queryService;
    private volatile PartyService partyService;
    private volatile Clock clock;
    private volatile UserService userService;
    private volatile EventService eventService;
    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;

    public MeteringServiceImpl() {
    }

    @Inject
    public MeteringServiceImpl(Clock clock, OrmService ormService, IdsService idsService, EventService eventService, PartyService partyService, QueryService queryService, UserService userService, NlsService nlsService) {
        this.clock = clock;
        setOrmService(ormService);
        setIdsService(idsService);
        setEventService(eventService);
        setPartyService(partyService);
        setQueryService(queryService);
        setUserService(userService);
        setNlsService(nlsService);
        activate();
        if (!dataModel.isInstalled()) {
        	install();
        }
    }

    @Override
    public Optional<ServiceCategory> getServiceCategory(ServiceKind kind) {
        return dataModel.mapper(ServiceCategory.class).getOptional(kind);
    }

    @Override
    public Optional<ReadingType> getReadingType(String mRid) {
        return dataModel.mapper(ReadingType.class).getOptional(mRid);
    }

    @Override
    public void install() {
    	dataModel.install(true, true);
        new InstallerImpl(this, idsService, partyService, userService, eventService).install();
    }

    @Override
    public ServiceLocation newServiceLocation() {
        return new ServiceLocationImpl(dataModel, eventService);
    }

    @Override
    public Optional<ServiceLocation> findServiceLocation(String mRID) {
        return dataModel.mapper(ServiceLocation.class).getUnique("mRID", mRID);
    }

    @Override
    public Optional<ServiceLocation> findServiceLocation(long id) {
        return dataModel.mapper(ServiceLocation.class).getOptional(id);
    }

    @Override
    public Optional<UsagePoint> findUsagePoint(long id) {
        return dataModel.mapper(UsagePoint.class).getOptional(id);
    }

    @Override
    public ReadingStorer createOverrulingStorer() {
        return new ReadingStorerImpl(idsService, eventService, true);
    }

    @Override
    public ReadingStorer createNonOverrulingStorer() {
        return new ReadingStorerImpl(idsService, eventService, false);
    }

    @Override
    public Query<UsagePoint> getUsagePointQuery() {
        return queryService.wrap(
                dataModel.query(
                        UsagePoint.class,
                        ServiceLocation.class,
                        MeterActivation.class,
                        EndDevice.class,
                        UsagePointAccountability.class,
                        Party.class,
                        PartyRepresentation.class));
    }
    
    @SuppressWarnings("unchecked")
	@Override
    public Query<Meter> getMeterQuery() {
    	QueryExecutor<?> executor = dataModel.query(EndDevice.class,
                MeterActivation.class,
                UsagePoint.class,
                ServiceLocation.class,
                Channel.class);
    	executor.setRestriction(Operator.EQUAL.compare("class", Meter.TYPE_IDENTIFIER));
    	return queryService.wrap((QueryExecutor<Meter>) executor);
    }

    @Override
    public Query<MeterActivation> getMeterActivationQuery() {
        return queryService.wrap(
                dataModel.query(MeterActivation.class,
                        UsagePoint.class,
                        EndDevice.class,
                        ServiceLocation.class));
    }

    @Override
    public Query<ServiceLocation> getServiceLocationQuery() {
        return queryService.wrap(
                dataModel.query(ServiceLocation.class,
                        UsagePoint.class,
                        MeterActivation.class,
                        EndDevice.class));
    }

    @Override
    public List<JournalEntry<ServiceLocation>> findServiceLocationJournal(long id) {
        return dataModel.mapper(ServiceLocation.class).getJournal(id);
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(COMPONENTNAME, "CIM Metering");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
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
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
            	bind(ChannelBuilder.class).to(ChannelBuilderImpl.class);
                bind(MeteringService.class).toInstance(MeteringServiceImpl.this);
                bind(DataModel.class).toInstance(dataModel);
                bind(EventService.class).toInstance(eventService);
                bind(IdsService.class).toInstance(idsService);
                bind(PartyService.class).toInstance(partyService);
                bind(Thesaurus.class).toInstance(thesaurus);
            }
        });
    }

    @Deactivate
    public void deactivate() {
    }

    @Override
    public Condition hasAccountability() {
        return hasAccountability(clock.now());
    }

    @Override
    public Condition hasAccountability(Date when) {
    	return 
    		Where.where("accountabilities.interval").isEffective(when).and(
    		Where.where("accountabilities.party.representations.interval").isEffective(when).and(
    		Where.where("accountabilities.party.representations.delegate").
    			isEqualTo(dataModel.getPrincipal().getName())));
    }

    public Clock getClock() {
        return clock;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(MeteringService.COMPONENTNAME, Layer.DOMAIN);
    }

    @Override
    public QueryUsagePointGroup createQueryUsagePointGroup(Condition condition) {
        QueryUsagePointGroupImpl queryUsagePointGroup = new QueryUsagePointGroupImpl(dataModel, this);
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
    public Optional<MeterActivation> findMeterActivation(long id) {
        return dataModel.mapper(MeterActivation.class).getOptional(id);
    }

    @Override
    public Optional<Channel> findChannel(long id) {
        return dataModel.mapper(Channel.class).getOptional(id);
    }

    @Override
    public List<ReadingType> getAvailableReadingTypes() {
        return dataModel.mapper(ReadingType.class).find();
    }

    @Override
    public Optional<AmrSystem> findAmrSystem(long id) {
        return dataModel.mapper(AmrSystem.class).getOptional(id);
    }

    DataModel getDataModel() {
        return dataModel;
    }
    
    AmrSystemImpl createAmrSystem(int id, String name) {
    	AmrSystemImpl system = dataModel.getInstance(AmrSystemImpl.class).init(id, name);
    	system.save();
    	return system;
    }
    
    EndDeviceEventTypeImpl createEndDeviceEventType(String mRID) {
    	EndDeviceEventTypeImpl endDeviceEventType = dataModel.getInstance(EndDeviceEventTypeImpl.class).init(mRID);
    	dataModel.persist(endDeviceEventType);
    	return endDeviceEventType;
    }

	ReadingTypeImpl createReadingType(String mRID, String aliasName) {
		ReadingTypeImpl readingType = dataModel.getInstance(ReadingTypeImpl.class).init(mRID, aliasName);
		dataModel.persist(readingType);
		return readingType;
	}
	
	ServiceCategoryImpl createServiceCategory(ServiceKind serviceKind) {
		ServiceCategoryImpl serviceCategory = dataModel.getInstance(ServiceCategoryImpl.class).init(serviceKind);
		dataModel.persist(serviceCategory);
		return serviceCategory;
	}
    
    
    
}
