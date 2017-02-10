package com.energyict.mdc.device.alarms.rest.resource;


import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.resource.StandardParametersBean;
import com.elster.jupiter.issue.rest.response.IssueReasonInfo;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.alarms.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.BeanParam;
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

@Path("/reasons")
public class ReasonResource extends BaseAlarmResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ALARM,Privileges.Constants.ASSIGN_ALARM,Privileges.Constants.CLOSE_ALARM,Privileges.Constants.COMMENT_ALARM,Privileges.Constants.ACTION_ALARM})
    public Response getReasons(@BeanParam StandardParametersBean params) {
        Condition condition = Condition.TRUE;
        Query<IssueReason> query = getIssueService().query(IssueReason.class);
        List<IssueReason> reasons = query.select(condition).stream()
                .filter(reason -> reason.getIssueType().getKey().toLowerCase().equals("devicealarm"))
                .sorted((first, second) -> first.getName().toLowerCase().compareToIgnoreCase(second.getName().toLowerCase()))
                .collect(Collectors.toList());
        if (params.getFirst("like") != null) {
            reasons = reasons.stream().filter(reason -> reason.getName().toLowerCase().contains(params.getFirst("like").toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Response.ok().entity(reasons.stream().map(IssueReasonInfo::new).collect(Collectors.toList())).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ALARM,Privileges.Constants.ASSIGN_ALARM,Privileges.Constants.CLOSE_ALARM,Privileges.Constants.COMMENT_ALARM,Privileges.Constants.ACTION_ALARM})
    public Response getReason(@PathParam("id") String key){
        Optional<IssueReason> reasonRef = getIssueService().findReason(key);
        if(!reasonRef.isPresent()){
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return Response.ok().entity(new IssueReasonInfo(reasonRef.get())).build();
    }

}
