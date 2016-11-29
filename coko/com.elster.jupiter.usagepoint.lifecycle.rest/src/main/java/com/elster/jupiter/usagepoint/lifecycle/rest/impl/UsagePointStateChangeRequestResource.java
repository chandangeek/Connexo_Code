package com.elster.jupiter.usagepoint.lifecycle.rest.impl;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointStateChangeRequest;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;
import com.elster.jupiter.util.streams.DecoratedStream;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/usagepoint/{id}/transitions")
public class UsagePointStateChangeRequestResource {

    private final ResourceHelper resourceHelper;
    private final UsagePointLifeCycleService usagePointLifeCycleService;
    private final UsagePointTransitionInfoFactory usagePointTransitionInfoFactory;
    private final PropertyValueInfoService propertyValueInfoService;
    private final UsagePointStateChangeRequestInfoFactory changeRequestInfoFactory;

    @Inject
    public UsagePointStateChangeRequestResource(UsagePointLifeCycleService usagePointLifeCycleService,
                                                ResourceHelper resourceHelper,
                                                UsagePointTransitionInfoFactory usagePointTransitionInfoFactory,
                                                PropertyValueInfoService propertyValueInfoService,
                                                UsagePointStateChangeRequestInfoFactory changeRequestInfoFactory) {
        this.usagePointLifeCycleService = usagePointLifeCycleService;
        this.resourceHelper = resourceHelper;
        this.usagePointTransitionInfoFactory = usagePointTransitionInfoFactory;
        this.propertyValueInfoService = propertyValueInfoService;
        this.changeRequestInfoFactory = changeRequestInfoFactory;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response getAvailableActionsForCurrentDevice(@PathParam("id") long id,
                                                        @HeaderParam("X-CONNEXO-APPLICATION-NAME") String application,
                                                        @BeanParam JsonQueryParameters queryParameters) {
        List<IdWithNameInfo> transitions = this.usagePointLifeCycleService
                .getAvailableTransitions(this.resourceHelper.getUsagePointOrThrowException(id).getState(), application)
                .stream()
                .map(IdWithNameInfo::new)
                .sorted((a1, a2) -> a1.name.compareToIgnoreCase(a2.name))
                .collect(Collectors.toList());
        return Response.ok(PagedInfoList.fromCompleteList("transitions", transitions, queryParameters)).build();
    }

    @GET
    @Transactional
    @Path("/{tid}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public UsagePointTransitionInfo getPropertiesForTransition(@PathParam("id") long usagePointId,
                                                               @PathParam("tid") long transitionId,
                                                               @BeanParam JsonQueryParameters queryParameters) {
        return this.usagePointTransitionInfoFactory.from(this.resourceHelper.getTransitionByIdOrThrowException(transitionId));
    }

    @PUT
    @Path("/{tid}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    public Response performTransition(@PathParam("id") long usagePointId,
                                      @PathParam("tid") long transitionId,
                                      @HeaderParam("X-CONNEXO-APPLICATION-NAME") String application,
                                      @BeanParam JsonQueryParameters queryParameters,
                                      UsagePointTransitionInfo info) {
        UsagePoint usagePoint = this.resourceHelper.lockUsagePoint(info.usagePoint);
        UsagePointTransition transition = this.resourceHelper.getTransitionByIdOrThrowException(transitionId);
        Map<String, Object> propertiesMap = DecoratedStream.decorate(transition.getActions().stream())
                .flatMap(microAction -> microAction.getPropertySpecs().stream())
                .distinct(PropertySpec::getName)
                .collect(Collectors.toMap(PropertySpec::getName, propertySpec -> this.propertyValueInfoService.findPropertyValue(propertySpec, info.properties)));
        UsagePointStateChangeRequest changeRequest;
        if (info.transitionNow) {
            changeRequest = this.usagePointLifeCycleService.performTransition(usagePoint, transition, application, propertiesMap);
        } else {
            changeRequest = this.usagePointLifeCycleService.scheduleTransition(usagePoint, transition, info.effectiveTimestamp, application, propertiesMap);
        }
        return Response.ok(this.changeRequestInfoFactory.from(changeRequest)).build();
    }

    @GET
    @Path("/history")
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response getChangeRequestHistory(@PathParam("id") long usagePointId,
                                            @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = this.resourceHelper.getUsagePointOrThrowException(usagePointId);
        List<UsagePointStateChangeRequestInfo> history = this.usagePointLifeCycleService.getHistory(usagePoint)
                .stream()
                .map(this.changeRequestInfoFactory::from)
                .collect(Collectors.toList());
        return Response.ok(PagedInfoList.fromCompleteList("history", history, queryParameters)).build();
    }
}
