package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.RestQuery;
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
    private final QueryService queryService;
    private final DeviceConfigurationService deviceConfigurationService;

    @Inject
    public FirmwareVersionResource(FirmwareService firmwareService, QueryService queryService, DeviceConfigurationService deviceConfigurationService) {
        this.firmwareService = firmwareService;
        this.queryService = queryService;
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE_TYPE, Privileges.ADMINISTRATE_DEVICE_TYPE})
    public PagedInfoList getFirmwareVersions(@PathParam("id") long id, @BeanParam JsonQueryFilter filter, @BeanParam QueryParameters queryParameters) {
        DeviceType deviceType =  findDeviceTypeOrElseThrowException(id);

/*
        List<? extends FirmwareVersion> firmwareVersions = ListPager
                .of(firmwareService.getFirmwareVersionQuery().select(getFirmwareVersionConditions(filter), Order.descending("firmwareVersion")))
                .from(queryParameters).find();

                //firmwareService.getFirmwareVersionQuery().select(getFirmwareVersionConditions(filter), Order.descending("firmwareVersion"));

        List<? extends FirmwareVersion> versions =  (firmwareService.getFirmwareVersionQuery().select(getFirmwareVersionConditions(filter), Order.descending("firmwareVersion")))
*/


        Finder<FirmwareVersion> allFirmwaresFinder = firmwareService.findAllFirmwareVersions(getFirmwareVersionConditions(filter, deviceType));
        List<FirmwareVersion> allFirmwares = allFirmwaresFinder.from(queryParameters).find();
        List<FirmwareVersionInfo> firmwareInfos = FirmwareVersionInfo.from(allFirmwares);
        return PagedInfoList.asJson("data", firmwareInfos, queryParameters);
    }

/*    private List<? extends FirmwareVersion> queryFirmwareVersions(QueryParameters queryParameters, JsonQueryFilter filter) {
        Query<? extends FirmwareVersion> query = firmwareService.getFirmwareVersionQuery();
        RestQuery<? extends FirmwareVersion> restQuery = queryService.wrap(query);
        return restQuery.select(queryParameters, getFirmwareVersionConditions(filter), Order.descending("lastRun").nullsLast());
    }*/

    private Condition getFirmwareVersionConditions(JsonQueryFilter filter, DeviceType deviceType) {
        Condition condition = where("deviceType").isEqualTo(deviceType);

        return condition;
    }

    private DeviceType findDeviceTypeOrElseThrowException(long deviceTypeId) {
        return deviceConfigurationService.findDeviceType(deviceTypeId).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }
}
