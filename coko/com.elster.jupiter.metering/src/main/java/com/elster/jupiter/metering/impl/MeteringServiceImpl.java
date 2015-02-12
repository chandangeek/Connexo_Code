package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.PurgeConfiguration;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.ServiceLocation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointAccountability;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.metering.events.EndDeviceEventType;
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
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.conditions.Where;
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.MessageInterpolator;
import java.time.Clock;
import java.time.Instant;
import java.time.Period;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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

    private volatile boolean createAllReadingTypes;
    private volatile String[] requiredReadingTypes;

    public MeteringServiceImpl() {
        this.createAllReadingTypes = true;
    }

    @Inject
    public MeteringServiceImpl(Clock clock, OrmService ormService, IdsService idsService, EventService eventService, PartyService partyService, QueryService queryService, UserService userService, NlsService nlsService,
                               @Named("createReadingTypes") boolean createAllReadingTypes,@Named("requiredReadingTypes") String requiredReadingTypes) {
        this.clock = clock;
        this.createAllReadingTypes = createAllReadingTypes;
        this.requiredReadingTypes = requiredReadingTypes.split(";");
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
        try {
            dataModel.install(true, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        new InstallerImpl(this, idsService, partyService, userService, eventService, thesaurus, createAllReadingTypes, requiredReadingTypes).install();
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("ORM", "IDS", "PRT", "USR", "EVT", "NLS");
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
    public Optional<UsagePoint> findUsagePoint(String mRID) {
        List<UsagePoint> usagePoints = dataModel.mapper(UsagePoint.class).select(Operator.EQUAL.compare("mRID", mRID));
        return usagePoints.isEmpty() ? Optional.empty() : Optional.of(usagePoints.get(0));
    }

    @Override
    public Optional<Meter> findMeter(long id) {
        return dataModel.mapper(Meter.class).getOptional(id);
    }

    @Override
    public Optional<EndDevice> findEndDevice(long id) {
        return dataModel.mapper(EndDevice.class).getOptional(id);
    }

    @Override
    public Optional<Meter> findMeter(String mRid) {
        List<Meter> meters = dataModel.mapper(Meter.class).select(Operator.EQUAL.compare("mRID", mRid));
        return meters.isEmpty() ? Optional.empty() : Optional.of(meters.get(0));
    }

    @Override
    public Optional<EndDevice> findEndDevice(String mRid) {
        List<EndDevice> endDevices = dataModel.mapper(EndDevice.class).select(Operator.EQUAL.compare("mRID", mRid));
        return endDevices.isEmpty() ? Optional.empty() : Optional.of(endDevices.get(0));
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
                        UsagePointDetail.class,
                        ServiceLocation.class,
                        MeterActivation.class,
                        EndDevice.class,
                        UsagePointAccountability.class,
                        Party.class,
                        PartyRepresentation.class));
    }

    @Override
    public Query<EndDevice> getEndDeviceQuery() {
        return queryService.wrap(
                dataModel.query(
                        EndDevice.class,
                        MeterActivation.class
                )
        );
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
    public final void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(COMPONENTNAME, "CIM Metering");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
    }

    @Reference
    public final void setIdsService(IdsService idsService) {
        this.idsService = idsService;
    }

    @Reference
    public final void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }

    @Reference
    public final void setPartyService(PartyService partyService) {
        this.partyService = partyService;
    }

    @Reference
    public final void setUserService(UserService userService) {
        this.userService = userService;
    }

    public EventService getEventService() {
        return eventService;
    }

    @Reference
    public final void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Activate
    public final void activate() {
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
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(Clock.class).toInstance(clock);
            }
        });
    }

    @Deactivate
    public final void deactivate() {
    }

    @Override
    public Condition hasAccountability() {
        return hasAccountability(clock.instant());
    }

    @Override
    public Condition hasAccountability(Instant when) {
        return
                Where.where("accountabilities.interval").isEffective(when).and(
                        Where.where("accountabilities.party.representations.interval").isEffective(when).and(
                                Where.where("accountabilities.party.representations.delegate").
                                        isEqualTo(dataModel.getPrincipal().getName())));
    }

    @Override
    public List<EndDeviceEventType> getAvailableEndDeviceEventTypes() {
        return dataModel.mapper(EndDeviceEventType.class).find();
    }

    @Override
    public Optional<EndDeviceEventType> getEndDeviceEventType(String mRID) {
        return dataModel.mapper(EndDeviceEventType.class).getOptional(mRID);
    }

    public Clock getClock() {
        return clock;
    }

    @Reference
    public final void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public final void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(MeteringService.COMPONENTNAME, Layer.DOMAIN);
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
    public List<ReadingType> getAllReadingTypesWithoutInterval() {
        Condition withoutIntervals = Where.where("mRID").matches("^0.[0-9]+.0", "");
        return dataModel.mapper(ReadingType.class).select(withoutIntervals);
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

    @Override
    public ReadingType createReadingType(String mRID, String aliasName) {
        ReadingTypeImpl readingType = dataModel.getInstance(ReadingTypeImpl.class).init(mRID, aliasName);
        dataModel.persist(readingType);
        return readingType;
    }

    // bulk insert
    public void createAllReadingTypes(List<Pair<String, String>> readingTypes) {
        List<ReadingType> availableReadingTypes = getAvailableReadingTypes();
        List<String> availableReadingTypeCodes = availableReadingTypes.parallelStream().map(ReadingType::getMRID).collect(Collectors.toList());
        List<Pair<String, String>> filteredReadingTypes = readingTypes.parallelStream().filter(readingTypePair -> !availableReadingTypeCodes.contains(readingTypePair.getFirst())).collect(Collectors.toList());
        List<ReadingType> preStoredReadingTypes = filteredReadingTypes.parallelStream().map(filteredReadingType -> dataModel.getInstance(ReadingTypeImpl.class).init(filteredReadingType.getFirst(), filteredReadingType.getLast())).collect(Collectors.toList());
        List<List<ReadingType>> chunks = createChunksOf(preStoredReadingTypes, 1000);
        chunks.stream().forEach(chunk -> dataModel.mapper(ReadingType.class).persist(chunk));
    }

    List<List<ReadingType>> createChunksOf(List<ReadingType> readingTypes, int chunkSize) {
        List<List<ReadingType>> partitions = new LinkedList<>();
        for (int i = 0; i < readingTypes.size(); i += chunkSize) {
            partitions.add(readingTypes.subList(i, i + Math.min(chunkSize, readingTypes.size() - i)));
        }
        return partitions;
    }

    ServiceCategoryImpl createServiceCategory(ServiceKind serviceKind) {
        ServiceCategoryImpl serviceCategory = dataModel.getInstance(ServiceCategoryImpl.class).init(serviceKind);
        dataModel.persist(serviceCategory);
        return serviceCategory;
    }

    @Override
    public void purge(PurgeConfiguration purgeConfiguration) {
        new DataPurger(purgeConfiguration, this).purge();
    }

    @Override
    public void configurePurge(PurgeConfiguration purgeConfiguration) {
        registerVaults().stream()
                .filter(testRetention(purgeConfiguration.registerRetention()))
                .forEach(vault -> vault.setRetentionDays(purgeConfiguration.registerDays()));
        intervalVaults().stream()
                .filter(testRetention(purgeConfiguration.intervalRetention()))
                .forEach(vault -> vault.setRetentionDays(purgeConfiguration.intervalDays()));
        dailyVaults().stream()
                .filter(testRetention(purgeConfiguration.dailyRetention()))
                .forEach(vault -> vault.setRetentionDays(purgeConfiguration.dailyDays()));
    }

    private Predicate<Vault> testRetention(Optional<Period> periodHolder) {
        return vault -> periodHolder.filter(period -> !vault.getRetention().equals(period)).isPresent();
    }

    List<Vault> registerVaults() {
        return idsService.getVault(MeteringService.COMPONENTNAME, ChannelImpl.IRREGULARVAULTID)
                .map(vault -> Arrays.asList(vault))
                .orElse(Collections.emptyList());
    }

    List<Vault> intervalVaults() {
        return idsService.getVault(MeteringService.COMPONENTNAME, ChannelImpl.INTERVALVAULTID)
                .map(vault -> Arrays.asList(vault))
                .orElse(Collections.emptyList());
    }

    List<Vault> dailyVaults() {
        return idsService.getVault(MeteringService.COMPONENTNAME, ChannelImpl.DAILYVAULTID)
                .map(vault -> Arrays.asList(vault))
                .orElse(Collections.emptyList());
    }

}
