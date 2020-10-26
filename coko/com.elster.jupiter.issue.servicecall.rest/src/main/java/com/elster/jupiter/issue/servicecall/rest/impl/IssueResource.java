/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.servicecall.rest.impl;

import com.elster.jupiter.bpm.BpmProcessDefinition;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.issue.rest.request.BulkIssueRequest;
import com.elster.jupiter.issue.rest.resource.StandardParametersBean;
import com.elster.jupiter.issue.rest.response.ActionInfo;
import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.issue.servicecall.ServiceCallIssue;
import com.elster.jupiter.issue.servicecall.ServiceCallIssueService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import java.util.List;
import java.util.stream.Collectors;

import static com.elster.jupiter.issue.rest.request.RequestHelper.ID;
import static com.elster.jupiter.issue.rest.response.ResponseHelper.entity;

@Path("/issues")
public class IssueResource {

    private final ServiceCallIssueService serviceCallIssueService;
    private final ServiceCallIssueInfoFactory serviceCallIssueInfoFactory;
    private final BpmService bpmService;
    private final Thesaurus thesaurus;

    @Inject
    public IssueResource(ServiceCallIssueService serviceCallIssueService, ServiceCallIssueInfoFactory serviceCallIssueInfoFactory,
                         BpmService bpmService, Thesaurus thesaurus) {
        this.serviceCallIssueService = serviceCallIssueService;
        this.serviceCallIssueInfoFactory = serviceCallIssueInfoFactory;
        this.bpmService = bpmService;
        this.thesaurus = thesaurus;
    }

    @GET @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE, Privileges.Constants.ASSIGN_ISSUE, Privileges.Constants.CLOSE_ISSUE, Privileges.Constants.COMMENT_ISSUE, Privileges.Constants.ACTION_ISSUE})
    public Response getIssueById(@PathParam("id") long id) {
        ServiceCallIssue issue = serviceCallIssueService.findIssue(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return Response.ok(serviceCallIssueInfoFactory.asInfo(issue, DeviceInfo.class)).build();
    }

    @GET
    @Transactional
    @Path("/{" + ID + "}/processes")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE, Privileges.Constants.ASSIGN_ISSUE, Privileges.Constants.CLOSE_ISSUE, Privileges.Constants.COMMENT_ISSUE, Privileges.Constants.ACTION_ISSUE})
    public IssueProcessInfos getAvailableProcesses(@PathParam(ID) long id, @BeanParam StandardParametersBean params, @HeaderParam("Authorization") String auth) {
        String jsonContent;
        IssueProcessInfos issueProcessInfos = new IssueProcessInfos();
        JSONArray arr = null;
        if (params.get("variableid") != null && params.get("variablevalue") != null) {
            try {
                String rest = "/services/rest/tasks/allprocesses?";
                rest += "variableid=" + params.get("variableid").get(0);
                rest += "&variablevalue=" + params.get("variablevalue").get(0);
                jsonContent = bpmService.getBpmServer().doGet(rest, auth);
                if (!"".equals(jsonContent)) {
                    JSONObject obj = new JSONObject(jsonContent);
                    arr = obj.getJSONArray("processInstances");
                }
            } catch (JSONException e) {
                throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE)
                        .entity(thesaurus.getString("error.flow.unavailable", "Cannot connect to Flow; HTTP error {0}."))
                        .build());
            } catch (RuntimeException e) {
                throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE)
                        .entity(String.format(thesaurus.getString("error.flow.invalid.response", "Invalid response received, please check your Flow version."), e.getMessage()))
                        .build());
            }
            List<BpmProcessDefinition> activeProcesses = bpmService.getActiveBpmProcessDefinitions();
            issueProcessInfos = new IssueProcessInfos(arr);
            issueProcessInfos.processes = issueProcessInfos.processes.stream()
                    .filter(s -> !s.status.equals("1") || activeProcesses.stream().anyMatch(a -> s.name.equals(a.getProcessName()) && s.version.equals(a.getVersion())))
                    .collect(Collectors.toList());
        }
        return issueProcessInfos;
    }

}