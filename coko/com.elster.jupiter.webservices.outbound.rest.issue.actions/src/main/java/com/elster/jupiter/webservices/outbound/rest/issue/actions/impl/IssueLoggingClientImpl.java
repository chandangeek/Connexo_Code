/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservices.outbound.rest.issue.actions.impl;
import com.elster.jupiter.issue.share.IssueWebServiceClient;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;

import javax.ws.rs.client.WebTarget;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IssueLoggingClientImpl implements IssueWebServiceClient {

    private static final Logger LOGGER = Logger.getLogger(IssueLoggingClientImpl.class.getName());
    private volatile WebTarget target;

    public IssueLoggingClientImpl(WebTarget target) {
        this.target = target;
    }

    @Override
    public String getWebServiceName(){
        return "Issue logging";
    }

    @Override
    public boolean call(Issue issue, EndPointConfiguration endPointConfiguration)
    {
        try {
            StringBuilder comment = new StringBuilder();
            comment.append(issue.getIssueId() + ": " + issue.getTitle() + " has been generated ");
            comment.append("for " + issue.getDevice().getName() + " device; ");
            comment.append("issue created on " + issue.getCreateDateTime().toString());
            LOGGER.log(Level.INFO, comment.toString());

            // add comment
            issue.addComment(String.format("%s: %s has been forwarded to the remote system.", issue.getIssueId(), issue.getTitle()), issue.getAssignee().getUser());
        } catch (RuntimeException e) {
            return false;
        }
        return true;
    }
}
