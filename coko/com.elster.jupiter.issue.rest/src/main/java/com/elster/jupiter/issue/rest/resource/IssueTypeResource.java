package com.elster.jupiter.issue.rest.resource;

import com.elster.jupiter.issue.rest.response.IssueTypeInfo;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.util.conditions.Condition;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static com.elster.jupiter.issue.rest.response.ResponseHelper.ok;

@Path("/issuetypes")
public class IssueTypeResource extends BaseResource {

    /**
     * <b>API link</b>: <a href="http://confluence.eict.vpdc/display/JUPU/REST+API#RESTAPI-Getissuetypes">Get issue types</a><br />
     * <b>Pagination</b>: false<br />
     * <b>Mandatory parameters</b>: none<br />
     * <b>Optional parameters</b>: none<br />
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_ISSUE)
    public Response getIssueTypes(){
        List<IssueType> issueTypes = getIssueService().query(IssueType.class).select(Condition.TRUE);
        return ok(issueTypes, IssueTypeInfo.class).build();
    }
}
