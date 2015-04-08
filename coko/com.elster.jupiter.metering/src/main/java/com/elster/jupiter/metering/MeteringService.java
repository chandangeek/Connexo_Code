package com.elster.jupiter.metering;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.orm.JournalEntry;
import com.elster.jupiter.util.conditions.Condition;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MeteringService {
    String COMPONENTNAME = "MTR";

    ServiceLocation newServiceLocation();

    ReadingStorer createUpdatingStorer();

    ReadingStorer createOverrulingStorer();

    ReadingStorer createNonOverrulingStorer();

    Optional<ReadingType> getReadingType(String mRid);

    Optional<ServiceLocation> findServiceLocation(String mRid);

    Optional<ServiceLocation> findServiceLocation(long id);

    Optional<ServiceCategory> getServiceCategory(ServiceKind kind);

    Optional<UsagePoint> findUsagePoint(long id);

    Optional<Meter> findMeter(long id);

    Optional<Meter> findMeter(String mRid);

    Optional<EndDevice> findEndDevice(String mRid);

    Optional<MeterActivation> findMeterActivation(long meterActivationId);

    Optional<Channel> findChannel(long id);

    Optional<AmrSystem> findAmrSystem(long id);

    List<ReadingType> getAvailableReadingTypes();

    /**
     * Get a list of ReadingTypes which don't have an interval (eg. MacroPeriod and TimePeriod are NOT_APPLICABLE)
     * @since v1.1
     */
    List<ReadingType> getAllReadingTypesWithoutInterval();

    List<JournalEntry<ServiceLocation>> findServiceLocationJournal(long id);

    Query<UsagePoint> getUsagePointQuery();

    Query<MeterActivation> getMeterActivationQuery();

    Query<ServiceLocation> getServiceLocationQuery();

    Condition hasAccountability();

    Condition hasAccountability(Instant when);

    Query<Meter> getMeterQuery();

    Optional<UsagePoint> findUsagePoint(String mRID);

    Query<EndDevice> getEndDeviceQuery();

    Optional<EndDevice> findEndDevice(long id);

    List<EndDeviceEventType> getAvailableEndDeviceEventTypes();

    Optional<EndDeviceEventType> getEndDeviceEventType(String mRID);

    void configurePurge(PurgeConfiguration purgeConfiguration);

    ReadingType createReadingType(String mRID, String aliasName);

    void purge(PurgeConfiguration purgeConfiguration);
}
