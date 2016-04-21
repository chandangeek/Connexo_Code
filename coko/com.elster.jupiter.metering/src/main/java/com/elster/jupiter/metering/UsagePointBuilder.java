package com.elster.jupiter.metering;

public interface UsagePointBuilder {

    UsagePointBuilder withAliasName(String aliasName);

    UsagePointBuilder withDescription(String description);

    UsagePointBuilder withMRID(String mRID);

    UsagePointBuilder withLocation(Location location);

    UsagePointBuilder withGeoCoordinates(GeoCoordinates geoCoordinates);

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
}