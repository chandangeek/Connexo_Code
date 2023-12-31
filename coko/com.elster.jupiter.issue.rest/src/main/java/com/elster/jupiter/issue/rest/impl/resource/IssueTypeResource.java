/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.impl.resource;

import com.elster.jupiter.issue.rest.response.IssueTypeInfo;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.IssueTypes;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.elster.jupiter.issue.rest.request.RequestHelper.KEY;
import static com.elster.jupiter.issue.rest.response.ResponseHelper.entity;
import static com.elster.jupiter.util.conditions.Where.where;

@Path("/issuetypes")
public class IssueTypeResource extends BaseResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE, Privileges.Constants.ASSIGN_ISSUE, Privileges.Constants.CLOSE_ISSUE, Privileges.Constants.COMMENT_ISSUE, Privileges.Constants.ACTION_ISSUE})
    public Response getIssueTypes(@HeaderParam("X-CONNEXO-APPLICATION-NAME") String appKey) {
        List<IssueType> issueTypes = getSupportedIssueTypes(appKey);
        return entity(issueTypes, IssueTypeInfo.class).build();
    }

    @GET
    @Path("/notmanual")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE, Privileges.Constants.ASSIGN_ISSUE, Privileges.Constants.CLOSE_ISSUE, Privileges.Constants.COMMENT_ISSUE, Privileges.Constants.ACTION_ISSUE})
    public Response getNotManualIssueTypes(@HeaderParam("X-CONNEXO-APPLICATION-NAME") String appKey) {
        List<IssueType> issueTypes = getSupportedIssueTypesWithoutManual(appKey);
        return entity(issueTypes, IssueTypeInfo.class).build();
    }

    private List<IssueType> getSupportedIssueTypes(String appKey) {
        if (appKey != null) {
            switch (appKey) {
                case "INS":
                    return getIssueService().query(IssueType.class)
                            .select(where(KEY).isEqualTo(IssueTypes.USAGEPOINT_DATA_VALIDATION.getName()));
                case "MDC":
                    return getIssueService().query(IssueType.class)
                            .select(where(KEY).in(Arrays.asList(
                                    IssueTypes.DATA_COLLECTION.getName(),
                                    IssueTypes.DATA_VALIDATION.getName(),
                                    IssueTypes.DEVICE_LIFECYCLE.getName(),
                                    IssueTypes.SERVICE_CALL_ISSUE.getName(),
                                    IssueTypes.TASK.getName(),
                                    IssueTypes.MANUAL.getName(),
                                    IssueTypes.WEB_SERVICE.getName()
                            )));
            }
        }
        return Collections.emptyList();
    }

    private List<IssueType> getSupportedIssueTypesWithoutManual(String appKey) {
        if (appKey != null) {
            switch (appKey) {
                case "INS":
                    return getIssueService().query(IssueType.class)
                            .select(where(KEY).isEqualTo(IssueTypes.USAGEPOINT_DATA_VALIDATION
                                    .getName()));
                case "MDC":
                    return getIssueService().query(IssueType.class)
                            .select(where(KEY).in(new ArrayList<String>() {{
                                add(IssueTypes.DATA_COLLECTION.getName());
                                add(IssueTypes.DATA_VALIDATION.getName());
                                add(IssueTypes.DEVICE_LIFECYCLE.getName());
                                add(IssueTypes.SERVICE_CALL_ISSUE.getName());
                                add(IssueTypes.TASK.getName());
                                add(IssueTypes.WEB_SERVICE.getName());
                            }}));
            }
        }
        return Collections.emptyList();
    }
}
