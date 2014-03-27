package com.elster.jupiter.issue.rest.resource;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.response.AssignListInfo;
import com.elster.jupiter.issue.rest.response.AssigneeFilterListInfo;
import com.elster.jupiter.issue.share.entity.AssigneeRole;
import com.elster.jupiter.issue.share.entity.AssigneeTeam;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Condition;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.util.List;

import static com.elster.jupiter.util.conditions.Where.where;

@Path("/assignees")
public class AssigneeResource extends BaseResource {
    public AssigneeResource() {
        super();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public AssigneeFilterListInfo getAllAssignees(@Context UriInfo uriInfo, @Context SecurityContext securityContext) {
        // We shouldn't return aything if the 'like' parameter is absent or it is an empty string.
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        if (queryParameters.get("like") != null) {
            String searchText = queryParameters.get("like").get(0);
            if (!searchText.isEmpty()){
                String dbSearchText = "%" + searchText + "%";

                Condition condition = where("name").likeIgnoreCase(dbSearchText);
                Condition conditionUser = where("authenticationName").likeIgnoreCase(dbSearchText);

                Query<AssigneeTeam> queryTeam = getIssueService().query(AssigneeTeam.class);
                List<AssigneeTeam> listTeam = getQueryService().wrap(queryTeam).select(queryParameters, condition, "name");

                Query<AssigneeRole> queryRole = getIssueService().query(AssigneeRole.class);
                List<AssigneeRole> listRole = getQueryService().wrap(queryRole).select(queryParameters, condition, "name");

                Query<User> queryUser = getUserService().getUserQuery();
                List<User> listUsers = getQueryService().wrap(queryUser).select(queryParameters, conditionUser, "authname");

                return new AssigneeFilterListInfo(listTeam, listRole, listUsers);
            }
        }
        return AssigneeFilterListInfo.defaults((User)securityContext.getUserPrincipal());
    }


    @GET
    @Path("/groups")
    @Produces(MediaType.APPLICATION_JSON)
    public AssignListInfo getGroups(@Context UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        Query<AssigneeTeam> query = getIssueService().query(AssigneeTeam.class);
        List<AssigneeTeam> list = getQueryService().wrap(query).select(queryParameters);
        return new AssignListInfo(list);
    }

    @GET
    @Path("/roles")
    @Produces(MediaType.APPLICATION_JSON)
    public AssignListInfo getTeams(@Context UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        Query<AssigneeRole> query = getIssueService().query(AssigneeRole.class);
        List<AssigneeRole> list = getQueryService().wrap(query).select(queryParameters);
        return new AssignListInfo(list);
    }
}
