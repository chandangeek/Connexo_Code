package com.elster.jupiter.metering;

import com.elster.jupiter.util.geo.SpatialCoordinates;

public interface UsagePointBuilder {

    UsagePointBuilder withAliasName(String aliasName);

    UsagePointBuilder withDescription(String description);

    UsagePointBuilder withLocation(Location location);

    UsagePointBuilder withGeoCoordinates(SpatialCoordinates geoCoordinates);

    // TODO: update import & remove
    @Deprecated
    UsagePointBuilder withName(String name);

    UsagePointBuilder withIsSdp(boolean isSdp);

    UsagePointBuilder withIsVirtual(boolean isVirtual);

    UsagePointBuilder withOutageRegion(String outageRegion);

    UsagePointBuilder withReadRoute(String readRoute);

    UsagePointBuilder withServicePriority(String servicePriority);

    UsagePointBuilder withServiceDeliveryRemark(String serviceDeliveryRemark);

    UsagePointBuilder withServiceLocation(ServiceLocation location);

    UsagePointBuilder withServiceLocationString(String serviceLocationString);

    UsagePoint create();

    UsagePoint validate();

    LocationBuilder newLocationBuilder();
}
