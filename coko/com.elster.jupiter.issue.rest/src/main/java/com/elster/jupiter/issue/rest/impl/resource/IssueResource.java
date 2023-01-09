/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.issue.rest.impl.resource;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.rest.MessageSeeds;
import com.elster.jupiter.issue.rest.request.AddIssueRequest;
import com.elster.jupiter.issue.rest.request.AssignIssueRequest;
import com.elster.jupiter.issue.rest.request.AssignSingleIssueRequest;
import com.elster.jupiter.issue.rest.request.BulkAddIssueRequest;
import com.elster.jupiter.issue.rest.request.BulkIssueRequest;
import com.elster.jupiter.issue.rest.request.BulkSnoozeRequest;
import com.elster.jupiter.issue.rest.request.CloseBulkIssueRequest;
import com.elster.jupiter.issue.rest.request.CloseIssueRequest;
import com.elster.jupiter.issue.rest.request.CreateCommentRequest;
import com.elster.jupiter.issue.rest.request.EntityReference;
import com.elster.jupiter.issue.rest.request.PerformActionRequest;
import com.elster.jupiter.issue.rest.request.SetPriorityIssueRequest;
import com.elster.jupiter.issue.rest.request.SingleSnoozeRequest;
import com.elster.jupiter.issue.rest.resource.IssueResourceHelper;
import com.elster.jupiter.issue.rest.resource.IssueRestModuleConst;
import com.elster.jupiter.issue.rest.resource.StandardParametersBean;
import com.elster.jupiter.issue.rest.response.ActionInfo;
import com.elster.jupiter.issue.rest.response.IssueInfoFactory;
import com.elster.jupiter.issue.rest.response.cep.IssueActionTypeInfo;
import com.elster.jupiter.issue.rest.response.issue.IssueInfo;
import com.elster.jupiter.issue.rest.response.issue.IssueInfoFactoryService;
import com.elster.jupiter.issue.rest.transactions.AssignIssueTransaction;
import com.elster.jupiter.issue.rest.transactions.AssignSingleIssueTransaction;
import com.elster.jupiter.issue.rest.transactions.AssignToMeSingleIssueTransaction;
import com.elster.jupiter.issue.rest.transactions.BulkSnoozeTransaction;
import com.elster.jupiter.issue.rest.transactions.SingleSnoozeTransaction;
import com.elster.jupiter.issue.rest.transactions.UnassignSingleIssueTransaction;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.issue.share.IssueActionResult;
import com.elster.jupiter.issue.share.IssueFilter;
import com.elster.jupiter.issue.share.IssueGroupInfo;
import com.elster.jupiter.issue.share.IssueProvider;
import com.elster.jupiter.issue.share.IssueResourceUtility;
import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.LocationService;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Order;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.elster.jupiter.issue.rest.request.RequestHelper.ID;
import static com.elster.jupiter.issue.rest.request.RequestHelper.KEY;
import static com.elster.jupiter.issue.rest.request.RequestHelper.LIMIT;
import static com.elster.jupiter.issue.rest.request.RequestHelper.START;
import static com.elster.jupiter.issue.rest.response.ResponseHelper.entity;
import static com.elster.jupiter.issue.rest.response.ResponseHelper.entityAccepted;

@Path("/issues")
public class IssueResource extends BaseResource {

    private final IssueResourceHelper issueResourceHelper;
    private final IssueInfoFactory issueInfoFactory;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final IssueInfoFactoryService issueInfoFactoryService;
    private final TransactionService transactionService;
    private final IssueService issueService;
    private final LocationService locationService;
    private final Clock clock;
    private final IssueResourceUtility issueResourceUtility;
    private final EventService eventService;

    private static final int ISSUE_BATCH_SIZE = 1000;

    @Inject
    public IssueResource(IssueResourceHelper issueResourceHelper, IssueInfoFactory issueInfoFactory, ConcurrentModificationExceptionFactory conflictFactory, IssueInfoFactoryService issueInfoFactoryService, IssueService issueService,
                         TransactionService transactionService, LocationService locationService, Clock clock, IssueResourceUtility issueResourceUtility, EventService eventService) {
        this.issueResourceHelper = issueResourceHelper;
        this.issueInfoFactory = issueInfoFactory;
        this.conflictFactory = conflictFactory;
        this.issueInfoFactoryService = issueInfoFactoryService;
        this.issueService = issueService;
        this.transactionService = transactionService;
        this.locationService = locationService;
        this.clock = clock;
        this.issueResourceUtility = issueResourceUtility;
        this.eventService = eventService;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE, Privileges.Constants.ASSIGN_ISSUE, Privileges.Constants.CLOSE_ISSUE, Privileges.Constants.COMMENT_ISSUE, Privileges.Constants.ACTION_ISSUE})
    public PagedInfoList getAllIssues(@BeanParam StandardParametersBean params, @BeanParam JsonQueryParameters queryParams, @BeanParam JsonQueryFilter filter) {
        validateMandatory(params, START, LIMIT);
        IssueFilter issueFilter = issueResourceHelper.buildFilterFromQueryParameters(filter);
        Finder<? extends Issue> finder = getIssueService().findIssues(issueResourceHelper.buildFilterFromQueryParameters(filter), EndDevice.class);
        if (queryParams.getStart().isPresent() && queryParams.getLimit().isPresent()) {
            finder.paged(queryParams.getStart().get(), queryParams.getLimit().get());
        }
        addSorting(finder, params);
        List<? extends Issue> issues = finder.find();
        List<IssueInfo> issueInfos = new ArrayList<>();
        for (Issue baseIssue : issues) {
            for (IssueProvider issueProvider : getIssueService().getIssueProviders(baseIssue.getReason().getIssueType().getKey())) {
                Optional<? extends Issue> issueRef = issueProvider.findIssue(baseIssue.getId());
                issueRef.ifPresent(issue -> {
                    IssueInfo issueInfo = (IssueInfo) issueInfoFactoryService.getInfoFactoryFor(issue).from(issue);
                    if (!issueFilter.getUsagePoints().isEmpty()) {
                        issueFilter.getUsagePoints().stream().forEach(usagePoint -> {
                            try {
                                if (usagePoint.getId() == issueInfo.usagePointInfo.getId()) {
                                    issueInfos.add(issueInfo);
                                }
                            } catch (NullPointerException e) {
                            }
                        });
                    } else {
                        issueInfos.add(issueInfo);
                    }
                });
            }
        }
        return PagedInfoList.fromPagedList("data", issueInfos, queryParams);
    }

    @GET
    @Path("/count")
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE, Privileges.Constants.ASSIGN_ISSUE, Privileges.Constants.CLOSE_ISSUE, Privileges.Constants.COMMENT_ISSUE, Privileges.Constants.ACTION_ISSUE})
    public Response getAllIssuesCount(@BeanParam JsonQueryFilter filter) {
        return entity(getIssueService().findIssues(issueResourceHelper.buildFilterFromQueryParameters(filter)).count()).build();
    }

    @GET
    @Transactional
    @Path("/{id}")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE, Privileges.Constants.ASSIGN_ISSUE, Privileges.Constants.CLOSE_ISSUE, Privileges.Constants.COMMENT_ISSUE, Privileges.Constants.ACTION_ISSUE})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public IssueInfo getIssueById(@PathParam("id") long id) {
        Optional<? extends Issue> issue = getIssueService().findIssue(id);
        return issue.map(isu -> {
            for (IssueProvider issueProvider : getIssueService().getIssueProviders()) {
                Optional<? extends Issue> issueRef = issueProvider.findIssue(isu.getId());
                if (issueRef.isPresent()) {
                    return (IssueInfo) issueInfoFactoryService.getInfoFactoryFor(issueRef.get())
                            .from(issueRef.get());
                }
            }
            return null;
        }).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    @GET
    @Transactional
    @Path("/{id}/comments")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE, Privileges.Constants.ASSIGN_ISSUE, Privileges.Constants.CLOSE_ISSUE, Privileges.Constants.COMMENT_ISSUE, Privileges.Constants.ACTION_ISSUE})
    public PagedInfoList getComments(@PathParam(ID) long id, @BeanParam JsonQueryParameters queryParameters) {
        Issue issue = issueService.findIssue(id)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return PagedInfoList.fromCompleteList("comments", issueResourceHelper.getIssueComments(issue), queryParameters);
    }

    @POST
    @Transactional
    @Path("/{id}/comments")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ACTION_ISSUE)
    public Response postComment(CreateCommentRequest request, @PathParam(ID) long id, @Context SecurityContext securityContext) {
        Issue issue = issueService.findIssue(id)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return Response.ok(issueResourceHelper.postComment(issue, request, securityContext))
                .status(Response.Status.CREATED)
                .build();
    }

    @DELETE
    @Path("/{id}/comments/{commentId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ACTION_ISSUE)
    public Response removeComment(CreateCommentRequest request, @PathParam(ID) long id, @PathParam("commentId") long commentId, @Context SecurityContext securityContext) {
        Issue issue = issueService.findIssue(id)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        try (TransactionContext context = transactionService.getContext()) {
            issueResourceHelper.removeComment(issue, commentId, request, securityContext);
            context.commit();
        }
        return Response.ok().build();
    }

    @PUT
    @Path("/{id}/comments/{commentId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ACTION_ISSUE)
    public Response editComment(CreateCommentRequest request, @PathParam(ID) long id, @PathParam("commentId") long commentId, @Context SecurityContext securityContext) {
        Issue issue = issueService.findIssue(id)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        try (TransactionContext context = transactionService.getContext()) {
            issueResourceHelper.editComment(issue, commentId, request, securityContext);
            context.commit();
        }
        return Response.ok().build();
    }

    @PUT
    @Path("/assigntome/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ACTION_ISSUE)
    public Response postAssignToMe(PerformActionRequest request, @PathParam(ID) long id, @Context SecurityContext securityContext) {
        issueService.findAndLockIssueByIdAndVersion(id, request.issue.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(request.issue.title)
                        .withActualVersion(() -> issueService.findIssue(id)
                                .map(Issue::getVersion)
                                .orElse(null))
                        .supplier());

        User performer = (User) securityContext.getUserPrincipal();
        Function<ActionInfo, Issue> issueProvider = result -> getIssue(id, result);
        ActionInfo info = transactionService.execute(new AssignToMeSingleIssueTransaction(performer, issueProvider, getThesaurus()));
        return entity(info).build();
    }

    @PUT
    @Path("/unassign/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ACTION_ISSUE)
    public Response postUnassign(PerformActionRequest request, @PathParam(ID) long id) {
        issueService.findAndLockIssueByIdAndVersion(id, request.issue.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(request.issue.title)
                        .withActualVersion(() -> issueService.findIssue(id)
                                .map(Issue::getVersion)
                                .orElse(null))
                        .supplier());

        Function<ActionInfo, Issue> issueProvider = result -> getIssue(id, result);
        ActionInfo info = transactionService.execute(new UnassignSingleIssueTransaction(issueProvider, getThesaurus()));
        return entity(info).build();
    }

    @GET
    @Transactional
    @Path("/{id}/actions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE, Privileges.Constants.ASSIGN_ISSUE, Privileges.Constants.CLOSE_ISSUE, Privileges.Constants.COMMENT_ISSUE, Privileges.Constants.ACTION_ISSUE})
    public PagedInfoList getActions(@PathParam(ID) long id, @BeanParam JsonQueryParameters queryParameters) {
        Issue issue = issueService.findIssue(id)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        List<IssueActionTypeInfo> issueActions = new ArrayList<>();
        for (IssueProvider issueProvider : issueService.getIssueProviders()) {
            Optional<? extends Issue> issueRef = issue.getStatus().isHistorical() ?
                    issueProvider.getHistoricalIssue((HistoricalIssue) issue) : issueProvider.getOpenIssue((OpenIssue) issue);
            if (issueRef.isPresent()) {
                issueActions = issueResourceHelper.getListOfAvailableIssueActions(issueRef.get());
            }
        }
        return PagedInfoList.fromCompleteList("issueActions", issueActions, queryParameters);
    }

    @GET
    @Transactional
    @Path("/{id}/actions/{key}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE, Privileges.Constants.ASSIGN_ISSUE, Privileges.Constants.CLOSE_ISSUE, Privileges.Constants.COMMENT_ISSUE, Privileges.Constants.ACTION_ISSUE})
    public Response getActionTypeById(@PathParam(ID) long id, @PathParam(KEY) long actionId) {
        Issue issue = issueService.findIssue(id)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        issueService.findIssue(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return Response.ok(issueResourceHelper.getIssueActionById(issue, actionId)).build();
    }

    @PUT
    @Transactional
    @Path("/{id}/actions/{key}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ACTION_ISSUE)
    public Response performAction(PerformActionRequest request, @PathParam(ID) long id, @PathParam(KEY) long actionId) {
        Issue baseIssue = issueService.findAndLockIssueByIdAndVersion(id, request.issue.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(request.issue.title)
                        .withActualVersion(() -> issueService.findIssue(id)
                                .map(Issue::getVersion)
                                .orElse(null))
                        .supplier());
        request.id = actionId;

        for (IssueProvider issueProvider : issueService.getIssueProviders()) {
            Optional<? extends Issue> issueRef = baseIssue.getStatus().isHistorical() ?
                    issueProvider.getHistoricalIssue((HistoricalIssue) baseIssue) : issueProvider.getOpenIssue((OpenIssue) baseIssue);
            if (issueRef.isPresent()) {
                IssueActionResult info = issueResourceHelper.performIssueAction(issueRef.get(), request);
                return Response.ok(info).build();
            }
        }
        return Response.ok(issueResourceHelper.performIssueAction(baseIssue, request)).build();
    }


    @PUT
    @Path("/assignissue")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ACTION_ISSUE)
    public Response performAssignTo(AssignSingleIssueRequest request, @Context SecurityContext securityContext) {
        User performer = (User) securityContext.getUserPrincipal();
        Function<ActionInfo, Issue> issueProvider = result -> getIssue(request.issue.getId(), result);
        ActionInfo info = transactionService.execute(new AssignSingleIssueTransaction(request, performer, issueProvider, getThesaurus()));
        return entity(info).build();
    }


    @GET
    @Path("/groupedlist")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE, Privileges.Constants.ASSIGN_ISSUE, Privileges.Constants.CLOSE_ISSUE, Privileges.Constants.COMMENT_ISSUE, Privileges.Constants.ACTION_ISSUE})
    public PagedInfoList getGroupedList(@BeanParam StandardParametersBean params, @BeanParam JsonQueryParameters queryParameters,
                                        @BeanParam JsonQueryFilter filter, @HeaderParam("X-CONNEXO-APPLICATION-NAME") String appKey) {

        Finder<? extends Issue> finder = getIssueService().findIssues(issueResourceHelper.buildFilterFromQueryParameters(filter), EndDevice.class);
        List<? extends Issue> issues = finder.find();
        String value = filter.getPropertyList("field").get(0).substring(1, filter.getPropertyList("field").get(0).length() - 1);
        List<IssueGroupInfo> infos = issueResourceUtility.getIssueGroupList(issues, value);

        if (filter.getString(IssueRestModuleConst.FIELD).equals(IssueRestModuleConst.LOCATION)) {
            // replace location id with location name for group name
            List<IssueGroupInfo> replacedInfos = new LinkedList<>();
            for (IssueGroupInfo info : infos) {
                String groupName = info.description;
                if (isNumericValue(groupName)) {
                    Optional<Location> location = locationService.findLocationById(Long.valueOf(groupName));
                    if (location.isPresent()) {
                        groupName = location.get().toString();
                    }
                }
                replacedInfos.add(new IssueGroupInfo(info.id, groupName, info.number));
            }
            infos = replacedInfos;
        }

        return PagedInfoList.fromPagedList("issueGroups", infos, queryParameters);
    }

    @PUT
    @Path("/assign")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ASSIGN_ISSUE)
    @Deprecated
    public Response assignIssues(AssignIssueRequest request, @BeanParam JsonQueryFilter filter, @Context SecurityContext securityContext) {
        User performer = (User) securityContext.getUserPrincipal();
        ActionInfo info = transactionService.execute(new AssignIssueTransaction(request, performer, getIssueProvider(request, filter)));
        return entity(info).build();
    }

    @PUT
    @Path("/snooze")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ACTION_ISSUE, Privileges.Constants.CLOSE_ISSUE})
    public Response snooze(SingleSnoozeRequest request, @Context SecurityContext securityContext) {
        User performer = (User) securityContext.getUserPrincipal();
        Function<ActionInfo, Issue> issueProvider = result -> getIssue(request.issue.getId(), result);

        if (Instant.ofEpochMilli(request.snoozeDateTime).isBefore(Instant.now(clock))) {
            throw new LocalizedFieldValidationException(MessageSeeds.SNOOZE_TIME_BEFORE_CURRENT_TIME, "until");
        } else {
            ActionInfo info = transactionService.execute(new SingleSnoozeTransaction(request, performer, issueProvider, getThesaurus()));
            return entity(info).build();
        }
    }

    @PUT
    @Path("/bulksnooze")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ASSIGN_ISSUE, Privileges.Constants.CLOSE_ISSUE})
    @Deprecated
    public Response bulkSnooze(BulkSnoozeRequest request, @Context SecurityContext securityContext, @BeanParam JsonQueryFilter filter) {
        User performer = (User) securityContext.getUserPrincipal();
        ActionInfo info = transactionService.execute(new BulkSnoozeTransaction(request, performer, getIssueProvider(request, filter), getThesaurus(), clock));
        return entity(info).build();
    }

    @PUT
    @Transactional
    @Path("/setpriority")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ACTION_ISSUE)
    public Response setPriority(SetPriorityIssueRequest request, @BeanParam JsonQueryFilter filter) {
        ActionInfo info = doBulkSetPriority(request, getIssueProvider(request, filter));
        return entity(info).build();
    }

    @PUT
    @Transactional
    @Path("/close")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.CLOSE_ISSUE)
    @Deprecated
    public Response closeIssues(CloseIssueRequest request, @Context SecurityContext securityContext, @BeanParam JsonQueryFilter filter) {
        /* TODO this method should be refactored when FE implements dynamic actions for bulk operations */
        User performer = (User) securityContext.getUserPrincipal();
        ActionInfo response = new ActionInfo();
        List<OpenIssue> issues = getOpenIssuesListToClose(request, response, getIssueProvider(request, filter));
        if (issues.size() >= ISSUE_BATCH_SIZE) {
            for (List<OpenIssue> batch : splitList(issues)) {
                eventService.postEvent("com/elster/jupiter/issues/BULK_CLOSE", CloseBulkIssueRequest.getRequest(batch, request.status, request.comment, performer));
            }
            return entityAccepted(response).build();
        } else {
            doBulkClose(request, response, performer, issues);
            return entity(response).build();
        }
    }

    private Function<ActionInfo, List<? extends Issue>> getIssueProvider(BulkIssueRequest request, JsonQueryFilter filter) {
        return request.allIssues ? bulkResults -> getIssuesForBulk(filter) : bulkResult -> getUserSelectedIssues(request, bulkResult);
    }

    @POST
    @Transactional
    @Path("/bulkadd")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.CREATE_ISSUE)
    public Response addIssues(BulkAddIssueRequest request) {
        ActionInfo response = new ActionInfo();
        for (AddIssueRequest addIssueRequest : request.getIssues()) {
            response.addSuccess(issueResourceHelper.createNewIssue(addIssueRequest).getId());
        }
        return entity(response).build();
    }

    private List<? extends Issue> getIssuesForBulk(JsonQueryFilter filter) {
        return getIssueService().findIssues(issueResourceHelper.buildFilterFromQueryParameters(filter)).find().stream()
                .map(issue -> getIssueService().wrapOpenOrHistorical(issue)).collect(Collectors.toList());
    }

    private List<? extends Issue> getUserSelectedIssues(BulkIssueRequest request, ActionInfo bulkResult) {
        List<Issue> issuesForBulk = new ArrayList<>(request.issues.size());
        for (EntityReference issueRef : request.issues) {
            Optional<? extends Issue> issue = issueService.findIssue(issueRef.getId());
            if (issue.isPresent()) {
                issuesForBulk.add(issue.get());
            } else {
                bulkResult.addFail(getThesaurus().getFormat(MessageSeeds.ISSUE_DOES_NOT_EXIST)
                        .format(), issueRef.getId(), "Issue (id = " + issueRef.getId() + ")");
            }
        }
        return issuesForBulk;
    }

    private Issue getIssue(Long id, ActionInfo result) {
        Issue issue = issueService.findIssue(id).orElse(null);
        if (issue == null) {
            result.addFail(getThesaurus().getFormat(MessageSeeds.ISSUE_DOES_NOT_EXIST)
                    .format(), id, "Issue (id = " + id + ")");
        }
        return issue;
    }

    private Class<? extends Issue> getQueryApiClass(JsonQueryFilter filter) {
        List<IssueStatus> statuses = filter.hasProperty(IssueRestModuleConst.STATUS)
                ? filter.getStringList(IssueRestModuleConst.STATUS)
                .stream()
                .map(s -> issueService.findStatus(s).get())
                .collect(Collectors.toList())
                : Collections.emptyList();
        if (statuses.isEmpty()) {
            return Issue.class;
        }
        if (statuses.stream().noneMatch(IssueStatus::isHistorical)) {
            return OpenIssue.class;
        }
        if (statuses.stream().allMatch(IssueStatus::isHistorical)) {
            return HistoricalIssue.class;
        }
        return Issue.class;
    }

    private void addSorting(Finder<? extends Issue> finder, StandardParametersBean parameters) {
        Order[] orders = parameters.getOrder("");
        for (Order order : orders) {
            finder.sorted(order);
        }
        finder.sorted("id", false);
    }

    private ActionInfo doBulkSetPriority(SetPriorityIssueRequest request, Function<ActionInfo, List<? extends Issue>> issueProvider) {
        ActionInfo response = new ActionInfo();
        for (Issue issue : issueProvider.apply(response)) {
            issue.setPriority(Priority.fromStringValue(request.priority));
            issue.update();
            response.addSuccess(issue.getId());
        }
        return response;
    }

    private List<OpenIssue> getOpenIssuesListToClose(CloseIssueRequest request, ActionInfo response, Function<ActionInfo, List<? extends Issue>> issueProvider) {
        Optional<IssueStatus> status = issueService.findStatus(request.status);
        List<OpenIssue> issues = new ArrayList<>();
        if (status.isPresent() && status.get().isHistorical()) {
            for (Issue issue : issueProvider.apply(response)) {
                if (issue.getStatus().isHistorical()) {
                    response.addFail(getThesaurus().getFormat(MessageSeeds.ISSUE_ALREADY_CLOSED)
                            .format(), issue.getId(), issue.getTitle());
                } else {
                    OpenIssue current = null;
                    if (issue instanceof OpenIssue) {
                        current = (OpenIssue) issue;
                    } else {
                        // user set both open and close statuses in filter
                        Optional<OpenIssue> optionalIssue = issueService.findOpenIssue(issue.getId());
                        if (optionalIssue.isPresent()) {
                            current = optionalIssue.get();
                        }
                    }
                    if (current != null) {
                        issues.add(current);
                    }
                }
            }
        } else {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        return issues;
    }

    private void doBulkClose(CloseIssueRequest request, ActionInfo response, User performer, List<OpenIssue> issues) {
        Optional<IssueStatus> status = issueService.findStatus(request.status);
        for (OpenIssue issue : issues) {
            issue.addComment(request.comment, performer);
            issue.close(status.get());
            response.addSuccess(issue.getId());
        }
    }

    private List<List<OpenIssue>> splitList(List<OpenIssue> issues) {
        List<List<OpenIssue>> batch = new ArrayList<>();
        final int size = issues.size();
        for (int i = 0; i < size; i += ISSUE_BATCH_SIZE) {
            batch.add(issues.subList(i, Math.min(size, i + ISSUE_BATCH_SIZE)));
        }
        return batch;
    }

    private boolean isNumericValue(String id) {
        try {
            Long.parseLong(id);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}


