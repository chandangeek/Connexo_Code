/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.rest.resource;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.bpm.BpmProcessDefinition;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.issue.rest.request.BulkIssueRequest;
import com.elster.jupiter.issue.rest.request.CloseIssueRequest;
import com.elster.jupiter.issue.rest.request.EntityReference;
import com.elster.jupiter.issue.rest.resource.IssueResourceHelper;
import com.elster.jupiter.issue.rest.resource.StandardParametersBean;
import com.elster.jupiter.issue.rest.response.ActionInfo;
import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueActionType;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.streams.Functions;
import com.energyict.mdc.device.data.QueueMessage;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionQueueMessage;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.RescheduleConnectionTaskQueueMessage;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.entity.IssueDataCollection;
import com.energyict.mdc.issue.datacollection.rest.IssueProcessInfos;
import com.energyict.mdc.issue.datacollection.rest.ModuleConstants;
import com.energyict.mdc.issue.datacollection.rest.i18n.DataCollectionIssueTranslationKeys;
import com.energyict.mdc.issue.datacollection.rest.i18n.MessageSeeds;
import com.energyict.mdc.issue.datacollection.rest.response.DataCollectionIssueInfoFactory;

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
    private final MessageService messageService;
    private final AppService appService;
    private final JsonService jsonService;
    private final IssueDataCollectionService issueDataCollectionService;
    private final DataCollectionIssueInfoFactory issuesInfoFactory;
    private final IssueResourceHelper issueResourceHelper;
    private final ExceptionFactory exceptionFactory;
    private final BpmService bpmService;
    private final ConcurrentModificationExceptionFactory conflictFactory;


    @Inject
    public IssueResource(IssueService issueService, MessageService messageService, AppService appService, JsonService jsonService, IssueDataCollectionService issueDataCollectionService, DataCollectionIssueInfoFactory dataCollectionIssuesInfoFactory, IssueResourceHelper issueResourceHelper, ExceptionFactory exceptionFactory, ConcurrentModificationExceptionFactory conflictFactory, BpmService bpmService) {
        this.issueService = issueService;
        this.messageService = messageService;
        this.issueDataCollectionService = issueDataCollectionService;
        this.issuesInfoFactory = dataCollectionIssuesInfoFactory;
        this.issueResourceHelper = issueResourceHelper;
        this.exceptionFactory = exceptionFactory;
        this.appService = appService;
        this.jsonService = jsonService;
        this.conflictFactory = conflictFactory;
        this.bpmService = bpmService;
    }

    @GET @Transactional
    @Path("/{" + ID + "}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE, Privileges.Constants.ASSIGN_ISSUE, Privileges.Constants.CLOSE_ISSUE, Privileges.Constants.COMMENT_ISSUE, Privileges.Constants.ACTION_ISSUE})
    public Response getIssueById(@PathParam(ID) long id) {
        Optional<? extends IssueDataCollection> issue = getIssueDataCollectionService().findIssue(id);
        return issue.map(i -> entity(issuesInfoFactory.asInfo(i, DeviceInfo.class)).build())
                    .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    @GET @Transactional
    @Path("/{" + ID + "}/processes")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE, Privileges.Constants.ASSIGN_ISSUE, Privileges.Constants.CLOSE_ISSUE, Privileges.Constants.COMMENT_ISSUE, Privileges.Constants.ACTION_ISSUE})
    public IssueProcessInfos getTimeine(@PathParam(ID) long id, @BeanParam StandardParametersBean params, @HeaderParam("Authorization") String auth) {
        String jsonContent;
        IssueProcessInfos issueProcessInfos = new IssueProcessInfos();
        JSONArray arr = null;
        if(params.get("variableid") != null && params.get("variablevalue") != null ) {
            try {
                String rest = "/rest/tasks/allprocesses?";
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

    @PUT @Transactional
    @Path("/retrycomm")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ACTION_ISSUE)
    public Response retryCommunicationIssues(BulkIssueRequest request, @Context SecurityContext securityContext, @BeanParam JsonQueryFilter filter) throws Exception {
        verifyAppServerExistsOrThrowException(CommunicationTaskService.COMMUNICATION_RESCHEDULER_QUEUE_DESTINATION);
        ActionInfo response = new ActionInfo();
        return queueCommunicationBulkAction(getIssueProvider(request, filter).apply(response), ModuleConstants.ACTION_CLASS_RETRY_COMMUNICATION, response);
    }

    @PUT @Transactional
    @Path("/retrycommnow")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ACTION_ISSUE)
    public Response retryCommunicationNowIssues(BulkIssueRequest request, @Context SecurityContext securityContext, @BeanParam JsonQueryFilter filter) throws Exception {
        verifyAppServerExistsOrThrowException(CommunicationTaskService.COMMUNICATION_RESCHEDULER_QUEUE_DESTINATION);
        ActionInfo response = new ActionInfo();
        return queueCommunicationBulkAction(getIssueProvider(request, filter).apply(response), ModuleConstants.ACTION_CLASS_RETRY_COMMUNICATION_NOW, response);
    }

    @PUT @Transactional
    @Path("/retryconn")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ACTION_ISSUE)
    public Response retryConnectionIssues(BulkIssueRequest request, @Context SecurityContext securityContext, @BeanParam JsonQueryFilter filter) throws Exception {
        verifyAppServerExistsOrThrowException(ConnectionTaskService.CONNECTION_RESCHEDULER_QUEUE_DESTINATION);
        ActionInfo response = new ActionInfo();
        return queueConnectionBulkAction(getIssueProvider(request, filter).apply(response), response);
    }

    @PUT @Transactional
    @Path("/close")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.CLOSE_ISSUE)
    @Deprecated
    public Response closeIssues(CloseIssueRequest request, @Context SecurityContext securityContext, @BeanParam JsonQueryFilter filter) {
        /* TODO this method should be refactored when FE implements dynamic actions for bulk operations */
        User performer = (User) securityContext.getUserPrincipal();
        Function<ActionInfo, List<? extends Issue>> issueProvider;
        if (request.allIssues) {
            issueProvider = bulkResults -> getIssuesForBulk(filter);
        } else {
            issueProvider = bulkResult -> getUserSelectedIssues(request, bulkResult, false);
        }
        return entity(doBulkClose(request, performer, issueProvider)).build();
    }

    private Function<ActionInfo, List<? extends IssueDataCollection>> getIssueProvider(BulkIssueRequest request, JsonQueryFilter filter) {
        Function<ActionInfo, List<? extends IssueDataCollection>> issueProvider;
        if (request.allIssues) {
            issueProvider = bulkResults -> getIssuesForBulk(filter);
        } else {
            issueProvider = bulkResult -> getUserSelectedIssues(request, bulkResult, true);
        }
        return issueProvider;
    }

    private List<? extends IssueDataCollection> getIssuesForBulk(JsonQueryFilter filter) {
        return issueService.findIssues(issueResourceHelper.buildFilterFromQueryParameters(filter), EndDevice.class, User.class, IssueReason.class, IssueStatus.class, IssueType.class).stream()
                .map(issue -> {
                    if (issue.getStatus().isHistorical()) {
                        return issueDataCollectionService.findHistoricalIssue(issue.getId());
                    } else {
                        return issueDataCollectionService.findOpenIssue(issue.getId());
                    }
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Response queueConnectionBulkAction(List<? extends IssueDataCollection> issues, ActionInfo response) throws Exception {
        Optional<DestinationSpec> destinationSpec = messageService.getDestinationSpec(ConnectionTaskService.CONNECTION_RESCHEDULER_QUEUE_DESTINATION);
        if (destinationSpec.isPresent()) {

            List<IssueDataCollection> issuesToRetry = issues.stream().filter(is -> isActionApplicable(is, response, ModuleConstants.ACTION_CLASS_RETRY_CONNECTION)).collect(Collectors.toList());
            issuesToRetry.stream().map(IssueDataCollection::getConnectionTask)
                    .flatMap(Functions.asStream())
                    .forEach(t -> processMessagePost(new RescheduleConnectionTaskQueueMessage(t.getId(), "scheduleNow"), destinationSpec.get()));
            issuesToRetry.stream()
                    .forEach(issue -> {
                        issue.setStatus(issueService.findStatus(IssueStatus.IN_PROGRESS).get());
                        issue.update();
                        response.addSuccess(issue.getId());
                    });
            return entity(response).build();
        } else {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_MESSAGE_QUEUE);
        }
    }

    private Response queueCommunicationBulkAction(List<? extends IssueDataCollection> issues, String actionClassName, ActionInfo response) throws Exception {
        Optional<DestinationSpec> destinationSpec = messageService.getDestinationSpec(CommunicationTaskService.COMMUNICATION_RESCHEDULER_QUEUE_DESTINATION);
        if (destinationSpec.isPresent()) {
            List<IssueDataCollection> issuesToRetry = issues.stream().filter(is -> isActionApplicable(is, response, actionClassName)).collect(Collectors.toList());
            issuesToRetry.stream().map(IssueDataCollection::getCommunicationTask)
                    .flatMap(Functions.asStream())
                    .forEach(t -> processMessagePost(new ComTaskExecutionQueueMessage
                            (t.getId(), ModuleConstants.ACTION_CLASS_RETRY_COMMUNICATION.equals(actionClassName) ? "scheduleNow" : "runNow"), destinationSpec.get()));
            issuesToRetry.stream()
                    .forEach(issue -> {
                        issue.setStatus(issueService.findStatus(IssueStatus.IN_PROGRESS).get());
                        issue.update();
                        response.addSuccess(issue.getId());
                    });
            return entity(response).build();
        } else {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_MESSAGE_QUEUE);
        }
    }

    private boolean isActionApplicable(Issue issue, ActionInfo response, String actionClassName) {
        if (issueResourceHelper.getListOfAvailableIssueActionsTypes(issue).stream()
                .map(IssueActionType::getClassName)
                .anyMatch(s -> s.equals(actionClassName))) {
            return true;
        }
        response.addFail(getThesaurus().getFormat(DataCollectionIssueTranslationKeys.RETRY_NOT_SUPPORTED).format(), issue.getId(), issue.getTitle());
        return false;
    }

    private void processMessagePost(QueueMessage message, DestinationSpec destinationSpec) {
        String json = jsonService.serialize(message);
        destinationSpec.message(json).send();
    }

    private void verifyAppServerExistsOrThrowException(String destinationName) {
        if (!verifyAppServerExists(destinationName)) {
            throw exceptionFactory.newException(MessageSeeds.NO_APPSERVER);
        }
    }

    private boolean verifyAppServerExists(String destinationName) {
        return appService.findAppServers().stream().
                filter(AppServer::isActive).
                flatMap(server -> server.getSubscriberExecutionSpecs().stream()).
                map(execSpec -> execSpec.getSubscriberSpec().getDestination()).
                filter(DestinationSpec::isActive).
                filter(spec -> !spec.getSubscribers().isEmpty()).
                anyMatch(spec -> destinationName.equals(spec.getName()));
    }

    private List<? extends IssueDataCollection> getUserSelectedIssues(BulkIssueRequest request, ActionInfo bulkResult, boolean isBulkRetry) {
        List<IssueDataCollection> issuesForBulk = new ArrayList<>(request.issues.size());
        for (EntityReference issueRef : request.issues) {
            IssueDataCollection issue = getIssueDataCollectionService().findOpenIssue(issueRef.getId()).orElse(null);
            if (issue == null) {
                issue = getIssueDataCollectionService().findHistoricalIssue(issueRef.getId()).orElse(null);
            }
            if (issue == null) {
                bulkResult.addFail(isBulkRetry ? getThesaurus().getFormat(DataCollectionIssueTranslationKeys.RETRY_NOT_SUPPORTED).format() :
                  getThesaurus().getFormat(DataCollectionIssueTranslationKeys.ISSUE_DOES_NOT_EXIST).format(), issueRef.getId(), "Issue (id = " + issueRef.getId() + ")");
            } else {
                issuesForBulk.add(issue);
            }
        }
        return issuesForBulk;
    }

    private ActionInfo doBulkClose(CloseIssueRequest request, User performer, Function<ActionInfo, List<? extends Issue>> issueProvider) {
        ActionInfo response = new ActionInfo();
        Optional<IssueStatus> status = getIssueService().findStatus(request.status);
        if (status.isPresent() && status.get().isHistorical()) {
            for (Issue issue : issueProvider.apply(response)) {
                if (issue.getStatus().isHistorical()) {
                    response.addFail(getThesaurus().getFormat(DataCollectionIssueTranslationKeys.ISSUE_ALREADY_CLOSED).format(), issue.getId(), issue.getTitle());
                } else {
                    issue.addComment(request.comment, performer);
                    if (issue instanceof OpenIssue) {
                        ((OpenIssue) issue).close(status.get());
                    } else {
                        // user set both open and close statuses in filter
                        getIssueDataCollectionService().findOpenIssue(issue.getId()).ifPresent(
                                openIssue -> openIssue.close(status.get())
                        );
                    }
                    response.addSuccess(issue.getId());
                }
            }
        } else {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        return response;
    }

}
