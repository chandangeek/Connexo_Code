package com.elster.jupiter.issue.rest.resource;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.response.AssigneeFilterListInfo;
import com.elster.jupiter.issue.rest.response.IssueAssigneeInfo;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

import static com.elster.jupiter.issue.rest.i18n.MessageSeeds.ISSUE_ASSIGNEE_UNASSIGNED;
import static com.elster.jupiter.issue.rest.i18n.MessageSeeds.getString;
import static com.elster.jupiter.issue.rest.request.RequestHelper.ASSIGNEE_TYPE;
import static com.elster.jupiter.issue.rest.request.RequestHelper.ID;
import static com.elster.jupiter.issue.rest.request.RequestHelper.LIKE;
import static com.elster.jupiter.issue.rest.request.RequestHelper.ME;
import static com.elster.jupiter.issue.rest.response.ResponseHelper.entity;
import static com.elster.jupiter.util.conditions.Where.where;

@Path("/assignees")
public class AssigneeResource extends BaseResource {
    /**
     * <b>API link</b>: <a href="http://confluence.eict.vpdc/display/JUPU/REST+API#RESTAPI-Getassignees">Get assignees</a><br />
     * <b>Pagination</b>: false<br />
     * <b>Mandatory parameters</b>: none<br />
     * <b>Optional parameters</b>: '{@value com.elster.jupiter.issue.rest.request.RequestHelper#LIKE}'<br />
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.VIEW_ISSUE,Privileges.ASSIGN_ISSUE,Privileges.CLOSE_ISSUE,Privileges.COMMENT_ISSUE,Privileges.ACTION_ISSUE})
    public AssigneeFilterListInfo getAllAssignees(@BeanParam StandardParametersBean params, @Context SecurityContext securityContext) {
        String searchText = params.getFirst(LIKE);
        Boolean findMe = Boolean.parseBoolean(params.getFirst(ME));

        if (searchText != null && !searchText.isEmpty()) {
            String dbSearchText = "%" + searchText + "%";
            Condition conditionUser = where("authenticationName").likeIgnoreCase(dbSearchText);
            Query<User> queryUser = getUserService().getUserQuery();
            List<User> listUsers = queryUser.select(conditionUser, Order.ascending("authname"));

            return new AssigneeFilterListInfo(listUsers);
        }
        return AssigneeFilterListInfo.defaults((User)securityContext.getUserPrincipal(), getThesaurus(), findMe);
    }

    /**
     * <b>API link</b>: none<br />
     * <b>Pagination</b>: false<br />
     * <b>Mandatory parameters</b>: '{@value com.elster.jupiter.issue.rest.request.RequestHelper#ID}', '{@value com.elster.jupiter.issue.rest.request.RequestHelper#ASSIGNEE_TYPE}'<br />
     * <b>Optional parameters</b>: none<br />
     */
    @GET
    @Path("/{" + ID + "}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.VIEW_ISSUE,Privileges.ASSIGN_ISSUE,Privileges.CLOSE_ISSUE,Privileges.COMMENT_ISSUE,Privileges.ACTION_ISSUE})
    public Response getAssignee(@PathParam(ID) long id, @QueryParam(ASSIGNEE_TYPE) String assigneeType){
        IssueAssignee assignee = getIssueService().findIssueAssignee(assigneeType, id);
        if (assignee == null) {
            //Takes care of Unassigned issues which would have userId of "-1"
            if (id < 0){
                String unassignedText = getString(ISSUE_ASSIGNEE_UNASSIGNED, getThesaurus());
                return entity(new IssueAssigneeInfo("UnexistingType", -1L, unassignedText)).build();
            }
            //Not unassigned, so this user really doesn't exist
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return entity(new IssueAssigneeInfo(assignee)).build();
    }

    @GET
    @Path("/users")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.VIEW_ISSUE,Privileges.ASSIGN_ISSUE,Privileges.CLOSE_ISSUE,Privileges.COMMENT_ISSUE,Privileges.ACTION_ISSUE})
    public Response getUsers(@BeanParam StandardParametersBean params) {
        String searchText = params.getFirst(LIKE);
        Condition condition = Condition.TRUE;
        if (searchText != null && !searchText.isEmpty()) {
            String dbSearchText = "%" + searchText + "%";
            condition = condition.and(where("authenticationName").likeIgnoreCase(dbSearchText));
        }
        Query<User> query = getUserService().getUserQuery();
        List<User> list = query.select(condition, Order.ascending("authenticationName"));
        return Response.ok(new AssigneeFilterListInfo(list)).build();
    }
}
