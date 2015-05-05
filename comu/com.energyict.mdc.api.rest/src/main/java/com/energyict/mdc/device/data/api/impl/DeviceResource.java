package com.energyict.mdc.device.data.api.impl;

import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.security.Privileges;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ElementKind;
import javax.validation.metadata.ConstraintDescriptor;
import javax.ws.rs.BeanParam;
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
import javax.ws.rs.core.UriInfo;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Created by bvn on 4/22/15.
 */
@Path("/devices")
public class DeviceResource {

    private final DeviceService deviceService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final Provider<RegisterResource> registerResourceProvider;
    private final DeviceInfoFactory deviceInfoFactory;

    @Inject
    public DeviceResource(DeviceService deviceService, DeviceConfigurationService deviceConfigurationService, Provider<RegisterResource> registerResourceProvider, DeviceInfoFactory deviceInfoFactory) {
        this.deviceService = deviceService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.registerResourceProvider = registerResourceProvider;
        this.deviceInfoFactory = deviceInfoFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA})
    @Path("/{mrid}")
    public Response getDevice(@PathParam("mrid") String mRID) {
        DeviceInfo deviceInfo = deviceService.findByUniqueMrid(mRID).map(deviceInfoFactory::plain).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND.getStatusCode()));
        return Response.ok(deviceInfo).build();
    }

    @GET
    @Produces("application/h+json;charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA})
    @Path("/{mrid}")
    public Response getHypermediaDevice(@PathParam("mrid") String mRID, @Context UriInfo uriInfo) {
        DeviceInfo deviceInfo = deviceService.findByUniqueMrid(mRID).map(d -> deviceInfoFactory.asHypermedia(d, uriInfo)).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND.getStatusCode()));
        return Response.ok(deviceInfo).build();
    }

    @GET
    @Produces("application/hal+json;charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA})
    @Path("/{mrid}")
    public Response getHalDevice(@PathParam("mrid") String mRID, @Context UriInfo uriInfo) {
        HalInfo deviceInfo = deviceService.findByUniqueMrid(mRID).map(d -> deviceInfoFactory.asHal(d, uriInfo)).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND.getStatusCode()));

        return Response.ok(deviceInfo).build();
    }


    @GET
    @Produces("application/h+json;charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA})
    public Response getDevices(@BeanParam QueryParameters queryParameters,@Context UriInfo uriInfo) {
        List<DeviceInfo> infos = deviceService.findAllDevices(Condition.TRUE).stream().limit(10).map(d -> deviceInfoFactory.asHypermedia(d, uriInfo)).collect(toList());

        return Response.ok(PagedInfoList.asJson("devices", infos, queryParameters)).build();
    }

//    @GET
//    @Produces("application/hal+json; charset=UTF-8")
//    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA})
//    public Response getHALDevices(@Context UriInfo uriInfo) {
//        List<DeviceInfo> deviceInfos = deviceService.findAllDevices(Condition.TRUE).stream().limit(10).map(deviceInfoFactory::from).collect(toList());
//        return Response.ok(HalInfo.wrap(deviceInfos)).build();
//    }
//
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

            return deviceInfoFactory.plain(newDevice);
        } catch (ConstraintViolationException e) {
            throw new LegacyConstraintViolationException(e, new HashMap<>());
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
    private final javax.validation.Path rewrittenPath;

    public LegacyConstraintViolation(ConstraintViolation<T> violation, Map<String, String> renames) {
        this.violation = violation;
        this.renames.putAll(renames);
        RewrittenPath nodes = new RewrittenPath();
        for (javax.validation.Path.Node node : violation.getPropertyPath()) {
            if (renames.containsKey(node.getName())) {
                nodes.add(new javax.validation.Path.Node() {
                    @Override
                    public String getName() {
                        return renames.get(node.getName());
                    }

                    @Override
                    public boolean isInIterable() {
                        return false;
                    }

                    @Override
                    public Integer getIndex() {
                        return null;
                    }

                    @Override
                    public Object getKey() {
                        return null;
                    }

                    @Override
                    public ElementKind getKind() {
                        return null;
                    }

                    @Override
                    public <T extends javax.validation.Path.Node> T as(Class<T> aClass) {
                        return null;
                    }
                });
            } else {
                nodes.add(node);
            }
        }
        this.rewrittenPath = nodes;

    }

    class RewrittenPath extends ArrayList<javax.validation.Path.Node> implements javax.validation.Path {

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
        return this.rewrittenPath;
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
