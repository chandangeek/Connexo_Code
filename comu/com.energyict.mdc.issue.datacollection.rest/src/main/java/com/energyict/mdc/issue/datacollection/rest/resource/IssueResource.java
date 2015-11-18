package com.energyict.mdc.issue.datacollection.rest.resource;

import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionFilter;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.entity.IssueDataCollection;
import com.energyict.mdc.issue.datacollection.rest.i18n.MessageSeeds;
import com.energyict.mdc.issue.datacollection.rest.response.DataCollectionIssueInfoFactory;

import com.elster.jupiter.domain.util.Finder;
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
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.transaction.TransactionContext;
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

import static com.elster.jupiter.issue.rest.request.RequestHelper.ID;
import static com.elster.jupiter.issue.rest.request.RequestHelper.KEY;
import static com.elster.jupiter.issue.rest.request.RequestHelper.LIMIT;
import static com.elster.jupiter.issue.rest.request.RequestHelper.START;
import static com.elster.jupiter.issue.rest.response.ResponseHelper.entity;

@Path("/issue")
public class IssueResource extends BaseResource {

    private final IssueService issueService;
    private final MeteringService meteringService;
    private final UserService userService;
    private final IssueDataCollectionService issueDataCollectionService;
    private final DataCollectionIssueInfoFactory issuesInfoFactory;
    private final IssueResourceHelper issueResourceHelper;
    private final ConcurrentModificationExceptionFactory conflictFactory;

    @Inject
    public IssueResource(IssueService issueService, MeteringService meteringService, UserService userService, IssueDataCollectionService issueDataCollectionService, DataCollectionIssueInfoFactory dataCollectionIssuesInfoFactory, IssueResourceHelper issueResourceHelper, ConcurrentModificationExceptionFactory conflictFactory) {
        this.issueService = issueService;
        this.meteringService = meteringService;
        this.userService = userService;
        this.issueDataCollectionService = issueDataCollectionService;
        this.issuesInfoFactory = dataCollectionIssuesInfoFactory;
        this.issueResourceHelper = issueResourceHelper;
        this.conflictFactory = conflictFactory;
    }

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE,Privileges.Constants.ASSIGN_ISSUE,Privileges.Constants.CLOSE_ISSUE,Privileges.Constants.COMMENT_ISSUE,Privileges.Constants.ACTION_ISSUE})
    public PagedInfoList getAllIssues(@BeanParam StandardParametersBean params, @BeanParam JsonQueryParameters queryParams, @BeanParam JsonQueryFilter filter) {
        validateMandatory(params, START, LIMIT);
        Finder<? extends IssueDataCollection> finder = issueDataCollectionService.findIssues(buildFilterFromQueryParameters(filter), EndDevice.class, User.class, IssueReason.class,
                IssueStatus.class, IssueType.class);
        addSorting(finder, params);
        if (queryParams.getStart().isPresent() && queryParams.getLimit().isPresent()) {
            finder.paged(queryParams.getStart().get(), queryParams.getLimit().get());
        }
        return PagedInfoList.fromPagedList("data", issuesInfoFactory.asInfos(finder.find()), queryParams);
    }

    private List<? extends IssueDataCollection> getIssuesForBulk(JsonQueryFilter filter) {
        return issueDataCollectionService.findIssues(buildFilterFromQueryParameters(filter), EndDevice.class, User.class, IssueReason.class,
                IssueStatus.class, IssueType.class).find();
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
    @Path("/{" + ID + "}/comments")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE, Privileges.Constants.ASSIGN_ISSUE, Privileges.Constants.CLOSE_ISSUE, Privileges.Constants.COMMENT_ISSUE, Privileges.Constants.ACTION_ISSUE})
    public PagedInfoList getComments(@PathParam(ID) long id, @BeanParam JsonQueryParameters queryParameters) {
        IssueDataCollection issue = getIssueDataCollectionService().findIssue(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return PagedInfoList.fromCompleteList("comments", issueResourceHelper.getIssueComments(issue), queryParameters);
    }

    @POST @Transactional
    @Path("/{" + ID + "}/comments")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.COMMENT_ISSUE)
    public Response postComment(@PathParam("id") long id, CreateCommentRequest request, @Context SecurityContext securityContext) {
        IssueDataCollection issue = getIssueDataCollectionService().findIssue(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return Response.ok(issueResourceHelper.postComment(issue, request, securityContext)).status(Response.Status.CREATED).build();
    }

    @GET @Transactional
    @Path("/{" + ID + "}/actions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE, Privileges.Constants.ASSIGN_ISSUE, Privileges.Constants.CLOSE_ISSUE, Privileges.Constants.COMMENT_ISSUE, Privileges.Constants.ACTION_ISSUE})
    public PagedInfoList getActions(@PathParam("id") long id, @BeanParam JsonQueryParameters queryParameters) {
        IssueDataCollection issue = getIssueDataCollectionService().findIssue(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return PagedInfoList.fromCompleteList("issueActions", issueResourceHelper.getListOfAvailableIssueActions(issue), queryParameters);
    }

    @GET @Transactional
    @Path("/{" + ID + "}/actions/{" + KEY + "}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE,Privileges.Constants.ASSIGN_ISSUE,Privileges.Constants.CLOSE_ISSUE,Privileges.Constants.COMMENT_ISSUE,Privileges.Constants.ACTION_ISSUE})
    public Response getActionTypeById(@PathParam(ID) long id, @PathParam(KEY) long actionId){
        getIssueDataCollectionService().findIssue(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return Response.ok(issueResourceHelper.getIssueActionById(actionId)).build();
    }

    @PUT @Transactional
    @Path("/{" + ID + "}/actions/{" + KEY + "}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ACTION_ISSUE)
    public Response performAction(@PathParam(ID) long id, @PathParam(KEY) long actionId, PerformActionRequest request) {
        IssueDataCollection issue = getIssueDataCollectionService().findAndLockIssueDataCollectionByIdAndVersion(id, request.issue.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(request.issue.title)
                        .withActualVersion(() -> getIssueDataCollectionService().findIssue(id)
                                .map(IssueDataCollection::getVersion)
                                .orElse(null))
                        .supplier());
        request.id = actionId;
        return Response.ok(issueResourceHelper.performIssueAction(issue, request)).build();
    }

    @PUT @Transactional
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

    private List<? extends Issue> getUserSelectedIssues(BulkIssueRequest request, ActionInfo bulkResult) {
        List<Issue> issuesForBulk = new ArrayList<>(request.issues.size());
        for (EntityReference issueRef : request.issues) {
            Issue issue = getIssueDataCollectionService().findOpenIssue(issueRef.getId()).orElse(null);
            if (issue == null) {
                issue = getIssueDataCollectionService().findHistoricalIssue(issueRef.getId()).orElse(null);
            }
            if (issue == null) {
                bulkResult.addFail(getThesaurus().getFormat(MessageSeeds.ISSUE_DOES_NOT_EXIST).format(), issueRef.getId(), "Issue (id = " + issueRef.getId() + ")");
            } else {
                issuesForBulk.add(issue);
            }
        }
        return issuesForBulk;
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
            issueProvider = bulkResult -> getUserSelectedIssues(request, bulkResult);
        }
        return entity(doBulkClose(request, performer, issueProvider)).build();
    }

    private ActionInfo doBulkClose(CloseIssueRequest request, User performer, Function<ActionInfo, List<? extends Issue>> issueProvider) {
        ActionInfo response = new ActionInfo();
        try (TransactionContext context = getTransactionService().getContext()) {
            Optional<IssueStatus> status = getIssueService().findStatus(request.status);
            if (status.isPresent() && status.get().isHistorical()) {
                for (Issue issue : issueProvider.apply(response)) {
                    if (issue.getStatus().isHistorical()) {
                        response.addFail(getThesaurus().getFormat(MessageSeeds.ISSUE_ALREADY_CLOSED).format(), issue.getId(), issue.getTitle());
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
            context.commit();
        }
        return response;
    }

    private IssueDataCollectionFilter buildFilterFromQueryParameters(JsonQueryFilter jsonFilter) {
        IssueDataCollectionFilter filter = new IssueDataCollectionFilter();
        jsonFilter.getStringList("status").stream()
                .flatMap(s -> issueService.findStatus(s).map(Stream::of).orElse(Stream.empty()))
                .forEach(filter::addStatus);
        if (jsonFilter.hasProperty("reason") && issueService.findReason(jsonFilter.getString("reason")).isPresent()) {
            filter.setIssueReason(issueService.findReason(jsonFilter.getString("reason")).get());
        }
        if (jsonFilter.hasProperty("meter") && meteringService.findEndDevice(jsonFilter.getString("meter")).isPresent()) {
            filter.addDevice(meteringService.findEndDevice(jsonFilter.getString("meter")).get());
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

    private Finder<? extends IssueDataCollection> addSorting(Finder<? extends IssueDataCollection> finder, StandardParametersBean parameters) {
        Order[] orders = parameters.getOrder("baseIssue.");
        for(Order order : orders) {
            finder.sorted(order.getName(), order.ascending());
        }
        return finder;
    }
}