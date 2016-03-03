package com.elster.jupiter.metering.config;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface MeterRole {

    String getKey();

    String getDisplayName();

}
