/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.rest.resource;

import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;

import com.elster.jupiter.util.HasName;
import com.energyict.mdc.device.alarms.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Path("/devicegroups")
public class DeviceGroupResource extends BaseAlarmResource{

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ALARM,Privileges.Constants.ASSIGN_ALARM,Privileges.Constants.CLOSE_ALARM,Privileges.Constants.COMMENT_ALARM,Privileges.Constants.ACTION_ALARM})
    public PagedInfoList getDeviceGroups(@BeanParam JsonQueryParameters queryParameters) {
        List<IdWithNameInfo> infos = getMeteringGroupService().findEndDeviceGroups()
                .stream()
                .sorted(Comparator.comparing(HasName::getName))
                .map(deviceGroup -> new IdWithNameInfo(deviceGroup.getId(), deviceGroup.getName()))
                .collect(Collectors.toList());

        return PagedInfoList.fromCompleteList("devicegroups", infos, queryParameters);
    }
}
