package com.elster.jupiter.metering;

import aQute.bnd.annotation.ProviderType;

import java.util.Optional;

@ProviderType
public interface EndDeviceAttributesProvider {

    Optional<String> getSerialNumber(EndDevice endDevice);

    Optional<String> getType(EndDevice endDevice);
}
