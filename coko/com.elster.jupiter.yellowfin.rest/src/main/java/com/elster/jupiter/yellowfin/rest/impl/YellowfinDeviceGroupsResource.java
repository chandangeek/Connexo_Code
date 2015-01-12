package com.elster.jupiter.yellowfin.rest.impl;

import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.yellowfin.groups.AdHocDeviceGroup;
import com.elster.jupiter.yellowfin.groups.YellowfinGroupsService;
import com.elster.jupiter.yellowfin.groups.impl.AdHocDeviceGroupImpl;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
    @Path("/dynamic")
    @Consumes(MediaType.APPLICATION_JSON)
    public void cacheDynamicGroup(YellowfinDeviceGroupInfos groupInfos) {
        try(TransactionContext context = transactionService.getContext()){
            for(YellowfinDeviceGroupInfo group : groupInfos.groups){
                yellowfinGroupsService.cacheDynamicDeviceGroup(group.name);
            }
            context.commit();
        }
    }

    @POST
    @Path("/adhoc")
    @Produces(MediaType.APPLICATION_JSON)
    public YellowfinDeviceGroupInfo cacheAdHocGroup(@BeanParam QueryParameters queryParameters, @Context UriInfo uriInfo) {
        Condition condition = Condition.TRUE;

        YellowfinDeviceGroupInfo groupInfo = new YellowfinDeviceGroupInfo();

        MultivaluedMap<String, String> uriParams = uriInfo.getQueryParameters();
        condition = condition.and(addDeviceQueryCondition(uriParams));

        Finder<Device> allDevicesFinder = deviceService.findAllDevices(condition);
        List<Device> allDevices = allDevicesFinder.from(queryParameters).find();

        try(TransactionContext context = transactionService.getContext()){
            Optional<AdHocDeviceGroup>  adhocGroup = yellowfinGroupsService.cacheAdHocDeviceGroup(allDevices);
            context.commit();
            if(adhocGroup.isPresent()){
                groupInfo.name = adhocGroup.get().getName();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return groupInfo;
    }

    private Condition addDeviceQueryCondition(MultivaluedMap<String, String> uriParams) {

        Condition conditionDevice = Condition.TRUE;
        String mRID = uriParams.getFirst("mRID");
        if (mRID != null) {
            mRID = replaceRegularExpression(mRID);
            conditionDevice = !isRegularExpression(mRID)
                    ? conditionDevice.and(where("mRID").isEqualTo(mRID))
                    : conditionDevice.and(where("mRID").likeIgnoreCase(mRID));
        }
        String serialNumber = uriParams.getFirst("serialNumber");
        if (serialNumber != null) {
            serialNumber = replaceRegularExpression(serialNumber);
            conditionDevice = !isRegularExpression(serialNumber)
                    ? conditionDevice.and(where("serialNumber").isEqualTo(serialNumber))
                    : conditionDevice.and(where("serialNumber").likeIgnoreCase(serialNumber));
        }
        String deviceType = uriParams.getFirst("deviceTypeName");
        if (deviceType != null) {
            conditionDevice = conditionDevice.or(createMultipleConditions(Arrays.asList(deviceType.split(",")), "deviceConfiguration.deviceType.name"));
        }
        String deviceConfiguration = uriParams.getFirst("deviceConfigurationName");
        if (deviceConfiguration != null) {
            conditionDevice = conditionDevice.or(createMultipleConditions(Arrays.asList(deviceConfiguration.split(",")), "deviceConfiguration.name"));
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
