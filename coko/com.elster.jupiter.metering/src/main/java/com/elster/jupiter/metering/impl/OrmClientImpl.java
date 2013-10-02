package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.*;
import com.elster.jupiter.orm.*;
import com.elster.jupiter.orm.cache.TypeCache;

public class OrmClientImpl implements OrmClient {
	
	private final DataModel dataModel;
	
	public OrmClientImpl(DataModel dataModel) {
		this.dataModel = dataModel;
	}
	
	@Override
	public TypeCache<ServiceCategory> getServiceCategoryFactory() {
		return Bus.getComponentCache().getTypeCache(ServiceCategory.class, ServiceCategoryImpl.class, TableSpecs.MTR_SERVICECATEGORY.name());
	}
	
	@Override
	public DataMapper<ServiceLocation> getServiceLocationFactory() {
		return dataModel.getDataMapper(ServiceLocation.class, ServiceLocationImpl.class, TableSpecs.MTR_SERVICELOCATION.name());
	}
	
	@Override
	public TypeCache<AmrSystem> getAmrSystemFactory() {
		return Bus.getComponentCache().getTypeCache(AmrSystem.class, AmrSystemImpl.class , TableSpecs.MTR_AMRSYSTEM.name());
	}
	
	@Override
	public TypeCache<ReadingType> getReadingTypeFactory() {
		return Bus.getComponentCache().getTypeCache(ReadingType.class, ReadingTypeImpl.class, TableSpecs.MTR_READINGTYPE.name());
	}
	
	@Override
	public DataMapper<UsagePoint> getUsagePointFactory() {
		return dataModel.getDataMapper(UsagePoint.class, UsagePointImpl.class, TableSpecs.MTR_USAGEPOINT.name());
	}
	
	@Override
	public DataMapper<Meter> getMeterFactory() {
		return dataModel.getDataMapper(Meter.class, MeterImpl.class, TableSpecs.MTR_METER.name());
	}

	@Override
	public DataMapper<MeterActivation> getMeterActivationFactory() {
		return dataModel.getDataMapper(MeterActivation.class, MeterActivationImpl.class, TableSpecs.MTR_METERACTIVATION.name());
	}
	
	@Override
	public DataMapper<Channel> getChannelFactory() {
		return dataModel.getDataMapper(Channel.class, ChannelImpl.class, TableSpecs.MTR_CHANNEL.name());
	}

	@Override
	public DataMapper<ReadingTypeInChannel> getReadingTypeInChannelFactory() {
		return dataModel.getDataMapper(ReadingTypeInChannel.class, ReadingTypeInChannel.class, TableSpecs.MTR_READINGTYPEINCHANNEL.name());
	}

	@Override
	public void install(boolean executeDdl,boolean saveMappings) {
		dataModel.install(executeDdl,saveMappings);		
	}
	
	@Override
	public DataModel getDataModel() {
		return dataModel;
	}

	@Override
	public DataMapper<UsagePointAccountability> getUsagePointAccountabilityFactory() {
		return dataModel.getDataMapper(UsagePointAccountability.class, UsagePointAccountabilityImpl.class, TableSpecs.MTR_UPACCOUNTABILITY.name());
	}
	
		
}
