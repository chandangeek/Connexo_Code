package com.elster.jupiter.issue.rest.resource;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.request.AssignIssueRequest;
import com.elster.jupiter.issue.rest.request.CloseIssueRequest;
import com.elster.jupiter.issue.rest.request.CreateCommentRequest;
import com.elster.jupiter.issue.rest.response.*;
import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.issue.rest.response.issue.IssueInfo;
import com.elster.jupiter.issue.rest.response.issue.IssueListInfo;
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

import static com.elster.jupiter.util.conditions.Where.where;

@Path("/issue")
public class IssueResource extends BaseResource {

    public IssueResource() {
        super();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllIssues(@BeanParam StandardParametersBean params) {
        Class<? extends BaseIssue> apiClass = getQueryApiClass(params);

        Query<? extends BaseIssue> query = getIssueService().query(apiClass, EndDevice.class, User.class, IssueReason.class,
                IssueStatus.class, AssigneeRole.class, AssigneeTeam.class);
        Condition condition = getQueryCondition(params);
        List<? extends BaseIssue> list = query.select(condition, params.getFrom(), params.getTo(), params.getOrder());
        IssueListInfo resultList = new IssueListInfo(list, params.getStart(), params.getLimit());
        return Response.ok().entity(resultList).build();
    }

    @GET
    @Path("/groupedlist")
    @Produces(MediaType.APPLICATION_JSON)
    public IssueGroupListInfo getGroupedList(@BeanParam StandardParametersBean params) {
        List<GroupByReasonEntity> resultList = Collections.<GroupByReasonEntity>emptyList();
        if (params.get("field") != null) {
            try (TransactionContext context = getTransactionService().getContext()) {
                List<Long> ids = parseLongParams(params.get("id"));
                long id = ids.size() > 0 ? ids.get(0) : 0;
                GroupQueryBuilder builder = new GroupQueryBuilder();
                builder.setId(id).setFrom(params.getFrom()).setTo(params.getTo()).setStatuses(params.get("status"))
                        .setSourceClass(getQueryApiClass(params)).setGroupColumn(params.get("field").get(0));
                resultList = getIssueService().getIssueGroupList(builder);
                context.commit();
            }
        }
        return new IssueGroupListInfo(resultList, params.getStart(), params.getLimit());
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public RootEntity getIssueById(@PathParam("id") long id) {
        Optional<Issue> issue = getIssueService().findIssue(id, true);
        if (!issue.isPresent()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return new RootEntity<IssueInfo>(new IssueInfo<DeviceInfo>(issue.get(), DeviceInfo.class));
    }

    @GET
    @Path("/{id}/comments")
    @Produces(MediaType.APPLICATION_JSON)
    public IssueCommentListInfo getComments(@PathParam("id") long id, @BeanParam StandardParametersBean params) {
        Condition condition = where("issueId").isEqualTo(id);
        Query<IssueComment> query = getIssueService().query(IssueComment.class, User.class);
        List<IssueComment> commentsList = query.select(condition, params.getStart(), 0, params.getOrder());
        return new IssueCommentListInfo(commentsList);
    }

    @POST
    @Path("/{id}/comments")
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
        ActionInfo info = getTransactionService().execute(new CloseIssuesTransaction(request, getIssueService(), author));
        return new RootEntity<ActionInfo>(info);
    }

    @PUT
    @Path("/assign")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public RootEntity assignIssues(AssignIssueRequest request, @Context SecurityContext securityContext){
        User author = (User)securityContext.getUserPrincipal();
        ActionInfo info = getTransactionService().execute(new AssignIssueTransaction(request, getIssueService(), author));
        return new RootEntity<ActionInfo>(info);
    }

    private Class<? extends BaseIssue> getQueryApiClass(StandardParametersBean params){
        boolean isHistorical = false;
        boolean isActual = false;

        Query<IssueStatus> statusQuery = getIssueService().query(IssueStatus.class);
        for(Long status : parseLongParams(params.get("status"))) {
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
        } else if (isHistorical){
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
        }
        return condition;
    }

    private Condition addAssigneeQueryCondition(StandardParametersBean params) {
        Condition conditionAssignee = Condition.TRUE;
        if (params.get("assigneeId") != null) {
            Long assigneeId = Long.parseLong(params.get("assigneeId").get(0));
            if (assigneeId > 0) {
                if (params.get("assigneeType") != null) {
                    IssueAssigneeType assigneeType = IssueAssigneeType.fromString(params.get("assigneeType").get(0));
                    conditionAssignee = where(assigneeType.name().toLowerCase() + ".id").isEqualTo(assigneeId);
                }
            } else {
                conditionAssignee = where("type").isNull();
            }
        }
        return conditionAssignee;
    }

    private Condition addReasonQueryCondition(StandardParametersBean params) {
        Condition conditionReason = Condition.FALSE;
        for(Long reason : parseLongParams(params.get("reason"))) {
            conditionReason = conditionReason.or(where("reason.id").isEqualTo(reason));
        }
        conditionReason = conditionReason == Condition.FALSE ? Condition.TRUE : conditionReason;
        return conditionReason;
    }

    private Condition addStatusQueryCondition(StandardParametersBean params) {
        Condition conditionStatus = Condition.FALSE;
            for(Long status : parseLongParams(params.get("status"))) {
                conditionStatus = conditionStatus.or(where("status.id").isEqualTo(status));
            }
        conditionStatus = conditionStatus == Condition.FALSE ? Condition.TRUE : conditionStatus;
        return  conditionStatus;
    }
}