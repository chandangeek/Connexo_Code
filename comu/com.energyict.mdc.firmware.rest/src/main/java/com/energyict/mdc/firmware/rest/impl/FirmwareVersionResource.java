package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.security.Privileges;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static com.elster.jupiter.util.conditions.Where.where;

@Path("/devicetypes/{id}/firmwares")
public class FirmwareVersionResource {
    private final FirmwareService firmwareService;
    private final RestQueryService restQueryService;
    private final DeviceConfigurationService deviceConfigurationService;

    private static final String FILTER_STATUS_PARAMETER = "status";
    private static final String FILTER_TYPE_PARAMETER = "type";

    @Inject
    public FirmwareVersionResource(FirmwareService firmwareService, RestQueryService restQueryService, DeviceConfigurationService deviceConfigurationService) {
        this.firmwareService = firmwareService;
        this.restQueryService = restQueryService;
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE_TYPE, Privileges.ADMINISTRATE_DEVICE_TYPE})
    public PagedInfoList getFirmwareVersions(@PathParam("id") long id, @BeanParam JsonQueryFilter filter, @BeanParam QueryParameters queryParameters) {
        DeviceType deviceType =  findDeviceTypeOrElseThrowException(id);

        Finder<FirmwareVersion> allFirmwaresFinder = firmwareService.findAllFirmwareVersions(getFirmwareVersionConditions(filter, deviceType));
        List<FirmwareVersion> allFirmwares = allFirmwaresFinder.from(queryParameters).find();
        List<FirmwareVersionInfo> firmwareInfos = FirmwareVersionInfo.from(allFirmwares);
        return PagedInfoList.fromPagedList("data", firmwareInfos, queryParameters);
    }

    private Condition getFirmwareVersionConditions(JsonQueryFilter filter, DeviceType deviceType) {
        Condition condition = where("deviceType").isEqualTo(deviceType);

        if (filter.hasFilters()) {
            if (filter.hasProperty(FILTER_STATUS_PARAMETER)) {
                condition = condition.and(where("firmwareStatus").isEqualTo(FirmwareStatus.from(filter.getString(FILTER_STATUS_PARAMETER))));
            }
            if (filter.hasProperty(FILTER_TYPE_PARAMETER)) {
                condition = condition.and(where("firmwareType").isEqualTo(FirmwareType.from(filter.getString(FILTER_TYPE_PARAMETER))));
            }
        }
        return condition;
    }

    private DeviceType findDeviceTypeOrElseThrowException(long deviceTypeId) {
        return deviceConfigurationService.findDeviceType(deviceTypeId).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }
}
