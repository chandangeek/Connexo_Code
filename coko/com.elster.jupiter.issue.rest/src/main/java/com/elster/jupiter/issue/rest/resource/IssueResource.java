package com.elster.jupiter.issue.rest.resource;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.request.AssignIssueRequest;
import com.elster.jupiter.issue.rest.request.CloseIssueRequest;
import com.elster.jupiter.issue.rest.request.CreateCommentRequest;
import com.elster.jupiter.issue.rest.request.PerformActionRequest;
import com.elster.jupiter.issue.rest.response.ActionInfo;
import com.elster.jupiter.issue.rest.response.IssueCommentInfo;
import com.elster.jupiter.issue.rest.response.IssueGroupInfo;
import com.elster.jupiter.issue.rest.response.RootEntity;
import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.issue.rest.response.issue.IssueInfo;
import com.elster.jupiter.issue.rest.transactions.AssignIssueTransaction;
import com.elster.jupiter.issue.rest.transactions.CloseIssuesTransaction;
import com.elster.jupiter.issue.rest.transactions.CreateCommentTransaction;
import com.elster.jupiter.issue.share.entity.*;
import com.elster.jupiter.issue.share.service.GroupQueryBuilder;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Condition;
import com.google.common.base.Optional;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import java.util.Collections;
import java.util.List;

import static com.elster.jupiter.issue.rest.request.RequestHelper.*;
import static com.elster.jupiter.issue.rest.response.ResponseHelper.ok;
import static com.elster.jupiter.util.conditions.Where.where;

@Path("/issue")
public class IssueResource extends BaseResource {

    /**
     * <b>API link</b>: <a href="http://confluence.eict.vpdc/display/JUPU/REST+API#RESTAPI-Getthelistofissues">Get the list of issues</a><br />
     * <b>Pagination</b>: true<br />
     * <b>Mandatory parameters</b>:
     *      '{@value com.elster.jupiter.issue.rest.request.RequestHelper#START}',
     *      '{@value com.elster.jupiter.issue.rest.request.RequestHelper#LIMIT}',
     *      '{@value com.elster.jupiter.issue.rest.request.RequestHelper#ISSUE_TYPE}'
     * <br />
     * <b>Optional parameters</b>:
     *      '{@value com.elster.jupiter.issue.rest.request.RequestHelper#SORT}',
     *      '{@value com.elster.jupiter.issue.rest.request.RequestHelper#REASON}',
     *      '{@value com.elster.jupiter.issue.rest.request.RequestHelper#ASSIGNEE_ID}'
     *      '{@value com.elster.jupiter.issue.rest.request.RequestHelper#ASSIGNEE_TYPE}'
     *      '{@value com.elster.jupiter.issue.rest.request.RequestHelper#STATUS}'
     *      '{@value com.elster.jupiter.issue.rest.request.RequestHelper#METER}'
     * <br />
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllIssues(@BeanParam StandardParametersBean params) {
        validateMandatory(params, ISSUE_TYPE, START, LIMIT);
        Class<? extends BaseIssue> apiClass = getQueryApiClass(params);

        Query<? extends BaseIssue> query = getIssueService().query(apiClass, EndDevice.class, User.class, IssueReason.class,
                IssueStatus.class, AssigneeRole.class, AssigneeTeam.class, IssueType.class);
        Condition condition = getQueryCondition(params);
        List<? extends BaseIssue> list = query.select(condition, params.getFrom(), params.getTo(), params.getOrder());
        return ok(list, IssueInfo.class, params.getStart(), params.getLimit()).build();
    }

    /**
     * <b>API link</b>: <a href="http://confluence.eict.vpdc/display/JUPU/REST+API#RESTAPI-Getgroups">Get groups</a><br />
     * <b>Pagination</b>: true<br />
     * <b>Mandatory parameters</b>:
     *      '{@value com.elster.jupiter.issue.rest.request.RequestHelper#START}',
     *      '{@value com.elster.jupiter.issue.rest.request.RequestHelper#LIMIT}',
     *      '{@value com.elster.jupiter.issue.rest.request.RequestHelper#ISSUE_TYPE}'
     *      '{@value com.elster.jupiter.issue.rest.request.RequestHelper#FIELD}'
     * <br />
     * <b>Optional parameters</b>:
     *      '{@value com.elster.jupiter.issue.rest.request.RequestHelper#ID}',
     *      '{@value com.elster.jupiter.issue.rest.request.RequestHelper#ASSIGNEE_ID}'
     *      '{@value com.elster.jupiter.issue.rest.request.RequestHelper#ASSIGNEE_TYPE}'
     *      '{@value com.elster.jupiter.issue.rest.request.RequestHelper#METER}'
     * <br />
     */
    @GET
    @Path("/groupedlist")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGroupedList(@BeanParam StandardParametersBean params) {
        validateMandatory(params, ISSUE_TYPE, START, LIMIT, FIELD);
        List<GroupByReasonEntity> resultList = Collections.<GroupByReasonEntity>emptyList();
        try (TransactionContext context = getTransactionService().getContext()) {
            GroupQueryBuilder builder = new GroupQueryBuilder();
            builder.setId(params.getFirstLong(ID)) // Reason id
                    .setFrom(params.getFrom()).setTo(params.getTo()) // Pagination
                    .setSourceClass(getQueryApiClass(params)) // Issues, Historical Issues or Both
                    .setGroupColumn(params.getFirst(FIELD)) // Main grouping column
                    .setIssueType(params.getFirst(ISSUE_TYPE)) // Reasons only with specific issue type
                    .setStatuses(params.get(STATUS)) // All selected statuses
                    .setAssigneeType(params.getFirst(ASSIGNEE_TYPE)) // User, Group ot Role type of assignee
                    .setAssigneeId(params.getFirstLong(ASSIGNEE_ID)) // Id of selected assignee
                    .setMeterId(params.getFirstLong(METER)); // Filter by meter MRID
            resultList = getIssueService().getIssueGroupList(builder);
            context.commit();
        }
        return ok(resultList, IssueGroupInfo.class, params.getStart(), params.getLimit()).build();
    }

    /**
     * <b>API link</b>: <a href="http://confluence.eict.vpdc/display/JUPU/REST+API#RESTAPI-Getissuedetails">Get issue details</a><br />
     * <b>Pagination</b>: false<br />
     * <b>Mandatory parameters</b>: '{@value com.elster.jupiter.issue.rest.request.RequestHelper#ID}'<br />
     * <b>Optional parameters</b>: none<br />
     */
    @GET
    @Path("/{" + ID + "}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getIssueById(@PathParam(ID) long id) {
        Optional<Issue> issue = getIssueService().findIssue(id, true);
        if (!issue.isPresent()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return ok(new IssueInfo<DeviceInfo>(issue.get(), DeviceInfo.class)).build();
    }

    /**
     * <b>API link</b>: <a href="http://confluence.eict.vpdc/display/JUPU/REST+API#RESTAPI-Viewcommentsfortheissue">View comments for the issue</a><br />
     * <b>Pagination</b>: false<br />
     * <b>Mandatory parameters</b>: '{@value com.elster.jupiter.issue.rest.request.RequestHelper#ID}'<br />
     * <b>Optional parameters</b>: none<br />
     */
    @GET
    @Path("/{" + ID + "}/comments")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getComments(@PathParam(ID) long id, @BeanParam StandardParametersBean params) {
        Condition condition = where("issueId").isEqualTo(id);
        Query<IssueComment> query = getIssueService().query(IssueComment.class, User.class);
        List<IssueComment> commentsList = query.select(condition);
        return ok(commentsList, IssueCommentInfo.class).build();
    }

    @POST
    @Path("/{" + ID + "}/comments")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postComment(@PathParam("id") long id, CreateCommentRequest request, @Context SecurityContext securityContext) {
        User author = (User)securityContext.getUserPrincipal();
        if (request.getComment() == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        IssueComment comment = getTransactionService().execute(new CreateCommentTransaction(id, request.getComment(), author, getIssueService()));
        return Response.status(Response.Status.CREATED).entity(new RootEntity<IssueCommentInfo>(new IssueCommentInfo(comment))).build();
    }

    @PUT
    @Path("/close")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public RootEntity closeIssues(CloseIssueRequest request, @Context SecurityContext securityContext){
        User author = (User)securityContext.getUserPrincipal();
        ActionInfo info = getTransactionService().execute(new CloseIssuesTransaction(request, getIssueService(), author, getThesaurus()));
        return new RootEntity<ActionInfo>(info);
    }

    @PUT
    @Path("/assign")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public RootEntity assignIssues(AssignIssueRequest request, @Context SecurityContext securityContext){
        User author = (User)securityContext.getUserPrincipal();
        ActionInfo info = getTransactionService().execute(new AssignIssueTransaction(request, getIssueService(), author, getThesaurus()));
        return new RootEntity<ActionInfo>(info);
    }
    
    @PUT
    @Path("{" + ID + "}/action")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response performAction(@PathParam(ID) long id, PerformActionRequest request) {
        Optional<Issue> issue = getIssueService().findIssue(id, true);
        if (!issue.isPresent()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        Optional<IssueActionType> action = getIssueActionService().findActionType(request.getId());
        if (!action.isPresent()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        
        getIssueActionService().executeAction(action.get(), issue.get(), request.getParameters());
        return Response.ok().build();
    }

    private Class<? extends BaseIssue> getQueryApiClass(StandardParametersBean params){
        boolean isHistorical = false;
        boolean isActual = false;

        Query<IssueStatus> statusQuery = getIssueService().query(IssueStatus.class);
        for(Long status : params.getLong("status")) {
            Optional<IssueStatus> issueStatusRef = statusQuery.get(status);
            if (issueStatusRef.isPresent()) {
                if ( issueStatusRef.get().isFinal()) {
                    isHistorical = true;
                } else {
                    isActual = true;
                }
            }
        }

        Class<? extends BaseIssue> apiClass = BaseIssue.class;
        if (isActual && !isHistorical) {
            apiClass = Issue.class;
        } else if (isHistorical && !isActual){
            apiClass = HistoricalIssue.class;
        }
        return apiClass;
    }

    private Condition getQueryCondition(StandardParametersBean params) {
        Condition condition = Condition.TRUE;
        if(params.getQueryParameters().size() > 0) {
            condition = condition.and(addAssigneeQueryCondition(params));
            condition = condition.and(addReasonQueryCondition(params));
            condition = condition.and(addStatusQueryCondition(params));
            condition = condition.and(addMeterQueryCondition(params));
        }
        return condition;
    }

    private Condition addAssigneeQueryCondition(StandardParametersBean params) {
        Condition conditionAssignee = Condition.TRUE;
        if (params.get(ASSIGNEE_ID) != null) {
            Long assigneeId = params.getFirstLong(ASSIGNEE_ID);
            if (assigneeId > 0) {
                String assigneeType = params.getFirst(ASSIGNEE_TYPE);
                if (getIssueService().checkIssueAssigneeType(assigneeType)) {
                    conditionAssignee = where(params.getFirst(ASSIGNEE_TYPE).toLowerCase() + ".id").isEqualTo(assigneeId);
                }
            } else {
                conditionAssignee = where("assigneeType").isNull();
            }
        }
        return conditionAssignee;
    }

    private Condition addReasonQueryCondition(StandardParametersBean params) {
        Condition conditionReason = Condition.FALSE;
        for(Long reason : params.getLong(REASON)) {
            conditionReason = conditionReason.or(where("reason.id").isEqualTo(reason));
        }
        conditionReason = conditionReason == Condition.FALSE ? Condition.TRUE : conditionReason;
        IssueType issueType = getIssueService().findIssueType(params.getFirst(ISSUE_TYPE)).orNull();
        conditionReason = conditionReason.and(where("reason.issueType").isEqualTo(issueType));
        return conditionReason;
    }

    private Condition addStatusQueryCondition(StandardParametersBean params) {
        Condition conditionStatus = Condition.FALSE;
            for(Long status : params.getLong(STATUS)) {
                conditionStatus = conditionStatus.or(where("status.id").isEqualTo(status));
            }
        conditionStatus = conditionStatus == Condition.FALSE ? Condition.TRUE : conditionStatus;
        return  conditionStatus;
    }

    private Condition addMeterQueryCondition(StandardParametersBean params) {
        Condition conditionMeter = Condition.FALSE;
        for(Long meter : params.getLong(METER)) {
            conditionMeter = conditionMeter.or(where("device.id").isEqualTo(meter));
        }
        conditionMeter = conditionMeter == Condition.FALSE ? Condition.TRUE : conditionMeter;
        return conditionMeter;
    }
}