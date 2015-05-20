package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.domain.util.QueryParameters;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareService;

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
import java.util.List;
import java.util.stream.Collectors;

@Path("/campaigns")
public class FirmwareCampaignResource {
    private final FirmwareService firmwareService;
    private final ResourceHelper resourceHelper;
    private final FirmwareCampaignInfoFactory campaignInfoFactory;
    private final DeviceInFirmwareCampaignInfoFactory deviceInCampaignInfoFactory;

    @Inject
    public FirmwareCampaignResource(FirmwareService firmwareService, ResourceHelper resourceHelper, FirmwareCampaignInfoFactory campaignInfoFactory, DeviceInFirmwareCampaignInfoFactory deviceInCampaignInfoFactory) {
        this.firmwareService = firmwareService;
        this.resourceHelper = resourceHelper;
        this.campaignInfoFactory = campaignInfoFactory;
        this.deviceInCampaignInfoFactory = deviceInCampaignInfoFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFirmwareCampaigns(@BeanParam JsonQueryParameters queryParameters){
        List<FirmwareCampaignInfo> firmwareCampaigns = firmwareService.getFirmwareCampaigns()
                .from(queryParameters)
                .stream()
                .map(campaignInfoFactory::from)
                .collect(Collectors.toList());
        return Response.ok(PagedInfoList.fromPagedList("firmwareCampaigns", firmwareCampaigns, queryParameters)).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFirmwareCampaignById(@PathParam("id") long firmwareCampaignId){
        FirmwareCampaign firmwareCampaign = resourceHelper.findFirmwareCampaignOrThrowException(firmwareCampaignId);
        return Response.ok(campaignInfoFactory.from(firmwareCampaign)).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addFirmwareCampaign(FirmwareCampaignInfo info){
        FirmwareCampaign firmwareCampaign = info.create(firmwareService, resourceHelper);
        firmwareCampaign.save();
        return Response.ok(campaignInfoFactory.from(firmwareCampaign)).build();
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response editFirmwareCampaign(FirmwareCampaignInfo info){
        FirmwareCampaign firmwareCampaign = resourceHelper.findFirmwareCampaignOrThrowException(info.id);
        info.writeTo(firmwareCampaign);
        firmwareCampaign.save();
        return Response.ok(campaignInfoFactory.from(firmwareCampaign)).build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteFirmwareCampaign(@PathParam("id") long firmwareCampaignId){
        FirmwareCampaign firmwareCampaign = resourceHelper.findFirmwareCampaignOrThrowException(firmwareCampaignId);
        firmwareCampaign.delete();
        return Response.ok(campaignInfoFactory.from(firmwareCampaign)).build();
    }

    @GET
    @Path("/{id}/devices")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDevicesForFirmwareCampaign(@PathParam("id") long firmwareCampaignId, @BeanParam JsonQueryParameters queryParameters){
        FirmwareCampaign firmwareCampaign = resourceHelper.findFirmwareCampaignOrThrowException(firmwareCampaignId);
        return Response.ok(PagedInfoList.fromPagedList("devices", deviceInCampaignInfoFactory.from(firmwareCampaign.getDevices()), queryParameters)).build();
    }
}
