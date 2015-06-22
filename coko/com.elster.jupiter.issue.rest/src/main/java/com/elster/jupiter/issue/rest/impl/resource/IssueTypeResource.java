package com.elster.jupiter.issue.rest.impl.resource;

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

import static com.elster.jupiter.issue.rest.response.ResponseHelper.entity;

@Path("/issuetypes")
public class IssueTypeResource extends BaseResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_ISSUE, Privileges.ASSIGN_ISSUE, Privileges.CLOSE_ISSUE, Privileges.COMMENT_ISSUE, Privileges.ACTION_ISSUE})
    public Response getIssueTypes() {
        List<IssueType> issueTypes = getIssueService().query(IssueType.class).select(Condition.TRUE);
        issueTypes.sort((it1, it2) -> it1.getName().compareToIgnoreCase(it2.getName()));
        return entity(issueTypes, IssueTypeInfo.class).build();
    }
}
