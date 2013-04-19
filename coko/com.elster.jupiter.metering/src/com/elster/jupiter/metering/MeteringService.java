package com.elster.jupiter.metering;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Query;

public interface MeteringService {
	ReadingType getReadingType(String mRid);
	ServiceLocation newServiceLocation();
	ServiceLocation findServiceLocation(String mRid);
	ServiceLocation findServiceLocation(long id);
	ServiceCategory getServiceCategory(ServiceKind kind);
	Finder<UsagePoint> getUsagePointFinder();
	UsagePoint findUsagePoint(long id);
	ReadingStorer createStorer(boolean overrules);
	void install(boolean executeDdl, boolean storeMappings,boolean createMasterData);	
	Query<UsagePoint> getUsagePointQuery();
	Query<MeterActivation> getMeterActivationQuery();
	Query<ServiceLocation> getServiceLocationQuery();
}
