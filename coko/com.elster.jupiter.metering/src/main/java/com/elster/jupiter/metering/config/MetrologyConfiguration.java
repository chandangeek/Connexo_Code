package com.elster.jupiter.metering.config;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.List;

@ProviderType
public interface MetrologyConfiguration {
    long getId();

    String getName();

    void updateName(String name);

    void delete();

    long getVersion();

    Instant getCreateTime();

    Instant getModTime();

    String getUserName();

    boolean isActive();

    void activate();

    void deactivate();

    List<RegisteredCustomPropertySet> getCustomPropertySets();

    void addCustomPropertySet(RegisteredCustomPropertySet registeredCustomPropertySet);

    void removeCustomPropertySet(RegisteredCustomPropertySet registeredCustomPropertySet);

}