package com.elster.jupiter.metering.config;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.ReadingType;
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

    List<MetrologyContract> getContracts();

    MetrologyContract addMandatoryMetrologyContract(MetrologyPurpose metrologyPurpose);

    MetrologyContract addMetrologyContract(MetrologyPurpose metrologyPurpose);

    void removeMetrologyContract(MetrologyContract metrologyContract);

    List<ReadingTypeRequirement> getRequirements();

    MetrologyConfigurationReadingTypeRequirementBuilder addReadingTypeRequirement(String name);

    void removeReadingTypeRequirement(ReadingTypeRequirement readingTypeRequirement);

    ReadingTypeDeliverable addReadingTypeDeliverable(String name, ReadingType readingType, Formula formula);

    void removeReadingTypeDeliverable(ReadingTypeDeliverable deliverable);

    List<ReadingTypeDeliverable> getDeliverables();

    @ProviderType
    interface MetrologyConfigurationReadingTypeRequirementBuilder {

        MetrologyConfigurationReadingTypeRequirementBuilder withName(String name);

        FullySpecifiedReadingType withReadingType(ReadingType readingType);

        PartiallySpecifiedReadingType withReadingTypeTemplate(ReadingTypeTemplate readingTypeTemplate);
    }
}