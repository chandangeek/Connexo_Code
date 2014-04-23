package com.elster.jupiter.issue.rest.resource;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.response.AssigneeFilterListInfo;
import com.elster.jupiter.issue.rest.response.IssueAssigneeInfo;
import com.elster.jupiter.issue.share.entity.AssigneeRole;
import com.elster.jupiter.issue.share.entity.AssigneeTeam;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

import static com.elster.jupiter.issue.rest.request.RequestHelper.*;
import static com.elster.jupiter.issue.rest.response.ResponseHelper.ok;
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
    public AssigneeFilterListInfo getAllAssignees(@BeanParam StandardParametersBean params, @Context SecurityContext securityContext) {
        String searchText = params.getFirst(LIKE);
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
        return AssigneeFilterListInfo.defaults((User)securityContext.getUserPrincipal(), getThesaurus());
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
    public Response getAssignee(@PathParam(ID) long id, @QueryParam(ASSIGNEE_TYPE) String assigneeType){
        IssueAssignee assignee = getIssueService().findIssueAssignee(assigneeType, id);
        if (assignee == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return ok(new IssueAssigneeInfo(assignee)).build();
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
    public Response getGroups() {
        Query<AssigneeTeam> query = getIssueService().query(AssigneeTeam.class);
        List<AssigneeTeam> list = query.select(Condition.TRUE);
        return ok(list, IssueAssigneeInfo.class).build();
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
    public Response getTeams() {
        Query<AssigneeRole> query = getIssueService().query(AssigneeRole.class);
        List<AssigneeRole> list = query.select(Condition.TRUE);
        return ok(list, IssueAssigneeInfo.class).build();
    }
}
