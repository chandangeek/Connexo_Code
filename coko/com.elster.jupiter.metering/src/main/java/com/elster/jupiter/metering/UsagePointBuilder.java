package com.elster.jupiter.metering;

import java.time.Instant;

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

    UsagePointBuilder setServiceLocation(ServiceLocation location);

    UsagePointBuilder withInstallationTime(Instant installationTime);

    UsagePoint create();
}