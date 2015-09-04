package com.energyict.mdc.issue.datacollection.rest.resource;

import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.entity.HistoricalIssueDataCollection;
import com.energyict.mdc.issue.datacollection.entity.IssueDataCollection;
import com.energyict.mdc.issue.datacollection.entity.OpenIssueDataCollection;
import com.energyict.mdc.issue.datacollection.rest.i18n.MessageSeeds;
import com.energyict.mdc.issue.datacollection.rest.response.DataCollectionIssueInfoFactory;

import com.elster.jupiter.domain.util.Query;
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
import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Condition;

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

import static com.elster.jupiter.issue.rest.request.RequestHelper.ASSIGNEE;
import static com.elster.jupiter.issue.rest.request.RequestHelper.ID;
import static com.elster.jupiter.issue.rest.request.RequestHelper.KEY;
import static com.elster.jupiter.issue.rest.request.RequestHelper.LIMIT;
import static com.elster.jupiter.issue.rest.request.RequestHelper.METER;
import static com.elster.jupiter.issue.rest.request.RequestHelper.REASON;
import static com.elster.jupiter.issue.rest.request.RequestHelper.START;
import static com.elster.jupiter.issue.rest.request.RequestHelper.STATUS;
import static com.elster.jupiter.issue.rest.response.ResponseHelper.entity;
import static com.elster.jupiter.util.conditions.Where.where;

@Path("/issue")
public class IssueResource extends BaseResource {

    private final DataCollectionIssueInfoFactory issuesInfoFactory;
    private final IssueResourceHelper issueResourceHelper;

    @Inject
    public IssueResource(DataCollectionIssueInfoFactory dataCollectionIssuesInfoFactory, IssueResourceHelper issueResourceHelper) {
        this.issuesInfoFactory = dataCollectionIssuesInfoFactory;
        this.issueResourceHelper = issueResourceHelper;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_ISSUE,Privileges.ASSIGN_ISSUE,Privileges.CLOSE_ISSUE,Privileges.COMMENT_ISSUE,Privileges.ACTION_ISSUE})
    public PagedInfoList getAllIssues(@BeanParam StandardParametersBean params, @BeanParam JsonQueryParameters queryParams, @BeanParam JsonQueryFilter filter) {
        validateMandatory(params, START, LIMIT);
        List<? extends IssueDataCollection> list = getFilteredIssues(params, filter);
        return PagedInfoList.fromPagedList("data", issuesInfoFactory.asInfos(list), queryParams);
    }

    private List<? extends IssueDataCollection> getFilteredIssues(StandardParametersBean params, JsonQueryFilter filter) {
        Class<? extends IssueDataCollection> apiClass = getQueryApiClass(filter);
        Class<? extends Issue> eagerClass = getEagerApiClass(apiClass);
        Query<? extends IssueDataCollection> query = getIssueDataCollectionService().query(apiClass, eagerClass, EndDevice.class, User.class, IssueReason.class,
                IssueStatus.class, IssueType.class);
        Condition condition = getQueryCondition(filter);
        return query.select(condition, params.getFrom(), params.getTo(), params.getOrder("baseIssue."));
    }

    private List<? extends IssueDataCollection> getIssuesForBulk(JsonQueryFilter filter) {
        Condition condition = getQueryCondition(filter);
        Query<OpenIssueDataCollection> openQuery = getIssueDataCollectionService().query(OpenIssueDataCollection.class, OpenIssue.class, EndDevice.class, User.class, IssueReason.class,
                IssueStatus.class, IssueType.class);
        Query<HistoricalIssueDataCollection> closeQuery = getIssueDataCollectionService().query(HistoricalIssueDataCollection.class, HistoricalIssue.class, EndDevice.class, User.class, IssueReason.class,
                IssueStatus.class, IssueType.class);
        List<IssueDataCollection> issues = new ArrayList<>();
        issues.addAll(openQuery.select(condition));
        issues.addAll(closeQuery.select(condition));
        return issues;
    }

    @GET
    @Path("/{" + ID + "}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_ISSUE, Privileges.ASSIGN_ISSUE, Privileges.CLOSE_ISSUE, Privileges.COMMENT_ISSUE, Privileges.ACTION_ISSUE})
    public Response getIssueById(@PathParam(ID) long id) {
        Optional<? extends IssueDataCollection> issue = getIssueDataCollectionService().findIssue(id);
        return issue.map(i -> entity(issuesInfoFactory.asInfo(i, DeviceInfo.class)).build())
                    .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    @GET
    @Path("/{" + ID + "}/comments")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_ISSUE, Privileges.ASSIGN_ISSUE, Privileges.CLOSE_ISSUE, Privileges.COMMENT_ISSUE, Privileges.ACTION_ISSUE})
    public PagedInfoList getComments(@PathParam(ID) long id, @BeanParam JsonQueryParameters queryParameters) {
        IssueDataCollection issue = getIssueDataCollectionService().findIssue(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return PagedInfoList.fromCompleteList("comments", issueResourceHelper.getIssueComments(issue), queryParameters);
    }

    @POST
    @Path("/{" + ID + "}/comments")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.COMMENT_ISSUE)
    public Response postComment(@PathParam("id") long id, CreateCommentRequest request, @Context SecurityContext securityContext) {
        IssueDataCollection issue = getIssueDataCollectionService().findIssue(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return Response.ok(issueResourceHelper.postComment(issue, request, securityContext)).status(Response.Status.CREATED).build();
    }

    @GET
    @Path("/{" + ID + "}/actions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_ISSUE, Privileges.ASSIGN_ISSUE, Privileges.CLOSE_ISSUE, Privileges.COMMENT_ISSUE, Privileges.ACTION_ISSUE})
    public PagedInfoList getActions(@PathParam("id") long id, @BeanParam JsonQueryParameters queryParameters) {
        IssueDataCollection issue = getIssueDataCollectionService().findIssue(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return PagedInfoList.fromCompleteList("issueActions", issueResourceHelper.getListOfAvailableIssueActions(issue), queryParameters);
    }

    @GET
    @Path("/{" + ID + "}/actions/{" + KEY + "}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_ISSUE,Privileges.ASSIGN_ISSUE,Privileges.CLOSE_ISSUE,Privileges.COMMENT_ISSUE,Privileges.ACTION_ISSUE})
    public Response getActionTypeById(@PathParam(ID) long id, @PathParam(KEY) long actionId){
        getIssueDataCollectionService().findIssue(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return Response.ok(issueResourceHelper.getIssueActionById(actionId)).build();
    }

    @PUT
    @Path("/{" + ID + "}/actions/{" + KEY + "}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ACTION_ISSUE)
    public Response performAction(@PathParam(ID) long id, @PathParam(KEY) long actionId, PerformActionRequest request) {
        IssueDataCollection issue = getIssueDataCollectionService().findIssue(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        request.id = actionId;
        return Response.ok(issueResourceHelper.performIssueAction(issue, request)).build();
    }

    @PUT
    @Path("/assign")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.ASSIGN_ISSUE)
    @Deprecated
    public Response assignIssues(AssignIssueRequest request, @Context SecurityContext securityContext, @BeanParam JsonQueryFilter filter/*StandardParametersBean params*/) {
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

    @PUT
    @Path("/close")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.CLOSE_ISSUE)
    @Deprecated
    public Response closeIssues(CloseIssueRequest request, @Context SecurityContext securityContext, @BeanParam JsonQueryFilter filter/*StandardParametersBean params*/) {
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

    private Class<? extends IssueDataCollection> getQueryApiClass(JsonQueryFilter filter) {
        boolean isHistorical = false;
        boolean isActual = false;

        for (String status : filter.getStringList(STATUS)/* params.get(STATUS)*/) {
            Optional<IssueStatus> issueStatusRef = getIssueService().findStatus(status);
            if (issueStatusRef.isPresent()) {
                if (issueStatusRef.get().isHistorical()) {
                    isHistorical = true;
                } else {
                    isActual = true;
                }
            }
        }

        Class<? extends IssueDataCollection> apiClass = IssueDataCollection.class;
        if (isActual && !isHistorical) {
            apiClass = OpenIssueDataCollection.class;
        } else if (isHistorical && !isActual) {
            apiClass = HistoricalIssueDataCollection.class;
        }
        return apiClass;
    }

    private Class<? extends Issue> getEagerApiClass(Class<? extends Issue> queryApi) {
        if (OpenIssueDataCollection.class.equals(queryApi)) {
            return OpenIssue.class;
        } else if (HistoricalIssueDataCollection.class.equals(queryApi)) {
            return HistoricalIssue.class;
        }
        return Issue.class;
    }


    private Condition getQueryCondition( JsonQueryFilter filter) {
        Condition condition = Condition.TRUE;
        if (filter.hasFilters()) {
            condition = condition.and(addAssigneeQueryCondition(filter));
            condition = condition.and(addReasonQueryCondition(filter));
            condition = condition.and(addStatusQueryCondition(filter));
            condition = condition.and(addMeterQueryCondition(filter));
        }
        return condition;
    }

    private Condition addAssigneeQueryCondition(JsonQueryFilter filter) {
        Condition conditionAssignee = Condition.TRUE;
        if (filter.hasProperty(ASSIGNEE)) {
            IssueAssigneeInfo assignee = filter.getProperty(ASSIGNEE, new IssueAssigneeInfoAdapter());
            if (assignee.getId() != null && assignee.getId() > 0) {
                if (getIssueService().checkIssueAssigneeType(assignee.getType())) {
                    conditionAssignee = where("baseIssue." + assignee.getType().toLowerCase() + ".id").isEqualTo(assignee.getId());
                }
            } else { // Filter by unassigned
                conditionAssignee = where("baseIssue.assigneeType").isNull();
            }
        }
        return conditionAssignee;
    }

    private Condition addReasonQueryCondition(JsonQueryFilter filter) {
        Condition conditionReason = Condition.TRUE;
        if (filter.hasProperty(REASON)) {
            conditionReason = conditionReason.and(where("baseIssue.reason.key").isEqualTo(filter.getString(REASON)));
        }
        final Condition finalConditionReason = conditionReason;
        return getIssueService()
                .findIssueType(IssueDataCollectionService.DATA_COLLECTION_ISSUE)
                .map(it -> finalConditionReason.and(where("baseIssue.reason.issueType").isEqualTo(it))).get();
    }

    private Condition addStatusQueryCondition(JsonQueryFilter filter) {
        Condition conditionStatus = Condition.FALSE;
        for (String status : filter.getStringList(STATUS)) {
            conditionStatus = conditionStatus.or(where("baseIssue.status.key").isEqualTo(status));
        }
        conditionStatus = conditionStatus == Condition.FALSE ? Condition.TRUE : conditionStatus;
        return conditionStatus;
    }

    private Condition addMeterQueryCondition(JsonQueryFilter filter) {
        Condition conditionMeter = Condition.TRUE;
        if (filter.hasProperty(METER)) {
            conditionMeter = conditionMeter.and(where("baseIssue.device.mRID").isEqualTo(filter.getString(METER)));
        }
        return conditionMeter;
    }
}