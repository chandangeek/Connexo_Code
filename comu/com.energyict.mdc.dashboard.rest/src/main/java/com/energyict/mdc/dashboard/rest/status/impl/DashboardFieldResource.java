package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.energyict.mdc.common.rest.FieldResource;
import com.energyict.mdc.dashboard.rest.DashboardApplication;
import java.util.EnumSet;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * Why the wrapped return value? JavaScript people didn't want to see a naked JSON list, had to be
 * wrapped with meaningful field name.
 */

@Path("/field")
public class DashboardFieldResource extends FieldResource {

    @Inject
    public DashboardFieldResource(NlsService nlsService) {
        super(nlsService.getThesaurus(DashboardApplication.COMPONENT_NAME, Layer.REST));
    }

    @GET
    @Path("/breakdown")
    public Object getBreakdownValues() {
        return asJsonArrayObjectWithTranslation("breakdowns", "breakdown", EnumSet.of(FilterOption.comPortPool, FilterOption.connectionType, FilterOption.deviceType));
    }

}
