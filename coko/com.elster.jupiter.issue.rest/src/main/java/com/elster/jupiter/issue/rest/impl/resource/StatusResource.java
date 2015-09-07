package com.elster.jupiter.issue.rest.impl.resource;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.response.IssueStatusInfo;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.util.conditions.Condition;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

import static com.elster.jupiter.issue.rest.request.RequestHelper.ID;
import static com.elster.jupiter.issue.rest.response.ResponseHelper.entity;

@Path("/statuses")
public class StatusResource extends BaseResource {

    /**
     * <b>API link</b>: <a href="http://confluence.eict.vpdc/display/JUPU/REST+API#RESTAPI-Getstatuses">Get statuses</a><br />
     * <b>Pagination</b>: false<br />
     * <b>Mandatory parameters</b>: none<br />
     * <b>Optional parameters</b>: none<br />
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE,Privileges.Constants.ASSIGN_ISSUE,Privileges.Constants.CLOSE_ISSUE,Privileges.Constants.COMMENT_ISSUE,Privileges.Constants.ACTION_ISSUE})
    public Response getStatuses() {
        Query<IssueStatus> query = getIssueService().query(IssueStatus.class);
        List<IssueStatus> statuses = query.select(Condition.TRUE);
        return entity(statuses, IssueStatusInfo.class).build();
    }

    /**
     * <b>API link</b>: <a href="http://confluence.eict.vpdc/display/JUPU/REST+API#RESTAPI-Getstatuses">Get statuses</a><br />
     * <b>Pagination</b>: false<br />
     * <b>Mandatory parameters</b>: '{@value com.elster.jupiter.issue.rest.request.RequestHelper#ID}'<br />
     * <b>Optional parameters</b>: none<br />
     */
    @GET
    @Path("/{" + ID + "}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE,Privileges.Constants.ASSIGN_ISSUE,Privileges.Constants.CLOSE_ISSUE,Privileges.Constants.COMMENT_ISSUE,Privileges.Constants.ACTION_ISSUE})
    public Response getStatus(@PathParam(ID) String key){
        Optional<IssueStatus> statusRef = getIssueService().findStatus(key);
        if(!statusRef.isPresent()){
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return entity(new IssueStatusInfo(statusRef.get())).build();
    }
}
