package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cps.rest.CustomPropertySetInfoFactory;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.time.Clock;
import java.util.List;

@Path("/field")
public class FieldResource {

    private final CustomPropertySetInfoFactory customPropertySetInfoFactory;
    private final TransactionService transactionService;
    private final UsagePointInfoFactory usagePointInfoFactory;
    private final Clock clock;
    private final ResourceHelper resourceHelper;


    @Inject
    public FieldResource(CustomPropertySetInfoFactory customPropertySetInfoFactory, TransactionService transactionService, UsagePointInfoFactory usagePointInfoFactory, Clock clock, ResourceHelper resourceHelper) {
        this.customPropertySetInfoFactory = customPropertySetInfoFactory;
        this.transactionService = transactionService;
        this.usagePointInfoFactory = usagePointInfoFactory;
        this.clock = clock;
        this.resourceHelper = resourceHelper;
    }

    @POST
    @Path("/metrologyconfigurations")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.VIEW_METROLOGY_CONFIGURATION})
    public List<MetrologyConfigurationInfo> getAvailableMetrologyConfigurations(@BeanParam JsonQueryParameters queryParameters, UsagePointInfo info) {
        UsagePoint usagePoint;
        List<MetrologyConfigurationInfo> metrologyConfigurations;

        TransactionContext transaction = transactionService.getContext();
        usagePoint = usagePointInfoFactory.newUsagePointBuilder(info).create();
        info.techInfo.getUsagePointDetailBuilder(usagePoint, clock).create();
        metrologyConfigurations = resourceHelper.getAvailableMetrologyConfigurations(usagePoint, customPropertySetInfoFactory);
        transaction.close();

        return metrologyConfigurations;
    }

}
