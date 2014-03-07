package com.elster.jupiter.issue.rest.resource;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.request.AssignIssueRequest;
import com.elster.jupiter.issue.rest.request.CloseIssueRequest;
import com.elster.jupiter.issue.rest.response.ActionInfo;
import com.elster.jupiter.issue.rest.response.IssueGroupListInfo;
import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.issue.rest.response.issue.IssueInfo;
import com.elster.jupiter.issue.rest.response.issue.IssueListInfo;
import com.elster.jupiter.issue.rest.transactions.AssignIssueTransaction;
import com.elster.jupiter.issue.rest.transactions.CloseIssuesTransaction;
import com.elster.jupiter.issue.share.entity.*;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Condition;
import com.google.common.base.Optional;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.elster.jupiter.util.conditions.Where.where;

@Path("/issue")
public class IssueResource extends BaseResource {

    public IssueResource() {
        super();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllIssues(@BeanParam StandardParametersBean params) {
        Condition condition = Condition.TRUE;
        Query<Issue> query = getIssueMainService().query(Issue.class, EndDevice.class, Issue.class, User.class,
                IssueReason.class, IssueStatus.class, AssigneeRole.class, AssigneeTeam.class);
        if (params.get("reason") != null) {
            condition = where("reason.name").isEqualTo(params.get("reason").get(0));
        }
        List<Issue> list = query.select(condition, params.getFrom(), params.getTo(), params.getOrder());
        IssueListInfo resultList = new IssueListInfo(list, params.getStart(), params.getLimit());
        return Response.ok().entity(resultList).build();
    }

    @GET
    @Path("/groupedlist")
    @Produces(MediaType.APPLICATION_JSON)
    public IssueGroupListInfo getGroupedList(@BeanParam StandardParametersBean params) {
        Map<String, Long> resultMap = Collections.<String, Long>emptyMap();
        if (params.get("reason") != null) {
            try (TransactionContext context = getTransactionService().getContext()) {
                resultMap = getIssueService().getIssueGroupList(params.get("reason").get(0), false, params.getFrom(), params.getTo());
                context.commit();
            }
        }
        return new IssueGroupListInfo(resultMap, params.getStart(), params.getLimit());
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public IssueInfo getIssueById(@PathParam("id") long id) {
        Optional<Issue> issue = getIssueMainService().get(Issue.class, id);
        if (!issue.isPresent()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return new IssueInfo<DeviceInfo>(issue.get(), DeviceInfo.class);
    }

    @PUT
    @Path("/close")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ActionInfo closeIssues(CloseIssueRequest request){
        return getTransactionService().execute(new CloseIssuesTransaction(request, getIssueService(), getIssueMainService()));
    }

    @PUT
    @Path("/assign")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ActionInfo assignIssues(AssignIssueRequest request){
        return getTransactionService().execute(new AssignIssueTransaction(request, getIssueService()));
    }
}