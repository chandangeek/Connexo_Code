package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.*;
import com.elster.jupiter.orm.*;

import static com.elster.jupiter.metering.impl.Bus.*;
import static com.elster.jupiter.metering.impl.TableSpecs.*;

class OrmClientImpl implements OrmClient {
	
	private final OrmService service;
	
	OrmClientImpl(OrmService service) {
		this.service = service;
	}
	
	@Override
	public DataMapper<ServiceCategory> getServiceCategoryFactory() {
		return service.getDataMapper(ServiceCategory.class, ServiceCategoryImpl.class , COMPONENTNAME , MTR_SERVICECATEGORY.name());
	}
	
	@Override
	public DataMapper<ServiceLocation> getServiceLocationFactory() {
		return service.getDataMapper(ServiceLocation.class, ServiceLocationImpl.class , COMPONENTNAME , MTR_SERVICELOCATION.name());
	}
	
	@Override
	public DataMapper<AmrSystem> getAmrSystemFactory() {
		return service.getDataMapper(AmrSystem.class, AmrSystemImpl.class , COMPONENTNAME , MTR_AMRSYSTEM.name());
	}
	
	@Override
	public DataMapper<ReadingType> getReadingTypeFactory() {
		return service.getDataMapper(ReadingType.class, ReadingTypeImpl.class , COMPONENTNAME , MTR_READINGTYPE.name());
	}
	
	@Override
	public DataMapper<UsagePoint> getUsagePointFactory() {
		return service.getDataMapper(UsagePoint.class, UsagePointImpl.class , COMPONENTNAME , MTR_USAGEPOINT.name());
	}
	
	@Override
	public DataMapper<Meter> getMeterFactory() {
		return service.getDataMapper(Meter.class, MeterImpl.class , COMPONENTNAME , MTR_METER.name());
	}

	@Override
	public DataMapper<MeterActivation> getMeterActivationFactory() {
		return service.getDataMapper(MeterActivation.class, MeterActivationImpl.class , COMPONENTNAME , MTR_METERACTIVATION.name());
	}
	
	@Override
	public DataMapper<Channel> getChannelFactory() {
		return service.getDataMapper(Channel.class, ChannelImpl.class , COMPONENTNAME , MTR_CHANNEL.name());
	}

	@Override
	public DataMapper<ReadingTypeInChannel> getReadingTypeInChannelFactory() {
		return service.getDataMapper(ReadingTypeInChannel.class, ReadingTypeInChannel.class , COMPONENTNAME , MTR_READINGTYPEINCHANNEL.name());
	}

	@Override
	public void install(boolean executeDdl,boolean saveMappings) {
		service.install(createComponent(),executeDdl,saveMappings);		
	}
	
	private Component createComponent() {
		Component result = service.newComponent(COMPONENTNAME,"CIM Metering");
		for (TableSpecs spec : TableSpecs.values()) {
			spec.addTo(result);			
		}
		return result;
	}
		
}
