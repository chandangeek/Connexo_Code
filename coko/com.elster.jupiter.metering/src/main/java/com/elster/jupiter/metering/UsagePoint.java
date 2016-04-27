package com.elster.jupiter.metering;

import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.cbo.MarketRoleKind;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.users.User;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@ProviderType
public interface UsagePoint extends IdentifiedObject, ReadingContainer {

    long getId();

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

    List<? extends MeterActivation> getMeterActivations();

    Optional<MeterActivation> getCurrentMeterActivation();

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

    MeterActivation activate(Instant start);

    MeterActivation activate(Meter meter, Instant start);

    List<UsagePointAccountability> getAccountabilities();

    UsagePointAccountability addAccountability(PartyRole role, Party party, Instant start);

    Optional<Party> getCustomer(Instant when);

    Optional<Party> getResponsibleParty(Instant when, MarketRoleKind marketRole);

    boolean hasAccountability(User user);

    List<? extends UsagePointDetail> getDetail(Range<Instant> range);

    Optional<? extends UsagePointDetail> getDetail(Instant when);

    void addDetail(UsagePointDetail usagePointDetail);

    UsagePointDetail terminateDetail(UsagePointDetail detail, Instant date);

    Optional<MeterActivation> getMeterActivation(Instant when);

    UsagePointDetailBuilder newDefaultDetailBuilder(Instant start);

    ElectricityDetailBuilder newElectricityDetailBuilder(Instant start);

    GasDetailBuilder newGasDetailBuilder(Instant instant);

    WaterDetailBuilder newWaterDetailBuilder(Instant instant);

    HeatDetailBuilder newHeatDetailBuilder(Instant start);

    List<? extends BaseReadingRecord> getReadingsWithFill(Range<Instant> range, ReadingType readingType);

    UsagePointConfigurationBuilder startingConfigurationOn(Instant startTime);

    Optional<UsagePointConfiguration> getConfiguration(Instant time);

    long getLocationId();

    Optional<Location> getLocation();

    void setLocation(long locationId);

    long getGeoCoordinatesId();

    Optional<GeoCoordinates> getGeoCoordinates();

    void setGeoCoordinates(GeoCoordinates geoCoordinates);

    /**
     * Applies the specified {@link MetrologyConfiguration} to this UsagePoint
     * from this point in time onward.
     *
     * @param metrologyConfiguration The MetrologyConfiguration
     * @see #apply(MetrologyConfiguration, Instant)
     */
    void apply(MetrologyConfiguration metrologyConfiguration);

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
    void apply(MetrologyConfiguration metrologyConfiguration, Instant when);

    /**
     * Gets the current {@link MetrologyConfiguration}
     * that has been applied to this UsagePoint.
     *
     * @return The current MetrologyConfiguration
     */
    Optional<MetrologyConfiguration> getMetrologyConfiguration();

    /**
     * Gets the {@link MetrologyConfiguration} that was
     * applied to this UsagePoint at the specified time.
     *
     * @param when The instant in time
     * @return The MetrologyConfiguration
     */
    Optional<MetrologyConfiguration> getMetrologyConfiguration(Instant when);

    /**
     * Gets the {@link MetrologyConfiguration}s that were
     * applied to this UsagePoint during the specified period in time.
     *
     * @param period The period in time
     * @return The List of MetrologyConfiguration
     */
    List<MetrologyConfiguration> getMetrologyConfigurations(Range<Instant> period);

    void removeMetrologyConfiguration(Instant when);

    UsagePointCustomPropertySetExtension forCustomProperties();

    ConnectionState getConnectionState();

    void setConnectionState(ConnectionState connectionState);

    void update();

    void delete();

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
}
