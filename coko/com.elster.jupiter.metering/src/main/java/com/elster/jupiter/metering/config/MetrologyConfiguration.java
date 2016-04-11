package com.elster.jupiter.metering.config;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.List;

@ProviderType
public interface MetrologyConfiguration extends HasId, HasName {

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