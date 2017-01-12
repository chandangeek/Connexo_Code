package com.elster.jupiter.metering.impl;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceControlType;
import com.elster.jupiter.metering.GasDayOptions;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.LocationMember;
import com.elster.jupiter.metering.LocationTemplate;
import com.elster.jupiter.metering.LocationTemplate.TemplateField;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeterFilter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.metering.PurgeConfiguration;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
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
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.metering.UsagePointFilter;
import com.elster.jupiter.metering.ami.HeadEndInterface;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.JournalEntry;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRepresentation;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Membership;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.conditions.Subquery;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.streams.DecoratedStream;
import com.elster.jupiter.util.time.DayMonthTime;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import org.osgi.framework.BundleContext;

import javax.validation.constraints.NotNull;
import java.time.Clock;
import java.time.Instant;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.metering.impl.LocationTemplateImpl.LocationTemplateElements;
import static com.elster.jupiter.metering.impl.LocationTemplateImpl.TemplateFieldImpl;
import static com.elster.jupiter.metering.impl.ReadingTypeImpl.Fields.mRID;
import static com.elster.jupiter.orm.Version.version;
import static com.elster.jupiter.upgrade.InstallIdentifier.identifier;
import static com.elster.jupiter.util.conditions.Where.where;

public class MeteringServiceImpl implements ServerMeteringService {
    private static final String LOCATION_TEMPLATE = "com.elster.jupiter.location.template";
    private static final String LOCATION_TEMPLATE_MANDATORY_FIELDS = "com.elster.jupiter.location.template.mandatoryfields";

    private IdsService idsService;
    private QueryService queryService;
    private Clock clock;
    private EventService eventService;
    private DataModel dataModel;
    private Thesaurus thesaurus;
    private MessageService messageService;
    private JsonService jsonService;
    private UpgradeService upgradeService;
    private MeteringDataModelService meteringDataModelService;

    private volatile LocationTemplate locationTemplate;
    private static ImmutableList<TemplateField> locationTemplateMembers;

    public MeteringServiceImpl(MeteringDataModelService meteringDataModelService, DataModel dataModel, Thesaurus thesaurus, Clock clock, IdsService idsService,
                               EventService eventService, QueryService queryService, MessageService messageService, JsonService jsonService,
                               UpgradeService upgradeService) {
        this.meteringDataModelService = meteringDataModelService;
        this.clock = clock;
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
        this.idsService = idsService;
        this.eventService = eventService;
        this.queryService = queryService;
        this.messageService = messageService;
        this.jsonService = jsonService;
        this.upgradeService = upgradeService;
    }

    @Override
    public void addHeadEndInterface(HeadEndInterface headEndInterface) {
        this.meteringDataModelService.addHeadEndInterface(headEndInterface);
    }

    @Override
    @SuppressWarnings("unused")
    public void removeHeadEndInterface(HeadEndInterface headEndInterface) {
        this.meteringDataModelService.removeHeadEndInterface(headEndInterface);
    }

    @Override
    public List<HeadEndInterface> getHeadEndInterfaces() {
        return this.meteringDataModelService.getHeadEndInterfaces();
    }

    @Override
    public Optional<HeadEndInterface> getHeadEndInterface(String amrSystem) {
        return getHeadEndInterfaces().stream()
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
    public Optional<UsagePoint> findUsagePointById(long id) {
        return dataModel.mapper(UsagePoint.class).getOptional(id);
    }

    @Override
    public Optional<UsagePoint> findAndLockUsagePointByIdAndVersion(long id, long version) {
        return dataModel.mapper(UsagePoint.class).lockObjectIfVersion(version, id);
    }

    @Override
    public Optional<UsagePoint> findAndLockUsagePointByMRIDAndVersion(String mRID, long version) {
        return findUsagePointByMRID(mRID).flatMap(usagePoint ->
                dataModel.mapper(UsagePoint.class).lockObjectIfVersion(version, usagePoint.getId()));
    }

    @Override
    public Optional<UsagePoint> findAndLockUsagePointByNameAndVersion(String name, long version) {
        return findUsagePointByName(name).flatMap(usagePoint ->
                dataModel.mapper(UsagePoint.class).lockObjectIfVersion(version, usagePoint.getId()));
    }

    @Override
    public Optional<UsagePoint> findUsagePointByMRID(String mRID) {
        return dataModel.mapper(UsagePoint.class).getUnique("mRID", mRID);
    }

    @Override
    public Optional<UsagePoint> findUsagePointByName(String name) {
        return dataModel.mapper(UsagePoint.class).getUnique("name", name, "obsoleteTime", null);
    }

    @Override
    public Optional<Meter> findMeterById(long id) {
        return dataModel.mapper(Meter.class).getOptional(id);
    }

    @Override
    public Optional<EndDevice> findEndDeviceById(long id) {
        return dataModel.mapper(EndDevice.class).getOptional(id);
    }

    @Override
    public Optional<Meter> findMeterByMRID(String mRID) {
        return dataModel.mapper(Meter.class).getUnique("mRID", mRID);
    }

    @Override
    public Optional<EndDevice> findEndDeviceByMRID(String mRID) {
        return dataModel.mapper(EndDevice.class).getUnique("mRID", mRID);
    }

    @Override
    public Optional<Meter> findMeterByName(String name) {
        return dataModel.mapper(Meter.class).getUnique("name", name, "obsoleteTime", null);
    }

    @Override
    public Optional<EndDevice> findEndDeviceByName(String name) {
        return dataModel.mapper(EndDevice.class).getUnique("name", name, "obsoleteTime", null);
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
                        PartyRepresentation.class,
                        UsagePointStateTemporalImpl.class));
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
    @SuppressWarnings("unchecked")
    public Query<Meter> getMeterQuery() {
        QueryExecutor<?> executor = dataModel.query(EndDevice.class, Location.class, LocationMember.class, EndDeviceLifeCycleStatus.class);
        executor.setRestriction(Operator.EQUAL.compare("class", Meter.TYPE_IDENTIFIER));
        return queryService.wrap((QueryExecutor<Meter>) executor);
    }

    @Override
    public Query<Meter> getMeterWithReadingQualitiesQuery(Range<Instant> readingQualityTimestamp, ReadingQualityType... readingQualityTypes) {
        QueryExecutor<Meter> meterQuery = dataModel.query(Meter.class, MeterActivation.class, ChannelsContainer.class, Channel.class);

        Condition suspectCondition = where("typeCode").in(Stream.of(readingQualityTypes).map(ReadingQualityType::getCode).collect(Collectors.toList()));
        if (!Range.all().equals(readingQualityTimestamp)) {
            suspectCondition = suspectCondition.and(where("readingTimestamp").in(readingQualityTimestamp));
        }
        Subquery rqrSubQuery = dataModel.query(ReadingQualityRecord.class).asSubquery(suspectCondition, "channelid");
        Membership channelsIn = ListOperator.IN.contains(rqrSubQuery, "meterActivations.channelsContainer.channels.id");
        // we need this subquery condition because oracle cannot handle coordinates in a distinct
        Condition devicesWithSuspectChannels = ListOperator.IN.contains(meterQuery.asSubquery(channelsIn, "id"), "id");
        meterQuery.setRestriction(devicesWithSuspectChannels);
        return queryService.wrap(meterQuery);
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

    public List<JournalEntry<ServiceLocation>> findServiceLocationJournal(long id) {
        return dataModel.mapper(ServiceLocation.class).getJournal(id);
    }

    /**
     * This method has an effect on resulting tableSpec, it must be called before adding TableSpecs.
     *
     * @param bundleContext The BundleContext
     * @param createDefaultLocationTemplate true if the default location template should be created
     */
    final void defineLocationTemplates(BundleContext bundleContext, boolean createDefaultLocationTemplate) {
        if (createDefaultLocationTemplate) {
            if (locationTemplate == null) {
                createDefaultLocationTemplate();
                createLocationTemplateDefaultData();
            }
        } else if (dataModel != null) {
            createNewTemplate(bundleContext);
        }
    }

    /**
     * This method highly depends on dataModel, so call it after database installation
     */
    final void readLocationTemplatesFromDatabase() {
        if (upgradeService.isInstalled(identifier("Pulse", MeteringDataModelService.COMPONENT_NAME), version(10, 2))) {
            getLocationTemplateFromDB().ifPresent(template -> {
                locationTemplate = template;
                locationTemplateMembers = ImmutableList.copyOf((template.getTemplateMembers()));
            });
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


    @Override
    public Optional<MeterActivation> findMeterActivation(long id) {
        return dataModel.mapper(MeterActivation.class).getOptional(id);
    }

    @Override
    public Finder<Meter> findMeters(MeterFilter filter) {
        Condition condition = Condition.TRUE;
        if (!Checks.is(filter.getName()).emptyOrOnlyWhiteSpace()) {
            condition = condition.and(where("name").likeIgnoreCase(filter.getName()));
        }
        if (!filter.getExcludedStates().isEmpty()) {
            condition = condition.and(ListOperator.NOT_IN.contains(DefaultFinder.of(EndDeviceLifeCycleStatus.class,
                    where("state.name").in(filter.getExcludedStates())
                            .and(where("interval").isEffective()), dataModel, State.class).asSubQuery("enddevice"), "id"));
        }
        return DefaultFinder.of(Meter.class, where("obsoleteTime").isNull().and(condition), dataModel).defaultSortColumn("name");
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
        if (!Checks.is(filter.getName()).emptyOrOnlyWhiteSpace()) {
            condition = condition.and(where("name").likeIgnoreCase(filter.getName()));
        }
        if (filter.isAccountabilityOnly()) {
            condition = condition.and(hasAccountability());
        }
        return DefaultFinder.of(UsagePoint.class, where("obsoleteTime").isNull().and(condition), dataModel);
    }

    @Override
    public ReadingTypeFieldsFactory getReadingTypeFieldCodesFactory() {
        return new ReadingTypeLocalizedFieldsFactory(this.meteringDataModelService.getThesaurus());
    }

    @Override
    public Finder<ReadingType> getReadingTypesByMridFilter(@NotNull ReadingTypeMridFilter filter) {
        return DefaultFinder.of(ReadingType.class, filter.getFilterCondition(), dataModel);
    }

    @Override
    public List<ReadingType> getAllReadingTypesWithoutInterval() {
        Condition withoutIntervals = where(mRID.name()).matches("^0\\.\\d+\\.0", "");
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

    @Override
    public AmrSystemImpl createAmrSystem(int id, String name) {
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
    @Override
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

    @Override
    public ServiceCategoryImpl createServiceCategory(ServiceKind serviceKind, boolean active) {
        ServiceCategoryImpl serviceCategory = dataModel.getInstance(ServiceCategoryImpl.class).init(serviceKind);
        serviceCategory.setActive(active);
        dataModel.persist(serviceCategory);
        return serviceCategory;
    }

    @Override
    public void purge(PurgeConfiguration purgeConfiguration) {
        new DataPurger(purgeConfiguration, this, clock).purge();
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

    private List<Vault> registerVaults() {
        return idsService.getVault(MeteringService.COMPONENTNAME, ChannelImpl.IRREGULARVAULTID)
                .map(Arrays::asList)
                .orElse(Collections.emptyList());
    }

    private List<Vault> intervalVaults() {
        return idsService.getVault(MeteringService.COMPONENTNAME, ChannelImpl.INTERVALVAULTID)
                .map(Arrays::asList)
                .orElse(Collections.emptyList());
    }

    private List<Vault> dailyVaults() {
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
    public MultiplierType createMultiplierType(String name) {
        MultiplierTypeImpl multiplierType = dataModel.getInstance(MultiplierTypeImpl.class).initWithCustomName(name);
        multiplierType.save();
        return multiplierType;
    }

    @Override
    public MultiplierType createMultiplierType(NlsKey name) {
        String localKey = "MultiplierType.custom." + name.getKey();
        this.meteringDataModelService.copyKeyIfMissing(name, localKey);
        MultiplierTypeImpl multiplierType = this.dataModel.getInstance(MultiplierTypeImpl.class)
                .initWithNlsNameKey(localKey);
        multiplierType.save();
        return multiplierType;
    }

    @Override
    public MultiplierType createMultiplierType(MultiplierType.StandardType standardType) {
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

    @Override
    public void createLocationTemplate() {
        LocationTemplateImpl.from(dataModel, locationTemplate.getTemplateFields(), locationTemplate.getMandatoryFields())
                .doSave();
    }

    @Override
    public LocationTemplate getLocationTemplate() {
        return locationTemplate;
    }

    private Optional<LocationTemplate> getLocationTemplateFromDB() {
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

    static List<TemplateField> getLocationTemplateMembers() {
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

    private void createNewTemplate(BundleContext context) {
        String locationTemplateFields = context.getProperty(LOCATION_TEMPLATE);
        String locationTemplateMandatoryFields = context.getProperty(LOCATION_TEMPLATE_MANDATORY_FIELDS);
        locationTemplate = new LocationTemplateImpl(dataModel).init(locationTemplateFields, locationTemplateMandatoryFields);
        locationTemplate
                .parseTemplate(locationTemplateFields, locationTemplateMandatoryFields);
        locationTemplateMembers = ImmutableList.copyOf(locationTemplate.getTemplateMembers());
    }

    @Override
    public GasDayOptions createGasDayOptions(DayMonthTime yearStart) {
        return GasDayOptionsImpl.create(this.dataModel, yearStart);
    }

    @Override
    public Optional<GasDayOptions> getGasDayOptions() {
        return this.dataModel.mapper(GasDayOptions.class).getOptional(GasDayOptionsImpl.SINGLETON_ID);
    }

}
