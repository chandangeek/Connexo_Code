package com.elster.jupiter.metering;

import java.util.Date;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.util.conditions.Condition;
import com.google.common.base.Optional;

public interface MeteringService {
	Optional<ReadingType> getReadingType(String mRid);
	ServiceLocation newServiceLocation();
	ServiceLocation findServiceLocation(String mRid);
	Optional<ServiceLocation> findServiceLocation(long id);
	Optional<ServiceCategory> getServiceCategory(ServiceKind kind);
	Optional<UsagePoint> findUsagePoint(long id);
	ReadingStorer createStorer(boolean overrules);
	Query<UsagePoint> getUsagePointQuery();
	Query<MeterActivation> getMeterActivationQuery();
	Query<ServiceLocation> getServiceLocationQuery();
	Condition hasAccountability();
	Condition hasAccountability(Date when);
}
