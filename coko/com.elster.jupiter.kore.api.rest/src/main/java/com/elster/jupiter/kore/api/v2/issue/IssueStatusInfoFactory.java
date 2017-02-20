/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v2.issue;

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

public class IssueStatusInfoFactory extends SelectableFieldFactory<IssueStatusInfo, IssueStatus> {


    @Inject
    public IssueStatusInfoFactory() {
    }

    public LinkInfo asLink(IssueStatus issueStatus, Relation relation, UriInfo uriInfo) {
        IssueStatusInfo info = new IssueStatusInfo();
        copySelectedFields(info, issueStatus, uriInfo, Arrays.asList("id", "version"));
        info.link = link(issueStatus, relation, uriInfo);
        return info;
    }

    public List<LinkInfo> asLink(Collection<? extends IssueStatus> issueStatuses, Relation relation, UriInfo uriInfo) {
        return issueStatuses.stream().map(i -> asLink(i, relation, uriInfo)).collect(toList());
    }
    private Link link(IssueStatus issueStatus, Relation relation, UriInfo uriInfo) {
        return Link.fromUriBuilder(getUriBuilder(uriInfo))
                .rel(relation.rel())
                .title("IssueStatus")
                .build(issueStatus.getId());
    }

    private UriBuilder getUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                .path(IssueResource.class)
                .path(IssueResource.class, "getStatus");
    }

    public IssueStatusInfo from(IssueStatus issueStatus, UriInfo uriInfo, Collection<String> fields) {
        IssueStatusInfo info  = new IssueStatusInfo();
        copySelectedFields(info, issueStatus, uriInfo, fields);
        return info;
    }

    @Override
    protected Map<String, PropertyCopier<IssueStatusInfo, IssueStatus>> buildFieldMap() {
        Map<String, PropertyCopier<IssueStatusInfo, IssueStatus>> map = new HashMap<>();
        map.put("id", (issueStatusInfo, issueStatus, uriInfo) -> issueStatusInfo.id = issueStatus.getKey());
        map.put("name", (issueStatusInfo, issueStatus, uriInfo) -> issueStatusInfo.name = issueStatus.getName());
        return map;
    }
}
