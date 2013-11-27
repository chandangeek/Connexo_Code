package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EnumeratedUsagePointGroup;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.QueryUsagePointGroup;
import com.elster.jupiter.metering.ReadingQuality;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceLocation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointAccountability;
import com.elster.jupiter.metering.UsagePointGroup;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.cache.TypeCache;
import com.google.common.collect.ImmutableMap;

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
	public DataMapper<EndDevice> getEndDeviceFactory() {
		return dataModel.getDataMapper(EndDevice.class, AbstractEndDeviceImpl.IMPLEMENTERS, TableSpecs.MTR_ENDDEVICE.name());
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

    @Override
    public DataMapper<EnumeratedUsagePointGroup.Entry> getEnumeratedUsagePointGroupEntryFactory() {
        return dataModel.getDataMapper(EnumeratedUsagePointGroup.Entry.class, EnumeratedUsagePointGroupImpl.EntryImpl.class, TableSpecs.MTR_ENUM_UP_IN_GROUP.name());
    }

    @Override
    public DataMapper<UsagePointGroup> getUsagePointGroupFactory() {
        return dataModel.getDataMapper(UsagePointGroup.class, AbstractUsagePointGroup.IMPLEMENTERS, TableSpecs.MTR_UP_GROUP.name());
    }

    @Override
    public DataMapper<QueryUsagePointGroup> getQueryUsagePointGroupFactory() {
        return dataModel.getDataMapper(QueryUsagePointGroup.class, ImmutableMap.<String, Class<?extends QueryUsagePointGroup>>of(QueryUsagePointGroup.TYPE_IDENTIFIER, QueryUsagePointGroupImpl.class), TableSpecs.MTR_UP_GROUP.name());
    }

    @Override
    public DataMapper<EnumeratedUsagePointGroup> getEnumeratedUsagePointGroupFactory() {
        return dataModel.getDataMapper(EnumeratedUsagePointGroup.class, ImmutableMap.<String, Class<?extends EnumeratedUsagePointGroup>>of(EnumeratedUsagePointGroup.TYPE_IDENTIFIER, EnumeratedUsagePointGroupImpl.class), TableSpecs.MTR_UP_GROUP.name());
    }

    @Override
    public DataMapper<QueryBuilderOperation> getQueryBuilderOperationFactory() {
        return dataModel.getDataMapper(QueryBuilderOperation.class, AbstractQueryBuilderOperation.IMPLEMENTERS, TableSpecs.MTR_QUERY_UP_GROUP_OP.name());
    }

    @Override
    public DataMapper<ReadingQuality> getReadingQualityFactory() {
        return dataModel.getDataMapper(ReadingQuality.class, ReadingQualityImpl.class, TableSpecs.MTR_READINGQUALITY.name());
    }
}
