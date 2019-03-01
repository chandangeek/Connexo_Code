package com.energyict.mdc.device.data;

import com.google.common.collect.Multimap;
import java.math.BigDecimal;

public interface DeviceBuilder{

    DeviceBuilder withBatch(String batch);

    DeviceBuilder withSerialNumber(String serialNumber);

    DeviceBuilder withManufacturer(String manufacturer);

    DeviceBuilder withModelNumber(String modelNumber);

    DeviceBuilder withModelVersion(String modelVersion);

    DeviceBuilder withMultiplier(BigDecimal multiplier);

    DeviceBuilder withYearOfCertification(Integer yearOfCertification);

    DeviceBuilder withZones(Multimap<String, String> zonesIds);

    Device create();
}
