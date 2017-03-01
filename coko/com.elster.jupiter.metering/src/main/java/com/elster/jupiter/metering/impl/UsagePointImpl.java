/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.MarketRoleKind;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.ConnectionState;
import com.elster.jupiter.metering.ElectricityDetailBuilder;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.GasDetailBuilder;
import com.elster.jupiter.metering.HeatDetailBuilder;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.LocationBuilder;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
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
import com.elster.jupiter.metering.UsagePointManagementException;
import com.elster.jupiter.metering.UsagePointMeterActivator;
import com.elster.jupiter.metering.WaterDetailBuilder;
import com.elster.jupiter.metering.ami.CompletionOptions;
import com.elster.jupiter.metering.ami.HeadEndInterface;
import com.elster.jupiter.metering.ami.UnsupportedCommandException;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.OverlapsOnMetrologyConfigurationVersionEnd;
import com.elster.jupiter.metering.config.OverlapsOnMetrologyConfigurationVersionStart;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeRequirementsCollector;
import com.elster.jupiter.metering.config.UnsatisfiedMerologyConfigurationEndDateInThePast;
import com.elster.jupiter.metering.config.UnsatisfiedMerologyConfigurationStartDateRelativelyLatestEnd;
import com.elster.jupiter.metering.config.UnsatisfiedMerologyConfigurationStartDateRelativelyLatestStart;
import com.elster.jupiter.metering.config.UnsatisfiedMetrologyConfigurationEndDate;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.impl.aggregation.MeterActivationSet;
import com.elster.jupiter.metering.impl.aggregation.ServerDataAggregationService;
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
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointStage;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointState;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.geo.SpatialCoordinates;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.time.RangeInstantBuilder;
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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;
import static com.elster.jupiter.util.streams.Currying.test;

@UniqueMRID(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.DUPLICATE_USAGE_POINT_MRID + "}")
@UniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.DUPLICATE_USAGE_POINT_NAME + "}")
@AllRequiredCustomPropertySetsHaveValues(groups = {Save.Update.class})
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
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.FIELD_TOO_LONG + "}")
    private String mRID;
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.REQUIRED + "}")
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
    private TemporalReference<UsagePointConnectionStateImpl> connectionState = Temporals.absent();
    private TemporalReference<UsagePointStateTemporalImpl> state = Temporals.absent();

    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;
    private Instant obsoleteTime;
    private long location;
    private SpatialCoordinates spatialCoordinates;

    private TemporalReference<UsagePointDetailImpl> detail = Temporals.absent();
    private List<EffectiveMetrologyConfigurationOnUsagePoint> metrologyConfigurations = new ArrayList<>();

    // associations
    private final Reference<ServiceCategory> serviceCategory = ValueReference.absent();
    private final Reference<ServiceLocation> serviceLocation = ValueReference.absent();
    private final List<IMeterActivation> meterActivations = new ArrayList<>();
    private final List<UsagePointAccountability> accountabilities = new ArrayList<>();
    private List<UsagePointConfigurationImpl> usagePointConfigurations = new ArrayList<>();
    private final Reference<Location> upLocation = ValueReference.absent();

    private final Clock clock;
    private final DataModel dataModel;
    private final EventService eventService;
    private final Thesaurus thesaurus;
    private final Provider<MeterActivationImpl> meterActivationFactory;
    private final Provider<UsagePointAccountabilityImpl> accountabilityFactory;
    private final CustomPropertySetService customPropertySetService;
    private final ServerMetrologyConfigurationService metrologyConfigurationService;
    private final ServerDataAggregationService dataAggregationService;
    private final UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService;
    private transient UsagePointCustomPropertySetExtensionImpl customPropertySetExtension;

    @Inject
    UsagePointImpl(
            Clock clock, DataModel dataModel, EventService eventService,
            Thesaurus thesaurus, Provider<MeterActivationImpl> meterActivationFactory,
            Provider<UsagePointAccountabilityImpl> accountabilityFactory,
            CustomPropertySetService customPropertySetService,
            ServerMetrologyConfigurationService metrologyConfigurationService,
            ServerDataAggregationService dataAggregationService,
            UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService) {
        this.clock = clock;
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.thesaurus = thesaurus;
        this.meterActivationFactory = meterActivationFactory;
        this.accountabilityFactory = accountabilityFactory;
        this.customPropertySetService = customPropertySetService;
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.dataAggregationService = dataAggregationService;
        this.usagePointLifeCycleConfigurationService = usagePointLifeCycleConfigurationService;
    }

    UsagePointImpl init(String name, ServiceCategory serviceCategory) {
        this.name = name;
        this.mRID = UUID.randomUUID().toString();
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
        this.installationTime = installationTime != null ? installationTime.truncatedTo(ChronoUnit.MINUTES) : null;
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
            Save.CREATE.save(dataModel, this);
            eventService.postEvent(EventType.USAGEPOINT_CREATED.topic(), this);
        } else {
            updateDeviceDefaultLocation();
            Save.UPDATE.save(dataModel, this);
            eventService.postEvent(EventType.USAGEPOINT_UPDATED.topic(), this);
        }
    }

    /**
     * This method will not work if there are meter activations, channel containers and channels linked
     * We keep this method for physical deletion in the future.
     **/
    @Override
    public void delete() {
        this.removeMetrologyConfigurationCustomPropertySetValues();
        this.removeMetrologyConfigurations();
        this.removeServiceCategoryCustomPropertySetValues();
        this.removeDetail();
        dataModel.remove(this);
    }

    private void removeMetrologyConfigurations() {
        Iterator<EffectiveMetrologyConfigurationOnUsagePoint> iterator = this.metrologyConfigurations.iterator();
        while (iterator.hasNext()) {
            EffectiveMetrologyConfigurationOnUsagePointImpl mc = (EffectiveMetrologyConfigurationOnUsagePointImpl) iterator.next();
            mc.prepareDelete();
            iterator.remove();
        }
    }

    private void removeDetail() {
        this.getDetail(Range.all()).forEach(detail::remove);
    }


    private void removeMetrologyConfigurationCustomPropertySetValues() {
        this.removeCustomPropertySetValues(
                getCurrentEffectiveMetrologyConfiguration()
                        .map(EffectiveMetrologyConfigurationOnUsagePoint::getMetrologyConfiguration)
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
    public List<EffectiveMetrologyConfigurationOnUsagePoint> getEffectiveMetrologyConfigurations(Range<Instant> when) {
        return this.metrologyConfigurations
                .stream()
                .filter(notEmpty())
                .filter(emc -> emc.getRange().isConnected(when) && !emc.getRange().intersection(when).isEmpty())
                .sorted(Comparator.comparing(EffectiveMetrologyConfigurationOnUsagePoint::getStart))
                .collect(Collectors.toList());
    }

    private Predicate<EffectiveMetrologyConfigurationOnUsagePoint> notEmpty() {
        return emc -> !emc.getStart().equals(emc.getEnd());
    }

    @Override
    public Optional<EffectiveMetrologyConfigurationOnUsagePoint> getEffectiveMetrologyConfiguration(Instant when) {
        return this.metrologyConfigurations.stream()
                .filter(notEmpty())
                .filter(emc -> emc.getRange().contains(when))
                .map(EffectiveMetrologyConfigurationOnUsagePoint.class::cast)
                .findFirst();
    }

    @Override
    public Optional<EffectiveMetrologyConfigurationOnUsagePoint> getEffectiveMetrologyConfigurationByStart(Instant start) {
        return this.metrologyConfigurations.stream()
                .filter(notEmpty())
                .filter(emc -> emc.getStart().equals(start))
                .map(EffectiveMetrologyConfigurationOnUsagePoint.class::cast)
                .findFirst();
    }

    @Override
    public Optional<EffectiveMetrologyConfigurationOnUsagePoint> getCurrentEffectiveMetrologyConfiguration() {
        return this.metrologyConfigurations.stream()
                .filter(notEmpty())
                .filter(emc -> emc.getRange().contains(clock.instant()))
                .map(EffectiveMetrologyConfigurationOnUsagePoint.class::cast)
                .findFirst();
    }

    @Override
    public List<EffectiveMetrologyConfigurationOnUsagePoint> getEffectiveMetrologyConfigurations() {
        return this.metrologyConfigurations.stream()
                .filter(notEmpty())
                .sorted(Comparator.comparing(EffectiveMetrologyConfigurationOnUsagePoint::getStart))
                .collect(Collectors.toList());
    }


    @Override
    public void apply(UsagePointMetrologyConfiguration metrologyConfiguration) {
        this.apply(metrologyConfiguration, this.clock.instant());
    }

    @Override
    public void apply(UsagePointMetrologyConfiguration metrologyConfiguration, Instant when) {
        this.apply(metrologyConfiguration, when, (Instant) null);
    }

    @Override
    public void apply(UsagePointMetrologyConfiguration metrologyConfiguration, Instant start, Instant end) {
        this.apply(metrologyConfiguration, Collections.emptySet(), start, end);
    }

    @Override
    public void apply(UsagePointMetrologyConfiguration metrologyConfiguration, Instant when, Set<MetrologyContract> optionalContractsToActivate) {
        this.apply(metrologyConfiguration, optionalContractsToActivate, when, null);
    }

    private void apply(UsagePointMetrologyConfiguration metrologyConfiguration, Set<MetrologyContract> optionalContractsToActivate, Instant start, Instant end) {
        UsagePointStage.Key usagePointStage = this.getState(start).getStage().getKey();
        if (usagePointStage != UsagePointStage.Key.PRE_OPERATIONAL) {
            throw UsagePointManagementException.incorrectStage(thesaurus);
        }
        validateEffectiveMetrologyConfigurationInterval(start, end);
        validateAndClosePreviousMetrologyConfigurationIfExists(start);
        Range<Instant> effectiveInterval = end != null ? Range.closedOpen(start, end) : Range.atLeast(start);
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration =
                createEffectiveMetrologyConfigurationWithContracts(metrologyConfiguration, optionalContractsToActivate, effectiveInterval);
        this.metrologyConfigurations.add(effectiveMetrologyConfiguration);
        activateMetersOnMetrologyConfiguration(this.getMeterActivations(start));
    }

    private void activateMetersOnMetrologyConfiguration(List<MeterActivation> meterActivations) {
        UsagePointMeterActivator linker = this.linkMeters().withFormValidation(UsagePointMeterActivator.FormValidation.DEFINE_METROLOGY_CONFIGURATION);

        meterActivations.stream()
                .filter(meterActivation -> meterActivation.getMeterRole().isPresent() && meterActivation.getEnd() == null)
                .forEach(meterActivation -> linker.activate(meterActivation.getMeter().get(), meterActivation.getMeterRole().get()));

        linker.complete();
    }

    private void validateEffectiveMetrologyConfigurationInterval(Instant start, Instant end) {
        if (end != null && (end.isBefore(start) || end.equals(start))) {
            throw new UnsatisfiedMetrologyConfigurationEndDate(thesaurus);
        }
    }

    private void validateAndClosePreviousMetrologyConfigurationIfExists(Instant newStartDate) {
        getEffectiveMetrologyConfigurations().stream()
                .sorted(Comparator.comparing(EffectiveMetrologyConfigurationOnUsagePoint::getStart).reversed())
                .findFirst()
                .ifPresent(effectiveMetrologyConfiguration -> {
                    validateOverlappingWithLatestMetrologyConfiguration(effectiveMetrologyConfiguration, newStartDate);
                    if (effectiveMetrologyConfiguration.getEnd() == null) {
                        effectiveMetrologyConfiguration.close(newStartDate);
                    }
                });
    }

    private void validateOverlappingWithLatestMetrologyConfiguration(EffectiveMetrologyConfigurationOnUsagePoint latestEffectiveMetrologyConfiguration, Instant nextStartDate) {
        Instant latestStartDate = latestEffectiveMetrologyConfiguration.getStart();
        Instant latestEndDate = latestEffectiveMetrologyConfiguration.getEnd();

        if (nextStartDate.isBefore(latestStartDate) || nextStartDate.equals(latestStartDate)) {
            throw new UnsatisfiedMerologyConfigurationStartDateRelativelyLatestStart(thesaurus);
        }
        if (latestEndDate != null && nextStartDate.isBefore(latestEndDate)) {
            throw new UnsatisfiedMerologyConfigurationStartDateRelativelyLatestEnd(thesaurus);
        }
    }

    private EffectiveMetrologyConfigurationOnUsagePointImpl createEffectiveMetrologyConfigurationWithContracts(UsagePointMetrologyConfiguration metrologyConfiguration, Set<MetrologyContract> optionalContractsToActivate, Range<Instant> effectiveInterval) {
        EffectiveMetrologyConfigurationOnUsagePointImpl effectiveMetrologyConfigurationOnUsagePoint = this.dataModel
                .getInstance(EffectiveMetrologyConfigurationOnUsagePointImpl.class)
                .initAndSaveWithInterval(this, metrologyConfiguration, Interval.of(effectiveInterval));
        effectiveMetrologyConfigurationOnUsagePoint.createEffectiveMetrologyContracts(optionalContractsToActivate);
        return effectiveMetrologyConfigurationOnUsagePoint;
    }

    @Override
    public void updateWithInterval(EffectiveMetrologyConfigurationOnUsagePoint metrologyConfigurationVersion, UsagePointMetrologyConfiguration metrologyConfiguration, Instant start, Instant end) {
        Instant startTimeOfCurrent = this.getCurrentEffectiveMetrologyConfiguration()
                .map(EffectiveMetrologyConfigurationOnUsagePoint::getStart)
                .orElse(null);

        if (end != null && metrologyConfigurationVersion.getStart()
                .equals(startTimeOfCurrent) && end.isBefore(this.clock.instant())) {
            throw new UnsatisfiedMerologyConfigurationEndDateInThePast(thesaurus);
        }
        this.getEffectiveMetrologyConfigurations()
                .stream()
                .filter(v -> !v.getStart().equals(metrologyConfigurationVersion.getStart()))
                .forEach(each -> checkOverlapsOfEffectiveMetrologyConfigurations(each, start, end));

        metrologyConfigurationVersion.close(metrologyConfigurationVersion.getStart());
        Long endDate;
        if (end != null) {
            endDate = end.toEpochMilli();
        } else {
            endDate = null;
        }
        basicCheckEffectiveMetrologyConfiguration(metrologyConfiguration, start, end);
        EffectiveMetrologyConfigurationOnUsagePointImpl effectiveMetrologyConfigurationOnUsagePoint = this.dataModel
                .getInstance(EffectiveMetrologyConfigurationOnUsagePointImpl.class)
                .initAndSaveWithInterval(this, metrologyConfiguration, Interval.of(RangeInstantBuilder.closedOpenRange(start.toEpochMilli(), endDate)));
        effectiveMetrologyConfigurationOnUsagePoint.createEffectiveMetrologyContracts();
        this.metrologyConfigurations.add(effectiveMetrologyConfigurationOnUsagePoint);
        this.update();
    }

    private void checkOverlapsOfEffectiveMetrologyConfigurations(EffectiveMetrologyConfigurationOnUsagePoint each, Instant start, Instant end) {
        if (each.isEffectiveAt(start)) {
            if (each.getEnd() != null) {
                throw new OverlapsOnMetrologyConfigurationVersionStart(thesaurus);
            }
        } else if (each.getStart().isAfter(start)) {
            if (end == null) {
                throw new OverlapsOnMetrologyConfigurationVersionEnd(thesaurus);
            } else if (each.isEffectiveAt(end)) {
                throw new OverlapsOnMetrologyConfigurationVersionEnd(thesaurus);
            }
        }
    }

    private void basicCheckEffectiveMetrologyConfiguration(UsagePointMetrologyConfiguration metrologyConfiguration, Instant start, Instant end) {
        if (end != null && (end.isBefore(start) || end.equals(start))) {
            throw new UnsatisfiedMetrologyConfigurationEndDate(thesaurus);
        }
        Range<Instant> range;
        if (end == null) {
            range = Range.atLeast(start);
        } else {
            range = Range.closedOpen(start, end);
        }
        List<MeterActivation> meterActivations = this.getMeterActivations(range);
        List<Pair<MeterRole, Meter>> pairs = new ArrayList<>();
        metrologyConfiguration.getMeterRoles().forEach(meterRole -> meterActivations
                .stream()
                .filter(meterActivation -> meterActivation.getMeterRole().isPresent() && meterActivation.getMeterRole().get().equals(meterRole))
                .findFirst()
                .ifPresent(meterActivation -> pairs.add(Pair.of(meterRole, meterActivation.getMeter().get()))));
        metrologyConfiguration.validateMeterCapabilities(pairs);
    }

    @Override
    public void removeMetrologyConfiguration(Instant when) {
        Optional<EffectiveMetrologyConfigurationOnUsagePoint> config = this.getEffectiveMetrologyConfiguration(when);
        if (config.isPresent()) {
            if (!config.get().getStart().isBefore(clock.instant())) {
                throw new IllegalArgumentException("Time of metrology configuration removal is before it was actually applied");
            }
            this.removeMetrologyConfigurationVersion(config.get());
        }
    }

    @Override
    public void removeMetrologyConfigurationVersion(EffectiveMetrologyConfigurationOnUsagePoint version) {
        if (version.getStart().isBefore(clock.instant())) {
            throw new RemoveCurrentEffectiveMetrologyConfigurationException(thesaurus);
        }
        version.close(version.getStart());
        this.update();
    }

    @Override
    public Optional<EffectiveMetrologyConfigurationOnUsagePoint> findEffectiveMetrologyConfigurationById(long id) {
        return dataModel.mapper(EffectiveMetrologyConfigurationOnUsagePoint.class).getOptional(id);
    }

    @Override
    public UsagePointCustomPropertySetExtension forCustomProperties() {
        if (this.customPropertySetExtension == null) {
            this.customPropertySetExtension = new UsagePointCustomPropertySetExtensionImpl(this.clock, this.customPropertySetService, this.thesaurus, this);
        }
        return this.customPropertySetExtension;
    }

    @Override
    @Deprecated
    public ConnectionState getConnectionState() {
        return getCurrentConnectionState().map(UsagePointConnectionState::getConnectionState).orElse(null);
    }

    @Override
    public Optional<UsagePointConnectionState> getCurrentConnectionState() {
        return getConnectionStateAt(this.clock.instant());
    }

    private Optional<UsagePointConnectionState> getConnectionStateAt(Instant time) {
        return this.connectionState.effective(time).map(Function.identity());
    }

    @Override
    public String getConnectionStateDisplayName() {
        return getCurrentConnectionState().map(UsagePointConnectionState::getConnectionStateDisplayName).orElse(null);
    }

    @Override
    public void setConnectionState(ConnectionState connectionState) {
        this.setConnectionState(connectionState, this.clock.instant());
    }

    @Override
    public void setConnectionState(ConnectionState newConnectionState, Instant effectiveDate) {
        if (effectiveDate.isBefore(getInstallationTime())) {
            throw ConnectionStateChangeException.stateChangeTimeShouldBeAfterInstallationTime(thesaurus);
        }
        Optional<UsagePointConnectionStateImpl> latestConnectionState = this.connectionState.all().stream()
                .max(Comparator.comparing(state -> state.getRange().lowerEndpoint()));
        if (latestConnectionState.isPresent()) {
            if (!isConnectionStateChangeFeasible(effectiveDate, latestConnectionState.get(), newConnectionState)) {
                throw ConnectionStateChangeException.stateChangeTimeShouldBeAfterLatestConnectionStateChange(thesaurus);
            }
            if (!isConnectionStateChangeNeeded(newConnectionState, latestConnectionState.get())) {
                return;
            }
            latestConnectionState.get().endAt(effectiveDate);
        }
        createNewConnectionState(newConnectionState, effectiveDate);
        touch();
    }

    private boolean isConnectionStateChangeFeasible(Instant effectiveDate, UsagePointConnectionState latestState, ConnectionState newState) {
        Range<Instant> latestStateInterval = latestState.getRange();
        return !(latestStateInterval.hasUpperBound() && effectiveDate.isBefore(latestStateInterval.upperEndpoint())) &&
                !(latestStateInterval.hasLowerBound() && effectiveDate.isBefore(latestStateInterval.lowerEndpoint())) &&
                !(effectiveDate.equals(latestStateInterval.lowerEndpoint()) && latestState.getConnectionState() != newState);
    }

    private boolean isConnectionStateChangeNeeded(ConnectionState newConnectionState, UsagePointConnectionState latestConnectionState) {
        return latestConnectionState.getConnectionState() != newConnectionState;
    }

    private void createNewConnectionState(ConnectionState connectionState, Instant effectiveDate) {
        UsagePointConnectionStateImpl usagePointConnectionState =
                this.dataModel.getInstance(UsagePointConnectionStateImpl.class).init(this, connectionState, Range.atLeast(effectiveDate));
        this.connectionState.add(usagePointConnectionState);
    }

    @Override
    public List<CompletionOptions> connect(Instant when, ServiceCall serviceCall) {
        return this.getMeterActivations(when)
                .stream()
                .map(MeterActivation::getMeter)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(meter -> meter.getHeadEndInterface()
                        .flatMap(headEndInterface -> createCommand(headEndInterface, he -> he.sendCommand(headEndInterface
                                .getCommandFactory()
                                .createConnectCommand(meter, when), when, serviceCall))))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(CompletionOptions.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public List<CompletionOptions> disconnect(Instant when, ServiceCall serviceCall) {
        return this.getMeterActivations(when)
                .stream()
                .map(MeterActivation::getMeter)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(meter -> meter.getHeadEndInterface()
                        .flatMap(headEndInterface -> createCommand(headEndInterface, he -> he.sendCommand(he.getCommandFactory()
                                .createDisconnectCommand(meter, when), when, serviceCall))))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(CompletionOptions.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public List<CompletionOptions> enableLoadLimit(Instant when, Quantity loadLimit, ServiceCall serviceCall) {
        return this.getMeterActivations(when)
                .stream()
                .map(MeterActivation::getMeter)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(meter -> meter.getHeadEndInterface()
                        .flatMap(headEndInterface -> createCommand(headEndInterface, he -> he.sendCommand(headEndInterface
                                .getCommandFactory()
                                .createEnableLoadLimitCommand(meter, loadLimit), when, serviceCall))))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(CompletionOptions.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public List<CompletionOptions> disableLoadLimit(Instant when, ServiceCall serviceCall) {
        return this.getMeterActivations(when)
                .stream()
                .map(MeterActivation::getMeter)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(meter -> meter.getHeadEndInterface()
                        .flatMap(headEndInterface -> createCommand(headEndInterface, he -> he.sendCommand(headEndInterface
                                .getCommandFactory()
                                .createDisableLoadLimitCommand(meter), when, serviceCall))))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(CompletionOptions.class::cast)
                .collect(Collectors.toList());
    }

    private Optional<CompletionOptions> createCommand(HeadEndInterface headEndInterface, Function<HeadEndInterface, CompletionOptions> function) {
        try {
            return Optional.ofNullable(function.apply(headEndInterface));
        } catch (UnsupportedCommandException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<CompletionOptions> readData(Instant when, List<ReadingType> readingTypes, ServiceCall serviceCall) {
        UsagePointMetrologyConfiguration metrologyConfiguration = getEffectiveMetrologyConfiguration(when).get()
                .getMetrologyConfiguration();

        ReadingTypeRequirementsCollector requirementsCollector = new ReadingTypeRequirementsCollector();

        metrologyConfiguration.getDeliverables()
                .stream()
                .filter(deliverable -> readingTypes.contains(deliverable.getReadingType()))
                .map(ReadingTypeDeliverable::getFormula)
                .map(Formula::getExpressionNode)
                .forEach(expressionNode -> expressionNode.accept(requirementsCollector));

        List<ReadingTypeRequirement> readingTypeRequirements = requirementsCollector.getReadingTypeRequirements();
        List<MeterActivationSet> meterActivationSets = dataAggregationService.getMeterActivationSets(this, when);

        List<ReadingType> configuredReadingTypes = this.getMeterActivations(when).stream()
                .filter(meterActivation -> meterActivationSets.stream()
                        .anyMatch(meterActivationSet -> readingTypeRequirements.stream()
                                .anyMatch(requirement -> meterActivation.getChannelsContainer()
                                        .getChannels()
                                        .containsAll(meterActivationSet.getMatchingChannelsFor(requirement)))))
                .map(ma -> getConfiguredMatchingReadingTypes(ma, readingTypeRequirements))
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());
        if (!readingTypeRequirements.stream()
                .allMatch(readingTypeRequirement -> configuredReadingTypes.stream()
                        .anyMatch(readingTypeRequirement::matches))) {
            return Collections.emptyList();
        }

        return this.getMeterActivations(when).stream()
                .map(meterActivation -> readData(when, meterActivation, readingTypeRequirements))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<CompletionOptions> readData(Instant when, MeterActivation meterActivation, List<ReadingTypeRequirement> readingTypeRequirements) {
        List<ReadingType> configuredReadingTypes = getConfiguredMatchingReadingTypes(meterActivation, readingTypeRequirements);
        Optional<Meter> meter = meterActivation.getMeter();

        if (meter.isPresent()) {
            if (!configuredReadingTypes.isEmpty()) {
                return meter.get().getHeadEndInterface()
                        .map(headEndInterface -> headEndInterface.scheduleMeterRead(meter.get()
                                , configuredReadingTypes.stream().filter(headEndInterface.getCapabilities(meter.get())
                                        .getConfiguredReadingTypes()::contains).collect(Collectors.toList())
                                , when));
            }
        }
        return Optional.empty();
    }

    private List<ReadingType> getConfiguredMatchingReadingTypes(MeterActivation meterActivation, List<ReadingTypeRequirement> readingTypeRequirements) {
        Optional<Meter> meter = meterActivation.getMeter();

        if (meter.isPresent()) {
            return meter.get().getHeadEndInterface()
                    .map(headEndInterface -> headEndInterface.getCapabilities(meter.get())
                            .getConfiguredReadingTypes()
                            .stream()
                            .filter(rt -> readingTypeRequirements.stream().anyMatch(rtr -> rtr.matches(rt)))
                            .collect(Collectors.toList())).orElse(Collections.emptyList());
        } else {
            return Collections.emptyList();
        }
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
        Optional<UsagePointDetailImpl> optional = this.getDetails().stream().filter(detail -> detail.getRange().contains(newDetail.getRange().lowerEndpoint())).findFirst();
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
        return getCurrentMeterActivations()
                .stream()
                .filter(ma -> ma.getMeter().isPresent())
                .map(ma -> ma.getMeter().get().getZoneId())
                .findAny()
                .orElse(ZoneId.systemDefault());
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
    public Optional<Location> getLocation() {
        return upLocation.getOptional();
    }

    @Override
    public void setLocation(long locationId) {
        this.location = locationId;
    }

    @Override
    public Optional<SpatialCoordinates> getSpatialCoordinates() {
        return spatialCoordinates == null ? Optional.empty() : Optional.of(spatialCoordinates);
    }

    @Override
    public void setSpatialCoordinates(SpatialCoordinates geoCoordinates) {
        this.spatialCoordinates = geoCoordinates;
    }

    @Override
    public LocationBuilder updateLocation() {
        return new LocationBuilderImpl(dataModel);
    }

    @Override
    public Optional<MeterActivation> getMeterActivation(Instant when) {
        return getMeterActivations(when).stream()
                .filter(ma -> ma.getMeterRole()
                        .map(MeterRole::getKey)
                        .map(DefaultMeterRole.DEFAULT.getKey()::equals)
                        .orElse(true))
                .sorted(Comparator.comparing(MeterActivation::getStart))
                .findFirst();
    }

    @Override
    public List<MeterActivation> getMeterActivations(Instant when) {
        return this.meterActivations
                .stream()
                .filter(meterActivation -> !meterActivation.getInterval().toClosedRange().isEmpty())
                .filter(meterActivation -> meterActivation.isEffectiveAt(when))
                .sorted(Comparator.comparing(MeterActivation::getStart))
                .collect(Collectors.toList());
    }

    List<MeterActivation> getMeterActivations(Range<Instant> range) {
        if (range == null) {
            throw new IllegalArgumentException("Range can't be null");
        }
        return this.meterActivations.stream()
                .filter(meterActivation -> {
                    try {
                        return !meterActivation.getRange().intersection(range).isEmpty();
                    } catch (IllegalArgumentException ex) {
                        return false;
                    }
                })
                .sorted(Comparator.comparing(MeterActivation::getStart))
                .collect(Collectors.toList());
    }

    @Override
    public List<MeterActivation> getMeterActivations() {
        return Collections.unmodifiableList(meterActivations);
    }

    @Override
    public List<MeterActivation> getCurrentMeterActivations() {
        return this.meterActivations.stream()
                .filter(MeterActivation::isCurrent)
                .sorted(Comparator.comparing(MeterActivation::getStart))
                .collect(Collectors.toList());
    }

    @Override
    public List<MeterActivation> getMeterActivations(MeterRole role) {
        return this.meterActivations
                .stream()
                .filter(test(this::roleMatches).with(role))
                .sorted(Comparator.comparing(MeterActivation::getStart))
                .collect(Collectors.toList());
    }

    private boolean roleMatches(IMeterActivation ma, MeterRole role) {
        return ma.getMeterRole().isPresent() && ma.getMeterRole().get().equals(role);
    }

    @Override
    public MeterActivation activate(Meter meter, Instant start) {
        return this.activate(meter, metrologyConfigurationService.findDefaultMeterRole(DefaultMeterRole.DEFAULT), start);
    }

    @Override
    public MeterActivation activate(Meter meter, MeterRole meterRole, Instant from) {
        MeterActivationImpl result = meterActivationFactory.get().init(meter, meterRole, this, from);
        result.save();
        adopt(result);
        return result;
    }

    @Override
    public UsagePointMeterActivator linkMeters() {
        return this.dataModel.getInstance(UsagePointMeterActivatorImpl.class).init(this);
    }

    @Override
    public UsagePointState getState() {
        return this.state.effective(this.clock.instant())
                .map(UsagePointStateTemporalImpl::getState)
                .orElseThrow(() -> new IllegalArgumentException("Usage point has no state at the moment."));
    }

    @Override
    public UsagePointState getState(Instant instant) {
        Objects.requireNonNull(instant);
        return this.state.effective(instant)
                .map(UsagePointStateTemporalImpl::getState)
                .orElseThrow(() -> new IllegalArgumentException("Usage point has no state at " + instant));
    }

    @Override
    public void setInitialState() {
        if (!state.all().isEmpty()) {
            throw new IllegalStateException("Usage point already has life cycle state");
        }
        setState(getInitialStateOfDefaultLifeCycle(), getInstallationTime());
    }

    private UsagePointState getInitialStateOfDefaultLifeCycle() {
        return usagePointLifeCycleConfigurationService.getDefaultLifeCycle().getStates().stream()
                .filter(UsagePointState::isInitial)
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Default usage point life cycle has no initial state"));
    }

    public void setState(UsagePointState state, Instant startTime) {
        Objects.requireNonNull(state);
        Objects.requireNonNull(startTime);
        this.state.all().stream()
                .filter(candidate -> !candidate.getRange().lowerEndpoint().isBefore(startTime))
                .findFirst()
                .ifPresent(nextState -> {
                    throw new IllegalArgumentException("Can't change state for usage point because it has state changes after that time: " + startTime);
                });
        this.state.effective(startTime).ifPresent(activeState -> activeState.close(startTime));
        this.state.add(this.dataModel.getInstance(UsagePointStateTemporalImpl.class).init(this, state, startTime));
        touch();
    }

    public void adopt(MeterActivationImpl meterActivation) {
        meterActivations.stream()
                .filter(activation -> activation.getId() != meterActivation.getId())
                .filter(activation -> this.sameMeterRole(activation, meterActivation))
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

    private boolean sameMeterRole(MeterActivation ma1, MeterActivation ma2) {
        return this.sameMeterRole(ma1.getMeterRole(), ma2.getMeterRole());
    }

    private boolean sameMeterRole(Optional<MeterRole> r1, Optional<MeterRole> r2) {
        if (r1.isPresent() && r2.isPresent()) {
            return Checks.is(r1.get()).equalTo(r2.get());
        } else {
            return !r1.isPresent() && !r2.isPresent();
        }
    }

    void refreshMeterActivations() {
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
        this.getMeterActivations()
                .forEach(meterActivation -> meterActivation.getMeter().ifPresent(meter -> {
                    if (upLocation.isPresent()) {
                        if (location != upLocation.get().getId()) {
                            if (!meter.getLocation().isPresent() || meter.getLocation().get().getId() == upLocation.get().getId()) {
                                findLocation(location).ifPresent(meter::setLocation);
                            }
                        }
                    } else if (location != 0) {
                        if (!meter.getLocation().isPresent()) {
                            findLocation(location).ifPresent(meter::setLocation);
                        }
                    }
                    if (spatialCoordinates != null) {
                        dataModel.mapper(UsagePoint.class).getOptional(this.getId()).ifPresent(up -> {
                            if (up.getSpatialCoordinates().isPresent()) {
                                if (!meter.getSpatialCoordinates().isPresent() || meter.getSpatialCoordinates()
                                        .get().equals(up.getSpatialCoordinates().get())) {
                                    meter.setSpatialCoordinates(spatialCoordinates);
                                }
                            } else {
                                if (!meter.getSpatialCoordinates().isPresent()) {
                                    meter.setSpatialCoordinates(spatialCoordinates);
                                }
                            }
                        });
                    } else {
                        meter.setSpatialCoordinates(null);
                    }
                    meter.update();
                }));
    }

    private Optional<Location> findLocation(long id) {
        return dataModel.mapper(Location.class).getOptional(id);
    }

    @Override
    public void makeObsolete() {
        this.obsoleteTime = this.clock.instant();
        this.dataModel.update(this, "obsoleteTime");
        this.getEffectiveMetrologyConfiguration(this.obsoleteTime)
                .ifPresent(efmc -> efmc.close(this.obsoleteTime));
        eventService.postEvent(EventType.USAGEPOINT_DELETED.topic(), this);
    }

    @Override
    public Optional<Instant> getObsoleteTime() {
        return Optional.ofNullable(this.obsoleteTime);
    }
}
