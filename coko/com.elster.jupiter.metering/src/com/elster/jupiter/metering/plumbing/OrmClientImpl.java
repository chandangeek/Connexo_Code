package com.elster.jupiter.metering.plumbing;

import com.elster.jupiter.metering.*;
import com.elster.jupiter.metering.impl.AmrSystemImpl;
import com.elster.jupiter.metering.impl.ChannelImpl;
import com.elster.jupiter.metering.impl.MeterActivationImpl;
import com.elster.jupiter.metering.impl.MeterImpl;
import com.elster.jupiter.metering.impl.ReadingTypeImpl;
import com.elster.jupiter.metering.impl.ReadingTypeInChannel;
import com.elster.jupiter.metering.impl.ServiceCategoryImpl;
import com.elster.jupiter.metering.impl.ServiceLocationImpl;
import com.elster.jupiter.metering.impl.UsagePointImpl;
import com.elster.jupiter.orm.*;
import com.elster.jupiter.orm.cache.TypeCache;

import static com.elster.jupiter.metering.plumbing.Bus.*;
import static com.elster.jupiter.metering.plumbing.TableSpecs.*;

class OrmClientImpl implements OrmClient {
	
	private final OrmService service;
	
	OrmClientImpl(OrmService service) {
		this.service = service;
	}
	
	@Override
	public TypeCache<ServiceCategory> getServiceCategoryFactory() {
		return Bus.getCacheService().getTypeCache(ServiceCategory.class, ServiceCategoryImpl.class , COMPONENTNAME , MTR_SERVICECATEGORY.name());
	}
	
	@Override
	public DataMapper<ServiceLocation> getServiceLocationFactory() {
		return service.getDataMapper(ServiceLocation.class, ServiceLocationImpl.class , COMPONENTNAME , MTR_SERVICELOCATION.name());
	}
	
	@Override
	public TypeCache<AmrSystem> getAmrSystemFactory() {
		return Bus.getCacheService().getTypeCache(AmrSystem.class, AmrSystemImpl.class, COMPONENTNAME , MTR_AMRSYSTEM.name());
	}
	
	@Override
	public TypeCache<ReadingType> getReadingTypeFactory() {
		return Bus.getCacheService().getTypeCache(ReadingType.class, ReadingTypeImpl.class , COMPONENTNAME , MTR_READINGTYPE.name());
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
