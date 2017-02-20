/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v2.issue;

import com.elster.jupiter.issue.share.entity.IssueComment;
import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.users.User;

public class IssueCommentInfo extends LinkInfo<Long> {
    public long id;
    public String comment;
    public UserInfo author;
    public Long creationDate;
    public Long version;


}
