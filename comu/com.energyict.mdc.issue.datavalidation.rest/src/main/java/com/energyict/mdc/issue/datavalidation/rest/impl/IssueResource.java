package com.energyict.mdc.issue.datavalidation.rest.impl;

import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.energyict.mdc.issue.datavalidation.DataValidationIssueFilter;
import com.energyict.mdc.issue.datavalidation.IssueDataValidation;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.issue.rest.MessageSeeds;
import com.elster.jupiter.issue.rest.request.AssignIssueRequest;
import com.elster.jupiter.issue.rest.request.BulkIssueRequest;
import com.elster.jupiter.issue.rest.request.CloseIssueRequest;
import com.elster.jupiter.issue.rest.request.CreateCommentRequest;
import com.elster.jupiter.issue.rest.request.EntityReference;
import com.elster.jupiter.issue.rest.request.PerformActionRequest;
import com.elster.jupiter.issue.rest.resource.IssueResourceHelper;
import com.elster.jupiter.issue.rest.resource.StandardParametersBean;
import com.elster.jupiter.issue.rest.response.ActionInfo;
import com.elster.jupiter.issue.rest.response.IssueAssigneeInfo;
import com.elster.jupiter.issue.rest.response.IssueAssigneeInfoAdapter;
import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.issue.rest.transactions.AssignIssueTransaction;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Order;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.elster.jupiter.issue.rest.response.ResponseHelper.entity;

@Path("/issues")
public class IssueResource {

    private final IssueService issueService;
    private final IssueDataValidationService issueDataValidationService;
    private final MeteringService meteringService;
    private final UserService userService;
    private final TransactionService transactionService;
    private final Thesaurus thesaurus;

    private final IssueResourceHelper issueResourceHelper;
    private final DataValidationIssueInfoFactory issueInfoFactory;
    private final ConcurrentModificationExceptionFactory conflictFactory;

    @Inject
    public IssueResource(IssueService issueService, IssueDataValidationService issueDataValidationService, MeteringService meteringService,
                         UserService userService, TransactionService transactionService, DataValidationIssueInfoFactory dataCollectionIssuesInfoFactory,
                         IssueResourceHelper issueResourceHelperFactory, Thesaurus thesaurus, ConcurrentModificationExceptionFactory conflictFactory) {
        this.issueService = issueService;
        this.issueDataValidationService = issueDataValidationService;
        this.meteringService = meteringService;
        this.userService = userService;
        this.transactionService = transactionService;
        this.issueInfoFactory = dataCollectionIssuesInfoFactory;
        this.issueResourceHelper = issueResourceHelperFactory;
        this.thesaurus = thesaurus;
        this.conflictFactory = conflictFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE, Privileges.Constants.ASSIGN_ISSUE, Privileges.Constants.CLOSE_ISSUE, Privileges.Constants.COMMENT_ISSUE, Privileges.Constants.ACTION_ISSUE})
    public PagedInfoList getDataValidationIssues(@BeanParam StandardParametersBean queryFilter, @BeanParam JsonQueryParameters queryParams, @BeanParam JsonQueryFilter filter) {
        Finder<? extends IssueDataValidation> finder = issueDataValidationService.findAllDataValidationIssues(buildFilterFromQueryParameters(filter));
        addSorting(finder, queryFilter);
        if (queryParams.getStart().isPresent() && queryParams.getLimit().isPresent()) {
            finder.paged(queryParams.getStart().get(), queryParams.getLimit().get());
        }
        List<? extends IssueDataValidation> issues = finder.find();
        return PagedInfoList.fromPagedList("dataValidationIssues", issueInfoFactory.asInfo(issues), queryParams);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE, Privileges.Constants.ASSIGN_ISSUE, Privileges.Constants.CLOSE_ISSUE, Privileges.Constants.COMMENT_ISSUE, Privileges.Constants.ACTION_ISSUE})
    public Response getIssueById(@PathParam("id") long id) {
        IssueDataValidation issue = issueDataValidationService.findIssue(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return Response.ok(issueInfoFactory.asInfo(issue, DeviceInfo.class)).build();
    }

    @GET
    @Path("/{id}/comments")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE, Privileges.Constants.ASSIGN_ISSUE, Privileges.Constants.CLOSE_ISSUE, Privileges.Constants.COMMENT_ISSUE, Privileges.Constants.ACTION_ISSUE})
    public PagedInfoList getComments(@PathParam("id") long id, @BeanParam JsonQueryParameters queryParameters) {
        IssueDataValidation issue = issueDataValidationService.findIssue(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return PagedInfoList.fromCompleteList("comments", issueResourceHelper.getIssueComments(issue), queryParameters);
    }

    @POST
    @Path("/{id}/comments")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.COMMENT_ISSUE)
    public Response postComment(@PathParam("id") long id, CreateCommentRequest request, @Context SecurityContext securityContext) {
        IssueDataValidation issue = issueDataValidationService.findIssue(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return Response.ok(issueResourceHelper.postComment(issue, request, securityContext)).status(Response.Status.CREATED).build();
    }

    @GET
    @Path("/{id}/actions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE, Privileges.Constants.ASSIGN_ISSUE, Privileges.Constants.CLOSE_ISSUE, Privileges.Constants.COMMENT_ISSUE, Privileges.Constants.ACTION_ISSUE})
    public PagedInfoList getActions(@PathParam("id") long id, @BeanParam JsonQueryParameters queryParameters) {
        IssueDataValidation issue = issueDataValidationService.findIssue(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return PagedInfoList.fromCompleteList("issueActions", issueResourceHelper.getListOfAvailableIssueActions(issue), queryParameters);
    }

    @GET
    @Path("/{id}/actions/{actionId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE, Privileges.Constants.ASSIGN_ISSUE, Privileges.Constants.CLOSE_ISSUE, Privileges.Constants.COMMENT_ISSUE, Privileges.Constants.ACTION_ISSUE})
    public Response getActionTypeById(@PathParam("id") long id, @PathParam("actionId") long actionId) {
        issueDataValidationService.findIssue(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return Response.ok(issueResourceHelper.getIssueActionById(actionId)).build();
    }

    @PUT
    @Path("/{id}/actions/{actionId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ACTION_ISSUE)
    public Response performAction(@PathParam("id") long id, @PathParam("actionId") long actionId, PerformActionRequest request) {
        IssueDataValidation issue = issueDataValidationService.findAndLockIssueDataValidationByIdAndVersion(id, request.issue.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(request.issue.title)
                        .withActualVersion(() -> issueDataValidationService.findIssue(request.id)
                                .map(IssueDataValidation::getVersion)
                                .orElse(null))
                        .supplier());
        request.id = actionId;
        return Response.ok(issueResourceHelper.performIssueAction(issue, request)).build();
    }

    @PUT
    @Path("/assign")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ASSIGN_ISSUE)
    @Deprecated
    public Response assignIssues(AssignIssueRequest request, @Context SecurityContext securityContext, @BeanParam JsonQueryFilter filter/* @BeanParam StandardParametersBean params*/) {
        /* TODO this method should be refactored when FE implements dynamic actions for bulk operations */
        User performer = (User) securityContext.getUserPrincipal();
        Function<ActionInfo, List<? extends Issue>> issueProvider;
        if (request.allIssues) {
            issueProvider = bulkResults -> getIssuesForBulk(filter);
        } else {
            issueProvider = bulkResult -> getUserSelectedIssues(request, bulkResult);
        }
        ActionInfo info = transactionService.execute(new AssignIssueTransaction(request, performer, issueProvider));
        return entity(info).build();
    }

    @PUT
    @Path("/close")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.CLOSE_ISSUE)
    @Deprecated
    public Response closeIssues(CloseIssueRequest request, @Context SecurityContext securityContext, @BeanParam JsonQueryFilter filter /*@BeanParam StandardParametersBean params*/) {
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

    private List<? extends IssueDataValidation> getIssuesForBulk(JsonQueryFilter filter) {
        return issueDataValidationService.findAllDataValidationIssues(buildFilterFromQueryParameters(filter)).find();
    }

    private List<? extends Issue> getUserSelectedIssues(BulkIssueRequest request, ActionInfo bulkResult) {
        List<Issue> issuesForBulk = new ArrayList<>(request.issues.size());
        for (EntityReference issueRef : request.issues) {
            Optional<? extends Issue> issue = issueDataValidationService.findIssue(issueRef.getId());
            if (issue.isPresent()) {
                issuesForBulk.add(issue.get());
            } else {
                bulkResult.addFail(thesaurus.getFormat(MessageSeeds.ISSUE_DOES_NOT_EXIST).format(), issueRef.getId(), "Issue (id = " + issueRef.getId() + ")");
            }
        }
        return issuesForBulk;
    }

    private ActionInfo doBulkClose(CloseIssueRequest request, User performer, Function<ActionInfo, List<? extends Issue>> issueProvider) {
        ActionInfo response = new ActionInfo();
        try (TransactionContext context = transactionService.getContext()) {
            Optional<IssueStatus> status = issueService.findStatus(request.status);
            if (status.isPresent() && status.get().isHistorical()) {
                for (Issue issue : issueProvider.apply(response)) {
                    if (issue.getStatus().isHistorical()) {
                        response.addFail(thesaurus.getFormat(MessageSeeds.ISSUE_ALREADY_CLOSED).format(), issue.getId(), issue.getTitle());
                    } else {
                        issue.addComment(request.comment, performer);
                        if (issue instanceof OpenIssue) {
                            ((OpenIssue) issue).close(status.get());
                        } else {
                            // user set both open and close statuses in filter
                            issueDataValidationService.findOpenIssue(issue.getId()).ifPresent(openIssue -> openIssue.close(status.get()));
                        }
                        response.addSuccess(issue.getId());
                    }
                }
            } else {
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }
            context.commit();
        }
        return response;
    }

    private DataValidationIssueFilter buildFilterFromQueryParameters(JsonQueryFilter jsonFilter) {
        DataValidationIssueFilter filter = new DataValidationIssueFilter();
        jsonFilter.getStringList("status").stream()
                .flatMap(s -> issueService.findStatus(s).map(Stream::of).orElse(Stream.empty()))
                .forEach(filter::addStatus);
        if (jsonFilter.hasProperty("reason") && issueService.findReason(jsonFilter.getString("reason")).isPresent()) {
            filter.setIssueReason(issueService.findReason(jsonFilter.getString("reason")).get());
        }
        if (jsonFilter.hasProperty("meter") && meteringService.findEndDevice(jsonFilter.getString("meter")).isPresent()) {
            filter.setDevice(meteringService.findEndDevice(jsonFilter.getString("meter")).get());
        }
        IssueAssigneeInfo issueAssigneeInfo = jsonFilter.getProperty("assignee", new IssueAssigneeInfoAdapter());
        String assigneeType = issueAssigneeInfo.getType();
        Long assigneeId = issueAssigneeInfo.getId();

        if (assigneeId != null && assigneeId > 0) {
            if (IssueAssignee.Types.USER.equals(assigneeType)) {
                userService.getUser(assigneeId).ifPresent(filter::setAssignee);
            }
        } else if (assigneeId != null && assigneeId != 0) {
            filter.setUnassignedOnly();
        }
        return filter;
    }

    private Finder<? extends IssueDataValidation> addSorting(Finder<? extends IssueDataValidation> finder, StandardParametersBean parameters) {
        Order[] orders = parameters.getOrder("baseIssue.");
        for(Order order : orders) {
            finder.sorted(order.getName(), order.ascending());
        }
        return finder;
    }
}