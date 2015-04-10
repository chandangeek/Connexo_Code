package com.energyict.mdc.device.lifecycle.config.rest.impl.resource;

import com.elster.jupiter.rest.util.RestValidationBuilder;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.Privileges;
import com.energyict.mdc.device.lifecycle.config.rest.impl.i18n.MessageSeeds;
import com.energyict.mdc.device.lifecycle.config.rest.impl.resource.requests.AuthorizedActionChangeRequest;
import com.energyict.mdc.device.lifecycle.config.rest.impl.resource.requests.AuthorizedActionRequestFactory;
import com.energyict.mdc.device.lifecycle.config.rest.info.AuthorizedActionInfo;
import com.energyict.mdc.device.lifecycle.config.rest.info.AuthorizedActionInfoFactory;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DeviceLifeCycleActionResource {
    private final DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private final ResourceHelper resourceHelper;
    private final AuthorizedActionInfoFactory authorizedActionInfoFactory;

    @Inject
    public DeviceLifeCycleActionResource(
            DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService,
            ResourceHelper resourceHelper,
            AuthorizedActionInfoFactory authorizedActionInfoFactory) {
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
        this.resourceHelper = resourceHelper;
        this.authorizedActionInfoFactory = authorizedActionInfoFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE_LIFE_CYCLES})
    public PagedInfoList getActionsForDeviceLifecycle(@PathParam("deviceLifeCycleId") Long deviceLifeCycleId, @BeanParam QueryParameters queryParams) {
        DeviceLifeCycle deviceLifeCycle = resourceHelper.findDeviceLifeCycleByIdOrThrowException(deviceLifeCycleId);
        List<AuthorizedActionInfo> transitions = deviceLifeCycle.getAuthorizedActions()
                .stream()
                .map(action -> authorizedActionInfoFactory.from(action))
                .sorted((t1, t2) -> t1.name.compareToIgnoreCase(t2.name)) // alphabetical sort
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("deviceLifeCycleActions", ListPager.of(transitions).from(queryParams).find(), queryParams);
    }

    @GET
    @Path("/{actionId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE_LIFE_CYCLES})
    public Response getAuthorizedActionById(@PathParam("deviceLifeCycleId") Long deviceLifeCycleId, @PathParam("actionId") Long actionId, @BeanParam QueryParameters queryParams) {
        DeviceLifeCycle deviceLifeCycle = resourceHelper.findDeviceLifeCycleByIdOrThrowException(deviceLifeCycleId);
        AuthorizedAction action = resourceHelper.findAuthorizedActionByIdOrThrowException(deviceLifeCycle, actionId);
        return Response.ok(authorizedActionInfoFactory.from(action)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.CONFIGURE_DEVICE_LIFE_CYCLES})
    public Response addActionsForDeviceLifecycle(@PathParam("deviceLifeCycleId") Long deviceLifeCycleId, AuthorizedActionInfo newAction) {
        validateInfo(newAction);
        DeviceLifeCycle deviceLifeCycle = resourceHelper.findDeviceLifeCycleByIdOrThrowException(deviceLifeCycleId);
        AuthorizedActionRequestFactory factory = new AuthorizedActionRequestFactory(this.resourceHelper);
        AuthorizedActionChangeRequest creationRequest = factory.from(deviceLifeCycle, newAction, AuthorizedActionRequestFactory.Operation.CREATE);
        AuthorizedAction authorizedAction = creationRequest.perform();
        return Response.ok(authorizedActionInfoFactory.from(authorizedAction)).build();
    }

    @PUT
    @Path("/{actionId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.CONFIGURE_DEVICE_LIFE_CYCLES})
    public Response editAuthorizedAction(@PathParam("deviceLifeCycleId") Long deviceLifeCycleId, AuthorizedActionInfo actionForEdit) {
        validateInfo(actionForEdit);
        DeviceLifeCycle deviceLifeCycle = resourceHelper.findDeviceLifeCycleByIdOrThrowException(deviceLifeCycleId);
        AuthorizedActionRequestFactory factory = new AuthorizedActionRequestFactory(this.resourceHelper);
        AuthorizedActionChangeRequest editRequest = factory.from(deviceLifeCycle, actionForEdit, AuthorizedActionRequestFactory.Operation.MODIFY);
        AuthorizedAction authorizedAction = editRequest.perform();
        return Response.ok(authorizedActionInfoFactory.from(authorizedAction)).build();
    }

    private void validateInfo(AuthorizedActionInfo actionForEdit) {
        Predicate<Long> check = id -> id != null && id > 0;
        new RestValidationBuilder()
                .notEmpty(actionForEdit.name, "name")
                .notEmpty(actionForEdit.triggeredBy != null ? actionForEdit.triggeredBy.symbol : null, "triggeredBy")
                .on(actionForEdit.fromState != null ? actionForEdit.fromState.id : null).field("fromState").check(check).message(MessageSeeds.FIELD_CAN_NOT_BE_EMPTY).test()
                .on(actionForEdit.toState != null ? actionForEdit.toState.id : null).field("toState").check(check).message(MessageSeeds.FIELD_CAN_NOT_BE_EMPTY).test()
                .validate();
    }

    @DELETE
    @Path("/{actionId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.CONFIGURE_DEVICE_LIFE_CYCLES})
    public Response deleteAuthorizedAction(@PathParam("deviceLifeCycleId") Long deviceLifeCycleId, @PathParam("actionId") Long actionId, @BeanParam QueryParameters queryParams) {
        DeviceLifeCycle deviceLifeCycle = resourceHelper.findDeviceLifeCycleByIdOrThrowException(deviceLifeCycleId);
        AuthorizedActionRequestFactory factory = new AuthorizedActionRequestFactory(this.resourceHelper);
        AuthorizedActionChangeRequest deleteRequest = factory.from(deviceLifeCycle, actionId, AuthorizedActionRequestFactory.Operation.DELETE);
        AuthorizedAction authorizedAction = deleteRequest.perform();
        return Response.ok(authorizedActionInfoFactory.from(authorizedAction)).build();
    }
}
