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
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;

public class OrmClientImpl implements OrmClient {
	
	private final DataModel dataModel;
	
	public OrmClientImpl(DataModel dataModel) {
		this.dataModel = dataModel;
	}
	
	@Override
	public DataMapper<ServiceCategory> getServiceCategoryFactory() {
		return dataModel.mapper(ServiceCategory.class);
	}
	
	@Override
	public DataMapper<ServiceLocation> getServiceLocationFactory() {
		return dataModel.mapper(ServiceLocation.class);
	}
	
	@Override
	public DataMapper<AmrSystem> getAmrSystemFactory() {
		return dataModel.mapper(AmrSystem.class);
	}
	
	@Override
	public DataMapper<ReadingType> getReadingTypeFactory() {
		return dataModel.mapper(ReadingType.class);
	}
	
	@Override
	public DataMapper<UsagePoint> getUsagePointFactory() {
		return dataModel.mapper(UsagePoint.class);
	}
	
	@Override
	public DataMapper<EndDevice> getEndDeviceFactory() {
		return dataModel.mapper(EndDevice.class);
	}

	@Override
	public DataMapper<MeterActivation> getMeterActivationFactory() {
		return dataModel.mapper(MeterActivation.class);
	}
	
	@Override
	public DataMapper<Channel> getChannelFactory() {
		return dataModel.mapper(Channel.class);
	}

	@Override
	public DataMapper<ReadingTypeInChannel> getReadingTypeInChannelFactory() {
		return dataModel.mapper(ReadingTypeInChannel.class);
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
		return dataModel.mapper(UsagePointAccountability.class);
	}

    @Override
    public DataMapper<EnumeratedUsagePointGroup.Entry> getEnumeratedUsagePointGroupEntryFactory() {
        return dataModel.mapper(EnumeratedUsagePointGroup.Entry.class);
    }

    @Override
    public DataMapper<UsagePointGroup> getUsagePointGroupFactory() {
        return dataModel.mapper(UsagePointGroup.class);
    }

    @Override
    public DataMapper<QueryUsagePointGroup> getQueryUsagePointGroupFactory() {
        return dataModel.mapper(QueryUsagePointGroup.class);
    }

    @Override
    public DataMapper<EnumeratedUsagePointGroup> getEnumeratedUsagePointGroupFactory() {
        return dataModel.mapper(EnumeratedUsagePointGroup.class);
    }

    @Override
    public DataMapper<QueryBuilderOperation> getQueryBuilderOperationFactory() {
        return dataModel.mapper(QueryBuilderOperation.class);
    }

    @Override
    public DataMapper<ReadingQuality> getReadingQualityFactory() {
        return dataModel.mapper(ReadingQuality.class);
    }

    @Override
    public DataMapper<EndDeviceEventType> getEndDeviceEventTypeFactory() {
        return dataModel.mapper(EndDeviceEventType.class);
    }

    @Override
    public DataMapper<EndDeviceEventRecord> getEndDeviceEventRecordFactory() {
        return dataModel.mapper(EndDeviceEventRecord.class);
    }
}
