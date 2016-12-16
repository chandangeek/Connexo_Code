package com.energyict.mdc.device.alarms.rest.resource;


import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.response.IssueStatusInfo;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.alarms.security.Privileges;

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
import java.util.stream.Collectors;

//FixMe do we need new statuses for alarms?
@Path("/statuses")
public class StatusResource  extends BaseAlarmResource{


    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ALARM,Privileges.Constants.ASSIGN_ALARM,Privileges.Constants.CLOSE_ALARM,Privileges.Constants.COMMENT_ALARM,Privileges.Constants.ACTION_ALARM})
    public Response getStatuses() {
        Query<IssueStatus> query = getIssueService().query(IssueStatus.class);
        List<IssueStatus> statuses = query.select(Condition.TRUE);
        return Response.ok().entity(statuses.stream().map(IssueStatusInfo::new).collect(Collectors.toList())).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ALARM,Privileges.Constants.ASSIGN_ALARM,Privileges.Constants.CLOSE_ALARM,Privileges.Constants.COMMENT_ALARM,Privileges.Constants.ACTION_ALARM})
    public Response getStatus(@PathParam("id") String key){
        Optional<IssueStatus> statusRef = getIssueService().findStatus(key);
        if(!statusRef.isPresent()){
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return Response.ok().entity(new IssueStatusInfo(statusRef.get())).build();
    }


}
