package com.energyict.mdc.issue.datacollection.rest.resource;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.request.*;
import com.elster.jupiter.issue.rest.resource.IssueResourceHelper;
import com.elster.jupiter.issue.rest.resource.StandardParametersBean;
import com.elster.jupiter.issue.rest.response.ActionInfo;
import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.issue.rest.transactions.AssignIssueTransaction;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.issue.share.entity.*;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.entity.HistoricalIssueDataCollection;
import com.energyict.mdc.issue.datacollection.entity.IssueDataCollection;
import com.energyict.mdc.issue.datacollection.entity.OpenIssueDataCollection;
import com.energyict.mdc.issue.datacollection.rest.i18n.MessageSeeds;
import com.energyict.mdc.issue.datacollection.rest.response.DataCollectionIssueInfoFactory;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.elster.jupiter.issue.rest.request.RequestHelper.*;
import static com.elster.jupiter.issue.rest.response.ResponseHelper.entity;
import static com.elster.jupiter.util.conditions.Where.where;
import static com.energyict.mdc.issue.datacollection.rest.i18n.MessageSeeds.getString;

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
    public PagedInfoList getAllIssues(@BeanParam StandardParametersBean params, @BeanParam JsonQueryParameters queryParams) {
        validateMandatory(params, START, LIMIT);
        List<? extends IssueDataCollection> list = getFilteredIssues(params);
        return PagedInfoList.fromPagedList("data", issuesInfoFactory.asInfos(list), queryParams);
    }

    private List<? extends IssueDataCollection> getFilteredIssues(StandardParametersBean params) {
        Class<? extends IssueDataCollection> apiClass = getQueryApiClass(params);
        Class<? extends Issue> eagerClass = getEagerApiClass(apiClass);
        Query<? extends IssueDataCollection> query = getIssueDataCollectionService().query(apiClass, eagerClass, EndDevice.class, User.class, IssueReason.class,
                IssueStatus.class, IssueType.class);
        Condition condition = getQueryCondition(params);
        return query.select(condition, params.getFrom(), params.getTo(), params.getOrder("baseIssue."));
    }

    private List<? extends IssueDataCollection> getIssuesForBulk(StandardParametersBean params) {
        Condition condition = getQueryCondition(params);
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
        return entity(issueResourceHelper.performIssueAction(issue, request)).build();
    }

    @PUT
    @Path("/assign")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.ASSIGN_ISSUE)
    @Deprecated
    public Response assignIssues(AssignIssueRequest request, @Context SecurityContext securityContext, @BeanParam StandardParametersBean params) {
        /* TODO this method should be refactored when FE implements dynamic actions for bulk operations */
        User performer = (User) securityContext.getUserPrincipal();
        Function<ActionInfo, List<? extends Issue>> issueProvider;
        if (request.allIssues) {
            issueProvider = bulkResults -> getIssuesForBulk(params);
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
                bulkResult.addFail(getString(MessageSeeds.ISSUE_DOES_NOT_EXIST, getThesaurus()), issueRef.getId(), "Issue (id = " + issueRef.getId() + ")");
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
    public Response closeIssues(CloseIssueRequest request, @Context SecurityContext securityContext, @BeanParam StandardParametersBean params) {
        /* TODO this method should be refactored when FE implements dynamic actions for bulk operations */
        User performer = (User) securityContext.getUserPrincipal();
        Function<ActionInfo, List<? extends Issue>> issueProvider;
        if (request.allIssues) {
            issueProvider = bulkResults -> getIssuesForBulk(params);
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
                        response.addFail(getString(MessageSeeds.ISSUE_ALREADY_CLOSED, getThesaurus()), issue.getId(), issue.getTitle());
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

    private Class<? extends IssueDataCollection> getQueryApiClass(StandardParametersBean params) {
        boolean isHistorical = false;
        boolean isActual = false;

        for (String status : params.get(STATUS)) {
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


    private Condition getQueryCondition(StandardParametersBean params) {
        Condition condition = Condition.TRUE;
        if (params.getQueryParameters().size() > 0) {
            condition = condition.and(addAssigneeQueryCondition(params));
            condition = condition.and(addReasonQueryCondition(params));
            condition = condition.and(addStatusQueryCondition(params));
            condition = condition.and(addMeterQueryCondition(params));
        }
        return condition;
    }

    private Condition addAssigneeQueryCondition(StandardParametersBean params) {
        Condition conditionAssignee = Condition.TRUE;
        if (!params.get(ASSIGNEE_ID).isEmpty()) {
            Long assigneeId = params.getFirstLong(ASSIGNEE_ID);
            if (assigneeId > 0) {
                String assigneeType = params.getFirst(ASSIGNEE_TYPE);
                if (getIssueService().checkIssueAssigneeType(assigneeType)) {
                    conditionAssignee = where("baseIssue." + params.getFirst(ASSIGNEE_TYPE).toLowerCase() + ".id").isEqualTo(assigneeId);
                }
            } else { // Filter by unassigned
                conditionAssignee = where("baseIssue.assigneeType").isNull();
            }
        }
        return conditionAssignee;
    }

    private Condition addReasonQueryCondition(StandardParametersBean params) {
        Condition conditionReason = Condition.FALSE;
        for (String reason : params.get(REASON)) {
            conditionReason = conditionReason.or(where("baseIssue.reason.key").isEqualTo(reason));
        }
        conditionReason = conditionReason == Condition.FALSE ? Condition.TRUE : conditionReason;
        final Condition finalConditionReason = conditionReason;
        return getIssueService()
                .findIssueType(IssueDataCollectionService.DATA_COLLECTION_ISSUE)
                .map(it -> finalConditionReason.and(where("baseIssue.reason.issueType").isEqualTo(it))).get();
    }

    private Condition addStatusQueryCondition(StandardParametersBean params) {
        Condition conditionStatus = Condition.FALSE;
        for (String status : params.get(STATUS)) {
            conditionStatus = conditionStatus.or(where("baseIssue.status.key").isEqualTo(status));
        }
        conditionStatus = conditionStatus == Condition.FALSE ? Condition.TRUE : conditionStatus;
        return conditionStatus;
    }

    private Condition addMeterQueryCondition(StandardParametersBean params) {
        Condition conditionMeter = Condition.FALSE;
        for (String meter : params.get(METER)) {
            conditionMeter = conditionMeter.or(where("baseIssue.device.mRID").isEqualTo(meter));
        }
        conditionMeter = conditionMeter == Condition.FALSE ? Condition.TRUE : conditionMeter;
        return conditionMeter;
    }
}