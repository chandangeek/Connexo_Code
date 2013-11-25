package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.EnumeratedUsagePointGroup;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.QueryUsagePointGroup;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceLocation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointAccountability;
import com.elster.jupiter.metering.UsagePointGroup;
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

    DataMapper<UsagePointGroup> getUsagePointGroupFactory();

    DataMapper<EnumeratedUsagePointGroup.Entry> getEnumeratedUsagePointGroupEntryFactory();

    void install(boolean executeDdl, boolean storeMappings);

    DataModel getDataModel();

    DataMapper<QueryBuilderOperation> getQueryBuilderOperationFactory();

    DataMapper<QueryUsagePointGroup> getQueryUsagePointGroupFactory();

    DataMapper<EnumeratedUsagePointGroup> getEnumeratedUsagePointGroupFactory();
}