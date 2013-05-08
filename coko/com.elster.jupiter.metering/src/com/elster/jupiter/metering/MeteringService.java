package com.elster.jupiter.metering;

import java.util.Date;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.util.conditions.Condition;

public interface MeteringService {
	ReadingType getReadingType(String mRid);
	ServiceLocation newServiceLocation();
	ServiceLocation findServiceLocation(String mRid);
	ServiceLocation findServiceLocation(long id);
	ServiceCategory getServiceCategory(ServiceKind kind);
	UsagePoint findUsagePoint(long id);
	ReadingStorer createStorer(boolean overrules);
	Query<UsagePoint> getUsagePointQuery();
	Query<MeterActivation> getMeterActivationQuery();
	Query<ServiceLocation> getServiceLocationQuery();
	Condition hasAccountability();
	Condition hasAccountability(Date when);
}
