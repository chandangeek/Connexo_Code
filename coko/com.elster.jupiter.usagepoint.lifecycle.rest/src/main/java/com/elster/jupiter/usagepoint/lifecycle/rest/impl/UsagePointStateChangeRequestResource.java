package com.elster.jupiter.usagepoint.lifecycle.rest.impl;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.RestValidationBuilder;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.usagepoint.lifecycle.RequiredMicroActionPropertiesException;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointStateChangeRequest;
import com.elster.jupiter.usagepoint.lifecycle.config.MicroAction;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointMicroActionFactory;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/usagepoint/{uname}/transitions")
public class UsagePointStateChangeRequestResource {

    private final ResourceHelper resourceHelper;
    private final UsagePointLifeCycleService usagePointLifeCycleService;
    private final UsagePointTransitionInfoFactory usagePointTransitionInfoFactory;
    private final PropertyValueInfoService propertyValueInfoService;
    private final UsagePointStateChangeRequestInfoFactory changeRequestInfoFactory;
    private final UsagePointMicroActionFactory usagePointMicroActionFactory;
    private final Thesaurus thesaurus;

    @Inject
    public UsagePointStateChangeRequestResource(UsagePointLifeCycleService usagePointLifeCycleService,
                                                ResourceHelper resourceHelper,
                                                UsagePointTransitionInfoFactory usagePointTransitionInfoFactory,
                                                PropertyValueInfoService propertyValueInfoService,
                                                UsagePointStateChangeRequestInfoFactory changeRequestInfoFactory,
                                                UsagePointMicroActionFactory usagePointMicroActionFactory,
                                                Thesaurus thesaurus) {
        this.usagePointLifeCycleService = usagePointLifeCycleService;
        this.resourceHelper = resourceHelper;
        this.usagePointTransitionInfoFactory = usagePointTransitionInfoFactory;
        this.propertyValueInfoService = propertyValueInfoService;
        this.changeRequestInfoFactory = changeRequestInfoFactory;
        this.usagePointMicroActionFactory = usagePointMicroActionFactory;
        this.thesaurus = thesaurus;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response getAvailableTransitions(@PathParam("uname") String usagePointName,
                                            @HeaderParam("X-CONNEXO-APPLICATION-NAME") String application,
                                            @BeanParam JsonQueryParameters queryParameters) {
        List<IdWithNameInfo> transitions = this.usagePointLifeCycleService
                .getAvailableTransitions(this.resourceHelper.getUsagePointOrThrowException(usagePointName).getState(), application)
                .stream()
                .map(IdWithNameInfo::new)
                .sorted((a1, a2) -> a1.name.compareToIgnoreCase(a2.name))
                .collect(Collectors.toList());
        return Response.ok(PagedInfoList.fromCompleteList("transitions", transitions, queryParameters)).build();
    }

    @GET
    @Path("/{tid}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public UsagePointTransitionInfo getPropertiesForTransition(@PathParam("tid") long transitionId) {
        return this.usagePointTransitionInfoFactory.from(this.resourceHelper.getTransitionByIdOrThrowException(transitionId));
    }

    @PUT
    @Path("/{tid}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    public Response performTransition(@PathParam("uname") String usagePointName,
                                      @PathParam("tid") long transitionId,
                                      @HeaderParam("X-CONNEXO-APPLICATION-NAME") String application,
                                      UsagePointTransitionInfo info) {
        UsagePoint usagePoint = this.resourceHelper.lockUsagePoint(info.usagePoint);
        UsagePointTransition transition = this.resourceHelper.getTransitionByIdOrThrowException(transitionId);
        UsagePointStateChangeRequest changeRequest;
        try {
            if (info.properties != null) {
                this.valueAvailableForAllRequiredProperties(transition, info.properties);
            }
            Map<String, Object> propertiesMap = DecoratedStream.decorate(transition.getActions().stream())
                    .flatMap(microAction -> microAction.getPropertySpecs().stream())
                    .distinct(PropertySpec::getName)
                    .collect(Collectors.toMap(PropertySpec::getName, propertySpec -> this.propertyValueInfoService.findPropertyValue(propertySpec, info.properties)));
            if (info.transitionNow) {
                changeRequest = this.usagePointLifeCycleService.performTransition(usagePoint, transition, application, propertiesMap);
            } else {
                changeRequest = this.usagePointLifeCycleService.scheduleTransition(usagePoint, transition, info.effectiveTimestamp, application, propertiesMap);
            }
            return Response.ok(this.changeRequestInfoFactory.from(changeRequest, application)).build();
        } catch (RequiredMicroActionPropertiesException violationEx) {
            wrapWithFormValidationErrorAndRethrow(violationEx);
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    private void valueAvailableForAllRequiredProperties(UsagePointTransition transition, List<PropertyInfo> propertyInfos) {
        Set<String> propertySpecsWithMissedValues = new HashSet<>();
        for (PropertyInfo propertyInfo : propertyInfos) {
            if (!hasValue(propertyInfo)) {
                propertySpecsWithMissedValues.add(propertyInfo.key);
            }
        }
        Set<String> missingRequiredPropertySpecNames = transition
                .getActions()
                .stream()
                .flatMap(ma -> this.getPropertySpecsFor(ma).stream())
                .filter(PropertySpec::isRequired)
                .map(PropertySpec::getName)
                .filter(propertySpecsWithMissedValues::contains)
                .collect(Collectors.toSet());
        if (!missingRequiredPropertySpecNames.isEmpty()) {
            throw new RequiredMicroActionPropertiesException(this.thesaurus, MessageSeeds.MISSING_REQUIRED_PROPERTY_VALUES, missingRequiredPropertySpecNames);
        }
    }

    private List<PropertySpec> getPropertySpecsFor(MicroAction action) {
        return this.usagePointMicroActionFactory.from(action.getKey()).get().getPropertySpecs();
    }

    private void wrapWithFormValidationErrorAndRethrow(RequiredMicroActionPropertiesException violationEx) {
        RestValidationBuilder formValidationErrorBuilder = new RestValidationBuilder();
        violationEx.getViolatedPropertySpecNames()
                .forEach(propertyName ->
                        formValidationErrorBuilder.addValidationError(
                                new LocalizedFieldValidationException(MessageSeeds.FIELD_CAN_NOT_BE_EMPTY, propertyName)));
        formValidationErrorBuilder.validate();
    }

    private boolean hasValue(PropertyInfo propertyInfo) {
        PropertyValueInfo<?> propertyValueInfo = propertyInfo.getPropertyValueInfo();
        return propertyValueInfo != null && propertyValueInfo.getValue() != null && !"".equals(propertyValueInfo.getValue());
    }

    @GET
    @Path("/history")
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response getChangeRequestHistory(@PathParam("uname") String usagePointName,
                                            @BeanParam JsonQueryParameters queryParameters,
                                            @HeaderParam("X-CONNEXO-APPLICATION-NAME") String application) {
        UsagePoint usagePoint = this.resourceHelper.getUsagePointOrThrowException(usagePointName);
        List<UsagePointStateChangeRequestInfo> history = this.usagePointLifeCycleService.getHistory(usagePoint)
                .stream()
                .map(changeRequest -> this.changeRequestInfoFactory.from(changeRequest, application))
                .collect(Collectors.toList());
        return Response.ok(PagedInfoList.fromCompleteList("history", history, queryParameters)).build();
    }

    @PUT
    @Path("/history/{hid}")
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response cancelScheduledChangeRequest(@PathParam("uname") String usagePointName,
                                                 @PathParam("hid") long historyId) {
        UsagePoint usagePoint = this.resourceHelper.getUsagePointOrThrowException(usagePointName);
        this.usagePointLifeCycleService.getHistory(usagePoint)
                .stream()
                .filter(record -> record.getId() == historyId)
                .findFirst()
                .ifPresent(UsagePointStateChangeRequest::cancel);
        return Response.ok().build();
    }
}
