package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.security.Privileges;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
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
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Created by bvn on 4/22/15.
 */
@Path("/devices")
public class DeviceResource {

    private final DeviceService deviceService;
    private final DeviceInfoFactory deviceInfoFactory;

    @Inject
    public DeviceResource(DeviceService deviceService, DeviceInfoFactory deviceInfoFactory) {
        this.deviceService = deviceService;
        this.deviceInfoFactory = deviceInfoFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA})
    @Path("/{mrid}")
    public Response getHypermediaDevice(@PathParam("mrid") String mRID, @BeanParam FieldList fields, @Context UriInfo uriInfo) {
        DeviceInfo deviceInfo = deviceService.findByUniqueMrid(mRID).map(d -> deviceInfoFactory.asHypermedia(d, uriInfo, fields.getFields())).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND.getStatusCode()));
        return Response.ok(deviceInfo).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA})
    public Response getHypermediaDevices(@BeanParam JsonQueryParameters queryParameters, @BeanParam FieldList fields, @Context UriInfo uriInfo) {
        List<DeviceInfo> infos = deviceService.findAllDevices(Condition.TRUE).from(queryParameters).stream().map(d -> deviceInfoFactory.asHypermedia(d, uriInfo, fields.getFields())).collect(toList());
        UriBuilder uri = uriInfo.getBaseUriBuilder().path(DeviceResource.class);
        return Response.ok(PagedInfoList.from(infos, queryParameters, uri)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADD_DEVICE)
    public DeviceInfo addDevice(DeviceInfo info, @Context SecurityContext securityContext) {
        return null;
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
