package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.lifecycle.config.DefaultState;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DeviceStateAccessFilter implements ContainerRequestFilter {
    private static final Logger LOGGER = Logger.getLogger(DeviceStateAccessFilter.class.getName());
    private static final int MRID_SEGMENT_POSITION = 1;
    private static final int MINIMUM_SEGMENT_COUNT = 2;

    private final ResourceInfo resourceInfo;
    private final UriInfo uriInfo;
    private final DeviceService deviceService;

    @Inject
    public DeviceStateAccessFilter(ResourceInfo resourceInfo, UriInfo uriInfo, DeviceService deviceService) {
        this.resourceInfo = resourceInfo;
        this.uriInfo = uriInfo;
        this.deviceService = deviceService;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        List<PathSegment> pathSegments = this.uriInfo.getPathSegments(true);
        if (pathSegments == null || pathSegments.size() < MINIMUM_SEGMENT_COUNT){
            LOGGER.warning("You applied the RestrictedDeviceState annotation for incorrect resource. " +
                    "The annotated resource url MUST match this template: \"/device/mrid/*\"");
            return;
        }
        if (!isRestrictedHttpMethod(requestContext)){
            // User performs allowed request, for example GET request while only PUT and POST are forbidden
            return;
        }
        Optional<Device> device = deviceService.findByUniqueMrid(pathSegments.get(MRID_SEGMENT_POSITION).getPath());
        if (device.isPresent() && !getRestrictedDeviceStates().contains(device.get().getState().getName())){
            // Current device state is not restricted, so stop our work
            return;
        }
        requestContext.abortWith(Response.status(Response.Status.NOT_FOUND).build());
    }

    private Set<String> getRestrictedDeviceStates(){
        Set<DefaultState> restrictedStates = new HashSet<>();
        DeviceStatesRestricted classAnnotation = resourceInfo.getResourceClass().getAnnotation(DeviceStatesRestricted.class);
        if (classAnnotation != null){
            restrictedStates.addAll(Arrays.asList(classAnnotation.value()));
        }
        DeviceStatesRestricted methodAnnotation = resourceInfo.getResourceMethod().getAnnotation(DeviceStatesRestricted.class);
        if (methodAnnotation != null){
            restrictedStates.addAll(Arrays.asList(methodAnnotation.value()));
        }
        if (restrictedStates.isEmpty()){
            LOGGER.warning("The RestrictedDeviceState annotation doesn't have any restricted state. " +
                    "Class = " + resourceInfo.getResourceClass().getName() + ", method = " + resourceInfo.getResourceMethod().getName());
        }
        return restrictedStates.stream().map(DefaultState::getKey).collect(Collectors.toSet());
    }

    private boolean isRestrictedHttpMethod(ContainerRequestContext requestContext){
        DeviceStatesRestricted classAnnotation = resourceInfo.getResourceClass().getAnnotation(DeviceStatesRestricted.class);
        if (classAnnotation != null && classAnnotation.methods() != null){
            return Arrays.asList(classAnnotation.methods()).contains(requestContext.getMethod());
        }
        return false;
    }
}
