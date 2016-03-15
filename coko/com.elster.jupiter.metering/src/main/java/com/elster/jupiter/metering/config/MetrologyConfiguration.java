package com.elster.jupiter.metering.config;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ReadingTypeTemplate;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

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

    List<MetrologyContract> getContracts();

    MetrologyContract addMandatoryMetrologyContract(MetrologyPurpose metrologyPurpose);

    MetrologyContract addMetrologyContract(MetrologyPurpose metrologyPurpose);

    void removeMetrologyContract(MetrologyContract metrologyContract);

    List<ReadingTypeRequirement> getRequirements();

    FullySpecifiedReadingType addFullySpecifiedReadingTypeRequirement(String name, ReadingType readingType);

    PartiallySpecifiedReadingType addPartiallySpecifiedReadingTypeRequirement(String name, ReadingTypeTemplate readingTypeTemplate);

    void removeReadingTypeRequirement(ReadingTypeRequirement readingTypeRequirement);

    List<ReadingTypeDeliverable> getDeliverables();

}