package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.MarketRoleKind;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.ConnectionState;
import com.elster.jupiter.metering.ElectricityDetailBuilder;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.GasDetailBuilder;
import com.elster.jupiter.metering.GeoCoordinates;
import com.elster.jupiter.metering.HeatDetailBuilder;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceLocation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointAccountability;
import com.elster.jupiter.metering.UsagePointConfiguration;
import com.elster.jupiter.metering.UsagePointConnectionState;
import com.elster.jupiter.metering.UsagePointCustomPropertySetExtension;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.metering.UsagePointDetailBuilder;
import com.elster.jupiter.metering.UsagePointMeterActivator;
import com.elster.jupiter.metering.WaterDetailBuilder;
import com.elster.jupiter.metering.ami.CompletionOptions;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.impl.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.impl.config.EffectiveMetrologyConfigurationOnUsagePointImpl;
import com.elster.jupiter.metering.impl.config.ServerMetrologyConfigurationService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.TemporalReference;
import com.elster.jupiter.orm.associations.Temporals;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRepresentation;
import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Quantity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;

@UniqueMRID(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.DUPLICATE_USAGEPOINT + "}")
public class UsagePointImpl implements UsagePoint {
    // persistent fields
    @SuppressWarnings("unused")
    private long id;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.FIELD_TOO_LONG + "}")
    private String aliasName;
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.FIELD_TOO_LONG + "}")
    private String description;
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.FIELD_TOO_LONG + "}")
    private String serviceLocationString;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.FIELD_TOO_LONG + "}")
    private String mRID;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.FIELD_TOO_LONG + "}")
    private String name;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private boolean isSdp;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private boolean isVirtual;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.FIELD_TOO_LONG + "}")
    private String outageRegion;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.FIELD_TOO_LONG + "}")
    private String readRoute;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.FIELD_TOO_LONG + "}")
    private String servicePriority;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Instant installationTime;
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.FIELD_TOO_LONG + "}")
    private String serviceDeliveryRemark;
    private TemporalReference<UsagePointConnectionState> connectionState = Temporals.absent();
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;
    private long location;

    private TemporalReference<UsagePointDetailImpl> detail = Temporals.absent();
    private TemporalReference<EffectiveMetrologyConfigurationOnUsagePoint> metrologyConfiguration = Temporals.absent();

    // associations
    private final Reference<ServiceCategory> serviceCategory = ValueReference.absent();
    private final Reference<ServiceLocation> serviceLocation = ValueReference.absent();
    private final List<IMeterActivation> meterActivations = new ArrayList<>();
    private final List<UsagePointAccountability> accountabilities = new ArrayList<>();
    private List<UsagePointConfigurationImpl> usagePointConfigurations = new ArrayList<>();
    private final Reference<Location> upLocation = ValueReference.absent();
    private final Reference<GeoCoordinates> geoCoordinates = ValueReference.absent();

    private final Clock clock;
    private final DataModel dataModel;
    private final EventService eventService;
    private final MeteringService meteringService;
    private final Thesaurus thesaurus;
    private final Provider<IMeterActivation> meterActivationFactory;
    private final Provider<UsagePointAccountabilityImpl> accountabilityFactory;
    private final CustomPropertySetService customPropertySetService;
    private final ServerMetrologyConfigurationService metrologyConfigurationService;
    private transient UsagePointCustomPropertySetExtensionImpl customPropertySetExtension;

    @Inject
    UsagePointImpl(
            Clock clock, DataModel dataModel, EventService eventService,
            Thesaurus thesaurus, Provider<IMeterActivation> meterActivationFactory,
            Provider<UsagePointAccountabilityImpl> accountabilityFactory,
            CustomPropertySetService customPropertySetService, MeteringService meteringService, ServerMetrologyConfigurationService metrologyConfigurationService) {
        this.clock = clock;
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.thesaurus = thesaurus;
        this.meterActivationFactory = meterActivationFactory;
        this.accountabilityFactory = accountabilityFactory;
        this.customPropertySetService = customPropertySetService;
        this.meteringService = meteringService;
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    UsagePointImpl init(String mRID, ServiceCategory serviceCategory) {
        this.mRID = mRID;
        this.serviceCategory.set(serviceCategory);
        this.isSdp = true;
        return this;
    }

    @Override
    public UsagePointDetailBuilder newDefaultDetailBuilder(Instant start) {
        Interval interval = Interval.of(Range.atLeast(start));
        return new DefaultUsagePointDetailBuilderImpl(dataModel, this, interval);
    }

    @Override
    public ElectricityDetailBuilder newElectricityDetailBuilder(Instant start) {
        Interval interval = Interval.of(Range.atLeast(start));
        return new ElectricityDetailBuilderImpl(dataModel, this, interval);
    }

    @Override
    public GasDetailBuilder newGasDetailBuilder(Instant start) {
        Interval interval = Interval.of(Range.atLeast(start));
        return new GasDetailBuilderImpl(dataModel, this, interval);
    }

    @Override
    public WaterDetailBuilder newWaterDetailBuilder(Instant start) {
        Interval interval = Interval.of(Range.atLeast(start));
        return new WaterDetailBuilderImpl(dataModel, this, interval);
    }

    @Override
    public HeatDetailBuilder newHeatDetailBuilder(Instant start) {
        Interval interval = Interval.of(Range.atLeast(start));
        return new HeatDetailBuilderImpl(dataModel, this, interval);
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getAliasName() {
        return aliasName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getServiceLocationString() {
        return serviceLocationString;
    }

    @Override
    public String getMRID() {
        return mRID;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isSdp() {
        return isSdp;
    }

    @Override
    public boolean isVirtual() {
        return isVirtual;
    }

    @Override
    public String getOutageRegion() {
        return outageRegion;
    }

    @Override
    public String getReadRoute() {
        return readRoute;
    }

    @Override
    public String getServicePriority() {
        return servicePriority;
    }

    @Override
    public ServiceCategory getServiceCategory() {
        return serviceCategory.get();
    }

    @Override
    public Optional<ServiceLocation> getServiceLocation() {
        return serviceLocation.getOptional();
    }

    @Override
    public String getServiceDeliveryRemark() {
        return serviceDeliveryRemark;
    }

    @Override
    public Instant getInstallationTime() {
        return installationTime;
    }

    @Override
    public void setInstallationTime(Instant installationTime) {
        this.installationTime = installationTime;
    }

    @Override
    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void setMRID(String mRID) {
        this.mRID = mRID;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setSdp(boolean isSdp) {
        this.isSdp = isSdp;
    }

    @Override
    public void setVirtual(boolean isVirtual) {
        this.isVirtual = isVirtual;
    }

    @Override
    public void setOutageRegion(String outageRegion) {
        this.outageRegion = outageRegion;
    }

    @Override
    public void setReadRoute(String readRoute) {
        this.readRoute = readRoute;
    }

    @Override
    public void setServicePriority(String servicePriority) {
        this.servicePriority = servicePriority;
    }

    @Override
    public void setServiceLocation(ServiceLocation serviceLocation) {
        this.serviceLocation.set(serviceLocation);
    }

    @Override
    public void setServiceLocationString(String serviceLocationString) {
        this.serviceLocationString = serviceLocationString;
    }

    @Override
    public void setServiceDeliveryRemark(String serviceDeliveryRemark) {
        this.serviceDeliveryRemark = serviceDeliveryRemark;
    }

    @Override
    public void update() {
        doSave();
    }

    void doSave() {
        if (id == 0) {
            if(installationTime==null) {
                throw new IllegalStateException();
            }
            this.setConnectionState(ConnectionState.UNDER_CONSTRUCTION, installationTime);
            Save.CREATE.save(dataModel, this);
            eventService.postEvent(EventType.USAGEPOINT_CREATED.topic(), this);
        } else {
            updateDeviceDefaultLocation();
            Save.UPDATE.save(dataModel, this);
            eventService.postEvent(EventType.USAGEPOINT_UPDATED.topic(), this);
        }
    }

    @Override
    public void delete() {
        this.removeMetrologyConfigurationCustomPropertySetValues();
        this.removeServiceCategoryCustomPropertySetValues();
        this.removeDetail();
        dataModel.remove(this);
        eventService.postEvent(EventType.USAGEPOINT_DELETED.topic(), this);
    }

    private void removeDetail() {
        this.getDetail(Range.all()).forEach(detail::remove);
    }


    private void removeMetrologyConfigurationCustomPropertySetValues() {
        this.removeCustomPropertySetValues(
                getMetrologyConfiguration()
                        .map(MetrologyConfiguration::getCustomPropertySets)
                        .orElse(Collections.emptyList()));
    }

    private void removeServiceCategoryCustomPropertySetValues() {
        this.removeCustomPropertySetValues(getServiceCategory().getCustomPropertySets());
    }

    private void removeCustomPropertySetValues(List<RegisteredCustomPropertySet> registeredCustomPropertySets) {
        registeredCustomPropertySets
                .stream()
                .map(RegisteredCustomPropertySet::getCustomPropertySet)
                .forEach(cps -> customPropertySetService.removeValuesFor(cps, this));
    }

    @Override
    public Instant getCreateDate() {
        return createTime;
    }

    @Override
    public Instant getModificationDate() {
        return modTime;
    }

    public long getVersion() {
        return version;
    }

    @Override
    public List<UsagePointAccountability> getAccountabilities() {
        return ImmutableList.copyOf(accountabilities);
    }

    @Override
    public UsagePointAccountability addAccountability(PartyRole role, Party party, Instant start) {
        UsagePointAccountability accountability = accountabilityFactory.get().init(this, party, role, start);
        accountabilities.add(accountability);
        return accountability;
    }

    @Override
    public boolean hasAccountability(User user) {
        for (UsagePointAccountability accountability : getAccountabilities()) {
            for (PartyRepresentation representation : accountability.getParty().getCurrentDelegates()) {
                if (representation.getDelegate().equals(user)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Optional<MetrologyConfiguration> getMetrologyConfiguration() {
        return this.getMetrologyConfiguration(this.clock.instant());
    }

    @Override
    public Optional<MetrologyConfiguration> getMetrologyConfiguration(Instant when) {
        return this.metrologyConfiguration.effective(when)
                .map(EffectiveMetrologyConfigurationOnUsagePoint::getMetrologyConfiguration);
    }

    Optional<EffectiveMetrologyConfigurationOnUsagePoint> getEffectiveMetrologyConfiguration(Instant when) {
        return this.metrologyConfiguration.effective(when);
    }

    @Override
    public List<MetrologyConfiguration> getMetrologyConfigurations(Range<Instant> period) {
        return this.metrologyConfiguration
                .effective(period)
                .stream()
                .map(EffectiveMetrologyConfigurationOnUsagePoint::getMetrologyConfiguration)
                .collect(Collectors.toList());
    }

    @Override
    public void apply(MetrologyConfiguration metrologyConfiguration) {
        this.apply(metrologyConfiguration, this.clock.instant());
    }

    @Override
    public void apply(MetrologyConfiguration metrologyConfiguration, Instant when) {
        this.removeMetrologyConfiguration(when);
        this.metrologyConfiguration.add(
                this.dataModel
                        .getInstance(EffectiveMetrologyConfigurationOnUsagePointImpl.class)
                        .initAndSave(this, metrologyConfiguration, when));
    }

    @Override
    public void removeMetrologyConfiguration(Instant when) {
        Optional<EffectiveMetrologyConfigurationOnUsagePoint> current = this.metrologyConfiguration.effective(this.clock.instant());
        if (current.isPresent()) {
            if (!current.get().getRange().contains(when)) {
                throw new IllegalArgumentException("Time of metrology configuration removal is before it was actually applied");
            }
            current.get().close(when);
        }
    }

    @Override
    public UsagePointCustomPropertySetExtension forCustomProperties() {
        if (this.customPropertySetExtension == null) {
            this.customPropertySetExtension = new UsagePointCustomPropertySetExtensionImpl(this.clock, this.customPropertySetService, this.thesaurus, this);
        }
        return this.customPropertySetExtension;
    }

    @Override
    public ConnectionState getConnectionState() {
        return this.connectionState.effective(this.clock.instant()).map(UsagePointConnectionState::getConnectionState).orElseThrow(IllegalStateException::new);
    }

    @Override
    public void setConnectionState(ConnectionState connectionState) {
        Instant effective = this.clock.instant();
        this.setConnectionState(connectionState, effective);
    }

    @Override
    public void setConnectionState(ConnectionState connectionState, Instant effective) {
        if(!this.connectionState.effective(Range.all()).isEmpty()) {
            this.closeCurrentConnectionState(effective);
        }
        this.createNewState(effective,connectionState);
        this.touch();
    }

    private void closeCurrentConnectionState(Instant now){
        UsagePointConnectionState currentState = this.connectionState.effective(now).get();
        currentState.close(now);
        this.dataModel.update(currentState);
    }

    private void createNewState(Instant effective, ConnectionState connectionState) {
        Interval stateEffectivityInterval = Interval.of(Range.atLeast(effective));
        UsagePointConnectionState usagePointConnectionState = this.dataModel
                .getInstance(UsagePointConnectionStateImpl.class)
                .initialize(stateEffectivityInterval, this, connectionState);
        this.connectionState.add(usagePointConnectionState);
    }

    @Override
    public List<CompletionOptions> connect(Instant when, ServiceCall serviceCall) {
        return this.getMeterActivations()
                .stream()
                .flatMap(meterActivation -> meterActivation.getMeter()
                        .isPresent() ? Stream.of(meterActivation.getMeter().get()) : Stream.empty())
                .map(meter -> meter.getHeadEndInterface()
                        .map(headEndInterface -> headEndInterface.sendCommand(headEndInterface.getCommandFactory()
                                .createConnectCommand(meter, when), when, serviceCall)))
                .flatMap(completionOptions -> completionOptions.isPresent() ? Stream.of(completionOptions.get()) : Stream
                        .empty())
                .collect(Collectors.toList());
    }

    @Override
    public List<CompletionOptions> disconnect(Instant when, ServiceCall serviceCall) {
        return this.getMeterActivations()
                .stream()
                .flatMap(meterActivation -> meterActivation.getMeter()
                        .isPresent() ? Stream.of(meterActivation.getMeter().get()) : Stream.empty())
                .map(meter -> meter.getHeadEndInterface()
                        .map(headEndInterface -> headEndInterface.sendCommand(headEndInterface.getCommandFactory()
                                .createDisconnectCommand(meter, when), when, serviceCall)))
                .flatMap(completionOptions -> completionOptions.isPresent() ? Stream.of(completionOptions.get()) : Stream
                        .empty())
                .collect(Collectors.toList());
    }

    @Override
    public List<CompletionOptions> enableLoadLimit(Instant when, Quantity loadLimit, ServiceCall serviceCall) {
        //not implemented yet
        return null;
    }

    @Override
    public List<CompletionOptions> disableLoadLimit(Instant when, ServiceCall serviceCall) {
        //not implemented yet
        return null;
    }

    @Override
    public List<CompletionOptions> readData(Instant when, List<ReadingType> readingTypes, ServiceCall serviceCall) {
        //not implemented yet
        return null;
    }

    @Override
    public Optional<UsagePointDetailImpl> getDetail(Instant instant) {
        return detail.effective(instant);
    }

    @Override
    public List<UsagePointDetailImpl> getDetail(Range<Instant> range) {
        return detail.effective(range);
    }

    @Override
    public List<UsagePointDetailImpl> getDetails() {
        return detail.all();
    }

    @Override
    public void addDetail(UsagePointDetail newDetail) {
        Optional<UsagePointDetailImpl> optional = this.getDetail(newDetail.getRange().lowerEndpoint());
        if (optional.isPresent()) {
            UsagePointDetailImpl previousDetail = optional.get();
            this.terminateDetail(previousDetail, newDetail.getRange().lowerEndpoint());
        }
        validateAddingDetail(newDetail);
        detail.add((UsagePointDetailImpl) newDetail);
        touch();
    }

    void touch() {
        if (id != 0) {
            dataModel.touch(this);
        }
    }

    @Override
    public UsagePointDetail terminateDetail(UsagePointDetail detail, Instant date) {
        UsagePointDetailImpl toUpdate = null;
        if (detail.getUsagePoint() == this) {
            toUpdate = (UsagePointDetailImpl) detail;
        }
        if (toUpdate == null || !detail.isEffectiveAt(date)) {
            throw new IllegalArgumentException();
        }
        toUpdate.terminate(date);
        dataModel.update(toUpdate);
        touch();
        return toUpdate;
    }

    private void validateAddingDetail(UsagePointDetail candidate) {
        for (UsagePointDetail usagePointDetail : detail.effective(candidate.getRange())) {
            if (candidate.conflictsWith(usagePointDetail)) {
                throw new IllegalArgumentException("Conflicts with existing usage point characteristics : " + candidate);
            }
        }
        Save.CREATE.validate(dataModel, candidate);
    }

    @Override
    public List<? extends BaseReadingRecord> getReadingsWithFill(Range<Instant> range, ReadingType readingType) {
        List<? extends BaseReadingRecord> notFilled = MeterActivationsImpl.from(meterActivations, range)
                .getReadings(range, readingType);
        List<IntervalReadingRecord> filled = new ArrayList<>();

        if (!readingType.isRegular()) {
            return notFilled;
        }
        IntervalReadingRecord previous = null;
        for (BaseReadingRecord brr : notFilled) {
            IntervalReadingRecord irr = (IntervalReadingRecord) brr;
            if (previous == null) {
                filled.add(irr);
                previous = irr;
                continue;
            }
            Instant previousTime = previous.getTimeStamp()
                    .plus(Duration.ofMinutes(readingType.getMeasuringPeriod().getMinutes()));
            while (previousTime.compareTo(brr.getTimeStamp()) != 0) {
                IntervalReadingRecord irri = new EmptyIntervalReadingRecordImpl(readingType, previousTime);
                previousTime = previousTime.plus(Duration.ofMinutes(readingType.getMeasuringPeriod().getMinutes()));
                filled.add(irri);
            }
            filled.add(irr);
            previous = irr;
        }
        return filled;
    }

    @Override
    public Optional<Party> getCustomer(Instant when) {
        return getResponsibleParty(when, MarketRoleKind.ENERGYSERVICECONSUMER);
    }

    @Override
    public Optional<Party> getResponsibleParty(Instant when, MarketRoleKind marketRole) {
        for (UsagePointAccountability each : getAccountabilities()) {
            if (each.isEffectiveAt(when) && each.getRole().getMRID().equals(marketRole.name())) {
                return Optional.of(each.getParty());
            }
        }
        return Optional.empty();
    }

    @Override
    public ZoneId getZoneId() {
        return ZoneId.systemDefault();
    }

    void addConfiguration(UsagePointConfigurationImpl usagePointConfiguration) {
        usagePointConfigurations.add(usagePointConfiguration);
    }

    @Override
    public UsagePointConfigurationBuilder startingConfigurationOn(Instant startTime) {
        return new UsagePointConfigurationBuilderImpl(dataModel, this, startTime);
    }

    @Override
    public Optional<UsagePointConfiguration> getConfiguration(Instant time) {
        return usagePointConfigurations.stream()
                .filter(usagePointConfiguration -> usagePointConfiguration.isEffectiveAt(time))
                .map(UsagePointConfiguration.class::cast)
                .findAny();
    }

    @Override
    public long getLocationId() {
        return location;
    }

    @Override
    public Optional<Location> getLocation() {
        return upLocation.getOptional();
    }

    @Override
    public void setLocation(long locationId) {
        this.location = locationId;
    }

    @Override
    public long getGeoCoordinatesId() {
        Optional<GeoCoordinates> coordinates = getGeoCoordinates();
        return coordinates.isPresent() ? coordinates.get().getId() : 0L;
    }

    @Override
    public Optional<GeoCoordinates> getGeoCoordinates() {
        return geoCoordinates.getOptional();
    }

    @Override
    public void setGeoCoordinates(GeoCoordinates geoCoordinates) {
        this.geoCoordinates.set(geoCoordinates);
    }

    @Override
    public Optional<MeterActivation> getMeterActivation(Instant when) {
        return getMeterActivations(when).stream()
                .filter(ma -> ma.getMeterRole().isPresent() && ma.getMeterRole()
                        .get()
                        .getKey()
                        .equals(DefaultMeterRole.DEFAULT.getKey()))
                .findFirst();
    }

    @Override
    public List<MeterActivation> getMeterActivations(Instant when) {
        return this.meterActivations.stream()
                .filter(meterActivation -> meterActivation.isEffectiveAt(when))
                .collect(Collectors.toList());
    }

    @Override
    public List<MeterActivation> getMeterActivations() {
        return meterActivations.stream()
                .filter(ma -> !ma.getStart().equals(ma.getEnd()))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<MeterActivation> getCurrentMeterActivation() {
        return getCurrentMeterActivations().stream()
                .filter(ma -> ma.getMeterRole().isPresent() && ma.getMeterRole()
                        .get()
                        .getKey()
                        .equals(DefaultMeterRole.DEFAULT.getKey()))
                .findFirst();
    }

    @Override
    public List<MeterActivation> getCurrentMeterActivations() {
        return this.meterActivations.stream()
                .filter(MeterActivation::isCurrent)
                .collect(Collectors.toList());
    }

    @Override
    public List<MeterActivation> getMeterActivations(MeterRole role) {
        return this.meterActivations.stream()
                .filter(ma -> !ma.getStart().equals(ma.getEnd()))
                .filter(ma -> ma.getMeterRole().isPresent() && ma.getMeterRole().get().equals(role))
                .collect(Collectors.toList());
    }

    @Override
    public MeterActivation activate(Meter meter, Instant start) {
        return this.activate(meter, metrologyConfigurationService.findDefaultMeterRole(DefaultMeterRole.DEFAULT), start);
    }

    @Override
    public MeterActivation activate(Meter meter, MeterRole meterRole, Instant from) {
        MeterActivationImpl result = meterActivationFactory.get().init(meter, meterRole, this, from);
        dataModel.persist(result);
        adopt(result);
        return result;
    }

    @Override
    public UsagePointMeterActivator linkMeters() {
        return this.dataModel.getInstance(UsagePointMeterActivatorImpl.class).init(this);
    }

    public void adopt(MeterActivationImpl meterActivation) {
        meterActivations.stream()
                .filter(activation -> activation.getId() != meterActivation.getId())
                .reduce((m1, m2) -> m2)
                .ifPresent(last -> {
                    if (last.getRange().lowerEndpoint().isAfter(meterActivation.getRange().lowerEndpoint())) {
                        throw new IllegalArgumentException("Invalid start date");
                    } else {
                        if (!last.getRange().hasUpperBound() || last.getRange()
                                .upperEndpoint()
                                .isAfter(meterActivation.getRange().lowerEndpoint())) {
                            last.endAt(meterActivation.getRange().lowerEndpoint());
                        }
                    }
                });
        Optional<Meter> meter = meterActivation.getMeter();
        if (meter.isPresent()) {
            // if meter happens to be the same meter of the last meteractivation that we just closed a few lines above,
            // best is to refresh the meter so the updated meteractivations are refetched from db. see COPL-854
            Meter existing = dataModel.mapper(Meter.class).getExisting(meter.get().getId());
            ((MeterImpl) existing).adopt(meterActivation);
        }
        meterActivations.add(meterActivation);
    }

    public void refreshMeterActivations() {
        this.meterActivations.clear();
        this.meterActivations.addAll(this.dataModel.query(MeterActivationImpl.class)
                .select(where("usagePoint").isEqualTo(this)));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UsagePointImpl usagePoint = (UsagePointImpl) o;
        return id == usagePoint.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    private void updateDeviceDefaultLocation() {
        this.getMeterActivations().stream()
                .forEach(meterActivation -> meterActivation.getMeter().ifPresent(meter -> {
                    if (upLocation.isPresent()) {
                        if (location != upLocation.get().getId()) {
                            if (!meter.getLocation().isPresent() || meter.getLocationId() == upLocation.get().getId()) {
                                meteringService.findLocation(location).ifPresent(meter::setLocation);
                            }
                        }
                    } else if (location != 0) {
                        if (!meter.getLocation().isPresent()) {
                            meteringService.findLocation(location).ifPresent(meter::setLocation);
                        }
                    }
                    if (geoCoordinates.getOptional().isPresent()) {
                        GeoCoordinates geo = geoCoordinates.getOptional().get();
                        dataModel.mapper(UsagePoint.class).getOptional(this.getId()).ifPresent(up -> {
                            if (up.getGeoCoordinates().isPresent()) {
                                if (!meter.getGeoCoordinates().isPresent() || meter.getGeoCoordinates()
                                        .get()
                                        .getId() == up.getGeoCoordinates().get().getId()) {
                                    meter.setGeoCoordinates(geo);
                                }
                            } else {
                                if (!meter.getGeoCoordinates().isPresent()) {
                                    meter.setGeoCoordinates(geo);
                                }
                            }
                        });
                    } else {
                        meter.setGeoCoordinates(null);
                    }
                    meter.update();
                }));
    }
}
