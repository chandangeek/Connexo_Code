package com.elster.jupiter.metering.plumbing;

import com.elster.jupiter.metering.*;
import com.elster.jupiter.metering.impl.ReadingTypeInChannel;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.cache.TypeCache;

public interface OrmClient {
	TypeCache<ServiceCategory> getServiceCategoryFactory();
	DataMapper<ServiceLocation> getServiceLocationFactory();
	TypeCache<AmrSystem> getAmrSystemFactory();
	TypeCache<ReadingType> getReadingTypeFactory();
	DataMapper<UsagePoint> getUsagePointFactory();
	DataMapper<Meter> getMeterFactory();
	DataMapper<MeterActivation> getMeterActivationFactory();
	DataMapper<Channel> getChannelFactory();
	DataMapper<ReadingTypeInChannel> getReadingTypeInChannelFactory();
	DataMapper<UsagePointAccountability> getUsagePointAccountabilityFactory();
	void install(boolean executeDdl , boolean storeMappings);	
	DataModel getDataModel();
}