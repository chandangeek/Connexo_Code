package com.elster.jupiter.metering;

import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.cbo.MarketRoleKind;
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

    void touch();

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
