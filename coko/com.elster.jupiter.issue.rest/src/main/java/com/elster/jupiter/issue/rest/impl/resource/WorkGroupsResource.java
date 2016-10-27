package com.elster.jupiter.issue.rest.impl.resource;

import com.elster.jupiter.issue.rest.resource.StandardParametersBean;
import com.elster.jupiter.issue.rest.response.WorkGroupInfo;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.util.List;
import java.util.stream.Collectors;

import static com.elster.jupiter.issue.rest.TranslationKeys.ISSUE_ASSIGNEE_UNASSIGNED;

@Path("/workgroups")
public class WorkGroupsResource extends BaseResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE, Privileges.Constants.ASSIGN_ISSUE, Privileges.Constants.CLOSE_ISSUE, Privileges.Constants.COMMENT_ISSUE, Privileges.Constants.ACTION_ISSUE})
    public PagedInfoList getAllWorkGroupAssignees(@BeanParam StandardParametersBean params, @BeanParam JsonQueryParameters queryParameters, @Context SecurityContext securityContext) {
        if (params.getUriInfo().getQueryParameters().getFirst("myworkgroups") != null && params.getUriInfo()
                .getQueryParameters()
                .getFirst("myworkgroups")
                .equals("true")) {
            return PagedInfoList.fromCompleteList("workgroups", getUserService().getWorkGroups()
                    .stream()
                    .filter(workGroup -> workGroup.getUsersInWorkGroup()
                            .stream()
                            .anyMatch(user -> user.equals(securityContext.getUserPrincipal())))
                    .map(WorkGroupInfo::new)
                    .sorted((first, second) -> first.name.toLowerCase().compareTo(second.name.toLowerCase()))
                    .collect(Collectors.toList()), queryParameters);
        } else {
            List<WorkGroupInfo> workGroupInfos = getUserService().getWorkGroups()
                    .stream()
                    .map(WorkGroupInfo::new)
                    .sorted((first, second) -> first.name.toLowerCase().compareTo(second.name.toLowerCase()))
                    .collect(Collectors.toList());
            workGroupInfos.add(0, new WorkGroupInfo(getThesaurus().getFormat(ISSUE_ASSIGNEE_UNASSIGNED).format()));
            return PagedInfoList.fromCompleteList("workgroups", workGroupInfos, queryParameters);
        }
    }
}
