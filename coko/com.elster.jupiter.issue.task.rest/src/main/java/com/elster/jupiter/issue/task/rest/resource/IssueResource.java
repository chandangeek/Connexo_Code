/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.task.rest.resource;

import com.elster.jupiter.bpm.BpmProcessDefinition;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.issue.rest.request.BulkIssueRequest;
import com.elster.jupiter.issue.rest.request.EntityReference;
import com.elster.jupiter.issue.rest.resource.IssueResourceHelper;
import com.elster.jupiter.issue.rest.resource.StandardParametersBean;
import com.elster.jupiter.issue.rest.response.ActionInfo;
import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.issue.task.TaskIssueService;
import com.elster.jupiter.issue.task.TaskIssue;
import com.elster.jupiter.issue.task.rest.IssueProcessInfos;
import com.elster.jupiter.issue.task.rest.i18n.TaskIssueTranslationKeys;
import com.elster.jupiter.issue.task.rest.response.TaskIssueInfoFactory;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.users.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.elster.jupiter.issue.rest.request.RequestHelper.ID;
import static com.elster.jupiter.issue.rest.response.ResponseHelper.entity;

@Path("/issues")
public class IssueResource extends BaseResource {

    private final IssueService issueService;
    private final TaskIssueService taskIssueService;
    private final TaskIssueInfoFactory issuesInfoFactory;
    private final IssueResourceHelper issueResourceHelper;
    private final BpmService bpmService;


    @Inject
    public IssueResource(IssueService issueService, TaskIssueService taskIssueService, TaskIssueInfoFactory taskIssuesInfoFactory, IssueResourceHelper issueResourceHelper, BpmService bpmService) {
        this.issueService = issueService;
        this.taskIssueService = taskIssueService;
        this.issuesInfoFactory = taskIssuesInfoFactory;
        this.issueResourceHelper = issueResourceHelper;
        this.bpmService = bpmService;
    }

    @GET
    @Transactional
    @Path("/{" + ID + "}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE, Privileges.Constants.ASSIGN_ISSUE, Privileges.Constants.CLOSE_ISSUE, Privileges.Constants.COMMENT_ISSUE, Privileges.Constants.ACTION_ISSUE})
    public Response getIssueById(@PathParam(ID) long id) {
        Optional<? extends TaskIssue> issue = getTaskIssueService().findIssue(id);
        return issue.map(i -> entity(issuesInfoFactory.asInfo(i, DeviceInfo.class)).build())
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    @GET
    @Transactional
    @Path("/{" + ID + "}/processes")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE, Privileges.Constants.ASSIGN_ISSUE, Privileges.Constants.CLOSE_ISSUE, Privileges.Constants.COMMENT_ISSUE, Privileges.Constants.ACTION_ISSUE})
    public IssueProcessInfos getTimeine(@PathParam(ID) long id, @BeanParam StandardParametersBean params, @HeaderParam("Authorization") String auth) {
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
                        .entity(getThesaurus().getString("error.flow.unavailable", "Cannot connect to Flow; HTTP error {0}."))
                        .build());
            } catch (RuntimeException e) {
                throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE)
                        .entity(String.format(getThesaurus().getString("error.flow.invalid.response", "Invalid response received, please check your Flow version."), e.getMessage()))
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


    private Function<ActionInfo, List<? extends TaskIssue>> getIssueProvider(BulkIssueRequest request, JsonQueryFilter filter) {
        Function<ActionInfo, List<? extends TaskIssue>> issueProvider;
        if (request.allIssues) {
            issueProvider = bulkResults -> getIssuesForBulk(filter);
        } else {
            issueProvider = bulkResult -> getUserSelectedIssues(request, bulkResult, true);
        }
        return issueProvider;
    }

    private List<? extends TaskIssue> getIssuesForBulk(JsonQueryFilter filter) {
        return issueService.findIssues(issueResourceHelper.buildFilterFromQueryParameters(filter), EndDevice.class, User.class, IssueReason.class, IssueStatus.class, IssueType.class).stream()
                .map(issue -> {
                    if (issue.getStatus().isHistorical()) {
                        return taskIssueService.findHistoricalIssue(issue.getId());
                    } else {
                        return taskIssueService.findOpenIssue(issue.getId());
                    }
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private List<? extends TaskIssue> getUserSelectedIssues(BulkIssueRequest request, ActionInfo bulkResult, boolean isBulkRetry) {
        List<TaskIssue> issuesForBulk = new ArrayList<>(request.issues.size());
        for (EntityReference issueRef : request.issues) {
            TaskIssue issue = getTaskIssueService().findOpenIssue(issueRef.getId()).orElse(null);
            if (issue == null) {
                issue = getTaskIssueService().findHistoricalIssue(issueRef.getId()).orElse(null);
            }
            if (issue == null) {
                bulkResult.addFail(isBulkRetry ? getThesaurus().getFormat(TaskIssueTranslationKeys.RETRY_NOT_SUPPORTED).format() :
                        getThesaurus().getFormat(TaskIssueTranslationKeys.ISSUE_DOES_NOT_EXIST).format(), issueRef.getId(), "Issue (id = " + issueRef.getId() + ")");
            } else {
                issuesForBulk.add(issue);
            }
        }
        return issuesForBulk;
    }


}
