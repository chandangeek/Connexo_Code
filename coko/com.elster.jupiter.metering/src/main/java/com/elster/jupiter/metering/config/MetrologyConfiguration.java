package com.elster.jupiter.metering.config;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.List;

@ProviderType
public interface MetrologyConfiguration extends HasId, HasName {

    void updateName(String name);

    long getVersion();

    Instant getCreateTime();

    Instant getModTime();

    String getUserName();

    ServiceCategory getServiceCategory();

    String getDescription();

    MetrologyConfigurationStatus getStatus();

    boolean isActive();

    void activate();

    void deactivate();

    List<RegisteredCustomPropertySet> getCustomPropertySets();

    void addCustomPropertySet(RegisteredCustomPropertySet registeredCustomPropertySet);

    void removeCustomPropertySet(RegisteredCustomPropertySet registeredCustomPropertySet);

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

    void delete();

    @ProviderType
    interface MetrologyConfigurationReadingTypeRequirementBuilder {

        FullySpecifiedReadingType withReadingType(ReadingType readingType);

        PartiallySpecifiedReadingType withReadingTypeTemplate(ReadingTypeTemplate readingTypeTemplate);
    }
}