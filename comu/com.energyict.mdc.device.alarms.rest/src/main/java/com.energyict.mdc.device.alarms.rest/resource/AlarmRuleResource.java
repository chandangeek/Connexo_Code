/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.rest.resource;


import com.elster.jupiter.issue.rest.response.AssignmentRuleInfo;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleTemplateInfo;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleTemplateInfoFactory;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.entity.AssignmentRule;
import com.elster.jupiter.issue.share.service.IssueAssignmentService;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.util.conditions.Condition;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

import static com.elster.jupiter.issue.rest.response.ResponseHelper.entity;

@Path("/rules")
public class AlarmRuleResource extends BaseAlarmResource {

    private final IssueAssignmentService issueAssignmentService;
    private final CreationRuleTemplateInfoFactory templateInfoFactory;

    @Inject
    public AlarmRuleResource(IssueAssignmentService issueAssignmentService, CreationRuleTemplateInfoFactory templateInfoFactory) {
        this.issueAssignmentService = issueAssignmentService;
        this.templateInfoFactory = templateInfoFactory;
    }

    @GET
    @Path("/assign")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.VIEW_ASSIGNMENT_RULE)
    public Response getAssignmentRules() {
        List<AssignmentRule> assignmentRules = issueAssignmentService.getAssignmentRuleQuery().select(Condition.TRUE);
        return entity(assignmentRules, AssignmentRuleInfo.class).build();
    }

    @GET
    @Path("/templates")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_CREATION_RULE, Privileges.Constants.VIEW_CREATION_RULE})
    public PagedInfoList getCreationRulesTemplates(@BeanParam JsonQueryParameters params) {
        List<CreationRuleTemplateInfo> infos = getIssueCreationService()
                .getCreationRuleTemplates().stream()
                .filter(template -> template.getIssueType().getKey().equals("devicealarm"))
                .map(templateInfoFactory::asInfo)
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("creationRuleTemplates", infos, params);
    }

    @GET
    @Path("/templates/{name}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_CREATION_RULE, Privileges.Constants.VIEW_CREATION_RULE})
    public Response getTemplate(@PathParam("name") String name) {
        CreationRuleTemplate template = getIssueCreationService().findCreationRuleTemplate(name)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return Response.ok(templateInfoFactory.asInfo(template)).build();
    }
}
