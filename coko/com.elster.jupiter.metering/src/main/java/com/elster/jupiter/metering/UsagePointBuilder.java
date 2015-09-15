package com.elster.jupiter.metering;

public interface UsagePointBuilder {

    UsagePoint create();

    UsagePointBuilder setAliasName(String aliasName);
    UsagePointBuilder setDescription(String description);
    UsagePointBuilder setMRID(String mRID);
    UsagePointBuilder setName(String name);
    UsagePointBuilder setIsSdp(boolean isSdp);
    UsagePointBuilder setIsVirtual(boolean isVirtual);
    UsagePointBuilder setOutageRegion(String outageRegion);
    UsagePointBuilder setReadCycle(String readCycle);
    UsagePointBuilder setReadRoute(String readRoute);
    UsagePointBuilder setServicePriority(String servicePriority);
    UsagePointBuilder setServiceLocation(ServiceLocation serviceLocation);
}
