package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.MarketRoleKind;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.ElectricityDetailBuilder;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.GasDetailBuilder;
import com.elster.jupiter.metering.HeatDetailBuilder;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingContainer;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceLocation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointAccountability;
import com.elster.jupiter.metering.UsagePointConfiguration;
import com.elster.jupiter.metering.UsagePointCustomPropertySetExtension;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.metering.UsagePointDetailBuilder;
import com.elster.jupiter.metering.WaterDetailBuilder;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.impl.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.impl.config.UsagePointMetrologyConfigurationImpl;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.TemporalReference;
import com.elster.jupiter.orm.associations.Temporals;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRepresentation;
import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.time.Interval;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.constraints.NotNull;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@UniqueMRID(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.DUPLICATE_USAGEPOINT + "}")
public class UsagePointImpl implements UsagePoint {
    // persistent fields
    private long id;
    private String aliasName;
    private String description;
    @NotNull
    private String mRID;
    private String name;
    @NotNull
    private boolean isSdp;
    @NotNull
    private boolean isVirtual;
    private String outageRegion;
    private String readRoute;
    private String servicePriority;
    @NotNull
    private Instant installationTime;
    private String serviceDeliveryRemark;
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    private TemporalReference<UsagePointDetailImpl> detail = Temporals.absent();
    private TemporalReference<UsagePointMetrologyConfiguration> metrologyConfiguration = Temporals.absent();

    // associations
    private final Reference<ServiceCategory> serviceCategory = ValueReference.absent();
    private final Reference<ServiceLocation> serviceLocation = ValueReference.absent();
    private final List<MeterActivationImpl> meterActivations = new ArrayList<>();
    private final List<UsagePointAccountability> accountabilities = new ArrayList<>();
    private List<UsagePointConfigurationImpl> usagePointConfigurations = new ArrayList<>();

    private final Clock clock;
    private final DataModel dataModel;
    private final EventService eventService;
    private final Thesaurus thesaurus;
    private final Provider<MeterActivationImpl> meterActivationFactory;
    private final Provider<UsagePointAccountabilityImpl> accountabilityFactory;
    private final CustomPropertySetService customPropertySetService;
    private transient UsagePointCustomPropertySetExtensionImpl customPropertySetExtension;

    @Inject
    UsagePointImpl(
            Clock clock, DataModel dataModel, EventService eventService,
            Thesaurus thesaurus, Provider<MeterActivationImpl> meterActivationFactory,
            Provider<UsagePointAccountabilityImpl> accountabilityFactory,
            CustomPropertySetService customPropertySetService) {
        this.clock = clock;
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.thesaurus = thesaurus;
        this.meterActivationFactory = meterActivationFactory;
        this.accountabilityFactory = accountabilityFactory;
        this.customPropertySetService = customPropertySetService;
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
    public long getServiceLocationId() {
        Optional<ServiceLocation> location = getServiceLocation();
        return location.isPresent() ? location.get().getId() : 0L;
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
    public void setServiceDeliveryRemark(String serviceDeliveryRemark) {
        this.serviceDeliveryRemark = serviceDeliveryRemark;
    }

    @Override
    public void update() {
        doSave();
    }

    void doSave() {
        if (id == 0) {
            Save.CREATE.save(dataModel, this);
            eventService.postEvent(EventType.USAGEPOINT_CREATED.topic(), this);
        } else {
            Save.UPDATE.save(dataModel, this);
            eventService.postEvent(EventType.USAGEPOINT_UPDATED.topic(), this);
        }
    }

    @Override
    public void delete() {
        this.removeMetrologyConfigurationCustomPropertySetValues();
        this.removeServiceCategoryCustomPropertySetValues();
        dataModel.remove(this);
        eventService.postEvent(EventType.USAGEPOINT_DELETED.topic(), this);
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
    public List<MeterActivationImpl> getMeterActivations() {
        return ImmutableList.copyOf(meterActivations);
    }

    @Override
    public Optional<MeterActivation> getCurrentMeterActivation() {
        return meterActivations.stream()
                .filter(MeterActivation::isCurrent)
                .map(MeterActivation.class::cast)
                .findAny();
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
    public MeterActivation activate(Instant start) {
        MeterActivationImpl result = meterActivationFactory.get().init(this, start);
        dataModel.persist(result);
        adopt(result);
        return result;
    }

    @Override
    public MeterActivation activate(Meter meter, Instant start) {
        MeterActivationImpl result = meterActivationFactory.get().init(meter, this, start);
        dataModel.persist(result);
        adopt(result);
        return result;
    }

	@Override
	public List<Instant> toList(ReadingType readingType, Range<Instant> exportInterval) {
		return getCurrentMeterActivation()
				.map(meterActivation -> meterActivation.toList(readingType, exportInterval))
				.orElseGet(Collections::emptyList);
	}

	public void adopt(MeterActivationImpl meterActivation) {
        meterActivations.stream()
                .filter(activation -> activation.getId() != meterActivation.getId())
                .reduce((m1, m2) -> m2)
                .ifPresent(last -> {
                    if (last.getRange().lowerEndpoint().isAfter(meterActivation.getRange().lowerEndpoint())) {
                        throw new IllegalArgumentException("Invalid start date");
                    } else {
                        if (!last.getRange().hasUpperBound() || last.getRange().upperEndpoint().isAfter(meterActivation.getRange().lowerEndpoint())) {
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
        return this.metrologyConfiguration.effective(when).map(UsagePointMetrologyConfiguration::getMetrologyConfiguration);
    }

    @Override
    public List<MetrologyConfiguration> getMetrologyConfigurations(Range<Instant> period) {
        return this.metrologyConfiguration
                .effective(period)
                .stream()
                .map(UsagePointMetrologyConfiguration::getMetrologyConfiguration)
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
                .getInstance(UsagePointMetrologyConfigurationImpl.class)
                .initAndSave(this, metrologyConfiguration, when));
    }

    @Override
    public void removeMetrologyConfiguration(Instant when) {
        Optional<UsagePointMetrologyConfiguration> current = this.metrologyConfiguration.effective(this.clock.instant());
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
    public Optional<UsagePointDetailImpl> getDetail(Instant instant) {
        return detail.effective(instant);
    }

    @Override
    public List<UsagePointDetailImpl> getDetail(Range<Instant> range) {
        return detail.effective(range);
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
    }

    @Override
    public List<? extends BaseReadingRecord> getReadings(Range<Instant> range, ReadingType readingType) {
        return MeterActivationsImpl.from(meterActivations, range).getReadings(range, readingType);
    }

    @Override
    public List<? extends BaseReadingRecord> getReadingsWithFill(Range<Instant> range, ReadingType readingType) {
        List<? extends BaseReadingRecord> notFilled = MeterActivationsImpl.from(meterActivations, range).getReadings(range, readingType);
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
            Instant previousTime = previous.getTimeStamp().plus(Duration.ofMinutes(readingType.getMeasuringPeriod().getMinutes()));
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
    public List<? extends BaseReadingRecord> getReadingsUpdatedSince(Range<Instant> range, ReadingType readingType, Instant since) {
        return MeterActivationsImpl.from(meterActivations, range).getReadingsUpdatedSince(range, readingType, since);
    }

    @Override
    public Set<ReadingType> getReadingTypes(Range<Instant> range) {
        return MeterActivationsImpl.from(meterActivations, range).getReadingTypes(range);
    }

    @Override
    public List<? extends BaseReadingRecord> getReadingsBefore(Instant when, ReadingType readingType, int count) {
        return MeterActivationsImpl.from(meterActivations).getReadingsBefore(when, readingType, count);
    }

    @Override
    public List<? extends BaseReadingRecord> getReadingsOnOrBefore(Instant when, ReadingType readingType, int count) {
        return MeterActivationsImpl.from(meterActivations).getReadingsOnOrBefore(when, readingType, count);
    }

    @Override
    public boolean hasData() {
        return MeterActivationsImpl.from(meterActivations).hasData();
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
    public boolean is(ReadingContainer other) {
        return other instanceof UsagePoint && ((UsagePoint) other).getId() == getId();
    }

    @Override
    public Optional<Meter> getMeter(Instant instant) {
        return getMeterActivation(instant).flatMap(MeterActivation::getMeter);
    }

    @Override
    public Optional<MeterActivation> getMeterActivation(Instant when) {
        return meterActivations.stream()
                .filter(meterActivation -> meterActivation.isEffectiveAt(when))
                .map(MeterActivation.class::cast)
                .findFirst();
    }

    @Override
    public Optional<UsagePoint> getUsagePoint(Instant instant) {
        return Optional.of(this);
    }

	@Override
	public ZoneId getZoneId() {
		return getCurrentMeterActivation()
				.map(MeterActivation::getZoneId)
				.orElse(ZoneId.systemDefault());
	}

	@Override
	public List<ReadingQualityRecord> getReadingQualities(ReadingQualityType readingQualityType, ReadingType readingType, Range<Instant> interval) {
		return meterActivations.stream()
				.flatMap(meterActivation -> meterActivation.getReadingQualities(readingQualityType, readingType, interval).stream())
				.collect(Collectors.toList());
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
}
