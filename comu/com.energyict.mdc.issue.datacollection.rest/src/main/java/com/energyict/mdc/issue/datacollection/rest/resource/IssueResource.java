package com.energyict.mdc.issue.datacollection.rest.resource;

import static com.elster.jupiter.issue.rest.i18n.MessageSeeds.ISSUE_DOES_NOT_EXIST;
import static com.elster.jupiter.issue.rest.i18n.MessageSeeds.getString;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.request.AssignIssueRequest;
import com.elster.jupiter.issue.rest.request.CloseIssueRequest;
import com.elster.jupiter.issue.rest.request.CreateCommentRequest;
import com.elster.jupiter.issue.rest.request.EntityReference;
import com.elster.jupiter.issue.rest.request.PerformActionRequest;
import com.elster.jupiter.issue.rest.resource.StandardParametersBean;
import com.elster.jupiter.issue.rest.response.ActionInfo;
import com.elster.jupiter.issue.rest.response.IssueCommentInfo;
import com.elster.jupiter.issue.rest.response.IssueGroupInfo;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleActionTypeInfo;
import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.issue.rest.transactions.AssignIssueTransaction;
import com.elster.jupiter.issue.rest.transactions.CreateCommentTransaction;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.issue.share.cep.IssueAction;
import com.elster.jupiter.issue.share.cep.IssueActionResult;
import com.elster.jupiter.issue.share.entity.AssigneeRole;
import com.elster.jupiter.issue.share.entity.AssigneeTeam;
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
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.entity.HistoricalIssueDataCollection;
import com.energyict.mdc.issue.datacollection.entity.IssueDataCollection;
import com.energyict.mdc.issue.datacollection.entity.OpenIssueDataCollection;
import com.energyict.mdc.issue.datacollection.rest.response.DataCollectionIssueInfoFactory;

@Path("/issue")
public class IssueResource extends BaseResource {
    
    private DataCollectionIssueInfoFactory issuesInfoFactory;
    
    @Inject
    public IssueResource(DataCollectionIssueInfoFactory dataCollectionIssuesInfoFactory) {
        this.issuesInfoFactory = dataCollectionIssuesInfoFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.VIEW_ISSUE,Privileges.ASSIGN_ISSUE,Privileges.CLOSE_ISSUE,Privileges.COMMENT_ISSUE,Privileges.ACTION_ISSUE})
    public PagedInfoList getAllIssues(@BeanParam StandardParametersBean params, @BeanParam QueryParameters queryParams) {
        validateMandatory(params, START, LIMIT);
        Class<? extends IssueDataCollection> apiClass = getQueryApiClass(params);
        Class<? extends Issue> eagerClass = getEagerApiClass(apiClass);
        Query<? extends IssueDataCollection> query = getIssueDataCollectionService().query(apiClass, eagerClass, EndDevice.class, User.class, IssueReason.class,
                IssueStatus.class, AssigneeRole.class, AssigneeTeam.class, IssueType.class);
        Condition condition = getQueryCondition(params);
        List<? extends IssueDataCollection> list = query.select(condition, params.getFrom(), params.getTo(), params.getOrder("baseIssue."));
        return PagedInfoList.asJson("data", issuesInfoFactory.asInfos(list), queryParams);
    }

    @GET
    @Path("/groupedlist")
    @Produces(MediaType.APPLICATION_JSON)
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
                    .from(params.getFrom()).to(params.getTo()); // Pagination
            resultList = getIssueService().getIssueGroupList(filter);
            context.commit();
        }
        return entity(resultList, IssueGroupInfo.class, params.getStart(), params.getLimit()).build();
    }

    @GET
    @Path("/{" + ID + "}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.VIEW_ISSUE,Privileges.ASSIGN_ISSUE,Privileges.CLOSE_ISSUE,Privileges.COMMENT_ISSUE,Privileges.ACTION_ISSUE})
    public Response getIssueById(@PathParam(ID) long id) {
        Optional<IssueDataCollection> issue = getIssueDataCollectionService().findIssue(id);
        return issue
                .map(i -> entity(issuesInfoFactory.asInfo(i, DeviceInfo.class)).build())
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    @GET
    @Path("/{" + ID + "}/comments")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.VIEW_ISSUE,Privileges.ASSIGN_ISSUE,Privileges.CLOSE_ISSUE,Privileges.COMMENT_ISSUE,Privileges.ACTION_ISSUE})
    public Response getComments(@PathParam(ID) long id, @BeanParam StandardParametersBean params) {
        Condition condition = where("issueId").isEqualTo(id);
        Query<IssueComment> query = getIssueService().query(IssueComment.class, User.class);
        List<IssueComment> commentsList = query.select(condition);
        return entity(commentsList, IssueCommentInfo.class).build();
    }

    @POST
    @Path("/{" + ID + "}/comments")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
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
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.VIEW_ISSUE,Privileges.ASSIGN_ISSUE,Privileges.CLOSE_ISSUE,Privileges.COMMENT_ISSUE,Privileges.ACTION_ISSUE})
    public Response getActions(@PathParam("id") long id) {
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
        
        List<IssueActionType> ruleActionTypes = query.select(condition);
        ruleActionTypes = ruleActionTypes.stream().filter(at -> {
            Optional<IssueAction> action = at.createIssueAction();
            return action.isPresent() && action.get().isApplicable(issueRef.get());
        }).collect(Collectors.toList());
        return entity(ruleActionTypes, CreationRuleActionTypeInfo.class).build();
    }

    @PUT
    @Path("/assign")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.ASSIGN_ISSUE)
    @Deprecated
    public Response assignIssues(AssignIssueRequest request, @Context SecurityContext securityContext) {
        /* TODO this method should be removed when FE implements dynamic actions */
        User author = (User) securityContext.getUserPrincipal();
        ActionInfo info = getTransactionService().execute(new AssignIssueTransaction(request, getIssueService(), author, getThesaurus()));
        return entity(info).build();
    }

    @PUT
    @Path("/close")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.CLOSE_ISSUE)
    @Deprecated
    public Response closeIssues(CloseIssueRequest request, @Context SecurityContext securityContext) {
        /* TODO this method should be removed when FE implements dynamic actions */
        ActionInfo response = new ActionInfo();
        try (TransactionContext context = getTransactionService().getContext()) {
            Optional<IssueStatus> status = getIssueService().findStatus(request.getStatus());
            if (request.getIssues() != null && status.isPresent() && status.get().isHistorical()) {
                for (EntityReference issueRef : request.getIssues()) {
                    Optional<OpenIssueDataCollection> issue = getIssueDataCollectionService().findOpenIssue(issueRef.getId());
                    if (!issue.isPresent()) {
                        response.addFail(getString(ISSUE_DOES_NOT_EXIST, getThesaurus()), issueRef.getId(), "Issue (id = " + issueRef.getId() + ")");
                    } else {
                        issue.get().addComment(request.getComment(), (User) securityContext.getUserPrincipal());
                        issue.get().close(status.get());
                        response.addSuccess(issueRef.getId());
                    }
                }
            } else {
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }
            context.commit();
        }
        return entity(response).build();
    }

    @GET
    @Path("/{" + ID + "}/actions/{" + KEY + "}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.VIEW_ISSUE,Privileges.ASSIGN_ISSUE,Privileges.CLOSE_ISSUE,Privileges.COMMENT_ISSUE,Privileges.ACTION_ISSUE})
    public Response getActionTypeById(@PathParam(KEY) long id){
        Optional<IssueActionType> actionTypeRef = getIssueActionService().findActionType(id);
        if (!actionTypeRef.isPresent()){
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return entity(new CreationRuleActionTypeInfo(actionTypeRef.get())).build();
    }


    @PUT
    @Path("/{" + ID + "}/actions/{" + KEY + "}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.ACTION_ISSUE)
    public Response performAction(@PathParam(ID) long id, PerformActionRequest request) {
        Optional<IssueDataCollection> issueRef = getIssueDataCollectionService().findIssue(id);
        if (!issueRef.isPresent()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        Optional<IssueActionType> action = getIssueActionService().findActionType(request.getId());
        if (!action.isPresent()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        IssueActionResult actionResult = getIssueActionService().executeAction(action.get(), issueRef.get(), request.getParameters());
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
                .findIssueType(IssueDataCollectionService.ISSUE_TYPE_UUID)
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