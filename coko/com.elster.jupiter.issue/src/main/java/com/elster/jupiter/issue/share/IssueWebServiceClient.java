/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.share;

import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;

/**
 * Created by H165696 on 7/26/2017.
 */
public interface IssueWebServiceClient {
    String getWebServiceName();

    boolean call(Issue issue, EndPointConfiguration endPointConfiguration);
}
