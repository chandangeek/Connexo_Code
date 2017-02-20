/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.rest.resource;

import com.elster.jupiter.issue.rest.resource.IssueRestModuleConst;
import com.elster.jupiter.issue.share.IssueGroupFilter;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueGroup;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.energyict.mdc.device.alarms.security.Privileges;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Path("/history")
public class HistoryResource extends BaseAlarmResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ALARM})
    public Response getAlarmHistory(@BeanParam JsonQueryFilter filter) {
        long endCreateTime = getEndDate();

        IssueGroupFilter groupFilter = getIssueService().newIssueGroupFilter();
        groupFilter.using(Issue.class)
                .withReasons(filter.getStringList(IssueRestModuleConst.REASON))
                .groupBy(filter.getString(IssueRestModuleConst.FIELD))
                .to(endCreateTime);

        List<IssueGroup> groupedResult = getDeviceAlarmService().getDeviceAlarmGroupList(groupFilter);
        JSONObject response = getResponse(groupedResult);
        return Response.ok(response.toJSONString(), MediaType.APPLICATION_JSON).build();
    }

    private long getEndDate() {
        // calculate next day in UTC
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.DATE, 1);
        endDate.set(Calendar.HOUR_OF_DAY, 0);
        endDate.set(Calendar.MINUTE, 0);
        endDate.set(Calendar.SECOND, 0);
        endDate.set(Calendar.MILLISECOND, 0);
        return endDate.getTimeInMillis();
    }

    private JSONObject getResponse(List<IssueGroup> groupedResult) {
        JSONArray data = new JSONArray();
        Stream<Map.Entry<BigDecimal, List<IssueGroup>>> sortedData = groupedResult.stream().collect(
                Collectors.groupingBy(issueGroup -> {
                    return (BigDecimal) issueGroup.getGroupKey();
                })).
                entrySet().stream().sorted(Map.Entry.<BigDecimal, List<IssueGroup>>comparingByKey());

        sortedData.forEach(o -> {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("date", o.getKey().toString());
            o.getValue().forEach(issueGroup -> {
                if (issueGroup.getGroupName() != null) {
                    jsonObject.put(issueGroup.getGroupName(), issueGroup.getCount());
                }
            });
            data.add(jsonObject);
        });

        List<String> reasons = groupedResult.stream()
                .filter(issueGroup -> issueGroup.getGroupName() != null)
                .map(IssueGroup::getGroupName)
                .distinct().collect(Collectors.toList());
        Collections.sort(reasons, (p1, p2) -> p1.compareToIgnoreCase(p2));

        JSONObject response = new JSONObject();
        response.put("fields", reasons);
        response.put("data", data);
        return response;
    }


}
