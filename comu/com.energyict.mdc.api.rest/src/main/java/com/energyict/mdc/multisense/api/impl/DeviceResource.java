package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.imp.DeviceImportService;
import com.energyict.mdc.device.data.security.Privileges;
import java.net.URI;
import java.util.List;
import java.util.Optional;
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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 4/22/15.
 */
@Path("/devices")
public class DeviceResource {

    private final DeviceService deviceService;
    private final DeviceInfoFactory deviceInfoFactory;
    private final DeviceConfigurationService deviceConfigurationService;
    private final DeviceImportService deviceImportService;

    @Inject
    public DeviceResource(DeviceService deviceService, DeviceInfoFactory deviceInfoFactory, DeviceConfigurationService deviceConfigurationService, DeviceImportService deviceImportService) {
        this.deviceService = deviceService;
        this.deviceInfoFactory = deviceInfoFactory;
        this.deviceConfigurationService = deviceConfigurationService;
        this.deviceImportService = deviceImportService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA})
    @Path("/{mrid}")
    public Response getDevice(@PathParam("mrid") String mRID, @BeanParam SelectedFields fields, @Context UriInfo uriInfo) {
        DeviceInfo deviceInfo = deviceService.findByUniqueMrid(mRID).map(d -> deviceInfoFactory.asHypermedia(d, uriInfo, fields.getFields())).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND.getStatusCode()));
        return Response.ok(deviceInfo).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA})
    public Response getDevices(@BeanParam JsonQueryParameters queryParameters, @BeanParam SelectedFields fields, @Context UriInfo uriInfo) {
        List<DeviceInfo> infos = deviceService.findAllDevices(Condition.TRUE).from(queryParameters).stream().map(d -> deviceInfoFactory.asHypermedia(d, uriInfo, fields.getFields())).collect(toList());
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().path(DeviceResource.class);
        return Response.ok(PagedInfoList.from(infos, queryParameters, uriBuilder, uriInfo)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADD_DEVICE)
    public Response createDevice(DeviceInfo info, @Context UriInfo uriInfo, @BeanParam SelectedFields fields) {
        Optional<DeviceConfiguration> deviceConfiguration = Optional.empty();
        if (info.deviceConfiguration != null && info.deviceConfiguration.id != null) {
            deviceConfiguration = deviceConfigurationService.findDeviceConfiguration(info.deviceConfiguration.id);
        }

        Device newDevice = deviceService.newDevice(deviceConfiguration.orElse(null), info.mRID, info.mRID);
        newDevice.setSerialNumber(info.serialNumber);
        newDevice.setYearOfCertification(info.yearOfCertification);
        newDevice.save();

        if (info.batch != null) {
            deviceImportService.addDeviceToBatch(newDevice, info.batch);
        }
        URI uri = uriInfo.getBaseUriBuilder().
                path(DeviceResource.class).
                path(DeviceResource.class, "getDevice").
                build(newDevice.getmRID());

        return Response.created(uri).build();
    }

    @PUT//the method designed like 'PATCH'
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE_COMMUNICATION)
    public DeviceInfo updateDevice(@PathParam("id") long id, DeviceInfo info, @Context SecurityContext securityContext) {
        Device device = deviceService.findAndLockDeviceByIdAndVersion(id, info.version).orElseThrow(() -> new WebApplicationException(Response.Status.CONFLICT));
//        updateGateway(info, device);
//        if (info.estimationStatus != null) {
//            updateEstimationStatus(info.estimationStatus, device);
//        }
        return null;
    }

    @DELETE
    @Path("/{mrid}")
    @RolesAllowed(Privileges.REMOVE_DEVICE)
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    public Response deleteDevice(@PathParam("mrid") String mrid) {
        Device device = deviceService.findByUniqueMrid(mrid).orElseThrow(() -> new WebApplicationException(Response.Status.CONFLICT));
        device.delete();
        return Response.ok().build();
    }

    @GET
    @Path("/fields")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA})
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    public Response getFields() {
        return Response.ok(deviceInfoFactory.getAvailableFields().stream().sorted().collect(toList())).build();
    }

}

