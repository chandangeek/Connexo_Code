package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.search.SearchService;

import javax.inject.Inject;
import javax.ws.rs.Path;

@Path("/usagepointgroups")
public class UsagePointGroupResource {
    private final MeteringGroupsService meteringGroupsService;
    private final MeteringService meteringService;
    private final SearchService searchService;
    private final ExceptionFactory exceptionFactory;
    private final UsagePointGroupInfoFactory usagePointGroupInfoFactory;
    private final ResourceHelper resourceHelper;

    @Inject
    public UsagePointGroupResource(MeteringGroupsService meteringGroupsService, MeteringService meteringService,
                                   SearchService searchService, ExceptionFactory exceptionFactory,
                                   UsagePointGroupInfoFactory usagePointGroupInfoFactory, ResourceHelper resourceHelper) {
        this.meteringGroupsService = meteringGroupsService;
        this.meteringService = meteringService;
        this.searchService = searchService;
        this.exceptionFactory = exceptionFactory;
        this.usagePointGroupInfoFactory = usagePointGroupInfoFactory;
        this.resourceHelper = resourceHelper;
    }

}
