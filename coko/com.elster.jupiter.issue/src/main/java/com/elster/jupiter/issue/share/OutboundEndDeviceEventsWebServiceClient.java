/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.share;

import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;

public interface OutboundEndDeviceEventsWebServiceClient {

    String NAME = "OutboundEndDeviceEvents";

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
     */
    void call(Issue issue, EndPointConfiguration endPointConfiguration);
}
