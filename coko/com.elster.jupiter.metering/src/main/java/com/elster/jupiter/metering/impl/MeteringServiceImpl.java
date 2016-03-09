package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.*;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.metering.impl.config.MetrologyConfigurationServiceImpl;
import com.elster.jupiter.metering.impl.search.PropertyTranslationKeys;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.JournalEntry;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRepresentation;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.conditions.Subquery;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.streams.DecoratedStream;

import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.text.html.Option;
import javax.validation.MessageInterpolator;
import javax.validation.constraints.NotNull;
import java.time.Clock;
import java.time.Instant;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;


@Component(name = "com.elster.jupiter.metering",
        service = {MeteringService.class, ServerMeteringService.class, InstallService.class, PrivilegesProvider.class, MessageSeedProvider.class, TranslationKeyProvider.class},
        property = "name=" + MeteringService.COMPONENTNAME)
public class MeteringServiceImpl implements ServerMeteringService, InstallService, PrivilegesProvider, TranslationKeyProvider, MessageSeedProvider {

    private volatile IdsService idsService;
    private volatile QueryService queryService;
    private volatile PartyService partyService;
    private volatile Clock clock;
    private volatile UserService userService;
    private volatile EventService eventService;
    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;
    private volatile MessageService messageService;
    private volatile JsonService jsonService;
    private volatile FiniteStateMachineService finiteStateMachineService;
    private volatile CustomPropertySetService customPropertySetService;

    private volatile boolean createAllReadingTypes;
    private volatile String[] requiredReadingTypes;

    public MeteringServiceImpl() {
        this.createAllReadingTypes = true;
    }

    @Inject
    public MeteringServiceImpl(
            Clock clock, OrmService ormService, IdsService idsService, EventService eventService, PartyService partyService, QueryService queryService, UserService userService, NlsService nlsService, MessageService messageService, JsonService jsonService,
            FiniteStateMachineService finiteStateMachineService, @Named("createReadingTypes") boolean createAllReadingTypes, @Named("requiredReadingTypes") String requiredReadingTypes, CustomPropertySetService customPropertySetService) {
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
        setMessageService(messageService);
        setJsonService(jsonService);
        setFiniteStateMachineService(finiteStateMachineService);
        setCustomPropertySetService(customPropertySetService);
        activate();
        if (!dataModel.isInstalled()) {
            install();
        }
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
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
    public List<ReadingType> findReadingTypes(List<String> mRids) {
        return dataModel.mapper(ReadingType.class).select(Where.where("mRID").in(mRids));
    }

    @Override
    public Finder<ReadingType> findReadingTypes(ReadingTypeFilter filter) {
        return DefaultFinder.of(ReadingType.class, filter.getCondition(), dataModel);
    }

    @Override
    public Optional<ReadingType> findAndLockReadingTypeByIdAndVersion(String mRID, long version) {
        return dataModel.mapper(ReadingType.class).lockObjectIfVersion(version, mRID);
    }

    @Override
    public void install() {
        try {
            dataModel.install(true, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        new InstallerImpl(this, idsService, partyService, userService, eventService, thesaurus, messageService, createAllReadingTypes, requiredReadingTypes, clock).install();
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("ORM", "IDS", "PRT", "USR", "EVT", "NLS", "FSM", "CPS");
    }

    @Override
    public ServiceLocationBuilderImpl newServiceLocation() {
        return new ServiceLocationBuilderImpl(dataModel);
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
    public Optional<UsagePoint> findAndLockUsagePointByIdAndVersion(long id, long version) {
        return dataModel.mapper(UsagePoint.class).lockObjectIfVersion(version, id);
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
        return dataModel.stream(EndDevice.class)
                .filter(Operator.EQUAL.compare("mRID", mRid))
                .filter(Operator.ISNULL.compare("obsoleteTime"))
                .findFirst();
    }

    @Override
    public ReadingStorer createOverrulingStorer() {
        return withListeners(ReadingStorerImpl.createOverrulingStorer(idsService, eventService));
    }

    @Override
    public ReadingStorer createNonOverrulingStorer() {
        return withListeners(ReadingStorerImpl.createNonOverrulingStorer(idsService, eventService));
    }

    @Override
    public ReadingStorer createUpdatingStorer() {
        return withListeners(ReadingStorerImpl.createUpdatingStorer(idsService, eventService));
    }

    @Override
    public ReadingStorer createUpdatingStorer(StorerProcess process) {
        return withListeners(ReadingStorerImpl.createUpdatingStorer(idsService, eventService, process));
    }

    private ReadingStorer withListeners(ReadingStorerImpl storer) {
        storer.subscribe(BackflowMarker.INSTANT);
        storer.subscribe(OverflowMarker.INSTANT);
        return storer;
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

    @Reference
    public final void setFiniteStateMachineService(FiniteStateMachineService service) {
        // method was added to make sure that the MeteringService is started after the FiniteStateMachineService
        // because the Metering datamodel depends on the FiniteStateMachine datamodel
        this.finiteStateMachineService = service;
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Activate
    public final void activate() {
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(ChannelBuilder.class).to(ChannelBuilderImpl.class);
                bind(MeteringService.class).toInstance(MeteringServiceImpl.this);
                bind(ServerMeteringService.class).toInstance(MeteringServiceImpl.this);
                bind(DataModel.class).toInstance(dataModel);
                bind(EventService.class).toInstance(eventService);
                bind(IdsService.class).toInstance(idsService);
                bind(PartyService.class).toInstance(partyService);
                bind(UserService.class).toInstance(userService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(Clock.class).toInstance(clock);
                bind(CustomPropertySetService.class).toInstance(customPropertySetService);
                bind(MetrologyConfigurationService.class).to(MetrologyConfigurationServiceImpl.class);
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
                where("accountabilities.interval").isEffective(when).and(
                        where("accountabilities.party.representations.interval").isEffective(when).and(
                                where("accountabilities.party.representations.delegate").
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
    public List<ReadingType> getAvailableEquidistantReadingTypes() {
        return dataModel.stream(ReadingType.class).filter(where(ReadingTypeImpl.Fields.equidistant.name()).isEqualTo(true))
                .collect(Collectors.toList());
    }

    @Override
    public List<ReadingType> getAvailableNonEquidistantReadingTypes() {
        return dataModel.stream(ReadingType.class).filter(where(ReadingTypeImpl.Fields.equidistant.name()).isEqualTo(false))
                .collect(Collectors.toList());
    }

    @Override
    public Finder<UsagePoint> getUsagePoints(UsagePointFilter filter) {
        Condition condition = Condition.TRUE;
        if (!Checks.is(filter.getMrid()).emptyOrOnlyWhiteSpace()) {
            condition = condition.and(where("mRID").likeIgnoreCase(filter.getMrid()));
        }
        if (filter.isAccountabilityOnly()) {
            condition = condition.and(hasAccountability());
        }
        return DefaultFinder.of(UsagePoint.class, condition, dataModel);
    }

    @Override
    public ReadingTypeFieldsFactory getReadingTypeFieldCodesFactory() {
        return new ReadingTypeLocalizedFieldsFactory(thesaurus);
    }

    @Override
    public Finder<ReadingType> getReadingTypesByMridFilter(@NotNull ReadingTypeMridFilter filter) {
        return DefaultFinder.of(ReadingType.class, filter.getFilterCondition(), dataModel);
    }

    @Override
    public List<ReadingType> getAllReadingTypesWithoutInterval() {
        Condition withoutIntervals = where(ReadingTypeImpl.Fields.mRID.name()).matches("^0.[0-9]+.0", "");
        return dataModel.mapper(ReadingType.class).select(withoutIntervals);
    }

    @Override
    public Optional<AmrSystem> findAmrSystem(long id) {
        return dataModel.mapper(AmrSystem.class).getOptional(id);
    }

    @Override
    public DataModel getDataModel() {
        return dataModel;
    }

    @Override
    public Thesaurus getThesaurus() {
        return thesaurus;
    }

    AmrSystemImpl createAmrSystem(int id, String name) {
        AmrSystemImpl system = dataModel.getInstance(AmrSystemImpl.class).init(id, name);
        system.save();
        return system;
    }

    @Override
    public EndDeviceEventTypeImpl createEndDeviceEventType(String mRID) {
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

        DecoratedStream.decorate(filteredReadingTypes.stream())
                .map(filteredReadingType -> dataModel.getInstance(ReadingTypeImpl.class).init(filteredReadingType.getFirst(), filteredReadingType.getLast()))
                .map(readingType -> ((ReadingType) readingType))
                .partitionPer(1000)
                .forEach(listPer1000 -> dataModel.mapper(ReadingType.class).persist(listPer1000));
    }

    ServiceCategoryImpl createServiceCategory(ServiceKind serviceKind, boolean active) {
        ServiceCategoryImpl serviceCategory = dataModel.getInstance(ServiceCategoryImpl.class).init(serviceKind);
        serviceCategory.setActive(active);
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
                .map(Arrays::asList)
                .orElse(Collections.emptyList());
    }

    List<Vault> intervalVaults() {
        return idsService.getVault(MeteringService.COMPONENTNAME, ChannelImpl.INTERVALVAULTID)
                .map(Arrays::asList)
                .orElse(Collections.emptyList());
    }

    List<Vault> dailyVaults() {
        return idsService.getVault(MeteringService.COMPONENTNAME, ChannelImpl.DAILYVAULTID)
                .map(Arrays::asList)
                .orElse(Collections.emptyList());
    }

    @Override
    public void changeStateMachine(Instant effective, FiniteStateMachine oldStateMachine, FiniteStateMachine newStateMachine, Subquery deviceAmrIdSubquery) {
        if (effective.isAfter(this.clock.instant())) {
            throw new IllegalArgumentException("Effective timestamp of the statemachine switch over cannot be in the future");
        }
        StateMachineSwitcher
                .forValidation(this.dataModel)
                .validate(effective, oldStateMachine, newStateMachine, deviceAmrIdSubquery);
        StateMachineSwitcher
                .forPublishing(this.dataModel, this.messageService, this.jsonService)
                .publishEvents(effective, oldStateMachine, newStateMachine, deviceAmrIdSubquery);
    }

    @Override
    public String getModuleName() {
        return MeteringService.COMPONENTNAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(userService.createModuleResourceWithPrivileges(MeteringService.COMPONENTNAME, DefaultTranslationKey.RESOURCE_USAGE_POINT
                        .getKey(), DefaultTranslationKey.RESOURCE_USAGE_POINT_DESCRIPTION.getKey(),
                Arrays.asList(
                        Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.ADMINISTER_ANY_USAGEPOINT,
                        Privileges.Constants.VIEW_OWN_USAGEPOINT, Privileges.Constants.ADMINISTER_OWN_USAGEPOINT)));
        resources.add(userService.createModuleResourceWithPrivileges(MeteringService.COMPONENTNAME, DefaultTranslationKey.RESOURCE_READING_TYPE
                        .getKey(), DefaultTranslationKey.RESOURCE_READING_TYPE_DESCRIPTION.getKey(),
                Arrays.asList(Privileges.Constants.ADMINISTER_READINGTYPE, Privileges.Constants.VIEW_READINGTYPE)));
        resources.add(userService.createModuleResourceWithPrivileges(MeteringService.COMPONENTNAME, DefaultTranslationKey.RESOURCE_SERVICE_CATEGORY
                        .getKey(), DefaultTranslationKey.RESOURCE_SERVICE_CATEGORY_DESCRIPTION.getKey(),
                Arrays.asList(Privileges.Constants.VIEW_SERVICECATEGORY)));
        return resources;
    }

    @Override
    public String getComponentName() {
        return MeteringService.COMPONENTNAME;
    }

    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> translationKeys = new ArrayList<>();
        Arrays.stream(DefaultTranslationKey.values()).forEach(translationKeys::add);
        Arrays.stream(ServiceKind.values()).forEach(translationKeys::add);
        Arrays.stream(Privileges.values()).forEach(translationKeys::add);
        Arrays.stream(PropertyTranslationKeys.values()).forEach(translationKeys::add);
        Arrays.stream(UsagePointConnectedKind.values()).forEach(translationKeys::add);
        Arrays.stream(AmiBillingReadyKind.values()).forEach(translationKeys::add);
        translationKeys.addAll(ReadingTypeTranslationKeys.allKeys());
        return translationKeys;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Override
    public MultiplierType createMultiplierType(String name) {
        MultiplierTypeImpl multiplierType = MultiplierTypeImpl.from(dataModel, name);
        multiplierType.save();
        return multiplierType;
    }

    @Override
    public Optional<MultiplierType> getMultiplierType(String name) {
        return dataModel.mapper(MultiplierType.class).getOptional(name);
    }

    @Override
    public List<MultiplierType> getMultiplierTypes() {
        return dataModel.mapper(MultiplierType.class).find();
    }

    @Override
    public LocationBuilder newLocationBuilder() {
        return new LocationBuilderImpl(dataModel);
    }

    @Override
    public Optional<Location> findDeviceLocation(String mRID) {
        return findMeter(mRID).isPresent() ? Optional.of(findMeter(mRID).get().getLocation()) : Optional.empty();
    }


    @Override
    public Optional<Location> findDeviceLocation(long id) {
        return findMeter(id).isPresent() ? Optional.of(findMeter(id).get().getLocation()) : Optional.empty();
    }

    @Override
    public Optional<Location> findUsagePointLocation(String mRID) {
        return findUsagePoint(mRID).isPresent() ? Optional.of(findUsagePoint(mRID).get().getLocation()) : Optional.empty();
    }

    @Override
    public Optional<Location> findUsagePointLocation(long id) {
        return findUsagePoint(id).isPresent() ? Optional.of(findUsagePoint(id).get().getLocation()) : Optional.empty();
    }

    @Override
    public Optional<List<LocationMember>> getLocationMembers(long locationId) {
        return Optional.of(dataModel.query(LocationMember.class).select(Operator.EQUAL.compare("id", locationId)));

    }

    @Override
    public Optional<LocationMember> getLocalizedLocationMember(long locationId, String locale) {
        Optional<LocationMember> result = Optional.empty();
        Condition idCondition = Operator.EQUAL.compare("id", locationId);
        Condition localeCondition = Operator.EQUALIGNORECASE.compare("locale", locale);
        List<LocationMember> locationMemberList = dataModel.query(LocationMember.class).select(idCondition.and(localeCondition));
        if (!locationMemberList.isEmpty()) {
            result = Optional.of(locationMemberList.get(0));
        }
        return result;

    }

}