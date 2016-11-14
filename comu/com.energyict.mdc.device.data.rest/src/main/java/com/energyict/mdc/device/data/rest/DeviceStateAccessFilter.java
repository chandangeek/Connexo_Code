package com.energyict.mdc.device.data.rest;

import com.elster.jupiter.users.User;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DeviceStateAccessFilter implements ContainerRequestFilter {
    private static final Logger LOGGER = Logger.getLogger(DeviceStateAccessFilter.class.getName());
    private static final int DEVICE_NAME_SEGMENT_POSITION = 1;
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
        if (pathSegments == null || pathSegments.size() < MINIMUM_SEGMENT_COUNT) {
            LOGGER.warning("You applied the RestrictedDeviceState annotation for incorrect resource. " +
                    "The annotated resource url MUST match this template: \"/some_path_segment/{device_name}/*\"");
            return;
        }
        if (isUserHasIgnoredRole(requestContext)) {
            return;
        }
        if (!isRestrictedHttpMethod(requestContext)) {
            return;
        }
        Optional<Device> device = deviceService.findDeviceByName(pathSegments.get(DEVICE_NAME_SEGMENT_POSITION).getPath());
        if (device.isPresent() && !getRestrictedDeviceStates().contains(device.get().getState().getName())) {
            // Current device state is not restricted, so stop our work
            return;
        }
        requestContext.abortWith(Response.status(Response.Status.NOT_FOUND).build());
    }

    private Set<String> getRestrictedDeviceStates() {
        Set<String> restrictedStates = Collections.emptySet();
        DeviceStatesRestricted methodAnnotation = resourceInfo.getResourceMethod().getAnnotation(DeviceStatesRestricted.class);
        if (methodAnnotation != null) {
            restrictedStates = Arrays.stream(methodAnnotation.value()).map(DefaultState::getKey).collect(Collectors.toSet());
        } else {
            DeviceStatesRestricted classAnnotation = resourceInfo.getResourceClass().getAnnotation(DeviceStatesRestricted.class);
            if (classAnnotation != null) {
                restrictedStates = Arrays.stream(classAnnotation.value()).map(DefaultState::getKey).collect(Collectors.toSet());
            }
        }
        if (restrictedStates.isEmpty()) {
            LOGGER.warning("The RestrictedDeviceState annotation doesn't have any restricted state. " +
                    "Class = " + resourceInfo.getResourceClass().getName() + ", method = " + resourceInfo.getResourceMethod().getName());
        }
        return restrictedStates;
    }

    private boolean isRestrictedHttpMethod(ContainerRequestContext requestContext) {
        if (this.resourceInfo.getResourceMethod().getAnnotation(DeviceStatesRestricted.class) != null) {
            // Allow method annotation override a class annotation
            // For example we restrict all PUT and POST requests for a specific class and just one GET request via annotated method
            return true;
        }
        DeviceStatesRestricted classAnnotation = this.resourceInfo.getResourceClass().getAnnotation(DeviceStatesRestricted.class);
        if (classAnnotation != null && classAnnotation.methods() != null) {
            return Arrays.asList(classAnnotation.methods()).contains(requestContext.getMethod());
        }
        return false;
    }

    private boolean isUserHasIgnoredRole(ContainerRequestContext requestContext) {
        String[] ignoredRoles = null;
        DeviceStatesRestricted methodAnnotation = resourceInfo.getResourceMethod().getAnnotation(DeviceStatesRestricted.class);
        if (methodAnnotation != null) {
            ignoredRoles = methodAnnotation.ignoredUserRoles();
        } else {
            DeviceStatesRestricted classAnnotation = resourceInfo.getResourceClass().getAnnotation(DeviceStatesRestricted.class);
            if (classAnnotation != null) {
                ignoredRoles = classAnnotation.ignoredUserRoles();
            }
        }
        if (ignoredRoles != null && ignoredRoles.length > 0) {
            User user = (User) requestContext.getSecurityContext().getUserPrincipal();
            if (user != null) {
                return Arrays.stream(ignoredRoles).anyMatch(candidate -> user.hasPrivilege(null, candidate));
            }
        }
        return false;
    }
}
