package com.elster.jupiter.issue.rest.impl.resource;

import com.elster.jupiter.issue.rest.resource.StandardParametersBean;
import com.elster.jupiter.issue.rest.response.IssueGroupInfo;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.issue.share.entity.*;
import com.elster.jupiter.issue.share.service.IssueGroupFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

@Path("/issues")
public class IssueResource extends BaseResource {

    @GET
    @Path("/groupedlist")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_ISSUE, Privileges.ASSIGN_ISSUE, Privileges.CLOSE_ISSUE, Privileges.COMMENT_ISSUE, Privileges.ACTION_ISSUE})
    public PagedInfoList getGroupedList(@BeanParam StandardParametersBean params, @BeanParam JsonQueryParameters queryParameters) {
        IssueGroupFilter filter = new IssueGroupFilter();
        filter.using(getQueryApiClass(params)) // Issues, Historical Issues or Both
              .onlyGroupWithKey(params.getFirst("id")) // Reason id
              .withIssueType(params.getFirst("issueType")) // Reasons only with specific issue type
              .withStatuses(params.get("status")) // All selected statuses
              .withAssigneeType(params.getFirst("assigneeType")) // User, Group or Role type of assignee
              .withAssigneeId(params.getFirstLong("assigneeId")) // Id of selected assignee
              .withMeterMrid(params.getFirst("meter")) // Filter by meter MRID
              .groupBy(params.getFirst("field")) // Main grouping column
              .setAscOrder(false) // Sorting (descending direction)
              .from(params.getFrom()).to(params.getTo()); // Pagination
        List<IssueGroup> resultList = getIssueService().getIssueGroupList(filter);
        List<IssueGroupInfo> infos = resultList.stream().map(IssueGroupInfo::new).collect(Collectors.toList());
        return PagedInfoList.fromPagedList("issueGroups", infos, queryParameters);
    }

    private Class<? extends Issue> getQueryApiClass(StandardParametersBean params) {
        List<IssueStatus> statuses = params.get("status").stream().map(s -> getIssueService().findStatus(s).get()).collect(Collectors.toList());
        if (statuses.isEmpty()) {
            return Issue.class;
        }
        if (statuses.stream().allMatch(status -> !status.isHistorical())) {
            return OpenIssue.class;
        }
        if (statuses.stream().allMatch(IssueStatus::isHistorical)) {
            return HistoricalIssue.class;
        }
        return Issue.class;
    }
}
