package com.elster.jupiter.metering;

import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.cbo.MarketRoleKind;
import com.elster.jupiter.metering.ami.CompletionOptions;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.servicecall.ServiceCall;
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

    void setMRID(String mRID);

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
     * @param metrologyConfiguration The UsagePointMetrologyConfiguration
     * @see #apply(UsagePointMetrologyConfiguration, Instant)
     */
    void apply(UsagePointMetrologyConfiguration metrologyConfiguration);

    /**
     * Applies the specified {@link MetrologyConfiguration} to this UsagePoint
     * from the specified instant in time onward.
     * Note that this may produce errors when e.g. the requirements
     * of the MetrologyConfiguration are not met by the Meter(s) that is/are
     * linked to this UsagePoint from that instant in time onward.
     *
     * @param metrologyConfiguration The MetrologyConfiguration
     * @param when The instant in time
     */
    void apply(UsagePointMetrologyConfiguration metrologyConfiguration, Instant when);

    /**
     * Gets the current {@link MetrologyConfiguration}
     * that has been applied to this UsagePoint.
     *
     * @return The current MetrologyConfiguration
     */
    Optional<UsagePointMetrologyConfiguration> getMetrologyConfiguration();

    /**
     * Gets the {@link MetrologyConfiguration} that was
     * applied to this UsagePoint at the specified time.
     *
     * @param when The instant in time
     * @return The MetrologyConfiguration
     */
    Optional<UsagePointMetrologyConfiguration> getMetrologyConfiguration(Instant when);

    /**
     * Gets the {@link MetrologyConfiguration}s that were
     * applied to this UsagePoint during the specified period in time.
     *
     * @param period The period in time
     * @return The List of MetrologyConfiguration
     */
    List<UsagePointMetrologyConfiguration> getMetrologyConfigurations(Range<Instant> period);

    void removeMetrologyConfiguration(Instant when);

    UsagePointCustomPropertySetExtension forCustomProperties();

    ConnectionState getConnectionState();

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
     * Use the {@link #getCurrentMeterActivations()} instead.
     * In fact this method returns the current meter activation for {@link com.elster.jupiter.metering.config.DefaultMeterRole#DEFAULT} meter role
     */
    @Deprecated
    Optional<MeterActivation> getCurrentMeterActivation();

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

    // TODO delete start (methods from ReadingContainer) =============================================================

    @Deprecated
    ZoneId getZoneId(); // dependency in data aggregation

    // TODO delete end ===============================================================================================

}
