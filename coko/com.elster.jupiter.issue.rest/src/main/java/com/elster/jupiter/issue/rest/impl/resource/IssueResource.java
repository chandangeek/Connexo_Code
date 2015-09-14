package com.elster.jupiter.issue.rest.impl.resource;

import com.elster.jupiter.issue.rest.resource.StandardParametersBean;
import com.elster.jupiter.issue.rest.response.IssueAssigneeInfo;
import com.elster.jupiter.issue.rest.response.IssueAssigneeInfoAdapter;
import com.elster.jupiter.issue.rest.response.IssueGroupInfo;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.issue.share.entity.*;
import com.elster.jupiter.issue.share.service.IssueGroupFilter;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Path("/issues")
public class IssueResource extends BaseResource {

    @GET
    @Path("/groupedlist")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_ISSUE, Privileges.ASSIGN_ISSUE, Privileges.CLOSE_ISSUE, Privileges.COMMENT_ISSUE, Privileges.ACTION_ISSUE})
    public PagedInfoList getGroupedList(@BeanParam StandardParametersBean params, @BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter filter) {
        IssueGroupFilter groupFilter = new IssueGroupFilter();
        IssueAssigneeInfo issueAssigneeInfo = filter.getProperty("assignee", new IssueAssigneeInfoAdapter());
        groupFilter.using(getQueryApiClass(filter)) // Issues, Historical Issues or Both
              .onlyGroupWithKey(filter.getString("reason"))  // Reason id
              .withIssueType(filter.getString("issueType")) // Reasons only with specific issue type
              .withStatuses(filter.getStringList("status")) // All selected statuses
              .withAssignee(issueAssigneeInfo.getId(), issueAssigneeInfo.getType())
              .withMeterMrid(filter.getString("meter")) // Filter by meter MRID
              .groupBy(filter.getString("field")) // Main grouping column
              .setAscOrder(false) // Sorting (descending direction)
              .from(params.getFrom()).to(params.getTo()); // Pagination
        List<IssueGroup> resultList = getIssueService().getIssueGroupList(groupFilter);
        List<IssueGroupInfo> infos = resultList.stream().map(IssueGroupInfo::new).collect(Collectors.toList());
        return PagedInfoList.fromPagedList("issueGroups", infos, queryParameters);
    }

    private Class<? extends Issue> getQueryApiClass(JsonQueryFilter filter) {
        List<IssueStatus> statuses = filter.hasProperty("status")
                ? filter.getStringList("status").stream().map(s -> getIssueService().findStatus(s).get()).collect(Collectors.toList())
                : Collections.EMPTY_LIST;
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
