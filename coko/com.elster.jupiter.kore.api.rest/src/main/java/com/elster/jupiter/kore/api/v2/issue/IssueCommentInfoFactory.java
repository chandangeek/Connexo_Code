/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v2.issue;

import com.elster.jupiter.issue.share.entity.IssueComment;
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

public class IssueCommentInfoFactory extends SelectableFieldFactory<IssueCommentInfo, IssueComment> {

    private final UserInfoFactory userInfoFactory;

    @Inject
    public IssueCommentInfoFactory(UserInfoFactory userInfoFactory) {
        this.userInfoFactory = userInfoFactory;
    }

    public LinkInfo asLink(IssueComment issueComment, Relation relation, UriInfo uriInfo) {
        IssueCommentInfo info = new IssueCommentInfo();
        copySelectedFields(info, issueComment, uriInfo, Arrays.asList("id", "version"));
        info.link = link(issueComment, relation, uriInfo);
        return info;
    }

    public List<LinkInfo> asLink(Collection<? extends IssueComment> issueComments, Relation relation, UriInfo uriInfo) {
        return issueComments.stream().map(i -> asLink(i, relation, uriInfo)).collect(toList());
    }

    private Link link(IssueComment issueComment, Relation relation, UriInfo uriInfo) {
        return Link.fromUriBuilder(getUriBuilder(uriInfo))
                .rel(relation.rel())
                .title("IssueComment")
                .build(issueComment.getId());
    }

    private UriBuilder getUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                .path(IssueResource.class)
                .path(IssueResource.class, "getIssueComments");
    }

    public IssueCommentInfo from(IssueComment issueComment, UriInfo uriInfo, Collection<String> fields) {
        IssueCommentInfo info = new IssueCommentInfo();
        copySelectedFields(info, issueComment, uriInfo, fields);
        return info;
    }

    @Override
    protected Map<String, PropertyCopier<IssueCommentInfo, IssueComment>> buildFieldMap() {
        Map<String, PropertyCopier<IssueCommentInfo, IssueComment>> map = new HashMap<>();
        map.put("id", (issueCommentInfo, issueComment, uriInfo) -> issueCommentInfo.id = issueComment.getId());
        map.put("comment", (issueCommentInfo, issueComment, uriInfo) -> issueCommentInfo.comment = issueComment.getComment());
        map.put("creationDate", (issueCommentInfo, issueComment, uriInfo) -> issueCommentInfo.creationDate = issueComment.getCreateTime().toEpochMilli());
        map.put("version", (issueCommentInfo, issueComment, uriInfo) -> issueCommentInfo.version = issueComment.getVersion());
        map.put("author", (issueCommentInfo, issueComment, uriInfo) -> issueCommentInfo.author = userInfoFactory.asInfo(issueComment.getUser()));

        return map;
    }
}
