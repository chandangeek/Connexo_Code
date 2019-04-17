/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.rest.resource;

import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;

import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

@Path("/devicegroups")
public class DeviceGroupResource extends BaseAlarmResource{

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getDeviceGroups(@BeanParam JsonQueryParameters queryParameters) {
        List<IdWithNameInfo> infos = getMeteringGroupService().findEndDeviceGroups()
                .stream()
                .map(deviceGroup -> new IdWithNameInfo(deviceGroup.getId(), deviceGroup.getName()))
                .collect(Collectors.toList());

        return PagedInfoList.fromCompleteList("devicegroups", infos, queryParameters);
    }
}
