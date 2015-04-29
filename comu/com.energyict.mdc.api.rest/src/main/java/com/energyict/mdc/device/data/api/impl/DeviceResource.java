package com.energyict.mdc.device.data.api.impl;

import com.elster.jupiter.rest.util.LegacyPropertyMapper;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.security.Privileges;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.metadata.ConstraintDescriptor;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Created by bvn on 4/22/15.
 */
@Path("/devices")
public class DeviceResource {

    private final DeviceService deviceService;
    private final DeviceConfigurationService deviceConfigurationService;

    @Inject
    public DeviceResource(DeviceService deviceService, DeviceConfigurationService deviceConfigurationService) {
        this.deviceService = deviceService;
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA})
    public Response getDevices() {
        return Response.ok(deviceService.findAllDevices(Condition.TRUE).stream().limit(10).map(DeviceInfo::from).collect(toList())).build();
    }

    @GET
    @Produces("application/hal+json; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA})
    public Response getHALDevices() {
        List<DeviceInfo> deviceInfos = deviceService.findAllDevices(Condition.TRUE).stream().limit(10).map(DeviceInfo::from).collect(toList());
        HalInfo.wrap(deviceInfos);
        return Response.ok(deviceInfos).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA})
    @Path("/{mrid}")
    public Response getDevice(@PathParam("mrid") String mRID) {
        DeviceInfo deviceInfo = deviceService.findByUniqueMrid(mRID).map(DeviceInfo::from).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND.getStatusCode()));
        return Response.ok(deviceInfo).build();
    }

    @GET
    @Produces("application/hal+json; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA})
    @Path("/{mrid}")
    public Response getHalDevice(@PathParam("mrid") String mRID) {
        DeviceInfo deviceInfo = deviceService.findByUniqueMrid(mRID).map(DeviceInfo::from).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND.getStatusCode()));
        HalInfo wrap = HalInfo.wrap(deviceInfo, "http://localhost:8085");
        return Response.ok(wrap).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADD_DEVICE)
    public DeviceInfo addDevice(DeviceInfo info, @Context SecurityContext securityContext) {
        try {
            Optional<DeviceConfiguration> deviceConfiguration = Optional.empty();
            if (info.deviceConfigurationId != null) {
                deviceConfiguration = deviceConfigurationService.findDeviceConfiguration(info.deviceConfigurationId);
            }

            Device newDevice = deviceService.newDevice(deviceConfiguration.orElse(null), info.mIRD, info.mIRD);
            newDevice.setSerialNumber(info.serialNumber);
            newDevice.setYearOfCertification(2015);
            newDevice.save();

            return DeviceInfo.from(newDevice);
        } catch (ConstraintViolationException e) {
            for (ConstraintViolation<?> constraintViolation : e.getConstraintViolations()) {
                javax.validation.Path propertyPath = constraintViolation.getPropertyPath();
                if (propertyPath != null) {
                    if (constraintViolation.getLeafBean() instanceof LegacyPropertyMapper) {
                        propertyPath = ((LegacyPropertyMapper) constraintViolation.getLeafBean()).getLegacyPropertyPath(propertyPath);
                    }
                }
            }
            throw e;
        }
    }

}

class LegacyConstraintViolationException extends ConstraintViolationException {

    public LegacyConstraintViolationException(ConstraintViolationException original, Map<String, String> propertyRenames) {
        super(original.getMessage(), original.getConstraintViolations().stream().map(cv -> new LegacyConstraintViolation<>(cv, propertyRenames)).collect(toSet()));
    }

}

class LegacyConstraintViolation<T> implements ConstraintViolation<T> {
    private final ConstraintViolation<T> violation;
    private final Map<String, String> renames = new HashMap<>();

    public LegacyConstraintViolation(ConstraintViolation<T> violation, Map<String, String> renames) {
        this.violation = violation;
        this.renames.putAll(renames);
//        for (javax.validation.Path.Node node : path) {
//            if (renames.containsKey(node.getName()) {
//                string.append(".")
//            }
//        }

    }

    /**
     * Search new property names and replace them with the original values for backwards compatibility
     */
    private javax.validation.Path searchAndReplaceNewPropertyNames(javax.validation.Path path) {
        StringBuilder string = new StringBuilder();
        return null;
    }

    @Override
    public String getMessage() {
        return violation.getMessage();
    }

    @Override
    public String getMessageTemplate() {
        return violation.getMessageTemplate();
    }

    @Override
    public T getRootBean() {
        return violation.getRootBean();
    }

    @Override
    public Class<T> getRootBeanClass() {
        return violation.getRootBeanClass();
    }

    @Override
    public Object getLeafBean() {
        return violation.getLeafBean();
    }

    @Override
    public Object[] getExecutableParameters() {
        return violation.getExecutableParameters();
    }

    @Override
    public Object getExecutableReturnValue() {
        return violation.getExecutableReturnValue();
    }

    @Override
    public javax.validation.Path getPropertyPath() {
        return violation.getPropertyPath();
    }

    @Override
    public Object getInvalidValue() {
        return violation.getInvalidValue();
    }

    @Override
    public ConstraintDescriptor<?> getConstraintDescriptor() {
        return violation.getConstraintDescriptor();
    }

    @Override
    public <U> U unwrap(Class<U> aClass) {
        return violation.unwrap(aClass);
    }
}
