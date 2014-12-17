package com.elster.jupiter.yellowfin.rest.impl;

import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.yellowfin.groups.YellowfinGroupsService;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.util.List;

import static com.elster.jupiter.util.conditions.Where.where;

@Path("/devicegroups")
public class YellowfinDeviceGroupsResource {

    private final YellowfinGroupsService yellowfinGroupsService;
    private final DeviceService deviceService;
    private final TransactionService transactionService;

    @Inject
    private YellowfinDeviceGroupsResource(YellowfinGroupsService yellowfinGroupsService, DeviceService deviceService, TransactionService transactionService){
        this.yellowfinGroupsService = yellowfinGroupsService;
        this.transactionService = transactionService;
        this.deviceService = deviceService;
    }

    @POST
    @Path("/dynamic/{groupname}")
    public void cacheDynamicGroup(@PathParam("groupname") String groupname) {
        try(TransactionContext context = transactionService.getContext()){
            yellowfinGroupsService.cacheDynamicDeviceGroup(groupname);
            context.commit();
        }
    }

    @POST
    @Path("/adhoc")
    public void cacheAdHocGroup(@BeanParam QueryParameters queryParameters, @Context UriInfo uriInfo) {
        Condition condition = Condition.TRUE;
        MultivaluedMap<String, String> uriParams = uriInfo.getQueryParameters();
        if (uriParams.containsKey("filter")) {
            condition = condition.and(addDeviceQueryCondition(uriInfo.getQueryParameters()));

            Finder<Device> allDevicesFinder = deviceService.findAllDevices(condition);
            List<Device> allDevices = allDevicesFinder.from(queryParameters).find();

            try(TransactionContext context = transactionService.getContext()){
                yellowfinGroupsService.cacheAdHocDeviceGroup(allDevices);
                context.commit();
            }
        }
    }

    private Condition addDeviceQueryCondition(MultivaluedMap<String, String> uriParams) {
        Condition conditionDevice = Condition.TRUE;
        JsonQueryFilter filter = new JsonQueryFilter(uriParams.getFirst("filter"));
        String mRID = filter.getString("mRID");
        if (mRID != null) {
            mRID = replaceRegularExpression(mRID);
            conditionDevice = !isRegularExpression(mRID)
                    ? conditionDevice.and(where("mRID").isEqualTo(mRID))
                    : conditionDevice.and(where("mRID").likeIgnoreCase(mRID));
        }
        String serialNumber = filter.getString("serialNumber");
        if (serialNumber != null) {
            serialNumber = replaceRegularExpression(serialNumber);
            conditionDevice = !isRegularExpression(serialNumber)
                    ? conditionDevice.and(where("serialNumber").isEqualTo(serialNumber))
                    : conditionDevice.and(where("serialNumber").likeIgnoreCase(serialNumber));
        }
        if (filter.hasProperty("deviceTypes")) {
            List<String> deviceTypes = filter.getStringList("deviceTypes");
            if (!deviceTypes.isEmpty()) {
                conditionDevice = conditionDevice.and(createMultipleConditions(deviceTypes, "deviceConfiguration.deviceType.id"));
            }
        }
        if (filter.hasProperty("deviceConfigurations")) {
            List<String> deviceConfigurations = filter.getStringList("deviceConfigurations");
            if (!deviceConfigurations.isEmpty()) {
                conditionDevice = conditionDevice.and(createMultipleConditions(deviceConfigurations, "deviceConfiguration.id"));
            }
        }
        return conditionDevice;
    }

    private Condition createMultipleConditions(List<String> params, String conditionField) {
        Condition condition = Condition.FALSE;
        for (String value : params) {
            condition = condition.or(where(conditionField).isEqualTo(value.trim()));
        }
        return condition;
    }

    private boolean isRegularExpression(String value) {
        if (value.contains("*")) {
            return true;
        }
        if (value.contains("?")) {
            return true;
        }
        if (value.contains("%")) {
            return true;
        }
        return false;
    }

    private String replaceRegularExpression(String value) {
        if (value.contains("*")) {
            value = value.replaceAll("\\*", "%");
            return value;
        }
        if (value.contains("?")) {
            value = value.replaceAll("\\?", "_");
            return value;
        }
        if (value.contains("%")) {
            return value;
        }
        return value;
    }
}
