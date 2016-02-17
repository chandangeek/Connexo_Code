package com.elster.jupiter.metering;

import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.cbo.MarketRoleKind;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.users.User;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UsagePoint extends IdentifiedObject, ReadingContainer {
    long getId();

    boolean isSdp();

    boolean isVirtual();

    String getOutageRegion();

    String getReadCycle();

    String getReadRoute();

    String getServicePriority();

    List<? extends MeterActivation> getMeterActivations();

    Optional<MeterActivation> getCurrentMeterActivation();

    long getServiceLocationId();

    Optional<ServiceLocation> getServiceLocation();

    ServiceCategory getServiceCategory();

    void setServiceLocation(ServiceLocation serviceLocation);

    void setServicePriority(String servicePriority);

    void setReadRoute(String readRoute);

    void setReadCycle(String readCycle);

    void setOutageRegion(String outageRegion);

    void setVirtual(boolean isVirtual);

    void setSdp(boolean isSdp);

    void setName(String name);

    void setMRID(String mRID);

    void setDescription(String description);

    void setAliasName(String aliasName);

    void update();

    Instant getCreateDate();

    Instant getModificationDate();

    long getVersion();

    MeterActivation activate(Instant start);

    MeterActivation activate(Meter meter, Instant start);

    List<UsagePointAccountability> getAccountabilities();

    UsagePointAccountability addAccountability(PartyRole role, Party party, Instant start);

    Optional<Party> getCustomer(Instant when);

    Optional<Party> getResponsibleParty(Instant when, MarketRoleKind marketRole);

    boolean hasAccountability(User user);

    void delete();

    List<? extends UsagePointDetail> getDetail(Range<Instant> range);

    Optional<? extends UsagePointDetail> getDetail(Instant when);

    void addDetail(UsagePointDetail usagePointDetail);

    UsagePointDetail terminateDetail(UsagePointDetail detail, Instant date);

    Optional<MeterActivation> getMeterActivation(Instant when);

    ElectricityDetailBuilder newElectricityDetailBuilder(Instant start);

    GasDetailBuilder newGasDetailBuilder(Instant instant);

    WaterDetailBuilder newWaterDetailBuilder(Instant instant);

    List<? extends BaseReadingRecord> getReadingsWithFill(Range<Instant> range, ReadingType readingType);

    UsagePointConfigurationBuilder startingConfigurationOn(Instant startTime);

    Optional<UsagePointConfiguration> getConfiguration(Instant time);

    /**
     * @deprecated This is not the type of method that we want on a public API, the object should be in charge of its version number
     */
    @Deprecated
    void touch();

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
