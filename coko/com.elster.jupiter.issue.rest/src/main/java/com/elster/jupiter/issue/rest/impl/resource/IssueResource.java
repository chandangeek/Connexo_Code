package com.elster.jupiter.issue.rest.impl.resource;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.request.CreateCommentRequest;
import com.elster.jupiter.issue.rest.resource.StandardParametersBean;
import com.elster.jupiter.issue.rest.response.IssueCommentInfo;
import com.elster.jupiter.issue.rest.response.IssueGroupInfo;
import com.elster.jupiter.issue.rest.transactions.CreateCommentTransaction;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.issue.share.entity.*;
import com.elster.jupiter.issue.share.service.IssueGroupFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

@Path("/issues")
public class IssueResource extends BaseResource {

    @GET
    @Path("/groupedlist")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_ISSUE, Privileges.ASSIGN_ISSUE, Privileges.CLOSE_ISSUE, Privileges.COMMENT_ISSUE, Privileges.ACTION_ISSUE})
    public PagedInfoList getGroupedList(@BeanParam StandardParametersBean params, @BeanParam JsonQueryParameters queryParameters) {
        List<IssueGroup> resultList;
        try (TransactionContext context = getTransactionService().getContext()) {
            IssueGroupFilter filter = new IssueGroupFilter();
            filter.using(getQueryApiClass(params)) // Issues, Historical Issues or Both
                    .onlyGroupWithKey(params.getFirst("id")) // Reason id
                    .withIssueType(params.getFirst("issueType")) // Reasons only with specific issue type
                    .withStatuses(params.get("status")) // All selected statuses
                    .withAssigneeType(params.getFirst("assigneeType")) // User, Group or Role type of assignee
                    .withAssigneeId(params.getFirstLong("assigneeId")) // Id of selected assignee
                    .withMeterMrid(params.getFirst("meter")) // Filter by meter MRID
                    .groupBy(params.getFirst("field")) // Main grouping column
                    .setAscOrder(false) // Sorting (descending direction)
                    .from(params.getFrom()).to(params.getTo()); // Pagination
            resultList = getIssueService().getIssueGroupList(filter);
            context.commit();
        }
        List<IssueGroupInfo> infos = resultList.stream().map(IssueGroupInfo::new).collect(Collectors.toList());
        return PagedInfoList.fromPagedList("issueGroups", infos, queryParameters);
    }

    @GET
    @Path("/{id}/comments")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_ISSUE, Privileges.ASSIGN_ISSUE, Privileges.CLOSE_ISSUE, Privileges.COMMENT_ISSUE, Privileges.ACTION_ISSUE})
    public PagedInfoList getComments(@PathParam("id") long id, @BeanParam JsonQueryParameters queryParameters) {
        Condition condition = where("issueId").isEqualTo(id);
        Query<IssueComment> query = getIssueService().query(IssueComment.class, User.class);
        List<IssueComment> commentsList = query.select(condition, Order.ascending("createTime"));
        List<IssueCommentInfo> infos = commentsList.stream().map(IssueCommentInfo::new).collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("comments", infos, queryParameters);
    }

    @POST
    @Path("/{id}/comments")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.COMMENT_ISSUE)
    public Response postComment(@PathParam("id") long id, CreateCommentRequest request, @Context SecurityContext securityContext) {
        User author = (User) securityContext.getUserPrincipal();
        if (request.getComment() == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        IssueComment comment = getTransactionService().execute(new CreateCommentTransaction(id, request.getComment(), author, getIssueService()));
        return Response.ok(new IssueCommentInfo(comment)).status(Response.Status.CREATED).build();
    }

    private Class<? extends Issue> getQueryApiClass(StandardParametersBean params) {
        List<IssueStatus> statuses = params.get("status").stream().map(s -> getIssueService().findStatus(s).get()).collect(Collectors.toList());
        if (statuses.isEmpty()) {
            return Issue.class;
        }
        if (statuses.stream().allMatch(status -> !status.isHistorical())) {
            return OpenIssue.class;
        }
        if (statuses.stream().allMatch(status -> status.isHistorical())) {
            return HistoricalIssue.class;
        }
        return Issue.class;
    }
}
