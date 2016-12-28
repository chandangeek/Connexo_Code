package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cps.rest.CustomPropertySetInfoFactory;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;
import com.elster.jupiter.usagepoint.lifecycle.rest.UsagePointLifeCycleTransitionInfoFactory;
import com.elster.jupiter.usagepoint.lifecycle.rest.UsagePointTransitionInfo;
import com.elster.jupiter.usagepoint.lifecycle.rest.UsagePointTransitionInfoFactory;

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
import java.util.stream.Collectors;

@Path("/field")
public class FieldResource {

    private final CustomPropertySetInfoFactory customPropertySetInfoFactory;
    private final TransactionService transactionService;
    private final UsagePointInfoFactory usagePointInfoFactory;
    private final Clock clock;
    private final ResourceHelper resourceHelper;
    private final UsagePointTransitionInfoFactory usagePointTransitionInfoFactory;


    @Inject
    public FieldResource(CustomPropertySetInfoFactory customPropertySetInfoFactory, TransactionService transactionService, UsagePointInfoFactory usagePointInfoFactory, Clock clock, ResourceHelper resourceHelper, UsagePointLifeCycleTransitionInfoFactory usagePointLifeCycleTransitionInfoFactory, UsagePointTransitionInfoFactory usagePointTransitionInfoFactory) {
        this.customPropertySetInfoFactory = customPropertySetInfoFactory;
        this.transactionService = transactionService;
        this.usagePointInfoFactory = usagePointInfoFactory;
        this.clock = clock;
        this.resourceHelper = resourceHelper;
        this.usagePointTransitionInfoFactory = usagePointTransitionInfoFactory;
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
        usagePoint = getUsagePoint(info);
        metrologyConfigurations = resourceHelper.getAvailableMetrologyConfigurations(usagePoint, customPropertySetInfoFactory);
        transaction.close();

        return metrologyConfigurations;
    }

    @POST
    @Path("/transitions")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public List<UsagePointTransitionInfo> getAvailableTransitions(UsagePointInfo usagePointInfo) {
        UsagePoint usagePoint;
        List<UsagePointTransitionInfo> availableTransitions;

        TransactionContext transaction = transactionService.getContext();
        usagePoint = getUsagePoint(usagePointInfo);
        List<UsagePointTransition> transitions = resourceHelper.getAvailableTransitions(usagePoint);
        availableTransitions = transitions.stream()
                .map(usagePointTransitionInfoFactory::from)
                .collect(Collectors.toList());
        transaction.close();

        return availableTransitions;
    }

    private UsagePoint getUsagePoint(UsagePointInfo info) {
        UsagePoint usagePoint;
        usagePoint = usagePointInfoFactory.newUsagePointBuilder(info).create();
        info.techInfo.getUsagePointDetailBuilder(usagePoint, clock).create();

        return usagePoint;
    }
}
