package com.elster.jupiter.metering;

public interface UsagePointBuilder {

    UsagePointBuilder withAliasName(String aliasName);

    UsagePointBuilder withDescription(String description);

    UsagePointBuilder withMRID(String mRID);

    UsagePointBuilder withName(String name);

    UsagePointBuilder withIsSdp(Boolean isSdp);

    UsagePointBuilder withIsVirtual(Boolean isVirtual);

    UsagePointBuilder withOutageRegion(String outageRegion);

    UsagePointBuilder withReadCycle(String readCycle);

    UsagePointBuilder withReadRoute(String readRoute);

    UsagePointBuilder withServicePriority(String servicePriority);

    UsagePoint build();

    String getAliasName();

    String getDescription();

    String getmRID();

    String getName();

    boolean isSdp();

    boolean isVirtual();

    String getOutageRegion();

    String getReadCycle();

    String getReadRoute();

    String getServicePriority();

    ServiceCategory getServiceCategory();

}