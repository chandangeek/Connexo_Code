/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.share;

import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;

import aQute.bnd.annotation.ConsumerType;

/**
 * Created by H165696 on 7/26/2017.
 */

@ConsumerType
public interface IssueWebServiceClient {
    /**
     * Get the registered web service name
     *
     * @return web service name
     */
    String getWebServiceName();

    /**
     * Invoked by the alarm/issue framework when a new alarm/issue was created
     *
     * @param issue - issue object
     * @param endPointConfiguration - end point configuration
     * @return true for success
     */
    boolean call(Issue issue, EndPointConfiguration endPointConfiguration);
}
