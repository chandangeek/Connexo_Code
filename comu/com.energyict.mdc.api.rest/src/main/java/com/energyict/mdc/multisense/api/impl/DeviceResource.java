package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PROPFIND;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.multisense.api.impl.utils.FieldSelection;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
import com.energyict.mdc.multisense.api.impl.utils.PagedInfoList;
import com.energyict.mdc.multisense.api.impl.utils.ResourceHelper;

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
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.elster.jupiter.util.Checks.is;
import static java.util.stream.Collectors.toList;

/**
 * @servicetag Device
 * @author bvn
 */
@Path("/devices")
public class DeviceResource {

    private final DeviceService deviceService;
    private final DeviceInfoFactory deviceInfoFactory;
    private final DeviceConfigurationService deviceConfigurationService;
    private final BatchService batchService;
    private final ExceptionFactory exceptionFactory;
    private final TopologyService topologyService;
    private final ResourceHelper resourceHelper;


    @Inject
    public DeviceResource(DeviceService deviceService, DeviceInfoFactory deviceInfoFactory, DeviceConfigurationService deviceConfigurationService, BatchService batchService, ExceptionFactory exceptionFactory, TopologyService topologyService, ResourceHelper resourceHelper) {
        this.deviceService = deviceService;
        this.deviceInfoFactory = deviceInfoFactory;
        this.deviceConfigurationService = deviceConfigurationService;
        this.batchService = batchService;
        this.exceptionFactory = exceptionFactory;
        this.topologyService = topologyService;
        this.resourceHelper = resourceHelper;
    }

    /**
     * View the contents of a uniquely identified device
     *
     * @summary View device identified by mRID
     *
     * @statuscode 404 If there is no device with the provided mRID
     * @statuscode 200 The device was successfully retrieved
     * @param mRID The device's mRID
     * @param uriInfo added by Jersey framework
     * @return Device information and links to related resources
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{mrid}")
    public DeviceInfo getDevice(@PathParam("mrid") String mRID, @BeanParam FieldSelection fields, @Context UriInfo uriInfo) {
        return deviceService.findByUniqueMrid(mRID).map(d -> deviceInfoFactory.asHypermedia(d, uriInfo, fields.getFields())).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND.getStatusCode()));
    }

    /**
     * View all devices
     *
     * @summary View all devices
     *
     * @statuscode 200 The devices were successfully retrieved
     * @param queryParameters Paging parameters 'start' and 'limit'
     * @param fieldSelection comma separated list of fields that will be add to the response. If absent, all fields will be added
     * @param uriInfo added by Jersey framework
     * @return Device information and links to related resources
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public PagedInfoList getDevices(@BeanParam JsonQueryParameters queryParameters, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        List<DeviceInfo> infos = deviceService.findAllDevices(Condition.TRUE).from(queryParameters).stream().map(d -> deviceInfoFactory.asHypermedia(d, uriInfo, fieldSelection.getFields())).collect(toList());
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().path(DeviceResource.class);
        return PagedInfoList.from(infos, queryParameters, uriBuilder, uriInfo);
    }

    /**
     * Create a new device
     * @param info JSON payload describing the device
     * @param uriInfo added by framework
     * @responseheader location href to newly created device
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response createDevice(DeviceInfo info, @Context UriInfo uriInfo) {
        Optional<DeviceConfiguration> deviceConfiguration = Optional.empty();
        if (info.deviceConfiguration != null && info.deviceConfiguration.id != null) {
            deviceConfiguration = deviceConfigurationService.findDeviceConfiguration(info.deviceConfiguration.id);
        }

        Device newDevice;
        if (!is(info.batch).emptyOrOnlyWhiteSpace()) {
            newDevice = deviceService.newDevice(deviceConfiguration.orElse(null), info.mRID, info.mRID, info.batch);
        } else {
            newDevice = deviceService.newDevice(deviceConfiguration.orElse(null), info.mRID, info.mRID);
        }
        newDevice.setSerialNumber(info.serialNumber);
        newDevice.setYearOfCertification(info.yearOfCertification);
        newDevice.save();

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
    public DeviceInfo updateDevice(@PathParam("mrid") String mrid, DeviceInfo info, @Context UriInfo uriInfo) {
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
        return deviceInfoFactory.asHypermedia(device, uriInfo, Collections.emptyList());
    }


    @DELETE
    @Path("/{mrid}")
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    public Response deleteDevice(@PathParam("mrid") String mrid) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        device.delete();
        return Response.ok().build();
    }

    @PROPFIND
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    public List<String> getFields() {
        return deviceInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }

}

