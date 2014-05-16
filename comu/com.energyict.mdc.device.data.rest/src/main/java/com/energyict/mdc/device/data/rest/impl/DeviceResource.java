package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.imp.DeviceImportService;
import com.elster.jupiter.issue.share.service.IssueService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static com.elster.jupiter.util.conditions.Where.where;

@Path("/devices")
public class DeviceResource {
    private final DeviceImportService deviceImportService;
    private final DeviceDataService deviceDataService;
    private final ResourceHelper resourceHelper;
    private final IssueService issueService;


    @Inject
    public DeviceResource(ResourceHelper resourceHelper, DeviceImportService deviceImportService, DeviceDataService deviceDataService, IssueService issueService) {
        this.resourceHelper = resourceHelper;
        this.deviceImportService = deviceImportService;
        this.deviceDataService = deviceDataService;
        this.issueService = issueService;
    }
	
	@GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getAllDevices(@BeanParam QueryParameters queryParameters, @BeanParam StandardParametersBean params) {
        Condition condition = getQueryCondition(params);
        Finder<Device> allDevicesFinder = deviceDataService.findAllDevices(condition);
        List<Device> allDevices = allDevicesFinder.from(queryParameters).find();
        List<DeviceInfo> deviceInfos = DeviceInfo.from(allDevices, deviceImportService, issueService);
        return PagedInfoList.asJson("devices", deviceInfos, queryParameters);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceInfo findDeviceType(@PathParam("id") long id) {
        Device device = resourceHelper.findDeviceByIdOrThrowException(id);
        return DeviceInfo.from(device, deviceImportService, issueService);
    }
	
	private Condition getQueryCondition(StandardParametersBean params) {
        Condition condition = Condition.TRUE;
        if(params.getQueryParameters().size() > 0) {
            condition = condition.and(addDeviceQueryCondition(params));
        }
        return condition;
    }

    private Condition addDeviceQueryCondition(StandardParametersBean params) {
        Condition conditionDevice = Condition.TRUE;
        String mRID = params.getFirst("mRID");
        String serialNumber = params.getFirst("serialNumber");
        if (mRID != null) {
            conditionDevice = conditionDevice.and(where("mRID").isEqualTo(mRID));
        }
        if (serialNumber != null) {
            conditionDevice = conditionDevice.and(where("serialNumber").isEqualTo(serialNumber));
        }
        return conditionDevice;
    }

}
