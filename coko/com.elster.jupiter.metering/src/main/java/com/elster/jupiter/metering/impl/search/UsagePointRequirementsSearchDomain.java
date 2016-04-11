package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.metering.impl.config.ServerMetrologyConfigurationService;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchDomain;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

public class UsagePointRequirementsSearchDomain extends UsagePointSearchDomain implements SearchDomain {

    private volatile ServerMeteringService meteringService;

    @SuppressWarnings("unused") // OSGI
    public UsagePointRequirementsSearchDomain() {
    }

    @Inject
    public UsagePointRequirementsSearchDomain(PropertySpecService propertySpecService, ServerMeteringService meteringService, ServerMetrologyConfigurationService metrologyConfigurationService) {
        super(propertySpecService, meteringService, metrologyConfigurationService);
        this.meteringService = meteringService;
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
        return this.meteringService.getThesaurus().getFormat(PropertyTranslationKeys.USAGE_POINT_REQUIREMENT_SEARCH_DOMAIN).format();
    }
}
