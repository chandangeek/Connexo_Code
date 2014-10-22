package com.elster.jupiter.issue.rest.resource;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.response.IssueStatusInfo;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.util.conditions.Condition;
import java.util.Optional;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

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
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_ISSUE)
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
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_ISSUE)
    public Response getStatus(@PathParam(ID) String key){
        Optional<IssueStatus> statusRef = getIssueService().findStatus(key);
        if(!statusRef.isPresent()){
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return entity(new IssueStatusInfo(statusRef.get())).build();
    }
}
