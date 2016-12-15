package com.elster.jupiter.metering;

import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.cbo.MarketRoleKind;
import com.elster.jupiter.metering.ami.CompletionOptions;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycle;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointStage;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointState;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.geo.SpatialCoordinates;
import com.elster.jupiter.util.units.Quantity;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@ProviderType
public interface UsagePoint extends HasId, IdentifiedObject {

    long getVersion();

    boolean isSdp();

    void setSdp(boolean isSdp);

    boolean isVirtual();

    void setVirtual(boolean isVirtual);

    String getOutageRegion();

    void setOutageRegion(String outageRegion);

    void setName(String name);

    String getAliasName();

    void setAliasName(String aliasName);

    String getDescription();

    void setDescription(String description);

    String getReadRoute();

    void setReadRoute(String readRoute);

    String getServicePriority();

    void setServicePriority(String servicePriority);

    Optional<ServiceLocation> getServiceLocation();

    void setServiceLocation(ServiceLocation serviceLocation);

    String getServiceLocationString();

    void setServiceLocationString(String serviceLocationString);

    ServiceCategory getServiceCategory();

    Instant getInstallationTime();

    void setInstallationTime(Instant installationTime);

    String getServiceDeliveryRemark();

    void setServiceDeliveryRemark(String serviceDeliveryRemark);

    Instant getCreateDate();

    Instant getModificationDate();

    List<UsagePointAccountability> getAccountabilities();

    UsagePointAccountability addAccountability(PartyRole role, Party party, Instant start);

    Optional<Party> getCustomer(Instant when);

    Optional<Party> getResponsibleParty(Instant when, MarketRoleKind marketRole);

    boolean hasAccountability(User user);

    List<? extends UsagePointDetail> getDetail(Range<Instant> range);

    /**
     * Get all existing details for this usage point
     */
    List<? extends UsagePointDetail> getDetails();

    Optional<? extends UsagePointDetail> getDetail(Instant when);

    void addDetail(UsagePointDetail usagePointDetail);

    UsagePointDetail terminateDetail(UsagePointDetail detail, Instant date);

    UsagePointDetailBuilder newDefaultDetailBuilder(Instant start);

    ElectricityDetailBuilder newElectricityDetailBuilder(Instant start);

    GasDetailBuilder newGasDetailBuilder(Instant instant);

    WaterDetailBuilder newWaterDetailBuilder(Instant instant);

    HeatDetailBuilder newHeatDetailBuilder(Instant start);

    List<? extends BaseReadingRecord> getReadingsWithFill(Range<Instant> range, ReadingType readingType);

    UsagePointConfigurationBuilder startingConfigurationOn(Instant startTime);

    Optional<UsagePointConfiguration> getConfiguration(Instant time);

    Optional<Location> getLocation();

    void setLocation(long locationId);

    Optional<SpatialCoordinates> getSpatialCoordinates();

    void setSpatialCoordinates(SpatialCoordinates spatialCoordinates);

    LocationBuilder updateLocation();

    /**
     * Applies the specified {@link UsagePointMetrologyConfiguration} to this UsagePoint
     * from this point in time onward.
     *
     * @param metrologyConfiguration The MetrologyConfiguration
     * @see #apply(UsagePointMetrologyConfiguration, Instant)
     */
    void apply(UsagePointMetrologyConfiguration metrologyConfiguration);

    /**
     * Applies the specified {@link UsagePointMetrologyConfiguration} to this UsagePoint
     * from the specified instant in time onward.
     * Note that this may produce errors when e.g. the requirements
     * of the MetrologyConfiguration are not met by the Meter(s) that is/are
     * linked to this UsagePoint from that instant in time onward.
     *
     * @param metrologyConfiguration The UsagePointMetrologyConfiguration
     * @param when The instant in time
     */
    void apply(UsagePointMetrologyConfiguration metrologyConfiguration, Instant when);

    void apply(UsagePointMetrologyConfiguration metrologyConfiguration, Instant start, Instant end);

    void updateWithInterval(EffectiveMetrologyConfigurationOnUsagePoint metrologyConfigurationVersion, UsagePointMetrologyConfiguration metrologyConfiguration, Instant start, Instant end);

    Optional<EffectiveMetrologyConfigurationOnUsagePoint> getEffectiveMetrologyConfigurationByStart(Instant start);

    Optional<EffectiveMetrologyConfigurationOnUsagePoint> getCurrentEffectiveMetrologyConfiguration();

    List<EffectiveMetrologyConfigurationOnUsagePoint> getEffectiveMetrologyConfigurations();

    List<EffectiveMetrologyConfigurationOnUsagePoint> getEffectiveMetrologyConfigurations(Range<Instant> when);

    Optional<EffectiveMetrologyConfigurationOnUsagePoint> getEffectiveMetrologyConfiguration(Instant when);

    void removeMetrologyConfiguration(Instant when);

    void removeMetrologyConfigurationVersion(EffectiveMetrologyConfigurationOnUsagePoint version);

    Optional<EffectiveMetrologyConfigurationOnUsagePoint> findEffectiveMetrologyConfigurationById(long id);

    UsagePointCustomPropertySetExtension forCustomProperties();

    /**
     * Returns current connection state of the usage point.
     *
     * @return the ConnectionState
     * @deprecated As connection states {@link ConnectionState#UNDER_CONSTRUCTION} and {@link ConnectionState#DEMOLISHED} were semantically
     * replaced by {@link UsagePointStage.Key#PRE_OPERATIONAL} and {@link UsagePointStage.Key#POST_OPERATIONAL} stages of {@link UsagePointLifeCycle}
     * this method should not be used anymore.
     * <p>
     * Use {@link UsagePoint#getCurrentConnectionState()} instead
     */
    @Deprecated
    ConnectionState getConnectionState();

    /**
     * Returns current connection state of the usage point or Optional.empty() if there is no effective connection state
     * (that make sense if usage point is in {@link UsagePointStage.Key#PRE_OPERATIONAL} or {@link UsagePointStage.Key#POST_OPERATIONAL} stage)
     *
     * @return the ConnectionState
     */
    Optional<ConnectionState> getCurrentConnectionState();

    String getConnectionStateDisplayName();

    void setConnectionState(ConnectionState connectionState);

    void setConnectionState(ConnectionState connectionState, Instant instant);

    List<CompletionOptions> connect(Instant when, ServiceCall serviceCall);

    List<CompletionOptions> disconnect(Instant when, ServiceCall serviceCall);

    List<CompletionOptions> enableLoadLimit(Instant when, Quantity loadLimit, ServiceCall serviceCall);

    List<CompletionOptions> disableLoadLimit(Instant when, ServiceCall serviceCall);

    List<CompletionOptions> readData(Instant when, List<ReadingType> readingTypes, ServiceCall serviceCall);

    void update();

    void delete();

    /**
     * Use the {@link #getMeterActivations(Instant)} instead.
     * In fact this method returns meter activation for {@link com.elster.jupiter.metering.config.DefaultMeterRole#DEFAULT} meter role
     */
    @Deprecated
    Optional<MeterActivation> getMeterActivation(Instant when);

    /**
     * Returns collection which contains one MeterActivation per meter role.
     */
    List<MeterActivation> getMeterActivations(Instant when);

    List<MeterActivation> getMeterActivations();

    /**
     * Returns collection which contains effective meter activations per meter role.
     */
    List<MeterActivation> getCurrentMeterActivations();

    /**
     * Returns the list of MeterActivations that are associated with the meters
     * activated on usage point in particular meter role.
     * The list is sorted ascending by start date of MeterActivation.
     */
    List<MeterActivation> getMeterActivations(MeterRole role);

    /**
     * Use the {@link #activate(Meter, MeterRole, Instant)} instead.
     * In fact the mentioned method will be called with {@link com.elster.jupiter.metering.config.DefaultMeterRole#DEFAULT}
     */
    @Deprecated
    MeterActivation activate(Meter meter, Instant start);

    MeterActivation activate(Meter meter, MeterRole meterRole, Instant from);

    UsagePointMeterActivator linkMeters();

    UsagePointState getState();

    UsagePointState getState(Instant instant);

    interface UsagePointConfigurationBuilder {

        UsagePointConfigurationBuilder endingAt(Instant endTime);

        UsagePointReadingTypeConfigurationBuilder configureReadingType(ReadingType readingType);

        UsagePointConfiguration create();
    }

    interface UsagePointReadingTypeConfigurationBuilder {

        UsagePointConfiguration create();

        UsagePointReadingTypeMultiplierConfigurationBuilder withMultiplierOfType(MultiplierType multiplierOfType);

    }

    interface UsagePointReadingTypeMultiplierConfigurationBuilder {

        UsagePointConfigurationBuilder calculating(ReadingType readingType);
    }

    ZoneId getZoneId();
}
