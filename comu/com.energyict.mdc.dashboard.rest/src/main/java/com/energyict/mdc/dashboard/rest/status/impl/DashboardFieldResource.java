package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.energyict.mdc.common.rest.FieldResource;
import com.energyict.mdc.dashboard.rest.DashboardApplication;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * Why the wrapped return value? JavaScript people didn't want to see a naked JSON list, had to be
 * wrapped with meaningful field name.
 */

@Path("/field")
public class DashboardFieldResource extends FieldResource {


    private final ProtocolPluggableService protocolPluggableService;

    @Inject
    public DashboardFieldResource(NlsService nlsService, ProtocolPluggableService protocolPluggableService) {
        super(nlsService.getThesaurus(DashboardApplication.COMPONENT_NAME, Layer.REST));
        this.protocolPluggableService = protocolPluggableService;
    }

    @GET
    @Path("/breakdown")
    @Produces("application/json")
    public Object getBreakdownValues() {
        return asJsonArrayObjectWithTranslation("breakdowns", "breakdown", new BreakdownOptionAdapter().getClientSideValues());
    }

    @GET
    @Path("/taskstatus")
    @Produces("application/json")
    public Object getTaskStatusValues() {
        return asJsonArrayObjectWithTranslation("taskStatuses", "taskStatus", new TaskStatusAdapter().getClientSideValues());
    }

    @GET
    @Path("/successindicator")
    @Produces("application/json")
    public Object getLatestResultValues() {
        return asJsonArrayObjectWithTranslation("successIndicators", "successIndicator", new SuccessIndicatorAdapter().getClientSideValues());
    }

    @GET
    @Path("/lifecyclestatus")
    @Produces("application/json")
    public Object getLifecycleStatus() {
        return asJsonArrayObjectWithTranslation("lifecycleStatuses", "lifecycleStatus", new ConnectionTaskLifecycleStatusAdaptor().getClientSideValues());
    }

    @GET
    @Path("/completioncodes")
    @Produces("application/json")
    public Object getCompletionCodes() {
        return asJsonArrayObjectWithTranslation("completionCodes", "completionCode", new CompletionCodeAdapter().getClientSideValues());    }

}
