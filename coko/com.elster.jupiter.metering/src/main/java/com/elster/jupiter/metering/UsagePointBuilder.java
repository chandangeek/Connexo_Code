package com.elster.jupiter.metering;

import java.time.Instant;

public interface UsagePointBuilder {

    UsagePointBuilder withAliasName(String aliasName);

    UsagePointBuilder withDescription(String description);

    UsagePointBuilder withMRID(String mRID);

    UsagePointBuilder withName(String name);

    UsagePointBuilder withIsSdp(boolean isSdp);

    UsagePointBuilder withIsVirtual(boolean isVirtual);

    UsagePointBuilder withOutageRegion(String outageRegion);

    UsagePointBuilder withReadRoute(String readRoute);

    UsagePointBuilder withServicePriority(String servicePriority);

    UsagePointBuilder withServiceDeliveryRemark(String serviceDeliveryRemark);

    UsagePointBuilder withServiceLocation(ServiceLocation location);

    UsagePointBuilder withServiceLocationString(String serviceLocationString);

    /**
     * For backwards compatibility only with API that should never have
     * been there in the first place and will be removed as soon
     * as {@link ServiceCategory#newUsagePoint(String)} has been removed.
     *
     * @param installationTime The time on which the UsagePoint was installed
     * @return The builder to support method chaining
     * @deprecated
     */
    @Deprecated
    UsagePointBuilder withInstallationTime(Instant installationTime);

    UsagePoint create();

    UsagePoint validate();
}