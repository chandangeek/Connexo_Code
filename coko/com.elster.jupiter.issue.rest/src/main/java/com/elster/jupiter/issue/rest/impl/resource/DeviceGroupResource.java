/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.impl.resource;

import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

@Path("/devicegroups")
public class DeviceGroupResource extends BaseResource{

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE,Privileges.Constants.ASSIGN_ISSUE,Privileges.Constants.CLOSE_ISSUE,Privileges.Constants.COMMENT_ISSUE,Privileges.Constants.ACTION_ISSUE})
    public PagedInfoList getDeviceGroups(@BeanParam JsonQueryParameters queryParameters) {
        List<IdWithNameInfo> infos = getMeteringGroupsService().findEndDeviceGroups()
                .stream()
                .map(deviceGroup -> new IdWithNameInfo(deviceGroup.getId(), deviceGroup.getName()))
                .collect(Collectors.toList());

        return PagedInfoList.fromCompleteList("devicegroups", infos, queryParameters);
    }

}
