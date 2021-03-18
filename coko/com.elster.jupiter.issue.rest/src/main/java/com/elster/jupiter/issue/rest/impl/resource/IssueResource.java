/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.impl.resource;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.issue.rest.MessageSeeds;
import com.elster.jupiter.issue.rest.request.AddIssueRequest;
import com.elster.jupiter.issue.rest.request.AssignIssueRequest;
import com.elster.jupiter.issue.rest.request.AssignSingleIssueRequest;
import com.elster.jupiter.issue.rest.request.BulkAddIssueRequest;
import com.elster.jupiter.issue.rest.request.BulkIssueRequest;
import com.elster.jupiter.issue.rest.request.BulkSnoozeRequest;
import com.elster.jupiter.issue.rest.request.CloseIssueRequest;
import com.elster.jupiter.issue.rest.request.CreateCommentRequest;
import com.elster.jupiter.issue.rest.request.EntityReference;
import com.elster.jupiter.issue.rest.request.PerformActionRequest;
import com.elster.jupiter.issue.rest.request.SetPriorityIssueRequest;
import com.elster.jupiter.issue.rest.request.SingleIssueRequest;
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
import java.util.Comparator;
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

@Path("/issues")
public class IssueResource extends BaseResource {

    private final IssueResourceHelper issueResourceHelper;
    private final IssueInfoFactory issueInfoFactory;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final IssueInfoFactoryService issueInfoFactoryService;
    private final TransactionService transactionService;
    private final LocationService locationService;
    private final Clock clock;
    private final IssueResourceUtility issueResourceUtility;

    @Inject
    public IssueResource(IssueResourceHelper issueResourceHelper, IssueInfoFactory issueInfoFactory, ConcurrentModificationExceptionFactory conflictFactory, IssueInfoFactoryService issueInfoFactoryService,
                         TransactionService transactionService, LocationService locationService, Clock clock, IssueResourceUtility issueResourceUtility) {
        this.issueResourceHelper = issueResourceHelper;
        this.issueInfoFactory = issueInfoFactory;
        this.conflictFactory = conflictFactory;
        this.issueInfoFactoryService = issueInfoFactoryService;
        this.transactionService = transactionService;
        this.locationService = locationService;
        this.clock = clock;
        this.issueResourceUtility = issueResourceUtility;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE, Privileges.Constants.ASSIGN_ISSUE, Privileges.Constants.CLOSE_ISSUE, Privileges.Constants.COMMENT_ISSUE, Privileges.Constants.ACTION_ISSUE})
    public PagedInfoList getAllIssues(@BeanParam StandardParametersBean params, @BeanParam JsonQueryParameters queryParams, @BeanParam JsonQueryFilter filter) {
        validateMandatory(params, START, LIMIT);
        IssueFilter issueFilter = issueResourceHelper.buildFilterFromQueryParameters(filter);
        Finder<? extends Issue> finder = getIssueService().findIssues(issueResourceHelper.buildFilterFromQueryParameters(filter), EndDevice.class);
        //addSorting(finder, params);
        if (queryParams.getStart().isPresent() && queryParams.getLimit().isPresent()) {
            finder.paged(queryParams.getStart().get(), queryParams.getLimit().get());
        }
        List<? extends Issue> issues = finder.find();
        List<IssueInfo> issueInfos = new ArrayList<>();
        for (Issue baseIssue : issues) {
            for (IssueProvider issueProvider : getIssueService().getIssueProviders()) {
                Optional<? extends Issue> issueRef = issueProvider.findIssue(baseIssue.getId());
                issueRef.ifPresent(issue -> {
                    IssueInfo issueInfo = IssueInfo.class.cast(issueInfoFactoryService.getInfoFactoryFor(issue).from(issue));
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
        if (issueInfos != null) {
            // default sort, required if all explicit sorters do not affect the list in any way
            // (ex. sort by priority and all issues have the same priority)
            issueInfos.sort(Comparator.comparing(IssueInfo::getId));
        }
        List<IssueInfo> issueInfosSorted = sortIssues(issueInfos, params);
        return PagedInfoList.fromPagedList("data", issueInfosSorted, queryParams);
    }

    @GET
    @Path("/count")
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE, Privileges.Constants.ASSIGN_ISSUE, Privileges.Constants.CLOSE_ISSUE, Privileges.Constants.COMMENT_ISSUE, Privileges.Constants.ACTION_ISSUE})
    public Response getAllIssuesCount(@BeanParam JsonQueryFilter filter) {
        return Response.ok(getIssueService().findIssues(issueResourceHelper.buildFilterFromQueryParameters(filter)).count()).build();
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
                    return IssueInfo.class.cast(issueInfoFactoryService.getInfoFactoryFor(issueRef.get())
                            .from(issueRef.get()));
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
    public PagedInfoList getComments(@PathParam("id") long id, @BeanParam JsonQueryParameters queryParameters) {
        Issue issue = getIssueService().findIssue(id)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return PagedInfoList.fromCompleteList("comments", issueResourceHelper.getIssueComments(issue), queryParameters);
    }

    @POST
    @Transactional
    @Path("/{id}/comments")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ACTION_ISSUE)
    public Response postComment(@PathParam("id") long id, CreateCommentRequest request, @Context SecurityContext securityContext) {
        Issue issue = getIssueService().findIssue(id)
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
    public Response removeComment(@PathParam("id") long id, @PathParam("commentId") long commentId, CreateCommentRequest request, @Context SecurityContext securityContext) {
        Issue issue = getIssueService().findIssue(id)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        try (TransactionContext context = transactionService.getContext()) {
            issueResourceHelper.removeComment(issue, commentId, request, securityContext);
            context.commit();
        }
        return Response.status(Response.Status.OK).build();
    }

    @PUT
    @Path("/{id}/comments/{commentId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ACTION_ISSUE)
    public Response editComment(@PathParam("id") long id, @PathParam("commentId") long commentId, CreateCommentRequest request, @Context SecurityContext securityContext) {
        Issue issue = getIssueService().findIssue(id)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        try (TransactionContext context = transactionService.getContext()) {
            issueResourceHelper.editComment(issue, commentId, request, securityContext);
            context.commit();
        }
        return Response.status(Response.Status.OK).build();
    }

    @PUT
    @Path("/assigntome/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ACTION_ISSUE)
    public Response postAssignToMe(@PathParam("id") long id, PerformActionRequest request, @Context SecurityContext securityContext) {
        getIssueService().findAndLockIssueByIdAndVersion(id, request.issue.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(request.issue.title)
                        .withActualVersion(() -> getIssueService().findIssue(id)
                                .map(Issue::getVersion)
                                .orElse(null))
                        .supplier());

        User performer = (User) securityContext.getUserPrincipal();
        Function<ActionInfo, Issue> issueProvider = result -> getIssue(id, result);
        ActionInfo info = getTransactionService().execute(new AssignToMeSingleIssueTransaction(performer, issueProvider, getThesaurus()));
        return entity(info).build();
    }

    @PUT
    @Path("/unassign/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ACTION_ISSUE)
    public Response postUnassign(@PathParam("id") long id, PerformActionRequest request, @Context SecurityContext securityContext) {
        getIssueService().findAndLockIssueByIdAndVersion(id, request.issue.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(request.issue.title)
                        .withActualVersion(() -> getIssueService().findIssue(id)
                                .map(Issue::getVersion)
                                .orElse(null))
                        .supplier());

        Function<ActionInfo, Issue> issueProvider = result -> getIssue(id, result);
        ActionInfo info = getTransactionService().execute(new UnassignSingleIssueTransaction(issueProvider, getThesaurus()));
        return entity(info).build();
    }

    @GET
    @Transactional
    @Path("/{" + ID + "}/actions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE, Privileges.Constants.ASSIGN_ISSUE, Privileges.Constants.CLOSE_ISSUE, Privileges.Constants.COMMENT_ISSUE, Privileges.Constants.ACTION_ISSUE})
    public PagedInfoList getActions(@PathParam("id") long id, @BeanParam JsonQueryParameters queryParameters) {
        Issue issue = getIssueService().findIssue(id)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        List<IssueActionTypeInfo> issueActions = new ArrayList<>();
        for (IssueProvider issueProvider : getIssueService().getIssueProviders()) {
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
    @Path("/{" + ID + "}/actions/{" + KEY + "}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE, Privileges.Constants.ASSIGN_ISSUE, Privileges.Constants.CLOSE_ISSUE, Privileges.Constants.COMMENT_ISSUE, Privileges.Constants.ACTION_ISSUE})
    public Response getActionTypeById(@PathParam(ID) long id, @PathParam(KEY) long actionId) {
        Issue issue = getIssueService().findIssue(id)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        getIssueService().findIssue(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return Response.ok(issueResourceHelper.getIssueActionById(issue, actionId)).build();
    }

    @PUT
    @Transactional
    @Path("/{" + ID + "}/actions/{" + KEY + "}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ACTION_ISSUE)
    public Response performAction(@PathParam(ID) long id, @PathParam(KEY) long actionId, PerformActionRequest request) {
        Issue baseIssue = getIssueService().findAndLockIssueByIdAndVersion(id, request.issue.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(request.issue.title)
                        .withActualVersion(() -> getIssueService().findIssue(id)
                                .map(Issue::getVersion)
                                .orElse(null))
                        .supplier());
        request.id = actionId;

        for (IssueProvider issueProvider : getIssueService().getIssueProviders()) {
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
    public Response performAssignTo(@Context SecurityContext securityContext, AssignSingleIssueRequest request) {
        User performer = (User) securityContext.getUserPrincipal();
        Function<ActionInfo, Issue> issueProvider;
        issueProvider = result -> getIssue(request, result);
        ActionInfo info = getTransactionService().execute(new AssignSingleIssueTransaction(request, performer, issueProvider, getThesaurus()));
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
    public Response assignIssues(AssignIssueRequest request, @Context SecurityContext securityContext, @BeanParam JsonQueryFilter filter) {
        /* TODO this method should be refactored when FE implements dynamic actions for bulk operations */
        User performer = (User) securityContext.getUserPrincipal();
        Function<ActionInfo, List<? extends Issue>> issueProvider;
        if (request.allIssues) {
            issueProvider = bulkResults -> getIssuesForBulk(filter);
        } else {
            issueProvider = bulkResult -> getUserSelectedIssues(request, bulkResult);
        }
        ActionInfo info = getTransactionService().execute(new AssignIssueTransaction(request, performer, issueProvider));
        return entity(info).build();
    }

    @PUT
    @Path("/snooze")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ACTION_ISSUE, Privileges.Constants.CLOSE_ISSUE})
    public Response snooze(@Context SecurityContext securityContext, SingleSnoozeRequest request) {
        User performer = (User) securityContext.getUserPrincipal();
        Function<ActionInfo, Issue> issueProvider;
        issueProvider = result -> getIssue(request, result);

        if (Instant.ofEpochMilli(request.snoozeDateTime).isBefore(Instant.now(clock))) {
            throw new LocalizedFieldValidationException(MessageSeeds.SNOOZE_TIME_BEFORE_CURRENT_TIME, "until");
        } else {
            ActionInfo info = getTransactionService().execute(new SingleSnoozeTransaction(request, performer, issueProvider, getThesaurus()));
            return entity(info).build();
        }

    }


    @PUT
    @Path("/bulksnooze")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ASSIGN_ISSUE, Privileges.Constants.CLOSE_ISSUE})
    @Deprecated
    public Response assignIssues(BulkSnoozeRequest request, @Context SecurityContext securityContext, @BeanParam JsonQueryFilter filter) {
        User performer = (User) securityContext.getUserPrincipal();
        Function<ActionInfo, List<? extends Issue>> issueProvider;

        if (request.allIssues) {
            issueProvider = bulkResults -> getIssuesForBulk(filter);
        } else {
            issueProvider = bulkResult -> getUserSelectedIssues(request, bulkResult);
        }
        ActionInfo info = getTransactionService().execute(new BulkSnoozeTransaction(request, performer, issueProvider, getThesaurus(), clock));
        return entity(info).build();
    }

    @PUT
    @Transactional
    @Path("/setpriority")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ACTION_ISSUE)
    public Response setPriority(SetPriorityIssueRequest request, @Context SecurityContext securityContext, @BeanParam JsonQueryFilter filter) {
        Function<ActionInfo, List<? extends Issue>> issueProvider;
        if (request.allIssues) {
            issueProvider = bulkResults -> getIssuesForBulk(filter);
        } else {
            issueProvider = bulkResult -> getUserSelectedIssues(request, bulkResult);
        }
        ActionInfo info = doBulkSetPriority(request, issueProvider);
        return Response.ok().entity(info).build();
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
        Function<ActionInfo, List<? extends Issue>> issueProvider;
        if (request.allIssues) {
            issueProvider = bulkResults -> getIssuesForBulk(filter);
        } else {
            issueProvider = bulkResult -> getUserSelectedIssues(request, bulkResult);
        }
        return entity(doBulkClose(request, performer, issueProvider)).build();
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

    private boolean isNumericValue(String id) {
        try {
            long number = Long.parseLong(id);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private List<? extends Issue> getIssuesForBulk(JsonQueryFilter filter) {
        return getIssueService().findIssues(issueResourceHelper.buildFilterFromQueryParameters(filter)).find().stream()
                .map(issue -> getIssueService().wrapOpenOrHistorical(issue)).collect(Collectors.toList());
    }

    private List<? extends Issue> getUserSelectedIssues(BulkIssueRequest request, ActionInfo bulkResult) {
        List<Issue> issuesForBulk = new ArrayList<>(request.issues.size());
        for (EntityReference issueRef : request.issues) {
            Optional<? extends Issue> issue = getIssueService().findIssue(issueRef.getId());
            if (issue.isPresent()) {
                issuesForBulk.add(issue.get());
            } else {
                bulkResult.addFail(getThesaurus().getFormat(MessageSeeds.ISSUE_DOES_NOT_EXIST)
                        .format(), issueRef.getId(), "Issue (id = " + issueRef.getId() + ")");
            }
        }
        return issuesForBulk;
    }

    private Issue getIssue(SingleIssueRequest request, ActionInfo result) {
        Issue issue = getIssueService().findIssue(request.issue.getId()).orElse(null);
        if (issue == null) {
            result.addFail(getThesaurus().getFormat(MessageSeeds.ISSUE_DOES_NOT_EXIST)
                    .format(), request.issue.getId(), "Issue (id = " + request.issue.getId() + ")");
        }
        return issue;
    }

    private Issue getIssue(Long id, ActionInfo result) {
        Issue issue = getIssueService().findIssue(id).orElse(null);
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
                .map(s -> getIssueService().findStatus(s).get())
                .collect(Collectors.toList())
                : Collections.EMPTY_LIST;
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

    private List<IssueInfo> sortIssues(List<IssueInfo> listIssues, StandardParametersBean parameters) {
        Order[] orders = parameters.getOrder("");
        Comparator<IssueInfo> comparatorIssue = null;
        for (Order order : orders) {
            comparatorIssue = getComparatorIssueInfo(order, comparatorIssue);
        }
        if (comparatorIssue != null) {
            listIssues.sort(comparatorIssue);
        }
        return listIssues;
    }

    public Comparator<IssueInfo> getComparatorIssueInfo(Order order, Comparator<IssueInfo> comparatorIssue) {
        Comparator<IssueInfo> comparatorIssueTemp = null;
        switch (order.getName()) {
            case "device_name":
                comparatorIssueTemp = Comparator.comparing(IssueInfo::getDeviceName);
                break;
            case "usagePoint_name":
                comparatorIssueTemp = Comparator.comparing(IssueInfo::getUsageName);
                break;
            case "priorityTotal":
                comparatorIssueTemp = Comparator.comparing(IssueInfo::getPriorityTotal);
                break;
            case "dueDate":
                comparatorIssueTemp = Comparator.comparing(IssueInfo::getDueDate);
                break;
            case "id":
                comparatorIssueTemp = Comparator.comparing(IssueInfo::getId);
                break;
            case "createDateTime":
                comparatorIssueTemp = Comparator.comparing(IssueInfo::getCreatedDateTime);
                break;
        }


        if (comparatorIssueTemp != null && !order.ascending()) {
            comparatorIssueTemp = comparatorIssueTemp.reversed();
        }

        if (comparatorIssue == null) {
            comparatorIssue = comparatorIssueTemp;
        } else {
            comparatorIssue = comparatorIssue.thenComparing(comparatorIssueTemp);
        }

        return comparatorIssue;
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

    private ActionInfo doBulkClose(CloseIssueRequest request, User performer, Function<ActionInfo, List<? extends Issue>> issueProvider) {
        ActionInfo response = new ActionInfo();
        Optional<IssueStatus> status = getIssueService().findStatus(request.status);
        if (status.isPresent() && status.get().isHistorical()) {
            for (Issue issue : issueProvider.apply(response)) {
                if (issue.getStatus().isHistorical()) {
                    response.addFail(getThesaurus().getFormat(MessageSeeds.ISSUE_ALREADY_CLOSED)
                            .format(), issue.getId(), issue.getTitle());
                } else {
                    issue.addComment(request.comment, performer);
                    if (issue instanceof OpenIssue) {
                        ((OpenIssue) issue).close(status.get());
                    } else {
                        // user set both open and close statuses in filter
                        getIssueService().findOpenIssue(issue.getId()).ifPresent(
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


