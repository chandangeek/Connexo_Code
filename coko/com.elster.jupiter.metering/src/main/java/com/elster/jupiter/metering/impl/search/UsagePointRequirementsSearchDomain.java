/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.metering.MeteringTranslationService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.metering.impl.config.ServerMetrologyConfigurationService;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;

import javax.inject.Inject;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class UsagePointRequirementsSearchDomain extends UsagePointSearchDomain implements SearchDomain {
    @Inject
    public UsagePointRequirementsSearchDomain(PropertySpecService propertySpecService, ServerMeteringService meteringService, MeteringTranslationService meteringTranslationService, ServerMetrologyConfigurationService metrologyConfigurationService, Clock clock, LicenseService licenseService) {
        super();
        setPropertySpecService(propertySpecService);
        setServerMetrologyConfigurationService(metrologyConfigurationService);
        setMeteringService(meteringService);
        setClock(clock);
        setLicenseService(licenseService);
        setMeteringTranslationService(meteringTranslationService);
    }

    @Override
    public List<String> targetApplications() {
        return Collections.singletonList("NONE");
    }

    @Override
    public String getId() {
        return UsagePoint.class.getName() + "-Requirements";
    }

    @Override
    public String displayName() {
        return getMetrologyConfigurationService().getThesaurus().getFormat(PropertyTranslationKeys.USAGE_POINT_REQUIREMENT_SEARCH_DOMAIN).format();
    }

    @Override
    public List<SearchableProperty> getProperties() {
        return new ArrayList<>(Arrays.asList(
                new ServiceCategorySearchableProperty(this, getPropertySpecService(), getMeteringTranslationService(), getMetrologyConfigurationService().getThesaurus()),
                new TypeSearchableProperty(this, getPropertySpecService(), getMetrologyConfigurationService().getThesaurus())));
    }

    @Override
    protected List<SearchableProperty> getServiceCategoryDynamicProperties(Collection<SearchablePropertyConstriction> constrictions) {
        return super.getServiceCategoryDynamicProperties(constrictions)
                .stream()
                .filter(property -> !property.getName().startsWith(LoadLimiterTypeSearchableProperty.FIELD_NAME))
                .collect(Collectors.toList());
    }

    @Override
    protected boolean isMultisensePropertiesOnly() {
        return false;
    }
}
