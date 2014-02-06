package com.elster.jupiter.metering;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.orm.JournalEntry;
import com.elster.jupiter.util.conditions.Condition;
import com.google.common.base.Optional;

import java.util.Date;
import java.util.List;

public interface MeteringService {
    String COMPONENTNAME = "MTR";

    ServiceLocation newServiceLocation();

    ReadingStorer createOverrulingStorer();

    ReadingStorer createNonOverrulingStorer();

    Optional<ReadingType> getReadingType(String mRid);

    Optional<ServiceLocation> findServiceLocation(String mRid);

    Optional<ServiceLocation> findServiceLocation(long id);

    Optional<ServiceCategory> getServiceCategory(ServiceKind kind);

    Optional<UsagePoint> findUsagePoint(long id);

    Optional<Meter> findMeter(long id);

    Optional<EndDevice> findEndDevice(String mRid);

    Optional<MeterActivation> findMeterActivation(long meterActivationId);

    Optional<Channel> findChannel(long id);

    Optional<AmrSystem> findAmrSystem(long id);

    List<ReadingType> getAvailableReadingTypes();

    List<JournalEntry<ServiceLocation>> findServiceLocationJournal(long id);

    Query<UsagePoint> getUsagePointQuery();

    Query<MeterActivation> getMeterActivationQuery();

    Query<ServiceLocation> getServiceLocationQuery();

    Condition hasAccountability();

    Condition hasAccountability(Date when);

	Query<Meter> getMeterQuery();

    Optional<UsagePoint> findUsagePoint(String mRID);

    Query<EndDevice> getEndDeviceQuery();

    Optional<EndDevice> findEndDevice(long id);
}
