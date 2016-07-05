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
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.AmiBillingReadyKind;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.BypassStatus;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceControlType;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.LocationMember;
import com.elster.jupiter.metering.LocationTemplate;
import com.elster.jupiter.metering.LocationTemplate.TemplateField;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.metering.PurgeConfiguration;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ReadingTypeFieldsFactory;
import com.elster.jupiter.metering.ReadingTypeFilter;
import com.elster.jupiter.metering.ReadingTypeMridFilter;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.ServiceLocation;
import com.elster.jupiter.metering.StorerProcess;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointAccountability;
import com.elster.jupiter.metering.UsagePointConnectedKind;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.metering.UsagePointFilter;
import com.elster.jupiter.metering.ami.HeadEndInterface;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.metering.impl.aggregation.CalculatedReadingRecordFactory;
import com.elster.jupiter.metering.impl.aggregation.CalculatedReadingRecordFactoryImpl;
import com.elster.jupiter.metering.impl.config.MeterActivationValidatorsWhiteboard;
import com.elster.jupiter.metering.impl.config.MetrologyConfigurationServiceImpl;
import com.elster.jupiter.metering.impl.config.ServerMetrologyConfigurationService;
import com.elster.jupiter.metering.impl.search.PropertyTranslationKeys;
import com.elster.jupiter.metering.impl.search.UsagePointRequirementsSearchDomain;
import com.elster.jupiter.metering.impl.upgraders.UpgraderV10_2;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.JournalEntry;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRepresentation;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.YesNoAnswer;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.conditions.Subquery;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.streams.DecoratedStream;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.validation.MessageInterpolator;
import javax.validation.constraints.NotNull;
import java.time.Clock;
import java.time.Instant;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.metering.impl.LocationTemplateImpl.LocationTemplateElements;
import static com.elster.jupiter.metering.impl.LocationTemplateImpl.TemplateFieldImpl;
import static com.elster.jupiter.orm.Version.version;
import static com.elster.jupiter.upgrade.InstallIdentifier.identifier;
import static com.elster.jupiter.util.conditions.Where.where;


@Component(name = "com.elster.jupiter.metering",
        service = {MeteringService.class, ServerMeteringService.class, MessageSeedProvider.class, TranslationKeyProvider.class},
        property = "name=" + MeteringService.COMPONENTNAME)
@Singleton
public class MeteringServiceImpl implements ServerMeteringService, TranslationKeyProvider, MessageSeedProvider {

    private volatile IdsService idsService;
    private volatile QueryService queryService;
    private volatile PartyService partyService;
    private volatile Clock clock;
    private volatile UserService userService;
    private volatile EventService eventService;
    private volatile DataModel dataModel;
    private volatile NlsService nlsService;
    private volatile Thesaurus thesaurus;
    private volatile MessageService messageService;
    private volatile JsonService jsonService;
    private volatile FiniteStateMachineService finiteStateMachineService;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile MetrologyConfigurationServiceImpl metrologyConfigurationService;
    private volatile SearchService searchService;
    private volatile PropertySpecService propertySpecService;
    private volatile UsagePointRequirementsSearchDomain usagePointRequirementsSearchDomain;
    private volatile LicenseService licenseService;
    private volatile MeterActivationValidatorsWhiteboard meterActivationValidatorsWhiteboard;
    private volatile UpgradeService upgradeService;
    private List<ServiceRegistration> serviceRegistrations = new ArrayList<>();

    private volatile boolean createAllReadingTypes;
    private volatile String[] requiredReadingTypes;
    private volatile LocationTemplate locationTemplate;
    private static ImmutableList<TemplateField> locationTemplateMembers;
    private static String LOCATION_TEMPLATE = "com.elster.jupiter.location.template";
    private static String LOCATION_TEMPLATE_MANDATORY_FIELDS = "com.elster.jupiter.location.template.mandatoryfields";
    private static final String MDC_URL = "com.energyict.mdc.url";
    private static final String ENERGY_AXIS_URL = "com.elster.jupiter.energyaxis.url";
    private Map<KnownAmrSystem, String> supportedApplicationsUrls = new HashMap<>();

    private List<HeadEndInterface> headEndInterfaces = new CopyOnWriteArrayList<>();

    public MeteringServiceImpl() {
        this.createAllReadingTypes = true;
    }

    @Inject
    public MeteringServiceImpl(
            Clock clock, OrmService ormService, IdsService idsService, EventService eventService, PartyService partyService, QueryService queryService, UserService userService, NlsService nlsService, MessageService messageService, JsonService jsonService,
            FiniteStateMachineService finiteStateMachineService, @Named("createReadingTypes") boolean createAllReadingTypes, @Named("requiredReadingTypes") String requiredReadingTypes, CustomPropertySetService customPropertySetService,
            PropertySpecService propertySpecService, SearchService searchService, LicenseService licenseService, MeterActivationValidatorsWhiteboard meterActivationValidatorsWhiteboard, UpgradeService upgradeService) {
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
        setPropertySpecService(propertySpecService);
        setSearchService(searchService);
        setLicenseService(licenseService);
        setMeterActivationValidatorsWhiteboard(meterActivationValidatorsWhiteboard);
        setUpgradeService(upgradeService);
        activate(null);
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
    @Reference(name = "ZHeadEndInterface", cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addHeadEndInterface(HeadEndInterface headEndInterface) {
        headEndInterfaces.add(headEndInterface);
    }

    @Override
    @SuppressWarnings("unused")
    public void removeHeadEndInterface(HeadEndInterface headEndInterface) {
        headEndInterfaces.remove(headEndInterface);
    }

    @Override
    public List<HeadEndInterface> getHeadEndInterfaces() {
        return Collections.unmodifiableList(this.headEndInterfaces);
    }

    @Override
    public Optional<HeadEndInterface> getHeadEndInterface(String amrSystem) {
        return headEndInterfaces.stream()
                .filter(itf -> itf.getAmrSystem().equalsIgnoreCase(amrSystem)).findFirst();
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
                        Location.class, LocationMember.class,
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

    @Override
    public Query<Meter> getMeterQuery() {
        QueryExecutor<?> executor = dataModel.query(EndDevice.class, Location.class, LocationMember.class, EndDeviceLifeCycleStatus.class);
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

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
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

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    @Reference
    public void setLicenseService(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    @Reference
    public void setMeterActivationValidatorsWhiteboard(MeterActivationValidatorsWhiteboard meterActivationValidatorsWhiteboard) {
        this.meterActivationValidatorsWhiteboard = meterActivationValidatorsWhiteboard;
    }

    @Activate
    public final void activate(BundleContext bundleContext) {
        if (bundleContext != null) {
            supportedApplicationsUrls.put(KnownAmrSystem.MDC, bundleContext.getProperty(MDC_URL));
            supportedApplicationsUrls.put(KnownAmrSystem.ENERGY_AXIS, bundleContext.getProperty(ENERGY_AXIS_URL));
        }
        if (dataModel != null && bundleContext != null) {
            createNewTemplate(bundleContext);
        } else if (bundleContext == null && locationTemplate == null) {
            createDefaultLocationTemplate();
            createLocationTemplateDefaultData();
        }
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }

        this.metrologyConfigurationService = new MetrologyConfigurationServiceImpl(this, this.userService, this.meterActivationValidatorsWhiteboard);
        this.usagePointRequirementsSearchDomain = new UsagePointRequirementsSearchDomain(this.propertySpecService, this, this.metrologyConfigurationService, this.clock, this.licenseService);
        this.searchService.register(this.usagePointRequirementsSearchDomain);
        registerMetrologyConfigurationService(bundleContext);

        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(ChannelBuilder.class).to(ChannelBuilderImpl.class);
                bind(MeteringServiceImpl.class).toInstance(MeteringServiceImpl.this);
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
                bind(MetrologyConfigurationService.class).toInstance(metrologyConfigurationService);
                bind(ServerMetrologyConfigurationService.class).toInstance(metrologyConfigurationService);
                bind(CalculatedReadingRecordFactory.class).to(CalculatedReadingRecordFactoryImpl.class);
                bind(UsagePointRequirementsSearchDomain.class).toInstance(usagePointRequirementsSearchDomain);
                bind(SearchService.class).toInstance(searchService);
                bind(PropertySpecService.class).toInstance(propertySpecService);
                bind(LicenseService.class).toInstance(licenseService);
                bind(MessageService.class).toInstance(messageService);
                bind(MetrologyConfigurationServiceImpl.class).toInstance(metrologyConfigurationService);
                bind(IMeterActivation.class).to(MeterActivationImpl.class);
            }
        });

        if (upgradeService.isInstalled(identifier("Pulse", COMPONENTNAME), version(10, 2))) {
            getLocationTemplateFromDB().ifPresent(template -> {
                locationTemplate = template;
                locationTemplateMembers = ImmutableList.copyOf((template.getTemplateMembers()));
            });
        }
        upgradeService.register(identifier("Pulse", COMPONENTNAME), dataModel, InstallerImpl.class, ImmutableMap.of(
                version(10, 2), UpgraderV10_2.class
        ));
    }

    private void registerMetrologyConfigurationService(BundleContext bundleContext) {
        if (bundleContext != null) {
            Dictionary<String, Object> properties = new Hashtable<>(1);
            properties.put("name", MetrologyConfigurationService.COMPONENT_NAME);
            this.serviceRegistrations.add(bundleContext.registerService(
                    new String[]{
                            MetrologyConfigurationService.class.getName(),
                            ServerMetrologyConfigurationService.class.getName(),
                            TranslationKeyProvider.class.getName()},
                    this.metrologyConfigurationService,
                    properties));
        }

    }

    @Deactivate
    public final void deactivate() {
        if (!this.serviceRegistrations.isEmpty()) {
            this.serviceRegistrations.forEach(ServiceRegistration::unregister);
        }
        if (this.usagePointRequirementsSearchDomain != null) {
            this.searchService.unregister(this.usagePointRequirementsSearchDomain);
        }
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
        this.nlsService = nlsService;
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
        return dataModel.stream(ReadingType.class)
                .filter(where(ReadingTypeImpl.Fields.equidistant.name()).isEqualTo(true))
                .collect(Collectors.toList());
    }

    @Override
    public List<ReadingType> getAvailableNonEquidistantReadingTypes() {
        return dataModel.stream(ReadingType.class)
                .filter(where(ReadingTypeImpl.Fields.equidistant.name()).isEqualTo(false))
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
        List<String> availableReadingTypeCodes =
                availableReadingTypes.parallelStream()
                        .map(ReadingType::getMRID)
                        .collect(Collectors.toList());
        List<Pair<String, String>> filteredReadingTypes =
                readingTypes.parallelStream()
                        .filter(readingTypePair -> !availableReadingTypeCodes.contains(readingTypePair.getFirst()))
                        .collect(Collectors.toList());

        DecoratedStream.decorate(filteredReadingTypes.stream())
                .map(filteredReadingType -> dataModel.getInstance(ReadingTypeImpl.class)
                        .init(filteredReadingType.getFirst(), filteredReadingType.getLast()))
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
        Arrays.stream(BypassStatus.values()).forEach(translationKeys::add);
        Arrays.stream(YesNoAnswer.values()).map(answer -> new TranslationKey() {
            @Override
            public String getKey() {
                return answer.toString();
            }

            @Override
            public String getDefaultFormat() {
                return answer.toString();
            }
        }).forEach(translationKeys::add);
        translationKeys.addAll(ReadingTypeTranslationKeys.allKeys());
        return translationKeys;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Override
    public MultiplierType createMultiplierType(String name) {
        MultiplierTypeImpl multiplierType = dataModel.getInstance(MultiplierTypeImpl.class).initWithCustomName(name);
        multiplierType.save();
        return multiplierType;
    }

    @Override
    public MultiplierType createMultiplierType(NlsKey name) {
        String localKey = "MultiplierType.custom." + name.getKey();
        this.copyKeyIfMissing(name, localKey);
        MultiplierTypeImpl multiplierType = this.dataModel.getInstance(MultiplierTypeImpl.class)
                .initWithNlsNameKey(localKey);
        multiplierType.save();
        return multiplierType;
    }

    @Override
    public void copyKeyIfMissing(NlsKey name, String localKey) {
        if (this.thesaurus.getTranslations().get(localKey) == null) {
            this.nlsService.copy(name, COMPONENTNAME, Layer.DOMAIN, key -> localKey);
        }
    }

    MultiplierType createMultiplierType(MultiplierType.StandardType standardType) {
        MultiplierTypeImpl multiplierType = this.dataModel.getInstance(MultiplierTypeImpl.class)
                .initWithNlsNameKey(standardType.translationKey());
        multiplierType.save();
        return multiplierType;
    }

    @Override
    public MultiplierType getMultiplierType(MultiplierType.StandardType standardType) {
        return this.dataModel.mapper(MultiplierType.class)
                .getUnique("name", standardType.translationKey(), "nameIsKey", true)
                .get();
    }

    @Override
    public Optional<MultiplierType> getMultiplierType(String name) {
        return this.dataModel.mapper(MultiplierType.class).getUnique("name", name, "nameIsKey", false);
    }

    @Override
    public List<MultiplierType> getMultiplierTypes() {
        return dataModel.mapper(MultiplierType.class).find();
    }

    boolean isCreateAllReadingTypes() {
        return createAllReadingTypes;
    }

    String[] getRequiredReadingTypes() {
        return requiredReadingTypes;
    }


    @Override
    public void createLocationTemplate() {
        LocationTemplateImpl.from(dataModel, locationTemplate.getTemplateFields(), locationTemplate.getMandatoryFields())
                .doSave();
    }

    @Override
    public LocationTemplate getLocationTemplate() {
        return locationTemplate;
    }

    public Optional<LocationTemplate> getLocationTemplateFromDB() {
        List<LocationTemplate> template = new ArrayList<>(dataModel.mapper(LocationTemplate.class).find());
        if (!template.isEmpty()) {
            LocationTemplateImpl dbTemplate = LocationTemplateImpl
                    .from(dataModel, template.get(0).getTemplateFields(), template.get(0).getMandatoryFields());
            dbTemplate
                    .parseTemplate(dbTemplate.getTemplateFields(), dbTemplate.getMandatoryFields());
            return Optional.of(dbTemplate);
        }
        return Optional.empty();
    }


    public static List<TemplateField> getLocationTemplateMembers() {
        return locationTemplateMembers;
    }

    private void createLocationTemplateDefaultData() {
        List<TemplateField> templateElements = new ArrayList<>();
        AtomicInteger index = new AtomicInteger(-1);
        Stream.of(LocationTemplateElements.values()).forEach(t ->
                templateElements.add(
                        new TemplateFieldImpl(
                                t.getElementAbbreviation(),
                                t.toString(),
                                index.incrementAndGet(),
                                index.intValue() % 2 == 0)));
        locationTemplateMembers = ImmutableList.copyOf(templateElements);
    }

    private void createDefaultLocationTemplate() {
        String locationElements = LocationTemplateImpl.ALLOWED_LOCATION_TEMPLATE_ELEMENTS.stream()
                .collect(Collectors.joining(","));
        locationTemplate = new LocationTemplateImpl(dataModel).init(locationElements, locationElements);
        locationTemplate
                .parseTemplate(locationElements, locationElements);
    }

    @Override
    public Optional<EndDeviceControlType> getEndDeviceControlType(String mRID) {
        return dataModel.mapper(EndDeviceControlType.class).getOptional(mRID);
    }

    @Override
    public EndDeviceControlTypeImpl createEndDeviceControlType(String mRID) {
        EndDeviceControlTypeImpl endDeviceControlType = dataModel.getInstance(EndDeviceControlTypeImpl.class).init(mRID);
        dataModel.persist(endDeviceControlType);
        return endDeviceControlType;
    }

    @Override
    public Map<KnownAmrSystem, String> getSupportedApplicationsUrls() {
        return Collections.unmodifiableMap(supportedApplicationsUrls);
    }

    private void createNewTemplate(BundleContext context) {
        String locationTemplateFields = context.getProperty(LOCATION_TEMPLATE);
        String locationTemplateMandatoryFields = context.getProperty(LOCATION_TEMPLATE_MANDATORY_FIELDS);
        locationTemplate = new LocationTemplateImpl(dataModel).init(locationTemplateFields, locationTemplateMandatoryFields);
        locationTemplate
                .parseTemplate(locationTemplateFields, locationTemplateMandatoryFields);
        locationTemplateMembers = ImmutableList.copyOf(locationTemplate.getTemplateMembers());
    }
}