package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.imp.DeviceImportService;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.topology.TopologyService;
import java.net.URI;
import java.util.Collections;
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
    private final ExceptionFactory exceptionFactory;
    private final TopologyService topologyService;


    @Inject
    public DeviceResource(DeviceService deviceService, DeviceInfoFactory deviceInfoFactory, DeviceConfigurationService deviceConfigurationService, DeviceImportService deviceImportService, ExceptionFactory exceptionFactory, TopologyService topologyService) {
        this.deviceService = deviceService;
        this.deviceInfoFactory = deviceInfoFactory;
        this.deviceConfigurationService = deviceConfigurationService;
        this.deviceImportService = deviceImportService;
        this.exceptionFactory = exceptionFactory;
        this.topologyService = topologyService;
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
    @Consumes(MediaType.APPLICATION_JSON+"; charset=UTF-8")
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

    @PUT
    @Path("/{mrid}")
    @Consumes(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE_COMMUNICATION)
    public Response updateDevice(@PathParam("mrid") String mrid, DeviceInfo info, @Context SecurityContext securityContext, @Context UriInfo uriInfo) {
        Device device = deviceService.findAndLockDeviceBymRIDAndVersion(mrid, info.version == null ? 0 : info.version).orElseThrow(() -> new WebApplicationException(Response.Status.CONFLICT));
        if (info.masterDevice!=null && info.masterDevice.mRID != null) {
            if (device.getDeviceConfiguration().isDirectlyAddressable()) {
                throw exceptionFactory.newException(MessageSeeds.IMPOSSIBLE_TO_SET_MASTER_DEVICE, device.getmRID());
            }
            Optional<Device> currentGateway = topologyService.getPhysicalGateway(device);
            if (!currentGateway.isPresent() || !currentGateway.get().getmRID().equals(info.masterDevice.mRID)) {
                Device newGateway = deviceService.findByUniqueMrid(info.masterDevice.mRID).orElseThrow(() -> new WebApplicationException("Unknown gateway mRID", Response.Status.BAD_REQUEST));
                topologyService.setPhysicalGateway(device, newGateway);
            }
        } else {
            if (topologyService.getPhysicalGateway(device).isPresent()) {
                topologyService.clearPhysicalGateway(device);
            }
        }
        device.setName(info.name);
        device.setSerialNumber(info.serialNumber);
        device.setYearOfCertification(info.yearOfCertification);
        device.save();
        return Response.ok(deviceInfoFactory.asHypermedia(device, uriInfo, Collections.emptyList())).build();
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

