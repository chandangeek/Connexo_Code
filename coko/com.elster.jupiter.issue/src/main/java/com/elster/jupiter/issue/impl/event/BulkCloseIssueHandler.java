/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.issue.impl.event;

import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.json.JsonService;

import com.google.inject.Injector;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BulkCloseIssueHandler implements MessageHandler {

    private static final Logger LOGGER = Logger.getLogger(BulkCloseIssueHandler.class.getName());

    private final JsonService jsonService;
    private final IssueService issueService;
    private final UserService userService;

    public BulkCloseIssueHandler(Injector injector) {
        this.jsonService = injector.getInstance(JsonService.class);
        this.issueService = injector.getInstance(IssueService.class);
        this.userService = injector.getInstance(UserService.class);
    }

    @Override
    public void process(Message message) {
        Map<String, Object> map = jsonService.deserialize(message.getPayload(), Map.class);
        if (map.get("event.topics").equals("com/elster/jupiter/issues/BULK_CLOSE")) {
            LOGGER.info("[BulkCloseIssueHandler] Closing has started");
            Optional<User> user = userService.findUser(String.valueOf(map.get("user")));
            Optional<IssueStatus> status = issueService.findStatus(String.valueOf(map.get("issueStatus")));
            List<Number> issuesId = (List<Number>) map.get("issueIds");
            String comment = (String) map.get("comment");
            if (status.isPresent() && user.isPresent()) {
                for (Number issueId : issuesId) {
                    Optional<OpenIssue> openIssue = issueService.findOpenIssue(issueId.longValue());
                    if (openIssue.isPresent()) {
                        OpenIssue issue = openIssue.get();
                        if (comment != null) {
                            issue.addComment(comment, user.get());
                        }
                        try {
                            LOGGER.fine("[BulkCloseIssueHandler] Trying to close issue with id=" + issue.getId());
                            issue.close(status.get());
                        } catch (Exception e) {
                            LOGGER.log(Level.SEVERE, "[BulkCloseIssueHandler] Issue with id=" + issue.getId() + " could not be closed", e);
                        }
                    }
                }
            } else {
                if (!status.isPresent()) {
                    LOGGER.log(Level.SEVERE, "[BulkCloseIssueHandler] Can't find status=" + map.get("issueStatus") + " from message to run");
                }
                if (!user.isPresent()) {
                    LOGGER.log(Level.SEVERE, "[BulkCloseIssueHandler] Can't find user=" + map.get("user") + " from message to run");
                }
            }
            LOGGER.info("[BulkCloseIssueHandler] Closing process was finished");
        }
    }


}
