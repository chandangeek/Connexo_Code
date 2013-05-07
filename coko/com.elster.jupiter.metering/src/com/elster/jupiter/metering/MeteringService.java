package com.elster.jupiter.metering;

import java.util.Date;

import com.elster.jupiter.conditions.Condition;
import com.elster.jupiter.domain.util.Query;

public interface MeteringService {
	ReadingType getReadingType(String mRid);
	ServiceLocation newServiceLocation();
	ServiceLocation findServiceLocation(String mRid);
	ServiceLocation findServiceLocation(long id);
	ServiceCategory getServiceCategory(ServiceKind kind);
	UsagePoint findUsagePoint(long id);
	ReadingStorer createStorer(boolean overrules);
	void install(boolean executeDdl, boolean storeMappings,boolean createMasterData);	
	Query<UsagePoint> getUsagePointQuery();
	Query<MeterActivation> getMeterActivationQuery();
	Query<ServiceLocation> getServiceLocationQuery();
	Condition hasAccountability();
	Condition hasAccountability(Date when);
}
