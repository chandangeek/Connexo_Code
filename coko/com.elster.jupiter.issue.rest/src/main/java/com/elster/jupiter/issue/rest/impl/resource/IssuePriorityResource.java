package com.elster.jupiter.issue.rest.impl.resource;


import com.elster.jupiter.issue.rest.MessageSeeds;
import com.elster.jupiter.issue.rest.request.SetPriorityRequest;
import com.elster.jupiter.issue.rest.response.ActionInfo;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.Transactional;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/{id}/priority")
public class IssuePriorityResource extends BaseResource{

    private final ConcurrentModificationExceptionFactory conflictFactory;

    @Inject
    public IssuePriorityResource(ConcurrentModificationExceptionFactory conflictFactory){
        this.conflictFactory = conflictFactory;
    }


    @PUT
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ACTION_ISSUE)
    public ActionInfo setPriority(@PathParam("id") long id, @BeanParam JsonQueryParameters queryParameters, SetPriorityRequest request) {
        ActionInfo actionInfo = new ActionInfo();
        Issue issue = getIssueService().findAndLockIssueByIdAndVersion(id, request.issue.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(request.issue.title)
                        .withActualVersion(() -> getIssueService().findIssue(id)
                                .map(Issue::getVersion)
                                .orElse(null))
                        .supplier());
        issue.setPriority(Priority.get(request.priority.urgency, request.priority.impact));
        issue.update();
        actionInfo.addSuccess(issue.getId(), getThesaurus().getFormat(MessageSeeds.ACTION_ISSUE_PRIORITY_WAS_SET).format());
        return actionInfo;
    }

}
