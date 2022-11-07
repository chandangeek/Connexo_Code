/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.issue.impl.event;


import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.json.JsonService;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name = "com.elster.jupiter.issue.impl.event.BulkCloseIssueHandlerFactory",
        service = MessageHandlerFactory.class,
        property = {"subscriber=" + BulkCloseIssueHandlerFactory.AQ_BULK_ISSUE_CLOSE_EVENT_SUBSC, "destination=" + BulkCloseIssueHandlerFactory.AQ_BULK_ISSUE_CLOSE_EVENT_DEST},
        immediate = true)
public class BulkCloseIssueHandlerFactory implements MessageHandlerFactory {
    public static final String AQ_BULK_ISSUE_CLOSE_EVENT_DEST = EventService.JUPITER_EVENTS;

    public static final String AQ_BULK_ISSUE_CLOSE_EVENT_SUBSC = "BulkCloseIssues";

    public static final String AQ_BULK_ISSUE_CLOSE_EVENT_DISPLAYNAME = "Bulk issue closing";

    private volatile JsonService jsonService;

    private volatile IssueService issueService;

    private volatile UserService userService;

    public BulkCloseIssueHandlerFactory() {
        super();
    }

    @Inject
    public BulkCloseIssueHandlerFactory(JsonService jsonService,
                                        IssueService issueService, UserService userService) {
        this();
        setJsonService(jsonService);
        setIssueService(issueService);
        setUserService(userService);
    }

    @Override
    public MessageHandler newMessageHandler() {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(JsonService.class).toInstance(jsonService);
                bind(IssueService.class).toInstance(issueService);
                bind(UserService.class).toInstance(userService);
            }
        });
        return new BulkCloseIssueHandler(injector);
    }

    @Reference
    public final void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public final void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

}
