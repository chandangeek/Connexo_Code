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
import javax.inject.Provider;
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
    private final Provider<ProtocolDialectResource> protocolDialectResourceProvider;


    @Inject
    public DeviceResource(ResourceHelper resourceHelper, DeviceImportService deviceImportService, DeviceDataService deviceDataService, IssueService issueService, Provider<ProtocolDialectResource> protocolDialectResourceProvider) {
        this.resourceHelper = resourceHelper;
        this.deviceImportService = deviceImportService;
        this.deviceDataService = deviceDataService;
        this.issueService = issueService;
        this.protocolDialectResourceProvider = protocolDialectResourceProvider;
    }
	
	@GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getAllDevices(@BeanParam QueryParameters queryParameters, @BeanParam StandardParametersBean params) {
        Condition condition = getQueryCondition(params);
        Finder<Device> allDevicesFinder = deviceDataService.findAllDevices(condition);
        List<Device> allDevices = allDevicesFinder.from(queryParameters).find();
        List<DeviceInfo> deviceInfos = DeviceInfo.from(allDevices);
        return PagedInfoList.asJson("devices", deviceInfos, queryParameters);
    }

    @GET
    @Path("/{mRID}")
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceInfo findDeviceType(@PathParam("mRID") String id) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(id);
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
        if (mRID != null) {
            conditionDevice =  !params.isRegExp()
                    ? conditionDevice.and(where("mRID").isEqualTo(mRID))
                    : conditionDevice.and(where("mRID").likeIgnoreCase(mRID));
        }
        String serialNumber = params.getFirst("serialNumber");
        if (serialNumber != null) {
            conditionDevice =  !params.isRegExp()
                    ? conditionDevice.and(where("serialNumber").isEqualTo(serialNumber))
                    : conditionDevice.and(where("serialNumber").likeIgnoreCase(serialNumber));
        }
        String deviceType = params.getFirst("deviceTypeName");
        if (deviceType != null) {
            conditionDevice = conditionDevice.and(createMultipleConditions(deviceType,"deviceConfiguration.deviceType.name"));
        }
        String deviceConfiguration = params.getFirst("deviceConfigurationName");
        if (deviceConfiguration != null) {
            conditionDevice = conditionDevice.and(createMultipleConditions(deviceConfiguration,"deviceConfiguration.name"));
        }
        return conditionDevice;
    }

    private Condition createMultipleConditions(String params, String conditionField) {
        Condition condition = Condition.FALSE;
        String[] values = params.split(",");
        for (String value : values) {
            condition = condition.or(where(conditionField).isEqualTo(value.trim()));
        }
        return condition;
    }

    @Path("/{mRID}/protocoldialects")
    public ProtocolDialectResource getProtocolDialectsResource() {
        return protocolDialectResourceProvider.get();
    }

}
