/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v2;

import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PropertyCopier;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.elster.jupiter.rest.api.util.v1.hypermedia.SelectableFieldFactory;

import javax.inject.Inject;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class AlarmStatusInfoFactory extends SelectableFieldFactory<AlarmStatusInfo, IssueStatus> {


    @Inject
    public AlarmStatusInfoFactory() {
    }

    public LinkInfo asLink(IssueStatus alarmStatus, Relation relation, UriInfo uriInfo) {
        AlarmStatusInfo info = new AlarmStatusInfo();
        copySelectedFields(info, alarmStatus, uriInfo, Arrays.asList("id", "version"));
        info.link = link(alarmStatus, relation, uriInfo);
        return info;
    }

    public List<LinkInfo> asLink(Collection<? extends IssueStatus> alarmStatuses, Relation relation, UriInfo uriInfo) {
        return alarmStatuses.stream().map(i -> asLink(i, relation, uriInfo)).collect(toList());
    }

    private Link link(IssueStatus alarmStatus, Relation relation, UriInfo uriInfo) {
        return Link.fromUriBuilder(getUriBuilder(uriInfo))
                .rel(relation.rel())
                .title("AlarmStatus")
                .build(alarmStatus.getId());
    }


    private Link linkClearedStatus(IssueStatus alarmStatus, Relation relation, UriInfo uriInfo) {
        return Link.fromUriBuilder(getUriBuilder(uriInfo))
                .rel(relation.rel())
                .title("AlarmStatus")
                .build(alarmStatus.getId());
    }

    private UriBuilder getUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                .path(AlarmResource.class)
                .path(AlarmResource.class, "getStatus");
    }

    public AlarmStatusInfo from(IssueStatus alarmStatus, UriInfo uriInfo, Collection<String> fields) {
        AlarmStatusInfo info = new AlarmStatusInfo();
        copySelectedFields(info, alarmStatus, uriInfo, fields);
        return info;
    }

    @Override
    protected Map<String, PropertyCopier<AlarmStatusInfo, IssueStatus>> buildFieldMap() {
        Map<String, PropertyCopier<AlarmStatusInfo, IssueStatus>> map = new HashMap<>();
        map.put("id", (alarmStatusInfo, alarmStatus, uriInfo) -> alarmStatusInfo.id = alarmStatus.getKey());
        map.put("name", (alarmStatusInfo, alarmStatus, uriInfo) -> alarmStatusInfo.name = alarmStatus.getName());
        map.put("clearedStatus", (alarmStatusInfo, alarmStatus, uriInfo) -> alarmStatusInfo.clearedStatus = false);
        return map;
    }
}
