/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.rest.resource;

import com.elster.jupiter.issue.rest.response.IssueActionInfoFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.energyict.mdc.device.alarms.entity.CreationRuleActionPhase;
import com.energyict.mdc.device.alarms.rest.response.CreationRuleActionPhaseInfo;
import com.energyict.mdc.device.alarms.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Path("/actions")
public class ActionResource extends BaseAlarmResource {

    private final IssueActionInfoFactory actionInfoFactory;

    @Inject
    public ActionResource(IssueActionInfoFactory actionInfoFactory) {
        this.actionInfoFactory = actionInfoFactory;
    }

    @GET
    @Path("/phases")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ALARM,Privileges.Constants.ASSIGN_ALARM,Privileges.Constants.CLOSE_ALARM,Privileges.Constants.COMMENT_ALARM,Privileges.Constants.ACTION_ALARM})
    public PagedInfoList getAllActionPhases(@BeanParam JsonQueryParameters queryParameters) {
        List<CreationRuleActionPhaseInfo> infos = Arrays.asList(CreationRuleActionPhase.values()).stream().map(phase -> new CreationRuleActionPhaseInfo(phase, getThesaurus())).collect(Collectors
                .toList());
        return PagedInfoList.fromCompleteList("creationRuleActionPhases", infos, queryParameters);
    }

}
