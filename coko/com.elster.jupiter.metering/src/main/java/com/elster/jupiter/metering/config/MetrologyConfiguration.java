/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.config;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@ProviderType
public interface MetrologyConfiguration extends HasId, HasName {

    MetrologyConfigurationUpdater startUpdate();

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

    void deprecate();

    List<RegisteredCustomPropertySet> getCustomPropertySets();

    void addCustomPropertySet(RegisteredCustomPropertySet registeredCustomPropertySet);

    void removeCustomPropertySet(RegisteredCustomPropertySet registeredCustomPropertySet);

    List<MetrologyContract> getContracts();

    MetrologyContract addMandatoryMetrologyContract(MetrologyPurpose metrologyPurpose);

    MetrologyContract addMetrologyContract(MetrologyPurpose metrologyPurpose);

    void removeMetrologyContract(MetrologyContract metrologyContract);

    List<ReadingTypeRequirement> getRequirements();

    MetrologyConfigurationReadingTypeRequirementBuilder newReadingTypeRequirement(String name);

    void removeReadingTypeRequirement(ReadingTypeRequirement readingTypeRequirement);

    ReadingTypeDeliverableBuilder newReadingTypeDeliverable(String name, ReadingType readingType, Formula.Mode mode);

    ReadingTypeDeliverableBuilder newReadingTypeDeliverable(String name, DeliverableType type, ReadingType readingType, Formula.Mode mode);

    void removeReadingTypeDeliverable(ReadingTypeDeliverable deliverable);

    List<ReadingTypeDeliverable> getDeliverables();

    void delete();

    List<ReadingTypeRequirement> getMandatoryReadingTypeRequirements();

    void makeObsolete();

    Optional<Instant> getObsoleteTime();

    @ProviderType
    interface MetrologyConfigurationReadingTypeRequirementBuilder {

        FullySpecifiedReadingTypeRequirement withReadingType(ReadingType readingType);

        PartiallySpecifiedReadingTypeRequirement withReadingTypeTemplate(ReadingTypeTemplate readingTypeTemplate);
    }
}