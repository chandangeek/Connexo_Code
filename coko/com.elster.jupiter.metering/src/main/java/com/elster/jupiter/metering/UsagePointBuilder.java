package com.elster.jupiter.metering;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.util.geo.SpatialCoordinates;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface UsagePointBuilder {

    UsagePointBuilder withAliasName(String aliasName);

    UsagePointBuilder withDescription(String description);

    UsagePointBuilder withLocation(Location location);

    UsagePointBuilder withGeoCoordinates(SpatialCoordinates geoCoordinates);

    UsagePointBuilder withIsSdp(boolean isSdp);

    UsagePointBuilder withIsVirtual(boolean isVirtual);

    UsagePointBuilder withOutageRegion(String outageRegion);

    UsagePointBuilder withReadRoute(String readRoute);

    UsagePointBuilder withServicePriority(String servicePriority);

    UsagePointBuilder withServiceDeliveryRemark(String serviceDeliveryRemark);

    UsagePointBuilder withServiceLocation(ServiceLocation location);

    UsagePointBuilder withServiceLocationString(String serviceLocationString);

    UsagePointBuilder addCustomPropertySetValues(RegisteredCustomPropertySet propertySet, CustomPropertySetValues values);

    UsagePoint create();

    UsagePoint validate();

    LocationBuilder newLocationBuilder();
}
