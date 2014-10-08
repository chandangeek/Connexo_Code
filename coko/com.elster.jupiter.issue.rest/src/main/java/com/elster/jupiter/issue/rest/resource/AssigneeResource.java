package com.elster.jupiter.issue.rest.resource;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.response.AssigneeFilterListInfo;
import com.elster.jupiter.issue.rest.response.IssueAssigneeInfo;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.issue.share.entity.AssigneeRole;
import com.elster.jupiter.issue.share.entity.AssigneeTeam;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.Collections;
import java.util.List;

import static com.elster.jupiter.issue.rest.i18n.MessageSeeds.ISSUE_ASSIGNEE_UNASSIGNED;
import static com.elster.jupiter.issue.rest.i18n.MessageSeeds.getString;
import static com.elster.jupiter.issue.rest.request.RequestHelper.*;
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
    @RolesAllowed(Privileges.VIEW_ISSUE)
    public AssigneeFilterListInfo getAllAssignees(@BeanParam StandardParametersBean params, @Context SecurityContext securityContext) {
        String searchText = params.getFirst(LIKE);
        Boolean findMe = Boolean.parseBoolean(params.getFirst(ME));

        if (searchText != null && !searchText.isEmpty()) {
            String dbSearchText = "%" + searchText + "%";

            Condition condition = where("name").likeIgnoreCase(dbSearchText);
            Condition conditionUser = where("authenticationName").likeIgnoreCase(dbSearchText);

            Query<AssigneeTeam> queryTeam = getIssueService().query(AssigneeTeam.class);
            List<AssigneeTeam> listTeam = queryTeam.select(condition, Order.ascending("name"));

            Query<AssigneeRole> queryRole = getIssueService().query(AssigneeRole.class);
            List<AssigneeRole> listRole = queryRole.select(condition, Order.ascending("name"));

            Query<User> queryUser = getUserService().getUserQuery();
            List<User> listUsers = queryUser.select(conditionUser, Order.ascending("authname"));

            return new AssigneeFilterListInfo(listTeam, listRole, listUsers);
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
    @RolesAllowed(Privileges.VIEW_ISSUE)
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

    /**
     * <b>API link</b>: <a href="http://confluence.eict.vpdc/display/JUPU/REST+API#RESTAPI-Getassigneegroups%28teams%29">Get assignee groups (teams)</a><br />
     * <b>Pagination</b>: false<br />
     * <b>Mandatory parameters</b>: none<br />
     * <b>Optional parameters</b>: none<br />
     */
    @GET
    @Path("/groups")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_ISSUE)
    public Response getGroups() {
        Query<AssigneeTeam> query = getIssueService().query(AssigneeTeam.class);
        List<AssigneeTeam> list = query.select(Condition.TRUE);
        return entity(list, IssueAssigneeInfo.class).build();
    }

    /**
     * <b>API link</b>: <a href="http://confluence.eict.vpdc/display/JUPU/REST+API#RESTAPI-Getassigneeroles">Get assignee roles</a><br />
     * <b>Pagination</b>: false<br />
     * <b>Mandatory parameters</b>: none<br />
     * <b>Optional parameters</b>: none<br />
     */
    @GET
    @Path("/roles")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_ISSUE)
    public Response getTeams() {
        Query<AssigneeRole> query = getIssueService().query(AssigneeRole.class);
        List<AssigneeRole> list = query.select(Condition.TRUE);
        return entity(list, IssueAssigneeInfo.class).build();
    }

    @GET
    @Path("/users")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_ISSUE)
    public Response getUsers(@BeanParam StandardParametersBean params) {
        String searchText = params.getFirst(LIKE);
        Condition condition = Condition.TRUE;
        if (searchText != null && !searchText.isEmpty()) {
            String dbSearchText = "%" + searchText + "%";
            condition = condition.and(where("authenticationName").likeIgnoreCase(dbSearchText));
        }
        Query<User> query = getUserService().getUserQuery();
        List<User> list = query.select(condition, Order.ascending("authenticationName"));
        return Response.ok(new AssigneeFilterListInfo(Collections.<AssigneeTeam>emptyList(), Collections.<AssigneeRole>emptyList(), list)).build();
    }
}
