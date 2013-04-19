package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.*;
import com.elster.jupiter.orm.DataMapper;

interface OrmClient {
	DataMapper<ServiceCategory> getServiceCategoryFactory();
	DataMapper<ServiceLocation> getServiceLocationFactory();
	DataMapper<AmrSystem> getAmrSystemFactory();
	DataMapper<ReadingType> getReadingTypeFactory();
	DataMapper<UsagePoint> getUsagePointFactory();
	DataMapper<Meter> getMeterFactory();
	DataMapper<MeterActivation> getMeterActivationFactory();

	DataMapper<Channel> getChannelFactory();
	DataMapper<ReadingTypeInChannel> getReadingTypeInChannelFactory();
	
	void install(boolean executeDdl , boolean storeMappings);
	
}