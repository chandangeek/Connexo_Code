package com.elster.jupiter.metering.config;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

@ProviderType
public interface MetrologyConfiguration extends HasId, HasName {

    ServiceCategory getServiceCategory();

    String getDescription();

    MetrologyConfigurationStatus getStatus();

    void activate();

    boolean isActive();

    List<RegisteredCustomPropertySet> getCustomPropertySets();

    void addCustomPropertySet(RegisteredCustomPropertySet registeredCustomPropertySet);

    void removeCustomPropertySet(RegisteredCustomPropertySet registeredCustomPropertySet);

    void delete();

    long getVersion();

}