package com.energyict.mdc.issue.datacollection.rest.resource;

import static com.elster.jupiter.issue.rest.request.RequestHelper.ASSIGNEE_ID;
import static com.elster.jupiter.issue.rest.request.RequestHelper.ASSIGNEE_TYPE;
import static com.elster.jupiter.issue.rest.request.RequestHelper.FIELD;
import static com.elster.jupiter.issue.rest.request.RequestHelper.ID;
import static com.elster.jupiter.issue.rest.request.RequestHelper.ISSUE_TYPE;
import static com.elster.jupiter.issue.rest.request.RequestHelper.KEY;
import static com.elster.jupiter.issue.rest.request.RequestHelper.LIMIT;
import static com.elster.jupiter.issue.rest.request.RequestHelper.METER;
import static com.elster.jupiter.issue.rest.request.RequestHelper.REASON;
import static com.elster.jupiter.issue.rest.request.RequestHelper.START;
import static com.elster.jupiter.issue.rest.request.RequestHelper.STATUS;
import static com.elster.jupiter.issue.rest.response.ResponseHelper.entity;
import static com.elster.jupiter.util.conditions.Where.where;
import static com.energyict.mdc.issue.datacollection.rest.i18n.MessageSeeds.getString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.request.AssignIssueRequest;
import com.elster.jupiter.issue.rest.request.BulkIssueRequest;
import com.elster.jupiter.issue.rest.request.CloseIssueRequest;
import com.elster.jupiter.issue.rest.request.CreateCommentRequest;
import com.elster.jupiter.issue.rest.request.EntityReference;
import com.elster.jupiter.issue.rest.request.PerformActionRequest;
import com.elster.jupiter.issue.rest.resource.StandardParametersBean;
import com.elster.jupiter.issue.rest.response.ActionInfo;
import com.elster.jupiter.issue.rest.response.IssueCommentInfo;
import com.elster.jupiter.issue.rest.response.IssueGroupInfo;
import com.elster.jupiter.issue.rest.response.PropertyUtils;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleActionInfoFactory;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleActionTypeInfo;
import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.issue.rest.transactions.AssignIssueTransaction;
import com.elster.jupiter.issue.rest.transactions.CreateCommentTransaction;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.issue.share.IssueAction;
import com.elster.jupiter.issue.share.IssueActionResult;
import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueActionType;
import com.elster.jupiter.issue.share.entity.IssueComment;
import com.elster.jupiter.issue.share.entity.IssueGroup;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueGroupFilter;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.entity.HistoricalIssueDataCollection;
import com.energyict.mdc.issue.datacollection.entity.IssueDataCollection;
import com.energyict.mdc.issue.datacollection.entity.OpenIssueDataCollection;
import com.energyict.mdc.issue.datacollection.rest.i18n.MessageSeeds;
import com.energyict.mdc.issue.datacollection.rest.response.DataCollectionIssueInfoFactory;

@Path("/issue")
public class IssueResource extends BaseResource {
    
    private final DataCollectionIssueInfoFactory issuesInfoFactory;
    private final CreationRuleActionInfoFactory actionInfoFactory;
    private final PropertyUtils propertyUtils;

    @Inject
    public IssueResource(DataCollectionIssueInfoFactory dataCollectionIssuesInfoFactory, CreationRuleActionInfoFactory actionInfoFactory, PropertyUtils propertyUtils) {
        this.issuesInfoFactory = dataCollectionIssuesInfoFactory;
        this.actionInfoFactory = actionInfoFactory;
        this.propertyUtils = propertyUtils;
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
    @Path("/groupedlist")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_ISSUE,Privileges.ASSIGN_ISSUE,Privileges.CLOSE_ISSUE,Privileges.COMMENT_ISSUE,Privileges.ACTION_ISSUE})
    public Response getGroupedList(@BeanParam StandardParametersBean params) {
        validateMandatory(params, ISSUE_TYPE, START, LIMIT, FIELD);
        List<IssueGroup> resultList = Collections.<IssueGroup>emptyList();
        try (TransactionContext context = getTransactionService().getContext()) {
            IssueGroupFilter filter = new IssueGroupFilter();
            filter.using(getQueryApiClass(params)) // Issues, Historical Issues or Both
                    .onlyGroupWithKey(params.getFirst(ID)) // Reason id
                    .withIssueType(params.getFirst(ISSUE_TYPE)) // Reasons only with specific issue type
                    .withStatuses(params.get(STATUS)) // All selected statuses
                    .withAssigneeType(params.getFirst(ASSIGNEE_TYPE)) // User, Group or Role type of assignee
                    .withAssigneeId(params.getFirstLong(ASSIGNEE_ID)) // Id of selected assignee
                    .withMeterMrid(params.getFirst(METER)) // Filter by meter MRID
                    .groupBy(params.getFirst(FIELD)) // Main grouping column
                    .setAscOrder(false) // Sorting (descending direction)
                    .from(params.getFrom()).to(params.getTo()); // Pagination
            resultList = getIssueService().getIssueGroupList(filter);
            context.commit();
        }
        return entity(resultList, IssueGroupInfo.class, params.getStart(), params.getLimit()).build();
    }

    @GET
    @Path("/{" + ID + "}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_ISSUE,Privileges.ASSIGN_ISSUE,Privileges.CLOSE_ISSUE,Privileges.COMMENT_ISSUE,Privileges.ACTION_ISSUE})
    public Response getIssueById(@PathParam(ID) long id) {
        Optional<IssueDataCollection> issue = getIssueDataCollectionService().findIssue(id);
        return issue
                .map(i -> entity(issuesInfoFactory.asInfo(i, DeviceInfo.class)).build())
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    @GET
    @Path("/{" + ID + "}/comments")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_ISSUE,Privileges.ASSIGN_ISSUE,Privileges.CLOSE_ISSUE,Privileges.COMMENT_ISSUE,Privileges.ACTION_ISSUE})
    public Response getComments(@PathParam(ID) long id, @BeanParam StandardParametersBean params) {
        Condition condition = where("issueId").isEqualTo(id);
        Query<IssueComment> query = getIssueService().query(IssueComment.class, User.class);
        List<IssueComment> commentsList = query.select(condition, Order.ascending("createTime"));
        return entity(commentsList, IssueCommentInfo.class).build();
    }

    @POST
    @Path("/{" + ID + "}/comments")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.COMMENT_ISSUE)
    public Response postComment(@PathParam("id") long id, CreateCommentRequest request, @Context SecurityContext securityContext) {
        User author = (User) securityContext.getUserPrincipal();
        if (request.getComment() == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        IssueComment comment = getTransactionService().execute(new CreateCommentTransaction(id, request.getComment(), author, getIssueService()));
        return entity(new IssueCommentInfo(comment)).status(Response.Status.CREATED).build();
    }

    @GET
    @Path("/{" + ID + "}/actions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_ISSUE,Privileges.ASSIGN_ISSUE,Privileges.CLOSE_ISSUE,Privileges.COMMENT_ISSUE,Privileges.ACTION_ISSUE})
    public PagedInfoList getActions(@PathParam("id") long id, @BeanParam JsonQueryParameters queryParameters) {
        Optional<IssueDataCollection> issueRef = getIssueDataCollectionService().findIssue(id);
        if (!issueRef.isPresent()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        Query<IssueActionType> query = getIssueService().query(IssueActionType.class, IssueType.class);
        
        IssueReason reason = issueRef.get().getReason();
        IssueType type = reason.getIssueType();
        
        Condition c0 = where("issueType").isNull();
        Condition c1 = where("issueType").isEqualTo(type).and(where("issueReason").isNull());
        Condition c2 = where("issueType").isEqualTo(type).and(where("issueReason").isEqualTo(reason));
        Condition condition = (c0).or(c1).or(c2);
        
        List<CreationRuleActionTypeInfo> infos = query.select(condition)
                               .stream()
                               .filter(actionType -> actionType.createIssueAction()
                                           .map(action -> action.isApplicable(issueRef.get()))
                                           .orElse(false))
                               .map(actionInfoFactory::asInfo)
                               .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("issueActions", infos, queryParameters);
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
        Function<ActionInfo, List<? extends Issue>> issueProvider = null;
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
        Function<ActionInfo, List<? extends Issue>> issueProvider = null;
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

    @GET
    @Path("/{" + ID + "}/actions/{" + KEY + "}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_ISSUE,Privileges.ASSIGN_ISSUE,Privileges.CLOSE_ISSUE,Privileges.COMMENT_ISSUE,Privileges.ACTION_ISSUE})
    public Response getActionTypeById(@PathParam(KEY) long id){
        IssueActionType actionType = getIssueActionService().findActionType(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return Response.ok(actionInfoFactory.asInfo(actionType)).build();
    }


    @PUT
    @Path("/{" + ID + "}/actions/{" + KEY + "}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ACTION_ISSUE)
    public Response performAction(@PathParam(ID) long id, PerformActionRequest request) {
        IssueDataCollection issue = getIssueDataCollectionService().findIssue(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        IssueActionType action = getIssueActionService().findActionType(request.id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        Map<String, Object> properties = new HashMap<>();
        for (PropertySpec propertySpec : action.createIssueAction().get().getPropertySpecs()) {
            Object value = propertyUtils.findPropertyValue(propertySpec, request.properties);
            if (value != null) {
                properties.put(propertySpec.getName(), value);
            }
        }
        IssueActionResult actionResult;
        try (TransactionContext context = getTransactionService().getContext()) {
            actionResult = getIssueActionService().executeAction(action, issue, properties);
            context.commit();
        }
        return entity(actionResult).build();
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